package com.demo.thymeleaf.expression;


import com.demo.thymeleaf.utils.QueryString;
import com.demo.thymeleaf.utils.SortDirection;
import org.thymeleaf.expression.Uris;

import javax.servlet.http.HttpServletRequest;
import java.util.*;
import java.util.stream.Collectors;

public class QueryStringHelper {

    private final Uris uris = new Uris();

    /**
     * Replaces only the first occurrence of 'key' with 'value' while maintaining the query strings original order.
     *
     * <h3>Given query string</h3>
     *
     * <pre>
     *     "suburb=west&region=AU&postcode=494849"
     * </pre>
     *
     * <h3>Thymeleaf usage</h3>
     *
     * <pre>
     *     th:with="newQueryString=${#qs.replaceFirst(#request, 'region', 'Australia')}"
     * </pre>
     *
     * <h3>Result</h3>
     *
     * <pre>
     *     newQueryString = "suburb=west&region=Australia&postcode=494849"
     * </pre>
     *
     * @param request The {@code HttpServletRequest}
     * @param key     The target key to replace the value for.
     * @param value   The replacement value.
     * @return The new query string.
     */
    public String replaceFirst(HttpServletRequest request, String key, String value) {
        return QueryString.of(request.getQueryString(), uris).replaceFirst(key, value);
    }

    /**
     * Replaces the nth key with the supplied value based on a keys relative index while maintaining the query strings
     * original order.
     *
     * <h3>Explanation</h3>
     * <p>{@code region} can be thought of as having an array of 3 values. The example illustrates replacing
     * {@code region[1], region[2]} with new values while {@code region[0]} remains unchanged.
     * </p>
     *
     * <h3>Given query string</h3>
     *
     * <pre>
     *     "region=AU&suburb=west&region=Australia&postcode=494849&region=AUS"
     *
     *     region = ['AU', 'Australia', 'AUS']
     * </pre>
     *
     * <h3>Thymeleaf usage</h3>
     *
     * <pre>
     *     th:with="newQueryString=${#qs.replaceNth(#request, {region: { 1: 'Auckland', 2: 'AUKL' }})}"
     * </pre>
     *
     * <h3>Result</h3>
     *
     * <pre>
     *     newQueryString = "region=AU&suburb=west&region=Auckland&postcode=494849&region=AUKL"
     * </pre>
     *
     * <p>If the key does not exist or an illegal index is provided, the query string is left unchanged.</p>
     *
     * <p><b>Note:</b> The syntax for the {@code stateChangeInstructions} Map must be exactly as the example shows,
     * otherwise there will be casting errors.</p>
     *
     * @param request                 The {@code HttpServletRequest}.
     * @param stateChangeInstructions A map containing the instructions on how to modify the query string.
     * @return The new query string.
     */
    public String replaceNth(HttpServletRequest request, Map<String, Map<Integer, String>> stateChangeInstructions) {
        return QueryString.of(request.getQueryString(), uris).replaceNth(stateChangeInstructions);
    }

