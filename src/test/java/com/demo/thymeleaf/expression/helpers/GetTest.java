package com.demo.thymeleaf.expression.helpers;

import com.demo.thymeleaf.expression.QueryStringHelper;
import org.junit.Test;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

public class GetTest {

    /**
     * When the key is not found, the original query string should be returned.
     */
    @Test
    public void getFirstValue_KeyNotFound_ReturnNull() {
        String query = "key4=ValueA&key2=ValueB&key3=ValueC&key2=ValueD&key4=ValueE&key2=ValueB";

        HttpServletRequest mockHttpRequest = mock(HttpServletRequest.class);
        when(mockHttpRequest.getQueryString()).thenReturn(query);

        QueryStringHelper helper = new QueryStringHelper();
        String result = helper.getFirstValue(mockHttpRequest, "missing");
        assertThat(result).isNull();

        verify(mockHttpRequest, times(1)).getQueryString();
        verifyNoMoreInteractions(mockHttpRequest);
    }

    /**
     * When the key argument is null, the original query string should be returned.
     */
    @Test
    public void getFirstValue_HandlesNull_ReturnNull() {
        String query = "key4=ValueA&key2=ValueB&key3=ValueC&key2=ValueD&key4=ValueE&key2=ValueB";

        HttpServletRequest mockHttpRequest = mock(HttpServletRequest.class);
        when(mockHttpRequest.getQueryString()).thenReturn(query);

        QueryStringHelper helper = new QueryStringHelper();
        String result = helper.getFirstValue(mockHttpRequest, null);
        assertThat(result).isNull();

        verify(mockHttpRequest, times(1)).getQueryString();
        verifyNoMoreInteractions(mockHttpRequest);
    }

    /**
     * key2 occurs 2 times with ValueB and ValueC. Only the first occurrence of ValueB should be returned.
     */
    @Test
    public void getFirstValue_DuplicateKeys_OnlyFirstValueReturned() {
        String query = "key4=ValueA&key2=ValueB&key3=ValueC&key2=ValueD&key4=ValueE&key2=ValueC";
        String expected = "ValueB";

        HttpServletRequest mockHttpRequest = mock(HttpServletRequest.class);
        when(mockHttpRequest.getQueryString()).thenReturn(query);

        QueryStringHelper helper = new QueryStringHelper();
        String result = helper.getFirstValue(mockHttpRequest, "key2");
        assertThat(result).isEqualTo(expected);

        verify(mockHttpRequest, times(1)).getQueryString();
        verifyNoMoreInteractions(mockHttpRequest);
    }

    /**
     * Asserts escaping query string works as expected.
     */
    @Test
    public void getFirstValue_DuplicateKeys_OnlyFirstValueReturned_WithEscaping() {
        String query = "key4=ValueA&key2=Value%20B&key3=ValueC&key2=ValueD&key4=ValueE&key2=Value%20C";
        String expected = "Value B";

        HttpServletRequest mockHttpRequest = mock(HttpServletRequest.class);
        when(mockHttpRequest.getQueryString()).thenReturn(query);

        QueryStringHelper helper = new QueryStringHelper();
        String result = helper.getFirstValue(mockHttpRequest, "key2");
        assertThat(result).isEqualTo(expected);

        verify(mockHttpRequest, times(1)).getQueryString();
        verifyNoMoreInteractions(mockHttpRequest);
    }

    /**
     * key2 occurs 2 times both with ValueB. Only the first occurrence of ValueB should be returned.
     * Note: This is more of an assumption given its not known which relative key was used given the values
     * are the same.
     */
    @Test
    public void getFirstValue_DuplicateKeys_WithSameValue_OnlyFirstValueReturned() {
        String query = "key4=ValueA&key2=Value%20B&key3=ValueC&key2=ValueD&key4=ValueE&key2=Value%20B";
        String expected = "Value B";

        HttpServletRequest mockHttpRequest = mock(HttpServletRequest.class);
        when(mockHttpRequest.getQueryString()).thenReturn(query);

        QueryStringHelper helper = new QueryStringHelper();
        String result = helper.getFirstValue(mockHttpRequest, "key2");
        assertThat(result).isEqualTo(expected);

        verify(mockHttpRequest, times(1)).getQueryString();
        verifyNoMoreInteractions(mockHttpRequest);
    }

