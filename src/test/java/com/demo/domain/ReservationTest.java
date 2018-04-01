package com.demo.domain;

import org.junit.Test;
import org.mockito.Mockito;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

public class ReservationTest {

    private Room createRoom() {
        Room room = new Room("ABC123", RoomType.Economy, 2, BigDecimal.valueOf(25.50));
        room.setHotel(new Hotel("Hotel Royal", "Melbourne", 4, "royal@hotel.com"));
        return room;
    }

    @Test
    public void isRoomFull() {
        Room room = createRoom();
        room.setBeds(2);

        Reservation reservation = new Reservation();
        reservation.setRoom(room);

        assertThat(reservation.isRoomFull()).isFalse();
        reservation.addGuest(new Guest("john", "smith", false));
        assertThat(reservation.isRoomFull()).isFalse();
        reservation.addGuest(new Guest("marie", "smith", false));
        assertThat(reservation.isRoomFull()).isTrue();
    }

    @Test
    public void addGuest_OnlyWhenThereAreFreeBeds() {
        Room room = createRoom();
        room.setBeds(2);

        Reservation reservation = new Reservation();
        reservation.setRoom(room);

        Guest john = new Guest("john", "smith", false);
        reservation.addGuest(john);
        Guest sara = new Guest("sara", "smith", true);
        reservation.addGuest(sara);

        reservation.addGuest(new Guest("marie", "smith", false));
        reservation.addGuest(new Guest("ryan", "smith", false));

        assertThat(reservation.getGuests().size()).isEqualTo(2);
        assertThat(reservation.getGuests()).containsExactly(john, sara);
    }

    @Test
    public void setGeneralExtras() {
        Reservation reservation = new Reservation();

        Set<Extra> generalExtras = Set.of(new Extra("a", BigDecimal.valueOf(1.50), Extra.Type.Basic, Extra.Category.General));
        reservation.setGeneralExtras(generalExtras);

        assertThat(reservation.getGeneralExtras()).isEqualTo(generalExtras);
    }

    @Test(expected = IllegalArgumentException.class)
    public void setGeneralExtras_ThrowsException_WhenInvalidExtras() {
        Reservation reservation = new Reservation();
        reservation.setGeneralExtras(
                Set.of(new Extra("a", BigDecimal.valueOf(1.50), Extra.Type.Basic, Extra.Category.Food))
        );
    }

    @Test
    public void setMealPlans() {
        Guest guest = new Guest("john", "smith", false);

        List<Extra> illegalFoodExtras = List.of(
                new Extra("a", BigDecimal.valueOf(1.23), Extra.Type.Basic, Extra.Category.Food),
                new Extra("b", BigDecimal.valueOf(2.40), Extra.Type.Basic, Extra.Category.Food)
        );

        Set<MealPlan> mealPlans = Set.of(new MealPlan(guest, new Reservation(), illegalFoodExtras, List.of()));

        Reservation reservation = new Reservation();
        reservation.setMealPlans(mealPlans);

        assertThat(reservation.getMealPlans()).isEqualTo(mealPlans);
    }

    @Test(expected = IllegalArgumentException.class)
    public void setMealPlans_ThrowsException_WhenInvalidExtras() {
        Guest guest1 = new Guest("john", "smith", false);
        Guest guest2 = new Guest("sally", "smith", false);

        // General category should not be allowed in a meal plan.
        List<Extra> illegalFoodExtras = List.of(
                new Extra("a", BigDecimal.valueOf(1.23), Extra.Type.Basic, Extra.Category.General),
                new Extra("b", BigDecimal.valueOf(2.40), Extra.Type.Basic, Extra.Category.Food));

        List<Extra> validFoodExtras = List.of(
                new Extra("c", BigDecimal.valueOf(1.23), Extra.Type.Basic, Extra.Category.Food),
                new Extra("d", BigDecimal.valueOf(2.40), Extra.Type.Basic, Extra.Category.Food));

        Reservation reservation = new Reservation();
        reservation.setMealPlans(
                Set.of(
                        new MealPlan(guest1, new Reservation(), illegalFoodExtras, List.of()),
                        new MealPlan(guest2, new Reservation(), validFoodExtras, List.of())
                )
        );
    }

