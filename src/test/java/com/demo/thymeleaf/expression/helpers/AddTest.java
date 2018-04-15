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
import static org.mockito.Mockito.verifyNoMoreInteractions;

public class AddTest {

    /**
     * When null is added, the original query string should be returned.
     */
    @Test
    public void add_HandlesNull_HasNoEffect() {
        String query = "key4=ValueA&key2=ValueB&key3=ValueC";

        HttpServletRequest mockHttpRequest = mock(HttpServletRequest.class);
        when(mockHttpRequest.getQueryString()).thenReturn(query);

        QueryStringHelper helper = new QueryStringHelper();
        String result = helper.add(mockHttpRequest, null, null);
        assertThat(result).isEqualTo(query);

        verify(mockHttpRequest, times(1)).getQueryString();
        verifyNoMoreInteractions(mockHttpRequest);
    }

    /**
     * When a key that does not exist is added, it should be added to the end of the query string.
     *
     * eg: key99 does not exist so 'key99=Added' is added to the end.
     */
    @Test
    public void add_UniqueKey_AddsToEnd() {
        String query = "key4=ValueA&key2=ValueB&key3=ValueC";
        String expected = "key4=ValueA&key2=ValueB&key3=ValueC&key99=Added";

        HttpServletRequest mockHttpRequest = mock(HttpServletRequest.class);
        when(mockHttpRequest.getQueryString()).thenReturn(query);

        QueryStringHelper helper = new QueryStringHelper();
        String result = helper.add(mockHttpRequest, "key99", "Added");
        assertThat(result).isEqualTo(expected);

        verify(mockHttpRequest, times(1)).getQueryString();
        verifyNoMoreInteractions(mockHttpRequest);
    }

    /**
     * If the key is empty it should have no effect and return the original query string.
     */
    @Test
    public void add_HandlesEmptyKey() {
        String query = "key4=ValueA&key2=ValueB&key3=ValueC";

        HttpServletRequest mockHttpRequest = mock(HttpServletRequest.class);
        when(mockHttpRequest.getQueryString()).thenReturn(query);

        QueryStringHelper helper = new QueryStringHelper();
        String result = helper.add(mockHttpRequest, "", "valueX");
        assertThat(result).isEqualTo(query);

        verify(mockHttpRequest, times(1)).getQueryString();
        verifyNoMoreInteractions(mockHttpRequest);
    }

    /**
     * If the key is empty it should have no effect and return the original query string.
     * This test checks to make sure white space is trimmed in the empty check.
     */
    @Test
    public void add_HandlesEmptyKey_Trim() {
        String query = "key4=ValueA&key2=ValueB&key3=ValueC";

        HttpServletRequest mockHttpRequest = mock(HttpServletRequest.class);
        when(mockHttpRequest.getQueryString()).thenReturn(query);

        QueryStringHelper helper = new QueryStringHelper();
        String result = helper.add(mockHttpRequest, "      ", "valueX");
        assertThat(result).isEqualTo(query);

        verify(mockHttpRequest, times(1)).getQueryString();
        verifyNoMoreInteractions(mockHttpRequest);
    }

    /**
     * If the value is empty it should have no effect and return the original query string.
     */
    @Test
    public void add_HandlesEmptyValue() {
        String query = "key4=ValueA&key2=ValueB&key3=ValueC";

        HttpServletRequest mockHttpRequest = mock(HttpServletRequest.class);
        when(mockHttpRequest.getQueryString()).thenReturn(query);

        QueryStringHelper helper = new QueryStringHelper();
        String result = helper.add(mockHttpRequest, "key10", "");
        assertThat(result).isEqualTo(query);

        verify(mockHttpRequest, times(1)).getQueryString();
        verifyNoMoreInteractions(mockHttpRequest);
    }

    /**
     * If the value is empty it should have no effect and return the original query string.
     * This test checks to make sure white space is trimmed in the empty check.
     */
    @Test
    public void add_HandlesEmptyValue_Trim() {
        String query = "key4=ValueA&key2=ValueB&key3=ValueC";

        HttpServletRequest mockHttpRequest = mock(HttpServletRequest.class);
        when(mockHttpRequest.getQueryString()).thenReturn(query);

        QueryStringHelper helper = new QueryStringHelper();
        String result = helper.add(mockHttpRequest, "key10", "          ");
        assertThat(result).isEqualTo(query);

        verify(mockHttpRequest, times(1)).getQueryString();
        verifyNoMoreInteractions(mockHttpRequest);
    }

