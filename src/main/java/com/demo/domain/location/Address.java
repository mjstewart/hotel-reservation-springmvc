package com.demo.domain.location;

import com.demo.util.Utils;

import javax.persistence.*;
import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.Objects;

@Embeddable
public class Address {
    private String business;

    @Column(nullable = false)
    @NotEmpty(message = "required")
    @NotNull(message = "required")
    private String streetLine1;

    private String streetLine2;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private State state;

    @Column(nullable = false)
    private String suburb;

    @Column(nullable = false)
    @Embedded
    @Valid
    private Postcode postcode;

    public Address(String business, String streetLine1, String streetLine2, State state,
                   String suburb, Postcode postcode) {
        this.business = business;
        this.streetLine1 = streetLine1;
        this.streetLine2 = streetLine2;
        this.state = state;
        this.suburb = suburb;
        this.postcode = postcode;
    }

    public Address() {
    }

    public String getBusiness() {
        return business;
    }

    public void setBusiness(String business) {
        this.business = business;
    }

    public String getStreetLine1() {
        return streetLine1;
    }

    public void setStreetLine1(String streetLine1) {
        this.streetLine1 = streetLine1;
    }

    public String getStreetLine2() {
        return streetLine2;
    }

    public void setStreetLine2(String streetLine2) {
        this.streetLine2 = streetLine2;
    }

    public State getState() {
        return state;
    }

    public void setState(State state) {
        this.state = state;
    }

    public String getSuburb() {
        return Utils.capitalizeWords(suburb);
    }

    public void setSuburb(String suburb) {
        this.suburb = suburb;
    }

    public Postcode getPostcode() {
        return postcode;
    }

    public void setPostcode(Postcode postcode) {
        this.postcode = postcode;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Address address = (Address) o;
        return Objects.equals(business, address.business) &&
                Objects.equals(streetLine1, address.streetLine1) &&
                Objects.equals(streetLine2, address.streetLine2) &&
                state == address.state &&
                Objects.equals(suburb, address.suburb) &&
                Objects.equals(postcode, address.postcode);
    }

    @Override
    public int hashCode() {
        return Objects.hash(business, streetLine1, streetLine2, state, suburb, postcode);
    }

    @Override
    public String toString() {
        return "Address{" +
                "business='" + business + '\'' +
                ", streetLine1='" + streetLine1 + '\'' +
                ", streetLine2='" + streetLine2 + '\'' +
                ", state=" + state +
                ", suburb='" + suburb + '\'' +
                ", postcode=" + postcode +
                '}';
    }
}
