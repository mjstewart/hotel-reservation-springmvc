package com.demo.reservation.flow.forms;

import com.demo.TimeProvider;
import com.demo.domain.ReservationDates;
import org.springframework.util.LinkedMultiValueMap;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class DateFormParams {

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
}
