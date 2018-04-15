package com.demo.thymeleaf.expression.helpers;

import com.demo.thymeleaf.expression.QueryStringHelper;
import org.junit.Test;

import javax.servlet.http.HttpServletRequest;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

public class ReplaceTest {

    /**
     * When the key does not exist, the original query string should be returned.
     */
    @Test
    public void replaceFirst_KeyDoesNotExist_HasNoEffect() {
        String query = "key2=value2&key2=value3&key3=value3&key2=value4";

        HttpServletRequest mockHttpRequest = mock(HttpServletRequest.class);
        when(mockHttpRequest.getQueryString()).thenReturn(query);

        QueryStringHelper helper = new QueryStringHelper();

        String result = helper.replaceFirst(mockHttpRequest, "missing", "value 999");

        assertThat(result).isEqualTo(query);

        verify(mockHttpRequest, times(1)).getQueryString();
        verifyNoMoreInteractions(mockHttpRequest);
    }

    /**
     * Handles null key and value arguments by returning the original query string.
     */
    @Test
    public void replaceFirst_HandlesNull_HasNoEffect() {
        String query = "key2=value2&key2=value3&key3=value3&key2=value4";

        HttpServletRequest mockHttpRequest = mock(HttpServletRequest.class);
        when(mockHttpRequest.getQueryString()).thenReturn(query);

        QueryStringHelper helper = new QueryStringHelper();

        String result = helper.replaceFirst(mockHttpRequest, null, null);

        assertThat(result).isEqualTo(query);

        verify(mockHttpRequest, times(1)).getQueryString();
        verifyNoMoreInteractions(mockHttpRequest);
    }

    /**
     * Replace a key that occurs at the very start of the query string while any other keys with the same name
     * remain unchanged.
     */
    @Test
    public void replaceFirst_FirstOccurrenceReplacedOnly() {
        String query = "key2=value2&key2=value3&key3=value3&key2=value4";

        HttpServletRequest mockHttpRequest = mock(HttpServletRequest.class);
        when(mockHttpRequest.getQueryString()).thenReturn(query);

        QueryStringHelper helper = new QueryStringHelper();

        String expected = "key2=value%20999&key2=value3&key3=value3&key2=value4";
        String result = helper.replaceFirst(mockHttpRequest, "key2", "value 999");

        assertThat(result).isEqualTo(expected);

        verify(mockHttpRequest, times(1)).getQueryString();
        verifyNoMoreInteractions(mockHttpRequest);
    }

    /**
     * Replace a key that occurs in the middle rather than at the very start of the query string.
     * The test targets key3 which only occurs once and is in the middle. This is more of a sanity check type test.
     */
    @Test
    public void replaceFirst_FirstOccurrenceMidWayReplacedOnly() {
        String query = "key2=value2&key2=value3&key3=value3&key2=value4";
        String expected = "key2=value2&key2=value3&key3=value%20999&key2=value4";

        HttpServletRequest mockHttpRequest = mock(HttpServletRequest.class);
        when(mockHttpRequest.getQueryString()).thenReturn(query);

        QueryStringHelper helper = new QueryStringHelper();

        String result = helper.replaceFirst(mockHttpRequest, "key3", "value 999");

        assertThat(result).isEqualTo(expected);

        verify(mockHttpRequest, times(1)).getQueryString();
        verifyNoMoreInteractions(mockHttpRequest);
    }

    /**
     * Replace a key that occurs last rather than at the very start of the query string. This is more of a
     * sanity check type test.
     */
    @Test
    public void replaceFirst_LastOccurrenceReplacedOnly() {
        String query = "key2=value2&key2=value3&key3=value3&key2=value4&key9=hello%20world";
        String expected = "key2=value2&key2=value3&key3=value3&key2=value4&key9=world%20hello%20there";

        HttpServletRequest mockHttpRequest = mock(HttpServletRequest.class);
        when(mockHttpRequest.getQueryString()).thenReturn(query);

        QueryStringHelper helper = new QueryStringHelper();

        String result = helper.replaceFirst(mockHttpRequest, "key9", "world hello there");

        assertThat(result).isEqualTo(expected);

        verify(mockHttpRequest, times(1)).getQueryString();
        verifyNoMoreInteractions(mockHttpRequest);
    }

