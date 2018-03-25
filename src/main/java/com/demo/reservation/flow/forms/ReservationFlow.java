package com.demo.reservation.flow.forms;

import com.demo.domain.Reservation;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.List;

public class ReservationFlow {
    @Valid
    private Reservation reservation = new Reservation();

    private int step = 0;
    private List<StepDescription> stepDescriptions;

    /**
     * Provide a list of names for testing.
     */
    public ReservationFlow() {
        stepDescriptions = new ArrayList<>();
        stepDescriptions.add(new StepDescription("Dates", "Choose your reservation dates"));
        stepDescriptions.add(new StepDescription("Guests", "Provide guest details"));
        stepDescriptions.add(new StepDescription("Extras", "Select optional extras"));
        stepDescriptions.add(new StepDescription("Meals", "Choose optional meal plans"));
        stepDescriptions.add(new StepDescription("Review", "Verify your reservation"));
        stepDescriptions.add(new StepDescription("Payment", "Provide payment details"));
    }

    public ReservationFlow(List<StepDescription> stepDescriptions) {
        this.stepDescriptions = stepDescriptions;
    }

    public Reservation getReservation() {
        return reservation;
    }

    public void setReservation(Reservation reservation) {
        this.reservation = reservation;
    }

    public int getStep() {
        return step;
    }

    /**
     * The ordered list of flow steps to provide feedback on the current step.
     */
    public List<StepDescription> getStepDescriptions() {
        return stepDescriptions;
    }

    public void incrementStep() {
        if (step + 1 < stepDescriptions.size()) {
            step++;
        }
    }

    public void decrementStep() {
        if (step - 1 >= 0) {
            step--;
        }
    }

    public StepDescription getActiveStep() {
        return stepDescriptions.get(step);
    }

    public static class StepDescription {
        private String title;
        private String description;

        public StepDescription(String title, String description) {
            this.title = title;
            this.description = description;
        }

        public String getTitle() {
            return title;
        }

        public String getDescription() {
            return description;
        }
    }

    @Override
    public String toString() {
        return "ReservationFlow{" +
                "reservation=" + reservation +
                ", step=" + step +
                ", stepDescriptions=" + stepDescriptions +
                '}';
    }
}
