package com.demo.domain;

import org.junit.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

public class MealPlanTest {

    @Test
    public void getTotalMealPlanCost_NoFood() {
        Guest guest = new Guest("john", "smith", false);
        Reservation reservation = new Reservation();

        BigDecimal nights = BigDecimal.valueOf(3);
        reservation.getDates().setCheckInDate(LocalDate.of(2018, 1, 1));
        reservation.getDates().setCheckOutDate(LocalDate.of(2018, 1, 4));

        MealPlan mealPlan = new MealPlan(guest, reservation, List.of(), List.of());

        assertThat(mealPlan.getTotalMealPlanCost()).isEqualTo(BigDecimal.ZERO);
    }

    @Test
    public void getTotalMealPlanCost_NoAdultDiscount() {
        Guest guest = new Guest("john", "smith", false);
        Reservation reservation = new Reservation();

        BigDecimal nights = BigDecimal.valueOf(3);
        reservation.getDates().setCheckInDate(LocalDate.of(2018, 1, 1));
        reservation.getDates().setCheckOutDate(LocalDate.of(2018, 1, 4));

        List<Extra> foodExtras = List.of(
                new Extra("Breakfast", new BigDecimal("2.00"), Extra.Type.Basic, Extra.Category.Food),
                new Extra("Lunch", new BigDecimal("4.12"), Extra.Type.Basic, Extra.Category.Food),
                new Extra("Dinner", new BigDecimal("5.63"), Extra.Type.Basic, Extra.Category.Food)
        );

        MealPlan mealPlan = new MealPlan(guest, reservation, foodExtras, List.of());

        BigDecimal expectedTotal = BigDecimal.valueOf(2.00).multiply(nights)
                .add(BigDecimal.valueOf(4.12).multiply(nights))
                .add(BigDecimal.valueOf(5.63).multiply(nights));

        assertThat(mealPlan.getTotalMealPlanCost()).isEqualTo(expectedTotal);
    }

    @Test
    public void getTotalMealPlanCost_ChildDiscountApplied() {
        Guest guest = new Guest("john", "smith", true);
        Reservation reservation = new Reservation();

        BigDecimal nights = BigDecimal.valueOf(3);
        reservation.getDates().setCheckInDate(LocalDate.of(2018, 1, 1));
        reservation.getDates().setCheckOutDate(LocalDate.of(2018, 1, 4));

        List<Extra> foodExtras = List.of(
                new Extra("Breakfast", new BigDecimal("2.00"), Extra.Type.Basic, Extra.Category.Food),
                new Extra("Lunch", new BigDecimal("4.12"), Extra.Type.Basic, Extra.Category.Food),
                new Extra("Dinner", new BigDecimal("5.63"), Extra.Type.Basic, Extra.Category.Food)
        );

        MealPlan mealPlan = new MealPlan(guest, reservation, foodExtras, List.of());

        BigDecimal total = BigDecimal.valueOf(2.00).multiply(nights)
                .add(BigDecimal.valueOf(4.12).multiply(nights))
                .add(BigDecimal.valueOf(5.63).multiply(nights));
        BigDecimal discount = total.multiply(BigDecimal.valueOf(Reservation.CHILD_DISCOUNT_PERCENT));
        BigDecimal expectedTotal = total.subtract(discount);

        assertThat(mealPlan.getTotalMealPlanCost()).isEqualTo(expectedTotal);
    }
}