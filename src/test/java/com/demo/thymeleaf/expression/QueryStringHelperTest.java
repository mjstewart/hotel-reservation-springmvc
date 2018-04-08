package com.demo.thymeleaf.expression;

import com.demo.thymeleaf.utils.ThymeleafExpressionParser;
import org.junit.Test;
import org.mockito.Mockito;
import org.thymeleaf.expression.Uris;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class QueryStringHelperTest {

    private ThymeleafExpressionParser mockIdentityParser(String query) {
        ThymeleafExpressionParser parser = Mockito.mock(ThymeleafExpressionParser.class);
        when(parser.parse(Mockito.eq(query))).thenReturn(query);
        return parser;
    }

    @Test
    public void replaceFirst_KeyDoesNotExist_HasNoEffect() {
        String query = "key2=value2&key2=value3&key3=value3&key2=value4";

        ThymeleafExpressionParser parser = mockIdentityParser(query);
        QueryStringHelper helper = new QueryStringHelper(parser, new Uris());

        String result = helper.replaceFirst(query, "missing", "value 999");

        assertThat(result).isEqualTo(query);
        verify(parser, times(1)).parse(query);
    }

    @Test
    public void replaceFirst_KeyExists_FirstOccurrenceReplacedOnly() {
        String query = "key2=value2&key2=value3&key3=value3&key2=value4";

        ThymeleafExpressionParser parser = mockIdentityParser(query);
        QueryStringHelper helper = new QueryStringHelper(parser, new Uris());

        // Also ensure %20 is added to the new value.
        String expected = "key2=value%20999&key2=value3&key3=value3&key2=value4";
        String result = helper.replaceFirst(query, "key2", "value 999");

        assertThat(result).isEqualTo(expected);
        verify(parser, times(1)).parse(query);
    }

    @SuppressWarnings({"MismatchedQueryAndUpdateOfCollection", "unchecked"})
    @Test
    public void replaceNth_KeyNotFound_HasNoEffect() {
        String query = "key2=value2&key2=value3&key3=value3&key2=value4&key4=value5";

        ThymeleafExpressionParser parser = mockIdentityParser(query);
        QueryStringHelper helper = new QueryStringHelper(parser, new Uris());

        // {missing: {0:'new value'}} - the key 'missing' is not in the query string
        Map stateChanges = new HashMap();
        Map keyValueChanges = new HashMap();
        keyValueChanges.put(0, "new value");
        stateChanges.put("missing", keyValueChanges);

        String result = helper.replaceNth(query, stateChanges);

        assertThat(result).isEqualTo(query);
        verify(parser, times(1)).parse(query);
    }

    @SuppressWarnings({"MismatchedQueryAndUpdateOfCollection", "unchecked"})
    @Test
    public void replaceNth_KeyFound_IllegalUpperBoundRelativeIndex_HasNoEffect() {
        String query = "key2=value2&key2=value3&key3=value3&key2=value4&key4=value5";

        // There are 3 key2 keys. Use this value for asserting the upper bound check being exceeded by 1.
        int totalKeys = 3;

        ThymeleafExpressionParser parser = mockIdentityParser(query);
        QueryStringHelper helper = new QueryStringHelper(parser, new Uris());

        /*
         * No generics are used since this simulates the expected Spring SpEL expression inserted in the template.
         * th:with="${#querystring.replaceNth(#request.getQueryString(), {key2: {4: 'new value'}})}"
         *
         * {key2: {4: 'new value'}} - there are only 3 key2 keys
         */
        Map stateChanges = new HashMap();
        Map keyValueChanges = new HashMap();
        keyValueChanges.put(totalKeys + 1, "new value");
        stateChanges.put("key2", keyValueChanges);

        String result = helper.replaceNth(query, stateChanges);

        assertThat(result).isEqualTo(query);
        verify(parser, times(1)).parse(query);
    }

    @SuppressWarnings({"MismatchedQueryAndUpdateOfCollection", "unchecked"})
    @Test
    public void replaceNth_KeyFound_IllegalLowerBoundRelativeIndex_HasNoEffect() {
        String query = "key2=value2&key2=value3&key3=value3&key2=value4&key4=value5";

        ThymeleafExpressionParser parser = mockIdentityParser(query);
        QueryStringHelper helper = new QueryStringHelper(parser, new Uris());

        /*
         * No generics are used since this simulates the expected Spring SpEL expression inserted in the template.
         * th:with="${#querystring.replaceNth(#request.getQueryString(), {key2: {-1: 'new value'}})}"
         *
         * {key2: {-1: 'new value'}} - lower bound exceeded
         */
        Map stateChanges = new HashMap();
        Map keyValueChanges = new HashMap();
        keyValueChanges.put(-1, "new value");
        stateChanges.put("key2", keyValueChanges);

        String result = helper.replaceNth(query, stateChanges);

        assertThat(result).isEqualTo(query);
        verify(parser, times(1)).parse(query);
    }

    /**
     * Asserts that key2 at relative index = 0 is replaced correctly.
     */
    @SuppressWarnings({"MismatchedQueryAndUpdateOfCollection", "unchecked"})
    @Test
    public void replaceNth_KeyFound_LegalRelativeIndex0_ReplacesValue() {
        String query = "key2=aa&key2=bb&key3=value3&key2=cc&key4=value5&key2=dd";
        String expected = "key2=aa%20new%20value&key2=bb&key3=value3&key2=cc&key4=value5&key2=dd";

        ThymeleafExpressionParser parser = mockIdentityParser(query);
        QueryStringHelper helper = new QueryStringHelper(parser, new Uris());

        /*
         * No generics are used since this simulates the expected Spring SpEL expression inserted in the template.
         * th:with="${#querystring.replaceNth(#request.getQueryString(), {key2: {0: 'aa new value'}})}"
         *
         * {key2: {0: 'new value'}}
         */
        Map stateChanges = new HashMap();
        Map keyValueChanges = new HashMap();
        keyValueChanges.put(0, "aa new value");
        stateChanges.put("key2", keyValueChanges);

        String result = helper.replaceNth(query, stateChanges);

        assertThat(result).isEqualTo(expected);
        verify(parser, times(1)).parse(query);
    }

    /**
     * Asserts that key2 at relative index = 1 is replaced correctly.
     */
    @SuppressWarnings({"MismatchedQueryAndUpdateOfCollection", "unchecked"})
    @Test
    public void replaceNth_KeyFound_LegalRelativeIndex1_ReplacesValue() {
        String query = "key2=aa&key2=bb&key3=value3&key2=cc&key4=value5&key2=dd";
        String expected = "key2=aa&key2=bb%20new%20value&key3=value3&key2=cc&key4=value5&key2=dd";

        ThymeleafExpressionParser parser = mockIdentityParser(query);
        QueryStringHelper helper = new QueryStringHelper(parser, new Uris());

        /*
         * No generics are used since this simulates the expected Spring SpEL expression inserted in the template.
         * th:with="${#querystring.replaceNth(#request.getQueryString(), {key2: {1: 'bb new value'}})}"
         *
         * {key2: {1: 'bb new value'}}
         */
        Map stateChanges = new HashMap();
        Map keyValueChanges = new HashMap();
        keyValueChanges.put(1, "bb new value");
        stateChanges.put("key2", keyValueChanges);

        String result = helper.replaceNth(query, stateChanges);

        assertThat(result).isEqualTo(expected);
        verify(parser, times(1)).parse(query);
    }

    /**
     * Asserts that key2 at relative index = 2 is replaced correctly.
     */
    @SuppressWarnings({"MismatchedQueryAndUpdateOfCollection", "unchecked"})
    @Test
    public void replaceNth_KeyFound_LegalRelativeIndex2_ReplacesValue() {
        String query = "key2=aa&key2=bb&key3=value3&key2=cc&key4=value5&key2=dd";
        String expected = "key2=aa&key2=bb&key3=value3&key2=cc%20new%20value&key4=value5&key2=dd";

        ThymeleafExpressionParser parser = mockIdentityParser(query);
        QueryStringHelper helper = new QueryStringHelper(parser, new Uris());

        /*
         * No generics are used since this simulates the expected Spring SpEL expression inserted in the template.
         * th:with="${#querystring.replaceNth(#request.getQueryString(), {key2: {2: 'cc new value'}})}"
         *
         * {key2: {2: 'cc new value'}}
         */
        Map stateChanges = new HashMap();
        Map keyValueChanges = new HashMap();
        keyValueChanges.put(2, "cc new value");
        stateChanges.put("key2", keyValueChanges);

        String result = helper.replaceNth(query, stateChanges);

        assertThat(result).isEqualTo(expected);
        verify(parser, times(1)).parse(query);
    }

    /**
     * Asserts that key2 at relative index = 3 is replaced correctly.
     */
    @SuppressWarnings({"MismatchedQueryAndUpdateOfCollection", "unchecked"})
    @Test
    public void replaceNth_KeyFound_LegalRelativeIndex3_ReplacesValue() {
        String query = "key2=aa&key2=bb&key3=value3&key2=cc&key4=value5&key2=dd";
        String expected = "key2=aa&key2=bb&key3=value3&key2=cc&key4=value5&key2=dd%20new%20value";

        ThymeleafExpressionParser parser = mockIdentityParser(query);
        QueryStringHelper helper = new QueryStringHelper(parser, new Uris());

        /*
         * No generics are used since this simulates the expected Spring SpEL expression inserted in the template.
         * th:with="${#querystring.replaceNth(#request.getQueryString(), {key2: {3: 'dd new value'}})}"
         *
         * {key2: {3: 'cc new value'}}
         */
        Map stateChanges = new HashMap();
        Map keyValueChanges = new HashMap();
        keyValueChanges.put(3, "dd new value");
        stateChanges.put("key2", keyValueChanges);

        String result = helper.replaceNth(query, stateChanges);

        assertThat(result).isEqualTo(expected);
        verify(parser, times(1)).parse(query);
    }

}