package com.demo.reservation;

import com.demo.TimeProvider;
import com.demo.domain.*;
import com.demo.domain.location.Address;
import com.demo.domain.location.Postcode;
import com.demo.domain.location.State;
import com.demo.persistance.RoomRepository;
import com.demo.reservation.flow.ReservationController;
import com.demo.reservation.flow.forms.DateFormParams;
import com.demo.reservation.flow.forms.ReservationFlow;
import org.hamcrest.Matchers;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultMatcher;
import org.springframework.util.LinkedMultiValueMap;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

import static com.demo.GlobalErrorMatchers.globalErrorMatchers;
import static com.demo.reservation.flow.forms.FlowMatchers.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Tests for the reservation booking flow.
 * Note: tests only look at the current {@code ReservationFlow.activeStep} with the assumption previous steps have
 * completed since we are testing individual flow steps, not going through the wizard from start to finish in 1 test.
 */
@RunWith(SpringRunner.class)
@WebMvcTest(ReservationController.class)
@ActiveProfiles("test")
public class ReservationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private RoomRepository roomRepository;

    @MockBean
    private ExtraRepository extraRepository;

    @MockBean
    private TimeProvider timeProvider;

    private Room createRoom() {
        Address address = new Address("Hotel Royal", "166 Albert Road", null,
                State.VIC, "Melbourne", new Postcode("3000"));

        Hotel hotel = new Hotel("Hotel Royal", address, 3, "royal@hotel.com");
        Room room = new Room("ABC123", RoomType.Economy, 4, BigDecimal.valueOf(23.50));
        room.setId(2L);
        room.setHotel(hotel);
        return room;
    }

    /**
     * Creates a {@code ReservationFlow} containing a {@code Reservation} in the expected state for entering
     * in the reservation dates. This corresponds to the starting {@code Reservation} state when the date form is first
     * retrieved from server. Note how the {@code ReservationFlow.activeStep} is not set since this will be tested.
     */
    private ReservationFlow pendingDateFlow() {
        ReservationFlow reservationFlow = new ReservationFlow();
        reservationFlow.getReservation().setRoom(createRoom());
        return reservationFlow;
    }

    /**
     * Creates a {@code ReservationFlow} containing a {@code Reservation} in the expected state after successfully
     * posting the reservation date form. This is the starting state for the Guest form.
     */
    private ReservationFlow dateCompletedFlow() {
        ReservationFlow reservationFlow = pendingDateFlow();

        LocalDate checkIn = LocalDate.now();
        LocalDate checkOut = checkIn.plusDays(5);
        ReservationDates reservationDates = new ReservationDates(
                checkIn, checkOut, LocalTime.of(11, 0), false, true
        );
        reservationFlow.getReservation().setDates(reservationDates);
        return reservationFlow;
    }

    /**
     * Creates a {@code ReservationFlow} in the expected state after completing the guest form. This is the starting
     * state for the Extras form. This will enable quickly rebuilding the current reservation flow state up to the
     * current flow state under test. Note how the {@code ReservationFlow.activeStep} is not set since this will be tested.
     * <ul>
     * <li>Step 1 - ReservationDates = completed</li>
     * <li>Step 2 - Guests = completed</li>
     * <li>Step 3 - General extras = pending</li>
     * </ul>
     */
    private ReservationFlow guestCompletedFlow() {
        ReservationFlow reservationFlow = dateCompletedFlow();
        reservationFlow.getReservation().addGuest(new Guest("john", "smith", false));
        return reservationFlow;
    }

    /**
     * Creates a {@code ReservationFlow} in the expected after entering in general extras details. This is the
     * starting state for the Meal plans form. This will enable quickly rebuilding the current reservation flow state
     * up to the current flow state under test. Note how the {@code ReservationFlow.activeStep} is not set since
     * this will be tested.
     *
     * <ul>
     * <li>Step 1 - ReservationDates = completed</li>
     * <li>Step 2 - Guests = completed</li>
     * <li>Step 3 - Extras = completed</li>
     * <li>Step 4 - Meal Plans = pending</li>
     * </ul>
     */
    private ReservationFlow extrasCompletedFlow() {
        ReservationFlow reservationFlow = guestCompletedFlow();
        reservationFlow.getReservation().setGeneralExtras(
                Set.of(new Extra("foxtel", BigDecimal.valueOf(4.5), Extra.Type.Premium, Extra.Category.General))
        );
        return reservationFlow;
    }

    /**
     * Creates a {@code ReservationFlow} in the expected state after completing the general extras form. This is the starting
     * state for the review form. This will enable quickly rebuilding the current reservation flow state up to the
     * current flow state under test. Note how the {@code ReservationFlow.activeStep} is not set since this will be tested.
     * <ul>
     * <li>Step 1 - ReservationDates = completed</li>
     * <li>Step 2 - Guests = completed</li>
     * <li>Step 3 - General extras = completed</li>
     * <li>Step 4 - Meal plans = completed</li>
     * <li>Step 5 - Review = pending</li>
     * </ul>
     */
    private ReservationFlow mealsCompletedFlow() {
        ReservationFlow reservationFlow = extrasCompletedFlow();
        reservationFlow.getReservation().createMealPlans();

        // As per above in guestCompletedFlow, there is 1 guest but lets just assume they don't select
        // a meal plan so its left empty.
        return reservationFlow;
    }

    /**
     * Simulates sending an empty form to the server.
     */
    private LinkedMultiValueMap<String, String> emptyParams() {
        return new LinkedMultiValueMap<>();
    }


    // Flow step 1 - GET date form

    @Test
    public void getDateForm_MissingRoomIdQueryParam_400BadRequest() throws Exception {
        mockMvc.perform(get("/reservation"))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void getDateForm_IllegalRoomIdType_400BadRequest() throws Exception {
        mockMvc.perform(get("/reservation?roomId=abc"))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void getDateForm_RoomIdDoesNotExist_404NotFound() throws Exception {
        when(roomRepository.findById(anyLong())).thenReturn(Optional.empty());

        mockMvc.perform(get("/reservation?roomId=34"))
                .andExpect(status().isNotFound());
    }

    /**
     * Given the room is found, its expected the Dates step is active but still incomplete which
     * should be set by default when {@code ReservationFlow} is instantiated.
     */
    @Test
    public void getDateForm_RetrievesRoomById() throws Exception {
        Room room = createRoom();
        when(roomRepository.findById(anyLong())).thenReturn(Optional.of(room));

        mockMvc.perform(get("/reservation?roomId=5"))
                .andExpect(status().isOk())
                .andExpect(modelHasActiveFlowStep(ReservationFlow.Step.Dates))
                .andExpect(modelHasIncompleteFlowStep(ReservationFlow.Step.Dates));

        verify(roomRepository, times(1)).findById(anyLong());
    }

    @Test
    public void getDateForm_ReturnsCorrectView() throws Exception {
        Room room = createRoom();
        when(roomRepository.findById(anyLong())).thenReturn(Optional.of(room));

        mockMvc.perform(get("/reservation?roomId=5"))
                .andExpect(status().isOk())
                .andExpect(view().name("reservation/dates"));
    }

    /**
     * Assert that the {@code ReservationFlow} is in the model with the correct state.
     *
     * <ul>
     * <li>The room must be set since the {@code Reservation} performs many calculations which depend on a {@code Room}.</li>
     * <li>dates must not null given the date form will be bound to this object.</li>
     * </ul>
     */
    @Test
    public void getDateForm_HasValidModel() throws Exception {
        Room room = createRoom();
        when(roomRepository.findById(anyLong())).thenReturn(Optional.of(room));

        mockMvc.perform(get("/reservation?roomId=5"))
                .andExpect(model().attributeExists("reservationFlow"))
                .andExpect(model().attribute("reservationFlow",
                        Matchers.hasProperty("reservation", Matchers.allOf(
                                Matchers.hasProperty("room", Matchers.is(room)),
                                Matchers.hasProperty("dates", Matchers.notNullValue())
                        ))));
    }

    // Flow step 1 - POST date form

    /**
     * SessionStatus should be set to complete but not sure how to test this.
     */
    @Test
    public void postDateForm_Cancel_RedirectHome() throws Exception {
        mockMvc.perform(post("/reservation/dates")
                .param("cancel", ""))
                .andExpect(view().name("redirect:/"));
    }

    @Test
    public void postDateForm_EmptyForm_ExpectedBeanValidationErrors() throws Exception {
        ReservationFlow reservationFlow = pendingDateFlow();
        // Setting to non date step to ensure date end points update to correct step since date is the default step
        // which can be misleading.
        reservationFlow.setActive(ReservationFlow.Step.Extras);
        reservationFlow.completeStep(ReservationFlow.Step.Dates);

        when(roomRepository.findById(anyLong())).thenReturn(Optional.of(reservationFlow.getReservation().getRoom()));

        mockMvc.perform(post("/reservation/dates")
                .sessionAttr("reservationFlow", reservationFlow)
                .params(emptyParams()))
                .andExpect(view().name("reservation/dates"))
                .andExpect(model().errorCount(4))
                .andExpect(model().attributeHasFieldErrorCode("reservationFlow", "reservation.dates.checkInDate", "NotNull"))
                .andExpect(model().attributeHasFieldErrorCode("reservationFlow", "reservation.dates.checkOutDate", "NotNull"))
                .andExpect(model().attributeHasFieldErrorCode("reservationFlow", "reservation.dates.estimatedCheckInTime", "NotNull"))
                .andExpect(model().attributeHasFieldErrorCode("reservationFlow", "reservation.dates.policyAcknowledged", "AssertTrue"))
                .andExpect(modelHasActiveFlowStep(ReservationFlow.Step.Dates))
                .andExpect(modelHasIncompleteFlowStep(ReservationFlow.Step.Dates))
                .andExpect(model().attributeExists("reservationFlow"));
    }

    @Test
    public void postDateForm_GlobalValidation_CheckIn_NotInPast() throws Exception {
        ReservationFlow reservationFlow = pendingDateFlow();
        // Setting to non date step to ensure date end points update to correct step since date is the default step
        // which can be misleading.
        reservationFlow.setActive(ReservationFlow.Step.Extras);
        reservationFlow.completeStep(ReservationFlow.Step.Dates);

        when(roomRepository.findById(anyLong())).thenReturn(Optional.of(reservationFlow.getReservation().getRoom()));
        when(timeProvider.localDate()).thenReturn(LocalDate.now());

        mockMvc.perform(post("/reservation/dates")
                .sessionAttr("reservationFlow", reservationFlow)
                .params(DateFormParams.checkInNotInFutureParams(timeProvider)))
                .andExpect(view().name("reservation/dates"))
                .andExpect(model().errorCount(1))
                .andExpect(model().attributeHasFieldErrorCode("reservationFlow", "reservation.dates", "checkInDate.future"))
                .andExpect(modelHasActiveFlowStep(ReservationFlow.Step.Dates))
                .andExpect(modelHasIncompleteFlowStep(ReservationFlow.Step.Dates))
                .andExpect(model().attributeExists("reservationFlow"));
    }

    @Test
    public void postDateForm_GlobalValidation_Checkout_OccursAfterCheckIn() throws Exception {
        ReservationFlow reservationFlow = pendingDateFlow();
        // Setting to non date step to ensure date end points update to correct step since date is the default step
        // which can be misleading.
        reservationFlow.setActive(ReservationFlow.Step.Extras);
        reservationFlow.completeStep(ReservationFlow.Step.Dates);

        when(roomRepository.findById(anyLong())).thenReturn(Optional.of(reservationFlow.getReservation().getRoom()));
        when(timeProvider.localDate()).thenReturn(LocalDate.now());

        mockMvc.perform(post("/reservation/dates")
                .sessionAttr("reservationFlow", reservationFlow)
                .params(DateFormParams.checkOutOccursBeforeCheckInParams(timeProvider)))
                .andExpect(view().name("reservation/dates"))
                .andExpect(model().errorCount(1))
                .andExpect(model().attributeHasFieldErrorCode("reservationFlow", "reservation.dates", "checkOutDate.afterCheckIn"))
                .andExpect(modelHasActiveFlowStep(ReservationFlow.Step.Dates))
                .andExpect(modelHasIncompleteFlowStep(ReservationFlow.Step.Dates))
                .andExpect(model().attributeExists("reservationFlow"));
    }

    @Test
    public void postDateForm_GlobalValidation_SatisfiesMinimumNightStay() throws Exception {
        ReservationFlow reservationFlow = pendingDateFlow();
        // Setting to non date step to ensure date end points update to correct step since date is the default step
        // which can be misleading.
        reservationFlow.setActive(ReservationFlow.Step.Extras);
        reservationFlow.completeStep(ReservationFlow.Step.Dates);

        when(roomRepository.findById(anyLong())).thenReturn(Optional.of(reservationFlow.getReservation().getRoom()));
        when(timeProvider.localDate()).thenReturn(LocalDate.now());

        mockMvc.perform(post("/reservation/dates")
                .sessionAttr("reservationFlow", reservationFlow)
                .params(DateFormParams.minimumNightsParams(timeProvider)))
                .andExpect(view().name("reservation/dates"))
                .andExpect(model().errorCount(1))
                .andExpect(model().attributeHasFieldErrorCode("reservationFlow", "reservation.dates", "checkOutDate.minNights"))
                .andExpect(modelHasActiveFlowStep(ReservationFlow.Step.Dates))
                .andExpect(modelHasIncompleteFlowStep(ReservationFlow.Step.Dates))
                .andExpect(model().attributeExists("reservationFlow"));
        ;
    }

    /**
     * Note the flash attributes are being used since we are redirecting. flash is used otherwise Model attributes
     * wont survive the redirect. ReservationFlow.Step.Dates should now be completed.
     */
    @Test
    public void postDateForm_Valid_RedirectToNextView() throws Exception {
        ReservationFlow reservationFlow = pendingDateFlow();
        // Setting to non date step to ensure date end points update to correct step since date is the default step
        // which can be misleading.
        reservationFlow.setActive(ReservationFlow.Step.Extras);
        reservationFlow.completeStep(ReservationFlow.Step.Dates);

        when(roomRepository.findById(anyLong())).thenReturn(Optional.of(reservationFlow.getReservation().getRoom()));
        when(timeProvider.localDate()).thenReturn(LocalDate.now());

        mockMvc.perform(post("/reservation/dates")
                .sessionAttr("reservationFlow", reservationFlow)
                .params(DateFormParams.validParams(timeProvider)))
                .andExpect(view().name("redirect:/reservation/guests"))
                .andExpect(flash().attributeExists("reservationFlow"))
                .andExpect(flashHasActiveFlowStep(ReservationFlow.Step.Dates))
                .andExpect(flashHasCompletedFlowStep(ReservationFlow.Step.Dates));
    }

    // Ajax dynamic room price fragment

    @Test
    public void roomCostFragment_ReturnsCorrectFragment() throws Exception {
        ReservationFlow reservationFlow = pendingDateFlow();
        when(timeProvider.localDate()).thenReturn(LocalDate.now());

        mockMvc.perform(post("/reservation/dates?prices")
                .sessionAttr("reservationFlow", reservationFlow)
                .params(DateFormParams.validParams(timeProvider)))
                .andExpect(view().name("reservation/fragments :: roomCosts"));
    }

    // Flow step 2 - guests

    /**
     * Specifically looking for a new {@code Guest} to bind to the form and the Guest step is active but incomplete.
     */
    @Test
    public void getGuestForm_HasValidStartingState() throws Exception {
        ReservationFlow reservationFlow = dateCompletedFlow();

        mockMvc.perform(get("/reservation/guests")
                .sessionAttr("reservationFlow", reservationFlow))
                .andExpect(view().name("reservation/guests"))
                .andExpect(model().attribute("guest", Matchers.is(new Guest())))
                .andExpect(model().attributeExists("reservationFlow"))
                .andExpect(modelHasActiveFlowStep(ReservationFlow.Step.Guests))
                .andExpect(modelHasIncompleteFlowStep(ReservationFlow.Step.Guests));
    }

    /**
     * Going back to the date view requires a redirect which means the Model should be in flash otherwise it wont
     * survive the redirect. The {@code ReservationFlow} step doesn't need checking as it will be updated when
     * GET date form is called.
     */
    @Test
    public void fromGuestBackToDates_GoBackToDateView_RedirectsCorrectView() throws Exception {
        ReservationFlow reservationFlow = dateCompletedFlow();
        Long roomId = reservationFlow.getReservation().getRoom().getId();

        mockMvc.perform(post("/reservation/guests")
                .param("back", "")
                .sessionAttr("reservationFlow", reservationFlow))
                .andExpect(view().name("redirect:/reservation?roomId=" + roomId))
                .andExpect(flash().attributeExists("reservationFlow"));
    }

    @Test
    public void postAddGuest_BeanValidationError_NullFields() throws Exception {
        ReservationFlow reservationFlow = dateCompletedFlow();

        ResultMatcher didNotAddGuest = model().attribute("reservationFlow",
                Matchers.hasProperty("reservation",
                        Matchers.hasProperty("guests", Matchers.hasSize(0))));

        /*
         * Since we are posting no guest details to the server, @ModelAttribute("guest") will create a new guest and
         * add it to the model. Since there are errors, this same guest will stay in the model for re rendering in
         * the template. We are simply creating an empty object to simulate what spring will do in this case.
         */
        ResultMatcher didNotCreateNewGuestObject = model().attribute("guest", Matchers.is(new Guest()));

        mockMvc.perform(post("/reservation/guests")
                .param("addGuest", "")
                .sessionAttr("reservationFlow", reservationFlow))
                .andExpect(model().errorCount(2))
                .andExpect(model().attributeHasFieldErrorCode("guest", "firstName", "NotNull"))
                .andExpect(model().attributeHasFieldErrorCode("guest", "lastName", "NotNull"))
                .andExpect(modelHasActiveFlowStep(ReservationFlow.Step.Guests))
                .andExpect(modelHasIncompleteFlowStep(ReservationFlow.Step.Guests))
                .andExpect(didNotAddGuest)
                .andExpect(didNotCreateNewGuestObject)
                .andExpect(model().attributeExists("reservationFlow"));
    }

    @Test
    public void postAddGuest_BeanValidationError_EmptyFields() throws Exception {
        ReservationFlow reservationFlow = dateCompletedFlow();

        ResultMatcher didNotAddGuest = model().attribute("reservationFlow",
                Matchers.hasProperty("reservation",
                        Matchers.hasProperty("guests", Matchers.hasSize(0))));

        /*
         * When spring sees @ModelAttribute("guest"), it will look in the form being posted and recreate a guest object
         * according to the forms key=value pairs and will add it to the model. Since there are errors, we are checking
         * to see that this guest stays in the model to be re rendered in the template to display the errors and to
         * prevent the form fields being cleared.
         */
        Guest formGuest = new Guest("", "", false);
        ResultMatcher didNotCreateNewGuestObject = model().attribute("guest", Matchers.is(formGuest));

        mockMvc.perform(post("/reservation/guests")
                .sessionAttr("reservationFlow", reservationFlow)
                .param("addGuest", "")
                .param("firstName", formGuest.getFirstName())
                .param("lastName", formGuest.getLastName())
                .param("child", formGuest.isChild() + ""))
                .andExpect(model().errorCount(2))
                .andExpect(model().attributeHasFieldErrorCode("guest", "firstName", "Size"))
                .andExpect(model().attributeHasFieldErrorCode("guest", "lastName", "Size"))
                .andExpect(modelHasActiveFlowStep(ReservationFlow.Step.Guests))
                .andExpect(modelHasIncompleteFlowStep(ReservationFlow.Step.Guests))
                .andExpect(didNotAddGuest)
                .andExpect(didNotCreateNewGuestObject)
                .andExpect(model().attributeExists("reservationFlow"));
    }

    @Test
    public void postAddGuest_GuestExists_HasGlobalError() throws Exception {
        ReservationFlow reservationFlow = dateCompletedFlow();

        // case should be irrelevant in detecting duplicates
        reservationFlow.getReservation().addGuest(new Guest("JoHn", "sMITh", false));

        ResultMatcher didNotAddGuest = model().attribute("reservationFlow",
                Matchers.hasProperty("reservation",
                        Matchers.hasProperty("guests", Matchers.hasSize(1))));

        /*
         * When spring sees @ModelAttribute("guest"), it will look in the form being posted and recreate a guest object
         * according to the forms key=value pairs and will add it to the model. Since there are errors, we are checking
         * to see that this guest stays in the model to be re rendered in the template to display the errors and to
         * prevent the form fields being cleared.
         */
        Guest formGuest = new Guest("john", "smith", false);
        ResultMatcher didNotCreateNewGuestObject = model().attribute("guest", Matchers.is(formGuest));

        mockMvc.perform(post("/reservation/guests")
                .sessionAttr("reservationFlow", reservationFlow)
                .param("addGuest", "")
                .param("firstName", formGuest.getFirstName())
                .param("lastName", formGuest.getLastName())
                .param("child", formGuest.isChild() + ""))
                .andExpect(model().errorCount(1))
                .andExpect(globalErrorMatchers().hasGlobalErrorCode("guest", "exists"))
                .andExpect(modelHasActiveFlowStep(ReservationFlow.Step.Guests))
                .andExpect(modelHasIncompleteFlowStep(ReservationFlow.Step.Guests))
                .andExpect(didNotAddGuest)
                .andExpect(didNotCreateNewGuestObject)
                .andExpect(model().attributeExists("reservationFlow"));
    }

    @Test
    public void postAddGuest_GuestLimitExceeded_HasGlobalError() throws Exception {
        ReservationFlow reservationFlow = dateCompletedFlow();

        // next guest added should be denied.
        reservationFlow.getReservation().getRoom().setBeds(1);
        reservationFlow.getReservation().addGuest(new Guest("john", "smith", false));

        ResultMatcher didNotAddGuest = model().attribute("reservationFlow",
                Matchers.hasProperty("reservation",
                        Matchers.hasProperty("guests", Matchers.hasSize(1))));

        /*
         * When spring sees @ModelAttribute("guest"), it will look in the form being posted and recreate a guest object
         * according to the forms key=value pairs and will add it to the model. Since there are errors, we are checking
         * to see that this guest stays in the model to be re rendered in the template to display the errors and to
         * prevent the form fields being cleared.
         */
        Guest formGuest = new Guest("marie", "clarke", false);
        ResultMatcher didNotCreateNewGuestObject = model().attribute("guest", Matchers.is(formGuest));

        mockMvc.perform(post("/reservation/guests")
                .sessionAttr("reservationFlow", reservationFlow)
                .param("addGuest", "")
                .param("firstName", formGuest.getFirstName())
                .param("lastName", formGuest.getLastName())
                .param("child", formGuest.isChild() + ""))
                .andExpect(model().errorCount(1))
                .andExpect(globalErrorMatchers().hasGlobalErrorCode("guest", "guestLimitExceeded"))
                .andExpect(modelHasActiveFlowStep(ReservationFlow.Step.Guests))
                .andExpect(modelHasIncompleteFlowStep(ReservationFlow.Step.Guests))
                .andExpect(didNotAddGuest)
                .andExpect(didNotCreateNewGuestObject)
                .andExpect(model().attributeExists("reservationFlow"));
    }

    /**
     * Asserts that a valid guest is added to the guest set and a new {@code Guest} is added to the model so
     * the UI form binds to a fresh object.
     */
    @Test
    public void postAddGuest_Valid() throws Exception {
        ReservationFlow reservationFlow = dateCompletedFlow();

        reservationFlow.getReservation().getRoom().setBeds(1);

        ResultMatcher reservationContainsGuest = model().attribute("reservationFlow",
                Matchers.hasProperty("reservation",
                        Matchers.hasProperty("guests",
                                Matchers.containsInAnyOrder(new Guest("marie", "clarke", false)))));

        /*
         * When spring sees @ModelAttribute("guest"), it will look in the form being posted and recreate a guest object
         * according to the forms key=value pairs and will add it to the model. Since there are no errors, we want to
         * create a new guest object so the model contains a fresh guest ready to be binded to the new form, otherwise
         * the old guest info will be in the form still!
         */
        ResultMatcher didCreateNewGuestObject = model().attribute("guest", Matchers.is(new Guest()));

        mockMvc.perform(post("/reservation/guests")
                .sessionAttr("reservationFlow", reservationFlow)
                .param("addGuest", "")
                .param("firstName", "marie")
                .param("lastName", "clarke")
                .param("child", "false"))
                .andExpect(view().name("reservation/guests"))
                .andExpect(model().hasNoErrors())
                .andExpect(didCreateNewGuestObject)
                .andExpect(reservationContainsGuest)
                .andExpect(modelHasActiveFlowStep(ReservationFlow.Step.Guests))
                .andExpect(modelHasIncompleteFlowStep(ReservationFlow.Step.Guests))
                .andExpect(model().attributeExists("reservationFlow"));
    }

    /**
     * Technically will never happen since the remove button will always contain a guest that exists.
     * <p>
     * Each time a guest is removed, a new {@code Guest} must be in the model so the form is ready to receive
     * a new {@code Guest}.
     */
    @Test
    public void postRemoveGuest_GuestIdNotFound_ShouldHaveNoEffect() throws Exception {
        // No guest is in here so the supplied guest id wont be found.
        ReservationFlow reservationFlow = dateCompletedFlow();

        ResultMatcher reservationContainsGuest = model().attribute("reservationFlow",
                Matchers.hasProperty("reservation",
                        Matchers.hasProperty("guests", Matchers.hasSize(0))));

        mockMvc.perform(post("/reservation/guests")
                .sessionAttr("reservationFlow", reservationFlow)
                .param("removeGuest", UUID.randomUUID().toString()))
                .andExpect(view().name("reservation/guests"))
                .andExpect(model().hasNoErrors())
                .andExpect(model().attribute("guest", Matchers.is(new Guest())))
                .andExpect(reservationContainsGuest)
                .andExpect(modelHasActiveFlowStep(ReservationFlow.Step.Guests))
                .andExpect(modelHasIncompleteFlowStep(ReservationFlow.Step.Guests))
                .andExpect(model().attributeExists("reservationFlow"));
    }

    /**
     * Note that a new {@code Guest} must be in the model to bind to the guest form.
     */
    @Test
    public void postRemoveGuest_GuestIdFound_ShouldBeRemoved() throws Exception {
        ReservationFlow reservationFlow = dateCompletedFlow();
        reservationFlow.getReservation().getRoom().setBeds(2);

        Guest guestA = new Guest("john", "smith", false);
        Guest guestB = new Guest("nicole", "smith", false);

        reservationFlow.getReservation().addGuest(guestA);
        reservationFlow.getReservation().addGuest(guestB);

        assertThat(reservationFlow.getReservation().getGuests()).containsExactlyInAnyOrder(guestA, guestB);

        ResultMatcher hasExpectedGuests = model().attribute("reservationFlow",
                Matchers.hasProperty("reservation",
                        Matchers.hasProperty("guests", Matchers.containsInAnyOrder(guestB))));

        // remove guestA
        mockMvc.perform(post("/reservation/guests")
                .sessionAttr("reservationFlow", reservationFlow)
                .param("removeGuest", guestA.getTempId().toString()))
                .andExpect(view().name("reservation/guests"))
                .andExpect(model().hasNoErrors())
                .andExpect(model().attribute("guest", Matchers.is(new Guest())))
                .andExpect(hasExpectedGuests)
                .andExpect(modelHasActiveFlowStep(ReservationFlow.Step.Guests))
                .andExpect(modelHasIncompleteFlowStep(ReservationFlow.Step.Guests))
                .andExpect(model().attributeExists("reservationFlow"));
    }

    /**
     * Try Transition from Guest form to Extras form and perform final guest checks. In this case there is a global
     * error because no guests exist when there is a constraint saying at least 1 must exist.
     *
     * <p>Since the addGuest handler validates guest adding logic, this prevents most invalid states. Transitioning
     * only requires checking if at least 1 guest exists.</p>
     */
    @Test
    public void postGuestToExtras_NoGuestExists_ExpectGlobalError() throws Exception {
        ReservationFlow reservationFlow = dateCompletedFlow();
        reservationFlow.getReservation().clearGuests();

        mockMvc.perform(post("/reservation/guests")
                .sessionAttr("reservationFlow", reservationFlow))
                .andExpect(view().name("reservation/guests"))
                .andExpect(model().errorCount(1))
                .andExpect(globalErrorMatchers().hasGlobalErrorCode("guest", "guests.noneExist"))
                .andExpect(modelHasActiveFlowStep(ReservationFlow.Step.Guests))
                .andExpect(modelHasIncompleteFlowStep(ReservationFlow.Step.Guests))
                .andExpect(model().attributeExists("reservationFlow"));
    }


    /**
     * Try Transition from Guest form to Extras form and perform final guest checks. In this case there is a global
     * error because there should be at least 1 adult.
     *
     * <p>Since the addGuest handler validates guest adding logic, this prevents most invalid states but
     * the final global checks are done on the final post.</p>
     */
    @Test
    public void postGuestToExtras_NoAdultExists_ExpectGlobalError() throws Exception {
        ReservationFlow reservationFlow = dateCompletedFlow();
        reservationFlow.getReservation().clearGuests();

        mockMvc.perform(post("/reservation/guests")
                .sessionAttr("reservationFlow", reservationFlow))
                .andExpect(view().name("reservation/guests"))
                .andExpect(model().errorCount(1))
                .andExpect(globalErrorMatchers().hasGlobalErrorCode("guest", "guests.noneExist"))
                .andExpect(modelHasActiveFlowStep(ReservationFlow.Step.Guests))
                .andExpect(modelHasIncompleteFlowStep(ReservationFlow.Step.Guests))
                .andExpect(model().attributeExists("reservationFlow"));
    }

    /**
     * When all guest info is entered in correctly, redirect to the next flow step.
     * <p>Note the flash attributes are being used since we are redirecting. If flash was not used then the Model
     * attributes wouldn't survive the redirect. ReservationFlow.Step.Guests should now be completed.</p>
     */
    @Test
    public void postGuestToExtras_Valid() throws Exception {
        ReservationFlow reservationFlow = dateCompletedFlow();
        reservationFlow.getReservation().getRoom().setBeds(3);
        reservationFlow.getReservation().addGuest(new Guest("john", "smith", false));

        mockMvc.perform(post("/reservation/guests")
                .sessionAttr("reservationFlow", reservationFlow))
                .andExpect(view().name("redirect:/reservation/extras"))
                .andExpect(model().hasNoErrors())
                .andExpect(flashHasActiveFlowStep(ReservationFlow.Step.Guests))
                .andExpect(flashHasCompletedFlowStep(ReservationFlow.Step.Guests))
                .andExpect(flash().attributeExists("reservationFlow"));
    }

    // Flow step 3 - extras

    /**
     * Assert that the Model contains the expected general extras fetched from repository and it
     * contains the reservationFlow from the session. Also ensure the reservationFlow step is on
     * Extras.
     */
    @Test
    public void getGeneralExtrasForm_HasValidStartingState() throws Exception {
        ReservationFlow reservationFlow = guestCompletedFlow();

        List<Extra> generalExtras = List.of(
                new Extra("foxtel", BigDecimal.valueOf(3.94), Extra.Type.Premium, Extra.Category.General)
        );
        when(extraRepository.findAllByTypeAndCategory(any(Extra.Type.class), eq(Extra.Category.General)))
                .thenReturn(generalExtras);

        mockMvc.perform(get("/reservation/extras")
                .sessionAttr("reservationFlow", reservationFlow))
                .andExpect(view().name("reservation/extras"))
                .andExpect(model().attribute("extras", Matchers.equalTo(generalExtras)))
                .andExpect(model().attributeExists("reservationFlow"))
                .andExpect(modelHasActiveFlowStep(ReservationFlow.Step.Extras))
                .andExpect(modelHasIncompleteFlowStep(ReservationFlow.Step.Extras));

        verify(extraRepository, times(1))
                .findAllByTypeAndCategory(any(Extra.Type.class), eq(Extra.Category.General));
        verifyNoMoreInteractions(extraRepository);
    }

    /**
     * Going back requires the model be transferred into flash attributes otherwise it will be lost
     * during the redirect.
     */
    @Test
    public void fromGeneralExtrasBackToGuests_RedirectsCorrectView() throws Exception {
        ReservationFlow reservationFlow = guestCompletedFlow();

        mockMvc.perform(post("/reservation/extras")
                .param("back", "")
                .sessionAttr("reservationFlow", reservationFlow))
                .andExpect(view().name("redirect:/reservation/guests"))
                .andExpect(flash().attributeExists("reservationFlow"));
    }

    /**
     * When the selected general extras are submitted, check the flow step is completed for Extras and
     * we redirect to the next meal view. flash attributes are needed so the Model state is not lost
     * across the redirect.
     * <p>
     * We cant test for checking extras are added to the reservation since all of that happens
     * using spring binding infrastructure so would need to make an integration test for that using
     * {@literal @SpringBootTest}.
     */
    @Test
    public void submitGeneralExtras_RedirectsToCorrectViewAndStep() throws Exception {
        ReservationFlow reservationFlow = guestCompletedFlow();

        mockMvc.perform(post("/reservation/extras")
                .sessionAttr("reservationFlow", reservationFlow))
                .andExpect(view().name("redirect:/reservation/meals"))
                .andExpect(flashHasActiveFlowStep(ReservationFlow.Step.Extras))
                .andExpect(flashHasCompletedFlowStep(ReservationFlow.Step.Extras))
                .andExpect(flash().attributeExists("reservationFlow"));
    }


    // Flow step 4 - meal plans

    /**
     * Redirects to general extras form when in the meal plans form. Note the flash attributes
     * used are so the Model is not lost between redirects.
     */
    @Test
    public void fromMealPlansBackToGeneralExtras() throws Exception {
        ReservationFlow reservationFlow = extrasCompletedFlow();

        mockMvc.perform(post("/reservation/meals")
                .param("back", "")
                .sessionAttr("reservationFlow", reservationFlow))
                .andExpect(view().name("redirect:/reservation/extras"))
                .andExpect(flash().attributeExists("reservationFlow"));
    }

    /**
     * Assert that the Model contains the expected objects required for template generation.
     * Since creating meal plans is quite complex we need
     * <ul>
     * <li>foodExtras: Each guest will be able to select an extra which will be added to
     * their {@code MealPlan}
     * </li>
     * <li>dietaryRequirements: Each guest will be able to select a dietary requirement which
     * will be added to their {@code MealPlan}
     * </li>
     * <li>Sanity check to ensure each guest has a meal plan created ready to be binded to the thymeleaf view.</li>
     * <li>reservationFlow: The reservationFlow must still be kept in the Model to continue to stay in the Session.</li>
     * </ul>
     * <p>
     */
    @Test
    public void getMeals_HasValidStartingState() throws Exception {
        ReservationFlow reservationFlow = extrasCompletedFlow();

        // A part of a meal plan, guests can choose 3 main daily meals
        List<Extra> foodExtras = List.of(
                new Extra("breakfast", BigDecimal.valueOf(3.20), Extra.Type.Basic, Extra.Category.Food),
                new Extra("lunch", BigDecimal.valueOf(5.40), Extra.Type.Basic, Extra.Category.Food),
                new Extra("dinner", BigDecimal.valueOf(8.90), Extra.Type.Basic, Extra.Category.Food)
        );

        // Make sure createMealPlans is called.
        Reservation reservationSpy = spy(reservationFlow.getReservation());
        reservationFlow.setReservation(reservationSpy);

        // So we can verify the correct call to get the food extras occurs.
        when(extraRepository.findAllByTypeAndCategory(any(Extra.Type.class), eq(Extra.Category.Food)))
                .thenReturn(foodExtras);

        ResultMatcher expectedMealPlansCreated = model().attribute("reservationFlow",
                Matchers.hasProperty("reservation",
                        Matchers.hasProperty("mealPlans",
                                Matchers.hasSize(reservationFlow.getReservation().getGuests().size()))));

        mockMvc.perform(get("/reservation/meals")
                .sessionAttr("reservationFlow", reservationFlow))
                .andExpect(view().name("reservation/meals"))
                .andExpect(model().attribute("foodExtras", Matchers.equalTo(foodExtras)))
                .andExpect(model().attributeExists("dietaryRequirements"))
                .andExpect(model().attributeExists("reservationFlow"))
                .andExpect(expectedMealPlansCreated)
                .andExpect(modelHasActiveFlowStep(ReservationFlow.Step.Meals))
                .andExpect(modelHasIncompleteFlowStep(ReservationFlow.Step.Meals));

        verify(extraRepository, times(1))
                .findAllByTypeAndCategory(any(Extra.Type.class), eq(Extra.Category.Food));
        verifyNoMoreInteractions(extraRepository);

        verify(reservationSpy, times(1)).createMealPlans();
    }
}