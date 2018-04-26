package com.demo.domain;

import org.junit.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.*;

public class MealPlanTest {

    /**
     * A {@code MealPlan} can be created with no food extras.
     */
    @Test
    public void createMealPlan_LegalMealPlanWithNoMeals() {
        Guest guest = new Guest("john", "smith", false);
        Reservation reservation = new Reservation();

        reservation.getDates().setCheckInDate(LocalDate.of(2018, 1, 1));
        reservation.getDates().setCheckOutDate(LocalDate.of(2018, 1, 4));

        List<Extra> foodExtras = List.of();
        MealPlan mealPlan = new MealPlan(guest, reservation, foodExtras, List.of());
        assertThat(mealPlan.getFoodExtras()).isEmpty();
    }

    /**
     * Cannot construct a {@code MealPlan} if the list of {@code Extra}s contains anything other than
     * {@code Extra.Category.Food}.
     */
    @Test
    public void createMealPlan_IllegalFoodExtras() {
        Guest guest = new Guest("john", "smith", false);
        Reservation reservation = new Reservation();

        reservation.getDates().setCheckInDate(LocalDate.of(2018, 1, 1));
        reservation.getDates().setCheckOutDate(LocalDate.of(2018, 1, 4));

        List<Extra> illegalFoodExtras = List.of(
                new Extra("a", BigDecimal.valueOf(1.50), Extra.Type.Basic, Extra.Category.Food),
                new Extra("b", BigDecimal.valueOf(3.50), Extra.Type.Basic, Extra.Category.General)
        );

        assertThatIllegalArgumentException()
                .isThrownBy(() -> new MealPlan(guest, reservation, illegalFoodExtras, List.of()));
    }

    /**
     * A {@code MealPlan} is constructed only when the list of {@code Extra}s contains all
     * elements of type {@code Extra.Category.Food}.
     */
    @Test
    public void createMealPlan_LegalFoodExtras() {
        Guest guest = new Guest("john", "smith", false);
        Reservation reservation = new Reservation();

        reservation.getDates().setCheckInDate(LocalDate.of(2018, 1, 1));
        reservation.getDates().setCheckOutDate(LocalDate.of(2018, 1, 4));

        List<Extra> legalFoodExtras = List.of(
                new Extra("a", BigDecimal.valueOf(1.50), Extra.Type.Basic, Extra.Category.Food),
                new Extra("b", BigDecimal.valueOf(3.50), Extra.Type.Basic, Extra.Category.Food)
        );

        MealPlan mealPlan = new MealPlan(guest, reservation, legalFoodExtras, List.of());
        mealPlan.setFoodExtras(legalFoodExtras);
        assertThat(mealPlan.getFoodExtras()).isEqualTo(legalFoodExtras);
    }

    /**
     * Cannot set the food extras list within {@code MealPlan} if the list of {@code Extra}s contains anything
     * other than {@code Extra.Category.Food}.
     */
    @Test
    public void createMealPlan_SetIllegalFoodExtras() {
        Guest guest = new Guest("john", "smith", false);
        Reservation reservation = new Reservation();

        reservation.getDates().setCheckInDate(LocalDate.of(2018, 1, 1));
        reservation.getDates().setCheckOutDate(LocalDate.of(2018, 1, 4));

        List<Extra> illegalFoodExtras = List.of(
                new Extra("a", BigDecimal.valueOf(1.50), Extra.Type.Basic, Extra.Category.Food),
                new Extra("b", BigDecimal.valueOf(3.50), Extra.Type.Basic, Extra.Category.General)
        );

        assertThatIllegalArgumentException()
                .isThrownBy(() -> new MealPlan(guest, reservation, List.of(), List.of()).setFoodExtras(illegalFoodExtras));
    }

    /**
     * A {@code MealPlan} sets the food extras only when the list of {@code Extra}s contains all
     * elements of type {@code Extra.Category.Food}.
     */
    @Test
    public void createMealPlan_SetLegalFoodExtras() {
        Guest guest = new Guest("john", "smith", false);
        Reservation reservation = new Reservation();

        reservation.getDates().setCheckInDate(LocalDate.of(2018, 1, 1));
        reservation.getDates().setCheckOutDate(LocalDate.of(2018, 1, 4));

        List<Extra> legalFoodExtras = List.of(
                new Extra("a", BigDecimal.valueOf(1.50), Extra.Type.Basic, Extra.Category.Food),
                new Extra("b", BigDecimal.valueOf(3.50), Extra.Type.Basic, Extra.Category.Food)
        );

        MealPlan mealPlan = new MealPlan(guest, reservation, List.of(), List.of());
        mealPlan.setFoodExtras(legalFoodExtras);
        assertThat(mealPlan.getFoodExtras()).isEqualTo(legalFoodExtras);
    }