    /**
     * When the key is not found, the original query string should be returned.
     */
    @Test
    public void getAllValues_KeyNotFound_EmptyListReturned() {
        String query = "key4=ValueA&key2=Value%20B&key3=ValueC&key2=ValueD&key4=ValueE&key2=Value%20B";

        HttpServletRequest mockHttpRequest = mock(HttpServletRequest.class);
        when(mockHttpRequest.getQueryString()).thenReturn(query);

        QueryStringHelper helper = new QueryStringHelper();
        List<String> result = helper.getAllValues(mockHttpRequest, "missing");
        assertThat(result).isEmpty();

        verify(mockHttpRequest, times(1)).getQueryString();
        verifyNoMoreInteractions(mockHttpRequest);
    }

    /**
     * When the key argument is null, the original query string should be returned.
     */
    @Test
    public void getAllValues_HandlesNull_EmptyListReturned() {
        String query = "key4=ValueA&key2=Value%20B&key3=ValueC&key2=ValueD&key4=ValueE&key2=Value%20B";

        HttpServletRequest mockHttpRequest = mock(HttpServletRequest.class);
        when(mockHttpRequest.getQueryString()).thenReturn(query);

        QueryStringHelper helper = new QueryStringHelper();
        List<String> result = helper.getAllValues(mockHttpRequest, null);
        assertThat(result).isEmpty();

        verify(mockHttpRequest, times(1)).getQueryString();
        verifyNoMoreInteractions(mockHttpRequest);
    }

    /**
     * key2 occurs 3 times both with the values ValueX,ValueY,ValueZ. The expected result is a list containing
     * the same 3 values in the exact same order as the relative indexes.
     */
    @Test
    public void getAllValues_DuplicateKeys_AllValuesReturnedInTheSameOrder() {
        String query = "key4=ValueA&key2=ValueX&key3=ValueC&key2=ValueY&key4=ValueE&key2=ValueZ";
        List<String> expected = new ArrayList<>(Arrays.asList("ValueX", "ValueY", "ValueZ"));

        HttpServletRequest mockHttpRequest = mock(HttpServletRequest.class);
        when(mockHttpRequest.getQueryString()).thenReturn(query);

        QueryStringHelper helper = new QueryStringHelper();
        List<String> result = helper.getAllValues(mockHttpRequest, "key2");
        assertThat(result).isEqualTo(expected);

        verify(mockHttpRequest, times(1)).getQueryString();
        verifyNoMoreInteractions(mockHttpRequest);
    }

    /**
     * key2 occurs 3 times both with ValueB,ValueB,ValueB. The expected result is a list containing these
     * 3 values in the same order as the relative position in the query string.
     *
     * This test confirms query string escaping of white space etc is working.
     */
    @Test
    public void getAllValues_DuplicateKeys_AllValuesReturnedInTheSameOrder_WithEscaping() {
        String query = "key4=ValueA&key2=Value%20B%20&key3=ValueC&key2=%20%20Value%20D&key4=ValueE&key2=%20%20%20Value%20E";
        List<String> expected = new ArrayList<>(Arrays.asList("Value B ", "  Value D", "   Value E"));

        HttpServletRequest mockHttpRequest = mock(HttpServletRequest.class);
        when(mockHttpRequest.getQueryString()).thenReturn(query);

        QueryStringHelper helper = new QueryStringHelper();
        List<String> result = helper.getAllValues(mockHttpRequest, "key2");
        assertThat(result).isEqualTo(expected);

        verify(mockHttpRequest, times(1)).getQueryString();
        verifyNoMoreInteractions(mockHttpRequest);
    }

    /**
     * key8 occurs only once, therefore the resulting list should only contain ValueA.
     */
    @Test
    public void getAllValues_SingleKey_ReturnsSingleValue() {
        String query = "key4=ValueA&key2=ValueB&key3=ValueC&key8=%20%20Value8%20833.34.384%20%205&key2=ValueD&key4=ValueE&key2=ValueC";
        List<String> expected = new ArrayList<>(Collections.singletonList("  Value8 833.34.384  5"));

        HttpServletRequest mockHttpRequest = mock(HttpServletRequest.class);
        when(mockHttpRequest.getQueryString()).thenReturn(query);

        QueryStringHelper helper = new QueryStringHelper();
        List<String> result = helper.getAllValues(mockHttpRequest, "key8");
        assertThat(result).isEqualTo(expected);

        verify(mockHttpRequest, times(1)).getQueryString();
        verifyNoMoreInteractions(mockHttpRequest);
    }
}