    /**
     * Confirm the query string is being escaped
     */
    @Test
    public void replaceFirst_EscapesCorrectly() {
        String query = "key2=value2&key2=value3&key3=value3&key2=value4&key9=hello%20world";
        String expected = "key2=value2&key2=value3&key3=value3&key2=value4&key9=%20%20world%201%202%20hello%204.3.2";

        HttpServletRequest mockHttpRequest = mock(HttpServletRequest.class);
        when(mockHttpRequest.getQueryString()).thenReturn(query);

        QueryStringHelper helper = new QueryStringHelper();

        String result = helper.replaceFirst(mockHttpRequest, "key9", "  world 1 2 hello 4.3.2");

        assertThat(result).isEqualTo(expected);

        verify(mockHttpRequest, times(1)).getQueryString();
        verifyNoMoreInteractions(mockHttpRequest);
    }

    /**
     * When the key is not found, the original query string should be returned.
     */
    @SuppressWarnings({"MismatchedQueryAndUpdateOfCollection", "unchecked"})
    @Test
    public void replaceNth_KeyNotFound_HasNoEffect() {
        String query = "key2=value2&key2=value3&key3=value3&key2=value4&key4=value5";

        HttpServletRequest mockHttpRequest = mock(HttpServletRequest.class);
        when(mockHttpRequest.getQueryString()).thenReturn(query);

        QueryStringHelper helper = new QueryStringHelper();

        // {missing: {0:'new value'}} - the key 'missing' is not in the query string
        Map stateChanges = new HashMap();
        Map keyValueChanges = new HashMap();
        keyValueChanges.put(0, "new value");
        stateChanges.put("missing", keyValueChanges);

        String result = helper.replaceNth(mockHttpRequest, stateChanges);

        assertThat(result).isEqualTo(query);

        verify(mockHttpRequest, times(1)).getQueryString();
        verifyNoMoreInteractions(mockHttpRequest);
    }

    /**
     * Handles null argument by returning the original query string.
     */
    @SuppressWarnings({"MismatchedQueryAndUpdateOfCollection", "unchecked"})
    @Test
    public void replaceNth_HandlesNull_HasNoEffect() {
        String query = "key2=value2&key2=value3&key3=value3&key2=value4&key4=value5";

        HttpServletRequest mockHttpRequest = mock(HttpServletRequest.class);
        when(mockHttpRequest.getQueryString()).thenReturn(query);

        QueryStringHelper helper = new QueryStringHelper();

        String result = helper.replaceNth(mockHttpRequest, null);

        assertThat(result).isEqualTo(query);

        verify(mockHttpRequest, times(1)).getQueryString();
        verifyNoMoreInteractions(mockHttpRequest);
    }

    /**
     * key2 relative values are [value2, value3, value4]. The test asserts original query string is
     * returned upon upper array bound being exceeded.
     */
    @SuppressWarnings({"MismatchedQueryAndUpdateOfCollection", "unchecked"})
    @Test
    public void replaceNth_IllegalUpperBoundRelativeIndex_HasNoEffect() {
        String query = "key2=value2&key2=value3&key3=value3&key2=value4&key4=value5";

        // There are 3 key2 keys. Use this value for asserting the upper bound check being exceeded by 1.
        int totalKeys = 3;

        HttpServletRequest mockHttpRequest = mock(HttpServletRequest.class);
        when(mockHttpRequest.getQueryString()).thenReturn(query);

        QueryStringHelper helper = new QueryStringHelper();

        /*
         * No generics are used since this simulates the expected Spring SpEL expression inserted in the template.
         * th:with="${#qs.replaceNth(#request, {key2: {4: 'new value'}})}"
         *
         * {key2: {4: 'new value'}} - there are only 3 key2 keys
         */
        Map stateChanges = new HashMap();
        Map keyValueChanges = new HashMap();
        keyValueChanges.put(totalKeys + 1, "new value");
        stateChanges.put("key2", keyValueChanges);

        String result = helper.replaceNth(mockHttpRequest, stateChanges);

        assertThat(result).isEqualTo(query);

        verify(mockHttpRequest, times(1)).getQueryString();
        verifyNoMoreInteractions(mockHttpRequest);
    }