    /**
     * When a key already exists and is receiving a new value, it should be added to the end only if the value does
     * not exist.
     *
     * Eg: key2 occurs 3 times with [ValueB, ValueE, ValueF]. The new value 'Added' is unique so 'key2=Added' is added
     * to the end.
     */
    @Test
    public void add_DuplicateKey_AddsToEnd() {
        String query = "key4=ValueA&key2=ValueB&key3=ValueC&key2=ValueE&key2=ValueF&key9=ValueK";
        String expected = "key4=ValueA&key2=ValueB&key3=ValueC&key2=ValueE&key2=ValueF&key9=ValueK&key2=Added";

        HttpServletRequest mockHttpRequest = mock(HttpServletRequest.class);
        when(mockHttpRequest.getQueryString()).thenReturn(query);

        QueryStringHelper helper = new QueryStringHelper();
        String result = helper.add(mockHttpRequest, "key2", "Added");
        assertThat(result).isEqualTo(expected);

        verify(mockHttpRequest, times(1)).getQueryString();
        verifyNoMoreInteractions(mockHttpRequest);
    }

    /**
     * When the same key and value already exist, nothing should get added and the original query string should be
     * returned.
     */
    @Test
    public void add_DuplicateKey_ValueExists_HasNoEffect() {
        String query = "key4=ValueA&key2=AlreadyExists&key3=ValueC&key2=ValueE&key2=ValueF&key9=ValueK";

        HttpServletRequest mockHttpRequest = mock(HttpServletRequest.class);
        when(mockHttpRequest.getQueryString()).thenReturn(query);

        QueryStringHelper helper = new QueryStringHelper();
        String result = helper.add(mockHttpRequest, "key2", "AlreadyExists");
        assertThat(result).isEqualTo(query);

        verify(mockHttpRequest, times(1)).getQueryString();
        verifyNoMoreInteractions(mockHttpRequest);
    }

    /**
     * When the same key and value already exist, nothing should get added and the original query string should be
     * returned.
     *
     * This test checks escaping is working as expected. The implementation must unescape the original query string
     * so the provided value can be matched correctly for example.
     */
    @Test
    public void add_DuplicateKey_ValueExists_WithEscaping_HasNoEffect() {
        String query = "key4=ValueA&key2=%20Already%20%20%20%20Exists%20&key3=ValueC&key2=ValueE&key2=ValueF&key9=ValueK";

        HttpServletRequest mockHttpRequest = mock(HttpServletRequest.class);
        when(mockHttpRequest.getQueryString()).thenReturn(query);

        QueryStringHelper helper = new QueryStringHelper();
        String result = helper.add(mockHttpRequest, "key2", " Already    Exists ");
        assertThat(result).isEqualTo(query);

        verify(mockHttpRequest, times(1)).getQueryString();
        verifyNoMoreInteractions(mockHttpRequest);
    }

    /**
     * When argument is null, the original query string should be returned.
     */
    @Test
    public void addAll_HandlesNull_HasNoEffect() {
        String query = "key4=ValueA&key2=ValueB&key3=ValueC";

        HttpServletRequest mockHttpRequest = mock(HttpServletRequest.class);
        when(mockHttpRequest.getQueryString()).thenReturn(query);

        QueryStringHelper helper = new QueryStringHelper();
        String result = helper.addAll(mockHttpRequest, null);
        assertThat(result).isEqualTo(query);

        verify(mockHttpRequest, times(1)).getQueryString();
        verifyNoMoreInteractions(mockHttpRequest);
    }

    /**
     * When a pair has an empty key it should be ignored.
     */
    @Test
    public void addAll_InvalidEmptyKeyInPair_HasNoEffect() {
        String query = "key4=ValueA&key2=ValueB&key3=ValueC";

        // Simulate SpEL expression for 2d list.
        List<List<String>> instructions = new ArrayList<>();
        instructions.add(new ArrayList<>(Arrays.asList("", "Added2")));

        HttpServletRequest mockHttpRequest = mock(HttpServletRequest.class);
        when(mockHttpRequest.getQueryString()).thenReturn(query);

        QueryStringHelper helper = new QueryStringHelper();
        String result = helper.addAll(mockHttpRequest, instructions);
        assertThat(result).isEqualTo(query);

        verify(mockHttpRequest, times(1)).getQueryString();
        verifyNoMoreInteractions(mockHttpRequest);
    }

    /**
     * When a pair has an empty key it should be ignored. Whitespace should be trimmed in empty check.
     */
    @Test
    public void addAll_InvalidEmptyKeyInPair_TrimSpace_HasNoEffect() {
        String query = "key4=ValueA&key2=ValueB&key3=ValueC";

        // Simulate SpEL expression for 2d list.
        List<List<String>> instructions = new ArrayList<>();
        instructions.add(new ArrayList<>(Arrays.asList("    ", "Added2")));

        HttpServletRequest mockHttpRequest = mock(HttpServletRequest.class);
        when(mockHttpRequest.getQueryString()).thenReturn(query);

        QueryStringHelper helper = new QueryStringHelper();
        String result = helper.addAll(mockHttpRequest, instructions);
        assertThat(result).isEqualTo(query);

        verify(mockHttpRequest, times(1)).getQueryString();
        verifyNoMoreInteractions(mockHttpRequest);
    }

