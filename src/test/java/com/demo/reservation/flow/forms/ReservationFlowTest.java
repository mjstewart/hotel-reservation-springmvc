package com.demo.reservation.flow.forms;

import org.junit.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

public class ReservationFlowTest {

    @Test
    public void transition() {
        ReservationFlow flow = new ReservationFlow(List.of(
                new ReservationFlow.StepDescription("a", "test a"),
                new ReservationFlow.StepDescription("b", "test b"),
                new ReservationFlow.StepDescription("c", "test c")
        ));

        assertThat(flow.getStep()).isEqualTo(0);
        assertThat(flow.getActiveStep().getTitle()).isEqualTo("a");

        flow.incrementStep();
        assertThat(flow.getStep()).isEqualTo(1);
        assertThat(flow.getActiveStep().getTitle()).isEqualTo("b");

        flow.incrementStep();
        assertThat(flow.getStep()).isEqualTo(2);
        assertThat(flow.getActiveStep().getTitle()).isEqualTo("c");

        // step number should not exceed upper step name list bounds.
        flow.incrementStep();
        assertThat(flow.getStep()).isEqualTo(2);
        assertThat(flow.getActiveStep().getTitle()).isEqualTo("c");

        flow.decrementStep();
        assertThat(flow.getStep()).isEqualTo(1);
        assertThat(flow.getActiveStep().getTitle()).isEqualTo("b");

        flow.decrementStep();
        assertThat(flow.getStep()).isEqualTo(0);
        assertThat(flow.getActiveStep().getTitle()).isEqualTo("a");

        // step number should not exceed lower step name list bounds.
        flow.decrementStep();
        assertThat(flow.getStep()).isEqualTo(0);
        assertThat(flow.getActiveStep().getTitle()).isEqualTo("a");
    }
}