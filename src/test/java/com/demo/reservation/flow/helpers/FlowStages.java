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

    public static ReservationFlow pendingDateFlow() {
        ReservationFlow reservationFlow = new ReservationFlow();
        reservationFlow.getReservation().setRoom(createRoom());
        return reservationFlow;
    }

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

    public static ReservationFlow guestCompletedFlow() {
        ReservationFlow reservationFlow = dateCompletedFlow();
        reservationFlow.getReservation().addGuest(new Guest("john", "smith", false));
        return reservationFlow;
    }

    public static ReservationFlow extrasCompletedFlow() {
        ReservationFlow reservationFlow = guestCompletedFlow();
        reservationFlow.getReservation().setGeneralExtras(
                Set.of(new Extra("foxtel", BigDecimal.valueOf(4.5), Extra.Type.Premium, Extra.Category.General))
        );
        return reservationFlow;
    }

    public static ReservationFlow mealsCompletedFlow() {
        ReservationFlow reservationFlow = extrasCompletedFlow();
        reservationFlow.getReservation().createMealPlans();

        return reservationFlow;
    }

    public static ReservationFlow reviewCompletedFlow() {
        return mealsCompletedFlow();
    }
}