    /**
     * Replaces the first N values associated with the key while maintaining the query strings original order.
     * Consider the below example.
     *
     * <h3>Explanation</h3>
     *
     * <p>The key {@code 'name'} can be visualised as an array containing 3 values. {@code ['john', 'joseph', 'smith']}.</p>
     * <p>The example shows 2 values being provided {@code ['mary', 'rose']} which replace the existing 2 values in
     * the corresponding index positions while leaving the last index of {@code 'smith'} unchanged.</p>
     *
     * <p>If 1 value was provided, only the first value would be replaced, whereas if 100 values were provided then
     * all 3 would be replaced by the first 3 corresponding replacement values.</p>
     *
     * <h3>Given query string</h3>
     *
     * <pre>
     *     "name=john&age=30&name=joseph&month=march&name=smith"
     * </pre>
     *
     * <h3>Thymeleaf usage</h3>
     *
     * <pre>
     *     th:with="newQueryString=${#qs.replaceN(#request, 'name', {'mary', 'rose'})}"
     * </pre>
     *
     * <h3>Result</h3>
     *
     * <pre>
     *     newQueryString = "name=mary&age=30&name=rose&month=march&name=smith"
     * </pre>
     *
     * <p>Supplying a non existing key has no effect.</p>
     *
     * <p><b>Note:</b> The syntax for the list of {@code values} must be exactly as the example shows,
     * otherwise there will be casting errors.</p>
     *
     * @param request The {@code HttpServletRequest}.
     * @param key     The target key to replace the corresponding values for.
     * @param values  The replacement values, eg values[0] will replace the keys value at index 0 in the query string.
     * @return The new query string.
     */
    public String replaceN(HttpServletRequest request, String key, List<String> values) {
        return QueryString.of(request.getQueryString(), uris).replaceN(key, values);
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
     * When there are duplicate keys such as {@code 'name'} containing 3 values {@code ['john', 'joseph', 'smith']}.
     * The effect of calling {@code removeFirst} is simply removing name[0].
     * </li>
     * </ol>
     *
     * <h3>Given query string</h3>
     *
     * <pre>
     *     "name=john&age=30&name=joseph&month=march&name=smith"
     * </pre>
     *
     * <h3>Thymeleaf usage</h3>
     *
     * <pre>
     *     Duplicate 1. th:with="newQueryString=${#qs.removeFirst(#request, 'name')}"
     *
     *     Unique 2. th:with="newQueryString=${#qs.removeFirst(#request, 'age')}"
     * </pre>
     *
     * <h3>Result</h3>
     *
     * <pre>
     *     Duplicate 1. newQueryString = "age=30&name=joseph&month=march&name=smith"
     *
     *     Unique 2. newQueryString = "name=john&name=joseph&month=march&name=smith"
     * </pre>
     *
     * <p>Supplying a non existing key has no effect.</p>
     *
     * @param request The {@code HttpServletRequest}.
     * @param key     The target key to remove.
     * @return The new query string.
     */
    public String removeFirst(HttpServletRequest request, String key) {
        return QueryString.of(request.getQueryString(), uris).removeFirst(key);
    }

    /**
     * Removes every key and their associated values while maintaining the query strings original order.
     *
     * <h3>Explanation</h3>
     * All occurrences of the supplied keys are removed. The example shows removing keys {@code ['region', 'postcode']}
     * with the resulting query string including only the other 2 keys of {@code 'suburb' and 'language'}.
     *
     * <h3>Given query string</h3>
     *
     * <pre>
     *     "region=AU&suburb=west&region=Australia&postcode=494849&region=AUS&language=en"
     * </pre>
     *
     * <h3>Thymeleaf usage</h3>
     *
     * <pre>
     *     th:with="newQueryString=${#qs.removeAll(#request, {'region', 'postcode'})}"
     * </pre>
     *
     * <h3>Result</h3>
     *
     * <pre>
     *     newQueryString = "suburb=west&language=en"
     * </pre>
     *
     * <p>Supplying a non existing key has no effect.</p>
     *
     * <p><b>Note:</b> The syntax for the list of {@code keys} must be exactly as the example shows, otherwise
     * there will be casting errors.</p>
     *
     * @param request The {@code HttpServletRequest}.
     * @param keys    The keys to remove.
     * @return The new query string.
     */
    public String removeAll(HttpServletRequest request, List<String> keys) {
        return QueryString.of(request.getQueryString(), uris).removeAll(keys);
    }

    /**
     * The same concept as {@code dropN} in functional languages where the first n occurrences of the target key are removed
     * while maintaining the query strings original order.
     *
     * <h3>Explanation</h3>
     * The key {@code 'name'} can be visualised as an array containing 3 values. {@code ['john', 'joseph', 'smith']}.
     * {@code removeN will simply delete the first n values as below}
     *
     * <pre>
     *     n=-1 -> ['john', 'joseph', 'smith']
     *     n=0 -> ['john', 'joseph', 'smith']
     *     n=1 -> ['joseph', 'smith']
     *     n=2 -> ['smith']
     *     n=3 -> []
     *     n=100 -> []
     * </pre>
     * <p>
     * The example removes the {@code 'name'} keys first 2 values with {@code 'smith'} still remaining.
     *
     * <h3>Given query string</h3>
     *
     * <pre>
     *     "name=john&age=30&name=joseph&month=march&name=smith"
     * </pre>
     *
     * <h3>Thymeleaf usage</h3>
     *
     * <pre>
     *     th:with="newQueryString=${#qs.removeN(#request, 'name', 2)}"
     * </pre>
     *
     * <h3>Result</h3>
     *
     * <pre>
     *     newQueryString = "age=30&month=march&name=smith"
     * </pre>
     *
     * <p>Supplying a non existing key has no effect.</p>
     *
     * @param request The {@code HttpServletRequest}.
     * @param key     The keys to remove.
     * @param n       The keys to remove.
     * @return The new query string.
     */
    public String removeN(HttpServletRequest request, String key, int n) {
        return QueryString.of(request.getQueryString(), uris).removeN(key, n);
    }

    // removes the nth occurrence of given key.

    /**
     * Removes the nth relative index of the given key while maintaining the query strings original order.
     *
     * <h3>Explanation</h3>
     * The concept of relative index is outlined below using the {@code 'name'} key as the target.
     *
     * <pre>
     *     "name=john&age=30&name=joseph&month=march&name=smith"
     *
     *               0         1         2      (relative indexes)
     *     name = ['john', 'joseph', 'smith']
     * </pre>
     *
     * <p>The example demonstrates removing the middle name at index 1.</p>
     *
     * <h3>Given query string</h3>
     *
     * <pre>
     *     "name=john&age=30&name=joseph&month=march&name=smith"
     * </pre>
     *
     * <h3>Thymeleaf usage</h3>
     *
     * <pre>
     *     th:with="newQueryString=${#qs.removeNth(#request, 'name', 1)}"
     * </pre>
     *
     * <h3>Result</h3>
     *
     * <pre>
     *     newQueryString = "name=john&age=30&month=march&name=smith"
     * </pre>
     *
     * <p>Supplying a non existing key or illegal index has no effect.</p>
     *
     * @param request  The {@code HttpServletRequest}.
     * @param key      The target key.
     * @param nthIndex The relative index to remove for the given key.
     * @return The new query string.
     */
    public String removeNth(HttpServletRequest request, String key, int nthIndex) {
        return QueryString.of(request.getQueryString(), uris).removeNth(key, nthIndex);
    }

    /**
     * Applies the same logic as {@link #removeNth(HttpServletRequest, String, int)} except provides the ability
     * to remove multiple relative indexes at the same time.
     *
     * <h3>Explanation</h3>
     * The example shows how its possible to remove relative indexes 0 and 2 while keeping the
     * middle name {@code 'joseph'}.
     *
     * <pre>
     *     "name=john&age=30&name=joseph&month=march&name=smith"
     *
     *               0         1         2      (relative indexes)
     *     name = ['john', 'joseph', 'smith']
     * </pre>
     *
     * <h3>Given query string</h3>
     *
     * <pre>
     *     "name=john&age=30&name=joseph&month=march&name=smith"
     * </pre>
     *
     * <h3>Thymeleaf usage</h3>
     *
     * <pre>
     *     th:with="newQueryString=${#qs.removeManyNth(#request, 'name', {0, 2})}"
     * </pre>
     *
     * <h3>Result</h3>
     *
     * <pre>
     *     newQueryString = "age=30&name=joseph&month=march"
     * </pre>
     *
     * <p>Supplying a non existing key or illegal indexes has no effect.</p>
     *
     * <p><b>Note:</b> The syntax for the list of {@code relativeIndexes} be exactly as the example shows,
     * otherwise there will be casting errors.</p>
     *
     * @param request         The {@code HttpServletRequest}.
     * @param key             The target key.
     * @param relativeIndexes The relative indexes to remove for the given key.
     * @return The new query string.
     */
    public String removeManyNth(HttpServletRequest request, String key, List<Integer> relativeIndexes) {
        if (key == null || relativeIndexes == null || relativeIndexes.isEmpty()) {
            return request.getQueryString();
        }
        return QueryString.of(request.getQueryString(), uris).removeManyNth(key, relativeIndexes);
    }

    // If the given key has the matched value remove it. If there are many duplicate keys having the same value, all keys will be removed.

    /**
     * Removes the target key if its value is equal to the matching value - (case insensitive equality).
     *
     * <h3>Explanation</h3>
     * The example shows that for the target key {@code 'region'}, only remove the key if the value is equal
     * to 'australia'. Note the case insensitivity.
     *
     * <h3>Given query string</h3>
     *
     * <pre>
     *     "region=AU&region=south&region=AUSTRALIA&sort=country,asc"
     * </pre>
     *
     * <h3>Thymeleaf usage</h3>
     *
     * <pre>
     *     th:with="newQueryString=${#qs.removeKeyMatchingValue(#request, 'region', 'australia')}"
     * </pre>
     *
     * <h3>Result</h3>
     *
     * <pre>
     *     newQueryString = "region=AU&region=south&sort=country,asc"
     * </pre>
     *
     * <p>Supplying a non existing key or null {@code valueMatch} has no effect.</p>
     *
     * @param request    The {@code HttpServletRequest}.
     * @param key        The target key.
     * @param valueMatch The value to match which triggers deletion.
     * @return The new query string.
     */
    public String removeKeyMatchingValue(HttpServletRequest request, String key, String valueMatch) {
        return QueryString.of(request.getQueryString(), uris).removeKeyMatchingValue(key, valueMatch);
    }

    /**
     * Similar to {@link #removeKeyMatchingValue(HttpServletRequest, String, String)} except no target key is
     * provided causing all keys to be eligible for removal if the value matches. Case insensitive equality applies.
     *
     * <h3>Explanation</h3>
     * The example shows any key matching the value 'australia' (case insensitive) will be removed.
     *
     * <h3>Given query string</h3>
     *
     * <pre>
     *     "region=AU&region=south&region=AUSTRALIA&sort=country,asc&locale=australia"
     * </pre>
     *
     * <h3>Thymeleaf usage</h3>
     *
     * <pre>
     *     th:with="newQueryString=${#qs.removeAnyKeyMatchingValue(#request, 'australia')}"
     * </pre>
     *
     * <h3>Result</h3>
     *
     * <pre>
     *     newQueryString = "region=AU&region=south&sort=country,asc"
     * </pre>
     *
     * <p>Supplying a null {@code valueMatch} has no effect.</p>
     *
     * @param request    The {@code HttpServletRequest}.
     * @param valueMatch The value to match which triggers deletion.
     * @return The new query string.
     */
    public String removeAnyKeyMatchingValue(HttpServletRequest request, String valueMatch) {
        return QueryString.of(request.getQueryString(), uris).removeAnyKeyMatchingValue(valueMatch);
    }

    /**
     * Gets the value associated with the first occurrence of the given key.
     *
     * <h3>Explanation</h3>
     * <ol>
     * <li>If all keys are unique, the single value associated to the key is returned - example 2.</li>
     * <li>If there are duplicate keys such as {@code 'name'} with values {@code ['john', 'joseph', 'smith']}
     * then {@code getFirstValue} returns {@code name[0]} as shown in example 1.
     * </li>
     * </ol>
     *
     * <h3>Given query string</h3>
     *
     * <pre>
     *     "name=john&age=30&name=joseph&month=march&name=smith"
     * </pre>
     *
     * <h3>Thymeleaf usage</h3>
     *
     * <pre>
     *     Duplicate 1. th:with="newQueryString=${#qs.getFirstValue(#request, 'name')}"
     *
     *     Unique: 2. th:with="newQueryString=${#qs.getFirstValue(#request, 'age')}"
     * </pre>
     *
     * <h3>Result</h3>
     *
     * <pre>
     *     Duplicate 1. "john"
     *
     *     Unique 2. "30"
     * </pre>
     *
     * <p>Supplying a non existing key or null will return null.</p>
     *
     * @param request The {@code HttpServletRequest}.
     * @param key     The key to get the value for.
     * @return The associated value or null if the key does not exist.
     */
    public String getFirstValue(HttpServletRequest request, String key) {
        return QueryString.of(request.getQueryString(), uris).getFirstValue(key);
    }

    /**
     * Gets all value associated with the the given key.
     *
     * <h3>Explanation</h3>
     * <ol>
     * <li>If all keys are unique, the returned list will contain the single value associated to the key - example 2.</li>
     * <li>If there are duplicate keys such as {@code 'name'} with values {@code ['john', 'joseph', 'smith']}
     * then {@code getFirstValue} returns all 3 values in a list as shown in example 1.
     * </li>
     * </ol>
     *
     * <h3>Given query string</h3>
     *
     * <pre>
     *     "name=john&age=30&name=joseph&month=march&name=smith"
     * </pre>
     *
     * <h3>Thymeleaf usage</h3>
     *
     * <pre>
     *     Duplicate 1. th:with="newQueryString=${#qs.getAllValues(#request, 'name')}"
     *
     *     Unique: 2. th:with="newQueryString=${#qs.getAllValues(#request, 'age')}"
     * </pre>
     *
     * <h3>Result</h3>
     *
     * <pre>
     *     Duplicate 1. ["john", "joseph", "smith"]
     *
     *     Unique 2. ["30"]
     * </pre>
     *
     * <p>Supplying a non existing key or null will return null.</p>
     *
     * @param request The {@code HttpServletRequest}.
     * @param key     The key to get the values for.
     * @return The associated value or null if the key does not exist.
     */
    public List<String> getAllValues(HttpServletRequest request, String key) {
        return QueryString.of(request.getQueryString(), uris).getAllValues(key);
    }

    /**
     * Adds the given key and value to the end of the query string. Note: The value is escaped
     * into a valid query string.
     *
     * <h3>Given query string</h3>
     *
     * <pre>
     *     "name=john&age=30"
     * </pre>
     *
     * <h3>Thymeleaf usage</h3>
     *
     * <pre>
     *     th:with="newQueryString=${#qs.add(#request, 'city', 'san francisco')}"
     * </pre>
     *
     * <h3>Result</h3>
     *
     * <pre>
     *     newQueryString = name=john&age=30&city=san%20francisco
     * </pre>
     *
     * <p>Supplying a non existing key, empty key or value will have no effect</p>
     *
     * @param request The {@code HttpServletRequest}.
     * @param key     The key.
     * @param value   The value to associate to the key.
     * @return The new query string.
     */
    public String add(HttpServletRequest request, String key, String value) {
        return QueryString.of(request.getQueryString(), uris).add(key, value);
    }

    /**
     * Behaves exactly like {@link #add(HttpServletRequest, String, String)} except adds many key value pairs
     * to the end of the query string in the exact order they are provided. If the key/value pair already exist, it is
     * ignored.
     *
     * <h3>Explanation</h3>
     * <p>The example shows adding 2 new key/value pairs (city and country) while ignoring {@code name='john'} since
     * it already exists.
     * </p>
     *
     * <h3>Given query string</h3>
     *
     * <pre>
     *     "name=john&age=30"
     * </pre>
     *
     * <h3>Thymeleaf usage</h3>
     *
     * <pre>
     *     th:with="newQueryString=${#qs.addAll(#request, {{'city', 'san francisco'}, {'country', 'US'}, {'name', 'john'}})}"
     * </pre>
     *
     * <h3>Result</h3>
     *
     * <pre>
     *     newQueryString = name=john&age=30&city=san%20francisco&country=US
     * </pre>
     *
     * <p>Supplying a non existing key, empty key or value will have no effect</p>
     *
     * <p><b>Note:</b> The syntax for the 2d list of {@code keyValuePairs} must be exactly as the example shows, otherwise
     * there will be casting errors.</p>
     *
     * @param request       The {@code HttpServletRequest}.
     * @param keyValuePairs The list of key value pairs to add to the end of the query string if it does not exist.
     * @return The new query string.
     */
    public String addAll(HttpServletRequest request, List<List<String>> keyValuePairs) {
        return QueryString.of(request.getQueryString(), uris).addAll(keyValuePairs);
    }

    /**
     * Convenience method to remove all occurrences of a list of keys followed by adding a list of key value pairs.
     *
     * <h3>Explanation</h3>
     * <p>This method is a useful alternative to executing a replace. Sometimes its not known the exact format of
     * the query string so it is easier to simply remove 1 or many keys followed by adding the new key/value pairs.</p>
     *
     * <p>The example demonstrates removing all occurrences of the postcode and sort keys then re-adding the new sort key
     * to the end of the query string. Using spring as an example, the primary sort field is defined first followed by
     * secondary sort keys. The example changes the secondary sort key to be the new primary sort field.
     * </p>
     *
     * <h3>Given query string</h3>
     *
     * <pre>
     *     "sort=country,asc&sort=city,desc&location=AU&region=north&postcode=4931495"
     * </pre>
     *
     * <h3>Thymeleaf usage</h3>
     *
     * <pre>
     *     th:with="newQueryString=${#qs.removeAndAdd(#request, {'postcode', 'sort'}, {{'sort', 'city,desc'}})}"
     * </pre>
     *
     * <h3>Result</h3>
     *
     * <pre>
     *     newQueryString = location=AU&region=north&sort=city,desc
     * </pre>
     *
     * <p>Supplying a non existing key, empty key or value will have no effect</p>
     *
     * <p><b>Note:</b> The syntax for the list arguments must be exactly as the example shows, otherwise
     * there will be casting errors.</p>
     *
     * @param request          The {@code HttpServletRequest}.
     * @param removeKeys       The keys to remove.
     * @param addKeyValuePairs The new key value pairs to add to the end of the query string.
     * @return The new query string.
     */
    public String removeAndAdd(HttpServletRequest request, List<String> removeKeys, List<List<String>> addKeyValuePairs) {
        String afterRemovalQueryString = QueryString.of(request.getQueryString(), uris).removeAll(removeKeys);
        return QueryString.of(afterRemovalQueryString, uris).addAll(addKeyValuePairs);
    }

    /**
     * Convenience method to perform a {@link #removeNth(HttpServletRequest, String, int)} on many unique keys while
     * maintaining the original query strings ordering. After the removal, the list of key value pairs are added to
     * the end of the query string.
     *
     * <h3>Explanation</h3>
     * <p>The example shows key {@code 'sort'} having values {@code ['country,asc', 'city,desc']} and key {@code 'region'}
     * with values {@code ['north, 'upper', 'border']}.</p>
     *
     * <p>The removal step has the effect of deleting {@code sort[0]} and {@code region[1], region[2]}. After the removal,
     * 2 new key value pairs are added to the end of the query string.</p>
     *
     * <h3>Given query string</h3>
     *
     * <pre>
     *     "sort=country,asc&sort=city,desc&location=AU&region=north&region=upper&region=border"
     * </pre>
     *
     * <h3>Thymeleaf usage</h3>
     *
     * <pre>
     *     th:with="newQueryString=${#qs.removeNthAndAdd(#request, {'sort': {0}, 'region': {1, 2}}, {{'postcode', '39481'}, {'locale', 'AU'}})}"
     * </pre>
     * *
     * <h3>Result</h3>
     *
     * <pre>
     *     newQueryString = sort=city,desc&location=AU&region=north&postcode=39481&locale=AU
     * </pre>
     *
     * <p>Supplying null or empty arguments will have no effect.</p>
     *
     * <p><b>Note:</b> The syntax for the {@code removeInstructions} and {@code addKeyValuePairs} must be exactly as
     * the example shows, otherwise there will be casting errors.</p>
     *
     * @param request            The {@code HttpServletRequest}.
     * @param removeInstructions The keys and corresponding relative value indexes to remove.
     * @param addKeyValuePairs   The new key value pairs to add to the end of the query string.
     * @return The new query string.
     */
    public String removeNthAndAdd(HttpServletRequest request,
                                  Map<String, List<Integer>> removeInstructions,
                                  List<List<String>> addKeyValuePairs) {
        if (removeInstructions == null || addKeyValuePairs == null) {
            return request.getQueryString();
        }

        // The reduction applies the removal action for each key
        String afterRemovalQueryString = removeInstructions.entrySet().stream()
                .reduce(request.getQueryString(), (queryString, entry) ->
                        QueryString.of(queryString, uris)
                                .removeManyNth(entry.getKey(), entry.getValue()), (a, b) -> a + "&" + b);

        try {
            QueryString queryString = QueryString.of(afterRemovalQueryString, uris);
            return queryString.addAll(addKeyValuePairs);
        } catch (IllegalArgumentException e) {
            // Invalid query string form, implies mismatch key=value pairs or empty string.
            return addKeyValuePairs.stream()
                    .map(QueryString.KeyValue::fromPair)
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .map(kv -> kv.escape(uris::escapeQueryParam))
                    .collect(Collectors.joining("&"));
        }
    }

    /**
     * Adds the supplied value to a range of numerical key values defined by relative index. Decrementing a value can be
     * achieved by supplying a negative number.
     *
     * <h3>Explanation</h3>
     * <p>The example shows key {@code 'policy'} having values {@code [10, 20, 30]}. Lets assume we want to add 5 to only
     * the values at index 1 and 2. The result is the last 2 values being incremented by 5 while the first value remains
     * unchanged at 10.</p>
     *
     * <h3>Given query string</h3>
     *
     * <pre>
     *     "policy=10&sort=country&policy=20&location=AU&border=north&policy=30"
     * </pre>
     *
     * <h3>Thymeleaf usage</h3>
     *
     * <pre>
     *     th:with="newQueryString=${#qs.adjustNumericValueBy(#request, 'policy', {1, 2}, 5)}"
     * </pre>
     *
     * <h3>Result</h3>
     *
     * <pre>
     *     newQueryString = policy=10&sort=country&policy=25&location=AU&border=north&policy=35
     * </pre>
     *
     * <p>Supplying null or empty arguments will have no effect.</p>
     *
     * <p><b>Note:</b> The syntax for the {@code relativeIndexes} must be exactly as the example shows,
     * otherwise there will be casting errors.</p>
     *
     * @param request         The {@code HttpServletRequest}.
     * @param key             The target key to find numeric values for.
     * @param relativeIndexes Which indexes to add the value to.
     * @param value           The value to add (for decrementing use a negative number).
     * @return The new query string.
     */
    public String adjustNumericValueBy(HttpServletRequest request, String key, List<Integer> relativeIndexes, int value) {
        return QueryString.of(request.getQueryString(), uris).adjustNumericValueBy(key, relativeIndexes, value);
    }

    /**
     * Functions the same as {@link #adjustNumericValueBy(HttpServletRequest, String, List, int)} except only updates
     * the first occurrence of the numeric key value. This method is provided for convenience to avoid having to provide
     * the relative index list. To decrement the value supply a negative number.
     *
     * <h3>Explanation</h3>
     * <p>The example shows key {@code 'policy'} having values {@code [10, 20, 30]}. Since this method only updates
     * index 0 (the first value), adding 2 to the current value will result in 12 as the new value.</p>
     *
     * <h3>Given query string</h3>
     *
     * <pre>
     *     "policy=10&sort=country&policy=20&location=AU&border=north&policy=30"
     * </pre>
     *
     * <h3>Thymeleaf usage</h3>
     *
     * <pre>
     *     th:with="newQueryString=${#qs.adjustFirstNumericValueBy(#request, 'policy', 2)}"
     * </pre>
     *
     * <h3>Result</h3>
     *
     * <pre>
     *     newQueryString = policy=12&sort=country&policy=20&location=AU&border=north&policy=30
     * </pre>
     *
     * <p>Supplying null or empty arguments will have no effect.</p>
     *
     * @param request         The {@code HttpServletRequest}.
     * @param key             The target key to find the first numeric value for.
     * @param value           The value to add (for decrementing use a negative number).
     * @return The new query string.
     */
    public String adjustFirstNumericValueBy(HttpServletRequest request, String key, int value) {
        return QueryString.of(request.getQueryString(), uris).adjustNumericValueBy(key, Collections.singletonList(0), value);
    }

    // designed to work with spring mvc paging and sorting repository.
    public String incrementPage(HttpServletRequest request) {
        return adjustNumericValueBy(request, "page", Collections.singletonList(0), 1);
    }

    public String decrementPage(HttpServletRequest request) {
        return QueryString.of(request.getQueryString(), uris)
                .adjustNumericValueBy("page", Collections.singletonList(0), -1, currentValue -> currentValue > 0);
    }


    public String setSortDirection(HttpServletRequest request, String sortField, String sortDirection) {
        if (sortField == null || sortField.isEmpty() || sortDirection == null || sortDirection.isEmpty()) {
            return request.getQueryString();
        }

        SortDirection direction = SortDirection.from(sortDirection);
        if (direction == SortDirection.NONE) {
            throw new IllegalArgumentException("Invalid sort direction '" + sortDirection + "', expect either 'asc' or 'desc'");
        }

        return QueryString.of(request.getQueryString(), uris)
                .setSortDirection(sortField, currentDirection -> direction);
    }


    // assumes default ordering is asc
    public String toggleSortDefaultAsc(HttpServletRequest request, String sortField) {
        return QueryString.of(request.getQueryString(), uris).toggleSortDefaultAsc(sortField);
    }

    public String toggleSortDefaultDesc(HttpServletRequest request, String sortField) {
        return QueryString.of(request.getQueryString(), uris).toggleSortDefaultDesc(sortField);
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
