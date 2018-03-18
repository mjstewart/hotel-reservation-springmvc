package com.demo.domain;

import javafx.util.Pair;
import org.springframework.format.annotation.DateTimeFormat;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Entity
public class Reservation {
    public static final double TAX_AMOUNT = 0.10;
    public static final double CHILD_DISCOUNT_PERCENT = 0.60;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private UUID reservationId = UUID.randomUUID();

    @OneToOne(mappedBy = "reservation")
    private Room room;

    @ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(
            name = "reservation_guests",
            joinColumns = @JoinColumn(name = "reservation_id", referencedColumnName = "id"),
            inverseJoinColumns = @JoinColumn(name = "guest_id", referencedColumnName = "id")
    )
    private Set<Guest> guests = new HashSet<>();

    @Embedded
    private ReservationDates reservationDates;

    // no CascadeType since Extra already has an id associated to it.
    @ManyToMany
    @JoinTable(
            name = "reservation_general_extras",
            joinColumns = @JoinColumn(name = "reservation_id", referencedColumnName = "id"),
            inverseJoinColumns = @JoinColumn(name = "general_extra_id", referencedColumnName = "id")
    )
    private Set<Extra> generalExtras = new HashSet<>();

    @OneToMany(cascade = CascadeType.ALL)
    private Set<MealPlan> mealPlans = new HashSet<>();

    @OneToMany(cascade = CascadeType.ALL)
    public Set<Payment> attemptedPayments = new HashSet<>();

    public LocalDateTime createdTime;

    /**
     * @return The time this {@code Reservation} was successfully paid for and persisted.
     */
    public LocalDateTime getCreatedTime() {
        return createdTime;
    }

    public void setCreatedTimeNow() {
        createdTime = LocalDateTime.now();
    }

    public Reservation() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Room getRoom() {
        return room;
    }

    public void setRoom(Room room) {
        this.room = room;
    }

    public Set<Guest> getGuests() {
        return guests;
    }

    public void setGuests(Set<Guest> guests) {
        this.guests = guests;
    }

    public UUID getReservationId() {
        return reservationId;
    }

    public Set<Extra> getGeneralExtras() {
        return generalExtras;
    }

    public void setGeneralExtras(Set<Extra> generalExtras) {
        this.generalExtras = generalExtras;
    }

    public Set<MealPlan> getMealPlans() {
        return mealPlans;
    }

    public void setMealPlans(Set<MealPlan> mealPlans) {
        this.mealPlans = mealPlans;
    }

    public void resetMealPlans() {
        mealPlans = new HashSet<>();
    }

    public void resetExtras() {
        generalExtras = new HashSet<>();
    }

    public ReservationDates getReservationDates() {
        return reservationDates;
    }

