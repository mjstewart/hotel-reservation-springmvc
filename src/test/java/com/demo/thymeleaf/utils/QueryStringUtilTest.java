package com.demo.thymeleaf.utils;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

import static org.junit.Assert.*;

import static org.assertj.core.api.Assertions.*;

public class QueryStringUtilTest {

    /**
     * When null key value pairs are supplied, an empty string is returned.
     */
    @Test
    public void toQueryString_NullKeyValuePairs() {
        List<List<String>> keyValuePairs = new ArrayList<>();

        String result = QueryStringUtil.toQueryString(keyValuePairs, Function.identity());
        assertThat(result).isEmpty();
    }

    /**
     * When there are no key value pairs, an empty string is returned.
     */
    @Test
    public void toQueryString_EmptyKeyValuePairs() {
        List<List<String>> keyValuePairs = new ArrayList<>();

        String result = QueryStringUtil.toQueryString(keyValuePairs, Function.identity());
        assertThat(result).isEmpty();
    }

    /**
     * When only 1 key value pair is provided it should be the only pair in the resulting query string without any
     * separators.
     */
    @Test
    public void toQueryString_OneKeyValuePair() {
        List<List<String>> keyValuePairs = new ArrayList<>();
        keyValuePairs.add(Arrays.asList("sort", "location,asc"));
        String result = QueryStringUtil.toQueryString(keyValuePairs, Function.identity());
        String expected = "sort=location,asc";

        assertThat(result).isEqualTo(expected);
    }

    /**
     * When many key value pairs is provided, they should be joined together with an ampersand in the order they
     * are provided.
     */
    @Test
    public void toQueryString_ManyKeyValuePairs() {
        List<List<String>> keyValuePairs = new ArrayList<>();
        keyValuePairs.add(Arrays.asList("sort", "location,asc"));
        keyValuePairs.add(Arrays.asList("sort", "country,desc"));
        keyValuePairs.add(Arrays.asList("location", "europe"));

        String result = QueryStringUtil.toQueryString(keyValuePairs, Function.identity());
        String expected = "sort=location,asc&sort=country,desc&location=europe";

        assertThat(result).isEqualTo(expected);
    }

    /**
     * Check to see if any format other than the expected {@code field,sortDirection} format
     * returns an empty string.
     */
    @Test
    public void extractSortField_HandlesInvalidFormats() {
        assertThat(QueryStringUtil.extractSortField(null)).isEqualTo("");
        assertThat(QueryStringUtil.extractSortField("")).isEqualTo("");
        assertThat(QueryStringUtil.extractSortField(",")).isEqualTo("");
        assertThat(QueryStringUtil.extractSortField(",b")).isEqualTo("");
    }

    /**
     * Check to see if the field is extracted correctly from a variety of potential formats despite there only
     * being 1 legitimate format of {@code field,sortDirection}.
     */
    @Test
    public void extractSortField_HandlesValidInputs() {
        assertThat(QueryStringUtil.extractSortField("a,b,c,d")).isEqualTo("a");
        assertThat(QueryStringUtil.extractSortField("location,asc")).isEqualTo("location");
        assertThat(QueryStringUtil.extractSortField("location")).isEqualTo("location");
    }
}