    @Test
    public void getChargeableLateCheckoutFee_WhenLateCheckout_ChargeFee() {
        BigDecimal lateCheckoutFee = BigDecimal.valueOf(20.50);

        Room room = createRoom();
        room.getHotel().setLateCheckoutFee(lateCheckoutFee);
        Reservation reservation = new Reservation();
        reservation.setRoom(room);
        reservation.getDates().setLateCheckout(true);

        assertThat(reservation.getChargeableLateCheckoutFee()).isEqualTo(lateCheckoutFee);
    }

    @Test
    public void getChargeableLateCheckoutFee_WhenNoLateCheckout_NoCharge() {
        BigDecimal lateCheckoutFee = BigDecimal.valueOf(20.50);

        Room room = createRoom();
        room.getHotel().setLateCheckoutFee(lateCheckoutFee);
        Reservation reservation = new Reservation();
        reservation.setRoom(room);
        reservation.getDates().setLateCheckout(false);

        assertThat(reservation.getChargeableLateCheckoutFee()).isEqualTo(BigDecimal.ZERO);
    }

    @Test
    public void getLateCheckoutFee_WhenLuxuryRoomType_NoLateCharge() {
        BigDecimal lateCheckoutFee = BigDecimal.valueOf(20.50);

        Room room = createRoom();
        room.setRoomType(RoomType.Luxury);
        room.getHotel().setLateCheckoutFee(lateCheckoutFee);

        Reservation reservation = new Reservation();
        reservation.setRoom(room);
        reservation.getDates().setLateCheckout(true);

        assertThat(reservation.getLateCheckoutFee()).isEqualTo(BigDecimal.ZERO);
    }

    @Test
    public void getLateCheckoutFee_WhenBusinessRoomType_ApplyLateCharge() {
        BigDecimal lateCheckoutFee = BigDecimal.valueOf(20.50);

        Room room = createRoom();
        room.setRoomType(RoomType.Business);
        room.getHotel().setLateCheckoutFee(lateCheckoutFee);

        Reservation reservation = new Reservation();
        reservation.setRoom(room);
        reservation.getDates().setLateCheckout(true);

        assertThat(reservation.getLateCheckoutFee()).isEqualTo(BigDecimal.ZERO);
    }

    @Test
    public void getLateCheckoutFee_WhenEconomyRoomType_ApplyLateCharge() {
        BigDecimal lateCheckoutFee = BigDecimal.valueOf(20.50);

        Room room = createRoom();
        room.setRoomType(RoomType.Economy);
        room.getHotel().setLateCheckoutFee(lateCheckoutFee);

        Reservation reservation = new Reservation();
        reservation.setRoom(room);
        reservation.getDates().setLateCheckout(true);

        assertThat(reservation.getLateCheckoutFee()).isEqualTo(lateCheckoutFee);
    }

    @Test
    public void getLateCheckoutFee_WhenBalconyRoomType_ApplyLateCharge() {
        BigDecimal lateCheckoutFee = BigDecimal.valueOf(20.50);

        Room room = createRoom();
        room.setRoomType(RoomType.Balcony);
        room.getHotel().setLateCheckoutFee(lateCheckoutFee);

        Reservation reservation = new Reservation();
        reservation.setRoom(room);
        reservation.getDates().setLateCheckout(true);

        assertThat(reservation.getLateCheckoutFee()).isEqualTo(lateCheckoutFee);
    }

    /**
     * Technically won't occur since the business rule of at least 1 night is validated.
     */
    @Test
    public void getTotalRoomCost_ZeroNights_NoCost() {
        Room room = createRoom();
        room.setRoomType(RoomType.Economy);
        room.getHotel().setLateCheckoutFee(BigDecimal.valueOf(20.50));

        BigDecimal costPerNight = BigDecimal.valueOf(23.80);
        room.setCostPerNight(costPerNight);

        Reservation reservation = new Reservation();
        reservation.setRoom(room);

        reservation.getDates().setCheckInDate(LocalDate.of(2018, 1, 1));
        reservation.getDates().setCheckOutDate(LocalDate.of(2018, 1, 1));

        assertThat(reservation.getTotalRoomCost()).isEqualTo(BigDecimal.ZERO);
    }

