package com.demo.thymeleaf.dialect;

import org.thymeleaf.dialect.AbstractDialect;
import org.thymeleaf.dialect.IExpressionObjectDialect;
import org.thymeleaf.expression.IExpressionObjectFactory;

public class QueryStringDialect extends AbstractDialect implements IExpressionObjectDialect {

    public QueryStringDialect() {
        super("urlAssist");
    }

    @Override
    public IExpressionObjectFactory getExpressionObjectFactory() {
        return new QueryStringExpressionFactory();
    }
}
