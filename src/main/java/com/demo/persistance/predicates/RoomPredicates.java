package com.demo.persistance.predicates;

import com.demo.domain.QRoom;
import com.querydsl.core.types.Predicate;

public final class RoomPredicates {

    private static final QRoom room = QRoom.room;

    private RoomPredicates() {
    }

    /**
     * Gets all the available rooms in the hotel identified by the supplied {@code hotelId}.
     * An available room means it has no {@code Reservation} assigned to it.
     *
     * <p>The reason the query is done through the {@code Room} and not {@code Hotel} is to get a {@code Page} as
     * there could be many rooms. {@code Hotel} will get ALL the {@code Room}s unpaged.</p>
     *
     * @param hotelId The hotel id to get available rooms for.
     * @return The {@code Predicate}.
     */
    public static Predicate availableRoom(Long hotelId) {
        return room.hotel.id.eq(hotelId).and(room.reservation.isNull());
    }
}