    /**
     * When a pair has an empty value it should be ignored.
     */
    @Test
    public void addAll_InvalidEmptyValueInPair_HasNoEffect() {
        String query = "key4=ValueA&key2=ValueB&key3=ValueC";

        // Simulate SpEL expression for 2d list.
        List<List<String>> instructions = new ArrayList<>();
        instructions.add(new ArrayList<>(Arrays.asList("key4", "")));

        HttpServletRequest mockHttpRequest = mock(HttpServletRequest.class);
        when(mockHttpRequest.getQueryString()).thenReturn(query);

        QueryStringHelper helper = new QueryStringHelper();
        String result = helper.addAll(mockHttpRequest, instructions);
        assertThat(result).isEqualTo(query);

        verify(mockHttpRequest, times(1)).getQueryString();
        verifyNoMoreInteractions(mockHttpRequest);
    }

    /**
     * When a pair has an empty value it should be ignored. Whitespace should be trimmed in empty check.
     */
    @Test
    public void addAll_InvalidEmptyValueInPair_TrimSpace_HasNoEffect() {
        String query = "key4=ValueA&key2=ValueB&key3=ValueC";

        // Simulate SpEL expression for 2d list.
        List<List<String>> instructions = new ArrayList<>();
        instructions.add(new ArrayList<>(Arrays.asList("key4", "       ")));

        HttpServletRequest mockHttpRequest = mock(HttpServletRequest.class);
        when(mockHttpRequest.getQueryString()).thenReturn(query);

        QueryStringHelper helper = new QueryStringHelper();
        String result = helper.addAll(mockHttpRequest, instructions);
        assertThat(result).isEqualTo(query);

        verify(mockHttpRequest, times(1)).getQueryString();
        verifyNoMoreInteractions(mockHttpRequest);
    }

    /**
     * 1 key value pair gets added
     */
    @Test
    public void addAll_OnePair() {
        String query = "key4=ValueA&key2=ValueB&key3=ValueC";
        String expected = "key4=ValueA&key2=ValueB&key3=ValueC&key8=Added2";

        // Simulate SpEL expression for 2d list.
        // {{'key2','Added2'}}
        List<List<String>> instructions = new ArrayList<>();
        instructions.add(new ArrayList<>(Arrays.asList("key8", "Added2")));

        HttpServletRequest mockHttpRequest = mock(HttpServletRequest.class);
        when(mockHttpRequest.getQueryString()).thenReturn(query);

        QueryStringHelper helper = new QueryStringHelper();
        String result = helper.addAll(mockHttpRequest, instructions);
        assertThat(result).isEqualTo(expected);

        verify(mockHttpRequest, times(1)).getQueryString();
        verifyNoMoreInteractions(mockHttpRequest);
    }

    /**
     * Many key value pairs should get added to the end. Any illegal key value pairs should have no effect rather than
     * throwing errors.
     */
    @Test
    public void addAll_ManyPairsAddedToEnd() {
        String query = "key4=ValueA&key2=ValueB&key3=ValueC";
        String expected = "key4=ValueA&key2=ValueB&key3=ValueC&key2=Added2&key2=Added3&key99=Added99";

        // Simulate SpEL expression for 2d list.
        // {{'key2','Added2'}, {'key2', 'Added3'}, {'key99', 'Added99'}}
        List<List<String>> instructions = new ArrayList<>();
        instructions.add(new ArrayList<>(Arrays.asList("key2", "Added2")));
        instructions.add(new ArrayList<>(Arrays.asList("key2", "Added3")));
        instructions.add(new ArrayList<>(Arrays.asList("key99", "Added99")));

        // Should be ignored since there must be a key and value.
        instructions.add(new ArrayList<>(Collections.singletonList("keyOnly")));
        instructions.add(new ArrayList<>(Arrays.asList("", "Added99")));
        instructions.add(new ArrayList<>(Arrays.asList("key100", "")));
        instructions.add(new ArrayList<>(Arrays.asList("", "")));

        HttpServletRequest mockHttpRequest = mock(HttpServletRequest.class);
        when(mockHttpRequest.getQueryString()).thenReturn(query);

        QueryStringHelper helper = new QueryStringHelper();
        String result = helper.addAll(mockHttpRequest, instructions);
        assertThat(result).isEqualTo(expected);

        verify(mockHttpRequest, times(1)).getQueryString();
        verifyNoMoreInteractions(mockHttpRequest);
    }

