package com.demo.hotel;

import com.demo.domain.Hotel;
import com.demo.domain.Room;
import com.demo.persistance.HotelRepository;
import com.demo.persistance.RoomPredicates;
import com.demo.persistance.RoomRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class HotelSearchController {

    private HotelRepository hotelRepository;
    private RoomRepository roomRepository;

    private int pageSize;

    public HotelSearchController(HotelRepository hotelRepository,
                                 RoomRepository roomRepository,
                                 @Value("${spring.data.web.pageable.default-page-size}") int pageSize) {
        this.hotelRepository = hotelRepository;
        this.roomRepository = roomRepository;
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

    @GetMapping(value = "/hotel/{id}/rooms")
    public String getHotelRooms(@PathVariable("id") Long id, Pageable pageable, Model model) {
        Page<Room> availableRooms = roomRepository.findAll(RoomPredicates.availableRoom(id), pageable);
        model.addAttribute("rooms", availableRooms);
        return "/hotel/rooms";
    }






    // TODO: for testing
    @GetMapping(value = "/hotels")
    public String getHotels(Pageable pageable, Model model) {
        Page<Hotel> results = hotelRepository.findAll(pageable);
        model.addAttribute("results", results);
        return "/hotel/hotels";
    }
}
