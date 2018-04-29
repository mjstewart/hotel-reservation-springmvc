package com.demo.persistance.predicates;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.Visitor;

import java.util.function.Supplier;

public class WhereClauseBuilder implements Predicate {

    private final BooleanBuilder delegate = new BooleanBuilder();

    public WhereClauseBuilder and(Predicate right) {
        delegate.and(right);
        return this;
    }

    public <V> WhereClauseBuilder andNullable(V value, Supplier<Predicate> supplier) {
        if (value != null) {
            and(supplier.get());
        }
        return this;
    }

    @Override
    public Predicate not() {
        return delegate.not();
    }

    @Override
    public <R, C> R accept(Visitor<R, C> v, C context) {
        return delegate.accept(v, context);
    }

    @Override
    public Class<? extends Boolean> getType() {
        return delegate.getType();
    }
}