    /**
     * Every nested list needs a key and value otherwise it should be ignored.
     */
    @Test
    public void addAll_OnePair_InvalidPair_HasNoEffect() {
        String query = "key4=ValueA&key2=ValueB&key3=ValueC";

        // Simulate SpEL expression for 2d list that is missing a value.
        // {{'key8'}}
        List<List<String>> instructions = new ArrayList<>();
        instructions.add(new ArrayList<>(Collections.singletonList("key8")));

        HttpServletRequest mockHttpRequest = mock(HttpServletRequest.class);
        when(mockHttpRequest.getQueryString()).thenReturn(query);

        QueryStringHelper helper = new QueryStringHelper();
        String result = helper.addAll(mockHttpRequest, instructions);
        assertThat(result).isEqualTo(query);

        verify(mockHttpRequest, times(1)).getQueryString();
        verifyNoMoreInteractions(mockHttpRequest);
    }

    /**
     * Ensure escaping white space is working. Eg white space is converted to %20 etc.
     *
     * Eg: if you add 'Added   3', it should be escaped in the expected query string.
     */
    @Test
    public void addAll_ManyPairsAddedToEnd_EscapingActive() {
        String query = "key4=ValueA&key2=ValueB&key3=ValueC";
        String expected = "key4=ValueA&key2=ValueB&key3=ValueC&key2=Added%202&key2=Added%20%203&key99=Added%2099%20";

        // Simulate SpEL expression for 2d list.
        // {{'key2','Added2'}, {'key2', 'Added3'}, {'key99', 'Added99'}}
        List<List<String>> instructions = new ArrayList<>();
        instructions.add(new ArrayList<>(Arrays.asList("key2", "Added 2")));
        instructions.add(new ArrayList<>(Arrays.asList("key2", "Added  3")));
        instructions.add(new ArrayList<>(Arrays.asList("key99", "Added 99 ")));

        HttpServletRequest mockHttpRequest = mock(HttpServletRequest.class);
        when(mockHttpRequest.getQueryString()).thenReturn(query);

        QueryStringHelper helper = new QueryStringHelper();
        String result = helper.addAll(mockHttpRequest, instructions);
        assertThat(result).isEqualTo(expected);

        verify(mockHttpRequest, times(1)).getQueryString();
        verifyNoMoreInteractions(mockHttpRequest);
    }

    /**
     * Notice the middle nested array only has 1 item which is invalid as a key is missing. All invalid items
     * are not included.
     */
    @Test
    public void addAll_ManyPairsWithInvalidPair_OnlyValidPairsAdded() {
        String query = "key4=ValueA&key2=ValueB&key3=ValueC";
        String expected = "key4=ValueA&key2=ValueB&key3=ValueC&key2=Added%202&key99=Added%2099%20";

        // Simulate SpEL expression for 2d list.
        // {{'key2','Added 2'}, {'Added3'}, {'key99', 'Added 99 '}}
        List<List<String>> instructions = new ArrayList<>();
        instructions.add(Arrays.asList("key2", "Added 2"));
        instructions.add(Arrays.asList("Added3"));
        instructions.add(Arrays.asList("key99", "Added 99 "));

        HttpServletRequest mockHttpRequest = mock(HttpServletRequest.class);
        when(mockHttpRequest.getQueryString()).thenReturn(query);

        QueryStringHelper helper = new QueryStringHelper();
        String result = helper.addAll(mockHttpRequest, instructions);
        assertThat(result).isEqualTo(expected);

        verify(mockHttpRequest, times(1)).getQueryString();
        verifyNoMoreInteractions(mockHttpRequest);
    }

    /**
     * Sanity check to ensure that when the exact same keys and values are added, the original query
     * string should be returned.
     */
    @Test
    public void addAll_ManyPairsAlreadyExist_ShouldHaveNoEffect() {
        String query = "key4=ValueA&key2=Value%20B&key3=%20ValueC%20&key4=%20Value%20CC%20";

        // Simulate SpEL expression for 2d list.
        List<List<String>> instructions = new ArrayList<>();
        instructions.add(Arrays.asList("key4", "ValueA"));
        instructions.add(Arrays.asList("key2", "Value B"));
        instructions.add(Arrays.asList("key3", " ValueC "));
        instructions.add(Arrays.asList("key4", " Value CC "));

        HttpServletRequest mockHttpRequest = mock(HttpServletRequest.class);
        when(mockHttpRequest.getQueryString()).thenReturn(query);

        QueryStringHelper helper = new QueryStringHelper();
        String result = helper.addAll(mockHttpRequest, instructions);
        assertThat(result).isEqualTo(query);

        verify(mockHttpRequest, times(1)).getQueryString();
        verifyNoMoreInteractions(mockHttpRequest);
    }
}
