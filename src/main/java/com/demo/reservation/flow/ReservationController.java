package com.demo.reservation.flow;

import com.demo.TimeProvider;
import com.demo.domain.*;
import com.demo.exceptions.NotFoundException;
import com.demo.persistance.RoomRepository;
import com.demo.reservation.ExtraRepository;
import com.demo.reservation.flow.forms.ReservationFlow;
import com.demo.reservation.testcheckboxes.Drink;
import com.demo.reservation.testcheckboxes.EnumDrink;
import com.demo.reservation.testcheckboxes.Person;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.Errors;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.support.SessionStatus;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.validation.Valid;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Controller
@SessionAttributes("reservationFlow")
public class ReservationController {

    private RoomRepository roomRepository;
    private ExtraRepository extraRepository;
    private TimeProvider timeProvider;

    public ReservationController(RoomRepository roomRepository,
                                 ExtraRepository extraRepository,
                                 TimeProvider timeProvider) {
        this.roomRepository = roomRepository;
        this.extraRepository = extraRepository;
        this.timeProvider = timeProvider;
    }

    /**
     * Since {@code reservationFlow} is used in the {@code SessionAttributes} on the controller level, it informs
     * spring to treat our {@code ReservationFlow} as session scoped. This method will be invoked the very first
     * HTTP request to populate the session with the new object.
     * <p>Subsequent HTTP requests will go directly to the handler and the statement
     * {@code @ModelAttribute("reservationFlow")} will grab the object directly from the session rather
     * than recreating it.
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
                              @ModelAttribute("reservationFlow") ReservationFlow reservationFlow)
            throws NotFoundException {
        reservationFlow.enterStep(ReservationFlow.Step.Dates);

        Optional<Room> maybeRoom = roomRepository.findById(roomId);
        if (!maybeRoom.isPresent()) {
            throw new NotFoundException();
        }

        maybeRoom.get().setReservation(reservationFlow.getReservation());

        return "reservation/dates";
    }

    /**
     * BindingResult must be directly after the @Valid object.
     * https://stackoverflow.com/questions/30297719/cannot-get-validation-working-with-spring-boot-and-thymeleaf/30298348
     * <p>
     * {@literal @Valid} @ModelAttribute DateForm dateForm - means the post request contains the form body which will spring will create
     * into a domain object for us and validate it using the JPA annotations.
     */
    @PostMapping("/reservation/dates")
    public String dates(@Valid @ModelAttribute("reservationFlow") ReservationFlow reservationFlow,
                        BindingResult bindingResult,
                        RedirectAttributes redirectAttributes) {
        reservationFlow.enterStep(ReservationFlow.Step.Dates);

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

    @PostMapping(value = "/reservation/dates", params = "cancel")
    public String cancelDates(SessionStatus sessionStatus) {
        sessionStatus.setComplete();
        return "redirect:/";
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
        reservationFlow.enterStep(ReservationFlow.Step.Guests);
        model.addAttribute("guest", new Guest());
        return "reservation/guests";
    }

    @PostMapping(value = "/reservation/guests", params = "back")
    public String fromGuestBackToDates(@ModelAttribute("reservationFlow") ReservationFlow reservationFlow,
                                       RedirectAttributes ra) {
        reservationFlow.enterStep(ReservationFlow.Step.Guests);
        ra.addFlashAttribute("reservationFlow", reservationFlow);
        return "redirect:/reservation?roomId=" + reservationFlow.getReservation().getRoom().getId();
    }

    @PostMapping(value = "/reservation/guests", params = "addGuest")
    public String postAddGuest(@Valid @ModelAttribute("guest") Guest guest,
                               BindingResult bindingResult,
                               @ModelAttribute("reservationFlow") ReservationFlow reservationFlow,
                               Model model) {
        reservationFlow.enterStep(ReservationFlow.Step.Guests);

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

    @PostMapping(value = "/reservation/guests", params = "removeGuest")
    public String postRemoveGuest(@RequestParam("removeGuest") UUID guestId,
                                  @ModelAttribute("reservationFlow") ReservationFlow reservationFlow,
                                  Model model) {
        reservationFlow.enterStep(ReservationFlow.Step.Guests);
        reservationFlow.getReservation().removeGuestById(guestId);
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
                                    @ModelAttribute("reservationFlow") ReservationFlow reservationFlow,
                                    RedirectAttributes redirectAttributes) {
        reservationFlow.enterStep(ReservationFlow.Step.Guests);

        if (!reservationFlow.getReservation().hasGuests()) {
            errors.reject("guests.noneExist", "There must be at least 1 guest");
            return "reservation/guests";
        }
        if (!reservationFlow.getReservation().hasAtLeastOneAdultGuest()) {
            errors.reject("guests.noAdults", "There must be at least 1 adult");
            return "reservation/guests";
        }

        redirectAttributes.addFlashAttribute("reservationFlow", reservationFlow);

        reservationFlow.completeStep(ReservationFlow.Step.Guests);
        return "redirect:/reservation/extras";
    }


    // Flow step 3

    @GetMapping("/reservation/extras")
    public String getGeneralExtrasForm(@ModelAttribute("reservationFlow") ReservationFlow reservationFlow, Model model) {
        reservationFlow.setActive(ReservationFlow.Step.Extras);

        List<Extra> generalExtras = extraRepository.findAllByTypeAndCategory(
                reservationFlow.getReservation().getExtraPricingType(), Extra.Category.General
        );
        model.addAttribute("extras", generalExtras);
        return "reservation/extras";
    }

    @PostMapping(value = "/reservation/extras", params = "back")
    public String fromGeneralExtrasBackToGuests(@ModelAttribute("reservationFlow") ReservationFlow reservationFlow,
                                                RedirectAttributes ra) {
        reservationFlow.setActive(ReservationFlow.Step.Guests);
        ra.addFlashAttribute("reservationFlow", reservationFlow);
        return "redirect:/reservation/guests";
    }

    @PostMapping(value = "/reservation/extras")
    public String submitGeneralExtras(@ModelAttribute("reservationFlow") ReservationFlow reservationFlow,
                                      RedirectAttributes ra) {
        reservationFlow.setActive(ReservationFlow.Step.Extras);

        ra.addFlashAttribute("reservationFlow", reservationFlow);
        reservationFlow.completeStep(ReservationFlow.Step.Extras);
        return "redirect:/reservation/meals";
    }

    @PostMapping(value = "/reservation/extras", params = "add")
    public String submitGeneralExtrasAjax(@ModelAttribute("reservationFlow") ReservationFlow reservationFlow) {
        return "reservation/fragments :: quickSummary";
    }


    // Flow step 4 - meals

    @PostMapping(value = "/reservation/meals", params = "back")
    public String fromMealPlansBackToGeneralExtras(@ModelAttribute("reservationFlow") ReservationFlow reservationFlow,
                                                   RedirectAttributes ra) {
        reservationFlow.setActive(ReservationFlow.Step.Meals);
        ra.addFlashAttribute("reservationFlow", reservationFlow);
        return "redirect:/reservation/extras";
    }

    @GetMapping("/reservation/meals")
    public String getMealPlans(@ModelAttribute("reservationFlow") ReservationFlow reservationFlow,
                               Model model) {
        reservationFlow.setActive(ReservationFlow.Step.Meals);

        reservationFlow.getReservation().createMealPlans();
        createMealPlanModel(reservationFlow, model);
        return "reservation/meals";
    }

    private void createMealPlanModel(ReservationFlow reservationFlow, Model model) {
        List<Extra> foodExtras = extraRepository.findAllByTypeAndCategory(
                reservationFlow.getReservation().getExtraPricingType(),
                Extra.Category.Food
        );
        model.addAttribute("foodExtras", foodExtras);
        model.addAttribute("dietaryRequirements", DietaryRequirement.values());
    }

    @PostMapping(value = "/reservation/meals", params = "add")
    public String postMealPlansAjax(@ModelAttribute("reservationFlow") ReservationFlow reservationFlow) {
        return "reservation/fragments :: quickSummary";
    }

    @PostMapping("/reservation/meals")
    public String postMealPlans(@ModelAttribute("reservationFlow") ReservationFlow reservationFlow,
                                Errors errors, Model model, RedirectAttributes ra) {
        reservationFlow.setActive(ReservationFlow.Step.Meals);

        Reservation reservation = reservationFlow.getReservation();
        for (int i = 0; i < reservation.getMealPlans().size(); i++) {
            // The template uses validationFieldPath variable to refer to the error field on the reservation object.
            if (reservation.getMealPlans().get(i).hasInvalidDietaryRequirements()) {
                errors.rejectValue("reservation.mealPlans[" + i + "].dietaryRequirements", "VeganMismatch",
                        "Cannot be Vegan and Vegetarian at the same time");
            }
        }

        if (errors.hasFieldErrors()) {
            createMealPlanModel(reservationFlow, model);
            return "reservation/meals";
        }

        ra.addFlashAttribute("reservationFlow", reservationFlow);
        reservationFlow.completeStep(ReservationFlow.Step.Meals);
        return "redirect:/reservation/review";
    }

    // Flow step 5 - review

    @GetMapping("/reservation/review")
    public String getReview(@ModelAttribute("reservationFlow") ReservationFlow reservationFlow) {
        reservationFlow.setActive(ReservationFlow.Step.Review);
        return "reservation/review";
    }

    @PostMapping(value = "/reservation/review", params = "back")
    public String fromReviewBackToMealPlans(@ModelAttribute("reservationFlow") ReservationFlow reservationFlow,
                                            RedirectAttributes ra) {
        reservationFlow.setActive(ReservationFlow.Step.Review);
        ra.addFlashAttribute("reservationFlow", reservationFlow);
        return "redirect:/reservation/meals";
    }

    @PostMapping("/reservation/review")
    public String postReview(@ModelAttribute("reservationFlow") ReservationFlow reservationFlow,
                             RedirectAttributes ra) {
        reservationFlow.setActive(ReservationFlow.Step.Review);

        ra.addFlashAttribute("reservationFlow", reservationFlow);
        reservationFlow.completeStep(ReservationFlow.Step.Review);
        return "redirect:/reservation/payment";
    }


    // Flow step 6 - payment

    @GetMapping("/reservation/payment")
    public String getPayment(@ModelAttribute("reservationFlow") ReservationFlow reservationFlow,
                             Model model) {
        reservationFlow.setActive(ReservationFlow.Step.Payment);
        model.addAttribute("pendingPayment", new PendingPayment(LocalDateTime.now()));
        return "reservation/payment";
    }

    @PostMapping(value = "/reservation/payment", params = "back")
    public String fromPaymentBackToReview(@ModelAttribute("reservationFlow") ReservationFlow reservationFlow,
                                          RedirectAttributes ra) {
        reservationFlow.setActive(ReservationFlow.Step.Payment);
        ra.addFlashAttribute("reservationFlow", reservationFlow);
        return "redirect:/reservation/review";
    }

    @PostMapping(value = "/reservation/payment", params = "cancel")
    public String cancelPayment(SessionStatus sessionStatus) {
        sessionStatus.setComplete();
        return "redirect:/";
    }

    @PostMapping("/reservation/payment")
    public String postPayment(@ModelAttribute("reservationFlow") ReservationFlow reservationFlow,
                              @Valid @ModelAttribute("pendingPayment") PendingPayment pendingPayment,
                              BindingResult bindingResult, SessionStatus sessionStatus) {
        reservationFlow.setActive(ReservationFlow.Step.Payment);

        if (bindingResult.hasErrors()) {
            return "reservation/payment";
        }

        Reservation reservation = reservationFlow.getReservation();
        // Simulate making a valid payment
        reservation.setCompletedPayment(pendingPayment.toCompletedPayment());

        /*
         * The new reservation is saved through the Room since the room owns the reservation in the
         * bi directional 1 to 1 mapping. This is to allow easier querying to identity rooms that
         * have reservations.
         */
        roomRepository.save(reservation.getRoom());
        sessionStatus.setComplete();

        reservationFlow.completeStep(ReservationFlow.Step.Payment);
        return "redirect:/reservation/completed";
    }

    // End flow

    @GetMapping("/reservation/completed")
    public String getFlowCompleted() {
        return "reservation/completed";
    }










    @GetMapping("/drinks")
    public String getDrinks(Model model) {
        Person person = new Person(30L, "John Smith");

        List<Drink> selectableDrinks = Arrays.asList(
                new Drink(1L, "coke"),
                new Drink(2L, "fanta"),
                new Drink(3L, "sprite")
        );

        model.addAttribute("person", person);
        model.addAttribute("selectableDrinks", selectableDrinks);

        return "reservation/drinks";
    }

    @GetMapping("/drinks2")
    public String getDrinks2(Model model) {
        Person person = new Person(30L, "John Smith");

        model.addAttribute("person", person);
        model.addAttribute("selectableEnumDrinks", EnumDrink.values());

        return "reservation/drinks";
    }



    @PostMapping("/drinks")
    public String postDrinks(@ModelAttribute("person") Person person) {
        System.out.println("Person has been posted!");
        System.out.println(person);
        return "reservation/drinks";
    }
}