    /**
     * key2 relative values are [value2, value3, value4]. The test asserts original query string is
     * returned upon lower array bound being exceeded.
     */
    @SuppressWarnings({"MismatchedQueryAndUpdateOfCollection", "unchecked"})
    @Test
    public void replaceNth_IllegalLowerBoundRelativeIndex_HasNoEffect() {
        String query = "key2=value2&key2=value3&key3=value3&key2=value4&key4=value5";

        HttpServletRequest mockHttpRequest = mock(HttpServletRequest.class);
        when(mockHttpRequest.getQueryString()).thenReturn(query);

        QueryStringHelper helper = new QueryStringHelper();

        /*
         * No generics are used since this simulates the expected Spring SpEL expression inserted in the template.
         * th:with="${#qs.replaceNth(#request, {key2: {-1: 'new value'}})}"
         *
         * {key2: {-1: 'new value'}} - lower bound exceeded
         */
        Map stateChanges = new HashMap();
        Map keyValueChanges = new HashMap();
        keyValueChanges.put(-1, "new value");
        stateChanges.put("key2", keyValueChanges);

        String result = helper.replaceNth(mockHttpRequest, stateChanges);

        assertThat(result).isEqualTo(query);

        verify(mockHttpRequest, times(1)).getQueryString();
        verifyNoMoreInteractions(mockHttpRequest);
    }

    /**
     * key2 relative values are [value2, value3, value4]. Confirm index 0 is replaced.
     */
    @SuppressWarnings({"MismatchedQueryAndUpdateOfCollection", "unchecked"})
    @Test
    public void replaceNth_LegalRelativeIndex0_ReplacesValue() {
        String query = "key2=aa&key2=bb&key3=value3&key2=cc&key4=value5&key2=dd";
        String expected = "key2=aa%20new%20value&key2=bb&key3=value3&key2=cc&key4=value5&key2=dd";

        HttpServletRequest mockHttpRequest = mock(HttpServletRequest.class);
        when(mockHttpRequest.getQueryString()).thenReturn(query);

        QueryStringHelper helper = new QueryStringHelper();

        /*
         * No generics are used since this simulates the expected Spring SpEL expression inserted in the template.
         * th:with="${#qs.replaceNth(#request, {key2: {0: 'aa new value'}})}"
         *
         * {key2: {0: 'new value'}}
         */
        Map stateChanges = new HashMap();
        Map keyValueChanges = new HashMap();
        keyValueChanges.put(0, "aa new value");
        stateChanges.put("key2", keyValueChanges);

        String result = helper.replaceNth(mockHttpRequest, stateChanges);

        assertThat(result).isEqualTo(expected);

        verify(mockHttpRequest, times(1)).getQueryString();
        verifyNoMoreInteractions(mockHttpRequest);
    }

    /**
     * key2 relative values are [value2, value3, value4]. Confirm index 1 is replaced.
     */
    @SuppressWarnings({"MismatchedQueryAndUpdateOfCollection", "unchecked"})
    @Test
    public void replaceNth_LegalRelativeIndex1_ReplacesValue() {
        String query = "key2=aa&key2=bb&key3=value3&key2=cc&key4=value5&key2=dd";
        String expected = "key2=aa&key2=bb%20new%20value&key3=value3&key2=cc&key4=value5&key2=dd";

        HttpServletRequest mockHttpRequest = mock(HttpServletRequest.class);
        when(mockHttpRequest.getQueryString()).thenReturn(query);

        QueryStringHelper helper = new QueryStringHelper();

        /*
         * No generics are used since this simulates the expected Spring SpEL expression inserted in the template.
         * th:with="${#qs.replaceNth(#request, {key2: {1: 'bb new value'}})}"
         *
         * {key2: {1: 'bb new value'}}
         */
        Map stateChanges = new HashMap();
        Map keyValueChanges = new HashMap();
        keyValueChanges.put(1, "bb new value");
        stateChanges.put("key2", keyValueChanges);

        String result = helper.replaceNth(mockHttpRequest, stateChanges);

        assertThat(result).isEqualTo(expected);

        verify(mockHttpRequest, times(1)).getQueryString();
        verifyNoMoreInteractions(mockHttpRequest);
    }

    /**
     * key2 relative values are [value2, value3, value4]. Confirm index 2 is replaced.
     */
    @SuppressWarnings({"MismatchedQueryAndUpdateOfCollection", "unchecked"})
    @Test
    public void replaceNth_LegalRelativeIndex2_ReplacesValue() {
        String query = "key2=aa&key2=bb&key3=value3&key2=cc&key4=value5&key2=dd";
        String expected = "key2=aa&key2=bb&key3=value3&key2=cc%20new%20value&key4=value5&key2=dd";

        HttpServletRequest mockHttpRequest = mock(HttpServletRequest.class);
        when(mockHttpRequest.getQueryString()).thenReturn(query);

        QueryStringHelper helper = new QueryStringHelper();

        /*
         * No generics are used since this simulates the expected Spring SpEL expression inserted in the template.
         * th:with="${#qs.replaceNth(#request, {key2: {2: 'cc new value'}})}"
         *
         * {key2: {2: 'cc new value'}}
         */
        Map stateChanges = new HashMap();
        Map keyValueChanges = new HashMap();
        keyValueChanges.put(2, "cc new value");
        stateChanges.put("key2", keyValueChanges);

        String result = helper.replaceNth(mockHttpRequest, stateChanges);

        assertThat(result).isEqualTo(expected);

        verify(mockHttpRequest, times(1)).getQueryString();
        verifyNoMoreInteractions(mockHttpRequest);
    }

