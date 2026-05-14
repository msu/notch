package edu.montana.notch.json5.query;

import edu.montana.notch.chisel.Location;
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

    public final String fileId;
    public final Location start, end;

    public QueryExpression(String fileId, Location start, Location end) {
        this.fileId = fileId;
        this.start = start;
        this.end = end;
    }

    public QueryExpression(String fileId, Span span) {
        this(fileId, span.start(), span.end());
    }

    @Override
    public final Span span() {
        return new Span(start, end);
    }

    public abstract Object query(JSON5Value value);
    public abstract Object evaluate();
}