    @Test
    public void getTotalRoomCost_CalculatesCorrectCost() {
        Room room = createRoom();
        room.setRoomType(RoomType.Economy);
        room.getHotel().setLateCheckoutFee(BigDecimal.valueOf(20.50));

        BigDecimal costPerNight = BigDecimal.valueOf(23.80);
        room.setCostPerNight(costPerNight);

        Reservation reservation = new Reservation();
        reservation.setRoom(room);

        reservation.getDates().setCheckInDate(LocalDate.of(2018, 1, 1));
        reservation.getDates().setCheckOutDate(LocalDate.of(2018, 1, 4));

        // expected cost for 3 nights
        BigDecimal expectedCost = costPerNight.multiply(BigDecimal.valueOf(3));

        assertThat(reservation.getTotalRoomCost()).isEqualTo(expectedCost);
    }

    @Test
    public void getTotalRoomCostWithLateCheckoutFee_NoCheckoutFee_RoomCostOnly() {
        Room room = createRoom();
        room.setRoomType(RoomType.Economy);
        room.getHotel().setLateCheckoutFee(BigDecimal.valueOf(20.50));

        BigDecimal costPerNight = BigDecimal.valueOf(23.80);
        room.setCostPerNight(costPerNight);

        Reservation reservation = new Reservation();
        reservation.setRoom(room);
        reservation.getDates().setLateCheckout(false);

        reservation.getDates().setCheckInDate(LocalDate.of(2018, 1, 1));
        reservation.getDates().setCheckOutDate(LocalDate.of(2018, 1, 4));

        // expected cost for 3 nights
        BigDecimal expectedCost = costPerNight.multiply(BigDecimal.valueOf(3));

        assertThat(reservation.getTotalRoomCostWithLateCheckoutFee()).isEqualTo(expectedCost);
    }

    @Test
    public void getTotalRoomCostWithLateCheckoutFee_WithCheckoutFee_CorrectCost() {
        Room room = createRoom();
        room.setRoomType(RoomType.Economy);
        BigDecimal lateCheckoutFee = BigDecimal.valueOf(20.50);
        room.getHotel().setLateCheckoutFee(lateCheckoutFee);

        BigDecimal costPerNight = BigDecimal.valueOf(23.80);
        room.setCostPerNight(costPerNight);

        Reservation reservation = new Reservation();
        reservation.setRoom(room);
        reservation.getDates().setLateCheckout(true);

        reservation.getDates().setCheckInDate(LocalDate.of(2018, 1, 1));
        reservation.getDates().setCheckOutDate(LocalDate.of(2018, 1, 4));

        // expected cost for 3 nights + late fee
        BigDecimal expectedCost = costPerNight.multiply(BigDecimal.valueOf(3)).add(lateCheckoutFee);

        assertThat(reservation.getTotalRoomCostWithLateCheckoutFee()).isEqualTo(expectedCost);
    }


    @Test
    public void getTotalGeneralExtrasCost_NoExtras() {
        Set<Extra> extras = Set.of();
        Reservation reservation = new Reservation();
        reservation.setGeneralExtras(extras);

        reservation.getDates().setCheckInDate(LocalDate.of(2018, 1, 1));
        reservation.getDates().setCheckOutDate(LocalDate.of(2018, 1, 4));

        assertThat(reservation.getTotalGeneralExtrasCost()).isEqualTo(BigDecimal.ZERO);
    }


    @Test
    public void getTotalGeneralExtrasCost() {
        // Sum the result of each extras daily price multiplied by total nights.
        Set<Extra> extras = Set.of(
                new Extra("a", BigDecimal.valueOf(1.20), Extra.Type.Basic, Extra.Category.General),
                new Extra("b", BigDecimal.valueOf(3.80), Extra.Type.Basic, Extra.Category.General)
        );

        Reservation reservation = new Reservation();
        reservation.setGeneralExtras(extras);

        BigDecimal nights = BigDecimal.valueOf(3);
        reservation.getDates().setCheckInDate(LocalDate.of(2018, 1, 1));
        reservation.getDates().setCheckOutDate(LocalDate.of(2018, 1, 4));

        BigDecimal expectedSum = BigDecimal.valueOf(1.20).multiply(nights)
                .add(BigDecimal.valueOf(3.80).multiply(nights));

        assertThat(reservation.getTotalGeneralExtrasCost()).isEqualTo(expectedSum);
    }

