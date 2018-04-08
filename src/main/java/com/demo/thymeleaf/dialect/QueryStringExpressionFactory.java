package com.demo.thymeleaf.dialect;

import com.demo.thymeleaf.expression.QueryStringHelper;
import com.demo.thymeleaf.utils.ThymeleafExpressionParser;
import org.thymeleaf.context.IExpressionContext;
import org.thymeleaf.expression.IExpressionObjectFactory;
import org.thymeleaf.expression.Uris;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class QueryStringExpressionFactory implements IExpressionObjectFactory {

    private static final String EVALUATION_VARIABLE_NAME = "querystring";

    @Override
    public Set<String> getAllExpressionObjectNames() {
        return Collections.unmodifiableSet(new HashSet<>(Arrays.asList(EVALUATION_VARIABLE_NAME)));
    }

    @Override
    public Object buildObject(IExpressionContext context, String expressionObjectName) {
        if (EVALUATION_VARIABLE_NAME.equals(expressionObjectName)) {
            return new QueryStringHelper(new ThymeleafExpressionParser(context), new Uris());
        }
        return null;
    }

    @Override
    public boolean isCacheable(String expressionObjectName) {
        return EVALUATION_VARIABLE_NAME.equals(expressionObjectName);
    }
}
