package com.demo.domain;

import org.junit.Test;

import java.math.BigDecimal;

import static org.junit.Assert.*;

import static org.assertj.core.api.Assertions.*;

public class ExtraTest {

    /**
     * The total cost of an {@code Extra} is {@code perNightPrice * getTotalNights}
     */
    @Test
    public void getTotalPrice() {
        BigDecimal perNightPrice = BigDecimal.valueOf(5.53);
        long totalNights = 5;

        Extra extra = new Extra("a", perNightPrice, Extra.Type.Basic, Extra.Category.General);

        assertThat(extra.getTotalPrice(totalNights))
                .isEqualTo(perNightPrice.multiply(BigDecimal.valueOf(totalNights)));
    }
}