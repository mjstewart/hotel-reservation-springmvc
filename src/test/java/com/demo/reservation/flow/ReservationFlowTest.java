package com.demo.reservation.flow;

import com.demo.reservation.flow.forms.ReservationFlow;
import org.junit.Test;

import static org.assertj.core.api.Assertions.*;

public class ReservationFlowTest {

    @Test
    public void beginsAtFlowStep0() {
        ReservationFlow flow = new ReservationFlow();
        assertThat(flow.getActiveStep()).isEqualTo(ReservationFlow.Step.Dates);
    }

    @Test
    public void hasCorrectFlowSequence() {
        ReservationFlow flow = new ReservationFlow();
        assertThat(flow.getStepDescriptions().size()).isEqualTo(6);
        assertThat(flow.getStepDescriptions().get(0).getTitle()).isEqualTo("Dates");
        assertThat(flow.getStepDescriptions().get(1).getTitle()).isEqualTo("Guests");
        assertThat(flow.getStepDescriptions().get(2).getTitle()).isEqualTo("Extras");
        assertThat(flow.getStepDescriptions().get(3).getTitle()).isEqualTo("Meals");
        assertThat(flow.getStepDescriptions().get(4).getTitle()).isEqualTo("Review");
        assertThat(flow.getStepDescriptions().get(5).getTitle()).isEqualTo("Payment");
    }

    @Test
    public void transition() {
        ReservationFlow flow = new ReservationFlow();
        assertThat(flow.isActive(ReservationFlow.Step.Dates)).isTrue();

        flow.completeStep(ReservationFlow.Step.Dates);
        assertThat(flow.isCompleted(ReservationFlow.Step.Dates)).isTrue();
        assertThat(flow.isActive(ReservationFlow.Step.Dates)).isTrue();

        flow.setActive(ReservationFlow.Step.Guests);
        assertThat(flow.isActive(ReservationFlow.Step.Guests)).isTrue();
        assertThat(flow.isCompleted(ReservationFlow.Step.Guests)).isFalse();
        flow.completeStep(ReservationFlow.Step.Guests);
        assertThat(flow.isActive(ReservationFlow.Step.Guests)).isTrue();
        assertThat(flow.isCompleted(ReservationFlow.Step.Guests)).isTrue();

        flow.incompleteStep(ReservationFlow.Step.Guests);
        assertThat(flow.isActive(ReservationFlow.Step.Guests)).isTrue();
        assertThat(flow.isCompleted(ReservationFlow.Step.Guests)).isFalse();
    }

    @Test
    public void activeStepFormatting() {
        ReservationFlow.StepDescription description =
                new ReservationFlow.StepDescription(0, "a", "test a");

        // assert 0 indexing is removed.
        assertThat(description.getFlowStepWithTitle()).isEqualTo("1. a");
        assertThat(description.getFlowStepWithDescription()).isEqualTo("1. test a");
    }
}