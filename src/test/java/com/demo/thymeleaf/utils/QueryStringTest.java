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
                        QueryString.KeyValue.fromKeyValue("state=VIC").get().toIndex(0)
                );

        assertThat(stateMap.get("region"))
                .containsExactly(
                        QueryString.KeyValue.fromKeyValue("region=outer").get().toIndex(1),
                        QueryString.KeyValue.fromKeyValue("region=north").get().toIndex(3),
                        QueryString.KeyValue.fromKeyValue("region=central").get().toIndex(10)
                );

        assertThat(stateMap.get("suburb"))
                .containsExactly(
                        QueryString.KeyValue.fromKeyValue("suburb=Melbourne").get().toIndex(2)
                );

        assertThat(stateMap.get("postcode"))
                .containsExactly(
                        QueryString.KeyValue.fromKeyValue("postcode=3000").get().toIndex(4)
                );

        assertThat(stateMap.get("page"))
                .containsExactly(
                        QueryString.KeyValue.fromKeyValue("page=0").get().toIndex(5)
                );

        assertThat(stateMap.get("sort"))
                .containsExactly(
                        QueryString.KeyValue.fromKeyValue("sort=stars,desc").get().toIndex(6),
                        QueryString.KeyValue.fromKeyValue("sort=name").get().toIndex(9)
                );

        assertThat(stateMap.get("locale"))
                .containsExactly(
                        QueryString.KeyValue.fromKeyValue("locale=3").get().toIndex(7)
                );

        assertThat(stateMap.get("age"))
                .containsExactly(
                        QueryString.KeyValue.fromKeyValue("age=50").get().toIndex(8)
                );

        // Sanity check, there are 10 keys, the next key if we were to add to the end would be 11.
        assertThat(queryString.getNextOverallIndex()).isEqualTo(11);
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