package com.demo.domain;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

/**
 * After a payment provider has processed the requested payment, the results are transferred into a domain type which
 * get kept in the {@code Reservation}.
 */
@Entity
public class Payment {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(nullable = false)
    private UUID transactionId;

    @Column(nullable = false)
    private LocalDateTime processedTime;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private PaymentStatus paymentStatus;

    private String comment;

    @Column(nullable = false)
    @NotNull
    @Enumerated(EnumType.STRING)
    private CreditCardType creditCardType;

    @Column(nullable = false)
    private String cardHolderName;

    @Column(nullable = false)
    private String last4CreditCardDigits;

    public Payment() {
    }

    public Payment(UUID transactionId, LocalDateTime processedTime, PaymentStatus paymentStatus, String comment,
                   @NotNull CreditCardType creditCardType, String cardHolderName, String creditCardNumber) {
        this.transactionId = transactionId;
        this.processedTime = processedTime;
        this.paymentStatus = paymentStatus;
        this.comment = comment;
        this.creditCardType = creditCardType;
        this.cardHolderName = cardHolderName;
        this.last4CreditCardDigits = creditCardNumber.substring(creditCardNumber.length() - 4);
    }

    public Long getId() {
        return id;
    }

    /**
     * @return Transaction id issued by the payment provider
     */
    public UUID getTransactionId() {
        return transactionId;
    }

    /**
     * @return Transaction time that the payment provider processed the payment
     */
    public LocalDateTime getProcessedTime() {
        return processedTime;
    }

    public PaymentStatus getPaymentStatus() {
        return paymentStatus;
    }

    /**
     * @return Comments from the payment provider providing more info as to why payment was declined for example.
     */
    public String getComment() {
        return comment;
    }

    public CreditCardType getCreditCardType() {
        return creditCardType;
    }

    public String getCardHolderName() {
        return cardHolderName;
    }

    public String getLast4CreditCardDigits() {
        return last4CreditCardDigits;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Payment payment = (Payment) o;
        return Objects.equals(transactionId, payment.transactionId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(transactionId);
    }
}
