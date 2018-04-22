package com.demo.thymeleaf.expression.helpers;

import com.demo.thymeleaf.expression.QueryStringHelper;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

public class SpringHelperTest {

    @Test
    public void incrementPage_QueryStringIsNull_ReturnEmptyString() {
        QueryStringHelper helper = new QueryStringHelper();
        String result = helper.incrementPage(null);
        assertThat(result).isEmpty();
    }

    @Test
    public void incrementPage_QueryStringIsEmpty_ReturnEmptyString() {
        QueryStringHelper helper = new QueryStringHelper();
        String result = helper.incrementPage("");
        assertThat(result).isEmpty();
    }

    /**
     * When the 'page' key is not found, the original query string should be returned.
     */
    @Test
    public void incrementPage_PageKeyNotFound_HasNoEffect() {
        String query = "key4=ab%20c&key8=100&key9=fgy&key8=23&key11=50&key8=hello_world-53&key8=-5&key8=23-about&key8=80";

        QueryStringHelper helper = new QueryStringHelper();
        String result = helper.incrementPage(query);
        assertThat(result).isEqualTo(query);
    }

    /**
     * When the 'page' key is found its value should be incremented by one.
     */
    @Test
    public void incrementPage_PageKeyIncrementedByOne() {
        String query = "key4=ab%20c&key8=100&page=0&key9=fgy&key8=23&key11=50&key8=hello_world-53&key8=-5&key8=23-about&key8=80";
        String expected = "key4=ab%20c&key8=100&page=1&key9=fgy&key8=23&key11=50&key8=hello_world-53&key8=-5&key8=23-about&key8=80";

        QueryStringHelper helper = new QueryStringHelper();
        String result = helper.incrementPage(query);
        assertThat(result).isEqualTo(expected);
    }

    /**
     * If for whatever reason the page value is non numeric, incrementing should do nothing and return original query string.
     */
    @Test
    public void incrementPage_PageKeyNotNumeric_HasNoEffect() {
        String query = "key4=ab%20c&key8=100&page=X-Y-Z&key9=fgy&key8=23&key11=50&key8=hello_world-53&key8=-5&key8=23-about&key8=80";
        String expected = "key4=ab%20c&key8=100&page=X-Y-Z&key9=fgy&key8=23&key11=50&key8=hello_world-53&key8=-5&key8=23-about&key8=80";

        QueryStringHelper helper = new QueryStringHelper();
        String result = helper.incrementPage(query);
        assertThat(result).isEqualTo(expected);
    }

    /**
     * Test that the incrementPage with max bound handles a null query string correctly.
     */
    @Test
    public void incrementPage_MaxBound_QueryStringIsNull_ReturnEmptyString() {
        QueryStringHelper helper = new QueryStringHelper();
        String result = helper.incrementPage(null, 5);
        assertThat(result).isEmpty();
    }

    /**
     * Test that the incrementPage with max bound handles a null query string correctly.
     */
    @Test
    public void incrementPage_MaxBound_QueryStringIsEmpty_ReturnEmptyString() {
        QueryStringHelper helper = new QueryStringHelper();
        String result = helper.incrementPage("", 5);
        assertThat(result).isEmpty();
    }

    /**
     * When the current page value is below the max bound, it should be incremented by 1.
     */
    @Test
    public void incrementPage_BelowMaxBound_Increments() {
        String query = "key2=500&key3=abc&page=5&key6=hello";
        String expected = "key2=500&key3=abc&page=6&key6=hello";

        QueryStringHelper helper = new QueryStringHelper();
        // current value is 5, max bound is 6 so it is ok to increment.
        String result = helper.incrementPage(query, 6);
        assertThat(result).isEqualTo(expected);
    }

    /**
     * When the current page value is equal to the max bound, it should not be incremented.
     */
    @Test
    public void incrementPage_EqualMaxBound_DoesNotIncrement() {
        String query = "key2=500&key3=abc&page=5&key6=hello";
        String expected = "key2=500&key3=abc&page=5&key6=hello";

        QueryStringHelper helper = new QueryStringHelper();
        // current value is 5, max bound is 5 so it is not incremented.
        String result = helper.incrementPage(query, 5);
        assertThat(result).isEqualTo(expected);
    }

    /**
     * When the current page value is greater than the max bound, it should not be incremented.
     */
    @Test
    public void incrementPage_AboveMaxBound_DoesNotIncrement() {
        String query = "key2=500&key3=abc&page=5&key6=hello";
        String expected = "key2=500&key3=abc&page=5&key6=hello";

        QueryStringHelper helper = new QueryStringHelper();
        // current value is 5, max bound is 6 so it is not incremented.
        String result = helper.incrementPage(query, 4);
        assertThat(result).isEqualTo(expected);
    }

    @Test
    public void decrementPage_QueryStringIsNull_ReturnEmptyString() {
        QueryStringHelper helper = new QueryStringHelper();
        String result = helper.decrementPage(null);
        assertThat(result).isEmpty();
    }

    @Test
    public void decrementPage_QueryStringIsEmpty_ReturnEmptyString() {
        QueryStringHelper helper = new QueryStringHelper();
        String result = helper.decrementPage("");
        assertThat(result).isEmpty();
    }

    /**
     * When the 'page' key is not found, the original query string should be returned.
     */
    @Test
    public void decrementPage_PageKeyNotFound_HasNoEffect() {
        String query = "key4=ab%20c&key8=100&key9=fgy&key8=23&key11=50&key8=hello_world-53&key8=-5&key8=23-about&key8=80";

        QueryStringHelper helper = new QueryStringHelper();
        String result = helper.decrementPage(query);
        assertThat(result).isEqualTo(query);
    }

    /**
     * When the 'page' key is found its value should be decremented by one ONLY if its value is greater than 0.
     */
    @Test
    public void decrementPage_PageKey_GreaterThanZero_DecrementedByOne() {
        String query = "key4=ab%20c&key8=100&page=1&key9=fgy&key8=23&key11=50&key8=hello_world-53&key8=-5&key8=23-about&key8=80";
        String expected = "key4=ab%20c&key8=100&page=0&key9=fgy&key8=23&key11=50&key8=hello_world-53&key8=-5&key8=23-about&key8=80";

        QueryStringHelper helper = new QueryStringHelper();
        String result = helper.decrementPage(query);
        assertThat(result).isEqualTo(expected);
    }

    /**
     * When the 'page' key is found its value should be decremented by one ONLY if its value is greater than 0.
     * This ensure page can only have min value of 0.
     */
    @Test
    public void decrementPage_PageKey_IsZero_ShouldNotBeDecrementedByOne() {
        String query = "key4=ab%20c&key8=100&page=0&key9=fgy&key8=23&key11=50&key8=hello_world-53&key8=-5&key8=23-about&key8=80";
        String expected = "key4=ab%20c&key8=100&page=0&key9=fgy&key8=23&key11=50&key8=hello_world-53&key8=-5&key8=23-about&key8=80";

        QueryStringHelper helper = new QueryStringHelper();
        String result = helper.decrementPage(query);
        assertThat(result).isEqualTo(expected);
    }

