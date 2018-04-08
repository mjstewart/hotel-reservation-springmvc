package com.demo.thymeleaf.expression;


import com.demo.thymeleaf.utils.QueryString;
import com.demo.thymeleaf.utils.ThymeleafExpressionParser;
import org.thymeleaf.expression.Uris;

import java.util.Map;

public class QueryStringHelper {

    private final ThymeleafExpressionParser parser;
    private final Uris uris;

    public QueryStringHelper(ThymeleafExpressionParser parser, Uris uris) {
        this.parser = parser;
        this.uris = uris;
    }

    public String replaceFirst(String attributeValue, String key, String value) {
        return QueryString.of(parser.parse(attributeValue), uris).replaceFirst(key, value);
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



//    public String incrementParamValue(String attributeValue, String param) {
//        String query = (String) parse(attributeValue);
//
//    }

}
