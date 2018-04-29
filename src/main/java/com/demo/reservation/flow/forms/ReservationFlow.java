package com.demo.reservation.flow.forms;

import com.demo.domain.Reservation;

import javax.validation.Valid;
import java.util.*;

/**
 * Stores the {@code Reservation} and the current flow {@code Step}. {@code Step} manipulation functions are dumb and
 * rely on controller logic to keep the flow step in sync.
 *
 * <p>There cannot be an incrementing counter given page refreshes must not advance flow steps.</p>
 */
public class ReservationFlow {

    public enum Step {
        Dates(0),
        Guests(1),
        Extras(2),
        Meals(3),
        Review(4),
        Payment(5);

        int flowStep;

        Step(int flowStep) {
            this.flowStep = flowStep;
        }

        public static Step from(int flowStep) {
            switch (flowStep) {
                case 0:
                    return Dates;
                case 1:
                    return Guests;
                case 2:
                    return Extras;
                case 3:
                    return Meals;
                case 4:
                    return Review;
                case 5:
                    return Payment;
                default:
                    return Dates;
            }
        }
    }

    @Valid
    private Reservation reservation = new Reservation();

    private List<StepDescription> stepDescriptions = new ArrayList<>();

    private Set<Step> completedSteps = new HashSet<>();

    private Step activeStep = Step.Dates;

    public ReservationFlow() {
        stepDescriptions.add(new StepDescription(0, "Dates", "Choose your reservation dates"));
        stepDescriptions.add(new StepDescription(1, "Guests", "Provide guest details"));
        stepDescriptions.add(new StepDescription(2, "Extras", "Select optional extras"));
        stepDescriptions.add(new StepDescription(3, "Meals", "Choose optional meal plans"));
        stepDescriptions.add(new StepDescription(4, "Review", "Verify your reservation"));
        stepDescriptions.add(new StepDescription(5, "Payment", "Provide payment details"));
    }

    public Reservation getReservation() {
        return reservation;
    }

    public void setReservation(Reservation reservation) {
        this.reservation = reservation;
    }

    public void setActive(Step step) {
        activeStep = step;
    }

    public Step getActiveStep() {
        return activeStep;
    }

    public StepDescription getActiveStepDescription() {
        return stepDescriptions.get(activeStep.flowStep);
    }

    public void completeStep(Step step) {
        completedSteps.add(step);
    }

    public void incompleteStep(Step step) {
        completedSteps.remove(step);
    }

    public boolean isActive(Step step) {
        return step == activeStep;
    }

    public boolean isCompleted(Step step) {
        return completedSteps.contains(step);
    }

    public void enterStep(Step step) {
        setActive(step);
        incompleteStep(step);
    }

    public List<StepDescription> getStepDescriptions() {
        return stepDescriptions;
    }

    public static class StepDescription {
        private int flowStep;
        private String title;
        private String description;

        public StepDescription(int flowStep, String title, String description) {
            this.flowStep = flowStep;
            this.title = title;
            this.description = description;
        }

        public int getFlowStep() {
            return flowStep;
        }

        public String getTitle() {
            return title;
        }

        public String getDescription() {
            return description;
        }

        private int normalizedFlowStep() {
            return flowStep + 1;
        }

        public String getFlowStepWithTitle() {
            return normalizedFlowStep() + ". " + title;
        }

        public String getFlowStepWithDescription() {
            return normalizedFlowStep() + ". " + description;
        }

        @Override
        public String toString() {
            return "StepDescription{" +
                    "flowStep=" + flowStep +
                    ", title='" + title + '\'' +
                    ", description='" + description + '\'' +
                    '}';
        }
    }

}
