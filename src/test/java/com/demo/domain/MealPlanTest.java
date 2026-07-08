package com.demo.domain;

import org.junit.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.*;

public class MealPlanTest {


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

    @Test
    public void getTotalMealPlanCost_NoFood() {
        Guest guest = new Guest("john", "smith", false);
        Reservation reservation = new Reservation();

        reservation.getDates().setCheckInDate(LocalDate.of(2018, 1, 1));
        reservation.getDates().setCheckOutDate(LocalDate.of(2018, 1, 4));

        MealPlan mealPlan = new MealPlan(guest, reservation, List.of(), List.of());

        assertThat(mealPlan.getTotalMealPlanCost()).isEqualTo(BigDecimal.ZERO);
    }


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

    @Test
    public void getTotalMealPlanCost_ChildDiscountApplied() {
        Guest guest = new Guest("john", "smith", true);
        Reservation reservation = new Reservation();

        BigDecimal totalNights = BigDecimal.valueOf(3);
        reservation.getDates().setCheckInDate(LocalDate.of(2018, 1, 1));
        reservation.getDates().setCheckOutDate(LocalDate.of(2018, 1, 4));

        // Adult prices form the base price, if guest is a child then discount is applied.
        BigDecimal breakfastPerNight = new BigDecimal("2.00");
        BigDecimal lunchPerNight = new BigDecimal("4.12");
        BigDecimal dinnerPerNight = new BigDecimal("5.63");
        List<Extra> foodExtras = List.of(
                new Extra("Breakfast", breakfastPerNight, Extra.Type.Basic, Extra.Category.Food),
                new Extra("Lunch", lunchPerNight, Extra.Type.Basic, Extra.Category.Food),
                new Extra("Dinner", dinnerPerNight, Extra.Type.Basic, Extra.Category.Food)
        );

        // apply discount to breakfast for the total night duration
        BigDecimal breakFastDiscount = breakfastPerNight.multiply(totalNights)
                .multiply(BigDecimal.valueOf(MealPlan.CHILD_DISCOUNT_PERCENT));
        BigDecimal breakFastTotal = breakfastPerNight.multiply(totalNights).subtract(breakFastDiscount);

        // apply discount to lunch for the total night duration
        BigDecimal lunchPerNightDiscount = lunchPerNight.multiply(totalNights)
                .multiply(BigDecimal.valueOf(MealPlan.CHILD_DISCOUNT_PERCENT));
        BigDecimal lunchTotal = lunchPerNight.multiply(totalNights).subtract(lunchPerNightDiscount);

        // apply discount to dinner for the total night duration
        BigDecimal dinnerPerNightDiscount = dinnerPerNight.multiply(totalNights)
                .multiply(BigDecimal.valueOf(MealPlan.CHILD_DISCOUNT_PERCENT));
        BigDecimal dinnerTotal = dinnerPerNight.multiply(totalNights).subtract(dinnerPerNightDiscount);

        MealPlan mealPlan = new MealPlan(guest, reservation, foodExtras, List.of());

        BigDecimal foodExtrasTotal = breakFastTotal.add(lunchTotal).add(dinnerTotal);
        assertThat(mealPlan.getTotalMealPlanCost()).isEqualTo(foodExtrasTotal);
    }

    @Test
    public void calculateExtraCost_InvalidExtraCategory_ThrowsException() {
        Guest guest = new Guest("john", "smith", false);
        Reservation reservation = new Reservation();

        reservation.getDates().setCheckInDate(LocalDate.of(2018, 1, 1));
        reservation.getDates().setCheckOutDate(LocalDate.of(2018, 1, 4));

        MealPlan mealPlan = new MealPlan(guest, reservation, List.of(), List.of());

        Extra extra = new Extra("foxtel", new BigDecimal("2.00"), Extra.Type.Basic, Extra.Category.General);

        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> mealPlan.calculateExtraCost(extra));
    }

    @Test
    public void calculateExtraCost_Adult_NoDiscountApplied() {
        Guest guest = new Guest("john", "smith", false);
        Reservation reservation = new Reservation();

        BigDecimal totalNights = BigDecimal.valueOf(3);
        reservation.getDates().setCheckInDate(LocalDate.of(2018, 1, 1));
        reservation.getDates().setCheckOutDate(LocalDate.of(2018, 1, 4));

        // Adult prices form the base price, if guest is a child then discount is applied.
        BigDecimal breakfastPerNight = new BigDecimal("2.00");

        MealPlan mealPlan = new MealPlan(guest, reservation, List.of(), List.of());

        BigDecimal expectedExtraCost = breakfastPerNight.multiply(totalNights);
        Extra extra = new Extra("Breakfast", breakfastPerNight, Extra.Type.Basic, Extra.Category.Food);

        assertThat(mealPlan.calculateExtraCost(extra)).isEqualTo(expectedExtraCost);
    }

    @Test
    public void calculateExtraCost_Child_DiscountApplied() {
        Guest guest = new Guest("john", "smith", true);
        Reservation reservation = new Reservation();

        BigDecimal totalNights = BigDecimal.valueOf(3);
        reservation.getDates().setCheckInDate(LocalDate.of(2018, 1, 1));
        reservation.getDates().setCheckOutDate(LocalDate.of(2018, 1, 4));

        // Adult prices form the base price, if guest is a child then discount is applied.
        BigDecimal breakfastPerNight = new BigDecimal("2.00");

        MealPlan mealPlan = new MealPlan(guest, reservation, List.of(), List.of());

        BigDecimal discount = breakfastPerNight.multiply(totalNights)
                .multiply(BigDecimal.valueOf(MealPlan.CHILD_DISCOUNT_PERCENT));
        BigDecimal expectedTotal = breakfastPerNight.multiply(totalNights).subtract(discount);

        Extra extra = new Extra("Breakfast", breakfastPerNight, Extra.Type.Basic, Extra.Category.Food);

        assertThat(mealPlan.calculateExtraCost(extra)).isEqualTo(expectedTotal);
    }

 
    @Test
    public void hasInvalidDietaryRequirements_InvalidVeganAndVegetarian() {
        Guest guest = new Guest("john", "smith", true);
        Reservation reservation = new Reservation();

        reservation.getDates().setCheckInDate(LocalDate.of(2018, 1, 1));
        reservation.getDates().setCheckOutDate(LocalDate.of(2018, 1, 4));

        // cant contain vegan and vegetarian at the same time.
        List<DietaryRequirement> dietaryRequirements = List.of(
                DietaryRequirement.GlutenIntolerant,
                DietaryRequirement.Vegetarian,
                DietaryRequirement.Vegan,
                DietaryRequirement.LactoseIntolerant
        );

        MealPlan mealPlan = new MealPlan(guest, reservation, List.of(), dietaryRequirements);
        assertThat(mealPlan.hasInvalidDietaryRequirements()).isTrue();
    }


    @Test
    public void hasInvalidDietaryRequirements_Valid() {
        Guest guest = new Guest("john", "smith", true);
        Reservation reservation = new Reservation();

        reservation.getDates().setCheckInDate(LocalDate.of(2018, 1, 1));
        reservation.getDates().setCheckOutDate(LocalDate.of(2018, 1, 4));

        // cant contain vegan and vegetarian at the same time.
        List<DietaryRequirement> dietaryRequirements = List.of(
                DietaryRequirement.GlutenIntolerant,
                DietaryRequirement.Vegan,
                DietaryRequirement.LactoseIntolerant
        );

        MealPlan mealPlan = new MealPlan(guest, reservation, List.of(), dietaryRequirements);
        assertThat(mealPlan.hasInvalidDietaryRequirements()).isFalse();
    }
}