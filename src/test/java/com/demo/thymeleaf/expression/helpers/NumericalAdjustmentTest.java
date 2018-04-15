package com.demo.thymeleaf.expression.helpers;

import com.demo.thymeleaf.expression.QueryStringHelper;
import org.junit.Test;

import javax.servlet.http.HttpServletRequest;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.verifyNoMoreInteractions;

public class NumericalAdjustmentTest {

    /**
     * When the key is missing, the original query string should be returned.
     */
    @Test
    public void adjustNumericValueBy_MissingKey_HasNoEffect() {
        String query = "key4=abc&key6=xyz&key8=100";

        HttpServletRequest mockHttpRequest = mock(HttpServletRequest.class);
        when(mockHttpRequest.getQueryString()).thenReturn(query);

        List<Object> relativeIndexes = new ArrayList<>();
        relativeIndexes.add("0");

        QueryStringHelper helper = new QueryStringHelper();
        String result = helper.adjustNumericValueBy(mockHttpRequest, "missing", relativeIndexes, 1);
        assertThat(result).isEqualTo(query);

        verify(mockHttpRequest, times(1)).getQueryString();
        verifyNoMoreInteractions(mockHttpRequest);
    }

    /**
     * When the key is null, the original query string should be returned.
     */
    @Test
    public void adjustNumericValueBy_HandlesNull_HasNoEffect() {
        String query = "key4=abc&key6=xyz&key8=100";

        HttpServletRequest mockHttpRequest = mock(HttpServletRequest.class);
        when(mockHttpRequest.getQueryString()).thenReturn(query);

        List<Object> relativeIndexes = new ArrayList<>();
        relativeIndexes.add("0");

        QueryStringHelper helper = new QueryStringHelper();
        String result = helper.adjustNumericValueBy(mockHttpRequest, null, relativeIndexes, 0);
        assertThat(result).isEqualTo(query);

        verify(mockHttpRequest, times(1)).getQueryString();
        verifyNoMoreInteractions(mockHttpRequest);
    }

    /**
     * When the value type is not an integer it should be ignored and the original
     * query string returned.
     */
    @Test
    public void adjustNumericValueBy_IncompatibleValueType() {
        String query = "key4=abc&key6=xyz&key8=100";

        HttpServletRequest mockHttpRequest = mock(HttpServletRequest.class);
        when(mockHttpRequest.getQueryString()).thenReturn(query);

        List<Object> relativeIndexes = new ArrayList<>();
        relativeIndexes.add("0");

        QueryStringHelper helper = new QueryStringHelper();
        String result = helper.adjustNumericValueBy(mockHttpRequest, "key6", relativeIndexes, 1);
        assertThat(result).isEqualTo(query);

        verify(mockHttpRequest, times(1)).getQueryString();
        verifyNoMoreInteractions(mockHttpRequest);
    }

    /**
     * When the value type is an integer it should be adjusted by the given value.
     * <p>
     * Adjust by 0 = no change and return original query string
     */
    @Test
    public void adjustNumericValueBy_ByValueIsZero_HasNoEffect() {
        String query = "key4=abc&key6=xyz&key8=100&key9=fgy";

        HttpServletRequest mockHttpRequest = mock(HttpServletRequest.class);
        when(mockHttpRequest.getQueryString()).thenReturn(query);

        List<Object> relativeIndexes = new ArrayList<>();
        relativeIndexes.add("0");

        QueryStringHelper helper = new QueryStringHelper();
        String result = helper.adjustNumericValueBy(mockHttpRequest, "key8", relativeIndexes, 0);
        assertThat(result).isEqualTo(query);

        verify(mockHttpRequest, times(1)).getQueryString();
        verifyNoMoreInteractions(mockHttpRequest);
    }

    /**
     * When the value type is an integer it should be adjusted by the given value.
     * <p>
     * Adjust by 5 = increment by 5 given 5 is a positive number.
     */
    @Test
    public void adjustNumericValueBy_IncrementsByValue() {
        String query = "key4=abc&key6=xyz&key8=100&key9=fgy";
        String expected = "key4=abc&key6=xyz&key8=105&key9=fgy";

        HttpServletRequest mockHttpRequest = mock(HttpServletRequest.class);
        when(mockHttpRequest.getQueryString()).thenReturn(query);

        List<Object> relativeIndexes = new ArrayList<>();
        relativeIndexes.add("0");

        QueryStringHelper helper = new QueryStringHelper();
        String result = helper.adjustNumericValueBy(mockHttpRequest, "key8", relativeIndexes, 5);
        assertThat(result).isEqualTo(expected);

        verify(mockHttpRequest, times(1)).getQueryString();
        verifyNoMoreInteractions(mockHttpRequest);
    }

