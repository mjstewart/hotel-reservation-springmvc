package com.demo.thymeleaf.expression.helpers;

import com.demo.thymeleaf.expression.QueryStringHelper;
import org.junit.Test;

import javax.servlet.http.HttpServletRequest;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

public class HybridTest {

    /**
     * When there are null arguments, the original query string should be returned.
     */
    @Test
    public void removeAllAndAdd_HandlesNull_HasNoEffect() {
        String query = "key4=ValueA&key2=ValueB&key3=ValueC";

        HttpServletRequest mockHttpRequest = mock(HttpServletRequest.class);
        when(mockHttpRequest.getQueryString()).thenReturn(query);

        QueryStringHelper helper = new QueryStringHelper();

        String result = helper.removeAndAdd(mockHttpRequest, null, null);
        assertThat(result).isEqualTo(query);

        verify(mockHttpRequest, times(1)).getQueryString();
        verifyNoMoreInteractions(mockHttpRequest);
    }

    /**
     * When the arguments are empty, the original query string should be returned.
     */
    @Test
    public void removeAllAndAdd_EmptyRemoveAndAddLists_HasNoEffect() {
        String query = "key4=ValueA&key2=ValueB&key3=ValueC";

        HttpServletRequest mockHttpRequest = mock(HttpServletRequest.class);
        when(mockHttpRequest.getQueryString()).thenReturn(query);

        QueryStringHelper helper = new QueryStringHelper();

        List<String> removeKeys = new ArrayList<>();
        List<List<String>> addKeyValues = new ArrayList<>();

        String result = helper.removeAndAdd(mockHttpRequest, removeKeys, addKeyValues);
        assertThat(result).isEqualTo(query);

        verify(mockHttpRequest, times(1)).getQueryString();
        verifyNoMoreInteractions(mockHttpRequest);
    }

    /**
     * When the remove key is not found, the new key/value pair should still be added to the end.
     */
    @Test
    public void removeAllAndAdd_RemoveKeyNotFound_StillAdd_WithEscaping() {
        String query = "key4=ValueA&key2=ValueB&key3=ValueC";
        String expected = "key4=ValueA&key2=ValueB&key3=ValueC&keyNew=New%20Value%20%20";

        HttpServletRequest mockHttpRequest = mock(HttpServletRequest.class);
        when(mockHttpRequest.getQueryString()).thenReturn(query);

        QueryStringHelper helper = new QueryStringHelper();

        List<String> removeKeys = Arrays.asList("missing");
        List<List<String>> addKeyValues = new ArrayList<>();
        addKeyValues.add(Arrays.asList("keyNew", "New Value  "));

        String result = helper.removeAndAdd(mockHttpRequest, removeKeys, addKeyValues);
        assertThat(result).isEqualTo(expected);

        verify(mockHttpRequest, times(1)).getQueryString();
        verifyNoMoreInteractions(mockHttpRequest);
    }

    /**
     * When all remove keys are not found, the new key/value pair should be added to the end.
     */
    @Test
    public void removeAllAndAdd_AllRemoveKeysNotFound_StillAdd() {
        String query = "key4=ValueA&key2=ValueB&key3=ValueC";
        String expected = "key4=ValueA&key2=ValueB&key3=ValueC&keyNew=New%20Value";

        HttpServletRequest mockHttpRequest = mock(HttpServletRequest.class);
        when(mockHttpRequest.getQueryString()).thenReturn(query);

        QueryStringHelper helper = new QueryStringHelper();

        List<String> removeKeys = Arrays.asList("missing1", "missing2", "missing3");
        List<List<String>> addKeyValues = new ArrayList<>();
        addKeyValues.add(Arrays.asList("keyNew", "New Value"));

        String result = helper.removeAndAdd(mockHttpRequest, removeKeys, addKeyValues);
        assertThat(result).isEqualTo(expected);

        verify(mockHttpRequest, times(1)).getQueryString();
        verifyNoMoreInteractions(mockHttpRequest);
    }

    /**
     * When the remove key is found, ALL occurrences of the remove key shall be removed.
     * The new key,value pair should be added to the end.
     */
    @Test
    public void removeAllAndAdd_SingleRemoveAndAddKeys() {
        String query = "key4=ValueA&key2=ValueB&key3=ValueC&key4=ValueX&key4=ValueY";
        String expected = "key2=ValueB&key3=ValueC&keyNew=New%20Value";

        HttpServletRequest mockHttpRequest = mock(HttpServletRequest.class);
        when(mockHttpRequest.getQueryString()).thenReturn(query);

        // Note: 1 remove key and 1 add key/value pair.
        List<String> removeKeys = Arrays.asList("key4");
        List<List<String>> addKeyValues = new ArrayList<>();
        addKeyValues.add(Arrays.asList("keyNew", "New Value"));

        QueryStringHelper helper = new QueryStringHelper();
        String result = helper.removeAndAdd(mockHttpRequest, removeKeys, addKeyValues);

        assertThat(result).isEqualTo(expected);

        verify(mockHttpRequest, times(1)).getQueryString();
        verifyNoMoreInteractions(mockHttpRequest);
    }

