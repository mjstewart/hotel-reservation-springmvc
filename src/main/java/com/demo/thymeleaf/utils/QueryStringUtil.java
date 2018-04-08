package com.demo.thymeleaf.utils;

import java.util.Optional;

public class QueryStringUtil {

    public Optional<String> getValue(String paramValuePair) {
        if (paramValuePair == null) {
            return Optional.empty();
        }
        String[] pair = paramValuePair.split("=");
        if (pair.length == 2) {
            return Optional.of(pair[1]);
        }
        return Optional.empty();
    }
}
