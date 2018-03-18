package com.demo.persistance;

import com.demo.domain.Room;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RoomRepository extends CrudRepository<Room, Long> {
    List<Room> getRoomsByReservationIsNull();
}