    /**
     * When the remove key is found, ALL occurrences of the remove key shall be removed with missing keys ignored.
     * The new key,value pair should be added to the end.
     */
    @Test
    public void removeAllAndAdd_ManyRemoveAndAddKeys() {
        String query = "key4=ValueA&key4=ValueAA&key2=ValueB&key3=ValueC&key4=ValueX&key4=ValueY&key2=YX3.23b";
        String expected = "key3=ValueC&keyNew2=newValue2&keyNew3=newValue%203";

        HttpServletRequest mockHttpRequest = mock(HttpServletRequest.class);
        when(mockHttpRequest.getQueryString()).thenReturn(query);

        // Note: 3 remove keys and 2 add key/value pair.
        List<String> removeKeys = Arrays.asList("key4", "missing", "key2");
        List<List<String>> addKeyValues = new ArrayList<>();
        addKeyValues.add(Arrays.asList("keyNew2", "newValue2"));
        addKeyValues.add(Arrays.asList("keyNew3", "newValue 3"));

        QueryStringHelper helper = new QueryStringHelper();
        String result = helper.removeAndAdd(mockHttpRequest, removeKeys, addKeyValues);

        assertThat(result).isEqualTo(expected);

        verify(mockHttpRequest, times(1)).getQueryString();
        verifyNoMoreInteractions(mockHttpRequest);
    }

    /**
     * Check that whitespace is escaped etc.
     */
    @Test
    public void removeAllAndAdd_ManyRemoveAndAddKeys_WithEscaping() {
        String query = "key4=ValueA&key4=ValueAA&key2=ValueB&key3=ValueC&key4=ValueX&key4=ValueY&key2=YX3.23b";
        String expected = "key3=ValueC&keyNew2=%20%20New%20Value%202&keyNew3=%20%20New%20.%20%20Value%203%20";

        HttpServletRequest mockHttpRequest = mock(HttpServletRequest.class);
        when(mockHttpRequest.getQueryString()).thenReturn(query);

        // Note: 2 remove keys with 1 missing and add 2 key/value pairs.
        List<String> removeKeys = Arrays.asList("key4", "missing", "key2");
        List<List<String>> addKeyValues = new ArrayList<>();
        addKeyValues.add(Arrays.asList("keyNew2", "  New Value 2"));
        addKeyValues.add(Arrays.asList("keyNew3", "  New .  Value 3 "));

        QueryStringHelper helper = new QueryStringHelper();
        String result = helper.removeAndAdd(mockHttpRequest, removeKeys, addKeyValues);

        assertThat(result).isEqualTo(expected);

        verify(mockHttpRequest, times(1)).getQueryString();
        verifyNoMoreInteractions(mockHttpRequest);
    }

    /**
     * When the arguments are null, the original query string should be returned.
     */
    @Test
    public void removeNthAndAdd_HandlesNull_HasNoEffect() {
        String query = "key4=aa&key2=bb&key3=cc&key2=dd&key4=ee&key2=ff&key4=hh&key2=ii&key3=jj&key9=kk";

        HttpServletRequest mockHttpRequest = mock(HttpServletRequest.class);
        when(mockHttpRequest.getQueryString()).thenReturn(query);

        QueryStringHelper helper = new QueryStringHelper();
        String result = helper.removeNthAndAdd(mockHttpRequest, null, null);

        assertThat(result).isEqualTo(query);

        verify(mockHttpRequest, times(1)).getQueryString();
        verifyNoMoreInteractions(mockHttpRequest);
    }

    /**
     * When the arguments are empty, the original query string should be returned.
     */
    @Test
    public void removeNthAndAdd_HandlesEmptyArgs_HasNoEffect() {
        String query = "key4=aa&key2=bb&key3=cc&key2=dd&key4=ee&key2=ff&key4=hh&key2=ii&key3=jj&key9=kk";

        HttpServletRequest mockHttpRequest = mock(HttpServletRequest.class);
        when(mockHttpRequest.getQueryString()).thenReturn(query);

        Map<Object, List<Object>> removeInstructions = new HashMap<>();
        List<List<String>> addKeyValuePairs = new ArrayList<>();

        QueryStringHelper helper = new QueryStringHelper();
        String result = helper.removeNthAndAdd(mockHttpRequest, removeInstructions, addKeyValuePairs);

        assertThat(result).isEqualTo(query);

        verify(mockHttpRequest, times(1)).getQueryString();
        verifyNoMoreInteractions(mockHttpRequest);
    }

