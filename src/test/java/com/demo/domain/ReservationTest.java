package com.demo.domain;

import com.demo.domain.location.Address;
import com.demo.domain.location.Postcode;
import com.demo.domain.location.State;
import org.junit.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

public class ReservationTest {

    private Room createRoom() {
        Address address = new Address("Royal Hotel", "166 Albert Road", null,
                State.VIC, "Melbourne", new Postcode("3000"));

        Room room = new Room("ABC123", RoomType.Economy, 2, BigDecimal.valueOf(25.50));
        room.setHotel(new Hotel("Royal Hotel", address, 4, "royal@hotel.com"));
        return room;
    }

    /**
     * Confirm isRoomFull determines a room is fool when its guest limit is reached.
     */
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
    public void hasGuests() {
        Room room = createRoom();
        room.setBeds(2);

        Reservation reservation = new Reservation();
        reservation.setRoom(room);

        assertThat(reservation.hasGuests()).isFalse();

        reservation.addGuest(new Guest("marie", "smith", false));
        assertThat(reservation.hasGuests()).isTrue();
    }

    @Test
    public void hasAtLeastOneAdultGuest() {
        Room room = createRoom();
        room.setBeds(2);

        Reservation reservation = new Reservation();
        reservation.setRoom(room);

        assertThat(reservation.hasAtLeastOneAdultGuest()).isFalse();

        reservation.addGuest(new Guest("marie", "smith", true));

        assertThat(reservation.hasAtLeastOneAdultGuest()).isFalse();

        reservation.addGuest(new Guest("marie", "smith", false));

        assertThat(reservation.hasAtLeastOneAdultGuest()).isTrue();
    }

    /**
     * You should not be able to add a guest to a room that is already full.
     */
    @Test
    public void addGuest_OnlyWhenThereAreFreeBeds() {
        Room room = createRoom();
        room.setBeds(2);

        Reservation reservation = new Reservation();
        reservation.setRoom(room);

        Guest john = new Guest("john", "smith", false);
        Guest sara = new Guest("sara", "smith", true);
        reservation.addGuest(john);
        reservation.addGuest(sara);

        reservation.addGuest(new Guest("marie", "smith", false));
        reservation.addGuest(new Guest("ryan", "smith", false));

        assertThat(reservation.getGuests().size()).isEqualTo(2);
        assertThat(reservation.getGuests()).containsExactlyInAnyOrder(john, sara);
    }

    @Test
    public void removeGuestById_NoGuestExists_HasNoEffect() {
        Reservation reservation = new Reservation();

        boolean removed = reservation.removeGuestById(UUID.randomUUID());
        assertThat(removed).isFalse();
        assertThat(reservation.getGuests()).isEmpty();
    }

    @Test
    public void removeGuestById_GuestExists_GuestIsRemoved() {
        Reservation reservation = new Reservation();
        Room room = createRoom();
        room.setBeds(2);
        reservation.setRoom(room);

        Guest guestA = new Guest("john", "smith", false);
        Guest guestB = new Guest("nicole", "smith", false);

        reservation.addGuest(guestA);
        reservation.addGuest(guestB);
        assertThat(reservation.getGuests()).containsExactlyInAnyOrder(guestA, guestB);

        boolean removed = reservation.removeGuestById(guestA.getTempId());

        assertThat(removed).isTrue();
        assertThat(reservation.getGuests()).containsExactly(guestB);
    }

    /**
     * All general extras must be {@code Extra.Category.General}, otherwise an exception is thrown.
     */
    @Test
    public void setGeneralExtras() {
        Reservation reservation = new Reservation();

        Set<Extra> generalExtras = Set.of(new Extra("a", BigDecimal.valueOf(1.50), Extra.Type.Basic, Extra.Category.General));
        reservation.setGeneralExtras(generalExtras);

        assertThat(reservation.getGeneralExtras()).isEqualTo(generalExtras);
    }