    /**
     * key2 relative values are [value2, value3, value4]. Confirm index 3 is replaced.
     */
    @SuppressWarnings({"MismatchedQueryAndUpdateOfCollection", "unchecked"})
    @Test
    public void replaceNth_LegalRelativeIndex3_ReplacesValue() {
        String query = "key2=aa&key2=bb&key3=value3&key2=cc&key4=value5&key2=dd";
        String expected = "key2=aa&key2=bb&key3=value3&key2=cc&key4=value5&key2=dd%20new%20value";

        HttpServletRequest mockHttpRequest = mock(HttpServletRequest.class);
        when(mockHttpRequest.getQueryString()).thenReturn(query);

        QueryStringHelper helper = new QueryStringHelper();

        /*
         * No generics are used since this simulates the expected Spring SpEL expression inserted in the template.
         * th:with="${#qs.replaceNth(#request, {key2: {3: 'dd new value'}})}"
         *
         * {key2: {3: 'dd new value'}}
         */
        Map stateChanges = new HashMap();
        Map keyValueChanges = new HashMap();
        keyValueChanges.put(3, "dd new value");
        stateChanges.put("key2", keyValueChanges);

        String result = helper.replaceNth(mockHttpRequest, stateChanges);

        assertThat(result).isEqualTo(expected);

        verify(mockHttpRequest, times(1)).getQueryString();
        verifyNoMoreInteractions(mockHttpRequest);
    }

    /**
     * Confirm the query string is being escaped
     */
    @SuppressWarnings({"MismatchedQueryAndUpdateOfCollection", "unchecked"})
    @Test
    public void replaceNth_ReplacesValueWithEscaping() {
        String query = "key2=aa&key2=bb&key3=value3&key2=cc&key4=value5&key2=dd";
        String expected = "key2=a%20new%20value&key2=bb&key3=value3&key2=cc&key4=value5&key2=dd";

        HttpServletRequest mockHttpRequest = mock(HttpServletRequest.class);
        when(mockHttpRequest.getQueryString()).thenReturn(query);

        QueryStringHelper helper = new QueryStringHelper();

        /*
         * No generics are used since this simulates the expected Spring SpEL expression inserted in the template.
         * th:with="${#qs.replaceNth(#request, {key2: {0: 'a new value'}})}"
         *
         * {key2: {0: 'aa new value'}}
         */
        Map stateChanges = new HashMap();
        Map keyValueChanges = new HashMap();
        keyValueChanges.put(0, "a new value");
        stateChanges.put("key2", keyValueChanges);

        String result = helper.replaceNth(mockHttpRequest, stateChanges);

        assertThat(result).isEqualTo(expected);

        verify(mockHttpRequest, times(1)).getQueryString();
        verifyNoMoreInteractions(mockHttpRequest);
    }

    /**
     * When the key does not exist the original query string should be returned.
     */
    @Test
    public void replaceN_KeyNotFound_HasNoEffect() {
        String query = "key2=aa&key4=yy&key2=bb&key3=value3&key2=cc&key4=value5&key2=dd";

        HttpServletRequest mockHttpRequest = mock(HttpServletRequest.class);
        when(mockHttpRequest.getQueryString()).thenReturn(query);

        QueryStringHelper helper = new QueryStringHelper();

        ArrayList<Object> values = new ArrayList<>(Arrays.asList("aa1", "bb 2", "cc 3", "dd"));

        String result = helper.replaceN(mockHttpRequest, "missing", values);

        assertThat(result).isEqualTo(query);

        verify(mockHttpRequest, times(1)).getQueryString();
        verifyNoMoreInteractions(mockHttpRequest);
    }