    /**
     * A meal plan with no food is $0
     */
    @Test
    public void getTotalMealPlanCost_NoFood() {
        Guest guest = new Guest("john", "smith", false);
        Reservation reservation = new Reservation();

        reservation.getDates().setCheckInDate(LocalDate.of(2018, 1, 1));
        reservation.getDates().setCheckOutDate(LocalDate.of(2018, 1, 4));

        MealPlan mealPlan = new MealPlan(guest, reservation, List.of(), List.of());

        assertThat(mealPlan.getTotalMealPlanCost()).isEqualTo(BigDecimal.ZERO);
    }

    /**
     * Adults should not receive any discount
     */
    @Test
    public void getTotalMealPlanCost_NoAdultDiscount() {
        Guest guest = new Guest("john", "smith", false);
        Reservation reservation = new Reservation();

        BigDecimal totalNights = BigDecimal.valueOf(3);
        reservation.getDates().setCheckInDate(LocalDate.of(2018, 1, 1));
        reservation.getDates().setCheckOutDate(LocalDate.of(2018, 1, 4));

        BigDecimal breakfastPerNight = new BigDecimal("2.00");
        BigDecimal lunchPerNight = new BigDecimal("4.12");
        BigDecimal dinnerPerNight = new BigDecimal("5.63");
        List<Extra> foodExtras = List.of(
                new Extra("Breakfast", breakfastPerNight, Extra.Type.Basic, Extra.Category.Food),
                new Extra("Lunch", lunchPerNight, Extra.Type.Basic, Extra.Category.Food),
                new Extra("Dinner", dinnerPerNight, Extra.Type.Basic, Extra.Category.Food)
        );

        MealPlan mealPlan = new MealPlan(guest, reservation, foodExtras, List.of());

        BigDecimal foodExtrasTotal = breakfastPerNight.multiply(totalNights)
                .add(lunchPerNight.multiply(totalNights))
                .add(dinnerPerNight.multiply(totalNights));

        assertThat(mealPlan.getTotalMealPlanCost()).isEqualTo(foodExtrasTotal);
    }

    /**
     * The child discount gets applied after the food extras total has been calculated.
     */
    @Test
    public void getTotalMealPlanCost_ChildDiscountApplied() {
        Guest guest = new Guest("john", "smith", true);
        Reservation reservation = new Reservation();

        BigDecimal totalNights = BigDecimal.valueOf(3);
        reservation.getDates().setCheckInDate(LocalDate.of(2018, 1, 1));
        reservation.getDates().setCheckOutDate(LocalDate.of(2018, 1, 4));

        BigDecimal breakfastPerNight = new BigDecimal("2.00");
        BigDecimal lunchPerNight = new BigDecimal("4.12");
        BigDecimal dinnerPerNight = new BigDecimal("5.63");

        List<Extra> foodExtras = List.of(
                new Extra("Breakfast", breakfastPerNight, Extra.Type.Basic, Extra.Category.Food),
                new Extra("Lunch", lunchPerNight, Extra.Type.Basic, Extra.Category.Food),
                new Extra("Dinner", dinnerPerNight, Extra.Type.Basic, Extra.Category.Food)
        );

        MealPlan mealPlan = new MealPlan(guest, reservation, foodExtras, List.of());

        BigDecimal foodExtrasTotal = breakfastPerNight.multiply(totalNights)
                .add(lunchPerNight.multiply(totalNights))
                .add(dinnerPerNight.multiply(totalNights));

        BigDecimal discount = foodExtrasTotal.multiply(BigDecimal.valueOf(Reservation.CHILD_DISCOUNT_PERCENT));
        BigDecimal expectedTotal = foodExtrasTotal.subtract(discount);

        assertThat(mealPlan.getTotalMealPlanCost()).isEqualTo(expectedTotal);
    }
}