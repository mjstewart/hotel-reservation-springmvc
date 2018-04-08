package com.demo.hotel;

import com.demo.TestHelpers;
import com.demo.domain.Hotel;
import com.demo.domain.location.Address;
import com.demo.domain.location.Postcode;
import com.demo.domain.location.State;
import com.demo.persistance.HotelRepository;
import org.hamcrest.FeatureMatcher;
import org.hamcrest.Matcher;
import org.hamcrest.Matchers;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.*;
import org.springframework.data.web.config.EnableSpringDataWebSupport;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;
import java.util.function.Function;

import static com.demo.TestHelpers.mappedAssertion;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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


    @Test
    public void getHotels_NoLocationQueryParams_ReturnsEmptyPageResults() throws Exception {
        FeatureMatcher<Page<Hotel>, Long> hasExpectedPageResult =
                mappedAssertion(Page::getTotalElements, Matchers.is(0L));

        mockMvc.perform(get("/hotel/search"))
                .andExpect(status().isOk())
                .andExpect(view().name("/hotel/hotels"))
                .andExpect(model().attribute("results", hasExpectedPageResult));

        verifyZeroInteractions(hotelRepository);
    }

    @Test
    public void getHotels_HotelsFound_AddsResultsToModel() throws Exception {
        Hotel hotel = new Hotel("Hotel Royal", new Address("Hotel Royal", "33 kent street", null,
                State.VIC, "Melbourne", new Postcode("3000")),
                5, "royal@hotel.com");
        List<Hotel> hotels = List.of(hotel);
        PageImpl<Hotel> results = new PageImpl<>(hotels, PageRequest.of(0, 20), hotels.size());

        when(hotelRepository.findAllByLocation(Mockito.eq("VIC"), Mockito.isNull(), Mockito.isNull(), any(Pageable.class)))
                .thenReturn(results);

        FeatureMatcher<Page<Hotel>, List<Hotel>> hasExpectedPageResult =
                mappedAssertion(Slice::getContent, Matchers.is(hotels));

        mockMvc.perform(get("/hotel/search?state=VIC"))
                .andExpect(status().isOk())
                .andExpect(view().name("/hotel/hotels"))
                .andExpect(model().attribute("results", hasExpectedPageResult));

        verify(hotelRepository, times(1))
                .findAllByLocation(Mockito.eq("VIC"), Mockito.isNull(), Mockito.isNull(), any(Pageable.class));
    }
}