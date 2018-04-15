package com.demo.thymeleaf.expression;


import com.demo.thymeleaf.utils.QueryString;
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
     *     "region=AU&suburb=west&postcode=494849"
     * </pre>
     *
     * <h3>Thymeleaf usage</h3>
     *
     * <pre>
     *     th:with="newQueryString=${#qs.replaceFirst(#request, 'region', 'Australia')}
     * </pre>
     *
     * <h3>Result</h3>
     *
     * <pre>
     *     newQueryString = "region=Australia&suburb=west&postcode=494849"
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
     *     th:with="newQueryString=${#qs.replaceNth(#request, {region: { 1: 'Auckland', 2: 'AUKL' }})}
     * </pre>
     *
     * <h3>Result</h3>
     *
     * <pre>
     *     newQueryString = "region=AU&suburb=west&region=Auckland&postcode=494849&region=AUKL"
     * </pre>
     * <p>
     * If the key does not exist or an illegal index is provided, the query string is left unchanged.
     *
     * @param request                 The {@code HttpServletRequest}.
     * @param stateChangeInstructions A map containing the instructions.
     * @return The new query string.
     */
    public String replaceNth(HttpServletRequest request, Map<Object, Map<Object, Object>> stateChangeInstructions) {
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
     * @param request The {@code HttpServletRequest}.
     * @param key     The target key to replace the corresponding values for.
     * @param values  The replacement values, eg values[0] will replace the keys value at index 0 in the query string.
     *                Note: values must be strings (wrap in single quotes) otherwise there will be casting errors.
     * @return The new query string.
     */
    public String replaceN(HttpServletRequest request, String key, List<Object> values) {
        List<String> castedValues = values.stream().map(obj -> (String) obj).collect(Collectors.toList());
        return QueryString.of(request.getQueryString(), uris).replaceN(key, castedValues);
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
    public String removeNth(HttpServletRequest request, String key, int nth) {
        return QueryString.of(request.getQueryString(), uris).removeNth(key, nth);
    }

    // warn about cast to int error
    public String removeManyNth(HttpServletRequest request, String key, List<Object> relativeIndexes) {
        if (key == null || relativeIndexes == null || relativeIndexes.isEmpty()) {
            return request.getQueryString();
        }

        List<Integer> castedIndexes = relativeIndexes.stream()
                .map(index -> Integer.parseInt((String) index))
                .collect(Collectors.toList());

        return QueryString.of(request.getQueryString(), uris).removeManyNth(key, castedIndexes);
    }

    // If the given key has the matched value remove it. If there are many duplicate keys having the same value, all keys will be removed.
    public String removeKeyMatchingValue(HttpServletRequest request, String key, String valueMatch) {
        return QueryString.of(request.getQueryString(), uris).removeKeyMatchingValue(key, valueMatch);
    }

    public String removeAnyKeyMatchingValue(HttpServletRequest request, String valueMatch) {
        return QueryString.of(request.getQueryString(), uris).removeAnyKeyMatchingValue(valueMatch);
    }

    // Pass in HttpRequestServlet and call getQueryString on all methods.
    // returns unescaped value.
    public String getFirstValue(HttpServletRequest request, String key) {
        return QueryString.of(request.getQueryString(), uris).getFirstValue(key);
    }

    // returns unescaped value.
    public List<String> getAllValues(HttpServletRequest request, String key) {
        return QueryString.of(request.getQueryString(), uris).getAllValues(key);
    }

    public String add(HttpServletRequest request, String key, String value) {
        return QueryString.of(request.getQueryString(), uris).add(key, value);
    }

    public String addAll(HttpServletRequest request, List<List<String>> keyValuePairs) {
        return QueryString.of(request.getQueryString(), uris).addAll(keyValuePairs);
    }

    public String removeAndAdd(HttpServletRequest request, List<String> removeKeys, List<List<String>> addKeyValuePairs) {
        String afterRemovalQueryString = QueryString.of(request.getQueryString(), uris).removeAll(removeKeys);
        return QueryString.of(afterRemovalQueryString, uris).addAll(addKeyValuePairs);
    }

    public String removeNthAndAdd(HttpServletRequest request,
                                  Map<Object, List<Object>> removeInstructions,
                                  List<List<String>> addKeyValuePairs) {
        if (removeInstructions == null || addKeyValuePairs == null) {
            return request.getQueryString();
        }

        // The reduction applies the removal action for each key
        String afterRemovalQueryString = removeInstructions.entrySet().stream()
                .reduce(request.getQueryString(), (queryString, entry) -> {
                    String key = (String) entry.getKey();

                    // casting...
                    List<Integer> relativeIndexes = entry.getValue().stream()
                            .map(index -> Integer.parseInt((String) index))
                            .collect(Collectors.toList());

                    return QueryString.of(queryString, uris).removeManyNth(key, relativeIndexes);
                }, (a, b) -> a + "&" + b);


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

    public String adjustNumericValueBy(HttpServletRequest request, String key, List<Object> relativeIndexes, int value) {
        List<Integer> castedIndexes = relativeIndexes.stream()
                .map(obj -> Integer.parseInt((String) obj))
                .collect(Collectors.toList());
        return QueryString.of(request.getQueryString(), uris).adjustNumericValueBy(key, castedIndexes, value);
    }

    public String incrementPage(HttpServletRequest request) {
        return adjustNumericValueBy(request, "page", Collections.singletonList("0"), 1);
    }

    public String decrementPage(HttpServletRequest request) {
        return QueryString.of(request.getQueryString(), uris)
                .adjustNumericValueBy("page", Collections.singletonList(0), -1, currentValue -> currentValue > 0);
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
