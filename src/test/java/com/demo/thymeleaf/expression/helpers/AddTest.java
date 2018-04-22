package com.demo.thymeleaf.expression.helpers;

import com.demo.thymeleaf.expression.QueryStringHelper;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class AddTest {

    @Test
    public void add_QueryStringIsNull_NewKeyValueAdded() {
        String expected = "location=east%20europe";

        QueryStringHelper helper = new QueryStringHelper();
        String result = helper.add(null, "location", "east europe");
        assertThat(result).isEqualTo(expected);
    }

    @Test
    public void add_QueryStringIsEmpty_NewKeyValueAdded() {
        String expected = "location=east%20europe";

        QueryStringHelper helper = new QueryStringHelper();
        String result = helper.add("", "location", "east europe");
        assertThat(result).isEqualTo(expected);
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

        QueryStringHelper helper = new QueryStringHelper();
        String result = helper.add(query, "key99", "Added");
        assertThat(result).isEqualTo(expected);
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

        QueryStringHelper helper = new QueryStringHelper();
        String result = helper.add(query, "key2", "Added");
        assertThat(result).isEqualTo(expected);
    }

    /**
     * When the same key and value already exist, nothing should get added and the original query string should be
     * returned.
     */
    @Test
    public void add_DuplicateKey_ValueExists_HasNoEffect() {
        String query = "key4=ValueA&key2=AlreadyExists&key3=ValueC&key2=ValueE&key2=ValueF&key9=ValueK";

        QueryStringHelper helper = new QueryStringHelper();
        String result = helper.add(query, "key2", "AlreadyExists");
        assertThat(result).isEqualTo(query);
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

        QueryStringHelper helper = new QueryStringHelper();
        String result = helper.add(query, "key2", " Already    Exists ");
        assertThat(result).isEqualTo(query);
    }

    @Test
    public void addAll_QueryStringIsNull_AddsAllKeyValuePairsHasNoEffect() {
        String expected = "city=san%20francisco&country=america&zip=4048530985&region=3%2084.231-21%208";

        // Simulate SpEL expression for 2d list.
        List<List<String>> instructions = new ArrayList<>();
        instructions.add(new ArrayList<>(Arrays.asList("city", "san francisco")));
        instructions.add(new ArrayList<>(Arrays.asList("country", "america")));
        instructions.add(new ArrayList<>(Arrays.asList("zip", "4048530985")));
        instructions.add(new ArrayList<>(Arrays.asList("region", "3 84.231-21 8")));

        QueryStringHelper helper = new QueryStringHelper();
        String result = helper.addAll(null, instructions);
        assertThat(result).isEqualTo(expected);
    }

    @Test
    public void addAll_QueryStringIsEmpty_AddsAllKeyValuePairsHasNoEffect() {
        String expected = "city=san%20francisco&country=america&zip=4048530985&region=3%2084.231-21%208";

        // Simulate SpEL expression for 2d list.
        List<List<String>> instructions = new ArrayList<>();
        instructions.add(new ArrayList<>(Arrays.asList("city", "san francisco")));
        instructions.add(new ArrayList<>(Arrays.asList("country", "america")));
        instructions.add(new ArrayList<>(Arrays.asList("zip", "4048530985")));
        instructions.add(new ArrayList<>(Arrays.asList("region", "3 84.231-21 8")));

        QueryStringHelper helper = new QueryStringHelper();
        String result = helper.addAll("", instructions);
        assertThat(result).isEqualTo(expected);
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

        QueryStringHelper helper = new QueryStringHelper();
        String result = helper.addAll(query, instructions);
        assertThat(result).isEqualTo(expected);
    }

    /**
     * Many key value pairs should get added to the end.
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

        QueryStringHelper helper = new QueryStringHelper();
        String result = helper.addAll(query, instructions);
        assertThat(result).isEqualTo(expected);
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

        QueryStringHelper helper = new QueryStringHelper();
        String result = helper.addAll(query, instructions);
        assertThat(result).isEqualTo(expected);
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

        QueryStringHelper helper = new QueryStringHelper();
        String result = helper.addAll(query, instructions);
        assertThat(result).isEqualTo(query);
    }

    /**
     * Sanity check to ensure that when the exact same keys are added but with different values,
     * the exact keys but with different values are appended to the end in the order provided.
     */
    @Test
    public void addAll_AllKeysExist_DifferentValues_AppendToEnd() {
        String query = "key4=ValueA&key2=Value%20B&key3=%20ValueC%20&key4=%20Value%20CC%20&key4=ValueA1&key2=Value%20B2&key3=%20ValueC%203&key4=%20Value%20CC%204";

        // Simulate SpEL expression for 2d list.
        List<List<String>> instructions = new ArrayList<>();
        instructions.add(Arrays.asList("key4", "ValueA1"));
        instructions.add(Arrays.asList("key2", "Value B2"));
        instructions.add(Arrays.asList("key3", " ValueC 3"));
        instructions.add(Arrays.asList("key4", " Value CC 4"));

        QueryStringHelper helper = new QueryStringHelper();
        String result = helper.addAll(query, instructions);
        assertThat(result).isEqualTo(query);
    }
}
