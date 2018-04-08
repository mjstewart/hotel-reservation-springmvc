package com.demo.thymeleaf.expression;

import com.demo.thymeleaf.utils.QueryString;
import com.demo.thymeleaf.utils.ThymeleafExpressionParser;
import org.junit.Test;
import org.mockito.Mockito;
import org.thymeleaf.expression.Uris;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class QueryStringHelperTest {

    private ThymeleafExpressionParser mockIdentityParser(String query) {
        ThymeleafExpressionParser parser = Mockito.mock(ThymeleafExpressionParser.class);
        when(parser.parse(Mockito.eq(query))).thenReturn(query);
        return parser;
    }

    @Test
    public void replaceFirst_KeyDoesNotExist_HasNoEffect() {
        String query = "key2=value2&key2=value3&key3=value3&key2=value4";

        ThymeleafExpressionParser parser = mockIdentityParser(query);
        QueryStringHelper helper = new QueryStringHelper(parser, new Uris());

        String result = helper.replaceFirst(query, "missing", "value 999");

        assertThat(result).isEqualTo(query);
        verify(parser, times(1)).parse(query);
    }

    @Test
    public void replaceFirst_KeyExists_FirstOccurrenceReplacedOnly() {
        String query = "key2=value2&key2=value3&key3=value3&key2=value4";

        ThymeleafExpressionParser parser = mockIdentityParser(query);
        QueryStringHelper helper = new QueryStringHelper(parser, new Uris());

        // Also ensure %20 is added to the new value.
        String expected = "key2=value%20999&key2=value3&key3=value3&key2=value4";
        String result = helper.replaceFirst(query, "key2", "value 999");

        assertThat(result).isEqualTo(expected);
        verify(parser, times(1)).parse(query);
    }
}