    /**
     * When the room is luxury, {@code Extra.Type.Premium} is the type to base food and general extras charging from.
     */
    @Test
    public void getExtraPricingType_LuxuryRoomIsPremium() {
        Reservation reservation = new Reservation();
        Room room = createRoom();
        room.setRoomType(RoomType.Luxury);

        reservation.setRoom(room);

        assertThat(reservation.getExtraPricingType()).isEqualTo(Extra.Type.Premium);
    }

    /**
     * When the room is business, {@code Extra.Type.Premium} is the type to base food and general extras charging from.
     */
    @Test
    public void getExtraPricingType_BusinessRoomIsPremium() {
        Reservation reservation = new Reservation();
        Room room = createRoom();
        room.setRoomType(RoomType.Business);

        reservation.setRoom(room);

        assertThat(reservation.getExtraPricingType()).isEqualTo(Extra.Type.Premium);
    }

    /**
     * When the room is Balcony, {@code Extra.Type.Basic} is the type to base food and general extras charging from.
     */
    @Test
    public void getExtraPricingType_BalconyRoomIsBasic() {
        Reservation reservation = new Reservation();
        Room room = createRoom();
        room.setRoomType(RoomType.Balcony);

        reservation.setRoom(room);

        assertThat(reservation.getExtraPricingType()).isEqualTo(Extra.Type.Basic);
    }

    /**
     * When the room is Economy, {@code Extra.Type.Basic} is the type to base food and general extras charging from.
     */
    @Test
    public void getExtraPricingType_EconomyRoomIsBasic() {
        Reservation reservation = new Reservation();
        Room room = createRoom();
        room.setRoomType(RoomType.Economy);

        reservation.setRoom(room);

        assertThat(reservation.getExtraPricingType()).isEqualTo(Extra.Type.Basic);
    }

    /**
     * If general extras contains a {@code Extra.Category.Food} an exception is thrown.
     */
    @Test(expected = IllegalArgumentException.class)
    public void setGeneralExtras_ThrowsException_WhenInvalidExtras() {
        Reservation reservation = new Reservation();
        reservation.setGeneralExtras(
                Set.of(new Extra("a", BigDecimal.valueOf(1.50), Extra.Type.Basic, Extra.Category.Food))
        );
        assertThat(reservation.getGeneralExtras()).isNull();
    }

    /**
     * When a customer chooses the late checkout option, the late fee should be returned.
     */
    @Test
    public void getChargeableLateCheckoutFee_WhenLateCheckout_ChargeFee() {
        BigDecimal lateCheckoutFee = BigDecimal.valueOf(20.50);

        Room room = createRoom();
        room.getHotel().setLateCheckoutFee(lateCheckoutFee);
        Reservation reservation = new Reservation();
        reservation.setRoom(room);

        // enable late checkout
        reservation.getDates().setLateCheckout(true);

        assertThat(reservation.getChargeableLateCheckoutFee()).isEqualTo(lateCheckoutFee);
    }

    /**
     * When a customer does not select late checkout, the chargeable late fee is $0.00.
     */
    @Test
    public void getChargeableLateCheckoutFee_WhenNoLateCheckout_NoCharge() {
        BigDecimal lateCheckoutFee = BigDecimal.valueOf(20.50);

        Room room = createRoom();
        room.getHotel().setLateCheckoutFee(lateCheckoutFee);
        Reservation reservation = new Reservation();
        reservation.setRoom(room);

        // no late checkout = $0.00
        reservation.getDates().setLateCheckout(false);

        assertThat(reservation.getChargeableLateCheckoutFee()).isEqualTo(BigDecimal.ZERO);
    }

    /**
     * When a customer selects late checkout but have a {@code RoomType.Luxury} room, the late checkout fee is waived and
     * should return $0.00.
     */
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

    /**
     * When a customer selects late checkout but have a {@code RoomType.Business}, the late checkout fee is waived and
     * should return $0.00.
     */
    @Test
    public void getLateCheckoutFee_WhenBusinessRoomType_NoLateCharge() {
        BigDecimal lateCheckoutFee = BigDecimal.valueOf(20.50);

        Room room = createRoom();
        room.setRoomType(RoomType.Business);
        room.getHotel().setLateCheckoutFee(lateCheckoutFee);

        Reservation reservation = new Reservation();
        reservation.setRoom(room);
        reservation.getDates().setLateCheckout(true);

        assertThat(reservation.getLateCheckoutFee()).isEqualTo(BigDecimal.ZERO);
    }

