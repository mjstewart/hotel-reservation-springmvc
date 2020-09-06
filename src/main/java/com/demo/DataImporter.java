package com.demo;

import com.demo.domain.Extra;
import com.demo.domain.Hotel;
import com.demo.domain.Room;
import com.demo.domain.RoomType;
import com.demo.domain.location.Address;
import com.demo.domain.location.Postcode;
import com.demo.domain.location.State;
import com.demo.persistance.HotelRepository;
import com.demo.reservation.ExtraRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalTime;

@Component
@Profile({"!test", "!integration"})
public class DataImporter {
    private HotelRepository hotelRepository;
    private ExtraRepository extraRepository;

    public DataImporter(HotelRepository hotelRepository, ExtraRepository extraRepository) {
        this.hotelRepository = hotelRepository;
        this.extraRepository = extraRepository;
    }

    @Bean
    public CommandLineRunner insertTestData() {
        return args -> {
            // For simplicity every hotel will have the same extras.
            System.out.println("-------------------CommandLineRunner, insserting sample data");
            // basic
            extraRepository.save(new Extra("Foxtel", new BigDecimal("1.20"), Extra.Type.Basic, Extra.Category.General));
            extraRepository.save(new Extra("Unlimited Internet", new BigDecimal("2.00"), Extra.Type.Basic, Extra.Category.General));
            extraRepository.save(new Extra("Laundry", new BigDecimal("2.50"), Extra.Type.Basic, Extra.Category.General));
            extraRepository.save(new Extra("Upgraded mini bar", new BigDecimal("12.00"), Extra.Type.Basic, Extra.Category.General));

            extraRepository.save(new Extra("Breakfast", new BigDecimal("2.00"), Extra.Type.Basic, Extra.Category.Food));
            extraRepository.save(new Extra("Lunch", new BigDecimal("4.00"), Extra.Type.Basic, Extra.Category.Food));
            extraRepository.save(new Extra("Dinner", new BigDecimal("5.60"), Extra.Type.Basic, Extra.Category.Food));

            // premium
            extraRepository.save(new Extra("Foxtel", new BigDecimal("0.20"), Extra.Type.Premium, Extra.Category.General));
            extraRepository.save(new Extra("Upgraded mini bar", new BigDecimal("1.50"), Extra.Type.Premium, Extra.Category.General));
            extraRepository.save(new Extra("Massage", new BigDecimal("6.00"), Extra.Type.Premium, Extra.Category.General));

            extraRepository.save(new Extra("Breakfast", new BigDecimal("1.50"), Extra.Type.Premium, Extra.Category.Food));
            extraRepository.save(new Extra("Lunch", new BigDecimal("3.20"), Extra.Type.Premium, Extra.Category.Food));
            extraRepository.save(new Extra("Dinner", new BigDecimal("5.00"), Extra.Type.Premium, Extra.Category.Food));

            createHotel1();
            createHotel2();
            createHotel3();
            createHotel4();
            createHotel5();
            createHotel6();
        };
    }

    private void createHotel1() {
        LocalTime earliestCheckInTime = LocalTime.of(9, 0);
        LocalTime latestCheckInTime = LocalTime.of(20, 0);
        LocalTime earliestCheckOutTime = LocalTime.of(12, 0);
        LocalTime latestCheckOutTime = LocalTime.of(14, 0);
        BigDecimal lateCheckoutFee = BigDecimal.valueOf(45.60);

        Address address = new Address("The Grand Hotel", "166 Albert Road", null,
                State.VIC, "Melbourne", new Postcode("3000"));

        Hotel grandHotel = new Hotel("The Grand Hotel", address, 4, "grandhotel.com.au",
                earliestCheckInTime,
                latestCheckInTime,
                earliestCheckOutTime,
                latestCheckOutTime,
                lateCheckoutFee);

        Room room1 = new Room("G1", RoomType.Economy, 1, BigDecimal.valueOf(65.12));
        Room room2 = new Room("G2", RoomType.Business, 2, BigDecimal.valueOf(105.45));
        Room room3 = new Room("G3", RoomType.Luxury, 4, BigDecimal.valueOf(205.66));
        Room room4 = new Room("G4", RoomType.Economy, 2, BigDecimal.valueOf(35.40));

        grandHotel.addRoom(room1);
        grandHotel.addRoom(room2);
        grandHotel.addRoom(room3);
        grandHotel.addRoom(room4);

        hotelRepository.save(grandHotel);
    }

    private void createHotel2() {
        LocalTime earliestCheckInTime = LocalTime.of(8, 0);
        LocalTime latestCheckInTime = LocalTime.of(19, 0);
        LocalTime earliestCheckOutTime = LocalTime.of(13, 0);
        LocalTime latestCheckOutTime = LocalTime.of(15, 0);
        BigDecimal lateCheckoutFee = BigDecimal.valueOf(29.40);

        Address address2 = new Address("Glen Iris", "99A Glen Road", null,
                State.VIC, "Glen Waverley", new Postcode("3150"));

        Hotel hotel = new Hotel("Glen Iris", address2, 3, "glenhotel.com.au",
                earliestCheckInTime,
                latestCheckInTime,
                earliestCheckOutTime,
                latestCheckOutTime,
                lateCheckoutFee);

        Room room1 = new Room("H1", RoomType.Economy, 5, BigDecimal.valueOf(85.12));
        Room room2 = new Room("H2", RoomType.Business, 2, BigDecimal.valueOf(105.45));
        Room room3 = new Room("H3", RoomType.Luxury, 4, BigDecimal.valueOf(205.66));
        Room room4 = new Room("H4", RoomType.Economy, 2, BigDecimal.valueOf(35.40));

        hotel.addRoom(room1);
        hotel.addRoom(room2);
        hotel.addRoom(room3);
        hotel.addRoom(room4);

        hotelRepository.save(hotel);
    }

