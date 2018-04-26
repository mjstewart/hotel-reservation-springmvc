package com.demo.domain;

import org.springframework.format.annotation.DateTimeFormat;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.validation.constraints.AssertTrue;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

@Embeddable
public class ReservationDates {
    @Column(nullable = false)
    @NotNull(message = "Check in date required")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate checkInDate;

    @Column(nullable = false)
    @NotNull(message = "Check out date required")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate checkOutDate;

    @Column(nullable = false)
    @NotNull(message = "Estimated check in time required")
    @DateTimeFormat(iso = DateTimeFormat.ISO.TIME)
    private LocalTime estimatedCheckInTime;

    @Column(nullable = false)
    private boolean lateCheckout = false;

    @Column(nullable = false)
    @AssertTrue(message = "Please acknowledge policy")
    private boolean policyAcknowledged = false;

    public ReservationDates() {
    }

    public ReservationDates(LocalDate checkInDate, LocalDate checkOutDate,
                            LocalTime estimatedCheckInTime, boolean lateCheckout,
                            boolean policyAcknowledged) {
        this.checkInDate = checkInDate;
        this.checkOutDate = checkOutDate;
        this.estimatedCheckInTime = estimatedCheckInTime;
        this.lateCheckout = lateCheckout;
        this.policyAcknowledged = policyAcknowledged;
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
    public boolean isPolicyAcknowledged() {
        return policyAcknowledged;
    }

    public void setPolicyAcknowledged(boolean policyAcknowledged) {
        this.policyAcknowledged = policyAcknowledged;
    }

    public long totalNights() {
        if (checkInDate == null || checkOutDate == null) {
            return 0;
        }
        return ChronoUnit.DAYS.between(checkInDate, checkOutDate);
    }

    public Optional<ValidationError> validate(LocalDate now) {
        if (checkInDate == null) {
            return Optional.of(new ValidationError("checkInDate.missing", "Missing check in date"));
        } else if (checkOutDate == null) {
            return Optional.of(new ValidationError("checkOutDate.missing", "Missing check out date"));
        } else if (checkInDate.isBefore(now)) {
            return Optional.of(new ValidationError("checkInDate.future", "Check in date must be in the future"));
        } else if (checkOutDate.isBefore(checkInDate)) {
            return Optional.of(new ValidationError("checkOutDate.afterCheckIn", "Check out date must occur after check in date"));
        } else if (totalNights() < 1) {
            // handles case where check in/out dates are the same.
            return Optional.of(new ValidationError("checkOutDate.minNights", "Reservation must be for at least 1 night"));
        }
        return Optional.empty();
    }

    public static class ValidationError {
        private String code;
        private String reason;

        public ValidationError(String code, String reason) {
            this.code = code;
            this.reason = reason;
        }

        public String getCode() {
            return code;
        }

        public String getReason() {
            return reason;
        }
    }

    @Override
    public String toString() {
        return "ReservationDates{" +
                "checkInDate=" + checkInDate +
                ", checkOutDate=" + checkOutDate +
                ", estimatedCheckInTime=" + estimatedCheckInTime +
                ", lateCheckout=" + lateCheckout +
                ", policyAcknowledged=" + policyAcknowledged +
                '}';
    }
}
