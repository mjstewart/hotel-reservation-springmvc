package com.demo.domain;

import com.demo.util.Utils;

import javax.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Entity
public class MealPlan implements Serializable {
    public static final double CHILD_DISCOUNT_PERCENT = 0.60;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    // Simplifies equal/hashCode
    private UUID mealPlanId = UUID.randomUUID();

    @OneToOne
    private Guest guest;

    @OneToOne
    private Reservation reservation;

    @ManyToMany
    private List<Extra> foodExtras;

    @ElementCollection
    private List<DietaryRequirement> dietaryRequirements;

    public MealPlan() {
    }

    public MealPlan(Guest guest, Reservation reservation) {
        this(guest, reservation, new ArrayList<>(), new ArrayList<>());
    }

    public MealPlan(Guest guest, Reservation reservation, List<Extra> foodExtras,
                    List<DietaryRequirement> dietaryRequirements) {
        this.guest = guest;
        this.reservation = reservation;
        setFoodExtras(foodExtras);
        this.dietaryRequirements = dietaryRequirements;
    }

    public UUID getMealPlanId() {
        return mealPlanId;
    }

    public Long getId() {
        return id;
    }

    public Guest getGuest() {
        return guest;
    }

    public void setGuest(Guest guest) {
        this.guest = guest;
    }

    public Reservation getReservation() {
        return reservation;
    }

    public void setReservation(Reservation reservation) {
        this.reservation = reservation;
    }

    public List<Extra> getFoodExtras() {
        return foodExtras;
    }

    public void setFoodExtras(List<Extra> foodExtras) {
        boolean containsInvalidCategories = foodExtras.stream()
                .anyMatch(e -> e.getCategory() != Extra.Category.Food);
        if (containsInvalidCategories) {
            throw new IllegalArgumentException("Contains invalid categories that are not Extra.Category.Food");
        }
        this.foodExtras = foodExtras;
    }

    public List<DietaryRequirement> getDietaryRequirements() {
        return dietaryRequirements;
    }

    public void setDietaryRequirements(List<DietaryRequirement> dietaryRequirements) {
        this.dietaryRequirements = dietaryRequirements;
    }

    public boolean isEmpty() {
        return foodExtras.isEmpty() && dietaryRequirements.isEmpty();
    }

    /**
     * @return {@code true} if {@code DietaryRequirement.Vegan} and {@code DietaryRequirement.Vegetarian} exist at the
     * same time.
     */
    public boolean hasInvalidDietaryRequirements() {
        return dietaryRequirements.stream()
                .filter(diet -> diet == DietaryRequirement.Vegan || diet == DietaryRequirement.Vegetarian)
                .count() == 2;
    }

    /**
     * @return The sum of calculating the total extra cost including total nights and child discounts for each extra.
     */
    public BigDecimal getTotalMealPlanCost() {
        return foodExtras.stream()
                .map(this::calculateExtraCost)
                .reduce(BigDecimal.ZERO, BigDecimal::add, BigDecimal::add);
    }

    /**
     * @param foodExtra The food extra
     * @return The food extra cost multiplied by total nights with child discounts applied if applicable.
     * @throws IllegalArgumentException if Extra is not {@code Extra.Category.Food}
     */
    public BigDecimal calculateExtraCost(Extra foodExtra) throws IllegalArgumentException {
        if (foodExtra.getCategory() != Extra.Category.Food) {
            throw new IllegalArgumentException("Extra is not of type Extra.Category.Food");
        }

        BigDecimal total = foodExtra.getTotalPrice(reservation.getDates().totalNights());
        if (guest.isChild()) {
            BigDecimal discount = total.multiply(BigDecimal.valueOf(CHILD_DISCOUNT_PERCENT));
            return total.subtract(discount);
        }
        return total;
    }

    public boolean hasFoodExtras() {
        return !foodExtras.isEmpty();
    }

    public boolean hasDietRequirements() {
        return !dietaryRequirements.isEmpty();
    }

    public String toFoodExtraCsv() {
        return Utils.toCsv(foodExtras, Extra::getDescription);
    }

    public String toDietRequirementsCsv() {
        return Utils.toCsv(dietaryRequirements, DietaryRequirement::getDescription);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MealPlan mealPlan = (MealPlan) o;
        return Objects.equals(mealPlanId, mealPlan.mealPlanId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(mealPlanId);
    }
}
