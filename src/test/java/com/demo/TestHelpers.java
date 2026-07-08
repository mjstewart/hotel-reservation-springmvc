package com.demo;

import org.hamcrest.FeatureMatcher;
import org.hamcrest.Matcher;

import java.util.function.Function;

public class TestHelpers {

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