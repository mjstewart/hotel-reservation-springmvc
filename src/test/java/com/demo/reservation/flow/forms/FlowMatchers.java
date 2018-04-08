package com.demo.reservation.flow.forms;

import org.hamcrest.FeatureMatcher;
import org.hamcrest.Matcher;
import org.hamcrest.Matchers;
import org.springframework.test.web.servlet.ResultMatcher;

import java.util.function.Function;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;

public class FlowMatchers {

    /**
     * Applies a mapping function on a {@code ReservationFlow} to produce a value {@code T}.
     * This value {@code T} is then fed into the {@code subMatcher} for the final assertion.
     *
     * @param mapper     The mapping function.
     * @param subMatcher Asserts the value produced by the mapping function.
     * @param <T>        The value produced by the mapping function.
     * @return The corresponding {@code FeatureMatcher}.
     */
    private static <T> FeatureMatcher<ReservationFlow, T> flowStateAssertion(Function<ReservationFlow, T> mapper,
                                                                             Matcher<T> subMatcher) {
        return new FeatureMatcher<>(subMatcher, "flowStateAssertion", "flowStateAssertion") {
            @Override
            protected T featureValueOf(ReservationFlow reservationFlow) {
                return mapper.apply(reservationFlow);
            }
        };
    }

    /**
     * Asserts the {@code Model} has the supplied {@code Step} as being completed.
     */
    public static ResultMatcher modelHasCompletedFlowStep(ReservationFlow.Step step) {
        FeatureMatcher<ReservationFlow, Boolean> matcher = flowStateAssertion(
                flow -> flow.isCompleted(step),
                Matchers.is(Boolean.TRUE)
        );
        return model().attribute("reservationFlow", matcher);
    }

    /**
     * Asserts the {@code FlashAttributes} has the supplied {@code Step} as being completed.
     */
    public static ResultMatcher flashHasCompletedFlowStep(ReservationFlow.Step step) {
        FeatureMatcher<ReservationFlow, Boolean> matcher = flowStateAssertion(
                flow -> flow.isCompleted(step),
                Matchers.is(Boolean.TRUE)
        );
        return flash().attribute("reservationFlow", matcher);
    }

    /**
     * Asserts the {@code Model} has the supplied {@code Step} set as active.
     */
    public static ResultMatcher modelHasActiveFlowStep(ReservationFlow.Step step) {
        FeatureMatcher<ReservationFlow, Boolean> matcher = flowStateAssertion(
                flow -> flow.isActive(step),
                Matchers.is(Boolean.TRUE)
        );
        return model().attribute("reservationFlow", matcher);
    }

    /**
     * Asserts the {@code FlashAttributes} has the supplied {@code Step} set as active.
     */
    public static ResultMatcher flashHasActiveFlowStep(ReservationFlow.Step step) {
        FeatureMatcher<ReservationFlow, Boolean> matcher = flowStateAssertion(
                flow -> flow.isActive(step),
                Matchers.is(Boolean.TRUE)
        );
        return flash().attribute("reservationFlow", matcher);
    }


    /**
     * Asserts the {@code Model} has the supplied {@code Step} as NOT being completed.
     */
    public static ResultMatcher modelHasIncompleteFlowStep(ReservationFlow.Step step) {
        FeatureMatcher<ReservationFlow, Boolean> matcher = flowStateAssertion(
                flow -> flow.isCompleted(step),
                Matchers.is(Boolean.FALSE)
        );
        return model().attribute("reservationFlow", matcher);
    }

    /**
     * Asserts the {@code FlashAttributes} has the supplied {@code Step} as NOT being completed.
     */
    public static ResultMatcher flashHasIncompleteFlowStep(ReservationFlow.Step step) {
        FeatureMatcher<ReservationFlow, Boolean> matcher = flowStateAssertion(
                flow -> flow.isCompleted(step),
                Matchers.is(Boolean.FALSE)
        );
        return flash().attribute("reservationFlow", matcher);
    }
}