    /**
     * Remove single keys value at some legal relative index then add new key/value pair.
     *
     * Consider key2, the relative indexes are [bb, dd, ff, ii]. This test is to check that
     * removing a relative index somewhere other than the first index such 'ff' actually works and is followed by adding.
     */
    @Test
    public void removeNthAndAdd_RemovesOne_AddsOne() {
        String query = "key4=aa&key2=bb&key3=cc&key2=dd&key4=ee&key2=ff&key4=hh&key2=ii&key3=jj&key9=kk";
        String expected = "key4=aa&key2=bb&key3=cc&key2=dd&key4=ee&key4=hh&key2=ii&key3=jj&key9=kk&key100=New%20Value";

        HttpServletRequest mockHttpRequest = mock(HttpServletRequest.class);
        when(mockHttpRequest.getQueryString()).thenReturn(query);

        // simulates spel expression
        Map<Object, List<Object>> removeInstructions = new HashMap<>();
        removeInstructions.put("key2", Collections.singletonList("2"));

        List<List<String>> addKeyValuePairs = new ArrayList<>();
        addKeyValuePairs.add(Arrays.asList("key100", "New Value"));

        QueryStringHelper helper = new QueryStringHelper();
        String result = helper.removeNthAndAdd(mockHttpRequest, removeInstructions, addKeyValuePairs);

        assertThat(result).isEqualTo(expected);

        verify(mockHttpRequest, times(1)).getQueryString();
        verifyNoMoreInteractions(mockHttpRequest);
    }

    /**
     * If key2 is considered, the relative indexes are [bb, dd, ff, ii]. This test is to check that
     * removing invalid indexes has no effect and is followed by adding the new key value pair with escaping.
     */
    @Test
    public void removeNthAndAdd_InvalidRemovalIndexesHasNoEffect_ButStillAddsWithEscaping() {
        String query = "key4=aa&key2=bb&key3=cc&key2=dd&key4=ee&key2=ff&key4=hh&key2=ii&key3=jj&key9=kk";
        String expected = "key4=aa&key2=bb&key3=cc&key2=dd&key4=ee&key2=ff&key4=hh&key2=ii&key3=jj&key9=kk&key100=%20New%20Value%20";

        HttpServletRequest mockHttpRequest = mock(HttpServletRequest.class);
        when(mockHttpRequest.getQueryString()).thenReturn(query);

        // simulates spel expression
        Map<Object, List<Object>> removeInstructions = new HashMap<>();
        removeInstructions.put("key2", Arrays.asList("-1", "4", "-100", "549"));

        List<List<String>> addKeyValuePairs = new ArrayList<>();
        addKeyValuePairs.add(Arrays.asList("key100", " New Value "));

        QueryStringHelper helper = new QueryStringHelper();
        String result = helper.removeNthAndAdd(mockHttpRequest, removeInstructions, addKeyValuePairs);

        assertThat(result).isEqualTo(expected);

        verify(mockHttpRequest, times(1)).getQueryString();
        verifyNoMoreInteractions(mockHttpRequest);
    }

    /**
     * If key2 is considered, the relative indexes are [bb, dd, ff, ii]. This test is to check that
     * removing many keys + relative indexes actually works and is followed by adding many key/values.
     */
    @Test
    public void removeNthAndAdd_RemovesMany_AddsMany() {
        String query = "key4=aa&key2=bb&key3=cc&key2=dd&key4=ee&key2=ff&key4=hh&key2=ii&key4=vv&key3=jj&key9=kk";
        String expected = "key4=aa&key3=cc&key2=dd&key2=ff&key4=vv&key3=jj&key9=kk&key100=New%20Value";

        HttpServletRequest mockHttpRequest = mock(HttpServletRequest.class);
        when(mockHttpRequest.getQueryString()).thenReturn(query);

        // simulates spel expression
        Map<Object, List<Object>> removeInstructions = new HashMap<>();
        // Remove values for key2 at relative index 0 and 3
        removeInstructions.put("key2", Arrays.asList("0", "3"));
        // Remove values for key2 at relative index 1 and 2
        removeInstructions.put("key4", Arrays.asList("1", "2"));

        List<List<String>> addKeyValuePairs = new ArrayList<>();
        addKeyValuePairs.add(Arrays.asList("key100", "New Value"));

        QueryStringHelper helper = new QueryStringHelper();
        String result = helper.removeNthAndAdd(mockHttpRequest, removeInstructions, addKeyValuePairs);

        assertThat(result).isEqualTo(expected);

        verify(mockHttpRequest, times(1)).getQueryString();
        verifyNoMoreInteractions(mockHttpRequest);
    }

