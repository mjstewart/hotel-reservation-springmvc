package com.demo.reservation;

import com.demo.TimeProvider;
import com.demo.domain.Hotel;
import com.demo.domain.ReservationDates;
import com.demo.domain.Room;
import com.demo.domain.RoomType;
import com.demo.persistance.RoomRepository;
import com.demo.reservation.flow.ReservationController;
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
import org.springframework.util.LinkedMultiValueMap;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

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
        Hotel hotel = new Hotel("Hotel Royal", "Melbourne", 3, "royal@hotel.com");
        Room room = new Room("ABC123", RoomType.Economy, 4, BigDecimal.valueOf(23.50));
        room.setHotel(hotel);
        return room;
    }

    private ReservationFlow createReservationFlow() {
        ReservationFlow reservationFlow = new ReservationFlow();
        reservationFlow.getReservation().setRoom(createRoom());
        return reservationFlow;
    }

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
    private LinkedMultiValueMap<String, String> toParams(ReservationDates dates) {
        LinkedMultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.put("reservation.dates.checkInDate", List.of(dates.getCheckInDate().format(DateTimeFormatter.ISO_DATE)));
        params.put("reservation.dates.checkOutDate", List.of(dates.getCheckOutDate().format(DateTimeFormatter.ISO_DATE)));
        params.put("reservation.dates.estimatedCheckInTime", List.of(dates.getEstimatedCheckInTime().format(DateTimeFormatter.ISO_TIME)));
        params.put("reservation.dates.lateCheckout", List.of(dates.isLateCheckout() + ""));
        params.put("reservation.dates.policyAcknowledged", List.of(dates.isPolicyAcknowledged() + ""));
        return params;
    }

    private LinkedMultiValueMap<String, String> emptyParams() {
        return new LinkedMultiValueMap<>();
    }

    /**
     * Checkout date is before the check in
     */
    private LinkedMultiValueMap<String, String> checkOutOccursBeforeCheckInParams(TimeProvider timeProvider) {
        LocalDate checkIn = timeProvider.localDate();
        LocalDate checkOut = checkIn.minusDays(1);
        LocalTime estimatedCheckInTime = LocalTime.of(10, 0);
        ReservationDates dates = new ReservationDates(checkIn, checkOut, estimatedCheckInTime,
                true, true);
        return toParams(dates);
    }

    /**
     * Check in date is the in the past
     */
    private LinkedMultiValueMap<String, String> checkInNotInFutureParams(TimeProvider timeProvider) {
        LocalDate checkIn = timeProvider.localDate().minusDays(1);
        LocalDate checkOut = checkIn.plusDays(1);
        LocalTime estimatedCheckInTime = LocalTime.of(10, 0);
        ReservationDates dates = new ReservationDates(checkIn, checkOut, estimatedCheckInTime,
                true, true);
        return toParams(dates);
    }

    /**
     * Total night stay is 0
     */
    private LinkedMultiValueMap<String, String> minimumNightsParams(TimeProvider timeProvider) {
        LocalDate checkIn = timeProvider.localDate();
        LocalTime estimatedCheckInTime = LocalTime.of(10, 0);
        ReservationDates dates = new ReservationDates(checkIn, checkIn, estimatedCheckInTime,
                true, true);
        return toParams(dates);
    }

    private LinkedMultiValueMap<String, String> validParams(TimeProvider timeProvider) {
        LocalDate checkIn = timeProvider.localDate();
        LocalDate checkOut = checkIn.plusDays(1);
        LocalTime estimatedCheckInTime = LocalTime.of(10, 0);
        ReservationDates dates = new ReservationDates(checkIn, checkOut, estimatedCheckInTime,
                false, true);
        return toParams(dates);
    }

    private LinkedMultiValueMap<String, String> noPolicyAcknowledgementParams(TimeProvider timeProvider) {
        LocalDate checkIn = timeProvider.localDate();
        LocalDate checkOut = checkIn.plusDays(1);
        LocalTime estimatedCheckInTime = LocalTime.of(10, 0);
        ReservationDates dates = new ReservationDates(checkIn, checkOut, estimatedCheckInTime,
                false, false);
        return toParams(dates);
    }

    // Flow step 1 - GET date form

    @Test
    public void getDateForm_RetrievesRoomById() throws Exception {
        Room room = createRoom();
        when(roomRepository.findById(anyLong())).thenReturn(Optional.of(room));

        mockMvc.perform(get("/reservation?roomId=5"))
                .andExpect(status().isOk());

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
    public void postDateForm_EmptyForm_ExpectedBeanValidationErrors() throws Exception {
        ReservationFlow reservationFlow = createReservationFlow();
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
                .andExpect(model().attribute("reservationFlow", Matchers.hasProperty("step", Matchers.is(0))));
    }

    @Test
    public void postDateForm_GlobalValidation_CheckIn_NotInPast() throws Exception {
        ReservationFlow reservationFlow = createReservationFlow();
        when(roomRepository.findById(anyLong())).thenReturn(Optional.of(reservationFlow.getReservation().getRoom()));
        when(timeProvider.localDate()).thenReturn(LocalDate.now());

        mockMvc.perform(post("/reservation/dates")
                .sessionAttr("reservationFlow", reservationFlow)
                .params(checkInNotInFutureParams(timeProvider)))
                .andExpect(view().name("reservation/dates"))
                .andExpect(model().errorCount(1))
                .andExpect(model().attributeHasFieldErrorCode("reservationFlow", "reservation.dates", "checkInDate.future"))
                .andExpect(model().attribute("reservationFlow", Matchers.hasProperty("step", Matchers.is(0))));
    }

    @Test
    public void postDateForm_GlobalValidation_Checkout_OccursAfterCheckIn() throws Exception {
        ReservationFlow reservationFlow = createReservationFlow();
        when(roomRepository.findById(anyLong())).thenReturn(Optional.of(reservationFlow.getReservation().getRoom()));
        when(timeProvider.localDate()).thenReturn(LocalDate.now());

        mockMvc.perform(post("/reservation/dates")
                .sessionAttr("reservationFlow", reservationFlow)
                .params(checkOutOccursBeforeCheckInParams(timeProvider)))
                .andExpect(view().name("reservation/dates"))
                .andExpect(model().errorCount(1))
                .andExpect(model().attributeHasFieldErrorCode("reservationFlow", "reservation.dates", "checkOutDate.afterCheckIn"))
                .andExpect(model().attribute("reservationFlow", Matchers.hasProperty("step", Matchers.is(0))));
    }

    @Test
    public void postDateForm_GlobalValidation_SatisfiesMinimumNightStay() throws Exception {
        ReservationFlow reservationFlow = createReservationFlow();
        when(roomRepository.findById(anyLong())).thenReturn(Optional.of(reservationFlow.getReservation().getRoom()));
        when(timeProvider.localDate()).thenReturn(LocalDate.now());

        mockMvc.perform(post("/reservation/dates")
                .sessionAttr("reservationFlow", reservationFlow)
                .params(minimumNightsParams(timeProvider)))
                .andExpect(view().name("reservation/dates"))
                .andExpect(model().errorCount(1))
                .andExpect(model().attributeHasFieldErrorCode("reservationFlow", "reservation.dates", "checkOutDate.minNights"))
                .andExpect(model().attribute("reservationFlow", Matchers.hasProperty("step", Matchers.is(0))));
    }

    @Test
    public void postDateForm_Valid_RedirectToNextView() throws Exception {
        ReservationFlow reservationFlow = createReservationFlow();
        when(roomRepository.findById(anyLong())).thenReturn(Optional.of(reservationFlow.getReservation().getRoom()));
        when(timeProvider.localDate()).thenReturn(LocalDate.now());

        mockMvc.perform(post("/reservation/dates")
                .sessionAttr("reservationFlow", reservationFlow)
                .params(validParams(timeProvider)))
                .andExpect(view().name("redirect:/reservation/guests"))
                .andExpect(flash().attribute("reservationFlow",
                        Matchers.hasProperty("step", Matchers.is(0))));
    }

    // Flow step 1 - Ajax dynamic room price fragment

    @Test
    public void roomCostFragment_ReturnsCorrectFragment() throws Exception {
        ReservationFlow reservationFlow = createReservationFlow();
        when(timeProvider.localDate()).thenReturn(LocalDate.now());

        mockMvc.perform(post("/reservation/dates?prices")
                .sessionAttr("reservationFlow", reservationFlow)
                .params(validParams(timeProvider)))
                .andExpect(view().name("reservation/fragments :: roomCosts"));
    }

    // Flow step 2 - guests

    @Test
    public void getGuestForm_CorrectViewAndFlowStep() throws Exception {
        ReservationFlow reservationFlow = createReservationFlow();

        mockMvc.perform(get("/reservation/guests")
                .sessionAttr("reservationFlow", reservationFlow))
                .andExpect(view().name("reservation/guests"))
                .andExpect(model().attribute("reservationFlow",
                        Matchers.hasProperty("step", Matchers.is(1))));
    }
}

