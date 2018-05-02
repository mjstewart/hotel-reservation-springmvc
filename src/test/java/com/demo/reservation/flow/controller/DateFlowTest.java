package com.demo.reservation.flow.controller;

import com.demo.TimeProvider;
import com.demo.domain.Reservation;
import com.demo.domain.ReservationDates;
import com.demo.domain.Room;
import com.demo.persistance.RoomRepository;
import com.demo.reservation.ExtraRepository;
import com.demo.reservation.flow.ReservationController;
import com.demo.reservation.flow.forms.ReservationFlow;
import com.demo.reservation.flow.helpers.FlowMatchers;
import com.demo.reservation.flow.helpers.FlowStages;
import org.hamcrest.Matchers;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.util.LinkedMultiValueMap;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

import static com.demo.reservation.flow.helpers.FlowMatchers.modelHasActiveFlowStep;
import static com.demo.reservation.flow.helpers.FlowMatchers.modelHasIncompleteFlowStep;
import static com.demo.reservation.flow.helpers.FlowStages.pendingDateFlow;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@RunWith(SpringRunner.class)
@WebMvcTest(ReservationController.class)
@ActiveProfiles("test")
public class DateFlowTest {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private RoomRepository roomRepository;

    @MockBean
    private ExtraRepository extraRepository;

    @MockBean
    private TimeProvider timeProvider;

    /**
     * Creates form params to simulate POST.
     * <p>
     * <p>{@code reservation.dates.checkInDate} is the path spring uses to bind request param to model. For example,
     * {@code ReservationController.dates} has the {@code ModelAttribute} of {@code ReservationFlow} which will contain
     * the form body.</p>
     * <p>
     * <p>Using {@code ReservationFlow} as the root object containing the form fields, spring will use reflection to
     * instantiate a {@code ReservationDates} by using the navigation path of {@code reservation.dates.checkInDate} etc.</p>
     */
    public static LinkedMultiValueMap<String, String> toReservationDatesParams(ReservationDates dates) {
        LinkedMultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.put("reservation.dates.checkInDate", List.of(dates.getCheckInDate().format(DateTimeFormatter.ISO_DATE)));
        params.put("reservation.dates.checkOutDate", List.of(dates.getCheckOutDate().format(DateTimeFormatter.ISO_DATE)));
        params.put("reservation.dates.estimatedCheckInTime", List.of(dates.getEstimatedCheckInTime().format(DateTimeFormatter.ISO_TIME)));
        params.put("reservation.dates.lateCheckout", List.of(dates.isLateCheckout() + ""));
        params.put("reservation.dates.policyAcknowledged", List.of(dates.isPolicyAcknowledged() + ""));
        return params;
    }

    /**
     * Check in date is the in the past
     */
    public static LinkedMultiValueMap<String, String> checkInNotInFutureParams(TimeProvider timeProvider) {
        LocalDate checkIn = timeProvider.localDate().minusDays(1);
        LocalDate checkOut = checkIn.plusDays(1);
        LocalTime estimatedCheckInTime = LocalTime.of(10, 0);
        ReservationDates dates = new ReservationDates(checkIn, checkOut, estimatedCheckInTime,
                true, true);
        return toReservationDatesParams(dates);
    }

    /**
     * Total night stay is 0 which handles check in/out dates being the same.
     */
    public static LinkedMultiValueMap<String, String> minimumNightsParams(TimeProvider timeProvider) {
        LocalDate checkIn = timeProvider.localDate();
        LocalTime estimatedCheckInTime = LocalTime.of(10, 0);
        ReservationDates dates = new ReservationDates(checkIn, checkIn, estimatedCheckInTime,
                true, true);
        return toReservationDatesParams(dates);
    }

    /**
     * Checkout date is before the check in
     */
    public static LinkedMultiValueMap<String, String> checkOutOccursBeforeCheckInParams(TimeProvider timeProvider) {
        LocalDate checkIn = timeProvider.localDate();
        LocalDate checkOut = checkIn.minusDays(1);
        LocalTime estimatedCheckInTime = LocalTime.of(10, 0);
        ReservationDates dates = new ReservationDates(checkIn, checkOut, estimatedCheckInTime,
                true, true);
        return toReservationDatesParams(dates);
    }

    public static LinkedMultiValueMap<String, String> noPolicyAcknowledgementParams(TimeProvider timeProvider) {
        LocalDate checkIn = timeProvider.localDate();
        LocalDate checkOut = checkIn.plusDays(1);
        LocalTime estimatedCheckInTime = LocalTime.of(10, 0);
        ReservationDates dates = new ReservationDates(checkIn, checkOut, estimatedCheckInTime,
                false, false);
        return toReservationDatesParams(dates);
    }

    public static LinkedMultiValueMap<String, String> validParams(TimeProvider timeProvider) {
        LocalDate checkIn = timeProvider.localDate();
        LocalDate checkOut = checkIn.plusDays(1);
        LocalTime estimatedCheckInTime = LocalTime.of(10, 0);
        ReservationDates dates = new ReservationDates(checkIn, checkOut, estimatedCheckInTime,
                false, true);
        return toReservationDatesParams(dates);
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
        Room room = FlowStages.createRoom();
        when(roomRepository.findById(anyLong())).thenReturn(Optional.of(room));

        mockMvc.perform(get("/reservation?roomId=5"))
                .andExpect(status().isOk())
                .andExpect(modelHasActiveFlowStep(ReservationFlow.Step.Dates))
                .andExpect(modelHasIncompleteFlowStep(ReservationFlow.Step.Dates));

        verify(roomRepository, times(1)).findById(anyLong());
    }

    @Test
    public void getDateForm_ReturnsCorrectView() throws Exception {
        Room room = FlowStages.createRoom();
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
        Room room = FlowStages.createRoom();
        when(roomRepository.findById(anyLong())).thenReturn(Optional.of(room));


        mockMvc.perform(get("/reservation?roomId=5"))
                .andExpect(model().attributeExists("reservationFlow"))
                .andExpect(model().attribute("reservationFlow",
                        Matchers.hasProperty("reservation", Matchers.allOf(
                                Matchers.hasProperty("room", Matchers.is(room)),
                                Matchers.hasProperty("room", Matchers.hasProperty("reservation", Matchers.notNullValue(Reservation.class))),
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
                .params(new LinkedMultiValueMap<>()))
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
                .params(checkInNotInFutureParams(timeProvider)))
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
                .params(checkOutOccursBeforeCheckInParams(timeProvider)))
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
                .params(minimumNightsParams(timeProvider)))
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
                .params(validParams(timeProvider)))
                .andExpect(view().name("redirect:/reservation/guests"))
                .andExpect(flash().attributeExists("reservationFlow"))
                .andExpect(FlowMatchers.flashHasActiveFlowStep(ReservationFlow.Step.Dates))
                .andExpect(FlowMatchers.flashHasCompletedFlowStep(ReservationFlow.Step.Dates));
    }

    // Ajax dynamic room price fragment

    @Test
    public void roomCostFragment_ReturnsCorrectFragment() throws Exception {
        ReservationFlow reservationFlow = pendingDateFlow();
        when(timeProvider.localDate()).thenReturn(LocalDate.now());

        mockMvc.perform(post("/reservation/dates?prices")
                .sessionAttr("reservationFlow", reservationFlow)
                .params(validParams(timeProvider)))
                .andExpect(view().name("reservation/fragments :: roomCosts"));
    }
}
