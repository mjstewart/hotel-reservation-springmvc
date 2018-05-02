package com.demo.domain;

import com.demo.converters.LocalDateTimeConverter;
import org.springframework.format.annotation.DateTimeFormat;

import javax.persistence.Convert;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import java.time.*;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Binds to payment form. Its assumed the payment would get processed then saved
 * using another domain object to represent the transaction.
 */
public class PendingPayment {
    @NotNull(message = "required")
    private CreditCardType creditCardType;

    @Pattern(regexp = "[0-9]{10}", message = "10 digit number expected")
    @NotNull
    private String creditCardNumber;

    @Pattern(regexp = "[0-9]{3}", message = "3 digit number expected")
    @NotNull
    private String cvv;

    @NotEmpty
    private String cardHolderName;

    @NotNull(message = "required")
    private Year cardExpiryYear;

    @NotNull(message = "required")
    private Month cardExpiryMonth;

    // This is the format, it doesnt tell spring how to convert from this format back into a date
    // which is why the LocalDateTimeConverter was created for this.
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime createdTime;

    public enum CreditCardType {
        MasterCard("Mastercard"),
        Visa("Visa");

        private String description;

        CreditCardType(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    /**
     * @param createdTime Used as a reference point to generate valid credit card expiry dates.
     */
    public PendingPayment(LocalDateTime createdTime) {
        this.createdTime = createdTime;
    }

    public CreditCardType getCreditCardType() {
        return creditCardType;
    }

    public void setCreditCardType(CreditCardType creditCardType) {
        this.creditCardType = creditCardType;
    }

    public String getCreditCardNumber() {
        return creditCardNumber;
    }

    public void setCreditCardNumber(String creditCardNumber) {
        this.creditCardNumber = creditCardNumber;
    }

    public String getCvv() {
        return cvv;
    }

    public void setCvv(String cvv) {
        this.cvv = cvv;
    }

    public String getCardHolderName() {
        return cardHolderName;
    }

    public void setCardHolderName(String cardHolderName) {
        this.cardHolderName = cardHolderName;
    }

    public YearMonth getCardExpiry() {
        return YearMonth.of(cardExpiryYear.getValue(), cardExpiryMonth.getValue());
    }

    public Year getCardExpiryYear() {
        return cardExpiryYear;
    }

    public void setCardExpiryYear(Year cardExpiryYear) {
        this.cardExpiryYear = cardExpiryYear;
    }

    public Month getCardExpiryMonth() {
        return cardExpiryMonth;
    }

    public void setCardExpiryMonth(Month cardExpiryMonth) {
        this.cardExpiryMonth = cardExpiryMonth;
    }

    public LocalDateTime getCreatedTime() {
        return createdTime;
    }

    public void setCreatedTime(LocalDateTime createdTime) {
        this.createdTime = createdTime;
    }

    /**
     * @return 10 years including the year this payment was createdTime in.
     */
    public List<Year> validExpiryYears() {
        Year currentYearNow = Year.of(createdTime.getYear());

        return IntStream.rangeClosed(0, 10)
                .mapToObj(currentYearNow::plusYears)
                .collect(Collectors.toList());
    }

    /**
     * @return All months starting from the month this payment was createdTime in.
     */
    public List<Month> validExpiryMonths() {
        Month currentMonthNow = createdTime.getMonth();
        return Arrays.stream(Month.values())
                .filter(month -> month.getValue() >= currentMonthNow.getValue())
                .collect(Collectors.toList());
    }

    public String last4CardDigits() {
        return creditCardNumber.substring(creditCardNumber.length() - 4);
    }

    public CompletedPayment toCompletedPayment() {
        return new CompletedPayment(creditCardType, last4CardDigits(), cvv, getCardExpiry());
    }

    @Override
    public String toString() {
        return "PendingPayment{" +
                "creditCardType=" + creditCardType +
                ", creditCardNumber='" + creditCardNumber + '\'' +
                ", cvv='" + cvv + '\'' +
                ", cardHolderName='" + cardHolderName + '\'' +
                ", cardExpiryYear=" + cardExpiryYear +
                ", cardExpiryMonth=" + cardExpiryMonth +
                ", createdTime=" + createdTime +
                '}';
    }
}
