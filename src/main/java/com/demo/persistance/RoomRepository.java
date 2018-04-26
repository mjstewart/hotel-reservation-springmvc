package com.demo.persistance;

import com.demo.domain.Room;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RoomRepository extends PagingAndSortingRepository<Room, Long>, QuerydslPredicateExecutor<Room> {

}
