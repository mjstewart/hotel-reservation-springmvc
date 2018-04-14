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
import java.util.Optional;
import java.util.UUID;

import static com.demo.GlobalErrorMatchers.globalErrorMatchers;
import static com.demo.reservation.flow.forms.FlowMatchers.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@RunWith(SpringRunner.class)
@WebMvcTest(ReservationController.class)
@ActiveProfiles("test")
public class ReservationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private RoomRepository roomRepository;

    @MockBean
    private TimeProvider timeProvider;

    private Room createRoom() {
        Address address = new Address("Hotel Royal", "166 Albert Road", null,
                State.VIC, "Melbourne", new Postcode("3000"));

        Hotel hotel = new Hotel("Hotel Royal", address, 3, "royal@hotel.com");
        Room room = new Room("ABC123", RoomType.Economy, 4, BigDecimal.valueOf(23.50));
        room.setHotel(hotel);
        return room;
    }

    /**
     * Creates a {@code ReservationFlow} in the expected state for entering in the reservation dates.
     *
     * <p>ReservationDates is empty ready to bind to the new ReservationDates form.</p>
     */
    private ReservationFlow toDateFlow() {
        ReservationFlow reservationFlow = new ReservationFlow();
        reservationFlow.getReservation().setRoom(createRoom());
        reservationFlow.setActive(ReservationFlow.Step.Dates);
        return reservationFlow;
    }

    /**
     * Creates a {@code ReservationFlow} in the expected state for entering in guest details.
     *
     * <ul>
     * <li>Step 1 - ReservationDates = valid</li>
     * <li>Step 2 - Guests - empty ready to bind to the new guest form.</li>
     * </ul>
     */
    private ReservationFlow toGuestFlow() {
        ReservationFlow reservationFlow = new ReservationFlow();
        reservationFlow.getReservation().setRoom(createRoom());

        reservationFlow.completeStep(ReservationFlow.Step.Dates);
        reservationFlow.setActive(ReservationFlow.Step.Guests);

        LocalDate checkIn = LocalDate.now();
        LocalDate checkOut = checkIn.plusDays(5);
        ReservationDates reservationDates = new ReservationDates(
                checkIn, checkOut, LocalTime.of(11, 0), false, true
        );
        reservationFlow.getReservation().setDates(reservationDates);
        return reservationFlow;
    }


    private LinkedMultiValueMap<String, String> emptyParams() {
        return new LinkedMultiValueMap<>();
    }


    // Flow step 1 - GET date form

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
     * Assert that the {@code ReservationFlow} is in the model with the correct state.
     *
     * <p>The room must be set since the {@code Reservation} performs many calculations which depend on a {@code Room}.
     */
    @Test
    public void getDateForm_HasValidModel() throws Exception {
        Room room = createRoom();
        when(roomRepository.findById(anyLong())).thenReturn(Optional.of(room));

        mockMvc.perform(get("/reservation?roomId=5"))
                .andExpect(model().attribute("reservationFlow",
                        Matchers.hasProperty("reservation",
                                Matchers.hasProperty("room", Matchers.is(room)))));
    }

    // Flow step 1 - POST date form

    @Test
    public void postDateForm_Cancel_RedirectHome() throws Exception {
        mockMvc.perform(post("/reservation/dates")
                .param("cancel", ""))
                .andExpect(view().name("redirect:/"));
    }

    @Test
    public void postDateForm_EmptyForm_ExpectedBeanValidationErrors() throws Exception {
        ReservationFlow reservationFlow = toDateFlow();
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
                .andExpect(modelHasIncompleteFlowStep(ReservationFlow.Step.Dates));
    }

    @Test
    public void postDateForm_GlobalValidation_CheckIn_NotInPast() throws Exception {
        ReservationFlow reservationFlow = toDateFlow();
        when(roomRepository.findById(anyLong())).thenReturn(Optional.of(reservationFlow.getReservation().getRoom()));
        when(timeProvider.localDate()).thenReturn(LocalDate.now());

        mockMvc.perform(post("/reservation/dates")
                .sessionAttr("reservationFlow", reservationFlow)
                .params(DateFormParams.checkInNotInFutureParams(timeProvider)))
                .andExpect(view().name("reservation/dates"))
                .andExpect(model().errorCount(1))
                .andExpect(model().attributeHasFieldErrorCode("reservationFlow", "reservation.dates", "checkInDate.future"))
                .andExpect(modelHasActiveFlowStep(ReservationFlow.Step.Dates))
                .andExpect(modelHasIncompleteFlowStep(ReservationFlow.Step.Dates));
    }

    @Test
    public void postDateForm_GlobalValidation_Checkout_OccursAfterCheckIn() throws Exception {
        ReservationFlow reservationFlow = toDateFlow();
        when(roomRepository.findById(anyLong())).thenReturn(Optional.of(reservationFlow.getReservation().getRoom()));
        when(timeProvider.localDate()).thenReturn(LocalDate.now());

        mockMvc.perform(post("/reservation/dates")
                .sessionAttr("reservationFlow", reservationFlow)
                .params(DateFormParams.checkOutOccursBeforeCheckInParams(timeProvider)))
                .andExpect(view().name("reservation/dates"))
                .andExpect(model().errorCount(1))
                .andExpect(model().attributeHasFieldErrorCode("reservationFlow", "reservation.dates", "checkOutDate.afterCheckIn"))
                .andExpect(modelHasActiveFlowStep(ReservationFlow.Step.Dates))
                .andExpect(modelHasIncompleteFlowStep(ReservationFlow.Step.Dates));
    }

    @Test
    public void postDateForm_GlobalValidation_SatisfiesMinimumNightStay() throws Exception {
        ReservationFlow reservationFlow = toDateFlow();
        when(roomRepository.findById(anyLong())).thenReturn(Optional.of(reservationFlow.getReservation().getRoom()));
        when(timeProvider.localDate()).thenReturn(LocalDate.now());

        mockMvc.perform(post("/reservation/dates")
                .sessionAttr("reservationFlow", reservationFlow)
                .params(DateFormParams.minimumNightsParams(timeProvider)))
                .andExpect(view().name("reservation/dates"))
                .andExpect(model().errorCount(1))
                .andExpect(model().attributeHasFieldErrorCode("reservationFlow", "reservation.dates", "checkOutDate.minNights"))
                .andExpect(modelHasActiveFlowStep(ReservationFlow.Step.Dates))
                .andExpect(modelHasIncompleteFlowStep(ReservationFlow.Step.Dates));
    }

    @Test
    public void postDateForm_Valid_RedirectToNextView() throws Exception {
        ReservationFlow reservationFlow = toDateFlow();
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
        ReservationFlow reservationFlow = toDateFlow();
        when(timeProvider.localDate()).thenReturn(LocalDate.now());

        mockMvc.perform(post("/reservation/dates?prices")
                .sessionAttr("reservationFlow", reservationFlow)
                .params(DateFormParams.validParams(timeProvider)))
                .andExpect(view().name("reservation/fragments :: roomCosts"));
    }

    // Flow step 2 - guests

    @Test
    public void getGuestForm_HasValidStartingState() throws Exception {
        ReservationFlow reservationFlow = toDateFlow();

        mockMvc.perform(get("/reservation/guests")
                .sessionAttr("reservationFlow", reservationFlow))
                .andExpect(view().name("reservation/guests"))
                .andExpect(model().attribute("guest", Matchers.is(new Guest())))
                .andExpect(modelHasActiveFlowStep(ReservationFlow.Step.Guests))
                .andExpect(modelHasIncompleteFlowStep(ReservationFlow.Step.Guests));
    }

    @Test
    public void postGuestForm_GoBackToDateView_ReturnsCorrectViewAndFlowStep() throws Exception {
        ReservationFlow reservationFlow = toDateFlow();

        mockMvc.perform(post("/reservation/guests")
                .param("back", "")
                .sessionAttr("reservationFlow", reservationFlow))
                .andExpect(view().name("redirect:/reservation/dates"))
                .andExpect(flash().attributeExists("reservationFlow"))
                .andExpect(flashHasActiveFlowStep(ReservationFlow.Step.Dates))
                .andExpect(flashHasIncompleteFlowStep(ReservationFlow.Step.Dates));
    }

    @Test
    public void postAddGuest_BeanValidationError_NullFields() throws Exception {
        ReservationFlow reservationFlow = toGuestFlow();

        ResultMatcher didNotAddGuest = model().attribute("reservationFlow",
                Matchers.hasProperty("reservation",
                        Matchers.hasProperty("guests", Matchers.hasSize(0))));

        // The binding stage will end up creating a new Guest but there will be nothing in it.
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
                .andExpect(didNotCreateNewGuestObject);
    }

    @Test
    public void postAddGuest_BeanValidationError_EmptyFields() throws Exception {
        ReservationFlow reservationFlow = toGuestFlow();

        ResultMatcher didNotAddGuest = model().attribute("reservationFlow",
                Matchers.hasProperty("reservation",
                        Matchers.hasProperty("guests", Matchers.hasSize(0))));

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
                .andExpect(didNotCreateNewGuestObject);
    }

    @Test
    public void postAddGuest_GuestExists_HasGlobalError() throws Exception {
        ReservationFlow reservationFlow = toGuestFlow();

        // case should be irrelevant in detecting duplicates
        reservationFlow.getReservation().addGuest(new Guest("JoHn", "sMITh", false));

        ResultMatcher didNotAddGuest = model().attribute("reservationFlow",
                Matchers.hasProperty("reservation",
                        Matchers.hasProperty("guests", Matchers.hasSize(1))));

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
                .andExpect(didNotCreateNewGuestObject);
    }

    @Test
    public void postAddGuest_GuestLimitExceeded_HasGlobalError() throws Exception {
        ReservationFlow reservationFlow = toGuestFlow();

        // next guest added should be denied.
        reservationFlow.getReservation().getRoom().setBeds(1);
        reservationFlow.getReservation().addGuest(new Guest("john", "smith", false));

        ResultMatcher didNotAddGuest = model().attribute("reservationFlow",
                Matchers.hasProperty("reservation",
                        Matchers.hasProperty("guests", Matchers.hasSize(1))));

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
                .andExpect(didNotCreateNewGuestObject);
    }

    /**
     * Asserts that a valid guest is added to the guest set and a new {@code Guest} is added to the model so
     * the UI form binds to a fresh object.
     */
    @Test
    public void postAddGuest_Valid() throws Exception {
        ReservationFlow reservationFlow = toGuestFlow();

        reservationFlow.getReservation().getRoom().setBeds(1);

        ResultMatcher reservationContainsGuest = model().attribute("reservationFlow",
                Matchers.hasProperty("reservation",
                        Matchers.hasProperty("guests",
                                Matchers.containsInAnyOrder(new Guest("marie", "clarke", false)))));

        // expect to create a new guest object to bind to the new form.
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
                .andExpect(modelHasIncompleteFlowStep(ReservationFlow.Step.Guests));
    }

    /**
     * Id not found should never happen as the button will always contain a guest that exists.
     * Note that a new {@code Guest} must be in the model to bind to the guest form.
     */
    @Test
    public void postRemoveGuest_GuestIdNotFound_ShouldHaveNoEffect() throws Exception {
        ReservationFlow reservationFlow = toGuestFlow();

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
                .andExpect(modelHasIncompleteFlowStep(ReservationFlow.Step.Guests));
    }

    /**
     * Note that a new {@code Guest} must be in the model to bind to the guest form.
     */
    @Test
    public void postRemoveGuest_GuestIdFound_ShouldBeRemoved() throws Exception {
        ReservationFlow reservationFlow = toGuestFlow();
        reservationFlow.getReservation().getRoom().setBeds(2);

        Guest guestA = new Guest("john", "smith", false);
        Guest guestB = new Guest("nicole", "smith", false);

        reservationFlow.getReservation().addGuest(guestA);
        reservationFlow.getReservation().addGuest(guestB);

        assertThat(reservationFlow.getReservation().getGuests()).containsExactlyInAnyOrder(guestA, guestB);

        ResultMatcher hasExpectedGuests = model().attribute("reservationFlow",
                Matchers.hasProperty("reservation",
                        Matchers.hasProperty("guests", Matchers.containsInAnyOrder(guestB))));

        mockMvc.perform(post("/reservation/guests")
                .sessionAttr("reservationFlow", reservationFlow)
                .param("removeGuest", guestA.getGuestId().toString()))
                .andExpect(view().name("reservation/guests"))
                .andExpect(model().hasNoErrors())
                .andExpect(model().attribute("guest", Matchers.is(new Guest())))
                .andExpect(hasExpectedGuests)
                .andExpect(modelHasActiveFlowStep(ReservationFlow.Step.Guests))
                .andExpect(modelHasIncompleteFlowStep(ReservationFlow.Step.Guests));
    }


    /**
     * Transition from Guest form to Extras form and perform final guest checks.
     *
     * <p>Since the addGuest handler validates guest adding logic, this prevents most invalid states. Transitioning
     * only requires checking if at least 1 guest exists.
     */
    @Test
    public void postGuestToExtras_NoGuestExists_ExpectGlobalError() throws Exception {
        ReservationFlow reservationFlow = toGuestFlow();
        reservationFlow.getReservation().clearGuests();

        mockMvc.perform(post("/reservation/guests")
                .sessionAttr("reservationFlow", reservationFlow))
                .andExpect(view().name("reservation/guests"))
                .andExpect(model().errorCount(1))
                .andExpect(globalErrorMatchers().hasGlobalErrorCode("guest", "guests.noneExist"))
                .andExpect(modelHasActiveFlowStep(ReservationFlow.Step.Guests))
                .andExpect(modelHasIncompleteFlowStep(ReservationFlow.Step.Guests));
    }

    @Test
    public void postGuestToExtras_Valid() throws Exception {
        ReservationFlow reservationFlow = toGuestFlow();
        reservationFlow.getReservation().getRoom().setBeds(3);
        reservationFlow.getReservation().addGuest(new Guest("john", "smith", false));

        mockMvc.perform(post("/reservation/guests")
                .sessionAttr("reservationFlow", reservationFlow))
                .andExpect(view().name("redirect:/reservation/extras"))
                .andExpect(model().hasNoErrors())
                .andExpect(flashHasActiveFlowStep(ReservationFlow.Step.Guests))
                .andExpect(flashHasCompletedFlowStep(ReservationFlow.Step.Guests));
    }


}