    /**
     * When there are null arguments, the original query string should be returned.
     */
    @Test
    public void replaceN_HandlesNull_HasNoEffect() {
        String query = "key2=aa&key4=yy&key2=bb&key3=value3&key2=cc&key4=value5&key2=dd";

        HttpServletRequest mockHttpRequest = mock(HttpServletRequest.class);
        when(mockHttpRequest.getQueryString()).thenReturn(query);

        QueryStringHelper helper = new QueryStringHelper();

        String result = helper.replaceN(mockHttpRequest, null, null);

        assertThat(result).isEqualTo(query);

        verify(mockHttpRequest, times(1)).getQueryString();
        verifyNoMoreInteractions(mockHttpRequest);
    }

    /**
     * If there are 4 occurrences of key2 but 20 values are provided, only the first 4 values are used
     * as the replacements and the rest are ignored.
     */
    @Test
    public void replaceN_MoreValuesProvided() {
        String query = "key2=aa&key4=yy&key2=bb&key3=value3&key2=cc&key4=value5&key2=dd";
        String expected = "key2=x%201&key4=yy&key2=x%202&key3=value3&key2=x3&key4=value5&key2=x4";

        HttpServletRequest mockHttpRequest = mock(HttpServletRequest.class);
        when(mockHttpRequest.getQueryString()).thenReturn(query);

        QueryStringHelper helper = new QueryStringHelper();

        ArrayList<Object> values = new ArrayList<>(Arrays.asList("x 1", "x 2", "x3", "x4", "x5", "x6", "x7"));

        String result = helper.replaceN(mockHttpRequest, "key2", values);

        assertThat(result).isEqualTo(expected);

        verify(mockHttpRequest, times(1)).getQueryString();
        verifyNoMoreInteractions(mockHttpRequest);
    }

    /**
     * If there are 4 occurrences of key2 but only 2 values are provided, only the first 2 values are used
     * as the replacements and the rest are ignored. This means the final 2 key2 values remain unchanged.
     */
    @Test
    public void replaceN_LessValuesProvided() {
        String query = "key2=aa&key4=yy&key2=bb&key3=value3&key2=cc&key4=value5&key2=dd";
        String expected = "key2=x%201&key4=yy&key2=x%202&key3=value3&key2=cc&key4=value5&key2=dd";

        HttpServletRequest mockHttpRequest = mock(HttpServletRequest.class);
        when(mockHttpRequest.getQueryString()).thenReturn(query);

        QueryStringHelper helper = new QueryStringHelper();

        ArrayList<Object> values = new ArrayList<>(Arrays.asList("x 1", "x 2"));

        String result = helper.replaceN(mockHttpRequest, "key2", values);

        assertThat(result).isEqualTo(expected);

        verify(mockHttpRequest, times(1)).getQueryString();
        verifyNoMoreInteractions(mockHttpRequest);
    }

    /**
     * If there are 4 occurrences of key2 and 4 values are provided, all 4 values are replaced
     */
    @Test
    public void replaceN_ExactValuesProvided() {
        String query = "key2=aa&key4=yy&key2=bb&key3=value3&key2=cc&key4=value5&key2=dd";
        String expected = "key2=x1&key4=yy&key2=x2&key3=value3&key2=x%203&key4=value5&key2=x%204";

        HttpServletRequest mockHttpRequest = mock(HttpServletRequest.class);
        when(mockHttpRequest.getQueryString()).thenReturn(query);

        QueryStringHelper helper = new QueryStringHelper();

        ArrayList<Object> values = new ArrayList<>(Arrays.asList("x1", "x2", "x 3", "x 4"));

        String result = helper.replaceN(mockHttpRequest, "key2", values);

        assertThat(result).isEqualTo(expected);

        verify(mockHttpRequest, times(1)).getQueryString();
        verifyNoMoreInteractions(mockHttpRequest);
    }

    /**
     * Confirm the query string is being escaped
     */
    @Test
    public void replaceN_ReplacesWithEscaping() {
        String query = "key2=aa&key4=yy&key2=bb&key3=value3&key2=cc&key4=value5&key2=dd";
        String expected = "key2=x%20%201&key4=yy&key2=%20x%202%20&key3=value3&key2=x%203&key4=value5&key2=%20%20x%204";

        HttpServletRequest mockHttpRequest = mock(HttpServletRequest.class);
        when(mockHttpRequest.getQueryString()).thenReturn(query);

        QueryStringHelper helper = new QueryStringHelper();

        ArrayList<Object> values = new ArrayList<>(Arrays.asList("x  1", " x 2 ", "x 3", "  x 4"));

        String result = helper.replaceN(mockHttpRequest, "key2", values);

        assertThat(result).isEqualTo(expected);

        verify(mockHttpRequest, times(1)).getQueryString();
        verifyNoMoreInteractions(mockHttpRequest);
    }
}
