package com.demo.thymeleaf.expression.helpers;

import com.demo.thymeleaf.expression.QueryStringHelper;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class GetTest {

    /**
     * Given the key wont exist in a null query string, null should be returned which behaves the same
     * as if the key was not found in an existing query string.
     */
    @Test
    public void getFirstValue_QueryStringIsNull_ReturnNull() {
        QueryStringHelper helper = new QueryStringHelper();
        String result = helper.getFirstValue(null, "key2");
        assertThat(result).isNull();
    }

    /**
     * Given the key wont exist in an empty query string, null should be returned which behaves the same
     * as if the key was not found in an existing query string.
     */
    @Test
    public void getFirstValue_QueryStringIsEmpty_ReturnNull() {
        QueryStringHelper helper = new QueryStringHelper();
        String result = helper.getFirstValue("", "key2");
        assertThat(result).isNull();
    }

    /**
     * When the key is not found, null should be returned.
     */
    @Test
    public void getFirstValue_KeyNotFound_ReturnNull() {
        String query = "key4=ValueA&key2=ValueB&key3=ValueC&key2=ValueD&key4=ValueE&key2=ValueB";

        QueryStringHelper helper = new QueryStringHelper();
        String result = helper.getFirstValue(query, "missing");
        assertThat(result).isNull();
    }

    /**
     * key2 occurs 2 times with ValueB and ValueC. Only the first occurrence of ValueB should be returned.
     */
    @Test
    public void getFirstValue_DuplicateKeys_OnlyFirstValueReturned() {
        String query = "key4=ValueA&key2=ValueB&key3=ValueC&key2=ValueD&key4=ValueE&key2=ValueC";
        String expected = "ValueB";

        QueryStringHelper helper = new QueryStringHelper();
        String result = helper.getFirstValue(query, "key2");
        assertThat(result).isEqualTo(expected);
    }

    /**
     * Asserts the result should be the unescaped query string value.
     */
    @Test
    public void getFirstValue_DuplicateKeys_OnlyFirstValueReturned_WithEscaping() {
        String query = "key4=ValueA&key2=Value%20B&key3=ValueC&key2=ValueD&key4=ValueE&key2=Value%20C";
        String expected = "Value B";

        QueryStringHelper helper = new QueryStringHelper();
        String result = helper.getFirstValue(query, "key2");
        assertThat(result).isEqualTo(expected);
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

        QueryStringHelper helper = new QueryStringHelper();
        String result = helper.getFirstValue(query, "key2");
        assertThat(result).isEqualTo(expected);
    }

    /**
     * When the key exists but query string is null, an empty list should be returned.
     */
    @Test
    public void getAllValues_QueryStringIsNull_ReturnsEmptyList() {
        QueryStringHelper helper = new QueryStringHelper();
        List<String> result = helper.getAllValues(null, "key2");
        assertThat(result).isEmpty();
    }

    /**
     * When the key argument is null, an empty list should be returned.
     */
    @Test
    public void getAllValues_QueryStringIsEmpty_ReturnsEmptyList() {
        QueryStringHelper helper = new QueryStringHelper();
        List<String> result = helper.getAllValues("", "key2");
        assertThat(result).isEmpty();
    }

    /**
     * When the key is not found, an empty list should be returned.
     */
    @Test
    public void getAllValues_KeyNotFound_EmptyListReturned() {
        String query = "key4=ValueA&key2=Value%20B&key3=ValueC&key2=ValueD&key4=ValueE&key2=Value%20B";

        QueryStringHelper helper = new QueryStringHelper();
        List<String> result = helper.getAllValues(query, "missing");
        assertThat(result).isEmpty();
    }

    /**
     * key2 occurs 3 times both with the values ValueX,ValueY,ValueZ. The expected result is a list containing
     * the same 3 values in the exact same order as the relative indexes.
     */
    @Test
    public void getAllValues_DuplicateKeys_AllValuesReturnedInTheSameOrder() {
        String query = "key4=ValueA&key2=ValueX&key3=ValueC&key2=ValueY&key4=ValueE&key2=ValueZ";
        List<String> expected = new ArrayList<>(Arrays.asList("ValueX", "ValueY", "ValueZ"));

        QueryStringHelper helper = new QueryStringHelper();
        List<String> result = helper.getAllValues(query, "key2");
        assertThat(result).isEqualTo(expected);
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

        QueryStringHelper helper = new QueryStringHelper();
        List<String> result = helper.getAllValues(query, "key2");
        assertThat(result).isEqualTo(expected);
    }

    /**
     * key8 occurs only once, therefore the resulting list should only contain the key8 value.
     */
    @Test
    public void getAllValues_SingleKey_ReturnsSingleValue() {
        String query = "key4=ValueA&key2=ValueB&key3=ValueC&key8=%20%20Value8%20833.34.384%20%205&key2=ValueD&key4=ValueE&key2=ValueC";
        List<String> expected = new ArrayList<>(Collections.singletonList("  Value8 833.34.384  5"));

        QueryStringHelper helper = new QueryStringHelper();
        List<String> result = helper.getAllValues(query, "key8");
        assertThat(result).isEqualTo(expected);
    }
}
