package com.demo.domain;

import org.junit.Test;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.*;

/**
 * Testes unitários para {@link Guest}.
 *
 * Cobre conversão de nomes, formatação, ordenação por comparator e igualdade.
 */
public class GuestTest {

    // ==================== Nome (case conversion) ====================

    /**
     * O primeiro nome deve ser convertido para lowercase ao ser definido.
     */
    @Test
    public void setFirstName_ConvertsToLowerCase() {
        Guest guest = new Guest("JOHN", "smith", false);
        assertThat(guest.getFirstName()).isEqualTo("john");
    }

    /**
     * O sobrenome deve ser convertido para lowercase ao ser definido.
     */
    @Test
    public void setLastName_ConvertsToLowerCase() {
        Guest guest = new Guest("john", "SMITH", false);
        assertThat(guest.getLastName()).isEqualTo("smith");
    }

    /**
     * Nomes com casing misto devem ser normalizados para lowercase.
     */
    @Test
    public void setNames_MixedCase_NormalizedToLowerCase() {
        Guest guest = new Guest("JoHn", "SmItH", false);
        assertThat(guest.getFirstName()).isEqualTo("john");
        assertThat(guest.getLastName()).isEqualTo("smith");
    }

    // ==================== getFormattedFullName() ====================

    /**
     * O nome formatado deve capitalizar a primeira letra de cada palavra.
     */
    @Test
    public void getFormattedFullName_CapitalizesCorrectly() {
        Guest guest = new Guest("john", "smith", false);
        assertThat(guest.getFormattedFullName()).isEqualTo("John Smith");
    }

    /**
     * Mesmo com entrada em uppercase, o nome formatado deve ter casing correto.
     */
    @Test
    public void getFormattedFullName_FromUpperCase_CapitalizesCorrectly() {
        Guest guest = new Guest("MARIE", "JONES", true);
        assertThat(guest.getFormattedFullName()).isEqualTo("Marie Jones");
    }

    // ==================== Comparator ====================

    /**
     * O comparator deve ordenar adultos antes de crianças.
     */
    @Test
    public void comparator_OrdersAdultsFirst() {
        Guest child = new Guest("alice", "smith", true);
        Guest adult = new Guest("bob", "smith", false);

        List<Guest> guests = Arrays.asList(child, adult);
        List<Guest> sorted = guests.stream()
                .sorted(Guest.comparator())
                .collect(Collectors.toList());

        assertThat(sorted).containsExactly(adult, child);
    }

    /**
     * Entre adultos, o comparator deve desempatar por firstName e depois lastName.
     */
    @Test
    public void comparator_BreaksTiesByFirstNameThenLastName() {
        Guest guestA = new Guest("bob", "alpha", false);
        Guest guestB = new Guest("alice", "zeta", false);
        Guest guestC = new Guest("bob", "beta", false);

        List<Guest> guests = Arrays.asList(guestA, guestB, guestC);
        List<Guest> sorted = guests.stream()
                .sorted(Guest.comparator())
                .collect(Collectors.toList());

        // alice primeiro, depois bob-alpha, depois bob-beta
        assertThat(sorted).containsExactly(guestB, guestA, guestC);
    }

    /**
     * Teste completo do comparator com adultos e crianças misturados.
     */
    @Test
    public void comparator_FullOrderingWithMixedGuestTypes() {
        Guest childA = new Guest("zoe", "smith", true);
        Guest childB = new Guest("amy", "jones", true);
        Guest adultA = new Guest("charlie", "brown", false);
        Guest adultB = new Guest("alice", "white", false);

        List<Guest> guests = Arrays.asList(childA, adultA, childB, adultB);
        List<Guest> sorted = guests.stream()
                .sorted(Guest.comparator())
                .collect(Collectors.toList());

        // Adultos primeiro (alice, charlie), depois crianças (amy, zoe)
        assertThat(sorted).containsExactly(adultB, adultA, childB, childA);
    }

    // ==================== equals / hashCode ====================

    /**
     * Dois hóspedes com mesmo nome e status de criança devem ser iguais.
     */
    @Test
    public void equals_SameNameAndChildStatus_AreEqual() {
        Guest guest1 = new Guest("john", "smith", false);
        Guest guest2 = new Guest("john", "smith", false);

        assertThat(guest1).isEqualTo(guest2);
        assertThat(guest1.hashCode()).isEqualTo(guest2.hashCode());
    }

    /**
     * Dois hóspedes com nomes diferentes não devem ser iguais.
     */
    @Test
    public void equals_DifferentName_NotEqual() {
        Guest guest1 = new Guest("john", "smith", false);
        Guest guest2 = new Guest("jane", "smith", false);

        assertThat(guest1).isNotEqualTo(guest2);
    }

    /**
     * Mesmo nome mas status de criança diferente não devem ser iguais.
     */
    @Test
    public void equals_SameNameDifferentChildStatus_NotEqual() {
        Guest adult = new Guest("john", "smith", false);
        Guest child = new Guest("john", "smith", true);

        assertThat(adult).isNotEqualTo(child);
    }

    /**
     * A igualdade é case-insensitive pois os nomes são convertidos para lowercase.
     */
    @Test
    public void equals_CaseInsensitive() {
        Guest guest1 = new Guest("JOHN", "SMITH", false);
        Guest guest2 = new Guest("john", "smith", false);

        assertThat(guest1).isEqualTo(guest2);
    }

    // ==================== isChild ====================

    /**
     * Um hóspede criado como child deve retornar true para isChild().
     */
    @Test
    public void isChild_ChildGuest_ReturnsTrue() {
        Guest child = new Guest("billy", "kid", true);
        assertThat(child.isChild()).isTrue();
    }

    /**
     * Um hóspede adulto deve retornar false para isChild().
     */
    @Test
    public void isChild_AdultGuest_ReturnsFalse() {
        Guest adult = new Guest("john", "smith", false);
        assertThat(adult.isChild()).isFalse();
    }
}