    /**
     * Sanity check - when all keys and relative indexes are removed, the result should be whatever is being
     * added (in this test nothing is being added so it should be an empty string).
     *
     * Note: The last 2 values of each removeInstruction is an upper and lower bound check on the expected
     * relative index positions.
     *
     * Eg consider key2. key2 has values [bb, dd, ff, ii, kk]. The relative indexes are the array indexes 0 to 4.
     * Therefore the last 2 values in the removeInstruction are 4 (upper bound) and -1 (lower bound).
     */
    @Test
    public void removeNthAndAdd_RemovesAll_WithNothingToAdd() {
        String query = "key4=aa&key2=bb&key3=cc&key2=dd&key4=ee&key2=ff&key4=hh&key2=ii&key4=vv&key3=jj&key9=kk";
        String expected = "";

        HttpServletRequest mockHttpRequest = mock(HttpServletRequest.class);
        when(mockHttpRequest.getQueryString()).thenReturn(query);

        // simulates spel expression
        Map<Object, List<Object>> removeInstructions = new HashMap<>();
        removeInstructions.put("key2", Arrays.asList("0", "1", "2", "3", "4", "-1"));
        removeInstructions.put("key4", Arrays.asList("0", "1", "2", "3", "4", "-1"));
        removeInstructions.put("key3", Arrays.asList("0", "1", "2", "-1"));
        removeInstructions.put("key9", Arrays.asList("0", "1", "-1"));

        List<List<String>> addKeyValuePairs = new ArrayList<>();

        QueryStringHelper helper = new QueryStringHelper();
        String result = helper.removeNthAndAdd(mockHttpRequest, removeInstructions, addKeyValuePairs);

        assertThat(result).isEqualTo(expected);

        verify(mockHttpRequest, times(1)).getQueryString();
        verifyNoMoreInteractions(mockHttpRequest);
    }

    /**
     * Sanity check - when all keys and relative indexes are removed, the result should be whatever is being
     * added. This also checks to ensure the added key/values are escaped correctly in the final query string.
     *
     * lower -1 and upper bound (size + 1) checks are also included in the last 2 index positions.
     */
    @Test
    public void removeNthAndAdd_RemovesAll_ThenAdds_WithEscaping() {
        String query = "key4=aa&key2=bb&key3=cc&key2=dd&key4=ee&key2=ff&key4=hh&key2=ii&key4=vv&key3=jj&key9=kk";
        String expected = "key500=Key%20500%20Value&key600=Key%20600%20Value";

        HttpServletRequest mockHttpRequest = mock(HttpServletRequest.class);
        when(mockHttpRequest.getQueryString()).thenReturn(query);

        // simulates spel expression
        Map<Object, List<Object>> removeInstructions = new HashMap<>();
        removeInstructions.put("key2", Arrays.asList("0", "1", "2", "3", "4", "-1"));
        removeInstructions.put("key4", Arrays.asList("0", "1", "2", "3", "4", "-1"));
        removeInstructions.put("key3", Arrays.asList("0", "1", "2", "-1"));
        removeInstructions.put("key9", Arrays.asList("0", "1", "-1"));

        List<List<String>> addKeyValuePairs = new ArrayList<>();
        addKeyValuePairs.add(Arrays.asList("key500", "Key 500 Value"));
        addKeyValuePairs.add(Arrays.asList("key600", "Key 600 Value"));

        // Should be ignored as 2 values are needed.
        addKeyValuePairs.add(Arrays.asList("key700"));
        addKeyValuePairs.add(Arrays.asList("", ""));
        addKeyValuePairs.add(Arrays.asList("key800", ""));
        addKeyValuePairs.add(Arrays.asList("", "valueXYZ"));

        QueryStringHelper helper = new QueryStringHelper();
        String result = helper.removeNthAndAdd(mockHttpRequest, removeInstructions, addKeyValuePairs);

        assertThat(result).isEqualTo(expected);

        verify(mockHttpRequest, times(1)).getQueryString();
        verifyNoMoreInteractions(mockHttpRequest);
    }
}
