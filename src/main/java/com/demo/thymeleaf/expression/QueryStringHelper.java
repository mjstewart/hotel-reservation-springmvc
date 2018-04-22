package com.demo.thymeleaf.expression;


import com.demo.thymeleaf.utils.QueryString;
import com.demo.thymeleaf.utils.QueryStringUtil;
import com.demo.thymeleaf.utils.SortDirection;
import org.thymeleaf.expression.Uris;

import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * This class provides the public methods exposed to the thymeleaf template caller.
 *
 * <p>Since {@code #request.getQueryString()} may be {@code null} or empty, this is the only argument to each
 * method that should be tested. Its reasonable to assume the caller will use the methods as instructed with plenty
 * of examples provided.</p>
 */
public class QueryStringHelper {

    private final Uris uris = new Uris();

    /**
     * Replaces only the first occurrence of 'key' with 'value' while maintaining the query strings original order.
     *
     * <h3>Thymeleaf usage</h3>
     *
     * <pre>
     *     #request.getQueryString() = "suburb=west&region=AU&postcode=494849"
     *
     *     th:with="newQueryString=${#qs.replaceFirst(#request.getQueryString(), 'region', 'Australia')}"
     * </pre>
     *
     * <h3>Result</h3>
     *
     * <pre>
     *     newQueryString = "suburb=west&region=Australia&postcode=494849"
     * </pre>
     *
     * <b>Note:</b>
     * <p>Supplying a {@code null} or empty {@code queryString} will return an empty string.
     * All other arguments must receive valid values otherwise the behaviour is undefined.</p>
     *
     * @param queryString The current query string.
     * @param key         The target key to replace the value for.
     * @param value       The replacement value.
     * @return The new query string.
     */
    public String replaceFirst(String queryString, String key, String value) {
        return QueryString.of(queryString, uris).replaceFirst(key, value);
    }

    /**
     * Replaces the nth key with the supplied value based on a keys relative index while maintaining the query strings
     * original order.
     *
     * <h3>Explanation</h3>
     * <p>Lets examine the {@code region} key in the below example. {@code region} can be thought of as having an
     * array of 3 values. {@code region = ['AU', 'Australia', 'AUS']}. {@code region[1], region[2]} are replaced
     * with the new values of 'Auckland' and 'AUKL' respectively while while {@code region[0]} remains unchanged.
     * </p>
     *
     * <h3>Thymeleaf usage</h3>
     *
     * <pre>
     *     #request.getQueryString() = "region=AU&suburb=west&region=Australia&postcode=494849&region=AUS"
     *
     *     th:with="newQueryString=${#qs.replaceNth(#request.getQueryString(), {region: { 1: 'Auckland', 2: 'AUKL' }})}"
     *     => newQueryString = "region=AU&suburb=west&region=Auckland&postcode=494849&region=AUKL"
     * </pre>
     *
     * <b>Note:</b>
     * <p>Supplying a {@code null} or empty {@code queryString} will return an empty string. The syntax for the
     * {@code stateChangeInstructions} Map must be exactly as the example shows, otherwise there will be casting errors.</p>
     *
     * @param queryString             The current query string.
     * @param stateChangeInstructions A map containing the instructions on how to modify the query string.
     * @return The new query string.
     */
    public String replaceNth(String queryString, Map<String, Map<Integer, String>> stateChangeInstructions) {
        return QueryString.of(queryString, uris).replaceNth(stateChangeInstructions);
    }

    /**
     * Replaces the first N values associated with the {@code key} while maintaining the query strings original order.
     * Consider the below example.
     *
     * <h3>Explanation</h3>
     *
     * <p>The key {@code 'name'} can be visualised as an array containing 3 values. {@code name = ['john', 'joseph', 'smith']}.</p>
     * <p>The example shows 2 values being provided {@code ['mary', 'rose']} which replace the existing 2 values in
     * the corresponding index positions while leaving the last index of {@code 'smith'} unchanged.
     * The transformation is equivalent to</p>
     *
     * <pre>
     *     name = ['john', 'joseph', 'smith']
     *     name[0] = 'mary'
     *     name[1] = 'rose'
     * </pre>
     *
     * <p>If 1 value was provided, only the first value would be replaced, whereas if 100 values were provided then
     * all 3 would be replaced by the first 3 corresponding replacement values.</p>
     *
     * <h3>Thymeleaf usage</h3>
     *
     * <pre>
     *     #request.getQueryString() = "name=john&age=30&name=joseph&month=march&name=smith"
     *
     *     th:with="newQueryString=${#qs.replaceN(#request.getQueryString(), 'name', {'mary', 'rose'})}"
     *     => newQueryString = "name=mary&age=30&name=rose&month=march&name=smith"
     * </pre>
     *
     * <b>Note:</b>
     * <p>Supplying a {@code null} or empty {@code queryString} will return an empty string.
     * All other arguments must receive valid values otherwise the behaviour is undefined.</p>
     *
     * @param queryString The current query string.
     * @param key         The target key to replace the corresponding values for.
     * @param values      The replacement values, eg values[0] will replace the keys value at index 0 in the query string.
     * @return The new query string.
     */
    public String replaceN(String queryString, String key, List<String> values) {
        return QueryString.of(queryString, uris).replaceN(key, values);
    }

    /**
     * Removes the first occurrence of the supplied key while maintaining the query strings original order.
     * If there are no duplicate keys, the entire key will be removed.
     *
     * <h3>Explanation</h3>
     *
     * <ol>
     * <li>When every key in the query string is unique (only appears once), this method simply removes the key
     * as shown in the below example 2.
     * </li>
     * <li>
     * When there are duplicate keys such as {@code 'name'} containing 3 values {@code name = ['john', 'joseph', 'smith']}.
     * The effect of calling {@code removeFirst} is simply removing name[0].
     * </li>
     * </ol>
     *
     * <h3>Thymeleaf usage</h3>
     *
     * <pre>
     *     #request.getQueryString() = "name=john&age=30&name=joseph&month=march&name=smith"
     *
     *     Duplicate 1. th:with="newQueryString=${#qs.removeFirst(#request.getQueryString(), 'name')}"
     *     => newQueryString = "age=30&name=joseph&month=march&name=smith" (other names still remain)
     *
     *     Unique 2. th:with="newQueryString=${#qs.removeFirst(#request.getQueryString(), 'age')}"
     *     => newQueryString = "name=john&name=joseph&month=march&name=smith" (age completely gone).
     * </pre>
     *
     * <b>Note:</b>
     * <p>Supplying a {@code null} or empty {@code queryString} will return an empty string.
     * All other arguments must receive valid values otherwise the behaviour is undefined.</p>
     *
     * @param queryString The current query string.
     * @param key         The target key to remove.
     * @return The new query string.
     */
    public String removeFirst(String queryString, String key) {
        return QueryString.of(queryString, uris).removeFirst(key);
    }

    /**
     * Removes every key and their associated values should the key exist while maintaining the
     * query strings original order.
     *
     * <p>The example shows removing keys {@code ['region', 'postcode']} with the resulting query string
     * including only the other 2 keys of {@code 'suburb' and 'language'} in the order they originally appeared.</p>
     *
     * <h3>Thymeleaf usage</h3>
     *
     * <pre>
     *     #request.getQueryString() = "region=AU&suburb=west&region=Australia&postcode=494849&region=AUS&language=en"
     *
     *     th:with="newQueryString=${#qs.removeAll(#request.getQueryString(), {'region', 'postcode'})}"
     *     => newQueryString = "suburb=west&language=en"
     * </pre>
     *
     * <b>Note:</b>
     * <p>Supplying a {@code null} or empty {@code queryString} will return an empty string.
     * The syntax for the list of {@code keys} must be exactly as the example shows, otherwise
     * there will be casting errors</p>
     *
     * @param queryString The current query string.
     * @param keys        The keys to remove.
     * @return The new query string.
     */
    public String removeAll(String queryString, List<String> keys) {
        return QueryString.of(queryString, uris).removeAll(keys);
    }

    /**
     * The same concept as {@code dropN} in functional languages where the first n occurrences of the target key are removed
     * while maintaining the query strings original order.
     *
     * <h3>Explanation</h3>
     * Using the example, the key {@code 'name'} can be visualised as an array containing 3 values.
     * {@code name = ['john', 'joseph', 'smith']}. {@code removeN} will simply delete the first n values as below.
     *
     * <pre>
     *     n=-1 -> ['john', 'joseph', 'smith'] (invalid n has no effect).
     *     n=0 -> ['john', 'joseph', 'smith'] (dropping nothing has no effect).
     *     n=1 -> ['joseph', 'smith']
     *     n=2 -> ['smith']
     *     n=3 -> []
     *     n=100 -> []
     * </pre>
     *
     * <h3>Thymeleaf usage</h3>
     * <p>The example removes the {@code 'name'} keys first 2 values with {@code 'smith'} still remaining.</p>
     * <pre>
     *     #request.getQueryString() = "name=john&age=30&name=joseph&month=march&name=smith"
     *
     *     th:with="newQueryString=${#qs.removeN(#request.getQueryString(), 'name', 2)}"
     *     => newQueryString = "age=30&month=march&name=smith"
     * </pre>
     *
     * <b>Note:</b>
     * <p>Supplying a {@code null} or empty {@code queryString} will return an empty string.
     * All other arguments must receive valid values otherwise the behaviour is undefined.</p>
     *
     * @param queryString The current query string.
     * @param key         The keys to remove.
     * @param n           The keys to remove.
     * @return The new query string.
     */
    public String removeN(String queryString, String key, int n) {
        return QueryString.of(queryString, uris).removeN(key, n);
    }

    // TODO UPTO HERE, remember to test example code


    /**
     * Removes the nth relative index of the given key while maintaining the query strings original order.
     * The concept of relative index is outlined below using the {@code 'name'} key as the target.
     *
     * <pre>
     *     "name=john&age=30&name=joseph&month=march&name=smith"
     *
     *               0         1         2      (relative indexes)
     *     name = ['john', 'joseph', 'smith']
     * </pre>
     *
     * <h3>Thymeleaf usage</h3>
     * <p>The example demonstrates removing the middle name at index 1.</p>
     * <pre>
     *     #request.getQueryString() = "name=john&age=30&name=joseph&month=march&name=smith"
     *
     *     th:with="newQueryString=${#qs.removeNth(#request.getQueryString(), 'name', 1)}"
     *     => newQueryString = "name=john&age=30&month=march&name=smith"
     * </pre>
     *
     * <b>Note:</b>
     * <p>Supplying a {@code null} or empty {@code queryString} will return an empty string.
     * All other arguments must receive valid values otherwise the behaviour is undefined.</p>
     *
     * @param queryString The current query string.
     * @param key         The target key.
     * @param nthIndex    The relative index to remove for the given key.
     * @return The new query string.
     */
    public String removeNth(String queryString, String key, int nthIndex) {
        return QueryString.of(queryString, uris).removeNth(key, nthIndex);
    }

    /**
     * Applies the same logic as {@link #removeNth(String, String, int)} except provides the ability
     * to remove multiple relative indexes at the same time. The below example shows how its possible to remove
     * relative indexes 0 and 2 while keeping the middle name {@code 'joseph'}.
     *
     * <pre>
     *     "name=john&age=30&name=joseph&month=march&name=smith"
     *
     *               0         1         2      (relative indexes)
     *     name = ['john', 'joseph', 'smith']
     * </pre>
     *
     * <h3>Thymeleaf usage</h3>
     *
     * <pre>
     *     #request.getQueryString() = "name=john&age=30&name=joseph&month=march&name=smith"
     *
     *     th:with="newQueryString=${#qs.removeManyNth(#request.getQueryString(), 'name', {0, 2})}"
     *     => "age=30&name=joseph&month=march"
     * </pre>
     *
     * <b>Note:</b>
     * <p>Supplying a {@code null} or empty {@code queryString} will return an empty string. The syntax for the
     * list of {@code relativeIndexes} must be exactly as the example shows, otherwise there will be casting errors.
     * All other arguments must receive valid values otherwise the behaviour is undefined.</p>
     *
     * @param queryString     The current query string.
     * @param key             The target key.
     * @param relativeIndexes The relative indexes to remove for the given key.
     * @return The new query string.
     */
    public String removeManyNth(String queryString, String key, List<Integer> relativeIndexes) {
        return QueryString.of(queryString, uris).removeManyNth(key, relativeIndexes);
    }

    /**
     * Removes the target key if its value is equal to the matching value by using case insensitive equality.
     * The example shows that for the target key {@code 'region'}, only remove the key if the value is equal
     * to 'australia'. Note the case insensitivity.
     *
     * <h3>Thymeleaf usage</h3>
     *
     * <pre>
     *     #request.getQueryString() = "region=AU&region=south&region=AUSTRALIA&sort=country,asc"
     *
     *     th:with="newQueryString=${#qs.removeKeyMatchingValue(#request.getQueryString(), 'region', 'australia')}"
     *     => newQueryString = "region=AU&region=south&sort=country,asc"
     * </pre>
     *
     * <b>Note:</b>
     * <p>Supplying a {@code null} or empty {@code queryString} will return an empty string.
     * All other arguments must receive valid values otherwise the behaviour is undefined.</p>
     *
     * @param queryString The current query string.
     * @param key         The target key.
     * @param valueMatch  The value to match which triggers deletion.
     * @return The new query string.
     */
    public String removeKeyMatchingValue(String queryString, String key, String valueMatch) {
        return QueryString.of(queryString, uris).removeKeyMatchingValue(key, valueMatch);
    }

    /**
     * Similar to {@link #removeKeyMatchingValue(String, String, String)} except no target key is
     * provided causing all keys to be eligible for removal if the value matches. Case insensitive equality applies.
     *
     * <h3>Thymeleaf usage</h3>
     * <p>The example shows any key matching the value 'australia' (case insensitive) will be removed.</p>
     * <pre>
     *     #request.getQueryString() = "region=AU&region=south&region=AUSTRALIA&sort=country,asc&locale=australia"
     *
     *     th:with="newQueryString=${#qs.removeAnyKeyMatchingValue(#request.getQueryString(), 'australia')}"
     *     => newQueryString = "region=AU&region=south&sort=country,asc"
     * </pre>
     *
     * <b>Note:</b>
     * <p>Supplying a {@code null} or empty {@code queryString} will return an empty string.
     * All other arguments must receive valid values otherwise the behaviour is undefined.</p>
     *
     * @param queryString The current query string.
     * @param valueMatch  The value to match which triggers deletion.
     * @return The new query string.
     */
    public String removeAnyKeyMatchingValue(String queryString, String valueMatch) {
        return QueryString.of(queryString, uris).removeAnyKeyMatchingValue(valueMatch);
    }

    /**
     * Gets the value associated with the first occurrence of the given key.
     *
     * <ol>
     * <li>If all keys are unique, the single value associated to the key is returned - example 2.</li>
     * <li>If there are duplicate keys such as {@code 'name'} with values {@code ['john', 'joseph', 'smith']}
     * then {@code getFirstValue} returns {@code name[0]} as shown in example 1.
     * </li>
     * <li>If no key is found, null is returned.</li>
     * </ol>
     *
     * <h3>Thymeleaf usage</h3>
     *
     * <pre>
     *     #request.getQueryString() = "name=john&age=30&name=joseph&month=march&name=smith"
     *
     *     Duplicate 1. th:with="newQueryString=${#qs.getFirstValue(#request.getQueryString(), 'name')}"
     *     => "john"
     *
     *     Unique: 2. th:with="newQueryString=${#qs.getFirstValue(#request.getQueryString(), 'age')}"
     *     => "30"
     *
     *     Not found: 3. th:with="newQueryString=${#qs.getFirstValue(#request.getQueryString(), 'city')}"
     *     => null
     * </pre>
     *
     * <b>Note:</b>
     * <p>Supplying a {@code null} or empty {@code queryString} will return an empty string.
     * All other arguments must receive valid values otherwise the behaviour is undefined.</p>
     *
     * @param queryString The current query string.
     * @param key         The key to get the value for.
     * @return The associated value or null if the key does not exist.
     */
    public String getFirstValue(String queryString, String key) {
        return QueryString.of(queryString, uris).getFirstValue(key);
    }

    /**
     * Gets all value associated with the the given key.
     *
     * <ol>
     * <li>If all keys are unique, the returned list will contain the single value associated to the key - example 2.</li>
     * <li>If there are duplicate keys such as {@code 'name'} with values {@code ['john', 'joseph', 'smith']}
     * then {@code getAllValues} returns all 3 values in a list as shown in example 1.
     * </li>
     * <li>If no key is found, an empty list is returned.</li>
     * </ol>
     *
     * <h3>Thymeleaf usage</h3>
     *
     * <pre>
     *     #request.getQueryString() = "name=john&age=30&name=joseph&month=march&name=smith"
     *
     *     Duplicate 1. th:with="newQueryString=${#qs.getAllValues(#request.getQueryString(), 'name')}"
     *     => ["john", "joseph", "smith"]
     *
     *     Unique: 2. th:with="newQueryString=${#qs.getAllValues(#request.getQueryString(), 'age')}"
     *     => ["30"]
     *
     *     Not found: 3. th:with="newQueryString=${#qs.getAllValues(#request.getQueryString(), 'city')}"
     *     => []
     * </pre>
     *
     * <b>Note:</b>
     * <p>Supplying a {@code null} or empty {@code queryString} will return an empty list.
     * All other arguments must receive valid values otherwise the behaviour is undefined.</p>
     *
     * @param queryString The current query string.
     * @param key         The key to get the values for.
     * @return The associated value or null if the key does not exist.
     */
    public List<String> getAllValues(String queryString, String key) {
        return QueryString.of(queryString, uris).getAllValues(key);
    }

    /**
     * Adds the given key and value to the end of the query string with the value being escaped to form a
     * valid query string.
     *
     * <ol>
     * <li>If exactly the same key and value exist, the original query string is returned unmodified.</li>
     * <li>If same key but different value is added, the new key and value pair is added to the end resulting
     * in 2 of the same keys but with different values. See Example 2</li>
     * <li>If the query string is empty or {@code null}, the key and value will be the new query string. See Example 3.</li>
     * </ol>
     *
     * <h3>Thymeleaf usage</h3>
     *
     * <pre>
     *     #request.getQueryString() = "name=john&age=30"
     *
     *     1. th:with="newQueryString=${#qs.add(#request.getQueryString(), 'city', 'san francisco')}"
     *     => newQueryString = name=john&age=30&city=san%20francisco
     *
     *     2. th:with="newQueryString=${#qs.add(#request.getQueryString(), 'name', 'smith')}"
     *     => newQueryString = name=john&age=30&name=smith
     *
     *     3. th:with="newQueryString=${#qs.add(null, 'city', 'melbourne')}"
     *     => newQueryString = city=melbourne
     * </pre>
     *
     * <b>Note:</b>
     * <p>Supplying a {@code null} or empty {@code queryString} will return the new key and value in query string form.
     * All other arguments must receive valid values otherwise the behaviour is undefined.</p>
     *
     * @param queryString The current query string.
     * @param key         The key.
     * @param value       The value to associate to the key.
     * @return The new query string.
     */
    public String add(String queryString, String key, String value) {
        return QueryString.of(queryString, uris).add(key, value);
    }

    /**
     * Behaves exactly like {@link #add(String, String, String)} except adds many key value pairs
     * to the end of the query string in the exact order they are provided. If the key/value pair already exist, it is
     * ignored.
     *
     * <h3>Thymeleaf usage</h3>
     * <p>The example shows adding 2 new key/value pairs (city and country) while ignoring {@code name='john'} since
     * it already exists.
     * </p>
     * <pre>
     *     #request.getQueryString() = "name=john&age=30"
     *
     *     th:with="newQueryString=${#qs.addAll(#request.getQueryString(), {{'city', 'san francisco'}, {'country', 'US'}, {'name', 'john'}})}"
     *     => name=john&age=30&city=san%20francisco&country=US
     * </pre>
     *
     * <b>Note:</b>
     * <p>The syntax for the 2d list of {@code keyValuePairs} must be exactly as the example shows, otherwise
     * there will be casting errors. If the query string is {@code null} or empty, all key value pairs will be
     * joined to form the new query string.</p>
     *
     * @param queryString   The current query string.
     * @param keyValuePairs The list of key value pairs to add to the end of the query string if it does not exist.
     * @return The new query string.
     */
    public String addAll(String queryString, List<List<String>> keyValuePairs) {
        return QueryString.of(queryString, uris).addAll(keyValuePairs);
    }

    /**
     * Convenience method to remove all occurrences of a list of keys followed by adding a list of new key value pairs.
     *
     * <p>This method is a useful alternative to executing a replace. Sometimes its not known the exact format of
     * the query string so it is easier to simply remove 1 or many keys followed by adding the new key/value pairs.</p>
     *
     * <h3>Thymeleaf usage</h3>
     * <p>The example demonstrates removing all occurrences of the 'postcode' and 'sort' keys. The key 'sort' is then
     * re-added to the end of the query string with a value of 'city,desc'. Using spring as an example, the primary
     * sort field is defined first followed by secondary sort keys. The example changes the secondary sort key of 'city'
     * to be the new primary sort field.
     * </p>
     *
     * <pre>
     *     #request.getQueryString() = "sort=country,asc&sort=city,desc&location=AU&region=north&postcode=4931495"
     *
     *     th:with="newQueryString=${#qs.removeAllAndAdd(#request.getQueryString(), {'postcode', 'sort'}, {{'sort', 'city,desc'}})}"
     *     => location=AU&region=north&sort=city,desc
     * </pre>
     *
     * <b>Note:</b>
     * <p>The syntax for the list arguments must be exactly as the example shows, otherwise
     * there will be casting errors. If the query string is {@code null} or empty, all new key value pairs will be
     * joined to form the new query string.</p>
     *
     * @param queryString      The current query string.
     * @param removeKeys       The keys to remove.
     * @param addKeyValuePairs The new key value pairs to add to the end of the query string.
     * @return The new query string.
     */
    public String removeAllAndAdd(String queryString, List<String> removeKeys, List<List<String>> addKeyValuePairs) {
        String afterRemovalQueryString = QueryString.of(queryString, uris).removeAll(removeKeys);

        try {
            QueryString newQueryString = QueryString.of(afterRemovalQueryString, uris);
            return newQueryString.addAll(addKeyValuePairs);
        } catch (IllegalArgumentException e) {
            // Invalid query string form, implies mismatch key=value pairs or empty string.
            return QueryStringUtil.toQueryString(addKeyValuePairs, uris::escapeQueryParam);
        }
    }

    /**
     * Convenience method to perform a {@link #removeNth(String, String, int)} on many unique keys while
     * maintaining the original query strings ordering. After the removal, the list of key value pairs are added to
     * the end of the query string.
     *
     * <h3>Thymeleaf usage</h3>
     * <p>The example shows key {@code 'sort'} having values {@code ['country,asc', 'city,desc']} and key {@code 'region'}
     * with values {@code ['north, 'upper', 'border']}.</p>
     *
     * <p>The removal step has the effect of deleting {@code sort[0]} and {@code region[1], region[2]}. After the removal,
     * 2 new key value pairs are added to the end of the query string.</p>
     *
     * <pre>
     *     #request.getQueryString() = "sort=country,asc&sort=city,desc&location=AU&region=north&region=upper&region=border"
     *
     *     th:with="newQueryString=${#qs.removeNthAndAdd(#request.getQueryString(), {'sort': {0}, 'region': {1, 2}}, {{'postcode', '39481'}, {'locale', 'AU'}})}"
     *     => newQueryString = sort=city,desc&location=AU&region=north&postcode=39481&locale=AU
     * </pre>
     *
     * <b>Note:</b>
     * <p>The syntax for the {@code removeInstructions} and {@code addKeyValuePairs} must be exactly as
     * the example shows, otherwise there will be casting errors. If the query string is {@code null} or empty,
     * all new key value pairs will be joined to form the new query string.</p>
     *
     * @param queryString        The current query string.
     * @param removeInstructions The keys and corresponding relative value indexes to remove.
     * @param addKeyValuePairs   The new key value pairs to add to the end of the query string.
     * @return The new query string.
     */
    public String removeNthAndAdd(String queryString,
                                  Map<String, List<Integer>> removeInstructions,
                                  List<List<String>> addKeyValuePairs) {
        if (queryString == null || queryString.isEmpty()) {
            return "";
        }
        if (removeInstructions == null || addKeyValuePairs == null) {
            return queryString;
        }

        // The reduction applies the removal action for each key
        String afterRemovalQueryString = removeInstructions.entrySet().stream()
                .reduce(queryString, (nextQueryString, entry) ->
                        QueryString.of(nextQueryString, uris)
                                .removeManyNth(entry.getKey(), entry.getValue()), (a, b) -> a + "&" + b);

        try {
            QueryString newQueryString = QueryString.of(afterRemovalQueryString, uris);
            return newQueryString.addAll(addKeyValuePairs);
        } catch (IllegalArgumentException e) {
            // Invalid query string form, implies mismatch key=value pairs or empty string.
            return QueryStringUtil.toQueryString(addKeyValuePairs, uris::escapeQueryParam);
        }
    }

    /**
     * Adds the supplied value to a range of numerical key values defined by relative index. Decrementing a value can be
     * achieved by supplying a negative number. Consider alternative methods such as {@code adjustFirstNumericValueBy}
     * or {@code incrementPage} if a specific key is being targeted.
     *
     * <h3>Thymeleaf usage</h3>
     * <p>The example shows key {@code 'policy'} having values {@code [10, 20, 30]}. Lets assume we want to add 5 to only
     * the values at index 1 and 2. The result is the last 2 values being incremented by 5 while the first value remains
     * unchanged at 10.</p>
     *
     * <pre>
     *     #request.getQueryString() = "policy=10&sort=country&policy=20&location=AU&border=north&policy=30"
     *
     *     th:with="newQueryString=${#qs.adjustNumericValueBy(#request.getQueryString(), 'policy', {1, 2}, 5)}"
     *     => newQueryString = policy=10&sort=country&policy=25&location=AU&border=north&policy=35
     * </pre>
     *
     * <b>Note:</b>
     * <p>The syntax for the {@code relativeIndexes} must be exactly as the example shows,
     * otherwise there will be casting errors. Supplying a {@code null} or empty {@code queryString} will return an empty string.
     * All other arguments must receive valid values otherwise the behaviour is undefined.</p>
     *
     * @param queryString     The current query string.
     * @param key             The target key to find numeric values for.
     * @param relativeIndexes Which indexes to add the value to.
     * @param value           The value to add (for decrementing use a negative number).
     * @return The new query string.
     */
    public String adjustNumericValueBy(String queryString, String key, List<Integer> relativeIndexes, int value) {
        return QueryString.of(queryString, uris).adjustNumericValueBy(key, relativeIndexes, value);
    }

    /**
     * Functions the same as {@link #adjustNumericValueBy(String, String, List, int)} except only updates
     * the first occurrence of the numeric key value. This method is provided for convenience to avoid having to provide
     * the relative index list. To decrement the value, supply a negative number.
     *
     * <h3>Thymeleaf usage</h3>
     * <p>The example shows key {@code 'policy'} having values {@code [10, 20, 30]}. Since this method only updates
     * index 0 (the first value), adding 2 to the current value at index 0 will result in 12 as the new value.</p>
     * <pre>
     *     #request.getQueryString() = "policy=10&sort=country&policy=20&location=AU&border=north&policy=30"
     *
     *     th:with="newQueryString=${#qs.adjustFirstNumericValueBy(#request.getQueryString(), 'policy', 2)}"
     *     => newQueryString = policy=12&sort=country&policy=20&location=AU&border=north&policy=30
     * </pre>
     *
     * <b>Note:</b>
     * <p>Supplying a {@code null} or empty {@code queryString} will return an empty string.
     * All other arguments must receive valid values otherwise the behaviour is undefined.</p>
     *
     * @param queryString The current query string.
     * @param key         The target key to find the first numeric value for.
     * @param value       The value to add (for decrementing use a negative number).
     * @return The new query string.
     */
    public String adjustFirstNumericValueBy(String queryString, String key, int value) {
        return QueryString.of(queryString, uris).adjustNumericValueBy(key, Collections.singletonList(0), value);
    }

    /**
     * Increments the value for key {@code 'page'} by 1 providing the value is numeric otherwise there is no effect.
     * This method is useful when working with spring {@code PagingAndSortingRepository} which uses
     * the key {@code 'page'} by convention.
     *
     * <h3>Thymeleaf usage</h3>
     *
     * <pre>
     *     #request.getQueryString() = "city=dallas&country=US&sort=country,desc&page=0"
     *
     *     th:with="newQueryString=${#qs.incrementPage(#request.getQueryString())}"
     *     => newQueryString = city=dallas&country=US&sort=country,desc&page=1
     * </pre>
     *
     * <b>Note:</b>
     * <p>Supplying a {@code null} or empty {@code queryString} will return an empty string.</p>
     *
     * @param queryString The current query string.
     * @return The new query string.
     */
    public String incrementPage(String queryString) {
        return adjustNumericValueBy(queryString, "page", Collections.singletonList(0), 1);
    }

    /**
     * The same as {@link #incrementPage(String)} except only increments the existing value if it is below the
     * {@code maxBound}. This is convenient to use to avoid having to implement additional bounds checking in the
     * template code.
     *
     * <h3>Thymeleaf Usage</h3>
     * <p>Consider a html table with paging where you would like to prevent the page count from exceeding the total pages
     * available from the {@code PagingAndSortingRepository}. The example assumes {@code customers} is of type
     * {@code Page<Customer>} and is in the {@code Model}. If there are 10 total pages, and the current page is 9
     * which is the last page, setting {@code maxBound = 9} will prevent the page from being incremented.</p>
     *
     * <pre>
     *     th:with="newQueryString=${#qs.incrementPage(#request.getQueryString(), customers.getTotalPages() - 1)}"
     * </pre>
     *
     * @param queryString The current query string.
     * @param maxBound    Increment current value only if it is below the {@code maxBound}.
     * @return The new query string.
     */
    public String incrementPage(String queryString, int maxBound) {
        Predicate<Integer> incrementIfBelowMax = currentValue -> currentValue < maxBound;
        return QueryString.of(queryString, uris)
                .adjustNumericValueBy("page", Collections.singletonList(0), 1, incrementIfBelowMax);
    }

    /**
     * Decrements the value for key {@code 'page'} by 1 providing the value is numeric otherwise there is no effect.
     * The value is not decremented below 0 which eliminates the need to do lower bound checking within the thymeleaf
     * template itself. This method is useful when working with spring {@code PagingAndSortingRepository} which uses
     * the key {@code 'page'} by convention.
     *
     * <h2>Thymeleaf usage</h2>
     *
     * <pre>
     *     #request.getQueryString() = "city=dallas&country=US&sort=country,desc&page=1"
     *
     *     th:with="newQueryString=${#qs.decrementPage(#request.getQueryString())}"
     *     => newQueryString = city=dallas&country=US&sort=country,desc&page=0
     * </pre>
     *
     * <b>Note:</b>
     * <p>Supplying a {@code null} or empty {@code queryString} will return an empty string.</p>
     *
     * @param queryString The current query string.
     * @return The new query string.
     */
    public String decrementPage(String queryString) {
        Predicate<Integer> decrementOnlyIfAboveZero = currentValue -> currentValue > 0;
        return QueryString.of(queryString, uris)
                .adjustNumericValueBy("page", Collections.singletonList(0), -1, decrementOnlyIfAboveZero);
    }

    /**
     * See {@link #setSortDirectionAsc(String, String)} as this method centralises the logic based on sort direction.
     *
     * @param queryString   The current query string
     * @param sortField     The sort field to change the sort direction for.
     * @param sortDirection The new sort direction (Must be SortDirection.ASC or DESC only).
     * @return The new query string.
     * @throws IllegalArgumentException If SortDirection.NONE
     */
    private String setSortDirection(String queryString, String sortField, SortDirection sortDirection) {
        if (sortDirection == SortDirection.NONE) {
            // wont occur given the publicly exposed methods control legal directions.
            throw new IllegalArgumentException("Invalid sort direction '" + sortDirection + "', expect either 'asc' or 'desc'");
        }

        return QueryString.of(queryString, uris).setSortDirection(sortField, currentDirection -> sortDirection);
    }

    /**
     * Finds a {@code 'sort'} key having a value equal to {@code sortField} which is to be changed to {@code asc}.
     * This method is useful when working with spring {@code PagingAndSortingRepository} which uses
     * the key {@code 'sort'} by convention
     *
     * <h3>Thymeleaf usage</h3>
     * <p>The example shows 'country' as not having an explicit sort direction meaning the default direction is
     * determined by the spring repository. Since the {@code setSortDirectionXXX} with trailing 'Asc' is used, 'country'
     * is set to direction 'asc'.</p>
     * <pre>
     *     #request.getQueryString() = "city=dallas&country=US&sort=country&page=1"
     *
     *     th:with="newQueryString=${#qs.setSortDirectionAsc(#request.getQueryString(), 'country')}"
     *     => newQueryString = city=dallas&country=US&sort=country,asc&page=1
     * </pre>
     *
     * <b>Note:</b>
     * <p>Supplying a {@code null} or empty {@code queryString} will return an empty string.
     * All other arguments must receive valid values otherwise the behaviour is undefined.</p>
     *
     * @param queryString The current query string
     * @param sortField   The sort field to change the sort direction to {@code asc}.
     * @return The new query string.
     */
    public String setSortDirectionAsc(String queryString, String sortField) {
        return setSortDirection(queryString, sortField, SortDirection.ASC);
    }

    /**
     * See {@link #setSortDirectionAsc(String, String)} as this method simply changes the sort field to direction
     * {@code desc}.
     *
     * @param queryString The current query string
     * @param sortField   The sort field to change the sort direction to {@code desc}.
     * @return The new query string.
     */
    public String setSortDirectionDesc(String queryString, String sortField) {
        return setSortDirection(queryString, sortField, SortDirection.DESC);
    }

    /**
     * Finds a {@code 'sort'} key having a value equal to {@code sortField} which is to have its sort direction toggled
     * to its opposite. E.g: 'asc' becomes 'desc' and vice versa. There are 2 variants of this method
     * {@code toggleSortDefaultXXX} with trailing 'Asc' or 'Desc'. The trailing direction determines how a sort field
     * with no direction is treated as illustrated in the below examples.
     *
     * <h3>Thymeleaf usage</h3>
     *
     * <h4>Implicit sort direction</h4>
     * 'country' has no explicit direction implying its default direction is 'asc' since
     * the {@code toggleSortDefaultXXX} trailing 'Asc' method is being used. Since the current direction is 'asc',
     * toggling causes the new direction to be 'desc'.
     *
     * <pre>
     *     #request.getQueryString() = "city=dallas&country=US&sort=country&page=1"
     *
     *     th:with="newQueryString=${#qs.toggleSortDefaultAsc(#request.getQueryString(), 'country')}"
     *     => newQueryString = city=dallas&country=US&sort=country,desc&page=1
     * </pre>
     *
     * <h4>Explicit sort direction</h4>
     * 'country' has an explicit direction of 'desc' resulting in the post toggle direction being 'asc'.
     * <pre>
     *     #request.getQueryString() = "city=dallas&country=US&sort=country,desc&page=1"
     *
     *     th:with="newQueryString=${#qs.toggleSortDefaultAsc(#request.getQueryString(), 'country')}"
     *     => newQueryString = city=dallas&country=US&sort=country,desc&page=1
     * </pre>
     *
     * <b>Note:</b>
     * <p>Supplying a {@code null} or empty {@code queryString} will return an empty string.
     * All other arguments must receive valid values otherwise the behaviour is undefined.</p>
     *
     * @param queryString The current query string
     * @param sortField   The sort field to toggle the sort direction for.
     * @return The new query string.
     */
    public String toggleSortDefaultAsc(String queryString, String sortField) {
        return QueryString.of(queryString, uris).toggleSortDefaultAsc(sortField);
    }

    /**
     * Works the same as {@link #toggleSortDefaultAsc(String, String)} except applies the default sort direction 'desc'.
     *
     * <h3>Thymeleaf usage</h3>
     *
     * <h4>Implicit sort direction</h4>
     * 'country' has no explicit direction implying its default direction is 'desc' since
     * the {@code toggleSortDefaultXXX} trailing 'Desc' method is being used. Since the current direction is 'desc',
     * toggling causes the new direction to be 'asc'.
     *
     * <pre>
     *     #request.getQueryString() = "city=dallas&country=US&sort=country&page=1"
     *
     *     th:with="newQueryString=${#qs.toggleSortDefaultDesc(#request.getQueryString(), 'country')}"
     *     => newQueryString = city=dallas&country=US&sort=country,asc&page=1
     * </pre>
     *
     * <h4>Explicit sort direction</h4>
     * 'country' has an explicit direction of 'asc' resulting in the post toggle direction being 'desc'.
     * <pre>
     *     #request.getQueryString() = "city=dallas&country=US&sort=country,asc&page=1"
     *
     *     th:with="newQueryString=${#qs.toggleSortDefaultDesc(#request.getQueryString(), 'country')}"
     *     => newQueryString = city=dallas&country=US&sort=country,desc&page=1
     * </pre>
     *
     * <b>Note:</b>
     * <p>Supplying a {@code null} or empty {@code queryString} will return an empty string.
     * All other arguments must receive valid values otherwise the behaviour is undefined.</p>
     *
     * @param queryString The current query string
     * @param sortField   The sort field to toggle the sort direction for.
     * @return The new query string.
     */
    public String toggleSortDefaultDesc(String queryString, String sortField) {
        return QueryString.of(queryString, uris).toggleSortDefaultDesc(sortField);
    }


    /**
     * See {@link #fieldSorterAsc(String)} since this method centralises the logic for all sort directions.
     *
     * @param queryString          The current query string.
     * @param field                The sort field.
     * @param defaultSortDirection The current direction of the {@code field} which acts as the reference point to
     *                             determine the next sort direction.
     * @return The new query string.
     */
    private String fieldSorter(String queryString, String field, SortDirection defaultSortDirection) {
        List<List<String>> newKeyValuePairs = new ArrayList<>();
        newKeyValuePairs.add(Arrays.asList("sort", field + "," + defaultSortDirection.value));

        if (queryString == null || queryString.trim().isEmpty()) {
            return addAll(queryString, newKeyValuePairs);
        }

        if (isFieldSorted(queryString, field)) {
            switch (defaultSortDirection) {
                case ASC:
                    return toggleSortDefaultAsc(queryString, field);
                case DESC:
                    return toggleSortDefaultDesc(queryString, field);
            }
        }

        return removeAllAndAdd(queryString, Collections.singletonList("sort"), newKeyValuePairs);
    }


    /**
     * Returns a function accepting a sort field applying sorting logic according to the below scenarios.
     * Consider the query string {@code 'sort=suburb,desc'}, the sort field provided to the returned function would
     * be 'suburb'. A function is returned to allow reuse within the thymeleaf template to avoid needing
     * to provide the query string each time.

     * <h3>Explanation</h3>
     * If the {@code field} exists in the query string, it is toggled to its opposite direction.
     * Otherwise all existing sort keys are removed with the new sort field {@code 'field,defaultDirection'}
     * appended to the end of the new query. 'defaultDirection' is determined by the trailing 'Asc' or 'Desc' in
     * the method name.
     *
     * <h3>Examples</h3>
     *
     * <h4>Query string is null or empty</h4>
     * <p>Since {@code fieldSorterAsc} is used, 'Asc' is the default direction for the sort field 'country'.</p>
     * <pre>
     *   #request.getQueryString() = null
     *
     *   fieldSorterAsc(#request.getQueryString()).apply("country") => "sort=country,asc"
     * </pre>
     *
     * <h4>Field exists and has an explicit sort direction</h4>
     * <p>{@code 'country'} has current sort direction of {@code asc} resulting in the opposite direction of {@code desc}
     * after the toggle.</p>
     * <pre>
     *   #request.getQueryString() = "sort=country,asc&sort=city"
     *
     *   fieldSorterAsc(#request.getQueryString()).apply("country") => "sort=country,desc&sort=city"
     * </pre>
     *
     * <h4>Field exists and has implicit sort direction</h4>
     * <p>Since {@code fieldSorterAsc} is used, 'Asc' is the default direction used by 'city' given there is no explicit
     * direction listed. The means 'city' is toggled to 'desc'.</p>
     * <pre>
     *   #request.getQueryString() = "sort=country,asc&sort=city"
     *
     *   fieldSorterAsc(#request.getQueryString()).apply("city") => "sort=country,asc&sort=city,desc"
     * </pre>
     *
     * <h4>Field does not exist</h4>
     * <p>Since {@code 'location'} does not exist as a sort field, a new sort key value pair is added to the end
     * of the query string upon removing any existing sorting. Note: the 'Asc' at the end of this method name
     * implies the default sort direction which is why 'location' is set to direction 'asc'.</p>
     * <pre>
     *   #request.getQueryString() = "sort=country,asc&sort=city"
     *
     *   fieldSorterAsc(#request.getQueryString()).apply("location") => "sort=location,asc"
     * </pre>
     *
     * <h4>How to sort on a nested object?</h4>
     * <p>Given a {@code Person} containing an {@code Address} with a suburb, use property dot notation to construct
     * the sort. Note: {@code Person} is assumed to exist in the {@code Model} available in the thymeleaf template.
     * To reiterate, since 'address.suburb' is not a current sort field, all existing sort fields are removed followed
     * by adding this new sort instruction to the end with default direction 'asc' implied by the trailing 'Asc' in the
     * method name.</p>
     * <pre>
     *   #request.getQueryString() = "sort=country,asc&sort=city"
     *
     *   fieldSorterAsc(#request.getQueryString()).apply("address.suburb") => "sort=address.suburb,asc"
     * </pre>
     *
     * <h3>Thymeleaf Usage</h3>
     *
     * <pre>
     *     {@literal
     *     <table> th:with="qstring=${#request.getQueryString()},
     *                      urlBuilder=${#qs.urlBuilder(#request.getRequestURI())
     *                      fieldSorterAsc=${#qs.fieldSorterAsc(qstring)},
     *                      fieldSorterDesc=${#qs.fieldSorterDesc(qstring)}"
     *             <thead>
     *                <tr>
     *                  <th th:with="newUrl=${urlBuilder.apply(fieldSorterAsc.apply('name'))}"
     *                     th:onclick="'javascript:onSortChange(\'' + ${newUrl} + '\');'">
     *                     Hotel
     *                  </th>
     *
     *                  <th th:with="newUrl=${urlBuilder.apply(fieldSorterAsc.apply('address.suburb'))}"
     *                     th:onclick="'javascript:onSortChange(\'' + ${newUrl} + '\');'">
     *                     Suburb
     *                 </th>
     *             </thead>
     *             ...
     *     </table>
     *     }
     * </pre>
     *
     * <b>Note:</b>
     * <p>Supplying a {@code null} or empty {@code queryString} will return a function accepting
     * the sort field. When this function is fully applied, the new query string will consist of 'sort=field,direction'
     * where 'direction' is derived from the trailing 'Asc' or 'Desc' of the {@code fieldSorterXXX} method.
     * All other arguments must receive valid values otherwise the behaviour is undefined.</p>
     *
     * @param queryString The current query string.
     * @return Function accepting the {@code field} to sort.
     */
    public Function<String, String> fieldSorterAsc(String queryString) {
        return field -> fieldSorter(queryString, field, SortDirection.ASC);

    }

    /**
     * See {@link #fieldSorterAsc(String)} since this method is the same except returns a function capable of
     * sorting a {@code field} having a default sort direction of {@code desc}.
     *
     * @param queryString The current query string.
     * @return Function accepting the {@code field} to sort.
     */
    public Function<String, String> fieldSorterDesc(String queryString) {
        return field -> fieldSorter(queryString, field, SortDirection.DESC);
    }

    /**
     * See {@link #valueWhenMatchesSortAsc(String, String, String, String)} for docs. This method centralises the logic.
     *
     * @param queryString      The current query string.
     * @param missingValue     The value to return if the sort field is not in the query string.
     * @param matchingValue    The value to return if there is a direction match.
     * @param nonMatchingValue The value to return upon no direction match.
     * @param direction        The sort direction to attempt to match to the existing sort {@code field}.
     * @return The function accepting the {@code String} sort field.
     */
    private Function<String, String> valueWhenMatchesSort(String queryString,
                                                          String missingValue,
                                                          String matchingValue, String nonMatchingValue,
                                                          SortDirection direction) {
        return field -> {
            String currentDirection = getCurrentSortDirection(queryString, field, direction);
            return currentDirection == null ? missingValue :
                    currentDirection.equals(direction.value) ? matchingValue : nonMatchingValue;
        };
    }

    /**
     * This method is useful for conditional css classes or tooltips where different values should be returned based
     * on the current sort direction. A function is returned accepting a single {@code String} sort field to avoid
     * having to supply the same values each time when only the sort field will change.</p>
     *
     * <p>There are 2 variants of this method {@code valueWhenMatchesSortXXX}. The trailing 'Asc' and 'Desc' is what
     * determines the equality check as shown in the examples.</p>
     *
     * <h3>Examples</h3>
     * <h4>Implicit sort direction</h4>
     * <p>'city' has no direction, using this method with the trailing 'Asc' treats 'city' as having the
     * default direction 'asc' which results in the 'matching' value being returned.
     * </p>
     * <pre>
     *     #request.getQueryString() = city=melbourne&state=vic&postcode=3000&sort=city
     *
     *     valueWhenMatchesSortAsc(#request.getQueryString(), "missing", "matching", "nonMatching")
     *                            .apply("city"); => "matching"
     * </pre>
     *
     * <h4>Explicit sort direction matches</h4>
     * <p>Similar to the above example but 'city' has an explicit direction 'asc' which matches the trailing 'Asc' of
     * this method causing the 'matching' value to be returned. If 'city' had direction 'desc' then use
     * {@code valueWhenMatchesSortDesc} to have the 'matching' value returned.
     * </p>
     * <pre>
     *     #request.getQueryString() = city=melbourne&state=vic&postcode=3000&sort=city,asc
     *
     *     valueWhenMatchesSortAsc(#request.getQueryString(), "missing", "matching", "nonMatching")
     *                            .apply("city"); => "matching"
     * </pre>
     *
     * <h4>Explicit sort direction does NOT match</h4>
     * <p>Similar to the above example but 'city' has an explicit direction 'desc'. Since this method has trailing 'Asc'
     * the equality check is {@code "desc".equals("asc")} which is false causing the 'nonMatching' value to be returned.
     * </p>
     * <pre>
     *     #request.getQueryString() = city=melbourne&state=vic&postcode=3000&sort=city,desc
     *
     *     valueWhenMatchesSortAsc(#request.getQueryString(), "missing", "matching", "nonMatching")
     *                            .apply("city"); => "nonMatching"
     * </pre>
     *
     * <h4>Field not found</h4>
     * <p>If the field does not appear in the query string under a sort key, the 'missing' value is returned. In this
     * example only 'city' is a sort field but the field supplied is 'country'.</p>
     * <pre>
     *     #request.getQueryString() = city=melbourne&state=vic&postcode=3000&sort=city,desc
     *
     *     valueWhenMatchesSortAsc(#request.getQueryString(), "missing", "matching", "nonMatching")
     *                            .apply("country"); => "nonMatching"
     * </pre>
     *
     * <p>The final example shows a use case for creating conditional tooltips. The first argument
     * 'Sort hotel name by default direction of ascending', displays how the column will be sorted given no sorting
     * is applied. Otherwise when the 'name' field has a sort direction equal to {@code asc}, the tooltip displays
     * the opposite direction of 'Sort hotel name by descending' which is equivalent to the 'matching' value in the above
     * examples.</p>
     *
     * <h3>Thymeleaf Usage</h3>
     *
     * <pre>
     *     {@literal
     *     <table class="ui sortable celled table"
     *                th:with="qstring=${#request.getQueryString()},
     *                         cssWhenFieldIsAsc=${#qs.valueWhenMatchesSortAsc(qstring, '', 'sorted ascending', 'sorted descending')}">
     *             <thead>
     *
     *             <tr>
     *                 <th th:classappend="${cssWhenFieldIsAsc.apply('name')}"
     *                     th:title="${#qs.valueWhenMatchesSortAsc(qstring,
     *                                  'Sort hotel name by default direction of ascending',
     *                                  'Sort hotel name descending',
     *                                  'Sort hotel name ascending')
     *                                 .apply('name')}">
     *                     Hotel
     *                 </th>
     *             </tr>
     *             </thead>
     *             <tbody>
     *             ...
     *    </table>
     *     }
     * </pre>
     *
     * <b>Note:</b>
     * <p>Supplying a {@code null} or empty {@code queryString} will return a function that returns
     * the 'missingValue'. All other arguments must receive valid values otherwise the behaviour is undefined.</p>
     *
     * @param queryString      The current query string.
     * @param missingValue     The value to return if the sort field is not in the query string.
     * @param matchingValue    The value to return if there is a direction match.
     * @param nonMatchingValue The value to return upon no direction match.
     * @return The function accepting the single {@code String} sort field.
     */
    public Function<String, String> valueWhenMatchesSortAsc(String queryString, String missingValue,
                                                            String matchingValue, String nonMatchingValue) {
        return valueWhenMatchesSort(queryString, missingValue, matchingValue, nonMatchingValue, SortDirection.ASC);
    }

    /**
     * See {@link #valueWhenMatchesSortAsc(String, String, String, String)} for docs but apply the opposite logic
     * for 'desc' direction.
     *
     * @param queryString      The current query string.
     * @param missingValue     The value to return if the sort field is not in the query string.
     * @param matchingValue    The value to return if there is a direction match.
     * @param nonMatchingValue The value to return upon no direction match.
     * @return The function accepting the single {@code String} sort field.
     */
    public Function<String, String> valueWhenMatchesSortDesc(String queryString, String missingValue,
                                                             String matchingValue, String nonMatchingValue) {
        return valueWhenMatchesSort(queryString, missingValue, matchingValue, nonMatchingValue, SortDirection.DESC);
    }


    /**
     * Removes all existing sort keys and associates the supplied field and sort direction values to a sort key which
     * gets appended to the end of the query string.
     *
     * <h3>Thymeleaf Usage</h3>
     * <p>Notice the new query string only had the new sort field and directions with all previous sorting removed.</p>
     *
     * <pre>
     *     #request.getQueryString() = city=melbourne&postcode=3000&page=0&sort=stars,desc&sort=name
     *
     * {@literal
     *   <h2 th:with="newQueryString=${#qs.createNewSort(#request.getQueryString(), {'city,desc', 'suburb'})}">
     * }
     *
     *     newQueryString = city=melbourne&postcode=3000&page=0&sort=city,desc&sort=suburb
     * </pre>
     *
     * <b>Note:</b>
     * <p>If the {@code queryString} is {@code null} or empty and {@code fieldAndDirections} contains values,
     * the new query string will consist of only the new {@code fieldAndDirections}, otherwise an empty
     * string will be returned.</p>
     *
     * @param queryString        The current query string.
     * @param fieldAndDirections The new sort field and directions such as {@code {'city,desc', 'suburb'}}
     * @return The new query string.
     */
    public String createNewSort(String queryString, List<String> fieldAndDirections) {
        List<List<String>> newKeyValuePairs = fieldAndDirections.stream()
                .map(fieldAndDirection -> Arrays.asList("sort", fieldAndDirection))
                .collect(Collectors.toList());

        return removeAllAndAdd(queryString, Collections.singletonList("sort"), newKeyValuePairs);
    }

    /**
     * Checks if the supplied {@code field} appears as a sort field.
     *
     * <h3>Thymeleaf Usage</h3>
     * <pre>
     *     #request.getQueryString() = city=melbourne&postcode=3000&page=0&sort=city,desc
     *
     * {@literal
     *   <h2 th:with="isSorted=${#qs.isFieldSorted(#request.getQueryString(), 'city')}">
     *   => isSorted = true
     * }
     * </pre>
     *
     * <b>Note:</b>
     * <p>Supplying a {@code null} or empty {@code queryString} will return {@code false}.
     * All other arguments must receive valid values otherwise the behaviour is undefined.</p>
     *
     * @param queryString The current query string.
     * @param field       The sort field.
     * @return {@code true} if the {@code field} appears as a sort field otherwise {@code false}.
     */
    public boolean isFieldSorted(String queryString, String field) {
        return getAllValues(queryString, "sort").stream()
                .map(QueryStringUtil::extractSortField)
                .anyMatch(value -> value.equals(field));
    }

    /**
     * See {@link #getCurrentSortDirectionAsc(String, String)} for examples. This method simply centralises the logic
     * based on the {@code defaultDirection}.
     *
     * @param queryString      The current query string.
     * @param field            The sort field.
     * @param defaultDirection The sort direction to use if no explicit sort direction exists or the sort field is not found.
     * @return The new query string.
     */
    private String getCurrentSortDirection(String queryString, String field, SortDirection defaultDirection) {
        final String sortFieldSeparator = ",";

        return getAllValues(queryString, "sort").stream()
                .map(value -> value.split(sortFieldSeparator))
                .filter(pair -> pair[0].equals(field))
                .map(pair -> pair.length == 1 ? defaultDirection.value : pair[1])
                .findFirst()
                .orElse(null);
    }


    /**
     * Extracts the {@code field} from the {@code queryString} if it appears as a sort field and returns its current
     * sort direction. There are 2 variants of this method {@code getCurrentSortDirectionXXX}. The trailing 'Asc' or
     * 'Desc' determines a sort fields default direction should it be missing. A missing sort direction is
     * referred to as an implicit direction which will return 'asc' if {@code getCurrentSortDirectionAsc}
     * is being used.
     *
     * <h3>Thymeleaf Usage</h3>
     *
     * <h4>Sort field has implicit direction</h4>
     * <p>sort field {@code suburb} has no direction so the fallback is to return the default 'asc' if
     * {@code getCurrentSortDirectionAsc} is being used.</p>
     *
     * <pre>
     *     #request.getQueryString() = "city=melbourne&postcode=3000&sort=suburb"
     *
     *     th:with="direction=${#qs.getCurrentSortDirectionAsc(#request.getQueryString(), 'suburb')}"
     *     => direction = "asc"
     * </pre>
     *
     * <h4>Sort field has explicit direction</h4>
     * <p>sort field {@code suburb} has an explicit sort direction of {@code desc} which is returned.</p>
     * <pre>
     *     #request.getQueryString() = "city=melbourne&postcode=3000&sort=suburb,desc"
     *
     *     th:with="direction=${#qs.getCurrentSortDirectionAsc(#request.getQueryString(), 'suburb')}"
     *     => direction = "desc"
     * </pre>
     *
     * <h4>Sort field does not exist</h4>
     * <p>sort field {@code country} does not exist so null is returned</p>
     * <pre>
     *     #request.getQueryString() = #request.getQueryString() = "city=melbourne&postcode=3000&sort=suburb"
     *
     *     th:with="direction=${#qs.getCurrentSortDirectionAsc(#request.getQueryString(), 'country')}"
     *     => direction = null
     * </pre>
     *
     * <h4>Sort field is a nested object</h4>
     * <p>Consider a {@code Person} containing an {@code Address}. For example, use the property path to access
     * the sort field 'address.suburb'. This assumes the {@code Person} is in the {@code Model}.</p>
     * <pre>
     *     #request.getQueryString() = "city=melbourne&postcode=3000&sort=address.suburb,desc"
     *
     *     th:with="direction=${#qs.getCurrentSortDirectionAsc(#request.getQueryString(), 'address.suburb')}"
     *     => direction = "desc"
     * </pre>
     *
     * <b>Note:</b>
     * <p>Supplying a {@code null} or empty {@code queryString} will return null.
     * All other arguments must receive valid values otherwise the behaviour is undefined.</p>
     *
     * @param queryString The current query string.
     * @param field       The sort field to find the sort direction for.
     * @return The sort direction of either {@code asc | desc} if found, otherwise {@code null}.
     */
    public String getCurrentSortDirectionAsc(String queryString, String field) {
        return getCurrentSortDirection(queryString, field, SortDirection.ASC);
    }

    /**
     * See {@link #getCurrentSortDirectionAsc(String, String)}. This method uses {@code desc} as the fallback
     * when the sort field contains no explicit sort direction.
     *
     * @param queryString The current query string.
     * @param field       The sort field to find the sort direction for.
     * @return The sort direction of either {@code asc | desc} if found, otherwise {@code null}.
     */
    public String getCurrentSortDirectionDesc(String queryString, String field) {
        return getCurrentSortDirection(queryString, field, SortDirection.DESC);
    }

    /**
     * Concatenates the request uri with the query string should it exist. The motivation for this method is to provide
     * a solution for restrictions preventing methods receiving the {@code HttpServletRequest} directly via
     * the {@code #request} object in Thymeleaf 3.0.9
     * http://forum.thymeleaf.org/Thymeleaf-3-0-9-JUST-PUBLISHED-td4030728.html.</p>
     *
     * <p>Since each method cannot accept a {@code #request}, its recommended to assign the query string to a new
     * variable before proceeding to rebuild the complete URI. </p>
     *
     * <h3>Thymeleaf usage</h3>
     *
     * <pre>
     *
     *     th:with="newQueryString=${#qs.incrementPage(#request.getQueryString())}"
     *     th:href="${#qs.url(#request.getRequestURI(), newQueryString)}"
     *
     * </pre>
     *
     * <p>Supplying a null or empty {@code requestURI} throws an IllegalArgumentException. If
     * the {@code queryString} is present it will be concatenated to the {@code requestURI}.</p>
     *
     * @param requestURI  The result of calling {@code #request.getRequestURI()}.
     * @param queryString The current query string.
     * @return The new query string.
     */
    public String url(String requestURI, String queryString) {
        if (requestURI == null || requestURI.isEmpty()) {
            throw new IllegalArgumentException("request URI cannot be null or empty");
        }

        return (queryString != null && !queryString.isEmpty()) ? requestURI + "?" + queryString : requestURI;
    }

    /**
     * See {@link #url(String, String)} for docs as this method is provided out of convenience to clean up thymeleaf
     * template code to avoid having to supply the {@code #request.getRequestURI()} each time.
     *
     * <p>
     *
     * <p>This method is a curried function accepting the {@code requestURI} and returns a function that
     * accepts the {@code queryString} which concatenates the 2 together. The below example shows the {@code urlBuilder}
     * being defined as a variable for reuse in every table header without needing to supply the {@code requestURI}
     * each time.</p>
     *
     * <h3>Thymeleaf Usage</h3>
     * <pre>
     *     {@literal
     *
     *     <table class="ui sortable celled table"
     *                th:with="qstring=${#request.getQueryString()},
     *                         urlBuilder=${#qs.urlBuilder(#request.getRequestURI())},
     *                         fieldSorterAsc=${#qs.fieldSorterAsc(qstring)}">
     *             <thead>
     *
     *             <tr>
     *                 <th th:with="newUrl=${urlBuilder.apply(fieldSorterAsc.apply('name'))}">
     *                     Hotel
     *                 </th>
     *              </tr>
     *              ....
     *              </thead>
     *    </table>
     *     }
     * </pre>
     *
     * @param requestURI The result of {@code #request.getRequestURI()}
     * @return The new query string.
     */
    public Function<String, String> urlBuilder(String requestURI) {
        return queryString -> url(requestURI, queryString);
    }

    /*
       also have variants of the same method that just take in a #request and call getQueryString for shorthand.


       // spring mvc convenience helpers.
       incrementPage
       decrementPage

       something to toggle sort order for a key. if desc, change to asc and vice versa.


       github java doc
       look at java8extras for how to integrate with ide autocomplete and structure project.
       ask thymeleaf people about contrib or maintain it as a separate dialect
     */


}
