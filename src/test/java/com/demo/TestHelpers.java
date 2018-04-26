package com.demo;

import org.hamcrest.FeatureMatcher;
import org.hamcrest.Matcher;

import java.util.function.Function;

public class TestHelpers {

    /**
     * The result of applying the mapping function is fed into the sub matcher for the final assertion.
     *
     * <p>
     * <p>Usage: Assert that the {@code Page} has 0 elements.</p>
     * <blockquote>
     * <pre>
     *      FeatureMatcher<Page<Hotel>, Long> hasExpectedPageResult = mappedAssertion(Page::getTotalElements, Matchers.is(0L));
     *
     *      ...
     *      .andExpect(model().attribute("resultPage", hasExpectedPageResult))
     * </pre>
     * </blockquote>
     *
     * @param mapper     The mapping function.
     * @param subMatcher Asserts the value produced by the mapping function.
     * @param <T>        Output of the mapping function
     * @param <U>        Input to the mapping function
     * @return The {@code FeatureMatcher}
     */
    public static <U, T> FeatureMatcher<U, T> mappedAssertion(Function<U, T> mapper,
                                                              Matcher<T> subMatcher) {
        return new FeatureMatcher<>(subMatcher, "mappedAssertion", "mappedAssertion") {
            @Override
            protected T featureValueOf(U value) {
                return mapper.apply(value);
            }
        };
    }
}