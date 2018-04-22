package com.demo.thymeleaf.utils;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

public class QueryStringUtil {

    public static String toQueryString(List<List<String>> keyValuePairs, Function<String, String> escapeMapper) {
        return keyValuePairs.stream()
                .map(QueryString.KeyValue::fromPair)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .map(qs -> qs.escape(escapeMapper))
                .collect(Collectors.joining("&"));
    }

    /**
     * Assumes value follows the spring {@code PagingAndSortingRepository} convention of supplying
     * {@code field,sortDirection} into the query string.
     *
     * @param value The value in {@code 'field,sortDirection'} format.
     * @return The extracted field.
     */
    public static String extractSortField(String value) {
        if (value == null || value.isEmpty()) {
            return "";
        }
        String[] tokens = value.split(",");
        return tokens.length > 0 ? tokens[0] : "";
    }
}
