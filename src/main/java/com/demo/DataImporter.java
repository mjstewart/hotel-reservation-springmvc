package com.demo;

import com.demo.domain.Extra;
import com.demo.domain.Hotel;
import com.demo.domain.Room;
import com.demo.domain.RoomType;
import com.demo.persistance.HotelRepository;
import com.demo.reservation.ExtraRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalTime;

@Component
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

            LocalTime earliestCheckInTime = LocalTime.of(9, 0);
            LocalTime latestCheckInTime = LocalTime.of(20, 0);
            LocalTime earliestCheckOutTime = LocalTime.of(12, 0);
            LocalTime latestCheckOutTime = LocalTime.of(14, 0);
            BigDecimal lateCheckoutFee = BigDecimal.valueOf(45.60);

            Hotel grandHotel = new Hotel("The Grand Hotel", "Melbourne", 4, "grandhotel.com.au",
                    earliestCheckInTime,
                    latestCheckInTime,
                    earliestCheckOutTime,
                    latestCheckOutTime,
                    lateCheckoutFee);

            Room room1 = new Room("Y7", RoomType.Economy, 1, BigDecimal.valueOf(65.12));
            Room room2 = new Room("Q18", RoomType.Business, 2, BigDecimal.valueOf(105.45));
            Room room3 = new Room("LL23", RoomType.Luxury, 4, BigDecimal.valueOf(205.66));
            Room room4 = new Room("P212A", RoomType.Economy, 2, BigDecimal.valueOf(35.40));

            grandHotel.addRoom(room1);
            grandHotel.addRoom(room2);
            grandHotel.addRoom(room3);
            grandHotel.addRoom(room4);

            hotelRepository.save(grandHotel);

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
        };
    }
}
