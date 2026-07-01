package com.demo.integration;

import com.demo.domain.*;
import com.demo.domain.location.Address;
import com.demo.domain.location.Postcode;
import com.demo.domain.location.State;
import org.junit.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.*;

/**
 * Testes de integração para o cálculo de custo completo de uma reserva.
 *
 * Estes testes integram múltiplas classes de domínio (Reservation, Room, Hotel, Extra, MealPlan, Guest)
 * para validar que o cálculo end-to-end do custo total funciona corretamente quando todas as
 * regras de negócio são aplicadas conjuntamente.
 *
 * Diferente dos testes unitários (que usam mocks), estes testes exercitam os objetos reais
 * e suas interações.
 */
public class ReservationCostIntegrationTest {

    // ==================== Helpers ====================

    private Hotel createHotel(BigDecimal lateCheckoutFee) {
        Address address = new Address("Hotel Royal", "166 Albert Road", null,
                State.VIC, "Melbourne", new Postcode("3000"));
        return new Hotel("Hotel Royal", address, 4, "royal@hotel.com",
                java.time.LocalTime.of(7, 0), java.time.LocalTime.of(22, 0),
                java.time.LocalTime.of(11, 0), java.time.LocalTime.of(22, 0),
                lateCheckoutFee);
    }

    private Room createRoom(RoomType type, BigDecimal costPerNight, int beds, BigDecimal lateCheckoutFee) {
        Hotel hotel = createHotel(lateCheckoutFee);
        Room room = new Room("R1", type, beds, costPerNight);
        room.setHotel(hotel);
        return room;
    }

    private Reservation createBaseReservation(Room room, LocalDate checkIn, LocalDate checkOut,
                                               boolean lateCheckout) {
        Reservation reservation = new Reservation();
        reservation.setRoom(room);
        ReservationDates dates = new ReservationDates(
                checkIn, checkOut,
                java.time.LocalTime.of(10, 0), lateCheckout, true
        );
        reservation.setDates(dates);
        return reservation;
    }

    // ==================== Cenário completo: Economy com late checkout + extras + meals ====================