    /**
     * When a customer selects late checkout but have a {@code RoomType.Economy}, the late checkout fee is applied.
     */
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

    /**
     * When a customer selects late checkout but have a {@code RoomType.Balcony}, the late checkout fee is applied.
     */
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
     * If the reservation was for 0 nights, the cost should be 0.
     * Technically this won't occur since the business rule of at least 1 night is validated, however is
     * tested as a sanity check to ensure room cost is being calculated correctly.
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

    /**
     * A room with an active late fee should not be considered in the total room cost calculation.
     */
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

        // expected cost for 3 nights, note how no late fee is considered for this calculation.
        BigDecimal expectedCost = costPerNight.multiply(BigDecimal.valueOf(3));

        assertThat(reservation.getTotalRoomCost()).isEqualTo(expectedCost);
    }

    /**
     * When the late checkout is NOT enabled and the room type does not waive the late checkout fee,
     * the late checkout fee should NOT be included in the total room cost.
     */
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

    /**
     * When the late checkout is enabled and the room type does not waive the late checkout fee,
     * the late checkout fee should be included in the total room cost.
     */
    @Test
    public void getTotalRoomCostWithLateCheckoutFee_WithLateCheckoutFee_CorrectCost() {
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

    /**
     * When there are no general extras, the cost should be zero.
     */
    @Test
    public void getTotalGeneralExtrasCost_NoExtras() {
        Set<Extra> extras = Set.of();
        Reservation reservation = new Reservation();
        reservation.setGeneralExtras(extras);

        reservation.getDates().setCheckInDate(LocalDate.of(2018, 1, 1));
        reservation.getDates().setCheckOutDate(LocalDate.of(2018, 1, 4));

        assertThat(reservation.getTotalGeneralExtrasCost()).isEqualTo(BigDecimal.ZERO);
    }

    /**
     * When there are general extras, the total cost should be equal to the sum of each extra multiplied by
     * the total night stay.
     */
    @Test
    public void getTotalGeneralExtrasCost() {
        BigDecimal nights = BigDecimal.valueOf(3);

        // Sum the result of each extras daily price multiplied by total nights.
        // Eg: (Extra "a" 1.20 * 3) + (Extra "b" 3.80 * 3).
        Set<Extra> extras = Set.of(
                new Extra("a", BigDecimal.valueOf(1.20), Extra.Type.Basic, Extra.Category.General),
                new Extra("b", BigDecimal.valueOf(3.80), Extra.Type.Basic, Extra.Category.General)
        );

        Reservation reservation = new Reservation();
        reservation.setGeneralExtras(extras);

        reservation.getDates().setCheckInDate(LocalDate.of(2018, 1, 1));
        reservation.getDates().setCheckOutDate(LocalDate.of(2018, 1, 4));

        BigDecimal expectedSum = BigDecimal.valueOf(1.20).multiply(nights)
                .add(BigDecimal.valueOf(3.80).multiply(nights));

        assertThat(reservation.getTotalGeneralExtrasCost()).isEqualTo(expectedSum);
    }

    /**
     * When there are no meal plans, the cost should be zero.
     */
    @Test
    public void getTotalMealPlansCost_NoMealPlans() {
        Reservation reservation = new Reservation();
        assertThat(reservation.getTotalMealPlansCost()).isEqualTo(BigDecimal.ZERO);
    }

    /**
     * When there are many meal plans, the total cost of all meal plans should be the sum of each individual meal plan
     * per guest.
     */
    @Test
    public void getTotalMealPlansCost() {
        Reservation reservation = new Reservation();

        BigDecimal nights = BigDecimal.valueOf(3);
        reservation.getDates().setCheckInDate(LocalDate.of(2018, 1, 1));
        reservation.getDates().setCheckOutDate(LocalDate.of(2018, 1, 4));

        // Meal plan 1 calculation
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

        // Meal plan 2 calculation
        List<Extra> foodExtrasPlan2 = List.of(
                new Extra("Dinner", new BigDecimal("5.24"), Extra.Type.Basic, Extra.Category.Food)
        );
        MealPlan mealPlan2 = new MealPlan(
                new Guest("sally", "smith", false),
                reservation, foodExtrasPlan2, List.of());

        BigDecimal expectedMealPlan2Cost = BigDecimal.valueOf(5.24).multiply(nights);

        BigDecimal expectedCost = expectedMealPlan1Cost.add(expectedMealPlan2Cost);

        reservation.setMealPlans(List.of(mealPlan1, mealPlan2));

        assertThat(reservation.getTotalMealPlansCost()).isEqualTo(expectedCost);
    }

    /**
     * Ensure that the total cost is calculated by including all the correct sub totals.
     */
    @Test
    public void getTotalCostExcludingTax() {
        Reservation reservation = mock(Reservation.class);

        // mock the sub totals
        when(reservation.getTotalRoomCostWithLateCheckoutFee()).thenReturn(BigDecimal.valueOf(5));
        when(reservation.getTotalGeneralExtrasCost()).thenReturn(BigDecimal.valueOf(6));
        when(reservation.getTotalMealPlansCost()).thenReturn(BigDecimal.valueOf(7));

        // then call the real method
        when(reservation.getTotalCostExcludingTax()).thenCallRealMethod();

        // then hope the real method sums everything up correctly
        assertThat(reservation.getTotalCostExcludingTax()).isEqualTo(BigDecimal.valueOf(18));

        // by actually calling the correct methods!
        verify(reservation, times(1)).getTotalRoomCostWithLateCheckoutFee();
        verify(reservation, times(1)).getTotalGeneralExtrasCost();
        verify(reservation, times(1)).getTotalMealPlansCost();
    }

    /**
     * Make sure that the correct tax is calculated from the total cost excluding tax.
     */
    @Test
    public void getTaxableAmount() {
        Reservation reservation = mock(Reservation.class);
        // the method under test should not be mocked.
        when(reservation.getTaxableAmount()).thenCallRealMethod();

        // mock the total cost without tax
        BigDecimal total = BigDecimal.valueOf(100);
        when(reservation.getTotalCostExcludingTax()).thenReturn(total);

        // then apply the tax
        BigDecimal taxableAmount = total.multiply(BigDecimal.valueOf(Reservation.TAX_AMOUNT));
        assertThat(reservation.getTaxableAmount()).isEqualTo(taxableAmount);

        // and make sure getTaxableAmount calls the correct method to base the tax amount off.
        verify(reservation, times(1)).getTotalCostExcludingTax();
    }

    @Test
    public void getTotalCostIncludingTax() {
        Reservation reservation = mock(Reservation.class);
        // the method under test should not be mocked.
        when(reservation.getTotalCostIncludingTax()).thenCallRealMethod();

        // the total cost without tax
        BigDecimal totalExcludingTax = BigDecimal.valueOf(121.60);
        when(reservation.getTotalCostExcludingTax()).thenReturn(totalExcludingTax);

        // + the taxable amount derived from getTotalCostExcludingTax
        BigDecimal taxableAmount = BigDecimal.valueOf(20.60);
        when(reservation.getTaxableAmount()).thenReturn(taxableAmount);

        // should equal the total cost including tax!
        assertThat(reservation.getTotalCostIncludingTax())
                .isEqualTo(totalExcludingTax.add(taxableAmount));

        // with the method under test calling the correct sub total methods to calculate the result
        verify(reservation, times(1)).getTotalCostExcludingTax();
        verify(reservation, times(1)).getTaxableAmount();
    }


    /**
     * Reservation.mealPlans should contain a list of {@code MealPlan}s created for each {@code Guest}.
     * In this test when there are no guests there should be no meal plans created.
     */
    @Test
    public void createMealPlans_NoGuestsExists_EmptyMealPlans() {
        Reservation reservation = new Reservation();
        Room room = createRoom();
        room.setBeds(2);
        reservation.setRoom(room);

        reservation.createMealPlans();
        assertThat(reservation.getMealPlans()).isEmpty();
    }

    /**
     * Reservation.mealPlans should contain a list of {@code MealPlan}s created for each {@code Guest}
     * which are ordered by the rules in {@code Guest.comparator} for display in thymeleaf template.
     */
    @Test
    public void createMealPlans_GuestsExists_MealPlansInCorrectOrder() {
        Reservation reservation = new Reservation();
        Room room = createRoom();

        // create room for 4 guests
        room.setBeds(4);
        reservation.setRoom(room);

        Guest guestA = new Guest("a", "e", true);
        reservation.addGuest(guestA);
        Guest guestB = new Guest("b", "g", false);
        reservation.addGuest(guestB);
        Guest guestC = new Guest("b", "f", false);
        reservation.addGuest(guestC);
        Guest guestD = new Guest("a", "c", true);
        reservation.addGuest(guestD);

        reservation.createMealPlans();

        reservation.getMealPlans().stream()
                .map(MealPlan::getGuest)
                .forEach(System.out::println);

        // child should be ordered last, tie between first name to be broken be ascending last name.
        assertThat(reservation.getMealPlans())
                .extracting(MealPlan::getGuest)
                .containsExactly(guestC, guestB, guestD, guestA);
    }

    /**
     * When each guest has a meal plan but no food extras are added, this should be treated as the meal
     * plans being empty which is useful to not display meal sub totals on UI if there is nothing selected.
     */
    @Test
    public void hasEmptyMealPlans_MealPlansWithNoFoodSumToZero() {
        Reservation reservation = new Reservation();
        Room room = createRoom();

        // create room for 4 guests
        room.setBeds(4);
        reservation.setRoom(room);

        Guest guestA = new Guest("a", "e", true);
        reservation.addGuest(guestA);
        Guest guestB = new Guest("b", "g", false);
        reservation.addGuest(guestB);
        Guest guestC = new Guest("b", "f", false);
        reservation.addGuest(guestC);
        Guest guestD = new Guest("a", "c", true);
        reservation.addGuest(guestD);

        // create the meal plans
        reservation.createMealPlans();

        // sanity check to ensure each guest has a meal plan
        assertThat(reservation.getMealPlans().size()).isEqualTo(4);

        // no guest added a food extra to the meal plan so should be empty!
        assertThat(reservation.hasEmptyMealPlans()).isTrue();
    }

    /**
     * When each guest has a meal plan but no food extras are added, this should be treated as the meal
     * plans being empty which is useful to not display meal sub totals on UI if there is nothing selected.
     */
    @Test
    public void hasEmptyMealPlans_MealPlansHaveFood() {
        Reservation reservation = new Reservation();
        Room room = createRoom();

        // create room for 4 guests
        room.setBeds(4);
        reservation.setRoom(room);

        Guest guestA = new Guest("a", "e", true);
        reservation.addGuest(guestA);
        Guest guestB = new Guest("b", "g", false);
        reservation.addGuest(guestB);
        Guest guestC = new Guest("b", "f", false);
        reservation.addGuest(guestC);
        Guest guestD = new Guest("a", "c", true);
        reservation.addGuest(guestD);

        // create the meal plans
        reservation.createMealPlans();

        // sanity check to ensure each guest has a meal plan
        assertThat(reservation.getMealPlans().size()).isEqualTo(4);

        // pick a random meal plan and add food to it which will cause the total price to be > 0.
        reservation.getMealPlans().get(0).setFoodExtras(List.of(
                new Extra("breakfast", BigDecimal.valueOf(5.50), Extra.Type.Basic, Extra.Category.Food)
        ));

        // 1 guest has a food extra
        assertThat(reservation.hasEmptyMealPlans()).isFalse();
    }
}