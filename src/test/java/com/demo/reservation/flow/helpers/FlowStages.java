package com.demo.reservation.flow.helpers;

import com.demo.domain.*;
import com.demo.domain.location.Address;
import com.demo.domain.location.Postcode;
import com.demo.domain.location.State;
import com.demo.reservation.flow.forms.ReservationFlow;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Set;

public class FlowStages {

    public static Room createRoom() {
        Address address = new Address("Hotel Royal", "166 Albert Road", null,
                State.VIC, "Melbourne", new Postcode("3000"));

        Hotel hotel = new Hotel("Hotel Royal", address, 3, "royal@hotel.com");
        Room room = new Room("ABC123", RoomType.Economy, 4, BigDecimal.valueOf(23.50));
        room.setId(2L);
        room.setHotel(hotel);
        return room;
    }

    /**
     * Creates a {@code ReservationFlow} containing a {@code Reservation} in the expected state for entering
     * in the reservation dates. This corresponds to the starting {@code Reservation} state when the date form is first
     * retrieved from server. Note how the {@code ReservationFlow.activeStep} is not set since this will be tested.
     */
    public static ReservationFlow pendingDateFlow() {
        ReservationFlow reservationFlow = new ReservationFlow();
        reservationFlow.getReservation().setRoom(createRoom());
        return reservationFlow;
    }

    /**
     * Creates a {@code ReservationFlow} containing a {@code Reservation} in the expected state after successfully
     * posting the reservation date form. This is the starting state for the Guest form.
     */
    public static ReservationFlow dateCompletedFlow() {
        ReservationFlow reservationFlow = pendingDateFlow();

        LocalDate checkIn = LocalDate.now();
        LocalDate checkOut = checkIn.plusDays(5);
        ReservationDates reservationDates = new ReservationDates(
                checkIn, checkOut, LocalTime.of(11, 0), false, true
        );
        reservationFlow.getReservation().setDates(reservationDates);
        return reservationFlow;
    }

    /**
     * Creates a {@code ReservationFlow} in the expected state after completing the guest form. This is the starting
     * state for the Extras form. This will enable quickly rebuilding the current reservation flow state up to the
     * current flow state under test. Note how the {@code ReservationFlow.activeStep} is not set since this will be tested.
     * <ul>
     * <li>Step 1 - ReservationDates = completed</li>
     * <li>Step 2 - Guests = completed</li>
     * <li>Step 3 - General extras = pending</li>
     * </ul>
     */
    public static ReservationFlow guestCompletedFlow() {
        ReservationFlow reservationFlow = dateCompletedFlow();
        reservationFlow.getReservation().addGuest(new Guest("john", "smith", false));
        return reservationFlow;
    }

    /**
     * Creates a {@code ReservationFlow} in the expected after entering in general extras details. This is the
     * starting state for the Meal plans form. This will enable quickly rebuilding the current reservation flow state
     * up to the current flow state under test. Note how the {@code ReservationFlow.activeStep} is not set since
     * this will be tested.
     *
     * <ul>
     * <li>Step 1 - ReservationDates = completed</li>
     * <li>Step 2 - Guests = completed</li>
     * <li>Step 3 - Extras = completed</li>
     * <li>Step 4 - Meal Plans = pending</li>
     * </ul>
     */
    public static ReservationFlow extrasCompletedFlow() {
        ReservationFlow reservationFlow = guestCompletedFlow();
        reservationFlow.getReservation().setGeneralExtras(
                Set.of(new Extra("foxtel", BigDecimal.valueOf(4.5), Extra.Type.Premium, Extra.Category.General))
        );
        return reservationFlow;
    }

    /**
     * Creates a {@code ReservationFlow} in the expected state after completing the general extras form. This is the starting
     * state for the review form. This will enable quickly rebuilding the current reservation flow state up to the
     * current flow state under test. Note how the {@code ReservationFlow.activeStep} is not set since this will be tested.
     * <ul>
     * <li>Step 1 - ReservationDates = completed</li>
     * <li>Step 2 - Guests = completed</li>
     * <li>Step 3 - General extras = completed</li>
     * <li>Step 4 - Meal plans = completed</li>
     * <li>Step 5 - Review = pending</li>
     * </ul>
     */
    public static ReservationFlow mealsCompletedFlow() {
        ReservationFlow reservationFlow = extrasCompletedFlow();
        reservationFlow.getReservation().createMealPlans();

        // As per above in guestCompletedFlow, there is 1 guest but lets just assume they don't select
        // a meal plan so its left empty.
        return reservationFlow;
    }

    /**
     * Simply returns the result of {@link #mealsCompletedFlow} since reviewing doesn't change the state.
     * This is provided for clarity and better semantics.
     */
    public static ReservationFlow reviewCompletedFlow() {
        return mealsCompletedFlow();
    }
}
