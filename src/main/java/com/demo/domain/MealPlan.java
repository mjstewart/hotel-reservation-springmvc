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
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private UUID mealPlanId = UUID.randomUUID();

    @OneToOne
    private Guest guest;

    @OneToOne
    private Reservation reservation;

    // No CascadeType as Extra already has an id associated to it.
    @ManyToMany
    private List<Extra> foodExtras = new ArrayList<>();

    @ElementCollection
    private List<DietaryRequirement> dietaryRequirements = new ArrayList<>();

    public MealPlan() {
    }

    public MealPlan(Guest guest, Reservation reservation, List<Extra> foodExtras,
                    List<DietaryRequirement> dietaryRequirements) {
        this.guest = guest;
        this.reservation = reservation;
        this.foodExtras = foodExtras;
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
     * Sum result of multiplying each food extra by total nights while applying any child discounts.
     *
     * <p>The discount must be applied AFTER multiplying the individual extras price by total nights, not
     * to the final aggregated sum.</p>
     *
     * @return The sum of the total meal plan costs
     */
    public BigDecimal getTotalMealPlanCost() {
        BigDecimal totalNights = new BigDecimal(reservation.getDates().totalNights());

        return foodExtras.stream()
                .map(extra -> applyDiscounts(extra, totalNights))
                .reduce(BigDecimal.ZERO, BigDecimal::add, BigDecimal::add);
    }

    public boolean hasFoodExtras() {
        return !foodExtras.isEmpty();
    }

    public boolean hasDietRequirements() {
        return !dietaryRequirements.isEmpty();
    }


    /**
     * The total value of the {@code Extra}s per night cost multiplied by total nights.
     *
     * Business rule - Children receive FoodExtras.CHILD_DISCOUNT_PERCENT off EACH meal.
     */
    public BigDecimal applyDiscounts(Extra extra, BigDecimal totalNights) {
        BigDecimal total = extra.getPerNightPrice().multiply(totalNights);
        if (guest.isChild()) {
            BigDecimal discount = total.multiply(BigDecimal.valueOf(Reservation.CHILD_DISCOUNT_PERCENT));
            return total.subtract(discount);
        }

        return total;
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
