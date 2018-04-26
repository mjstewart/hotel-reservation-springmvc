package com.demo.persistance;

import com.demo.domain.Hotel;
import com.demo.domain.Room;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface HotelRepository extends PagingAndSortingRepository<Hotel, Long>, QuerydslPredicateExecutor<Hotel> {

    /**
     * See HotelPredicates.byLocation to see a query dsl alternative approach.
     *
     * Finds all hotels by state and suburb and postcode.
     *
     * <p>coalesce simplifies having multiple optional args.</p>
     *
     * <pre>
     *     1. Example: state = null
     *     where upper(h.address.state) = coalesce(null, upper(h.address.state))
     *     where upper(h.address.state) = upper(h.address.state) // -> row will match itself..
     *
     *     2. Example: state != null
     *     where upper(h.address.state) = coalesce(VIC, upper(h.address.state))
     *     where upper(h.address.state) = VIC // the state will now be used in the query.
     * </pre>
     */
    @Query("select h from Hotel h " +
            "where upper(h.address.state) = coalesce(upper(:state), upper(h.address.state)) " +
            "and upper(h.address.suburb) = coalesce(upper(:suburb), upper(h.address.suburb)) " +
            "and h.address.postcode.value = coalesce(:postcode, h.address.postcode.value)"
    )
    Page<Hotel> findAllByLocation(@Param("state") String state,
                                  @Param("suburb") String suburb,
                                  @Param("postcode") String postcode,
                                  Pageable pageable);
}
