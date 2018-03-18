package com.demo.reservation.flow.forms;

import com.demo.reservation.flow.forms.DateForm;

import java.util.List;

public class ReservationFlowForms {
    private DateForm dateForm;

    private int step = 0;
    private List<String> stepNames = List.of("Dates");

    public DateForm getDateForm() {
        return dateForm;
    }

    public int getStep() {
        return step;
    }

    public List<String> getStepNames() {
        return stepNames;
    }

    public void incrementStep() {
        if (step + 1 < stepNames.size()) {
            step++;
        }
    }

    public void decrementStep() {
        if (step - 1 >= 0) {
            step--;
        }
    }

    public String getActiveStepName() {
        return stepNames.get(step);
    }

    @Override
    public String toString() {
        return "ReservationFlowForms{" +
                "step=" + step +
                ", activeStepName=" + getActiveStepName() +
                '}';
    }
}
