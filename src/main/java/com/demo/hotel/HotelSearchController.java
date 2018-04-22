package com.demo.hotel;

import com.demo.domain.Hotel;
import com.demo.persistance.HotelRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class HotelSearchController {

    private HotelRepository hotelRepository;

    private int pageSize;

    public HotelSearchController(HotelRepository hotelRepository,
                                 @Value("${spring.data.web.pageable.default-page-size}") int pageSize) {
        this.hotelRepository = hotelRepository;
        this.pageSize = pageSize;
    }

    @GetMapping(value = "/hotel/search")
    public String getHotels(@RequestParam(value = "state", required = false) String state,
                            @RequestParam(value = "suburb", required = false) String suburb,
                            @RequestParam(value = "postcode", required = false) String postcode,
                            Pageable pageable, Model model) {

        if (state == null && suburb == null && postcode == null) {
            model.addAttribute("results", Page.empty());
            return "/hotel/hotels";
        }

        Page<Hotel> results = hotelRepository.findAllByLocation(state, suburb, postcode, pageable);
        model.addAttribute("results", results);
        return "/hotel/hotels";
    }

    @GetMapping(value = "/hotels")
    public String getHotels(Pageable pageable, Model model) {
        // TODO add test

        Page<Hotel> results = hotelRepository.findAll(pageable);
        model.addAttribute("results", results);
        return "/hotel/hotels";
    }


//    @GetMapping("/")
//    public String getAvailableRooms(Model model, Pageable pageable) {
//        System.out.println("getAvailableRooms");
//        System.out.println(pageable);
//
//
//        PageRequest pageRequest = PageRequest.of(0, 10, Sort.Direction.DESC, "costPerNight");
//
//
//
//        Page<Room> rooms =
//                roomRepository.findAllByReservationIsNull(pageable);
//
//
////        List<Room> rooms = roomRepository.getRoomsByReservationIsNull().stream()
////                .sorted(Comparator.comparing(Room::getCostPerNight).reversed())
////                .collect(Collectors.toList());
////
////        model.addAttribute("rooms", rooms);
//        return "hotel/rooms";
//    }
}