    public void setReservationDates(ReservationDates reservationDates) {
        this.reservationDates = reservationDates;
    }

//    /**
//     * Calculates the chargeable late fee price only if the user has selected the late checkout option.
//     */
//    public BigDecimal getChargeableLateCheckoutFee() {
//        return lateCheckout ? getLateCheckoutFee() : BigDecimal.ZERO;
//    }
//
//    /**
//     * The late checkout fee depending on the type of room.
//     * For the actual chargeable fee, use {@link #getChargeableLateCheckoutFee()}
//     */
//    public BigDecimal getLateCheckoutFee() {
//        switch (room.getRoomType()) {
//            case Luxury:
//            case Business:
//                return BigDecimal.ZERO;
//            default:
//                return room.getHotel().getLateCheckoutFee();
//        }
//    }
//
//
//
//
//    /**
//     * @return Total nights * per night cost
//     */
//    public BigDecimal getTotalRoomCost() {
//        return room.getCostPerNight().multiply(new BigDecimal(getTotalNights()));
//    }
//
//    /**
//     * @return {@link #getTotalRoomCost} + {@link #getChargeableLateCheckoutFee}
//     */
//    public BigDecimal getTotalRoomCostWithLateCheckoutFee() {
//        return getTotalRoomCost().add(getChargeableLateCheckoutFee());
//    }
//
//    /**
//     * Total general extras cost based on total nights stay.
//     */
//    public BigDecimal getTotalGeneralExtrasCost() {
//        long totalNights = getTotalNights();
//        return generalExtras.stream().reduce(
//                BigDecimal.ZERO,
//                (acc, next) -> acc.add(next.getTotalPrice(totalNights))
//                , BigDecimal::add
//        );
//    }
//
//    /**
//     * @return The supplied {@code Extra} price multiplied by total nights stayed.
//     */
//    public BigDecimal getTotalGeneralExtraPrice(Extra extra) {
//        if (extra.getCategory() != Extra.Category.General) {
//            throw new IllegalArgumentException("Extra category is not General");
//        }
//        return extra.getTotalPrice(getTotalNights());
//    }
//
//    /**
//     * @return Total cost of all guests meal plans
//     */
//    public BigDecimal getTotalMealPlansCost() {
//        return mealPlans.stream()
//                .map(MealPlan::getTotalMealPlanCost)
//                .reduce(BigDecimal.ZERO, BigDecimal::add, BigDecimal::add);
//    }
//
//    /**
//     * Total cost including everything!
//     */
//    public BigDecimal getTotalCostExcludingTax() {
//        return getTotalRoomCostWithLateCheckoutFee()
//                .add(getTotalGeneralExtrasCost())
//                .add(getTotalMealPlansCost());
//    }
//
//    public BigDecimal getTaxableAmount() {
//        return getTotalCostExcludingTax().multiply(BigDecimal.valueOf(TAX_AMOUNT));
//    }
//
//    public BigDecimal getTotalCostIncludingTax() {
//        return getTotalCostExcludingTax().add(getTaxableAmount());
//    }
//
//    /**
//     * Useful for UI template rendering.
//     *
//     * @return List of sorted {@code MealPlan}s according to the {@code Guest} comparator.
//     */
//    public List<MealPlan> getSortedMealPlansByGuest() {
//        return mealPlans.stream()
//                .sorted(Comparator.comparing(MealPlan::getGuest, Guest.comparator()))
//                .collect(Collectors.toList());
//    }
//
//    /**
//     *
//     * @return List of sorted {@code Guest}s.
//     */
//    public List<Guest> getSortedGuests() {
//        return guests.stream()
//                .sorted(Guest.comparator())
//                .collect(Collectors.toList());
//    }
//
//    /**
//     * Used by UI templates to decide when to include in the payment summary since
//     * only food extras have a payment associated with them unlike dietary requirements.
//     *
//     * @return {@code true} if there exists at least 1 meal plan which has a food extra such as
//     * breakfast, lunch or dinner.
//     */
//    public boolean hasMealPlansWithFoodExtras() {
//        return mealPlans.stream()
//                .anyMatch(MealPlan::hasFoodExtras);
//    }
//
//    /**
//     *
//     * @return {@code true} if the {@code MealPlan} has a food plan or diet requirement.
//     */
//    public boolean hasMealPlans() {
//        return mealPlans.stream()
//                .anyMatch(mealPlan -> mealPlan.hasFoodExtras() || mealPlan.hasDietRequirements());
//    }
//
//    /**
//     * A history of {@code Payment}s are kept since its possible a payment may be declined and reattempted which
//     * we capture for potential payment investigations.
//     *
//     * @param payment
//     */
//    public void addAttemptedPayment(Payment payment) {
//        attemptedPayments.add(payment);
//    }
//
//    public List<Guest> getPrimaryContacts() {
//        return guests.stream()
//                .filter(Guest::isPrimaryContact)
//                .collect(Collectors.toList());
//    }
//
//    @Override
//    public boolean equals(Object o) {
//        if (this == o) return true;
//        if (o == null || getClass() != o.getClass()) return false;
//        Reservation that = (Reservation) o;
//        return Objects.equals(reservationId, that.reservationId);
//    }
//
//    @Override
//    public int hashCode() {
//        return Objects.hash(reservationId);
//    }
//
//    @Override
//    public String toString() {
//        return "Reservation{" +
//                "reservationId=" + reservationId +
//                '}';
//    }

//    /**
//     * Extracted for reuse in ajax reservationDates validation.
//     *
//     * @return Pair with key = field, value = error message or null if date span is valid.
//     */
//    public Pair<String, String> validateDates(LocalDate now) {
//        if (checkInDate == null) {
//            return new Pair<>("checkInDate", "Missing check in date");
//        } else if (checkOutDate == null) {
//            return new Pair<>("checkOutDate", "Missing check out date");
//        } else if (checkInDate.isBefore(now)) {
//            return new Pair<>("checkInDate", "Check in date must be in the future");
//        } else if (checkOutDate.isBefore(checkInDate)) {
//            return new Pair<>("checkOutDate", "Check out date must be after check in date");
//        } else if (getTotalNights() < 1) {
//            return new Pair<>("checkOutDate", "Reservation must be for at least 1 night");
//        }
//        return null;
//    }
}