    /**
     * Teste de integração completo para um quarto Economy com:
     * - 3 noites de estadia ($23.50/noite)
     * - Late checkout ($20.50)
     * - 2 extras gerais (Foxtel $1.20/noite + Internet $2.00/noite)
     * - 1 adulto com café da manhã ($2.00/noite)
     * - 1 criança com café da manhã ($2.00/noite com 60% desconto)
     * - Imposto de 10%
     *
     * Cálculos esperados:
     * - Room cost: 3 * 23.50 = 70.50
     * - Late checkout: 20.50 (Economy cobra)
     * - Room + late: 91.00
     * - General extras: (1.20 * 3) + (2.00 * 3) = 3.60 + 6.00 = 9.60
     * - Adult meal: 2.00 * 3 = 6.00
     * - Child meal: (2.00 * 3) - (6.00 * 0.60) = 6.00 - 3.60 = 2.40
     * - Total meals: 6.00 + 2.40 = 8.40
     * - Total excl. tax: 91.00 + 9.60 + 8.40 = 109.00
     * - Tax (10%): 10.90
     * - Total incl. tax: 119.90
     */
    @Test
    public void fullReservationCost_EconomyRoom_WithLateCheckout_WithExtras_WithMeals() {
        BigDecimal costPerNight = new BigDecimal("23.50");
        BigDecimal lateCheckoutFee = new BigDecimal("20.50");

        Room room = createRoom(RoomType.Economy, costPerNight, 2, lateCheckoutFee);

        LocalDate checkIn = LocalDate.of(2026, 7, 1);
        LocalDate checkOut = LocalDate.of(2026, 7, 4); // 3 noites

        Reservation reservation = createBaseReservation(room, checkIn, checkOut, true);

        // Adicionar hóspedes
        Guest adult = new Guest("john", "smith", false);
        Guest child = new Guest("billy", "smith", true);
        reservation.addGuest(adult);
        reservation.addGuest(child);

        // General extras (Basic para Economy)
        Set<Extra> generalExtras = Set.of(
                new Extra("Foxtel", new BigDecimal("1.20"), Extra.Type.Basic, Extra.Category.General),
                new Extra("Internet", new BigDecimal("2.00"), Extra.Type.Basic, Extra.Category.General)
        );
        reservation.setGeneralExtras(generalExtras);

        // Meal plans
        Extra breakfast = new Extra("Breakfast", new BigDecimal("2.00"), Extra.Type.Basic, Extra.Category.Food);

        MealPlan adultMealPlan = new MealPlan(adult, reservation, List.of(breakfast), List.of());
        MealPlan childMealPlan = new MealPlan(child, reservation, List.of(breakfast), List.of());

        reservation.setMealPlans(List.of(adultMealPlan, childMealPlan));

        // Verificações
        // Room cost: 3 * 23.50 = 70.50
        assertThat(reservation.getTotalRoomCost())
                .isEqualByComparingTo(new BigDecimal("70.50"));

        // Late checkout fee: Economy cobra $20.50
        assertThat(reservation.getChargeableLateCheckoutFee())
                .isEqualByComparingTo(new BigDecimal("20.50"));

        // Room + late: 70.50 + 20.50 = 91.00
        assertThat(reservation.getTotalRoomCostWithLateCheckoutFee())
                .isEqualByComparingTo(new BigDecimal("91.00"));

        // General extras: (1.20 * 3) + (2.00 * 3) = 9.60
        assertThat(reservation.getTotalGeneralExtrasCost())
                .isEqualByComparingTo(new BigDecimal("9.60"));

        // Adult meal: 2.00 * 3 = 6.00
        assertThat(adultMealPlan.getTotalMealPlanCost())
                .isEqualByComparingTo(new BigDecimal("6.00"));

        // Child meal: (2.00 * 3) * (1 - 0.60) = 6.00 * 0.40 = 2.40
        assertThat(childMealPlan.getTotalMealPlanCost())
                .isEqualByComparingTo(new BigDecimal("2.40"));

        // Total meals: 6.00 + 2.40 = 8.40
        assertThat(reservation.getTotalMealPlansCost())
                .isEqualByComparingTo(new BigDecimal("8.40"));

        // Total excl. tax: 91.00 + 9.60 + 8.40 = 109.00
        assertThat(reservation.getTotalCostExcludingTax())
                .isEqualByComparingTo(new BigDecimal("109.00"));

        // Tax (10%): 10.900
        assertThat(reservation.getTaxableAmount())
                .isEqualByComparingTo(new BigDecimal("10.900"));

        // Total incl. tax: 109.00 + 10.900 = 119.900
        assertThat(reservation.getTotalCostIncludingTax())
                .isEqualByComparingTo(new BigDecimal("119.900"));
    }

    // ==================== Cenário: Luxury room sem taxa de late checkout ====================

