package com.demo.util;


import org.junit.Test;

import java.util.List;
import java.util.function.Function;

import static org.assertj.core.api.Assertions.*;

public class UtilsTest {

    @Test
    public void toCsv() {
        assertThat(Utils.toCsv(List.of(), Function.identity())).isEqualTo("");
        assertThat(Utils.toCsv(List.of(""), Function.identity())).isEqualTo("");
        assertThat(Utils.toCsv(List.of("a"), Function.identity())).isEqualTo("a");
        assertThat(Utils.toCsv(List.of("a", "b", "c"), Function.identity())).isEqualTo("a, b, c");
    }

    @Test
    public void capitalize() {
        assertThat(Utils.capitalize("")).isEqualTo("");
        assertThat(Utils.capitalize("a")).isEqualTo("A");
        assertThat(Utils.capitalize("apple")).isEqualTo("Apple");
        assertThat(Utils.capitalize("someFuNkYTeXT")).isEqualTo("Somefunkytext");
    }

    @Test
    public void capitalizeWords() {
        assertThat(Utils.capitalizeWords("")).isEqualTo("");
        assertThat(Utils.capitalizeWords("a")).isEqualTo("A");
        assertThat(Utils.capitalizeWords("apple")).isEqualTo("Apple");
        assertThat(Utils.capitalizeWords("hi there person")).isEqualTo("Hi There Person");
        assertThat(Utils.capitalizeWords("hI thErE PeRson")).isEqualTo("Hi There Person");
    }
}