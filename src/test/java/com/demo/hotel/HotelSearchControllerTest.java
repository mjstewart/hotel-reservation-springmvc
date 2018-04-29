package com.demo.hotel;

import com.demo.domain.Hotel;
import com.demo.domain.Room;
import com.demo.domain.location.Address;
import com.demo.domain.location.Postcode;
import com.demo.domain.location.State;
import com.demo.persistance.HotelRepository;
import com.demo.persistance.predicates.RoomPredicates;
import com.demo.persistance.RoomRepository;
import org.hamcrest.FeatureMatcher;
import org.hamcrest.Matchers;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.*;
import org.springframework.data.web.config.EnableSpringDataWebSupport;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;

import static com.demo.TestHelpers.mappedAssertion;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@RunWith(SpringRunner.class)
@WebMvcTest(HotelSearchController.class)
@EnableSpringDataWebSupport
@ActiveProfiles("test")
public class HotelSearchControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private HotelRepository hotelRepository;

    @MockBean
    private RoomRepository roomRepository;

    /**
     * No search results should be returned when no location query parameters are provided.
     */
    @Test
    public void getHotels_NoLocationQueryParams_ReturnsEmptyPageResults() throws Exception {
        FeatureMatcher<Page<Hotel>, Long> hasExpectedPageResult =
                mappedAssertion(Page::getTotalElements, Matchers.is(0L));

        mockMvc.perform(get("/hotel/search"))
                .andExpect(status().isOk())
                .andExpect(view().name("/hotel/hotels"))
                .andExpect(model().attribute("hotels", hasExpectedPageResult));

        verify(hotelRepository, times(1))
                .findAllByLocation(isNull(), isNull(), isNull(), any(Pageable.class));
    }

    /**
     * When the query string contains a valid location but no hotels are found, the result should contain 0 hotels.
     */
    @Test
    public void getHotels_NoHotelsFound_AddsResultsToModel() throws Exception {
        List<Hotel> hotels = List.of();
        PageImpl<Hotel> results = new PageImpl<>(hotels, PageRequest.of(0, 20), hotels.size());

        when(hotelRepository.findAllByLocation(eq("WA"), isNull(), eq("4000"), any(Pageable.class)))
                .thenReturn(results);

        // sanity check to ensure the returned hotels from repository appears in the page content.
        FeatureMatcher<Page<Hotel>, List<Hotel>> hasExpectedPageResult =
                mappedAssertion(Slice::getContent, Matchers.is(hotels));

        mockMvc.perform(get("/hotel/search?state=WA&postcode=4000"))
                .andExpect(status().isOk())
                .andDo(print())
                .andExpect(view().name("/hotel/hotels"))
                .andExpect(model().attribute("hotels", hasExpectedPageResult));

        verify(hotelRepository, times(1))
                .findAllByLocation(eq("WA"), isNull(), eq("4000"), any(Pageable.class));
    }

    /**
     * When the query string contains a valid location, it should return the matching hotels.
     */
    @Test
    public void getHotels_HotelsFound_AddsResultsToModel() throws Exception {
        Hotel hotel = new Hotel("Hotel Royal", new Address("Hotel Royal", "33 kent street", null,
                State.VIC, "Melbourne", new Postcode("3000")),
                5, "royal@hotel.com");

        // Calling findAllByLocation will return the only hotel given the state matches.
        List<Hotel> hotels = List.of(hotel);
        PageImpl<Hotel> results = new PageImpl<>(hotels, PageRequest.of(0, 20), hotels.size());
        when(hotelRepository.findAllByLocation(eq("VIC"), isNull(), isNull(), any(Pageable.class)))
                .thenReturn(results);

        // sanity check to ensure the matched hotel appears in the page content.
        FeatureMatcher<Page<Hotel>, List<Hotel>> hasExpectedPageResult =
                mappedAssertion(Slice::getContent, Matchers.is(hotels));

        mockMvc.perform(get("/hotel/search?state=VIC"))
                .andExpect(status().isOk())
                .andExpect(view().name("/hotel/hotels"))
                .andExpect(model().attribute("hotels", hasExpectedPageResult));

        verify(hotelRepository, times(1))
                .findAllByLocation(eq("VIC"), isNull(), isNull(), any(Pageable.class));
    }

    /**
     * 400 bad request when the hotel id is non numeric.
     */
    @Test
    public void getAvailableHotelRooms_InvalidHotelId_BadRequest() throws Exception {
        mockMvc.perform(get("/hotel/abc/rooms"))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void getAvailableHotelRooms_HotelIdNotFound_Throws404() throws Exception {
        long hotelId = 4;
        mockMvc.perform(get(String.format("/hotel/%d/rooms", hotelId)))
                .andExpect(status().isNotFound());
    }

    /**
     * Note: The model must contain the hotel so the UI can display detailed information.
     */
    @Test
    public void getAvailableHotelRooms_HotelHasAvailableRooms() throws Exception {
        FeatureMatcher<Page<Hotel>, Long> hasExpectedPageResult =
                mappedAssertion(Page::getTotalElements, Matchers.is(1L));

        // Dummy hotel to return in the mock.
        Address address = new Address("Xavier Hotel", "100 smith road", "",
                State.QLD, "Brisbane", new Postcode("4000"));
        Hotel hotel = new Hotel("Xavier Hotel", address, 4, "xavier@hotel.com");
        hotel.setId(3L);

        // Rather than recreate a new hotel room, setting total elements to 1 will achieve the same thing for testing.
        PageImpl<Room> page = new PageImpl<>(List.of(), PageRequest.of(0, 20), 1);
        when(roomRepository.findAll(RoomPredicates.availableRoom(hotel.getId()), PageRequest.of(0, 20))).thenReturn(page);

        when(hotelRepository.findById(hotel.getId())).thenReturn(Optional.of(hotel));

        mockMvc.perform(get(String.format("/hotel/%d/rooms", hotel.getId())))
                .andExpect(status().isOk())
                .andExpect(view().name("/hotel/rooms"))
                .andExpect(model().attribute("hotel", Matchers.isA(Hotel.class)))
                .andExpect(model().attribute("rooms", hasExpectedPageResult));

        verify(roomRepository, times(1))
                .findAll(eq(RoomPredicates.availableRoom(hotel.getId())), any(Pageable.class));

        verify(hotelRepository, times(1)).findById(eq(hotel.getId()));
    }
}