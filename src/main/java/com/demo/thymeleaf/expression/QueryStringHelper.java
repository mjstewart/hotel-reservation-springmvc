package com.demo.thymeleaf.expression;


import com.demo.thymeleaf.utils.QueryString;
import com.demo.thymeleaf.utils.ThymeleafExpressionParser;
import org.thymeleaf.expression.Uris;

import java.util.Map;

public class QueryStringHelper {

    // Delete this, thymeleaf already resolves before passing into our methods.
    private final ThymeleafExpressionParser parser;
    private final Uris uris;

    public QueryStringHelper(ThymeleafExpressionParser parser, Uris uris) {
        this.parser = parser;
        this.uris = uris;
    }

    /**
     * Given query string
     *
     * <pre>
     *     "region=AU&suburb=west&postcode=494849"
     * </pre>
     *
     * <p>Thymeleaf usage
     *
     * <pre>
     *     th:with="newQueryString=${#querystring.replaceFirst(#request.getQueryString(), 'region', 'Australia')}
     * </pre>
     *
     * <p>Result
     *
     * <pre>
     *     newQueryString = "region=Australia&suburb=west&postcode=494849"
     * </pre>
     *
     * @param attributeValue The resolved query string.
     * @param key The target key to replace the value for.
     * @param value The replacement value.
     * @return The new query string.
     */
    public String replaceFirst(String attributeValue, String key, String value) {
        return QueryString.of(attributeValue, uris).replaceFirst(key, value);
    }

    public String replaceNth(String attributeValue, Map<Object, Map<Object, Object>> stateChangeInstructions) {
        return QueryString.of(attributeValue, uris).replaceNth(stateChangeInstructions);
    }

//    public String replaceAll(String attributeValue, String key, List<String> values) {
//        return QueryString.of(parser.parse(attributeValue), uris).replaceAll(key, values);
//    }

    public String rplace(Map map) {
        System.out.println("rplace");
//        Map map = parser.parseSpel(Map.class, attributeValue);

        System.out.println(map);
        Map sort = (Map) map.get("sort");

        return "HELLO :)";
    }


    /*
       replaceAll - just have corresponding list indexes be applied rather than explicitly stating the indexes.
                  - could just internally call replaceNth
       removeAll
       removeNth
       AddMany
       AddOne

       // spring mvc convenience helpers.
       incrementPage
       decrementPage

       something to toggle sort order for a key. if desc, change to asc and vice versa.

     */


//    public String incrementParamValue(String attributeValue, String param) {
//        String query = (String) parse(attributeValue);
//
//    }

}
