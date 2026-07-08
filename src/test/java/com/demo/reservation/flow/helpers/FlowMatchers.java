package com.demo.reservation.flow.helpers;

import com.demo.reservation.flow.forms.ReservationFlow;
import org.hamcrest.FeatureMatcher;
import org.hamcrest.Matcher;
import org.hamcrest.Matchers;
import org.springframework.test.web.servlet.ResultMatcher;

import java.util.function.Function;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;

public class FlowMatchers {


    public static <T> FeatureMatcher<ReservationFlow, T> flowStateAssertion(Function<ReservationFlow, T> mapper,
                                                                            Matcher<T> subMatcher) {
        return new FeatureMatcher<>(subMatcher, "flowStateAssertion", "flowStateAssertion") {
            @Override
            protected T featureValueOf(ReservationFlow reservationFlow) {
                return mapper.apply(reservationFlow);
            }
        };
    }


    public static ResultMatcher modelHasCompletedFlowStep(ReservationFlow.Step step) {
        // flow -> flow.isCompleted(step)  (true/false) ----> feeds into the subMatcher
        FeatureMatcher<ReservationFlow, Boolean> matcher = flowStateAssertion(
                flow -> flow.isCompleted(step),
                Matchers.is(Boolean.TRUE)
        );
        return model().attribute("reservationFlow", matcher);
    }


    public static ResultMatcher flashHasCompletedFlowStep(ReservationFlow.Step step) {
        FeatureMatcher<ReservationFlow, Boolean> matcher = flowStateAssertion(
                flow -> flow.isCompleted(step),
                Matchers.is(Boolean.TRUE)
        );
        return flash().attribute("reservationFlow", matcher);
    }

    public static ResultMatcher modelHasActiveFlowStep(ReservationFlow.Step step) {
        FeatureMatcher<ReservationFlow, Boolean> matcher = flowStateAssertion(
                flow -> flow.isActive(step),
                Matchers.is(Boolean.TRUE)
        );
        return model().attribute("reservationFlow", matcher);
    }


    public static ResultMatcher flashHasActiveFlowStep(ReservationFlow.Step step) {
        FeatureMatcher<ReservationFlow, Boolean> matcher = flowStateAssertion(
                flow -> flow.isActive(step),
                Matchers.is(Boolean.TRUE)
        );
        return flash().attribute("reservationFlow", matcher);
    }


    public static ResultMatcher modelHasIncompleteFlowStep(ReservationFlow.Step step) {
        FeatureMatcher<ReservationFlow, Boolean> matcher = flowStateAssertion(
                flow -> flow.isCompleted(step),
                Matchers.is(Boolean.FALSE)
        );
        return model().attribute("reservationFlow", matcher);
    }

    public static ResultMatcher flashHasIncompleteFlowStep(ReservationFlow.Step step) {
        FeatureMatcher<ReservationFlow, Boolean> matcher = flowStateAssertion(
                flow -> flow.isCompleted(step),
                Matchers.is(Boolean.FALSE)
        );
        return flash().attribute("reservationFlow", matcher);
    }
}
