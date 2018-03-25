package com.demo.domain;

import org.junit.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.*;

public class ReservationTest {

    private Room createRoom() {
        Room room = new Room("ABC123", RoomType.Economy, 2, BigDecimal.valueOf(25.50));
        room.setHotel(new Hotel("Hotel Royal", "Melbourne", 4, "royal@hotel.com"));
        return room;
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
}