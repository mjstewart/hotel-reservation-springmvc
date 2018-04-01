package com.demo.reservation.flow;

import com.demo.TimeProvider;
import com.demo.domain.Guest;
import com.demo.domain.ReservationDates;
import com.demo.domain.Room;
import com.demo.exceptions.NotFoundException;
import com.demo.persistance.RoomRepository;
import com.demo.reservation.flow.forms.ReservationFlow;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.validation.Valid;
import java.util.Optional;

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
        return new ReservationFlow();
    }

    // Flow step 1

    /**
     * Entry point to begin the reservation flow.
     */
    @GetMapping("/reservation")
    public String getDateForm(@RequestParam(value = "roomId") Long roomId,
                              @ModelAttribute("reservationFlow") ReservationFlow reservationFlow) throws NotFoundException {

        Optional<Room> maybeRoom = roomRepository.findById(roomId);
        if (!maybeRoom.isPresent()) {
            throw new NotFoundException();
        }

        reservationFlow.getReservation().setRoom(maybeRoom.get());

        return "reservation/dates";
    }

    @GetMapping("/reservation/dates")
    public String dates(@ModelAttribute("reservationFlow") ReservationFlow reservationFlow) {
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
    public String dates(@Valid @ModelAttribute("reservationFlow") ReservationFlow reservationFlow,
                        BindingResult bindingResult,
                        RedirectAttributes redirectAttributes) {

        if (bindingResult.hasErrors()) {
            return "reservation/dates";
        }

        Optional<ReservationDates.ValidationError> validationError =
                reservationFlow.getReservation().getDates().validate(timeProvider.localDate());

        if (validationError.isPresent()) {
            // Must be a field rejection as the dates form is bound to the property path reservation.dates.
            bindingResult.rejectValue("reservation.dates", validationError.get().getCode(),
                    validationError.get().getReason());
            return "reservation/dates";
        }

        reservationFlow.completeStep(ReservationFlow.Step.Dates);
        redirectAttributes.addFlashAttribute("reservationFlow", reservationFlow);
        return "redirect:/reservation/guests";
    }

    /**
     * No need to validate since errors are implied through a negative total night duration which simplifies the logic.
     */
    @PostMapping(value = "/reservation/dates", params = "prices")
    public String roomCostFragment(@ModelAttribute("reservationFlow") ReservationFlow reservationFlow) {
        return "reservation/fragments :: roomCosts";
    }

    // Flow step 2

    @GetMapping("/reservation/guests")
    public String getGuestForm(@ModelAttribute("reservationFlow") ReservationFlow reservationFlow, Model model) {
        reservationFlow.setActive(ReservationFlow.Step.Guests);
        model.addAttribute("guest", new Guest());
        return "reservation/guests";
    }

    @PostMapping(value = "/reservation/guests", params = "back")
    public String postGuestFormGoBack(@ModelAttribute("reservationFlow") ReservationFlow reservationFlow,
                                      RedirectAttributes redirectAttributes) {
        reservationFlow.incompleteStep(ReservationFlow.Step.Guests);
        redirectAttributes.addFlashAttribute("reservationFlow", reservationFlow);
        return "redirect:/reservation/dates";
    }

    @PostMapping(value = "/reservation/guests", params = "addGuest")
    public String postAddGuest(@Valid @ModelAttribute("guest") Guest guest,
                               BindingResult bindingResult,
                               @ModelAttribute("reservationFlow") ReservationFlow reservationFlow,
                               Model model) {

        if (bindingResult.hasErrors()) {
            return "reservation/guests";
        }

        if (reservationFlow.getReservation().getGuests().contains(guest)) {
            bindingResult.reject("exists", "A guest with this name already exists");
            return "reservation/guests";
        }

        if (reservationFlow.getReservation().isRoomFull()) {
            bindingResult.reject("guestLimitExceeded", "This room has the maximum number of guests");
            return "reservation/guests";
        }

        reservationFlow.getReservation().addGuest(guest);

        // create a new guest to rebind to the guest form
        model.addAttribute("guest", new Guest());

        return "reservation/guests";
    }

    /**
     * Note how {@code Errors} is used rather than {@code BindingResult}. This is because no bean validation is being
     * performed as this end point is for posting the intention to commit all current guests.
     */
    @PostMapping(value = "/reservation/guests")
    public String postGuestToExtras(@ModelAttribute(binding = false) Guest guest,
                                    Errors errors,
                                    @ModelAttribute ReservationFlow reservationFlow,
                                    RedirectAttributes redirectAttributes) {

        if (reservationFlow.getReservation().getGuests().isEmpty()) {
            errors.reject("guests.noneExist", "There must be at least 1 guest");
            return "reservation/guests";
        }

        redirectAttributes.addFlashAttribute("reservationFlow", reservationFlow);

        reservationFlow.completeStep(ReservationFlow.Step.Guests);
        return "redirect:/reservation/extras";
    }


    // Flow step 3

    @GetMapping("/reservation/extras")
    public String getExtrasForm(@ModelAttribute("reservationFlow") ReservationFlow reservationFlow, Model model) {
        reservationFlow.setActive(ReservationFlow.Step.Extras);
//        model.addAttribute("guest", new Guest());
        return "reservation/extras";
    }
}
