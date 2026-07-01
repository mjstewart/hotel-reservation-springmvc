package com.demo.domain;

import org.junit.Test;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

/**
 * Testes unitários para {@link ReservationDates}.
 *
 * Cobre todas as ramificações do método validate() e o cálculo de totalNights().
 */
public class ReservationDatesTest {

    // ==================== validate() ====================

    /**
     * Quando a data de check-in é null, deve retornar erro com código "checkInDate.missing".
     */
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

    /**
     * Quando a data de check-out é null, deve retornar erro com código "checkOutDate.missing".
     */
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

    /**
     * Quando a data de check-in é anterior à data atual, deve retornar erro
     * com código "checkInDate.future".
     */
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

    /**
     * Quando a data de check-out é anterior à data de check-in, deve retornar erro
     * com código "checkOutDate.afterCheckIn".
     */
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

    /**
     * Quando check-in e check-out são no mesmo dia (0 noites), deve retornar erro
     * com código "checkOutDate.minNights".
     */
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

    /**
     * Quando ambas as datas são null, totalNights deve retornar 0.
     */
    @Test
    public void totalNights_BothDatesNull_ReturnsZero() {
        ReservationDates dates = new ReservationDates();
        assertThat(dates.totalNights()).isEqualTo(0);
    }

    /**
     * Quando apenas checkInDate é null, totalNights deve retornar 0.
     */
    @Test
    public void totalNights_CheckInNull_ReturnsZero() {
        ReservationDates dates = new ReservationDates();
        dates.setCheckOutDate(LocalDate.of(2026, 7, 10));
        assertThat(dates.totalNights()).isEqualTo(0);
    }

    /**
     * Quando apenas checkOutDate é null, totalNights deve retornar 0.
     */
    @Test
    public void totalNights_CheckOutNull_ReturnsZero() {
        ReservationDates dates = new ReservationDates();
        dates.setCheckInDate(LocalDate.of(2026, 7, 5));
        assertThat(dates.totalNights()).isEqualTo(0);
    }

    /**
     * Quando as datas são válidas, totalNights deve retornar o número correto de noites.
     */
    @Test
    public void totalNights_ValidDates_ReturnsCorrectCount() {
        ReservationDates dates = new ReservationDates();
        dates.setCheckInDate(LocalDate.of(2026, 7, 1));
        dates.setCheckOutDate(LocalDate.of(2026, 7, 4));

        assertThat(dates.totalNights()).isEqualTo(3);
    }

    /**
     * Para uma estadia de 1 noite, totalNights deve retornar 1.
     */
    @Test
    public void totalNights_OneNight_ReturnsOne() {
        ReservationDates dates = new ReservationDates();
        dates.setCheckInDate(LocalDate.of(2026, 7, 1));
        dates.setCheckOutDate(LocalDate.of(2026, 7, 2));

        assertThat(dates.totalNights()).isEqualTo(1);
    }
}
