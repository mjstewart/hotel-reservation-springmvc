package com.demo.thymeleaf.expression.helpers;

import com.demo.thymeleaf.expression.QueryStringHelper;
import org.junit.Test;

import javax.servlet.http.HttpServletRequest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
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


    /*
     * Note for sorting tests.
     *
     * The spring syntax for sorting multiple fields is simply using the sort key multiple times.
     *
     * ?sort=country,desc&sort=state,asc&sort=city,desc
     *
     * The implementation automatically uses the 'sort' key by convention so the field needs to be specified.
     * Additionally the only valid sort keys are 'asc' and 'desc' based on spring convention.
     */

    /**
     * When there is no sort key, the original query string should be returned.
     */
    @Test
    public void setSortDirection_MissingSortKey_HasNoEffect() {
        String query = "city=san%20francisco&region=west&locale=us-east";

        HttpServletRequest mockHttpRequest = mock(HttpServletRequest.class);
        when(mockHttpRequest.getQueryString()).thenReturn(query);

        QueryStringHelper helper = new QueryStringHelper();
        String result = helper.setSortDirection(mockHttpRequest, "country", "asc");
        assertThat(result).isEqualTo(query);

        verify(mockHttpRequest, times(1)).getQueryString();
        verifyNoMoreInteractions(mockHttpRequest);
    }

    /**
     * When the sort arguments are null, the original query should be returned.
     */
    @Test
    public void setSortDirection_HandlesNullArgs() {
        String query = "city=san%20francisco&region=west&locale=us-east";

        HttpServletRequest mockHttpRequest = mock(HttpServletRequest.class);
        when(mockHttpRequest.getQueryString()).thenReturn(query);

        QueryStringHelper helper = new QueryStringHelper();
        String result = helper.setSortDirection(mockHttpRequest, null, null);
        assertThat(result).isEqualTo(query);

        verify(mockHttpRequest, times(1)).getQueryString();
        verifyNoMoreInteractions(mockHttpRequest);
    }

    /**
     * When the sort arguments are null, the original query should be returned.
     */
    @Test
    public void setSortDirection_HandlesEmpty() {
        String query = "city=san%20francisco&region=west&locale=us-east";

        HttpServletRequest mockHttpRequest = mock(HttpServletRequest.class);
        when(mockHttpRequest.getQueryString()).thenReturn(query);

        QueryStringHelper helper = new QueryStringHelper();
        String result = helper.setSortDirection(mockHttpRequest, "", "");
        assertThat(result).isEqualTo(query);

        verify(mockHttpRequest, times(1)).getQueryString();
        verifyNoMoreInteractions(mockHttpRequest);
    }

    /**
     * If a sort direction other than {@code asc, desc} (case sensitive) is supplied then an exception should be thrown.
     */
    @Test
    public void setSortDirection_IllegalSortDirection_ThrowsException() {
        String query = "city=san%20francisco&region=west&locale=us-east";

        HttpServletRequest mockHttpRequest = mock(HttpServletRequest.class);
        when(mockHttpRequest.getQueryString()).thenReturn(query);

        QueryStringHelper helper = new QueryStringHelper();

        assertThatIllegalArgumentException()
                .isThrownBy(() -> helper.setSortDirection(mockHttpRequest, "country", "ASCENDING"));

        verifyZeroInteractions(mockHttpRequest);
    }

    /**
     * When the existing field has no direction, the new value should have the sort direction appended
     * with a comma. {@code sort=country,asc}
     */
    @Test
    public void setSortDirection_SingleSortField_NoDirection() {
        String query = "city=san%20francisco&region=west&sort=country&locale=us-east";
        String expected = "city=san%20francisco&region=west&sort=country,asc&locale=us-east";

        HttpServletRequest mockHttpRequest = mock(HttpServletRequest.class);
        when(mockHttpRequest.getQueryString()).thenReturn(query);

        QueryStringHelper helper = new QueryStringHelper();
        String result = helper.setSortDirection(mockHttpRequest, "country", "asc");
        assertThat(result).isEqualTo(expected);

        verify(mockHttpRequest, times(1)).getQueryString();
        verifyNoMoreInteractions(mockHttpRequest);
    }

    /**
     * When the existing field already has a direction, the new value should consist of the existing sort field and
     * new sort direction separated by a comma. {@code sort=country,desc}
     */
    @Test
    public void setSortDirection_SingleSortField_WithDirection() {
        String query = "city=san%20francisco&region=west&sort=country,asc&locale=us-east";
        String expected = "city=san%20francisco&region=west&sort=country,desc&locale=us-east";

        HttpServletRequest mockHttpRequest = mock(HttpServletRequest.class);
        when(mockHttpRequest.getQueryString()).thenReturn(query);

        QueryStringHelper helper = new QueryStringHelper();
        String result = helper.setSortDirection(mockHttpRequest, "country", "desc");
        assertThat(result).isEqualTo(expected);

        verify(mockHttpRequest, times(1)).getQueryString();
        verifyNoMoreInteractions(mockHttpRequest);
    }

    /**
     * When the existing field already has a direction, the new value should consist of the existing sort field and
     * new sort direction separated by a comma. {@code sort=country,desc}
     *
     * This test checks that white space should be ignored. {@code sort= country   ,   asc) should be treated
     * the same as {@code sort=country,asc}.
     */
    @Test
    public void setSortDirection_SingleSortField_WithDirection_TrimSpace() {
        String query = "city=san%20francisco&region=west&sort=%20country%20%20,%20asc%20&locale=us-east";
        String expected = "city=san%20francisco&region=west&sort=country,desc&locale=us-east";

        HttpServletRequest mockHttpRequest = mock(HttpServletRequest.class);
        when(mockHttpRequest.getQueryString()).thenReturn(query);

        QueryStringHelper helper = new QueryStringHelper();
        String result = helper.setSortDirection(mockHttpRequest, "country", "desc");
        assertThat(result).isEqualTo(expected);

        verify(mockHttpRequest, times(1)).getQueryString();
        verifyNoMoreInteractions(mockHttpRequest);
    }

    /**
     * When the existing field has a direction, the new value should have the sort direction appended
     * with a comma. sort=country,desc.
     *
     * This test checks that irregular formatting should not matter such as having a trailing comma.
     * {@code sort=country,} should isolate 'country' directly.
     */
    @Test
    public void setSortDirection_SingleSortField_WithDirection_IgnoreSyntaxIssues() {
        String query = "city=san%20francisco&region=west&sort=country,&locale=us-east";
        String expected = "city=san%20francisco&region=west&sort=country,desc&locale=us-east";

        HttpServletRequest mockHttpRequest = mock(HttpServletRequest.class);
        when(mockHttpRequest.getQueryString()).thenReturn(query);

        QueryStringHelper helper = new QueryStringHelper();
        String result = helper.setSortDirection(mockHttpRequest, "country", "desc");
        assertThat(result).isEqualTo(expected);

        verify(mockHttpRequest, times(1)).getQueryString();
        verifyNoMoreInteractions(mockHttpRequest);
    }

    /**
     * When the existing field has a direction and the new direction is the same, the new query string should
     * be the same as the original.
     */
    @Test
    public void setSortDirection_SingleSortField_WithDirection_SetSameDirection() {
        String query = "city=san%20francisco&region=west&sort=country,desc&locale=us-east";

        HttpServletRequest mockHttpRequest = mock(HttpServletRequest.class);
        when(mockHttpRequest.getQueryString()).thenReturn(query);

        QueryStringHelper helper = new QueryStringHelper();
        String result = helper.setSortDirection(mockHttpRequest, "country", "desc");
        assertThat(result).isEqualTo(query);

        verify(mockHttpRequest, times(1)).getQueryString();
        verifyNoMoreInteractions(mockHttpRequest);
    }

    /**
     * The sort field 'country' is sorted based on property syntax. This test is to ensure the sort field
     * and sort direction are being correctly identified.
     */
    @Test
    public void setSortDirection_SingleSortField_WithSortFieldProperty() {
        String query = "city=san%20francisco&region=west&sort=country.name.id,desc&locale=us-east";
        String expected = "city=san%20francisco&region=west&sort=country.name.id,asc&locale=us-east";

        HttpServletRequest mockHttpRequest = mock(HttpServletRequest.class);
        when(mockHttpRequest.getQueryString()).thenReturn(query);

        QueryStringHelper helper = new QueryStringHelper();
        String result = helper.setSortDirection(mockHttpRequest, "country.name.id", "asc");
        assertThat(result).isEqualTo(expected);

        verify(mockHttpRequest, times(1)).getQueryString();
        verifyNoMoreInteractions(mockHttpRequest);
    }

    /**
     * When there are many sort fields, only the supplied sort field should be changed.
     */
    @Test
    public void setSortDirection_ManySortFields_UpdatesTargetFieldOnly() {
        String query = "city=san%20francisco&region=west&sort=country&sort=state,desc&locale=us-east&sort=city,asc";
        String expected = "city=san%20francisco&region=west&sort=country&sort=state,desc&locale=us-east&sort=city,desc";

        HttpServletRequest mockHttpRequest = mock(HttpServletRequest.class);
        when(mockHttpRequest.getQueryString()).thenReturn(query);

        QueryStringHelper helper = new QueryStringHelper();
        String result = helper.setSortDirection(mockHttpRequest, "city", "desc");
        assertThat(result).isEqualTo(expected);

        verify(mockHttpRequest, times(1)).getQueryString();
        verifyNoMoreInteractions(mockHttpRequest);
    }

    /**
     * When there are many sort fields, only the supplied sort field should be changed. This test applies the same
     * sort direction that already exists and expects the new query string to equal the original query string.
     */
    @Test
    public void setSortDirection_ManySortFields_SameDirection_UpdatesTargetFieldOnly() {
        String query = "city=san%20francisco&region=west&sort=country&sort=state,desc&locale=us-east&sort=city,asc";

        HttpServletRequest mockHttpRequest = mock(HttpServletRequest.class);
        when(mockHttpRequest.getQueryString()).thenReturn(query);

        QueryStringHelper helper = new QueryStringHelper();
        String result = helper.setSortDirection(mockHttpRequest, "city", "asc");
        assertThat(result).isEqualTo(query);

        verify(mockHttpRequest, times(1)).getQueryString();
        verifyNoMoreInteractions(mockHttpRequest);
    }

    /**
     * When there are null arguments, the original query string should be returned.
     */
    @Test
    public void toggleSortDefaultAsc_HandlesNullArgs() {
        String query = "city=san%20francisco&region=west&sort=country&locale=us";

        HttpServletRequest mockHttpRequest = mock(HttpServletRequest.class);
        when(mockHttpRequest.getQueryString()).thenReturn(query);

        QueryStringHelper helper = new QueryStringHelper();
        String result = helper.toggleSortDefaultAsc(mockHttpRequest, null);
        assertThat(result).isEqualTo(query);

        verify(mockHttpRequest, times(1)).getQueryString();
        verifyNoMoreInteractions(mockHttpRequest);
    }

    /**
     * When there are empty arguments, the original query string should be returned.
     */
    @Test
    public void toggleSortDefaultAsc_HandlesEmptyArgs() {
        String query = "city=san%20francisco&region=west&sort=country&locale=us";

        HttpServletRequest mockHttpRequest = mock(HttpServletRequest.class);
        when(mockHttpRequest.getQueryString()).thenReturn(query);

        QueryStringHelper helper = new QueryStringHelper();
        String result = helper.toggleSortDefaultAsc(mockHttpRequest, "");
        assertThat(result).isEqualTo(query);

        verify(mockHttpRequest, times(1)).getQueryString();
        verifyNoMoreInteractions(mockHttpRequest);
    }

    /**
     * When the sort key is missing, the original query string should be returned.
     */
    @Test
    public void toggleSortDefaultAsc_MissingSortKey() {
        String query = "city=san%20francisco&region=west&sort=country&locale=us";

        HttpServletRequest mockHttpRequest = mock(HttpServletRequest.class);
        when(mockHttpRequest.getQueryString()).thenReturn(query);

        QueryStringHelper helper = new QueryStringHelper();
        String result = helper.toggleSortDefaultAsc(mockHttpRequest, "city");
        assertThat(result).isEqualTo(query);

        verify(mockHttpRequest, times(1)).getQueryString();
        verifyNoMoreInteractions(mockHttpRequest);
    }

    /**
     * When the sort field has no direction, the new sort direction should be the opposite
     * of the default direction. In this test the default is 'asc' so the expectation is
     * {@code sort=country,desc}.
     */
    @Test
    public void toggleSortDefaultAsc_NoDirection() {
        String query = "city=san%20francisco&region=west&sort=country&locale=us";
        String expected = "city=san%20francisco&region=west&sort=country,desc&locale=us";

        HttpServletRequest mockHttpRequest = mock(HttpServletRequest.class);
        when(mockHttpRequest.getQueryString()).thenReturn(query);

        QueryStringHelper helper = new QueryStringHelper();
        String result = helper.toggleSortDefaultAsc(mockHttpRequest, "country");
        assertThat(result).isEqualTo(expected);

        verify(mockHttpRequest, times(1)).getQueryString();
        verifyNoMoreInteractions(mockHttpRequest);
    }

    /**
     * When the sort field has an existing direction, the new sort direction should be the
     * opposite. In this test 'country' has direction 'desc' so toggling will change it to 'asc'.
     */
    @Test
    public void toggleSortDefaultAsc_ExistingDirection_ToggleFromDescToAsc() {
        String query = "city=san%20francisco&region=west&sort=country,desc&locale=us";
        String expected = "city=san%20francisco&region=west&sort=country,asc&locale=us";

        HttpServletRequest mockHttpRequest = mock(HttpServletRequest.class);
        when(mockHttpRequest.getQueryString()).thenReturn(query);

        QueryStringHelper helper = new QueryStringHelper();
        String result = helper.toggleSortDefaultAsc(mockHttpRequest, "country");
        assertThat(result).isEqualTo(expected);

        verify(mockHttpRequest, times(1)).getQueryString();
        verifyNoMoreInteractions(mockHttpRequest);
    }

    /**
     * When the sort field has an existing direction, the new sort direction should be the
     * opposite. In this test 'country' has direction 'asc' so toggling will change it to 'desc'.
     */
    @Test
    public void toggleSortDefaultAsc_ExistingDirection_ToggleFromAscToDesc() {
        String query = "city=san%20francisco&region=west&sort=country,asc&locale=us";
        String expected = "city=san%20francisco&region=west&sort=country,desc&locale=us";

        HttpServletRequest mockHttpRequest = mock(HttpServletRequest.class);
        when(mockHttpRequest.getQueryString()).thenReturn(query);

        QueryStringHelper helper = new QueryStringHelper();
        String result = helper.toggleSortDefaultAsc(mockHttpRequest, "country");
        assertThat(result).isEqualTo(expected);

        verify(mockHttpRequest, times(1)).getQueryString();
        verifyNoMoreInteractions(mockHttpRequest);
    }


    /**
     * This test checks that white space should be ignored. {@code sort= country   ,   asc) should be treated
     * the same as {@code sort=country,asc}.
     */
    @Test
    public void toggleSortDefaultAsc_TrimSpace() {
        String query = "city=san%20francisco&region=west&sort=%20%20country%20%20%20%20,%20%20asc%20%20&locale=us";
        String expected = "city=san%20francisco&region=west&sort=country,desc&locale=us";

        HttpServletRequest mockHttpRequest = mock(HttpServletRequest.class);
        when(mockHttpRequest.getQueryString()).thenReturn(query);

        QueryStringHelper helper = new QueryStringHelper();
        String result = helper.toggleSortDefaultAsc(mockHttpRequest, "country");
        assertThat(result).isEqualTo(expected);

        verify(mockHttpRequest, times(1)).getQueryString();
        verifyNoMoreInteractions(mockHttpRequest);
    }

    /**
     * This test checks that irregular formatting should not matter such as having a trailing comma.
     * {@code sort=country,} should isolate 'country' directly.
     *
     * In this case since there is still a comma with no direction after it, the default direction 'asc'
     * means that 'desc' is the new direction.
     */
    @Test
    public void toggleSortDefaultAsc_IgnoreSyntaxIssues() {
        String query = "city=san%20francisco&region=west&sort=country,&locale=us";
        String expected = "city=san%20francisco&region=west&sort=country,desc&locale=us";

        HttpServletRequest mockHttpRequest = mock(HttpServletRequest.class);
        when(mockHttpRequest.getQueryString()).thenReturn(query);

        QueryStringHelper helper = new QueryStringHelper();
        String result = helper.toggleSortDefaultAsc(mockHttpRequest, "country");
        assertThat(result).isEqualTo(expected);

        verify(mockHttpRequest, times(1)).getQueryString();
        verifyNoMoreInteractions(mockHttpRequest);
    }

    /**
     * The sort field 'country' is sorted based on property syntax. This test is to ensure the sort field
     * and sort direction are being correctly identified.
     */
    @Test
    public void toggleSortDefaultAsc_WithSortFieldProperty() {
        String query = "city=san%20francisco&region=west&sort=country.name.id,desc&locale=us";
        String expected = "city=san%20francisco&region=west&sort=country.name.id,asc&locale=us";

        HttpServletRequest mockHttpRequest = mock(HttpServletRequest.class);
        when(mockHttpRequest.getQueryString()).thenReturn(query);

        QueryStringHelper helper = new QueryStringHelper();
        String result = helper.toggleSortDefaultAsc(mockHttpRequest, "country.name.id");
        assertThat(result).isEqualTo(expected);

        verify(mockHttpRequest, times(1)).getQueryString();
        verifyNoMoreInteractions(mockHttpRequest);
    }

    /**
     * When there are many sort fields, only the supplied sort field should be changed.
     */
    @Test
    public void toggleSortDefaultAsc_ManySortFields_UpdatesTargetFieldOnly() {
        String query = "city=san%20francisco&region=west&sort=country&sort=state,desc&locale=us-east&sort=city,asc";
        String expected = "city=san%20francisco&region=west&sort=country&sort=state,desc&locale=us-east&sort=city,desc";

        HttpServletRequest mockHttpRequest = mock(HttpServletRequest.class);
        when(mockHttpRequest.getQueryString()).thenReturn(query);

        QueryStringHelper helper = new QueryStringHelper();
        String result = helper.toggleSortDefaultAsc(mockHttpRequest, "city");
        assertThat(result).isEqualTo(expected);

        verify(mockHttpRequest, times(1)).getQueryString();
        verifyNoMoreInteractions(mockHttpRequest);
    }































    /**
     * When there are null arguments, the original query string should be returned.
     */
    @Test
    public void toggleSortDefaultDesc_HandlesNullArgs() {
        String query = "city=san%20francisco&region=west&sort=country&locale=us";

        HttpServletRequest mockHttpRequest = mock(HttpServletRequest.class);
        when(mockHttpRequest.getQueryString()).thenReturn(query);

        QueryStringHelper helper = new QueryStringHelper();
        String result = helper.toggleSortDefaultDesc(mockHttpRequest, null);
        assertThat(result).isEqualTo(query);

        verify(mockHttpRequest, times(1)).getQueryString();
        verifyNoMoreInteractions(mockHttpRequest);
    }

    /**
     * When there are empty arguments, the original query string should be returned.
     */
    @Test
    public void toggleSortDefaultDesc_HandlesEmptyArgs() {
        String query = "city=san%20francisco&region=west&sort=country&locale=us";

        HttpServletRequest mockHttpRequest = mock(HttpServletRequest.class);
        when(mockHttpRequest.getQueryString()).thenReturn(query);

        QueryStringHelper helper = new QueryStringHelper();
        String result = helper.toggleSortDefaultDesc(mockHttpRequest, "");
        assertThat(result).isEqualTo(query);

        verify(mockHttpRequest, times(1)).getQueryString();
        verifyNoMoreInteractions(mockHttpRequest);
    }

    /**
     * When the sort key is missing, the original query string should be returned.
     */
    @Test
    public void toggleSortDefaultDesc_MissingSortKey() {
        String query = "city=san%20francisco&region=west&sort=country&locale=us";

        HttpServletRequest mockHttpRequest = mock(HttpServletRequest.class);
        when(mockHttpRequest.getQueryString()).thenReturn(query);

        QueryStringHelper helper = new QueryStringHelper();
        String result = helper.toggleSortDefaultDesc(mockHttpRequest, "city");
        assertThat(result).isEqualTo(query);

        verify(mockHttpRequest, times(1)).getQueryString();
        verifyNoMoreInteractions(mockHttpRequest);
    }

    /**
     * When the sort field has no direction, the new sort direction should be the opposite
     * of the default direction. In this test the default is 'desc' so the expectation is
     * {@code sort=country,asc}.
     */
    @Test
    public void toggleSortDefaultDesc_NoDirection() {
        String query = "city=san%20francisco&region=west&sort=country&locale=us";
        String expected = "city=san%20francisco&region=west&sort=country,asc&locale=us";

        HttpServletRequest mockHttpRequest = mock(HttpServletRequest.class);
        when(mockHttpRequest.getQueryString()).thenReturn(query);

        QueryStringHelper helper = new QueryStringHelper();
        String result = helper.toggleSortDefaultDesc(mockHttpRequest, "country");
        assertThat(result).isEqualTo(expected);

        verify(mockHttpRequest, times(1)).getQueryString();
        verifyNoMoreInteractions(mockHttpRequest);
    }

    /**
     * When the sort field has an existing direction, the new sort direction should be the
     * opposite. In this test 'country' has direction 'desc' so toggling will change it to 'asc'.
     */
    @Test
    public void toggleSortDefaultDesc_ExistingDirection_ToggleFromDescToAsc() {
        String query = "city=san%20francisco&region=west&sort=country,desc&locale=us";
        String expected = "city=san%20francisco&region=west&sort=country,asc&locale=us";

        HttpServletRequest mockHttpRequest = mock(HttpServletRequest.class);
        when(mockHttpRequest.getQueryString()).thenReturn(query);

        QueryStringHelper helper = new QueryStringHelper();
        String result = helper.toggleSortDefaultDesc(mockHttpRequest, "country");
        assertThat(result).isEqualTo(expected);

        verify(mockHttpRequest, times(1)).getQueryString();
        verifyNoMoreInteractions(mockHttpRequest);
    }

    /**
     * When the sort field has an existing direction, the new sort direction should be the
     * opposite. In this test 'country' has direction 'asc' so toggling will change it to 'desc'.
     */
    @Test
    public void toggleSortDefaultDesc_ExistingDirection_ToggleFromAscToDesc() {
        String query = "city=san%20francisco&region=west&sort=country,asc&locale=us";
        String expected = "city=san%20francisco&region=west&sort=country,desc&locale=us";

        HttpServletRequest mockHttpRequest = mock(HttpServletRequest.class);
        when(mockHttpRequest.getQueryString()).thenReturn(query);

        QueryStringHelper helper = new QueryStringHelper();
        String result = helper.toggleSortDefaultDesc(mockHttpRequest, "country");
        assertThat(result).isEqualTo(expected);

        verify(mockHttpRequest, times(1)).getQueryString();
        verifyNoMoreInteractions(mockHttpRequest);
    }

    /**
     * This test checks that white space should be ignored. {@code sort= country   ,   asc) should be treated
     * the same as {@code sort=country,asc}.
     */
    @Test
    public void toggleSortDefaultDesc_TrimSpace() {
        String query = "city=san%20francisco&region=west&sort=%20%20country%20%20%20%20,%20%20asc%20%20&locale=us";
        String expected = "city=san%20francisco&region=west&sort=country,desc&locale=us";

        HttpServletRequest mockHttpRequest = mock(HttpServletRequest.class);
        when(mockHttpRequest.getQueryString()).thenReturn(query);

        QueryStringHelper helper = new QueryStringHelper();
        String result = helper.toggleSortDefaultDesc(mockHttpRequest, "country");
        assertThat(result).isEqualTo(expected);

        verify(mockHttpRequest, times(1)).getQueryString();
        verifyNoMoreInteractions(mockHttpRequest);
    }

    /**
     * This test checks that irregular formatting should not matter such as having a trailing comma.
     * {@code sort=country,} should isolate 'country' directly.
     *
     * In this case since there is still a comma with no direction after it, the default direction 'desc'
     * means that 'asc' is the new direction.
     */
    @Test
    public void toggleSortDefaultDesc_IgnoreSyntaxIssues() {
        String query = "city=san%20francisco&region=west&sort=country,&locale=us";
        String expected = "city=san%20francisco&region=west&sort=country,asc&locale=us";

        HttpServletRequest mockHttpRequest = mock(HttpServletRequest.class);
        when(mockHttpRequest.getQueryString()).thenReturn(query);

        QueryStringHelper helper = new QueryStringHelper();
        String result = helper.toggleSortDefaultDesc(mockHttpRequest, "country");
        assertThat(result).isEqualTo(expected);

        verify(mockHttpRequest, times(1)).getQueryString();
        verifyNoMoreInteractions(mockHttpRequest);
    }

    /**
     * The sort field 'country' is sorted based on property syntax. This test is to ensure the sort field
     * and sort direction are being correctly identified.
     */
    @Test
    public void toggleSortDefaultDesc_WithSortFieldProperty() {
        String query = "city=san%20francisco&region=west&sort=country.name.id,desc&locale=us";
        String expected = "city=san%20francisco&region=west&sort=country.name.id,asc&locale=us";

        HttpServletRequest mockHttpRequest = mock(HttpServletRequest.class);
        when(mockHttpRequest.getQueryString()).thenReturn(query);

        QueryStringHelper helper = new QueryStringHelper();
        String result = helper.toggleSortDefaultDesc(mockHttpRequest, "country.name.id");
        assertThat(result).isEqualTo(expected);

        verify(mockHttpRequest, times(1)).getQueryString();
        verifyNoMoreInteractions(mockHttpRequest);
    }

    /**
     * When there are many sort fields, only the supplied sort field should be changed.
     */
    @Test
    public void toggleSortDefaultDesc_ManySortFields_UpdatesTargetFieldOnly() {
        String query = "city=san%20francisco&region=west&sort=country&sort=state,desc&locale=us-east&sort=city,asc";
        String expected = "city=san%20francisco&region=west&sort=country&sort=state,desc&locale=us-east&sort=city,desc";

        HttpServletRequest mockHttpRequest = mock(HttpServletRequest.class);
        when(mockHttpRequest.getQueryString()).thenReturn(query);

        QueryStringHelper helper = new QueryStringHelper();
        String result = helper.toggleSortDefaultDesc(mockHttpRequest, "city");
        assertThat(result).isEqualTo(expected);

        verify(mockHttpRequest, times(1)).getQueryString();
        verifyNoMoreInteractions(mockHttpRequest);
    }
}