    @Test
    public void getTotalMealPlansCost_NoMealPlans() {
        Reservation reservation = new Reservation();
        assertThat(reservation.getTotalMealPlansCost()).isEqualTo(BigDecimal.ZERO);
    }

    @Test
    public void getTotalMealPlansCost() {
        Reservation reservation = new Reservation();

        BigDecimal nights = BigDecimal.valueOf(3);
        reservation.getDates().setCheckInDate(LocalDate.of(2018, 1, 1));
        reservation.getDates().setCheckOutDate(LocalDate.of(2018, 1, 4));

        List<Extra> foodExtrasPlan1 = List.of(
                new Extra("Breakfast", new BigDecimal("2.00"), Extra.Type.Basic, Extra.Category.Food),
                new Extra("Lunch", new BigDecimal("4.12"), Extra.Type.Basic, Extra.Category.Food),
                new Extra("Dinner", new BigDecimal("5.63"), Extra.Type.Basic, Extra.Category.Food)
        );
        MealPlan mealPlan1 = new MealPlan(
                new Guest("john", "smith", false),
                reservation, foodExtrasPlan1, List.of());

        BigDecimal expectedMealPlan1Cost = BigDecimal.valueOf(2.00).multiply(nights)
                .add(BigDecimal.valueOf(4.12).multiply(nights))
                .add(BigDecimal.valueOf(5.63).multiply(nights));

        List<Extra> foodExtrasPlan2 = List.of(
                new Extra("Dinner", new BigDecimal("5.24"), Extra.Type.Basic, Extra.Category.Food)
        );
        MealPlan mealPlan2 = new MealPlan(
                new Guest("sally", "smith", false),
                reservation, foodExtrasPlan2, List.of());

        BigDecimal expectedMealPlan2Cost = BigDecimal.valueOf(5.24).multiply(nights);

        BigDecimal expectedCost = expectedMealPlan1Cost.add(expectedMealPlan2Cost);

        reservation.setMealPlans(Set.of(mealPlan1, mealPlan2));

        assertThat(reservation.getTotalMealPlansCost()).isEqualTo(expectedCost);
    }

    @Test
    public void getTotalCostExcludingTax() {
        Reservation reservation = mock(Reservation.class);

        when(reservation.getTotalRoomCostWithLateCheckoutFee()).thenReturn(BigDecimal.valueOf(5));
        when(reservation.getTotalGeneralExtrasCost()).thenReturn(BigDecimal.valueOf(6));
        when(reservation.getTotalMealPlansCost()).thenReturn(BigDecimal.valueOf(7));
        when(reservation.getTotalCostExcludingTax()).thenCallRealMethod();

        assertThat(reservation.getTotalCostExcludingTax()).isEqualTo(BigDecimal.valueOf(18));

        verify(reservation, times(1)).getTotalRoomCostWithLateCheckoutFee();
        verify(reservation, times(1)).getTotalGeneralExtrasCost();
        verify(reservation, times(1)).getTotalMealPlansCost();
    }

    @Test
    public void getTaxableAmount() {
        Reservation reservation = mock(Reservation.class);
        when(reservation.getTaxableAmount()).thenCallRealMethod();

        BigDecimal total = BigDecimal.valueOf(100);
        when(reservation.getTotalCostExcludingTax()).thenReturn(total);
        BigDecimal taxableAmount = total.multiply(BigDecimal.valueOf(Reservation.TAX_AMOUNT));
        assertThat(reservation.getTaxableAmount()).isEqualTo(taxableAmount);

        verify(reservation, times(1)).getTotalCostExcludingTax();
    }

    @Test
    public void getTotalCostIncludingTax() {
        Reservation reservation = mock(Reservation.class);
        when(reservation.getTotalCostIncludingTax()).thenCallRealMethod();

        BigDecimal totalExcludingTax = BigDecimal.valueOf(121.60);
        when(reservation.getTotalCostExcludingTax()).thenReturn(totalExcludingTax);

        BigDecimal taxableAmount = BigDecimal.valueOf(20.60);
        when(reservation.getTaxableAmount()).thenReturn(taxableAmount);

        assertThat(reservation.getTotalCostIncludingTax())
                .isEqualTo(totalExcludingTax.add(taxableAmount));

        verify(reservation, times(1)).getTotalCostExcludingTax();
        verify(reservation, times(1)).getTaxableAmount();
    }
}