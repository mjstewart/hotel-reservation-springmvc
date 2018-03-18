package com.demo.domain;

import org.springframework.format.annotation.DateTimeFormat;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;

@Embeddable
public class ReservationDates {
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    @Column(nullable = false)
    private LocalDate checkInDate;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    @Column(nullable = false)
    private LocalDate checkOutDate;

    @Column(nullable = false)
    private LocalTime estimatedCheckInTime;

    @Column(nullable = false)
    private boolean lateCheckout = false;

    @Column(nullable = false)
    private boolean checkoutAcknowledged = false;

    public ReservationDates() {
    }

    public LocalDate getCheckInDate() {
        return checkInDate;
    }

    public void setCheckInDate(LocalDate checkInDate) {
        this.checkInDate = checkInDate;
    }

    public LocalDate getCheckOutDate() {
        return checkOutDate;
    }

    public void setCheckOutDate(LocalDate checkOutDate) {
        this.checkOutDate = checkOutDate;
    }

    public LocalTime getEstimatedCheckInTime() {
        return estimatedCheckInTime;
    }

    public void setEstimatedCheckInTime(LocalTime estimatedCheckInTime) {
        this.estimatedCheckInTime = estimatedCheckInTime;
    }

    /**
     * @return {@code true} if the late checkout option is selected
     */
    public boolean isLateCheckout() {
        return lateCheckout;
    }

    public void setLateCheckout(boolean lateCheckout) {
        this.lateCheckout = lateCheckout;
    }

    /**
     * @return {@code true} if check out terms and conditions have been accepted.
     */
    public boolean isCheckoutAcknowledged() {
        return checkoutAcknowledged;
    }

    public void setCheckoutAcknowledged(boolean checkoutAcknowledged) {
        this.checkoutAcknowledged = checkoutAcknowledged;
    }

    public long totalNights() {
        if (checkInDate == null || checkOutDate == null) {
            return 0;
        }
        return ChronoUnit.DAYS.between(checkInDate, checkOutDate);
    }
}
