package com.demo.thymeleaf.expression;


import com.demo.thymeleaf.utils.QueryString;
import org.thymeleaf.expression.Uris;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class QueryStringHelper {

    private final Uris uris = new Uris();

    /**
     * Replaces only the first occurrence of 'key' with 'value'.
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
     * Replaces the nth key with the supplied value based on a keys relative index. Consider the below query string,
     * where {@code region} can be thought of as having an array of 3 values. The example illustrates replacing
     * {@code region[1], region[2]} with new values with {@code region[0]} remaining unchanged.
     *
     * <h3>Given query string</h3>
     *
     * <pre>
     *     "region=AU&suburb=west&region=Australia&postcode=494849&region=AUS"
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
     *
     * @param request                 The {@code HttpServletRequest}
     * @param stateChangeInstructions A map containing the instructions
     * @return The new query string.
     */
    public String replaceNth(HttpServletRequest request, Map<Object, Map<Object, Object>> stateChangeInstructions) {
        return QueryString.of(request.getQueryString(), uris).replaceNth(stateChangeInstructions);
    }


    /**
     * Replaces N values associated with the key.
     * <br>
     * <br>
     * <p>For example, the key {@code name} has 2 values where name[0] is replaced with 'sally' and
     * name[1] is replaced with 'clarke'. The index positions in the value array correspond to
     * the query string positions. If a different number of replacement values are provided such as 10, only
     * the first 2 values in the original query string are replaced with the first 2 values in the replacement list.</p>
     *
     * <h3>Given query string</h3>
     *
     * <pre>
     *     "name=john&age=30&name=smith"
     * </pre>
     *
     * <h3>Thymeleaf usage</h3>
     *
     * <pre>
     *     th:with="newQueryString=${#qs.replaceN(#request.getQueryString(), ['sally', 'clarke'])}
     * </pre>
     *
     * <h3>Result</h3>
     *
     * <pre>
     *     newQueryString = "name=sally&age=30&name=clarke"
     * </pre>
     *
     * @param request The {@code HttpServletRequest}
     * @param key     The target key to replace the corresponding values for.
     * @param values  The replacement values, eg values[0] will replace the keys value at index 0 in the query string.
     * @return The new query string.
     */
    public String replaceN(HttpServletRequest request, String key, List<String> values) {
        return QueryString.of(request.getQueryString(), uris).replaceN(key, values);
    }

    public String removeFirst(HttpServletRequest request, String key) {
        return QueryString.of(request.getQueryString(), uris).removeFirst(key);
    }

    public String removeAll(HttpServletRequest request, List<String> keys) {
        return QueryString.of(request.getQueryString(), uris).removeAll(keys);
    }

    // remove n values of the given key.
    // if n is above number of values, the key is deleted.

    /**
     * Remove N values of the given key. If N exceeds the number of key occurrences, the key is completely removed.
     * If N is 0 or less, no change is made, otherwise all N occurrences are removed.
     *
     * @param key
     * @param n
     * @return
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


//    public String incrementParamValue(String attributeValue, String param) {
//        String query = (String) parse(attributeValue);
//
//    }

}
