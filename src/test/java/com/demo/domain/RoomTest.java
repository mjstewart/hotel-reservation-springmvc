package com.demo.domain;

import com.demo.domain.location.Address;
import com.demo.domain.location.Postcode;
import com.demo.domain.location.State;
import org.junit.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.*;


public class RoomTest {

    private Room createRoom() {
        return new Room("R101", RoomType.Economy, 2, BigDecimal.valueOf(50.00));
    }

    // ==================== setReservation() - Relacionamento bidirecional ====================


    @Test
    public void setReservation_SetsBidirectionalRelationship() {
        Room room = createRoom();
        Reservation reservation = new Reservation();

        room.setReservation(reservation);

        assertThat(room.getReservation()).isEqualTo(reservation);
        assertThat(reservation.getRoom()).isEqualTo(room);
    }


    @Test
    public void setReservation_Null_DoesNotSetReservation() {
        Room room = createRoom();

        room.setReservation(null);

        assertThat(room.getReservation()).isNull();
    }

    // ==================== isReserved() ====================


    @Test
    public void isReserved_WithReservation_ReturnsTrue() {
        Room room = createRoom();
        room.setReservation(new Reservation());

        assertThat(room.isReserved()).isTrue();
    }

 
    @Test
    public void isReserved_WithoutReservation_ReturnsFalse() {
        Room room = createRoom();

        assertThat(room.isReserved()).isFalse();
    }

    // ==================== equals / hashCode ====================


    @Test
    public void equals_SameRoomNumber_AreEqual() {
        Room room1 = new Room("R101", RoomType.Economy, 2, BigDecimal.valueOf(50.00));
        Room room2 = new Room("R101", RoomType.Luxury, 4, BigDecimal.valueOf(200.00));

        assertThat(room1).isEqualTo(room2);
        assertThat(room1.hashCode()).isEqualTo(room2.hashCode());
    }


    @Test
    public void equals_DifferentRoomNumber_NotEqual() {
        Room room1 = new Room("R101", RoomType.Economy, 2, BigDecimal.valueOf(50.00));
        Room room2 = new Room("R102", RoomType.Economy, 2, BigDecimal.valueOf(50.00));

        assertThat(room1).isNotEqualTo(room2);
    }

    // ==================== Getters / Setters básicos ====================


    @Test
    public void setHotel_AssociatesHotelCorrectly() {
        Room room = createRoom();
        Address address = new Address("Hotel", "1 St", null, State.VIC, "City", new Postcode("3000"));
        Hotel hotel = new Hotel("Test", address, 3, "test@email.com");

        room.setHotel(hotel);

        assertThat(room.getHotel()).isEqualTo(hotel);
    }


    @Test
    public void constructorAndGetters_SetFieldsCorrectly() {
        Room room = new Room("SUITE-A", RoomType.Luxury, 4, BigDecimal.valueOf(199.99));

        assertThat(room.getRoomNumber()).isEqualTo("SUITE-A");
        assertThat(room.getRoomType()).isEqualTo(RoomType.Luxury);
        assertThat(room.getBeds()).isEqualTo(4);
        assertThat(room.getCostPerNight()).isEqualByComparingTo(BigDecimal.valueOf(199.99));
    }
}