    /**
     * Quartos Luxury não devem ser cobrados pela taxa de late checkout.
     * Também verifica que extras Premium são usados.
     *
     * - 2 noites ($150.00/noite)
     * - Late checkout selecionado (mas Luxury isenta)
     * - 1 extra premium (Massage $6.00/noite)
     * - Sem meals
     *
     * Cálculos:
     * - Room: 2 * 150.00 = 300.00
     * - Late checkout: 0 (Luxury isenta)
     * - Extras: 6.00 * 2 = 12.00
     * - Total excl. tax: 312.00
     * - Tax: 31.20
     * - Total: 343.20
     */
    @Test
    public void fullReservationCost_LuxuryRoom_NoLateCheckoutFee_PremiumExtras() {
        BigDecimal costPerNight = new BigDecimal("150.00");
        BigDecimal lateCheckoutFee = new BigDecimal("45.00"); // configurado mas não cobrado para Luxury

        Room room = createRoom(RoomType.Luxury, costPerNight, 2, lateCheckoutFee);

        LocalDate checkIn = LocalDate.of(2026, 7, 1);
        LocalDate checkOut = LocalDate.of(2026, 7, 3); // 2 noites

        Reservation reservation = createBaseReservation(room, checkIn, checkOut, true);

        Guest adult = new Guest("john", "smith", false);
        reservation.addGuest(adult);

        // Luxury room usa Premium extras
        assertThat(reservation.getExtraPricingType()).isEqualTo(Extra.Type.Premium);

        Set<Extra> generalExtras = Set.of(
                new Extra("Massage", new BigDecimal("6.00"), Extra.Type.Premium, Extra.Category.General)
        );
        reservation.setGeneralExtras(generalExtras);

        // Verificações
        assertThat(reservation.getTotalRoomCost())
                .isEqualByComparingTo(new BigDecimal("300.00"));

        // Late checkout fee isenta para Luxury
        assertThat(reservation.getLateCheckoutFee()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(reservation.getChargeableLateCheckoutFee()).isEqualByComparingTo(BigDecimal.ZERO);

        assertThat(reservation.getTotalRoomCostWithLateCheckoutFee())
                .isEqualByComparingTo(new BigDecimal("300.00"));

        assertThat(reservation.getTotalGeneralExtrasCost())
                .isEqualByComparingTo(new BigDecimal("12.00"));

        assertThat(reservation.getTotalCostExcludingTax())
                .isEqualByComparingTo(new BigDecimal("312.00"));

        assertThat(reservation.getTaxableAmount())
                .isEqualByComparingTo(new BigDecimal("31.200"));

        assertThat(reservation.getTotalCostIncludingTax())
                .isEqualByComparingTo(new BigDecimal("343.200"));
    }

    // ==================== Cenário: Desconto infantil em múltiplas refeições ====================

    /**
     * Verifica que o desconto de 60% é aplicado corretamente em múltiplas refeições
     * para uma criança, enquanto o adulto paga preço integral.
     *
     * - 5 noites
     * - 1 adulto: Breakfast ($2.00) + Dinner ($5.60)
     * - 1 criança: Breakfast ($2.00) + Lunch ($4.00) + Dinner ($5.60)
     */
    @Test
    public void fullReservationCost_WithChildDiscount_MultipleExtras() {
        BigDecimal costPerNight = new BigDecimal("50.00");
        Room room = createRoom(RoomType.Economy, costPerNight, 2, BigDecimal.valueOf(15.00));

        LocalDate checkIn = LocalDate.of(2026, 7, 1);
        LocalDate checkOut = LocalDate.of(2026, 7, 6); // 5 noites

        Reservation reservation = createBaseReservation(room, checkIn, checkOut, false);

        Guest adult = new Guest("john", "smith", false);
        Guest child = new Guest("billy", "smith", true);
        reservation.addGuest(adult);
        reservation.addGuest(child);

        Extra breakfast = new Extra("Breakfast", new BigDecimal("2.00"), Extra.Type.Basic, Extra.Category.Food);
        Extra lunch = new Extra("Lunch", new BigDecimal("4.00"), Extra.Type.Basic, Extra.Category.Food);
        Extra dinner = new Extra("Dinner", new BigDecimal("5.60"), Extra.Type.Basic, Extra.Category.Food);

        // Adult: Breakfast + Dinner
        MealPlan adultMeal = new MealPlan(adult, reservation, List.of(breakfast, dinner), List.of());
        // Child: Breakfast + Lunch + Dinner (com 60% desconto)
        MealPlan childMeal = new MealPlan(child, reservation, List.of(breakfast, lunch, dinner), List.of());

        reservation.setMealPlans(List.of(adultMeal, childMeal));

        // Adult meal: (2.00 * 5) + (5.60 * 5) = 10.00 + 28.00 = 38.00
        assertThat(adultMeal.getTotalMealPlanCost())
                .isEqualByComparingTo(new BigDecimal("38.00"));

        // Child meal (each with 60% discount applied as: total - total*0.60 = total * 0.40):
        // Breakfast: (2.00 * 5) * 0.40 = 4.00
        // Lunch: (4.00 * 5) * 0.40 = 8.00
        // Dinner: (5.60 * 5) * 0.40 = 11.20
        // Total child: 4.00 + 8.00 + 11.20 = 23.20
        BigDecimal expectedChildMealTotal = new BigDecimal("2.00").multiply(new BigDecimal("5"))
                .multiply(new BigDecimal("0.40"))
                .add(new BigDecimal("4.00").multiply(new BigDecimal("5")).multiply(new BigDecimal("0.40")))
                .add(new BigDecimal("5.60").multiply(new BigDecimal("5")).multiply(new BigDecimal("0.40")));

        assertThat(childMeal.getTotalMealPlanCost())
                .isEqualByComparingTo(expectedChildMealTotal);

        // Total meals: 38.00 + 23.20 = 61.20
        BigDecimal expectedTotalMeals = new BigDecimal("38.00").add(expectedChildMealTotal);
        assertThat(reservation.getTotalMealPlansCost())
                .isEqualByComparingTo(expectedTotalMeals);
    }

    // ==================== Cenário: Somente quarto, sem extras nem refeições ====================

    /**
     * Uma reserva simples sem extras e sem refeições deve cobrar apenas o custo do quarto.
     *
     * - 2 noites ($100.00/noite)
     * - Sem late checkout
     * - Sem extras
     * - Sem meals
     *
     * Total excl. tax: 200.00
     * Tax: 20.00
     * Total: 220.00
     */
    @Test
    public void fullReservationCost_NoExtrasNoMeals_RoomCostOnly() {
        BigDecimal costPerNight = new BigDecimal("100.00");
        Room room = createRoom(RoomType.Business, costPerNight, 2, BigDecimal.valueOf(20.00));

        LocalDate checkIn = LocalDate.of(2026, 7, 1);
        LocalDate checkOut = LocalDate.of(2026, 7, 3); // 2 noites

        Reservation reservation = createBaseReservation(room, checkIn, checkOut, false);

        Guest adult = new Guest("john", "smith", false);
        reservation.addGuest(adult);

        // Sem extras e sem meal plans
        assertThat(reservation.getTotalRoomCost())
                .isEqualByComparingTo(new BigDecimal("200.00"));

        assertThat(reservation.getChargeableLateCheckoutFee())
                .isEqualByComparingTo(BigDecimal.ZERO);

        assertThat(reservation.getTotalGeneralExtrasCost())
                .isEqualByComparingTo(BigDecimal.ZERO);

        assertThat(reservation.getTotalMealPlansCost())
                .isEqualByComparingTo(BigDecimal.ZERO);

        assertThat(reservation.getTotalCostExcludingTax())
                .isEqualByComparingTo(new BigDecimal("200.00"));

        assertThat(reservation.getTaxableAmount())
                .isEqualByComparingTo(new BigDecimal("20.000"));

        assertThat(reservation.getTotalCostIncludingTax())
                .isEqualByComparingTo(new BigDecimal("220.000"));
    }

    // ==================== Cenário: Business room com late checkout isento ====================

    /**
     * Business rooms, assim como Luxury, não devem ser cobradas pela taxa de late checkout.
     */
    @Test
    public void fullReservationCost_BusinessRoom_NoLateCheckoutFee() {
        BigDecimal costPerNight = new BigDecimal("80.00");
        BigDecimal lateCheckoutFee = new BigDecimal("30.00");

        Room room = createRoom(RoomType.Business, costPerNight, 1, lateCheckoutFee);

        LocalDate checkIn = LocalDate.of(2026, 7, 1);
        LocalDate checkOut = LocalDate.of(2026, 7, 2); // 1 noite

        Reservation reservation = createBaseReservation(room, checkIn, checkOut, true);

        Guest adult = new Guest("john", "smith", false);
        reservation.addGuest(adult);

        // Business type: premium pricing, no late fee
        assertThat(reservation.getExtraPricingType()).isEqualTo(Extra.Type.Premium);
        assertThat(reservation.getLateCheckoutFee()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(reservation.getChargeableLateCheckoutFee()).isEqualByComparingTo(BigDecimal.ZERO);

        assertThat(reservation.getTotalRoomCostWithLateCheckoutFee())
                .isEqualByComparingTo(new BigDecimal("80.00"));
    }
}