    /**
     * When the value type is an integer it should be adjusted by the given value.
     * <p>
     * Adjust by -5 = decrement by 5 given 5 is a negative number.
     */
    @Test
    public void adjustNumericValueBy_DecrementsByValue() {
        String query = "key4=abc&key6=xyz&key8=100&key9=fgy";
        String expected = "key4=abc&key6=xyz&key8=95&key9=fgy";

        HttpServletRequest mockHttpRequest = mock(HttpServletRequest.class);
        when(mockHttpRequest.getQueryString()).thenReturn(query);

        List<Object> relativeIndexes = new ArrayList<>();
        relativeIndexes.add("0");

        QueryStringHelper helper = new QueryStringHelper();
        String result = helper.adjustNumericValueBy(mockHttpRequest, "key8", relativeIndexes, -5);
        assertThat(result).isEqualTo(expected);

        verify(mockHttpRequest, times(1)).getQueryString();
        verifyNoMoreInteractions(mockHttpRequest);
    }

    /**
     * When multiple relative indexes are provided, the value is updated by the new amount only if its numeric
     * otherwise it should be ignored.
     * <p>
     * Eg: key8 = [100, 23, 'hello_world-53', -5, '23-about', 80]
     *
     * This test updates index 1, 2, 3, 4 to make sure it only selectively updates numeric values.
     */
    @Test
    public void adjustNumericValueBy_ModifiesMultipleNumericKeys() {
        String query = "key4=ab%20c&key8=100&key9=fgy&key8=23&key11=50&key8=hello_world-53&key8=-5&key8=23-about&key8=80";
        String expected = "key4=ab%20c&key8=100&key9=fgy&key8=24&key11=50&key8=hello_world-53&key8=-4&key8=23-about&key8=80";

        HttpServletRequest mockHttpRequest = mock(HttpServletRequest.class);
        when(mockHttpRequest.getQueryString()).thenReturn(query);

        List<Object> relativeIndexes = new ArrayList<>();
        relativeIndexes.add("1");
        relativeIndexes.add("2");
        relativeIndexes.add("3");
        relativeIndexes.add("4");

        QueryStringHelper helper = new QueryStringHelper();
        String result = helper.adjustNumericValueBy(mockHttpRequest, "key8", relativeIndexes, 1);
        assertThat(result).isEqualTo(expected);

        verify(mockHttpRequest, times(1)).getQueryString();
        verifyNoMoreInteractions(mockHttpRequest);
    }

    /**
     * When all relative indexes are provided, the value is updated by the new amount only if its numeric
     * otherwise it should be ignored. Indexes outside of array bounds should be ignored.
     * <p>
     * Eg: key8 = [100, 23, 'hello_world-53', -5, '23-about', 80]
     */
    @Test
    public void adjustNumericValueBy_ModifiesAllNumericKeys_InvalidIndexesIgnored() {
        String query = "key4=ab%20c&key8=100&key9=fgy&key8=23&key11=50&key8=hello_world-53&key8=-5&key8=23-about&key8=80";
        String expected = "key4=ab%20c&key8=101&key9=fgy&key8=24&key11=50&key8=hello_world-53&key8=-4&key8=23-about&key8=81";

        HttpServletRequest mockHttpRequest = mock(HttpServletRequest.class);
        when(mockHttpRequest.getQueryString()).thenReturn(query);

        List<Object> relativeIndexes = new ArrayList<>();
        relativeIndexes.add("-1");
        relativeIndexes.add("0");
        relativeIndexes.add("1");
        relativeIndexes.add("2");
        relativeIndexes.add("3");
        relativeIndexes.add("4");
        relativeIndexes.add("5");
        relativeIndexes.add("6");

        QueryStringHelper helper = new QueryStringHelper();
        String result = helper.adjustNumericValueBy(mockHttpRequest, "key8", relativeIndexes, 1);
        assertThat(result).isEqualTo(expected);

        verify(mockHttpRequest, times(1)).getQueryString();
        verifyNoMoreInteractions(mockHttpRequest);
    }

    /**
     * When the 'page' key is not found, the original query string should be returned.
     */
    @Test
    public void incrementPage_PageKeyNotFound_HasNoEffect() {
        String query = "key4=ab%20c&key8=100&key9=fgy&key8=23&key11=50&key8=hello_world-53&key8=-5&key8=23-about&key8=80";

        HttpServletRequest mockHttpRequest = mock(HttpServletRequest.class);
        when(mockHttpRequest.getQueryString()).thenReturn(query);

        QueryStringHelper helper = new QueryStringHelper();
        String result = helper.incrementPage(mockHttpRequest);
        assertThat(result).isEqualTo(query);

        verify(mockHttpRequest, times(1)).getQueryString();
        verifyNoMoreInteractions(mockHttpRequest);
    }

    /**
     * When the 'page' key is found its value should be incremented by one.
     */
    @Test
    public void incrementPage_PageKeyIncrementedByOne() {
        String query = "key4=ab%20c&key8=100&page=0&key9=fgy&key8=23&key11=50&key8=hello_world-53&key8=-5&key8=23-about&key8=80";
        String expected = "key4=ab%20c&key8=100&page=1&key9=fgy&key8=23&key11=50&key8=hello_world-53&key8=-5&key8=23-about&key8=80";

        HttpServletRequest mockHttpRequest = mock(HttpServletRequest.class);
        when(mockHttpRequest.getQueryString()).thenReturn(query);

        QueryStringHelper helper = new QueryStringHelper();
        String result = helper.incrementPage(mockHttpRequest);
        assertThat(result).isEqualTo(expected);

        verify(mockHttpRequest, times(1)).getQueryString();
        verifyNoMoreInteractions(mockHttpRequest);
    }

