package com.demo.domain;

import org.junit.Test;

import java.time.LocalDateTime;
import java.time.Month;
import java.time.Year;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

public class PendingPaymentTest {

    /**
     * Should return the next 10 years starting from now.
     */
    @Test
    public void validExpiryYears() {
        LocalDateTime now = LocalDateTime.of(2010, 1, 1, 9, 0);
        PendingPayment pendingPayment = new PendingPayment(now);

        List<Year> expected = new ArrayList<>();
        expected.add(Year.of(2010));
        expected.add(Year.of(2011));
        expected.add(Year.of(2012));
        expected.add(Year.of(2013));
        expected.add(Year.of(2014));
        expected.add(Year.of(2015));
        expected.add(Year.of(2016));
        expected.add(Year.of(2017));
        expected.add(Year.of(2018));
        expected.add(Year.of(2019));
        expected.add(Year.of(2020));

        assertThat(pendingPayment.validExpiryYears()).containsExactlyElementsOf(expected);
    }

    /**
     * Should return only the next months from now (January) until December to create the full year.
     */
    @Test
    public void validExpiryMonths_CreateFullYear() {
        LocalDateTime now = LocalDateTime.of(2010, 1, 1, 9, 0);
        PendingPayment pendingPayment = new PendingPayment(now);

        List<Month> expected = new ArrayList<>();
        expected.add(Month.JANUARY);
        expected.add(Month.FEBRUARY);
        expected.add(Month.MARCH);
        expected.add(Month.APRIL);
        expected.add(Month.MAY);
        expected.add(Month.JUNE);
        expected.add(Month.JULY);
        expected.add(Month.AUGUST);
        expected.add(Month.SEPTEMBER);
        expected.add(Month.OCTOBER);
        expected.add(Month.NOVEMBER);
        expected.add(Month.DECEMBER);

        assertThat(pendingPayment.validExpiryMonths()).containsExactlyElementsOf(expected);
    }

    /**
     * Should return only the next months from now (October) until December to create a partial year.
     */
    @Test
    public void validExpiryMonths_CreatePartialYear() {
        LocalDateTime now = LocalDateTime.of(2010, 10, 1, 9, 0);
        PendingPayment pendingPayment = new PendingPayment(now);

        List<Month> expected = new ArrayList<>();
        expected.add(Month.OCTOBER);
        expected.add(Month.NOVEMBER);
        expected.add(Month.DECEMBER);

        assertThat(pendingPayment.validExpiryMonths()).containsExactlyElementsOf(expected);
    }

    /**
     * If its the last month of the year, December should still be returned.
     */
    @Test
    public void validExpiryMonths_LastMonthInYear_ReturnsDecemberOnly() {
        LocalDateTime now = LocalDateTime.of(2010, 12, 1, 9, 0);
        PendingPayment pendingPayment = new PendingPayment(now);

        List<Month> expected = new ArrayList<>();
        expected.add(Month.DECEMBER);

        assertThat(pendingPayment.validExpiryMonths()).containsExactlyElementsOf(expected);
    }

    @Test
    public void last4CardDigits() {
        PendingPayment pendingPayment = new PendingPayment(LocalDateTime.now());
        pendingPayment.setCreditCardNumber("1234567892");

        assertThat(pendingPayment.last4CardDigits()).isEqualTo("7892");
    }
}