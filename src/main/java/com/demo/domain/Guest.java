package com.demo.domain;

import com.demo.util.Utils;

import javax.persistence.*;
import javax.validation.constraints.Size;
import java.util.Comparator;
import java.util.Objects;
import java.util.UUID;

@Entity
public class Guest {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private UUID guestId = UUID.randomUUID();

    @Size(min = 2, max = 20)
    private String firstName;

    @Size(min = 2, max = 20)
    private String lastName;

    private String email;

    @Column(nullable = false)
    private String mobile;

    private boolean child;

    private boolean primaryContact;

    public Guest(String firstName, String lastName, String email, String mobile, boolean child, boolean primaryContact) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.mobile = mobile;
        this.child = child;
        this.primaryContact = primaryContact;
    }

    public Guest() {
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public boolean isChild() {
        return child;
    }

    public void setChild(boolean child) {
        this.child = child;
    }

    public boolean isPrimaryContact() {
        return primaryContact;
    }

    public void setPrimaryContact(boolean primaryContact) {
        this.primaryContact = primaryContact;
    }

    public UUID getGuestId() {
        return guestId;
    }

    public String getFormattedFullName() {
        return Utils.capitalize(firstName) + " " + Utils.capitalize(lastName);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Guest guest = (Guest) o;
        return Objects.equals(guestId, guest.guestId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(guestId);
    }

    @Override
    public String toString() {
        return "Guest{" +
                "id=" + id +
                ", guestId=" + guestId +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", email='" + email + '\'' +
                ", mobile='" + mobile + '\'' +
                ", child=" + child +
                ", primaryContact=" + primaryContact +
                '}';
    }

    public static Comparator<Guest> comparator() {
        return Comparator.comparing(Guest::isPrimaryContact).reversed()
                .thenComparing(Guest::getFirstName)
                .thenComparing(Guest::getLastName);
    }
}
