package com.demo.domain;

import com.demo.util.Utils;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.Comparator;
import java.util.Objects;
import java.util.UUID;

@Entity
public class Guest {
    // Allows UI to delete guest by its temp id rather than send full name details which equals/hashCode use.
    @Transient
    private UUID tempId = UUID.randomUUID();

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

    public UUID getTempId() {
        return tempId;
    }

    /**
     * @return A {@code Comparator} that orders adults first then by firstName then lastName.
     */
    public static Comparator<Guest> comparator() {
        return Comparator.comparing(Guest::isChild, Boolean::compareTo)
                .thenComparing(Guest::getFirstName)
                .thenComparing(Guest::getLastName);
    }

    /**
     * Cant use a UUID because that does not prevent a guest with the same name being added.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Guest guest = (Guest) o;
        return child == guest.child &&
                Objects.equals(firstName, guest.firstName) &&
                Objects.equals(lastName, guest.lastName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(firstName, lastName, child);
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
