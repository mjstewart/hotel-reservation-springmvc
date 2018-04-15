package com.demo.thymeleaf.expression.helpers;

import com.demo.thymeleaf.expression.QueryStringHelper;
import com.demo.thymeleaf.utils.SortDirection;
import org.junit.Test;

import javax.servlet.http.HttpServletRequest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.verifyNoMoreInteractions;

public class SpringHelperTest {
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

    // need to test whitespace between comma shouldnt matter. sort=country  , asc
    // what happens when sort == NONE ?
    @Test
    public void setSortDirection() {
        String query = "city=san%20francisco&region=west&sort=country&locale=us-east&transit=late%20urgent%20off-peak";

        HttpServletRequest mockHttpRequest = mock(HttpServletRequest.class);
        when(mockHttpRequest.getQueryString()).thenReturn(query);

        QueryStringHelper helper = new QueryStringHelper();
        String result = helper.setSortDirection(mockHttpRequest, "country", SortDirection.ASC);
        assertThat(result).isEqualTo(query);

        verify(mockHttpRequest, times(1)).getQueryString();
        verifyNoMoreInteractions(mockHttpRequest);
    }


//    /**
//     * When there is no sort key, the original query string should be returned.
//     */
//    @Test
//    public void toggleSortDirection_NoSortKey_HasNoEffect() {
//        String query = "city=san%20francisco&region=west&locale&us-east&transit=late%20urgent%20off-peak";
//
//        HttpServletRequest mockHttpRequest = mock(HttpServletRequest.class);
//        when(mockHttpRequest.getQueryString()).thenReturn(query);
//
//        QueryStringHelper helper = new QueryStringHelper();
//        String result = helper.toggleSortDirection(mockHttpRequest, "country");
//        assertThat(result).isEqualTo(query);
//
//        verify(mockHttpRequest, times(1)).getQueryString();
//        verifyNoMoreInteractions(mockHttpRequest);
//    }
//
//    /**
//     * Springs default is ascending order
//     */
//    @Test
//    public void toggleSortDirection_SortFieldHasNoOrder_ImpliedAsc_GetsChangedToDesc() {
//        String query = "city=san%20francisco&region=west&sort=country&locale&us-east&transit=late%20urgent%20off-peak";
//
//        HttpServletRequest mockHttpRequest = mock(HttpServletRequest.class);
//        when(mockHttpRequest.getQueryString()).thenReturn(query);
//
//        QueryStringHelper helper = new QueryStringHelper();
//        String result = helper.decrementPage(mockHttpRequest);
//        assertThat(result).isEqualTo(query);
//
//        verify(mockHttpRequest, times(1)).getQueryString();
//        verifyNoMoreInteractions(mockHttpRequest);
//    }
}
