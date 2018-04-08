package com.demo.thymeleaf.utils;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.expression.Expression;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.thymeleaf.IEngineConfiguration;
import org.thymeleaf.context.IExpressionContext;
import org.thymeleaf.standard.expression.IStandardExpression;
import org.thymeleaf.standard.expression.IStandardExpressionParser;
import org.thymeleaf.standard.expression.StandardExpressions;

import java.lang.reflect.ParameterizedType;
import java.util.List;

public class ThymeleafExpressionParser {
    private final IExpressionContext context;

    public ThymeleafExpressionParser(IExpressionContext context) {
        this.context = context;
    }

    /**
     * This parser accepts an unresolved thymeleaf expression such as {@code ${#request.getQueryString()})}} and
     * resolves it.
     *
     * @param attributeValue The attribute value to parse.
     * @return The parsed value cast into a String.
     */
    public String parse(String attributeValue) {
        // https://www.thymeleaf.org/doc/tutorials/3.0/extendingthymeleaf.html
        IEngineConfiguration configuration = context.getConfiguration();
        IStandardExpressionParser parser = StandardExpressions.getExpressionParser(configuration);
        IStandardExpression expression = parser.parseExpression(context, attributeValue);

        return (String) expression.execute(context);
    }

    public <T> T parseSpel(Class<T> clazz, String spelExpression) {
        SpelExpressionParser parser = new SpelExpressionParser();
        return parser.parseExpression(spelExpression).getValue(clazz);
    }
}
