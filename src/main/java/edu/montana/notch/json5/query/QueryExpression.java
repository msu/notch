package edu.montana.notch.json5.query;

import edu.montana.notch.chisel.Span;
import edu.montana.notch.chisel.Spanned;
import edu.montana.notch.json5.JSON5Value;

public abstract class QueryExpression implements Spanned {
    static final Object UNDEFINED = new Object() {
        @Override
        public String toString() {
            return "<UNDEFINED:JSON5>";
        }
    };

    public final Span span;

    public QueryExpression(Span span) {
        this.span = span;
    }

    @Override
    public final Span span() {
        return span;
    }

    public abstract Object query(JSON5Value value);

    public abstract Object evaluate();
}
