package com.demo.util;

import java.util.Collection;
import java.util.function.Function;
import java.util.stream.Collectors;

public class Utils {
    public static <T> String toCsv(Collection<T> coll, Function<T, CharSequence> keyMapper) {
        return coll.stream()
                .map(keyMapper)
                .collect(Collectors.joining(", "));
    }

    public static String capitalize(String s) {
        if (s.isEmpty()) return s;
        return s.substring(0, 1).toUpperCase() + s.substring(1).toLowerCase();
    }

}
