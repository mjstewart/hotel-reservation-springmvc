package com.demo.persistance;

import com.demo.domain.*;
import com.demo.domain.location.Address;
import com.demo.domain.location.Postcode;
import com.demo.domain.location.State;
import com.demo.persistance.predicates.RoomPredicates;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.junit4.SpringRunner;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.YearMonth;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@DataJpaTest
public class RoomRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private RoomRepository roomRepository;

    /**
     * A room cannot be available if the hotel cannot be found by its hotel id.
     */
    @Test
    public void availableRoom_HotelNotFound() {
        Long hotelId = entityManager.persistAndGetId(new Hotel("Hotel Royal",
                new Address("Hotel Royal", "33 kent street", null,
                        State.VIC, "Melbourne", new Postcode("3000")),
                4, "royal@hotel.com"), Long.class);
        Long nextMissingHotelId = hotelId + 1;

        PageRequest page = PageRequest.of(0, 20);
        Page<Room> rooms = roomRepository.findAll(RoomPredicates.availableRoom(nextMissingHotelId), page);
        assertThat(rooms.getTotalElements()).isEqualTo(0);
    }

    /**
     * When a hotel has many rooms but only 1 room is free (no reservation assigned) it should be the only room returned.
     */
    @Test
    public void availableRoom_AtLeastOneFree() {
        Hotel hotel = new Hotel("Hotel Royal",
                new Address("Hotel Royal", "33 kent street", null,
                        State.VIC, "Melbourne", new Postcode("3000")),
                4, "royal@hotel.com");

        // This room is free since the reservation is null by default.
        Room roomA = new Room("A", RoomType.Luxury, 2, BigDecimal.valueOf(63.3));
        hotel.addRoom(roomA);

        Room roomB = new Room("B", RoomType.Economy, 4, BigDecimal.valueOf(45.4));
        // boiler plate to create a valid reservation...
        Reservation reservation = new Reservation();
        ReservationDates reservationDates = new ReservationDates();
        reservationDates.setCheckInDate(LocalDate.now());
        reservationDates.setCheckOutDate(LocalDate.now().plusDays(3));
        reservationDates.setEstimatedCheckInTime(LocalTime.of(10, 0));
        reservationDates.setPolicyAcknowledged(true);
        reservation.setCompletedPayment(new CompletedPayment(PendingPayment.CreditCardType.MasterCard,
                "3455", "344", YearMonth.of(2018, 1)));
        reservation.setDates(reservationDates);

        roomB.setReservation(reservation);
        hotel.addRoom(roomB);

        Long hotelId = entityManager.persistAndGetId(hotel, Long.class);
        PageRequest page = PageRequest.of(0, 20);
        Page<Room> availableRooms = roomRepository.findAll(RoomPredicates.availableRoom(hotelId), page);

        // The only free hotel is returned.
        assertThat(availableRooms.getTotalElements()).isEqualTo(1);
        assertThat(availableRooms.getContent().get(0).getRoomNumber()).isEqualTo("A");
    }

    /**
     * When a hotel has no free rooms, an empty list should be returned. Note: Both rooms have a non null reservation.
     */
    @Test
    public void availableRoom_NoneFree() {
        Hotel hotel = new Hotel("Hotel Royal",
                new Address("Hotel Royal", "33 kent street", null,
                        State.VIC, "Melbourne", new Postcode("3000")),
                4, "royal@hotel.com");

        // boiler plate to create a valid reservation...
        Room roomA = new Room("A", RoomType.Luxury, 2, BigDecimal.valueOf(63.3));
        Reservation reservationA = new Reservation();
        ReservationDates reservationDatesA = new ReservationDates();
        reservationDatesA.setCheckInDate(LocalDate.now());
        reservationDatesA.setCheckOutDate(LocalDate.now().plusDays(3));
        reservationDatesA.setEstimatedCheckInTime(LocalTime.of(10, 0));
        reservationDatesA.setPolicyAcknowledged(true);
        reservationA.setDates(reservationDatesA);
        reservationA.setCompletedPayment(new CompletedPayment(PendingPayment.CreditCardType.MasterCard,
                "3455", "344", YearMonth.of(2018, 1)));
        roomA.setReservation(reservationA);

        // boiler plate to create a valid reservation...
        Room roomB = new Room("B", RoomType.Economy, 4, BigDecimal.valueOf(45.4));
        Reservation reservationB = new Reservation();
        ReservationDates reservationDatesB = new ReservationDates();
        reservationDatesB.setCheckInDate(LocalDate.now());
        reservationDatesB.setCheckOutDate(LocalDate.now().plusDays(3));
        reservationDatesB.setEstimatedCheckInTime(LocalTime.of(10, 0));
        reservationDatesB.setPolicyAcknowledged(true);
        reservationB.setDates(reservationDatesB);
        reservationB.setCompletedPayment(new CompletedPayment(PendingPayment.CreditCardType.MasterCard,
                "3455", "344", YearMonth.of(2018, 1)));
        roomB.setReservation(reservationB);

        hotel.addRoom(roomA);
        hotel.addRoom(roomB);

        Long id = entityManager.persistAndGetId(hotel, Long.class);
        PageRequest page = PageRequest.of(0, 20);
        Page<Room> availableRooms = roomRepository.findAll(RoomPredicates.availableRoom(id), page);

        // no rooms are free
        assertThat(availableRooms.getTotalElements()).isEqualTo(0);
        assertThat(availableRooms.getContent()).isEmpty();
    }
}