package com.demo.domain;

import com.demo.util.Utils;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.Objects;

@Entity
public class Guest {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Size(min = 2, max = 20)
    @NotNull(message = "required")
    @Column(nullable = false)
    private String firstName;

    @Size(min = 2, max = 20)
    @NotNull(message = "required")
    @Column(nullable = false)
    private String lastName;

    private boolean child;

    public Guest(String firstName, String lastName, boolean child) {
        setFirstName(firstName);
        setLastName(lastName);
        this.child = child;
    }

    public Guest() {
    }

    public String getFirstName() {
        return firstName;
    }

    /**
     * Converts to lowercase for consistent equality checks
     */
    public void setFirstName(String firstName) {
        this.firstName = firstName.toLowerCase();
    }

    public String getLastName() {
        return lastName;
    }

    /**
     * Converts to lowercase for consistent equality checks
     */
    public void setLastName(String lastName) {
        this.lastName = lastName.toLowerCase();
    }

    public boolean isChild() {
        return child;
    }

    public void setChild(boolean child) {
        this.child = child;
    }

    public String getFormattedFullName() {
        return Utils.capitalizeWords(firstName) + " " + Utils.capitalizeWords(lastName);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Guest guest = (Guest) o;
        return Objects.equals(firstName, guest.firstName) &&
                Objects.equals(lastName, guest.lastName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(firstName, lastName);
    }

    @Override
    public String toString() {
        return "Guest{" +
                "id=" + id +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", child=" + child +
                '}';
    }

}
