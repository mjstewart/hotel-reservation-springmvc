package com.demo.thymeleaf.utils;

import org.junit.Test;
import org.thymeleaf.expression.Uris;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

public class QueryStringTest {

    @Test
    public void construction_Illegal_MissingAtLeastOneKeyValuePair() {
        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> QueryString.of(null, new Uris()));

        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> QueryString.of("", new Uris()));

        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> QueryString.of("=", new Uris()));

        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> QueryString.of("=value", new Uris()));

        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> QueryString.of("key", new Uris()));

        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> QueryString.of("key=", new Uris()));
    }

    @Test
    public void getState() {
        String query = "state=VIC&region=outer&suburb=Melbourne&region=north&postcode=3000&page=0&sort=stars,desc&locale=3&age=50&sort=name&region=central";
        QueryString queryString = QueryString.of(query, new Uris());

        Map<String, List<QueryString.KeyValueIndex>> stateMap = queryString.getState();

        assertThat(stateMap.get("state"))
                .containsExactly(
                        new QueryString.KeyValueIndex(0, new QueryString.KeyValue("state", "VIC"))
                );

        assertThat(stateMap.get("region"))
                .containsExactly(
                        new QueryString.KeyValueIndex(1, new QueryString.KeyValue("region", "outer")),
                        new QueryString.KeyValueIndex(3, new QueryString.KeyValue("region", "north")),
                        new QueryString.KeyValueIndex(10, new QueryString.KeyValue("region", "central"))
                );

        assertThat(stateMap.get("suburb"))
                .containsExactly(
                        new QueryString.KeyValueIndex(2, new QueryString.KeyValue("suburb", "Melbourne"))
                );

        assertThat(stateMap.get("postcode"))
                .containsExactly(
                        new QueryString.KeyValueIndex(4, new QueryString.KeyValue("postcode", "3000"))
                );

        assertThat(stateMap.get("page"))
                .containsExactly(
                        new QueryString.KeyValueIndex(5, new QueryString.KeyValue("page", "0"))
                );

        assertThat(stateMap.get("sort"))
                .containsExactly(
                        new QueryString.KeyValueIndex(6, new QueryString.KeyValue("sort", "stars,desc")),
                        new QueryString.KeyValueIndex(9, new QueryString.KeyValue("sort", "name"))
                );

        assertThat(stateMap.get("locale"))
                .containsExactly(
                        new QueryString.KeyValueIndex(7, new QueryString.KeyValue("locale", "3"))
                );

        assertThat(stateMap.get("age"))
                .containsExactly(
                        new QueryString.KeyValueIndex(8, new QueryString.KeyValue("age", "50"))
                );
    }

    /**
     * If nothing is changed in the query string, it should be the same as the original.
     */
    @Test
    public void reconstructQueryString_NoChanges() {
        String query = "state=VIC&region=outer&suburb=Melbourne&region=north&postcode=3000&page=0&sort=stars,desc&locale=3&age=50&sort=name&region=central";
        QueryString queryString = QueryString.of(query, new Uris());
        assertThat(queryString.reconstructQueryString()).isEqualTo(query);
    }

    /**
     * Ensure escaped characters aren't re-escaped or forgotten.
     */
    @Test
    public void reconstructQueryString_HasExpectedEscapeCharacters() {
        String query = "state=new%20south%20wales&region=outer%20east&suburb=melbourne%20west&region=north%20south&postcode=3000&%20%20page=0";
        QueryString queryString = QueryString.of(query, new Uris());
        assertThat(queryString.reconstructQueryString()).isEqualTo(query);
    }
}