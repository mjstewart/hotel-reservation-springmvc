package com.demo.reservation.flow;

import com.demo.TimeProvider;
import com.demo.domain.ReservationDates;
import com.demo.domain.Room;
import com.demo.exceptions.NotFoundException;
import com.demo.persistance.RoomRepository;
import com.demo.reservation.flow.forms.DateForm;
import com.demo.reservation.flow.forms.GuestForm;
import com.demo.reservation.flow.forms.ReservationFlow;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.validation.Valid;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Controller
@SessionAttributes("reservationFlow")
public class ReservationController {

    private RoomRepository roomRepository;
    private TimeProvider timeProvider;

    public ReservationController(RoomRepository roomRepository, TimeProvider timeProvider) {
        this.roomRepository = roomRepository;
        this.timeProvider = timeProvider;
    }

    /**
     * Since {@code reservationFlowForms} is used in the {@code SessionAttributes} on the controller level, it informs
     * spring to treat our {@code ReservationFlow} as session scoped. This method will be invoked the very first
     * HTTP request to populate the session with the new object.
     * <p>Subsequent HTTP requests will go directly to the handler and the statement {@code @ModelAttribute("reservationFlowForms")}
     * will grab the object directly from the session.
     */
    @ModelAttribute("reservationFlow")
    public ReservationFlow getReservationFlow() {
        System.out.println("getReservationFlow()");
        return new ReservationFlow();
    }

    /**
     * Entry point to begin the reservation flow.
     */
    @GetMapping("/reservation")
    public String getDateForm(@RequestParam(value = "roomId") Long roomId,
                              @ModelAttribute ReservationFlow reservationFlow) throws NotFoundException {

        Optional<Room> maybeRoom = roomRepository.findById(roomId);
        if (!maybeRoom.isPresent()) {
            throw new NotFoundException();
        }

        reservationFlow.getReservation().setRoom(maybeRoom.get());

        return "reservation/dates";
    }

    /**
     * BindingResult must be directly after the @Valid object.
     * https://stackoverflow.com/questions/30297719/cannot-get-validation-working-with-spring-boot-and-thymeleaf/30298348
     *
     * @Valid @ModelAttribute DateForm dateForm - means the post request contains the form body which will spring will create
     * into a domain object for us and validate it using the JPA annotations.
     */
    @PostMapping("/reservation/dates")
    public String dates(@Valid @ModelAttribute ReservationFlow reservationFlow,
                        BindingResult bindingResult,
                        RedirectAttributes ra) {

        if (bindingResult.hasErrors()) {
            return "reservation/dates";
        }

        Optional<ReservationDates.ValidationError> validationError =
                reservationFlow.getReservation().getDates().validate(timeProvider.localDate());

        if (validationError.isPresent()) {
            bindingResult.rejectValue("reservation.dates", validationError.get().getCode(),
                    validationError.get().getReason());
            return "reservation/dates";
        }

        ra.addFlashAttribute("reservationFlow", reservationFlow);
        return "redirect:/reservation/guests";
    }

    /**
     * No need to validate since errors are implied through a negative total night duration which simplifies the logic.
     */
    @PostMapping(value = "/reservation/dates", params = "prices")
    public String roomCostFragment(@ModelAttribute ReservationFlow reservationFlow) {
        return "reservation/fragments :: roomCosts";
    }

    /**
     * GET Step 2
     */
    @GetMapping("/reservation/guests")
    public String getGuestForm(@ModelAttribute ReservationFlow reservationFlow, Model model) {
        reservationFlow.incrementStep();
        model.addAttribute("guestForm", new GuestForm());
        return "reservation/guests";
    }
}