    private void createHotel3() {
        LocalTime earliestCheckInTime = LocalTime.of(9, 0);
        LocalTime latestCheckInTime = LocalTime.of(20, 0);
        LocalTime earliestCheckOutTime = LocalTime.of(12, 0);
        LocalTime latestCheckOutTime = LocalTime.of(14, 0);
        BigDecimal lateCheckoutFee = BigDecimal.valueOf(45.60);

        Address address = new Address("Cevello Blanca", "2 smith street", null,
                State.VIC, "Carlton", new Postcode("3053"));

        Hotel hotel = new Hotel("Cevello Blanca", address, 5, "cevellohotel.com.au",
                earliestCheckInTime,
                latestCheckInTime,
                earliestCheckOutTime,
                latestCheckOutTime,
                lateCheckoutFee);

        Room room1 = new Room("C1", RoomType.Economy, 4, BigDecimal.valueOf(65.12));
        Room room2 = new Room("C2", RoomType.Business, 4, BigDecimal.valueOf(105.45));
        Room room3 = new Room("C3", RoomType.Luxury, 4, BigDecimal.valueOf(205.66));
        Room room4 = new Room("C4", RoomType.Economy, 1, BigDecimal.valueOf(35.40));

        hotel.addRoom(room1);
        hotel.addRoom(room2);
        hotel.addRoom(room3);
        hotel.addRoom(room4);

        hotelRepository.save(hotel);
    }

    private void createHotel4() {
        LocalTime earliestCheckInTime = LocalTime.of(9, 0);
        LocalTime latestCheckInTime = LocalTime.of(20, 0);
        LocalTime earliestCheckOutTime = LocalTime.of(12, 0);
        LocalTime latestCheckOutTime = LocalTime.of(14, 0);
        BigDecimal lateCheckoutFee = BigDecimal.valueOf(45.60);

        Address address = new Address("Bravo", "7 apple avenue", null,
                State.VIC, "Docklands", new Postcode("3008"));

        Hotel hotel = new Hotel("Bravo", address, 2, "bravoohotel.com.au",
                earliestCheckInTime,
                latestCheckInTime,
                earliestCheckOutTime,
                latestCheckOutTime,
                lateCheckoutFee);

        Room room1 = new Room("B1", RoomType.Economy, 4, BigDecimal.valueOf(35.12));
        Room room2 = new Room("B2", RoomType.Business, 5, BigDecimal.valueOf(115.35));
        Room room3 = new Room("B3", RoomType.Luxury, 4, BigDecimal.valueOf(215.36));
        Room room4 = new Room("B4", RoomType.Economy, 2, BigDecimal.valueOf(135.40));

        hotel.addRoom(room1);
        hotel.addRoom(room2);
        hotel.addRoom(room3);
        hotel.addRoom(room4);

        hotelRepository.save(hotel);
    }

    private void createHotel5() {
        LocalTime earliestCheckInTime = LocalTime.of(9, 0);
        LocalTime latestCheckInTime = LocalTime.of(20, 0);
        LocalTime earliestCheckOutTime = LocalTime.of(12, 0);
        LocalTime latestCheckOutTime = LocalTime.of(14, 0);
        BigDecimal lateCheckoutFee = BigDecimal.valueOf(45.60);

        Address address = new Address("Zamza", "7 zamza avenue", null,
                State.VIC, "Melbourne", new Postcode("3000"));

        Hotel hotel = new Hotel("Zamza", address, 4, "zamzaohotel.com.au",
                earliestCheckInTime,
                latestCheckInTime,
                earliestCheckOutTime,
                latestCheckOutTime,
                lateCheckoutFee);

        Room room1 = new Room("Z1", RoomType.Economy, 4, BigDecimal.valueOf(35.12));
        Room room2 = new Room("Z2", RoomType.Economy, 5, BigDecimal.valueOf(115.35));
        Room room3 = new Room("Z3", RoomType.Luxury, 4, BigDecimal.valueOf(215.36));
        Room room4 = new Room("Z4", RoomType.Economy, 2, BigDecimal.valueOf(135.40));

        hotel.addRoom(room1);
        hotel.addRoom(room2);
        hotel.addRoom(room3);
        hotel.addRoom(room4);

        hotelRepository.save(hotel);
    }

    private void createHotel6() {
        LocalTime earliestCheckInTime = LocalTime.of(9, 0);
        LocalTime latestCheckInTime = LocalTime.of(20, 0);
        LocalTime earliestCheckOutTime = LocalTime.of(12, 0);
        LocalTime latestCheckOutTime = LocalTime.of(14, 0);
        BigDecimal lateCheckoutFee = BigDecimal.valueOf(45.60);

        Address address = new Address("Xavier Hotel", "7 xavier road", null,
                State.VIC, "Melbourne", new Postcode("3000"));

        Hotel hotel = new Hotel("Xavier Hotel", address, 4, "xavierhotel.com.au",
                earliestCheckInTime,
                latestCheckInTime,
                earliestCheckOutTime,
                latestCheckOutTime,
                lateCheckoutFee);

        Room room1 = new Room("X1", RoomType.Economy, 4, BigDecimal.valueOf(13.12));
        Room room2 = new Room("X2", RoomType.Economy, 5, BigDecimal.valueOf(94.35));
        Room room3 = new Room("X3", RoomType.Luxury, 4, BigDecimal.valueOf(193.16));
        Room room4 = new Room("X4", RoomType.Economy, 2, BigDecimal.valueOf(19.40));

        hotel.addRoom(room1);
        hotel.addRoom(room2);
        hotel.addRoom(room3);
        hotel.addRoom(room4);

        hotelRepository.save(hotel);
    }
}
