package com.demo.persistance;

import com.demo.domain.Hotel;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface HotelRepository extends CrudRepository<Hotel, Long> {
}
