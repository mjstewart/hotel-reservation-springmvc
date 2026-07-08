package com.demo.domain;

import com.demo.domain.location.Address;
import com.demo.domain.location.Postcode;
import com.demo.domain.location.State;
import org.junit.Test;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

public class HotelTest {

    private Hotel createHotel() {
        Address address = new Address("Test Hotel", "1 Test Street", null,
                State.VIC, "Melbourne", new Postcode("3000"));
        return new Hotel("Test Hotel", address, 4, "test@hotel.com");
    }

    private Hotel createHotelWithCustomTimes(LocalTime earliest, LocalTime latest) {
        Address address = new Address("Test Hotel", "1 Test Street", null,
                State.VIC, "Melbourne", new Postcode("3000"));
        return new Hotel("Test Hotel", address, 4, "test@hotel.com",
                earliest, latest,
                LocalTime.of(11, 0), LocalTime.of(22, 0),
                BigDecimal.valueOf(15.95));
    }

    // ==================== allowableCheckInTimes() ====================

    @Test
    public void allowableCheckInTimes_DefaultTimes_GeneratesCorrectRange() {
        Hotel hotel = createHotel();
        // defaults: 7:00 to 22:00
        List<LocalTime> times = hotel.allowableCheckInTimes();

        assertThat(times).hasSize(16);
        assertThat(times.get(0)).isEqualTo(LocalTime.of(7, 0));
        assertThat(times.get(times.size() - 1)).isEqualTo(LocalTime.of(22, 0));
    }


    @Test
    public void allowableCheckInTimes_CustomRange_GeneratesCorrectHours() {
        Hotel hotel = createHotelWithCustomTimes(
                LocalTime.of(9, 0),
                LocalTime.of(12, 0)
        );

        List<LocalTime> times = hotel.allowableCheckInTimes();

        assertThat(times).hasSize(4);
        assertThat(times).containsExactly(
                LocalTime.of(9, 0),
                LocalTime.of(10, 0),
                LocalTime.of(11, 0),
                LocalTime.of(12, 0)
        );
    }


    @Test
    public void allowableCheckInTimes_SameTime_ReturnsSingleTime() {
        Hotel hotel = createHotelWithCustomTimes(
                LocalTime.of(14, 0),
                LocalTime.of(14, 0)
        );

        List<LocalTime> times = hotel.allowableCheckInTimes();

        assertThat(times).hasSize(1);
        assertThat(times).containsExactly(LocalTime.of(14, 0));
    }

    // ==================== addRoom() - Relacionamento bidirecional ====================


    @Test
    public void addRoom_SetsHotelBidirectional() {
        Hotel hotel = createHotel();
        Room room = new Room("R1", RoomType.Economy, 2, BigDecimal.valueOf(50.00));

        hotel.addRoom(room);

        assertThat(hotel.getRooms()).contains(room);
        assertThat(room.getHotel()).isEqualTo(hotel);
    }


    @Test
    public void addRoom_MultipleRooms_AllAdded() {
        Hotel hotel = createHotel();
        Room room1 = new Room("R1", RoomType.Economy, 1, BigDecimal.valueOf(30.00));
        Room room2 = new Room("R2", RoomType.Luxury, 4, BigDecimal.valueOf(200.00));
        Room room3 = new Room("R3", RoomType.Business, 2, BigDecimal.valueOf(100.00));

        hotel.addRoom(room1);
        hotel.addRoom(room2);
        hotel.addRoom(room3);

        assertThat(hotel.getRooms()).hasSize(3);
        assertThat(hotel.getRooms()).containsExactlyInAnyOrder(room1, room2, room3);
    }

    // ==================== equals / hashCode ====================


    @Test
    public void equals_SameAddressAndEmail_AreEqual() {
        Address address = new Address("Hotel", "1 St", null, State.VIC, "Melbourne", new Postcode("3000"));
        Hotel hotel1 = new Hotel("Hotel A", address, 3, "test@hotel.com");
        Hotel hotel2 = new Hotel("Hotel B", address, 5, "test@hotel.com");

        assertThat(hotel1).isEqualTo(hotel2);
    }


    @Test
    public void equals_DifferentEmail_NotEqual() {
        Address address = new Address("Hotel", "1 St", null, State.VIC, "Melbourne", new Postcode("3000"));
        Hotel hotel1 = new Hotel("Hotel A", address, 3, "a@hotel.com");
        Hotel hotel2 = new Hotel("Hotel B", address, 3, "b@hotel.com");

        assertThat(hotel1).isNotEqualTo(hotel2);
    }

    // ==================== Late checkout fee ====================


    @Test
    public void defaultLateCheckoutFee() {
        Hotel hotel = createHotel();
        assertThat(hotel.getLateCheckoutFee()).isEqualByComparingTo(BigDecimal.valueOf(15.95));
    }
}
