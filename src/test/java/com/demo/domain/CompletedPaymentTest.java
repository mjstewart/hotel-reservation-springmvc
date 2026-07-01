package com.demo.domain;

import org.junit.Test;

import java.time.*;

import static org.assertj.core.api.Assertions.*;

/**
 * Testes unitários para {@link CompletedPayment} e a conversão de {@link PendingPayment}
 * para {@code CompletedPayment} via {@link PendingPayment#toCompletedPayment()}.
 */
public class CompletedPaymentTest {

    // ==================== toCompletedPayment() ====================

    /**
     * toCompletedPayment deve mapear corretamente os campos do PendingPayment
     * para um CompletedPayment.
     */
    @Test
    public void toCompletedPayment_MapsFieldsCorrectly() {
        PendingPayment pending = new PendingPayment(LocalDateTime.of(2026, 7, 1, 10, 0));
        pending.setCreditCardType(PendingPayment.CreditCardType.Visa);
        pending.setCreditCardNumber("1234567890");
        pending.setCvv("123");
        pending.setCardHolderName("John Smith");
        pending.setCardExpiryYear(Year.of(2028));
        pending.setCardExpiryMonth(Month.JUNE);

        CompletedPayment completed = pending.toCompletedPayment();

        assertThat(completed.getCreditCardType()).isEqualTo(PendingPayment.CreditCardType.Visa);
        assertThat(completed.getLast4CreditCardDigits()).isEqualTo("7890");
        assertThat(completed.getCvv()).isEqualTo("123");
        assertThat(completed.getCardExpiry()).isEqualTo(YearMonth.of(2028, 6));
        assertThat(completed.getTransactionId()).isNotNull();
    }

    /**
     * toCompletedPayment com MasterCard deve preservar o tipo do cartão.
     */
    @Test
    public void toCompletedPayment_MasterCard_PreservesType() {
        PendingPayment pending = new PendingPayment(LocalDateTime.of(2026, 7, 1, 10, 0));
        pending.setCreditCardType(PendingPayment.CreditCardType.MasterCard);
        pending.setCreditCardNumber("9876543210");
        pending.setCvv("456");
        pending.setCardHolderName("Jane Doe");
        pending.setCardExpiryYear(Year.of(2030));
        pending.setCardExpiryMonth(Month.DECEMBER);

        CompletedPayment completed = pending.toCompletedPayment();

        assertThat(completed.getCreditCardType()).isEqualTo(PendingPayment.CreditCardType.MasterCard);
        assertThat(completed.getLast4CreditCardDigits()).isEqualTo("3210");
        assertThat(completed.getCardExpiry()).isEqualTo(YearMonth.of(2030, 12));
    }

    // ==================== CompletedPayment equals / hashCode ====================

    /**
     * Cada CompletedPayment deve ter um transactionId único, portanto duas instâncias
     * criadas independentemente não devem ser iguais.
     */
    @Test
    public void equals_DifferentTransactionIds_NotEqual() {
        CompletedPayment payment1 = new CompletedPayment(
                PendingPayment.CreditCardType.Visa, "1234", "123", YearMonth.of(2028, 6));
        CompletedPayment payment2 = new CompletedPayment(
                PendingPayment.CreditCardType.Visa, "1234", "123", YearMonth.of(2028, 6));

        // Each gets a unique UUID transactionId
        assertThat(payment1).isNotEqualTo(payment2);
    }

    /**
     * Um CompletedPayment deve ser igual a si mesmo.
     */
    @Test
    public void equals_SameInstance_IsEqual() {
        CompletedPayment payment = new CompletedPayment(
                PendingPayment.CreditCardType.Visa, "5678", "999", YearMonth.of(2027, 3));

        assertThat(payment).isEqualTo(payment);
    }

    // ==================== PendingPayment.getCardExpiry() ====================

    /**
     * getCardExpiry deve combinar corretamente o ano e mês do cartão.
     */
    @Test
    public void getCardExpiry_CombinesYearAndMonth() {
        PendingPayment pending = new PendingPayment(LocalDateTime.now());
        pending.setCardExpiryYear(Year.of(2029));
        pending.setCardExpiryMonth(Month.MARCH);

        assertThat(pending.getCardExpiry()).isEqualTo(YearMonth.of(2029, 3));
    }
}