    /**
     * If for whatever reason the page value is non numeric, incrementing should do nothing and return original query string.
     */
    @Test
    public void incrementPage_PageKeyNotNumeric_HasNoEffect() {
        String query = "key4=ab%20c&key8=100&page=X-Y-Z&key9=fgy&key8=23&key11=50&key8=hello_world-53&key8=-5&key8=23-about&key8=80";
        String expected = "key4=ab%20c&key8=100&page=X-Y-Z&key9=fgy&key8=23&key11=50&key8=hello_world-53&key8=-5&key8=23-about&key8=80";

        HttpServletRequest mockHttpRequest = mock(HttpServletRequest.class);
        when(mockHttpRequest.getQueryString()).thenReturn(query);

        QueryStringHelper helper = new QueryStringHelper();
        String result = helper.incrementPage(mockHttpRequest);
        assertThat(result).isEqualTo(expected);

        verify(mockHttpRequest, times(1)).getQueryString();
        verifyNoMoreInteractions(mockHttpRequest);
    }

    /**
     * When the 'page' key is not found, the original query string should be returned.
     */
    @Test
    public void decrementPage_PageKeyNotFound_HasNoEffect() {
        String query = "key4=ab%20c&key8=100&key9=fgy&key8=23&key11=50&key8=hello_world-53&key8=-5&key8=23-about&key8=80";

        HttpServletRequest mockHttpRequest = mock(HttpServletRequest.class);
        when(mockHttpRequest.getQueryString()).thenReturn(query);

        QueryStringHelper helper = new QueryStringHelper();
        String result = helper.decrementPage(mockHttpRequest);
        assertThat(result).isEqualTo(query);

        verify(mockHttpRequest, times(1)).getQueryString();
        verifyNoMoreInteractions(mockHttpRequest);
    }

    /**
     * When the 'page' key is found its value should be decremented by one ONLY if its value is greater than 0.
     */
    @Test
    public void decrementPage_PageKey_GreaterThanZero_DecrementedByOne() {
        String query = "key4=ab%20c&key8=100&page=1&key9=fgy&key8=23&key11=50&key8=hello_world-53&key8=-5&key8=23-about&key8=80";
        String expected = "key4=ab%20c&key8=100&page=0&key9=fgy&key8=23&key11=50&key8=hello_world-53&key8=-5&key8=23-about&key8=80";

        HttpServletRequest mockHttpRequest = mock(HttpServletRequest.class);
        when(mockHttpRequest.getQueryString()).thenReturn(query);

        QueryStringHelper helper = new QueryStringHelper();
        String result = helper.decrementPage(mockHttpRequest);
        assertThat(result).isEqualTo(expected);

        verify(mockHttpRequest, times(1)).getQueryString();
        verifyNoMoreInteractions(mockHttpRequest);
    }

    /**
     * When the 'page' key is found its value should be decremented by one ONLY if its value is greater than 0.
     * This ensure page can only have min value of 0.
     */
    @Test
    public void decrementPage_PageKey_IsZero_ShouldNotBeDecrementedByOne() {
        String query = "key4=ab%20c&key8=100&page=0&key9=fgy&key8=23&key11=50&key8=hello_world-53&key8=-5&key8=23-about&key8=80";
        String expected = "key4=ab%20c&key8=100&page=0&key9=fgy&key8=23&key11=50&key8=hello_world-53&key8=-5&key8=23-about&key8=80";

        HttpServletRequest mockHttpRequest = mock(HttpServletRequest.class);
        when(mockHttpRequest.getQueryString()).thenReturn(query);

        QueryStringHelper helper = new QueryStringHelper();
        String result = helper.decrementPage(mockHttpRequest);
        assertThat(result).isEqualTo(expected);

        verify(mockHttpRequest, times(1)).getQueryString();
        verifyNoMoreInteractions(mockHttpRequest);
    }

    /**
     * If for whatever reason the page value is non numeric, decrementing should do nothing and return original query string.
     */
    @Test
    public void decrementPage_PageKeyNotNumeric_HasNoEffect() {
        String query = "key4=ab%20c&key8=100&page=X-Y-Z&key9=fgy&key8=23&key11=50&key8=hello_world-53&key8=-5&key8=23-about&key8=80";
        String expected = "key4=ab%20c&key8=100&page=X-Y-Z&key9=fgy&key8=23&key11=50&key8=hello_world-53&key8=-5&key8=23-about&key8=80";

        HttpServletRequest mockHttpRequest = mock(HttpServletRequest.class);
        when(mockHttpRequest.getQueryString()).thenReturn(query);

        QueryStringHelper helper = new QueryStringHelper();
        String result = helper.decrementPage(mockHttpRequest);
        assertThat(result).isEqualTo(expected);

        verify(mockHttpRequest, times(1)).getQueryString();
        verifyNoMoreInteractions(mockHttpRequest);
    }
}
