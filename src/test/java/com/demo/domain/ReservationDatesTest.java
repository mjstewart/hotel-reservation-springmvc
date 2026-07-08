package com.demo.domain;

import org.junit.Test;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;


public class ReservationDatesTest {

    // ==================== validate() ====================


    @Test
    public void validate_NullCheckInDate_ReturnsError() {
        ReservationDates dates = new ReservationDates();
        dates.setCheckOutDate(LocalDate.of(2026, 7, 10));
        dates.setEstimatedCheckInTime(LocalTime.of(10, 0));

        Optional<ReservationDates.ValidationError> result = dates.validate(LocalDate.of(2026, 7, 1));

        assertThat(result).isPresent();
        assertThat(result.get().getCode()).isEqualTo("checkInDate.missing");
        assertThat(result.get().getReason()).isEqualTo("Missing check in date");
    }


    @Test
    public void validate_NullCheckOutDate_ReturnsError() {
        ReservationDates dates = new ReservationDates();
        dates.setCheckInDate(LocalDate.of(2026, 7, 5));
        dates.setEstimatedCheckInTime(LocalTime.of(10, 0));

        Optional<ReservationDates.ValidationError> result = dates.validate(LocalDate.of(2026, 7, 1));

        assertThat(result).isPresent();
        assertThat(result.get().getCode()).isEqualTo("checkOutDate.missing");
        assertThat(result.get().getReason()).isEqualTo("Missing check out date");
    }


    @Test
    public void validate_CheckInInPast_ReturnsError() {
        LocalDate now = LocalDate.of(2026, 7, 5);
        ReservationDates dates = new ReservationDates(
                now.minusDays(1), now.plusDays(2),
                LocalTime.of(10, 0), false, true
        );

        Optional<ReservationDates.ValidationError> result = dates.validate(now);

        assertThat(result).isPresent();
        assertThat(result.get().getCode()).isEqualTo("checkInDate.future");
        assertThat(result.get().getReason()).isEqualTo("Check in date must be in the future");
    }


    @Test
    public void validate_CheckOutBeforeCheckIn_ReturnsError() {
        LocalDate now = LocalDate.of(2026, 7, 5);
        ReservationDates dates = new ReservationDates(
                now, now.minusDays(1),
                LocalTime.of(10, 0), false, true
        );

        Optional<ReservationDates.ValidationError> result = dates.validate(now);

        assertThat(result).isPresent();
        assertThat(result.get().getCode()).isEqualTo("checkOutDate.afterCheckIn");
    }


    @Test
    public void validate_SameDay_ReturnsMinNightsError() {
        LocalDate now = LocalDate.of(2026, 7, 5);
        ReservationDates dates = new ReservationDates(
                now, now,
                LocalTime.of(10, 0), false, true
        );

        Optional<ReservationDates.ValidationError> result = dates.validate(now);

        assertThat(result).isPresent();
        assertThat(result.get().getCode()).isEqualTo("checkOutDate.minNights");
        assertThat(result.get().getReason()).isEqualTo("Reservation must be for at least 1 night");
    }

    /**
     * Quando as datas são válidas (check-in hoje, check-out amanhã), deve retornar vazio.
     */
    @Test
    public void validate_ValidDates_ReturnsEmpty() {
        LocalDate now = LocalDate.of(2026, 7, 5);
        ReservationDates dates = new ReservationDates(
                now, now.plusDays(3),
                LocalTime.of(10, 0), false, true
        );

        Optional<ReservationDates.ValidationError> result = dates.validate(now);

        assertThat(result).isEmpty();
    }

    // ==================== totalNights() ====================

    @Test
    public void totalNights_BothDatesNull_ReturnsZero() {
        ReservationDates dates = new ReservationDates();
        assertThat(dates.totalNights()).isEqualTo(0);
    }

    @Test
    public void totalNights_CheckInNull_ReturnsZero() {
        ReservationDates dates = new ReservationDates();
        dates.setCheckOutDate(LocalDate.of(2026, 7, 10));
        assertThat(dates.totalNights()).isEqualTo(0);
    }

    @Test
    public void totalNights_CheckOutNull_ReturnsZero() {
        ReservationDates dates = new ReservationDates();
        dates.setCheckInDate(LocalDate.of(2026, 7, 5));
        assertThat(dates.totalNights()).isEqualTo(0);
    }


    @Test
    public void totalNights_ValidDates_ReturnsCorrectCount() {
        ReservationDates dates = new ReservationDates();
        dates.setCheckInDate(LocalDate.of(2026, 7, 1));
        dates.setCheckOutDate(LocalDate.of(2026, 7, 4));

        assertThat(dates.totalNights()).isEqualTo(3);
    }

    @Test
    public void totalNights_OneNight_ReturnsOne() {
        ReservationDates dates = new ReservationDates();
        dates.setCheckInDate(LocalDate.of(2026, 7, 1));
        dates.setCheckOutDate(LocalDate.of(2026, 7, 2));

        assertThat(dates.totalNights()).isEqualTo(1);
    }
}
