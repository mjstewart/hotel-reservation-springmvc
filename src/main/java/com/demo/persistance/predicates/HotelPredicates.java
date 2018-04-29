package com.demo.persistance.predicates;

import com.demo.domain.QHotel;
import com.demo.domain.location.QAddress;
import com.querydsl.core.types.Predicate;

public final class HotelPredicates {

    private static final QHotel hotel = QHotel.hotel;

    private HotelPredicates() {
    }

    public static Predicate byLocation(String state, String suburb, String postcode) {
        QAddress address = hotel.address;

        /*
         * This is the same as doing a coalesce except coalesce cannot be used like you think it could be.
         *
         * if arg to coalesce is null, an exception is thrown. coalesce will only return its arg if the left hand side
         * 'this' instance is null so its the wrong way round for what we want to do.
         * address.state.stringValue().coalesce(state)
         */
        return new WhereClauseBuilder()
                .andNullable(state, () -> address.state.stringValue().equalsIgnoreCase(state))
                .andNullable(suburb, () -> address.suburb.equalsIgnoreCase(suburb))
                .andNullable(postcode, () -> address.postcode.value.eq(postcode));
    }
}
