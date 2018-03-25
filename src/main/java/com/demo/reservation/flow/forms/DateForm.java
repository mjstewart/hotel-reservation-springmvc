package com.demo.reservation.flow.forms;

import com.demo.domain.ReservationDates;

import javax.validation.Valid;

public class DateForm {
    @Valid
    private ReservationDates reservationDates = new ReservationDates();

    public DateForm() {
    }

    public ReservationDates getReservationDates() {
        return reservationDates;
    }

    public void setReservationDates(ReservationDates reservationDates) {
        this.reservationDates = reservationDates;
    }
}
