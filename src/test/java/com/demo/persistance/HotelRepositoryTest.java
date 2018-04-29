package com.demo.persistance;

import com.demo.domain.Hotel;
import com.demo.domain.location.Address;
import com.demo.domain.location.Postcode;
import com.demo.domain.location.State;
import com.demo.persistance.predicates.HotelPredicates;
import org.hamcrest.Matchers;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

@RunWith(SpringRunner.class)
@DataJpaTest
public class HotelRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private HotelRepository hotelRepository;

    private List<String> pageToHotelNames(Page<Hotel> page) {
        List<String> results = new ArrayList<>();
        page.map(Hotel::getName).forEach(results::add);
        return results;
    }

    @Test
    public void findAllLocation_Empty_ReturnsAll() {
        entityManager.persist(new Hotel("Hotel Royal",
                new Address("Hotel Royal", "33 kent street", null,
                        State.VIC, "Melbourne", new Postcode("3000")),
                4, "royal@hotel.com"));

        entityManager.persist(new Hotel("Hotel Summer",
                new Address("Hotel Summer", "133 kennedy avenue", null,
                        State.VIC, "Summer West", new Postcode("3500")),
                4, "summer@hotel.com"));

        entityManager.persist(new Hotel("Hotel EastNight",
                new Address("Hotel EastNight", "54 east avenue", null,
                        State.VIC, "EastNight Summer", new Postcode("3511")),
                4, "eastnight@hotel.com"));

        entityManager.persist(new Hotel("Hotel Ryde",
                new Address("Hotel Ryde", "11A ryde avenue", null,
                        State.NSW, "North Ryde", new Postcode("2800")),
                4, "ryde@hotel.com"));

        // empty matches all
        Page<Hotel> pageResult = hotelRepository.findAllByLocation(null, null, null,
                PageRequest.of(0, 20));

        assertThat(pageResult.getTotalElements(), Matchers.is(4L));
        assertThat(pageToHotelNames(pageResult),
                Matchers.containsInAnyOrder("Hotel Royal", "Hotel Summer", "Hotel EastNight", "Hotel Ryde"));
    }

    @Test
    public void findAllLocation_ByState() {
        entityManager.persist(new Hotel("Hotel Royal",
                new Address("Hotel Royal", "33 kent street", null,
                        State.VIC, "Melbourne", new Postcode("3000")),
                4, "royal@hotel.com"));

        entityManager.persist(new Hotel("Hotel Summer",
                new Address("Hotel Summer", "133 kennedy avenue", null,
                        State.VIC, "Summer West", new Postcode("3500")),
                4, "summer@hotel.com"));

        entityManager.persist(new Hotel("Hotel EastNight",
                new Address("Hotel EastNight", "54 east avenue", null,
                        State.VIC, "EastNight Summer", new Postcode("3511")),
                4, "eastnight@hotel.com"));

        entityManager.persist(new Hotel("Hotel Ryde",
                new Address("Hotel Ryde", "11A ryde avenue", null,
                        State.NSW, "North Ryde", new Postcode("2800")),
                4, "ryde@hotel.com"));

        // match short name state exactly.
        Page<Hotel> pageResult = hotelRepository.findAllByLocation(State.VIC.name(), null, null,
                PageRequest.of(0, 20));

        assertThat(pageResult.getTotalElements(), Matchers.is(3L));
        assertThat(pageToHotelNames(pageResult), Matchers.containsInAnyOrder("Hotel Summer", "Hotel Royal", "Hotel EastNight"));

        // case should not matter
        pageResult = hotelRepository.findAllByLocation("viC", null, null,
                PageRequest.of(0, 20));

        assertThat(pageResult.getTotalElements(), Matchers.is(3L));
        assertThat(pageToHotelNames(pageResult), Matchers.containsInAnyOrder("Hotel Summer", "Hotel Royal", "Hotel EastNight"));

        // long name state should not match
        pageResult = hotelRepository.findAllByLocation("victoria", null, null,
                PageRequest.of(0, 20));

        assertThat(pageResult.getTotalElements(), Matchers.is(0L));

        // no match
        pageResult = hotelRepository.findAllByLocation("WA", null, null,
                PageRequest.of(0, 20));

        assertThat(pageResult.getTotalElements(), Matchers.is(0L));
    }


    @Test
    public void findAllLocation_BySuburb() {
        entityManager.persist(new Hotel("Hotel Royal",
                new Address("Hotel Royal", "33 kent street", null,
                        State.VIC, "Melbourne", new Postcode("3000")),
                4, "royal@hotel.com"));

        entityManager.persist(new Hotel("Hotel Summer",
                new Address("Hotel Summer", "133 kennedy avenue", null,
                        State.VIC, "Melbourne", new Postcode("3500")),
                4, "summer@hotel.com"));

        entityManager.persist(new Hotel("Hotel EastNight",
                new Address("Hotel EastNight", "54 east avenue", null,
                        State.VIC, "EastNight Summer", new Postcode("3511")),
                4, "eastnight@hotel.com"));

        entityManager.persist(new Hotel("Hotel Ryde",
                new Address("Hotel Ryde", "11A ryde avenue", null,
                        State.NSW, "North Ryde", new Postcode("2800")),
                4, "ryde@hotel.com"));

        // match short name state exactly.
        Page<Hotel> pageResult = hotelRepository.findAllByLocation(null, "Melbourne", null,
                PageRequest.of(0, 20));

        assertThat(pageResult.getTotalElements(), Matchers.is(2L));
        assertThat(pageToHotelNames(pageResult), Matchers.containsInAnyOrder("Hotel Royal", "Hotel Summer"));

        // case should not matter
        pageResult = hotelRepository.findAllByLocation(null, "meLbOuRnE", null,
                PageRequest.of(0, 20));

        assertThat(pageResult.getTotalElements(), Matchers.is(2L));
        assertThat(pageToHotelNames(pageResult), Matchers.containsInAnyOrder("Hotel Royal", "Hotel Summer"));

        // no match found
        pageResult = hotelRepository.findAllByLocation(null, "adelaide", null,
                PageRequest.of(0, 20));

        assertThat(pageResult.getTotalElements(), Matchers.is(0L));
    }

    @Test
    public void findAllLocation_ByPostcode() {
        entityManager.persist(new Hotel("Hotel Royal",
                new Address("Hotel Royal", "33 kent street", null,
                        State.VIC, "Melbourne", new Postcode("3000")),
                5, "royal@hotel.com"));

        entityManager.persist(new Hotel("Hotel Summer",
                new Address("Hotel Summer", "133 kennedy avenue", null,
                        State.VIC, "Melbourne", new Postcode("3500")),
                3, "summer@hotel.com"));

        entityManager.persist(new Hotel("Hotel EastNight",
                new Address("Hotel EastNight", "54 east avenue", null,
                        State.VIC, "EastNight Summer", new Postcode("3511")),
                1, "eastnight@hotel.com"));

        entityManager.persist(new Hotel("Hotel Ryde",
                new Address("Hotel Ryde", "11A ryde avenue", null,
                        State.NSW, "North Ryde", new Postcode("2800")),
                5, "ryde@hotel.com"));

        // exact match
        Page<Hotel> pageResult = hotelRepository.findAllByLocation(null, null, "3000",
                PageRequest.of(0, 20));

        assertThat(pageResult.getTotalElements(), Matchers.is(1L));
        assertThat(pageToHotelNames(pageResult), Matchers.containsInAnyOrder("Hotel Royal"));

        // no partial matches allowed
        pageResult = hotelRepository.findAllByLocation(null, null, "30",
                PageRequest.of(0, 20));

        assertThat(pageResult.getTotalElements(), Matchers.is(0L));

        // no match found
        pageResult = hotelRepository.findAllByLocation(null, null, "9999",
                PageRequest.of(0, 20));

        assertThat(pageResult.getTotalElements(), Matchers.is(0L));
    }

    @Test
    public void findAllLocation_AllParametersUsed_ExactMatchFound() {
        entityManager.persist(new Hotel("Hotel Royal",
                new Address("Hotel Royal", "33 kent street", null,
                        State.VIC, "Melbourne", new Postcode("3000")),
                5, "royal@hotel.com"));

        entityManager.persist(new Hotel("Hotel Summer",
                new Address("Hotel Summer", "133 kennedy avenue", null,
                        State.VIC, "Hotel Summer", new Postcode("3500")),
                3, "summer@hotel.com"));

        entityManager.persist(new Hotel("Hotel EastNight",
                new Address("Hotel EastNight", "54 east avenue", null,
                        State.VIC, "EastNight Summer", new Postcode("3511")),
                1, "eastnight@hotel.com"));

        entityManager.persist(new Hotel("Hotel Ryde",
                new Address("Hotel Ryde", "11A ryde avenue", null,
                        State.NSW, "North Ryde", new Postcode("2800")),
                5, "ryde@hotel.com"));

        // exact match
        Page<Hotel> pageResult = hotelRepository.findAllByLocation(State.VIC.name(), "EastNight Summer", "3511",
                PageRequest.of(0, 20));

        assertThat(pageResult.getTotalElements(), Matchers.is(1L));
        assertThat(pageToHotelNames(pageResult), Matchers.containsInAnyOrder("Hotel EastNight"));
    }

    @Test
    public void queryDslTest_Empty_ReturnsAll() {
        entityManager.persist(new Hotel("Hotel Royal",
                new Address("Hotel Royal", "33 kent street", null,
                        State.VIC, "Melbourne", new Postcode("3000")),
                4, "royal@hotel.com"));

        entityManager.persist(new Hotel("Hotel Summer",
                new Address("Hotel Summer", "133 kennedy avenue", null,
                        State.VIC, "Summer West", new Postcode("3500")),
                4, "summer@hotel.com"));

        entityManager.persist(new Hotel("Hotel EastNight",
                new Address("Hotel EastNight", "54 east avenue", null,
                        State.VIC, "EastNight Summer", new Postcode("3511")),
                4, "eastnight@hotel.com"));

        entityManager.persist(new Hotel("Hotel Ryde",
                new Address("Hotel Ryde", "11A ryde avenue", null,
                        State.NSW, "North Ryde", new Postcode("2800")),
                4, "ryde@hotel.com"));

        // empty matches all
        Page<Hotel> pageResult = hotelRepository.findAll(HotelPredicates.byLocation(null, null, null),
                PageRequest.of(0, 20));

        assertThat(pageResult.getTotalElements(), Matchers.is(4L));
        assertThat(pageToHotelNames(pageResult),
                Matchers.containsInAnyOrder("Hotel Royal", "Hotel Summer", "Hotel EastNight", "Hotel Ryde"));
    }

    @Test
    public void queryDslTest_BySuburb() {
        entityManager.persist(new Hotel("Hotel Royal",
                new Address("Hotel Royal", "33 kent street", null,
                        State.VIC, "Melbourne", new Postcode("3000")),
                4, "royal@hotel.com"));

        entityManager.persist(new Hotel("Hotel Summer",
                new Address("Hotel Summer", "133 kennedy avenue", null,
                        State.VIC, "Melbourne", new Postcode("3500")),
                4, "summer@hotel.com"));

        entityManager.persist(new Hotel("Hotel EastNight",
                new Address("Hotel EastNight", "54 east avenue", null,
                        State.VIC, "EastNight Summer", new Postcode("3511")),
                4, "eastnight@hotel.com"));

        entityManager.persist(new Hotel("Hotel Ryde",
                new Address("Hotel Ryde", "11A ryde avenue", null,
                        State.NSW, "North Ryde", new Postcode("2800")),
                4, "ryde@hotel.com"));

        // match short name state exactly.
        Page<Hotel> pageResult = hotelRepository.findAll(HotelPredicates.byLocation(null, "Melbourne", null),
                PageRequest.of(0, 20));
        assertThat(pageResult.getTotalElements(), Matchers.is(2L));
        assertThat(pageToHotelNames(pageResult), Matchers.containsInAnyOrder("Hotel Royal", "Hotel Summer"));

        // case should not matter
        pageResult = hotelRepository.findAll(HotelPredicates.byLocation(null, "meLbOuRnE", null),
                PageRequest.of(0, 20));
        assertThat(pageResult.getTotalElements(), Matchers.is(2L));
        assertThat(pageToHotelNames(pageResult), Matchers.containsInAnyOrder("Hotel Royal", "Hotel Summer"));

        // no match found
        pageResult = hotelRepository.findAll(HotelPredicates.byLocation(null, "adelaide", null),
                PageRequest.of(0, 20));
        assertThat(pageResult.getTotalElements(), Matchers.is(0L));
    }

    @Test
    public void queryDslTest_ByState() {
        entityManager.persist(new Hotel("Hotel Royal",
                new Address("Hotel Royal", "33 kent street", null,
                        State.VIC, "Melbourne", new Postcode("3000")),
                4, "royal@hotel.com"));

        entityManager.persist(new Hotel("Hotel Summer",
                new Address("Hotel Summer", "133 kennedy avenue", null,
                        State.VIC, "Summer West", new Postcode("3500")),
                4, "summer@hotel.com"));

        entityManager.persist(new Hotel("Hotel EastNight",
                new Address("Hotel EastNight", "54 east avenue", null,
                        State.VIC, "EastNight Summer", new Postcode("3511")),
                4, "eastnight@hotel.com"));

        entityManager.persist(new Hotel("Hotel Ryde",
                new Address("Hotel Ryde", "11A ryde avenue", null,
                        State.NSW, "North Ryde", new Postcode("2800")),
                4, "ryde@hotel.com"));

        // match short name state exactly.
        Page<Hotel> pageResult = hotelRepository.findAll(HotelPredicates.byLocation(State.VIC.name(), null, null),
                PageRequest.of(0, 20));
        assertThat(pageResult.getTotalElements(), Matchers.is(3L));
        assertThat(pageToHotelNames(pageResult), Matchers.containsInAnyOrder("Hotel Summer", "Hotel Royal", "Hotel EastNight"));


        // case should not matter
        pageResult = hotelRepository.findAll(HotelPredicates.byLocation("viC", null, null),
                PageRequest.of(0, 20));
        assertThat(pageResult.getTotalElements(), Matchers.is(3L));
        assertThat(pageToHotelNames(pageResult), Matchers.containsInAnyOrder("Hotel Summer", "Hotel Royal", "Hotel EastNight"));

        // long name state should not match
        pageResult = hotelRepository.findAll(HotelPredicates.byLocation("victoria", null, null),
                PageRequest.of(0, 20));
        assertThat(pageResult.getTotalElements(), Matchers.is(0L));

        // no match
        pageResult = hotelRepository.findAll(HotelPredicates.byLocation("WA", null, null),
                PageRequest.of(0, 20));
        assertThat(pageResult.getTotalElements(), Matchers.is(0L));
    }

    @Test
    public void queryDslTest_ByPostcode() {
        entityManager.persist(new Hotel("Hotel Royal",
                new Address("Hotel Royal", "33 kent street", null,
                        State.VIC, "Melbourne", new Postcode("3000")),
                5, "royal@hotel.com"));

        entityManager.persist(new Hotel("Hotel Summer",
                new Address("Hotel Summer", "133 kennedy avenue", null,
                        State.VIC, "Melbourne", new Postcode("3500")),
                3, "summer@hotel.com"));

        entityManager.persist(new Hotel("Hotel EastNight",
                new Address("Hotel EastNight", "54 east avenue", null,
                        State.VIC, "EastNight Summer", new Postcode("3511")),
                1, "eastnight@hotel.com"));

        entityManager.persist(new Hotel("Hotel Ryde",
                new Address("Hotel Ryde", "11A ryde avenue", null,
                        State.NSW, "North Ryde", new Postcode("2800")),
                5, "ryde@hotel.com"));

        // exact match
        Page<Hotel> pageResult = hotelRepository.findAll(HotelPredicates.byLocation(null, null, "3000"),
                PageRequest.of(0, 20));
        assertThat(pageResult.getTotalElements(), Matchers.is(1L));
        assertThat(pageToHotelNames(pageResult), Matchers.containsInAnyOrder("Hotel Royal"));


        // no partial matches allowed
        pageResult = hotelRepository.findAll(HotelPredicates.byLocation(null, null, "30"),
                PageRequest.of(0, 20));
        assertThat(pageResult.getTotalElements(), Matchers.is(0L));


        // no match found
        pageResult = hotelRepository.findAllByLocation(null, null, "9999",
                PageRequest.of(0, 20));

        assertThat(pageResult.getTotalElements(), Matchers.is(0L));
    }

    @Test
    public void queryDslTest_AllParametersUsed_ExactMatchFound() {
        entityManager.persist(new Hotel("Hotel Royal",
                new Address("Hotel Royal", "33 kent street", null,
                        State.VIC, "Melbourne", new Postcode("3000")),
                5, "royal@hotel.com"));

        entityManager.persist(new Hotel("Hotel Summer",
                new Address("Hotel Summer", "133 kennedy avenue", null,
                        State.VIC, "Hotel Summer", new Postcode("3500")),
                3, "summer@hotel.com"));

        entityManager.persist(new Hotel("Hotel EastNight",
                new Address("Hotel EastNight", "54 east avenue", null,
                        State.VIC, "EastNight Summer", new Postcode("3511")),
                1, "eastnight@hotel.com"));

        entityManager.persist(new Hotel("Hotel Ryde",
                new Address("Hotel Ryde", "11A ryde avenue", null,
                        State.NSW, "North Ryde", new Postcode("2800")),
                5, "ryde@hotel.com"));

        // exact match
        Page<Hotel> pageResult = hotelRepository.findAll(HotelPredicates.byLocation(State.VIC.name(), "EastNight Summer", "3511"),
                PageRequest.of(0, 20));
        assertThat(pageResult.getTotalElements(), Matchers.is(1L));
        assertThat(pageToHotelNames(pageResult), Matchers.containsInAnyOrder("Hotel EastNight"));
    }

}