    /**
     * If for whatever reason the page value is non numeric, decrementing should do nothing and return original query string.
     */
    @Test
    public void decrementPage_PageKeyNotNumeric_HasNoEffect() {
        String query = "key4=ab%20c&key8=100&page=X-Y-Z&key9=fgy&key8=23&key11=50&key8=hello_world-53&key8=-5&key8=23-about&key8=80";
        String expected = "key4=ab%20c&key8=100&page=X-Y-Z&key9=fgy&key8=23&key11=50&key8=hello_world-53&key8=-5&key8=23-about&key8=80";

        QueryStringHelper helper = new QueryStringHelper();
        String result = helper.decrementPage(query);
        assertThat(result).isEqualTo(expected);
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


    // setSortDirectionAsc - Using explicit asc and desc methods for added type safety but requires duplicate tests.

    @Test
    public void setSortDirectionAsc_QueryStringIsNull_ReturnEmptyString() {
        QueryStringHelper helper = new QueryStringHelper();
        String result = helper.setSortDirectionAsc(null, "country");
        assertThat(result).isEmpty();
    }

    @Test
    public void setSortDirectionAsc_QueryStringIsEmpty_ReturnEmptyString() {
        QueryStringHelper helper = new QueryStringHelper();
        String result = helper.setSortDirectionAsc("", "country");
        assertThat(result).isEmpty();
    }

    /**
     * When there is no sort key, the original query string should be returned.
     * country is not in the query string.
     */
    @Test
    public void setSortDirectionAsc_MissingSortKey_HasNoEffect() {
        String query = "city=san%20francisco&region=west&locale=us-east";

        QueryStringHelper helper = new QueryStringHelper();
        String result = helper.setSortDirectionAsc(query, "country");
        assertThat(result).isEqualTo(query);
    }

    /**
     * When the sort field is missing, the original query should be returned.
     */
    @Test
    public void setSortDirectionAsc_EmptySortField() {
        String query = "city=san%20francisco&region=west&locale=us-east&sort=country,desc";

        QueryStringHelper helper = new QueryStringHelper();
        String result = helper.setSortDirectionAsc(query, "suburb");
        assertThat(result).isEqualTo(query);
    }

    /**
     * When the existing field has no direction, the new value should have the sort direction appended
     * with a comma. {@code sort=country,asc}.
     */
    @Test
    public void setSortDirectionAsc_SingleSortField_NoDirection() {
        String query = "city=san%20francisco&region=west&sort=country&locale=us-east";
        String expected = "city=san%20francisco&region=west&sort=country,asc&locale=us-east";

        QueryStringHelper helper = new QueryStringHelper();
        String result = helper.setSortDirectionAsc(query, "country");
        assertThat(result).isEqualTo(expected);
    }

    /**
     * When the existing field already has a direction, the new value should consist of the existing sort field and
     * 'asc' direction separated by a comma. {@code sort=country,asc}
     */
    @Test
    public void setSortDirectionAsc_SingleSortField_WithDirection() {
        String query = "city=san%20francisco&region=west&sort=country,desc&locale=us-east";
        String expected = "city=san%20francisco&region=west&sort=country,asc&locale=us-east";

        QueryStringHelper helper = new QueryStringHelper();
        String result = helper.setSortDirectionAsc(query, "country");
        assertThat(result).isEqualTo(expected);
    }

    /**
     * When the existing field already has a direction, the new value should consist of the existing sort field and
     * 'asc' direction separated by a comma. {@code sort=country,asc}
     * <p>
     * This test checks that white space should be ignored. {@code sort= country   ,   asc) should be treated
     * the same as {@code sort=country,asc}.
     */
    @Test
    public void setSortDirectionAsc_SingleSortField_WithDirection_TrimSpace() {
        String query = "city=san%20francisco&region=west&sort=%20country%20%20,%20desc%20&locale=us-east";
        String expected = "city=san%20francisco&region=west&sort=country,asc&locale=us-east";

        QueryStringHelper helper = new QueryStringHelper();
        String result = helper.setSortDirectionAsc(query, "country");
        assertThat(result).isEqualTo(expected);
    }

    /**
     * When the existing field has a direction, the new value should have the sort direction appended
     * with a comma. sort=country,desc.
     * <p>
     * This test checks that irregular formatting should not matter such as having a trailing comma.
     * {@code sort=country,} should isolate 'country' directly.
     */
    @Test
    public void setSortDirectionAsc_SingleSortField_WithDirection_IgnoreSyntaxIssues() {
        String query = "city=san%20francisco&region=west&sort=country,&locale=us-east";
        String expected = "city=san%20francisco&region=west&sort=country,asc&locale=us-east";

        QueryStringHelper helper = new QueryStringHelper();
        String result = helper.setSortDirectionAsc(query, "country");
        assertThat(result).isEqualTo(expected);
    }

    /**
     * When the existing field has a direction and the new direction is the same, the new query string should
     * be the same as the original.
     */
    @Test
    public void setSortDirectionAsc_SingleSortField_WithDirection_SetSameDirection() {
        String query = "city=san%20francisco&region=west&sort=country,asc&locale=us-east";

        QueryStringHelper helper = new QueryStringHelper();
        String result = helper.setSortDirectionAsc(query, "country");
        assertThat(result).isEqualTo(query);
    }

    /**
     * The sort field 'country' is sorted based on property syntax. This test is to ensure the sort field
     * and sort direction are being correctly identified.
     */
    @Test
    public void setSortDirectionAsc_SingleSortField_WithSortFieldProperty() {
        String query = "city=san%20francisco&region=west&sort=country.name.id,desc&locale=us-east";
        String expected = "city=san%20francisco&region=west&sort=country.name.id,asc&locale=us-east";

        QueryStringHelper helper = new QueryStringHelper();
        String result = helper.setSortDirectionAsc(query, "country.name.id");
        assertThat(result).isEqualTo(expected);
    }

    /**
     * When there are many sort fields, only the supplied sort field should be changed to 'asc'.
     */
    @Test
    public void setSortDirectionAsc_ManySortFields_UpdatesTargetFieldOnly() {
        String query = "city=san%20francisco&region=west&sort=country&sort=state,desc&locale=us-east&sort=city,desc";
        String expected = "city=san%20francisco&region=west&sort=country&sort=state,desc&locale=us-east&sort=city,asc";

        QueryStringHelper helper = new QueryStringHelper();
        String result = helper.setSortDirectionAsc(query, "city");
        assertThat(result).isEqualTo(expected);
    }

    /**
     * When there are many sort fields, only the supplied sort field should be changed. This test applies the same
     * sort direction that already exists and expects the new query string to equal the original query string.
     */
    @Test
    public void setSortDirectionAsc_ManySortFields_SameDirection_UpdatesTargetFieldOnly() {
        String query = "city=san%20francisco&region=west&sort=country&sort=state,desc&locale=us-east&sort=city,asc";

        QueryStringHelper helper = new QueryStringHelper();
        String result = helper.setSortDirectionAsc(query, "city");
        assertThat(result).isEqualTo(query);
    }

    // setSortDirectionDesc - Using explicit asc and desc methods for added type safety but requires duplicate tests.

    @Test
    public void setSortDirectionDesc_QueryStringIsNull_ReturnEmptyString() {
        QueryStringHelper helper = new QueryStringHelper();
        String result = helper.setSortDirectionDesc(null, "country");
        assertThat(result).isEmpty();
    }

    @Test
    public void setSortDirectionDesc_QueryStringIsEmpty_ReturnEmptyString() {
        QueryStringHelper helper = new QueryStringHelper();
        String result = helper.setSortDirectionDesc("", "country");
        assertThat(result).isEmpty();
    }

    /**
     * When there is no sort key, the original query string should be returned.
     * country is not in the query string.
     */
    @Test
    public void setSortDirectionDesc_MissingSortKey_HasNoEffect() {
        String query = "city=san%20francisco&region=west&locale=us-east";

        QueryStringHelper helper = new QueryStringHelper();
        String result = helper.setSortDirectionDesc(query, "country");
        assertThat(result).isEqualTo(query);
    }

    /**
     * When the sort field is null, the original query should be returned.
     */
    @Test
    public void setSortDirectionDesc_HandlesNullSortField() {
        String query = "city=san%20francisco&region=west&locale=us-east";

        QueryStringHelper helper = new QueryStringHelper();
        String result = helper.setSortDirectionDesc(query, null);
        assertThat(result).isEqualTo(query);
    }

    /**
     * When the sort field is empty, the original query should be returned.
     */
    @Test
    public void setSortDirectionDesc_EmptySortField() {
        String query = "city=san%20francisco&region=west&locale=us-east";

        QueryStringHelper helper = new QueryStringHelper();
        String result = helper.setSortDirectionDesc(query, "");
        assertThat(result).isEqualTo(query);
    }

    /**
     * When the existing field has no direction, the new value should have the sort direction appended
     * with a comma. {@code sort=country,desc}
     */
    @Test
    public void setSortDirectionDesc_SingleSortField_NoDirection() {
        String query = "city=san%20francisco&region=west&sort=country&locale=us-east";
        String expected = "city=san%20francisco&region=west&sort=country,desc&locale=us-east";

        QueryStringHelper helper = new QueryStringHelper();
        String result = helper.setSortDirectionDesc(query, "country");
        assertThat(result).isEqualTo(expected);
    }

    /**
     * When the existing field already has a direction, the new value should consist of the existing sort field and
     * 'desc' direction separated by a comma. {@code sort=country,desc}
     */
    @Test
    public void setSortDirectionDesc_SingleSortField_WithDirection() {
        String query = "city=san%20francisco&region=west&sort=country,asc&locale=us-east";
        String expected = "city=san%20francisco&region=west&sort=country,desc&locale=us-east";

        QueryStringHelper helper = new QueryStringHelper();
        String result = helper.setSortDirectionDesc(query, "country");
        assertThat(result).isEqualTo(expected);
    }

    /**
     * When the existing field already has a direction, the new value should consist of the existing sort field and
     * 'desc' direction separated by a comma. {@code sort=country,desc}
     * <p>
     * This test checks that white space should be ignored. {@code sort= country   ,   desc) should be treated
     * the same as {@code sort=country,desc}.
     */
    @Test
    public void setSortDirectionDesc_SingleSortField_WithDirection_TrimSpace() {
        String query = "city=san%20francisco&region=west&sort=%20country%20%20,%20desc%20&locale=us-east";
        String expected = "city=san%20francisco&region=west&sort=country,desc&locale=us-east";

        QueryStringHelper helper = new QueryStringHelper();
        String result = helper.setSortDirectionDesc(query, "country");
        assertThat(result).isEqualTo(expected);
    }

    /**
     * When the existing field has a direction, the new value should have the sort direction appended
     * with a comma. sort=country,desc.
     * <p>
     * This test checks that irregular formatting should not matter such as having a trailing comma.
     * {@code sort=country,} should isolate 'country' directly.
     */
    @Test
    public void setSortDirectionDesc_SingleSortField_WithDirection_IgnoreSyntaxIssues() {
        String query = "city=san%20francisco&region=west&sort=country,&locale=us-east";
        String expected = "city=san%20francisco&region=west&sort=country,desc&locale=us-east";

        QueryStringHelper helper = new QueryStringHelper();
        String result = helper.setSortDirectionDesc(query, "country");
        assertThat(result).isEqualTo(expected);
    }

    /**
     * When the existing field has a direction and the new direction is the same, the new query string should
     * be the same as the original.
     */
    @Test
    public void setSortDirectionDesc_SingleSortField_WithDirection_SetSameDirection() {
        String query = "city=san%20francisco&region=west&sort=country,desc&locale=us-east";

        QueryStringHelper helper = new QueryStringHelper();
        String result = helper.setSortDirectionDesc(query, "country");
        assertThat(result).isEqualTo(query);
    }

    /**
     * The sort field 'country' is sorted based on property syntax. This test is to ensure the sort field
     * and sort direction are being correctly identified.
     */
    @Test
    public void setSortDirectionDesc_SingleSortField_WithSortFieldProperty() {
        String query = "city=san%20francisco&region=west&sort=country.name.id,asc&locale=us-east";
        String expected = "city=san%20francisco&region=west&sort=country.name.id,desc&locale=us-east";

        QueryStringHelper helper = new QueryStringHelper();
        String result = helper.setSortDirectionDesc(query, "country.name.id");
        assertThat(result).isEqualTo(expected);
    }

    /**
     * When there are many sort fields, only the supplied sort field should be changed to 'desc'.
     */
    @Test
    public void setSortDirectionDesc_ManySortFields_UpdatesTargetFieldOnly() {
        String query = "city=san%20francisco&region=west&sort=country&sort=state,desc&locale=us-east&sort=city,asc";
        String expected = "city=san%20francisco&region=west&sort=country&sort=state,desc&locale=us-east&sort=city,desc";

        QueryStringHelper helper = new QueryStringHelper();
        String result = helper.setSortDirectionDesc(query, "city");
        assertThat(result).isEqualTo(expected);
    }

    /**
     * When there are many sort fields, only the supplied sort field should be changed. This test applies the same
     * sort direction that already exists and expects the new query string to equal the original query string.
     */
    @Test
    public void setSortDirectionDesc_ManySortFields_SameDirection_UpdatesTargetFieldOnly() {
        String query = "city=san%20francisco&region=west&sort=country&sort=state,desc&locale=us-east&sort=city,desc";

        QueryStringHelper helper = new QueryStringHelper();
        String result = helper.setSortDirectionDesc(query, "city");
        assertThat(result).isEqualTo(query);
    }

    @Test
    public void toggleSortDefaultAsc_QueryStringIsNull_ReturnEmptyString() {
        QueryStringHelper helper = new QueryStringHelper();
        String result = helper.toggleSortDefaultAsc(null, "country");
        assertThat(result).isEmpty();
    }

    @Test
    public void toggleSortDefaultAsc_QueryStringIsEmpty_ReturnEmptyString() {
        QueryStringHelper helper = new QueryStringHelper();
        String result = helper.toggleSortDefaultAsc("", "country");
        assertThat(result).isEmpty();
    }

    /**
     * When the sort key is missing, the original query string should be returned.
     */
    @Test
    public void toggleSortDefaultAsc_MissingSortKey() {
        String query = "city=san%20francisco&region=west&sort=country&locale=us";

        QueryStringHelper helper = new QueryStringHelper();
        String result = helper.toggleSortDefaultAsc(query, "city");
        assertThat(result).isEqualTo(query);
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

        QueryStringHelper helper = new QueryStringHelper();
        String result = helper.toggleSortDefaultAsc(query, "country");
        assertThat(result).isEqualTo(expected);
    }

    /**
     * When the sort field has an existing direction, the new sort direction should be the
     * opposite. In this test 'country' has direction 'desc' so toggling will change it to 'asc'.
     */
    @Test
    public void toggleSortDefaultAsc_ExistingDirection_ToggleFromDescToAsc() {
        String query = "city=san%20francisco&region=west&sort=country,desc&locale=us";
        String expected = "city=san%20francisco&region=west&sort=country,asc&locale=us";

        QueryStringHelper helper = new QueryStringHelper();
        String result = helper.toggleSortDefaultAsc(query, "country");
        assertThat(result).isEqualTo(expected);
    }

    /**
     * When the sort field has an existing direction, the new sort direction should be the
     * opposite. In this test 'country' has direction 'asc' so toggling will change it to 'desc'.
     */
    @Test
    public void toggleSortDefaultAsc_ExistingDirection_ToggleFromAscToDesc() {
        String query = "city=san%20francisco&region=west&sort=country,asc&locale=us";
        String expected = "city=san%20francisco&region=west&sort=country,desc&locale=us";

        QueryStringHelper helper = new QueryStringHelper();
        String result = helper.toggleSortDefaultAsc(query, "country");
        assertThat(result).isEqualTo(expected);
    }

    /**
     * This test checks that white space should be ignored. {@code sort= country   ,   asc) should be treated
     * the same as {@code sort=country,asc}.
     */
    @Test
    public void toggleSortDefaultAsc_TrimSpace() {
        String query = "city=san%20francisco&region=west&sort=%20%20country%20%20%20%20,%20%20asc%20%20&locale=us";
        String expected = "city=san%20francisco&region=west&sort=country,desc&locale=us";

        QueryStringHelper helper = new QueryStringHelper();
        String result = helper.toggleSortDefaultAsc(query, "country");
        assertThat(result).isEqualTo(expected);
    }

    /**
     * This test checks that irregular formatting should not matter such as having a trailing comma.
     * {@code sort=country,} should isolate 'country' directly.
     * <p>
     * In this case since there is still a comma with no direction after it, the default direction 'asc'
     * means that 'desc' is the new direction.
     */
    @Test
    public void toggleSortDefaultAsc_IgnoreSyntaxIssues() {
        String query = "city=san%20francisco&region=west&sort=country,&locale=us";
        String expected = "city=san%20francisco&region=west&sort=country,desc&locale=us";

        QueryStringHelper helper = new QueryStringHelper();
        String result = helper.toggleSortDefaultAsc(query, "country");
        assertThat(result).isEqualTo(expected);
    }

    /**
     * The sort field 'country' is sorted based on property syntax. This test is to ensure the sort field
     * and sort direction are being correctly identified.
     */
    @Test
    public void toggleSortDefaultAsc_WithSortFieldProperty() {
        String query = "city=san%20francisco&region=west&sort=country.name.id,desc&locale=us";
        String expected = "city=san%20francisco&region=west&sort=country.name.id,asc&locale=us";

        QueryStringHelper helper = new QueryStringHelper();
        String result = helper.toggleSortDefaultAsc(query, "country.name.id");
        assertThat(result).isEqualTo(expected);
    }

    /**
     * When there are many sort fields, only the supplied sort field should be changed.
     */
    @Test
    public void toggleSortDefaultAsc_ManySortFields_UpdatesTargetFieldOnly() {
        String query = "city=san%20francisco&region=west&sort=country&sort=state,desc&locale=us-east&sort=city,asc";
        String expected = "city=san%20francisco&region=west&sort=country&sort=state,desc&locale=us-east&sort=city,desc";

        QueryStringHelper helper = new QueryStringHelper();
        String result = helper.toggleSortDefaultAsc(query, "city");
        assertThat(result).isEqualTo(expected);
    }

    @Test
    public void toggleSortDefaultDesc_QueryStringIsNull_ReturnEmptyString() {
        QueryStringHelper helper = new QueryStringHelper();
        String result = helper.toggleSortDefaultDesc(null, "country");
        assertThat(result).isEmpty();
    }

    @Test
    public void toggleSortDefaultDesc_QueryStringIsEmpty_ReturnEmptyString() {
        QueryStringHelper helper = new QueryStringHelper();
        String result = helper.toggleSortDefaultDesc("", "country");
        assertThat(result).isEmpty();
    }

    /**
     * When the sort key is missing, the original query string should be returned.
     */
    @Test
    public void toggleSortDefaultDesc_MissingSortKey() {
        String query = "city=san%20francisco&region=west&sort=country&locale=us";

        QueryStringHelper helper = new QueryStringHelper();
        String result = helper.toggleSortDefaultDesc(query, "city");
        assertThat(result).isEqualTo(query);
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

        QueryStringHelper helper = new QueryStringHelper();
        String result = helper.toggleSortDefaultDesc(query, "country");
        assertThat(result).isEqualTo(expected);
    }

    /**
     * When the sort field has an existing direction, the new sort direction should be the
     * opposite. In this test 'country' has direction 'desc' so toggling will change it to 'asc'.
     */
    @Test
    public void toggleSortDefaultDesc_ExistingDirection_ToggleFromDescToAsc() {
        String query = "city=san%20francisco&region=west&sort=country,desc&locale=us";
        String expected = "city=san%20francisco&region=west&sort=country,asc&locale=us";

        QueryStringHelper helper = new QueryStringHelper();
        String result = helper.toggleSortDefaultDesc(query, "country");
        assertThat(result).isEqualTo(expected);
    }

    /**
     * When the sort field has an existing direction, the new sort direction should be the
     * opposite. In this test 'country' has direction 'asc' so toggling will change it to 'desc'.
     */
    @Test
    public void toggleSortDefaultDesc_ExistingDirection_ToggleFromAscToDesc() {
        String query = "city=san%20francisco&region=west&sort=country,asc&locale=us";
        String expected = "city=san%20francisco&region=west&sort=country,desc&locale=us";

        QueryStringHelper helper = new QueryStringHelper();
        String result = helper.toggleSortDefaultDesc(query, "country");
        assertThat(result).isEqualTo(expected);
    }


    /**
     * This test checks that white space should be ignored. {@code sort= country   ,   asc) should be treated
     * the same as {@code sort=country,asc}.
     */
    @Test
    public void toggleSortDefaultDesc_TrimSpace() {
        String query = "city=san%20francisco&region=west&sort=%20%20country%20%20%20%20,%20%20asc%20%20&locale=us";
        String expected = "city=san%20francisco&region=west&sort=country,desc&locale=us";

        QueryStringHelper helper = new QueryStringHelper();
        String result = helper.toggleSortDefaultDesc(query, "country");
        assertThat(result).isEqualTo(expected);
    }

    /**
     * This test checks that irregular formatting should not matter such as having a trailing comma.
     * {@code sort=country,} should isolate 'country' directly.
     * <p>
     * In this case since there is still a comma with no direction after it, the default direction 'desc'
     * means that 'asc' is the new direction.
     */
    @Test
    public void toggleSortDefaultDesc_IgnoreSyntaxIssues() {
        String query = "city=san%20francisco&region=west&sort=country,&locale=us";
        String expected = "city=san%20francisco&region=west&sort=country,asc&locale=us";

        QueryStringHelper helper = new QueryStringHelper();
        String result = helper.toggleSortDefaultDesc(query, "country");
        assertThat(result).isEqualTo(expected);
    }

    /**
     * The sort field 'country' is sorted based on property syntax. This test is to ensure the sort field
     * and sort direction are being correctly identified.
     */
    @Test
    public void toggleSortDefaultDesc_WithSortFieldProperty() {
        String query = "city=san%20francisco&region=west&sort=country.name.id,desc&locale=us";
        String expected = "city=san%20francisco&region=west&sort=country.name.id,asc&locale=us";

        QueryStringHelper helper = new QueryStringHelper();
        String result = helper.toggleSortDefaultDesc(query, "country.name.id");
        assertThat(result).isEqualTo(expected);
    }

    /**
     * When there are many sort fields, only the supplied sort field should be changed.
     */
    @Test
    public void toggleSortDefaultDesc_ManySortFields_UpdatesTargetFieldOnly() {
        String query = "city=san%20francisco&region=west&sort=country&sort=state,desc&locale=us-east&sort=city,asc";
        String expected = "city=san%20francisco&region=west&sort=country&sort=state,desc&locale=us-east&sort=city,desc";

        QueryStringHelper helper = new QueryStringHelper();
        String result = helper.toggleSortDefaultDesc(query, "city");
        assertThat(result).isEqualTo(expected);
    }

    @Test
    public void url_WhenNullRequestURI_ThrowException() {
        QueryStringHelper helper = new QueryStringHelper();
        assertThatIllegalArgumentException()
                .isThrownBy(() -> helper.url(null, "location=europe"));
    }

    @Test
    public void url_WhenEmptyRequestURI_ThrowException() {
        QueryStringHelper helper = new QueryStringHelper();
        assertThatIllegalArgumentException()
                .isThrownBy(() -> helper.url("", "location=europe"));
    }

    /**
     * When only the request uri is given and the query string is null, return only the request uri.
     */
    @Test
    public void url_UriAndNullQueryString_ReturnUriOnly() {
        QueryStringHelper helper = new QueryStringHelper();
        String result = helper.url("/home/main", null);

        String expect = "/home/main";

        assertThat(result).isEqualTo(expect);
    }

    /**
     * When only the request uri is given and the query string is empty, return only the request uri.
     */
    @Test
    public void url_UriAndEmptyQueryString_ReturnUriOnly() {
        QueryStringHelper helper = new QueryStringHelper();
        String result = helper.url("/home/main", "");

        String expect = "/home/main";

        assertThat(result).isEqualTo(expect);
    }

    /**
     * When both the request uri and query string exist, they should be concatenated
     * together with a {@code ?}.
     */
    @Test
    public void url_UriAndQueryStringExist_ReturnConcatenation() {
        QueryStringHelper helper = new QueryStringHelper();
        String result = helper.url("/home/main", "locale=eu&location=europe");

        String expect = "/home/main?locale=eu&location=europe";

        assertThat(result).isEqualTo(expect);
    }

    /**
     * When the query string is null, the new query string should be
     * {@code sort='field,defaultDirection'} where field is 'country' and defaultDirection
     * is determined by fieldSorterAsc (eg the 'Asc' part).
     */
    @Test
    public void fieldSorterAsc_QueryStringIsNull_ReturnsFieldWithDefaultDirection() {
        String expected = "sort=country,asc";
        QueryStringHelper helper = new QueryStringHelper();

        String result = helper.fieldSorterAsc(null).apply("country");
        assertThat(result).isEqualTo(expected);
    }

    /**
     * When the query string is empty, the new query string should be
     * {@code sort='field,defaultDirection'} where field is 'country' and defaultDirection
     * is determined by fieldSorterAsc (eg the 'Asc' part).
     */
    @Test
    public void fieldSorterAsc_QueryStringIsEmpty_ReturnsFieldWithDefaultDirection() {
        String expected = "sort=country,asc";
        QueryStringHelper helper = new QueryStringHelper();

        // Make sure string is trimmed.
        String result = helper.fieldSorterAsc("   ").apply("country");
        assertThat(result).isEqualTo(expected);
    }

    /**
     * fieldSorterAsc means 'Asc' is the default sort direction to use when the field has no direction.
     * <p>
     * Since 'city' has no direction, is its implicit direction is 'asc' resulting in 'desc' after the toggle.
     */
    @Test
    public void fieldSorterAsc_ImplicitSortDirection() {
        String query = "city=melbourne&state=vic&postcode=3000&sort=city&sort=postcode";
        String expected = "city=melbourne&state=vic&postcode=3000&sort=city,desc&sort=postcode";
        QueryStringHelper helper = new QueryStringHelper();

        String result = helper.fieldSorterAsc(query).apply("city");
        assertThat(result).isEqualTo(expected);
    }

    /**
     * fieldSorterAsc means 'Asc' is the default sort direction to use when the field has no direction.
     * <p>
     * However in this case, 'city' already has an explicit direction 'desc' so the toggle transitions it to 'asc'.
     */
    @Test
    public void fieldSorterAsc_ExplicitSortDirection() {
        String query = "city=melbourne&state=vic&postcode=3000&sort=city,desc&sort=postcode";
        String expected = "city=melbourne&state=vic&postcode=3000&sort=city,asc&sort=postcode";
        QueryStringHelper helper = new QueryStringHelper();

        String result = helper.fieldSorterAsc(query).apply("city");
        assertThat(result).isEqualTo(expected);
    }

    /**
     * 'location' does not exist in the query string, therefore all existing 'sort' keys are removed with
     * the new key 'sort=location,defaultDirection' added to the end. 'defaultDirection' is determined by
     * the fieldSorterAsc (eg the 'Asc' at the end).
     */
    @Test
    public void fieldSorterAsc_FieldDoesNotExist_ImplicitSortDirection() {
        String query = "city=melbourne&state=vic&postcode=3000&sort=city,desc&sort=postcode";
        String expected = "city=melbourne&state=vic&postcode=3000&sort=location,asc";
        QueryStringHelper helper = new QueryStringHelper();

        String result = helper.fieldSorterAsc(query).apply("location");
        assertThat(result).isEqualTo(expected);
    }

    /**
     * When the query string is null, the new query string should be
     * {@code sort='field,defaultDirection'} where field is 'country' and defaultDirection
     * is determined by fieldSorterDesc (eg the 'Desc' part).
     */
    @Test
    public void fieldSorterDesc_QueryStringIsNull_ReturnsFieldWithDefaultDirection() {
        String expected = "sort=country,desc";
        QueryStringHelper helper = new QueryStringHelper();

        String result = helper.fieldSorterDesc(null).apply("country");
        assertThat(result).isEqualTo(expected);
    }

    /**
     * When the query string is empty, the new query string should be
     * {@code sort='field,defaultDirection'} where field is 'country' and defaultDirection
     * is determined by fieldSorterDesc (eg the 'Desc' part).
     */
    @Test
    public void fieldSorterDesc_QueryStringIsEmpty_ReturnsFieldWithDefaultDirection() {
        String expected = "sort=country,desc";
        QueryStringHelper helper = new QueryStringHelper();

        // Make sure string is trimmed.
        String result = helper.fieldSorterDesc("   ").apply("country");
        assertThat(result).isEqualTo(expected);
    }

    /**
     * fieldSorterDesc means 'Desc' is the default sort direction to use when the field has no direction.
     * <p>
     * Since 'city' has no direction, is its implicit direction is 'desc' resulting in 'asc' after the toggle.
     */
    @Test
    public void fieldSorterDesc_ImplicitSortDirection() {
        String query = "city=melbourne&state=vic&postcode=3000&sort=city&sort=postcode";
        String expected = "city=melbourne&state=vic&postcode=3000&sort=city,asc&sort=postcode";
        QueryStringHelper helper = new QueryStringHelper();

        String result = helper.fieldSorterDesc(query).apply("city");
        assertThat(result).isEqualTo(expected);
    }

    /**
     * fieldSorterDesc means 'Desc' is the default sort direction to use when the field has no direction.
     * <p>
     * However in this case, 'city' already has an explicit direction 'desc' so the toggle transitions it to 'asc'.
     */
    @Test
    public void fieldSorterDesc_ExplicitSortDirection() {
        String query = "city=melbourne&state=vic&postcode=3000&sort=city,desc&sort=postcode";
        String expected = "city=melbourne&state=vic&postcode=3000&sort=city,asc&sort=postcode";
        QueryStringHelper helper = new QueryStringHelper();

        String result = helper.fieldSorterDesc(query).apply("city");
        assertThat(result).isEqualTo(expected);
    }

    /**
     * 'location' does not exist in the query string, therefore all existing 'sort' keys are removed with
     * the new key 'sort=location,defaultDirection' added to the end. 'defaultDirection' is determined by
     * the fieldSorterDesc (eg the 'Desc' at the end).
     */
    @Test
    public void fieldSorterDesc_FieldDoesNotExist_ImplicitSortDirection() {
        String query = "city=melbourne&state=vic&postcode=3000&sort=city,desc&sort=postcode";
        String expected = "city=melbourne&state=vic&postcode=3000&sort=location,desc";
        QueryStringHelper helper = new QueryStringHelper();

        String result = helper.fieldSorterDesc(query).apply("location");
        assertThat(result).isEqualTo(expected);
    }

    @Test
    public void valueWhenMatchesSortAsc_QueryStringIsNull_ReturnMissingValue() {
        QueryStringHelper helper = new QueryStringHelper();
        String result = helper.valueWhenMatchesSortAsc(null, "missing", "matching", "nonmatching")
                .apply("location");
        assertThat(result).isEqualTo("missing");
    }

    @Test
    public void valueWhenMatchesSortAsc_QueryStringIsEmpty_ReturnMissingValue() {
        QueryStringHelper helper = new QueryStringHelper();
        String result = helper.valueWhenMatchesSortAsc("", "missing", "matching", "nonmatching")
                .apply("location");
        assertThat(result).isEqualTo("missing");
    }

    /**
     * When the sort field is not found due to being null, the 'missing' value should be returned.
     */
    @Test
    public void valueWhenMatchesSortAsc_FieldIsNull_NotFound() {
        String query = "city=melbourne&state=vic&postcode=3000&sort=city,desc&sort=postcode";
        QueryStringHelper helper = new QueryStringHelper();
        String result = helper.valueWhenMatchesSortAsc(query, "missing", "matching", "nonmatching")
                .apply(null);
        assertThat(result).isEqualTo("missing");
    }

    /**
     * When the sort field is not found due to being empty, the 'missing' value should be returned.
     */
    @Test
    public void valueWhenMatchesSortAsc_FieldIsEmpty_NotFound() {
        String query = "city=melbourne&state=vic&postcode=3000&sort=city,desc&sort=postcode";
        QueryStringHelper helper = new QueryStringHelper();
        String result = helper.valueWhenMatchesSortAsc(query, "missing", "matching", "nonmatching")
                .apply("");
        assertThat(result).isEqualTo("missing");
    }

    /**
     * When the sort field is provided but is not found, the 'missing' value should be returned.
     */
    @Test
    public void valueWhenMatchesSortAsc_FieldNotFound() {
        String query = "city=melbourne&state=vic&postcode=3000&sort=city,desc&sort=postcode";
        QueryStringHelper helper = new QueryStringHelper();
        String result = helper.valueWhenMatchesSortAsc(query, "missing", "matching", "nonmatching")
                .apply("country");
        assertThat(result).isEqualTo("missing");
    }

    /**
     * Consider 'postcode'. 'postcode' is the field to find which succeeds and has no explicit direction.
     * The trailing 'Asc' in 'valueWhenMatchesSortAsc' implies 'postcode' is being sorted by 'asc' which
     * means it matches and returns the 'matching' value.
     *
     * This is just saying that 'postcode' really is 'postcode,asc' since we are telling it to be by using
     * the trailing 'Asc' for valueWhenMatchesSortAsc. This is why it will return the matching value.
     */
    @Test
    public void valueWhenMatchesSortAsc_FieldFound_MatchesImplicitAscSort() {
        String query = "city=melbourne&state=vic&postcode=3000&sort=city,desc&sort=postcode";
        QueryStringHelper helper = new QueryStringHelper();
        String result = helper.valueWhenMatchesSortAsc(query, "missing", "matching", "nonMatching")
                .apply("postcode");
        assertThat(result).isEqualTo("matching");
    }

    /**
     * Consider 'postcode'. 'postcode' has explicit 'asc' direction.
     * The trailing 'Asc' in 'valueWhenMatchesSortAsc' matches the explicit direction therefore the 'matching' value
     * is returned.
     *
     * Here we are making sure that the explicit postcode sort direction of 'asc' is the same as 'asc' since we
     * are using the 'asc' version of valueWhenMatchesSortAsc. Since both match 'asc' the matching value is returned.
     */
    @Test
    public void valueWhenMatchesSortAsc_FieldFound_MatchesExplicitAscSort() {
        String query = "city=melbourne&state=vic&postcode=3000&sort=city,desc&sort=postcode,asc";
        QueryStringHelper helper = new QueryStringHelper();
        String result = helper.valueWhenMatchesSortAsc(query, "missing", "matching", "nonMatching")
                .apply("postcode");
        assertThat(result).isEqualTo("matching");
    }

    /**
     * Consider 'postcode'. 'postcode' has explicit 'desc' direction.
     * The trailing 'Asc' in 'valueWhenMatchesSortAsc' means 'asc' will be compared to 'desc' which wont match resulting
     * in the 'nonMatchingValue' being returned.
     */
    @Test
    public void valueWhenMatchesSortAsc_FieldFound_NonMatchingExplicitDescSort() {
        String query = "city=melbourne&state=vic&postcode=3000&sort=city,desc&sort=postcode,desc";
        QueryStringHelper helper = new QueryStringHelper();
        String result = helper.valueWhenMatchesSortAsc(query, "missing", "matching", "nonMatching")
                .apply("postcode");
        assertThat(result).isEqualTo("nonMatching");
    }

    /**
     * When the sort field is not found due to being null, the 'missing' value should be returned.
     */
    @Test
    public void valueWhenMatchesSortDesc_FieldIsNull_NotFound() {
        String query = "city=melbourne&state=vic&postcode=3000&sort=city,desc&sort=postcode";
        QueryStringHelper helper = new QueryStringHelper();
        String result = helper.valueWhenMatchesSortDesc(query, "missing", "matching", "nonmatching")
                .apply(null);
        assertThat(result).isEqualTo("missing");
    }


    @Test
    public void valueWhenMatchesSortDesc_QueryStringIsNull_ReturnMissingValue() {
        QueryStringHelper helper = new QueryStringHelper();
        String result = helper.valueWhenMatchesSortDesc(null, "missing", "matching", "nonmatching")
                .apply("location");
        assertThat(result).isEqualTo("missing");
    }

    @Test
    public void valueWhenMatchesSortDesc_QueryStringIsEmpty_ReturnMissingValue() {
        QueryStringHelper helper = new QueryStringHelper();
        String result = helper.valueWhenMatchesSortDesc("", "missing", "matching", "nonmatching")
                .apply("location");
        assertThat(result).isEqualTo("missing");
    }

    /**
     * When the sort field is not found due to being empty, the 'missing' value should be returned.
     */
    @Test
    public void valueWhenMatchesSortDesc_FieldIsEmpty_NotFound() {
        String query = "city=melbourne&state=vic&postcode=3000&sort=city,desc&sort=postcode";
        QueryStringHelper helper = new QueryStringHelper();
        String result = helper.valueWhenMatchesSortDesc(query, "missing", "matching", "nonmatching")
                .apply("");
        assertThat(result).isEqualTo("missing");
    }

    /**
     * When the sort field is provided but is not found, the 'missing' value should be returned.
     */
    @Test
    public void valueWhenMatchesSortDesc_FieldNotFound() {
        String query = "city=melbourne&state=vic&postcode=3000&sort=city,desc&sort=postcode";
        QueryStringHelper helper = new QueryStringHelper();
        String result = helper.valueWhenMatchesSortDesc(query, "missing", "matching", "nonmatching")
                .apply("country");
        assertThat(result).isEqualTo("missing");
    }

    /**
     * Consider 'postcode'. 'postcode' is the field to find which succeeds and has no explicit direction.
     * The trailing 'Desc' in 'valueWhenMatchesSortDesc' implies 'postcode' is being sorted by 'desc' which
     * means it matches and returns the 'matching' value.
     *
     * This is just saying that 'postcode' really is 'postcode,desc' since we are telling it to be by using
     * the trailing 'Desc' for valueWhenMatchesSortDesc. This is why it will return the matching value.
     */
    @Test
    public void valueWhenMatchesSortDesc_FieldFound_MatchesImplicitDescSort() {
        String query = "city=melbourne&state=vic&postcode=3000&sort=city,desc&sort=postcode";
        QueryStringHelper helper = new QueryStringHelper();
        String result = helper.valueWhenMatchesSortDesc(query, "missing", "matching", "nonMatching")
                .apply("postcode");
        assertThat(result).isEqualTo("matching");
    }

    /**
     * Consider 'postcode'. 'postcode' has explicit 'desc' direction.
     * The trailing 'Desc' in 'valueWhenMatchesSortDesc' matches the explicit direction therefore the 'matching' value
     * is returned.
     *
     * Here we are making sure that the explicit postcode sort direction of 'desc' is the same as 'desc' since we
     * are using the 'desc' version of valueWhenMatchesSortDesc. Since both match 'desc' the matching value is returned.
     */
    @Test
    public void valueWhenMatchesSortDesc_FieldFound_MatchesExplicitDescSort() {
        String query = "city=melbourne&state=vic&postcode=3000&sort=city,desc&sort=postcode,desc";
        QueryStringHelper helper = new QueryStringHelper();
        String result = helper.valueWhenMatchesSortDesc(query, "missing", "matching", "nonMatching")
                .apply("postcode");
        assertThat(result).isEqualTo("matching");
    }

    /**
     * Consider 'postcode'. 'postcode' has explicit 'asc' direction.
     * The trailing 'Desc' in 'valueWhenMatchesSortDesc' means 'asc' will be compared to 'desc' which wont match resulting
     * in the 'nonMatchingValue' being returned.
     */
    @Test
    public void valueWhenMatchesSortDesc_FieldFound_NonMatchingExplicitDescSort() {
        String query = "city=melbourne&state=vic&postcode=3000&sort=city,desc&sort=postcode,asc";
        QueryStringHelper helper = new QueryStringHelper();
        String result = helper.valueWhenMatchesSortDesc(query, "missing", "matching", "nonMatching")
                .apply("postcode");
        assertThat(result).isEqualTo("nonMatching");
    }


    /**
     * When the query string is null and there are no field and direction values to add as the
     * new sort, an empty string is returned.
     */
    @Test
    public void createNewSort_QueryStringIsNull_NoFieldsToAdd_ReturnsEmptyString() {
        QueryStringHelper helper = new QueryStringHelper();

        String result = helper.createNewSort(null, Collections.emptyList());
        assertThat(result).isEmpty();
    }

    /**
     * When the query string is empty and there are no field and direction values to add as the
     * new sort, an empty string is returned.
     */
    @Test
    public void createNewSort_QueryStringIsEmpty_NoFieldsToAdd_ReturnsEmptyString() {
        QueryStringHelper helper = new QueryStringHelper();

        String result = helper.createNewSort("", Collections.emptyList());
        assertThat(result).isEmpty();
    }

    /**
     * When the query string is null and there are new sort field,direction values to add,
     * the query string should consist of only these new sort values.
     */
    @Test
    public void createNewSort_QueryStringIsNull_FieldsToAdd_ReturnsNewSortFields() {
        QueryStringHelper helper = new QueryStringHelper();

        List<String> fieldAndDirections = Arrays.asList("region,desc", "locale,asc");
        String expected = "sort=region,desc&sort=locale,asc";

        String result = helper.createNewSort(null, fieldAndDirections);
        assertThat(result).isEqualTo(expected);
    }

    /**
     * When the query string is empty and there are new sort field,direction values to add,
     * the query string should consist of only these new sort values.
     */
    @Test
    public void createNewSort_QueryStringIsEmpty_FieldsToAdd_ReturnsNewSortFields() {
        QueryStringHelper helper = new QueryStringHelper();

        List<String> fieldAndDirections = Arrays.asList("region,desc", "locale,asc");
        String expected = "sort=region,desc&sort=locale,asc";

        String result = helper.createNewSort("", fieldAndDirections);
        assertThat(result).isEqualTo(expected);
    }


    /**
     * When the query string exists but are no field,direction values to add, the query string should consist of
     * no sort values.
     */
    @Test
    public void createNewSort_NoFieldsToAdd_ReturnsNewSortFields() {
        String query = "city=melbourne&postcode=3000&sort=city&sort=postcode,desc";
        String expected = "city=melbourne&postcode=3000";

        QueryStringHelper helper = new QueryStringHelper();

        String result = helper.createNewSort(query, Collections.emptyList());
        assertThat(result).isEqualTo(expected);
    }
    /**
     * When the query string exists but and there are new field,direction values to add, the query string should
     * have the new sort values appended to the end.
     */
    @Test
    public void createNewSort_FieldsToAdd_ReturnsNewSortFields() {
        String query = "city=melbourne&postcode=3000&sort=city&sort=postcode,desc";
        String expected = "city=melbourne&postcode=3000&sort=region,desc&sort=locale,asc&sort=country";

        QueryStringHelper helper = new QueryStringHelper();

        List<String> fieldAndDirections = Arrays.asList("region,desc", "locale,asc", "country");

        String result = helper.createNewSort(query, fieldAndDirections);
        assertThat(result).isEqualTo(expected);
    }

    @Test
    public void isFieldSorted_QueryStringIsNull_ReturnFalse() {
        QueryStringHelper helper = new QueryStringHelper();
        boolean result = helper.isFieldSorted(null, "city");
        assertThat(result).isFalse();
    }

    @Test
    public void isFieldSorted_QueryStringIsEmpty_ReturnFalse() {
        QueryStringHelper helper = new QueryStringHelper();
        boolean result = helper.isFieldSorted("", "city");
        assertThat(result).isFalse();
    }

    @Test
    public void isFieldSorted_SortFieldDoesNotExist_ReturnFalse() {
        String query = "city=melbourne&postcode=3000&sort=city&sort=postcode,desc";

        QueryStringHelper helper = new QueryStringHelper();
        boolean result = helper.isFieldSorted(query, "state");
        assertThat(result).isFalse();
    }

    @Test
    public void isFieldSorted_SortFieldExists_ReturnTrue() {
        String query = "city=melbourne&postcode=3000&sort=city&sort=postcode,desc";

        QueryStringHelper helper = new QueryStringHelper();
        boolean result = helper.isFieldSorted(query, "postcode");
        assertThat(result).isTrue();
    }

    @Test
    public void getCurrentSortDirectionAsc_QueryStringIsNull_ReturnsNull() {
        QueryStringHelper helper = new QueryStringHelper();
        String result = helper.getCurrentSortDirectionAsc(null, "city");
        assertThat(result).isNull();
    }

    @Test
    public void getCurrentSortDirectionAsc_QueryStringIsEmpty_ReturnsNull() {
        QueryStringHelper helper = new QueryStringHelper();
        String result = helper.getCurrentSortDirectionAsc("", "city");
        assertThat(result).isNull();
    }

    @Test
    public void getCurrentSortDirectionAsc_SortFieldNotFound_ReturnsNull() {
        String query = "city=melbourne&postcode=3000&sort=locale&sort=postcode,desc";

        QueryStringHelper helper = new QueryStringHelper();
        String result = helper.getCurrentSortDirectionAsc(query, "city");
        assertThat(result).isNull();
    }

    /**
     * There are 2 getCurrentSortDirectionXXX methods. When the 'Asc' variant is used it means
     * the default sort direction is 'asc'. 'city' has no direction therefore the default direction of
     * 'asc' should be returned.
     */
    @Test
    public void getCurrentSortDirectionAsc_SortFieldFound_ImplicitDirection_ReturnsDefaultDirectionAsc() {
        String query = "city=melbourne&postcode=3000&sort=city&sort=postcode,desc";

        QueryStringHelper helper = new QueryStringHelper();
        String result = helper.getCurrentSortDirectionAsc(query, "city");
        assertThat(result).isEqualTo("asc");
    }

    /**
     * 'city' has an explicit direction of 'desc', therefore 'desc' should be returned.
     */
    @Test
    public void getCurrentSortDirectionAsc_SortFieldFound_ExplicitDirection_ReturnsExplicitDirection() {
        String query = "city=melbourne&postcode=3000&sort=city,desc&sort=postcode,desc";

        QueryStringHelper helper = new QueryStringHelper();
        String result = helper.getCurrentSortDirectionAsc(query, "city");
        assertThat(result).isEqualTo("desc");
    }

    /**
     * Make sure nested object direction is accessed correctly.
     */
    @Test
    public void getCurrentSortDirectionAsc_SortFieldFound_NestedObject_ReturnsExplicitDirection() {
        String query = "city=melbourne&postcode=3000&sort=city,desc&sort=address.city.postcode,desc";

        QueryStringHelper helper = new QueryStringHelper();
        String result = helper.getCurrentSortDirectionAsc(query, "address.city.postcode");
        assertThat(result).isEqualTo("desc");
    }

    @Test
    public void getCurrentSortDirectionDesc_QueryStringIsNull_ReturnsNull() {
        QueryStringHelper helper = new QueryStringHelper();
        String result = helper.getCurrentSortDirectionDesc(null, "city");
        assertThat(result).isNull();
    }

    @Test
    public void getCurrentSortDirectionDesc_QueryStringIsEmpty_ReturnsNull() {
        QueryStringHelper helper = new QueryStringHelper();
        String result = helper.getCurrentSortDirectionDesc("", "city");
        assertThat(result).isNull();
    }

    @Test
    public void getCurrentSortDirectionDesc_SortFieldNotFound_ReturnsNull() {
        String query = "city=melbourne&postcode=3000&sort=locale&sort=postcode,desc";

        QueryStringHelper helper = new QueryStringHelper();
        String result = helper.getCurrentSortDirectionDesc(query, "city");
        assertThat(result).isNull();
    }

    /**
     * There are 2 getCurrentSortDirectionXXX methods. When the 'Desc' variant is used it means
     * the default sort direction is 'desc'. 'city' has no direction therefore the default direction of
     * 'desc' should be returned.
     */
    @Test
    public void getCurrentSortDirectionDesc_SortFieldFound_ImplicitDirection_ReturnsDefaultDirectionAsc() {
        String query = "city=melbourne&postcode=3000&sort=city&sort=postcode,asc";

        QueryStringHelper helper = new QueryStringHelper();
        String result = helper.getCurrentSortDirectionDesc(query, "city");
        assertThat(result).isEqualTo("desc");
    }

    /**
     * 'city' has an explicit direction of 'asc', therefore 'asc' should be returned.
     */
    @Test
    public void getCurrentSortDirectionDesc_SortFieldFound_ExplicitDirection_ReturnsExplicitDirection() {
        String query = "city=melbourne&postcode=3000&sort=city,asc&sort=postcode,desc";

        QueryStringHelper helper = new QueryStringHelper();
        String result = helper.getCurrentSortDirectionDesc(query, "city");
        assertThat(result).isEqualTo("asc");
    }

    /**
     * Make sure nested object direction is accessed correctly.
     */
    @Test
    public void getCurrentSortDirectionDesc_SortFieldFound_NestedObject_ReturnsExplicitDirection() {
        String query = "city=melbourne&postcode=3000&sort=city,desc&sort=address.city.postcode,asc";

        QueryStringHelper helper = new QueryStringHelper();
        String result = helper.getCurrentSortDirectionDesc(query, "address.city.postcode");
        assertThat(result).isEqualTo("asc");
    }
}
