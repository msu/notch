package edu.montana.notch.json5.query;

import edu.montana.notch.chisel.Token;
import edu.montana.notch.json5.JSON5Array;
import edu.montana.notch.json5.JSON5Value;

public class IndexExpression extends QueryExpression {
    public final QueryExpression root;
    public final QueryExpression index;

    public IndexExpression(QueryExpression root, QueryExpression index, Token rBracket) {
        super(root.span.through(rBracket));
        this.root = root;
        this.index = index;
    }

    @Override
    public Object query(JSON5Value value) {
        var rootValue = root.query(value);
        if (!(rootValue instanceof JSON5Array arr)) {
            throw new RuntimeException("Index expression requires a JSON5 array");
        }
        var indexValue = (Integer) index.evaluate();
        return arr.get(indexValue);
    }

    @Override
    public Object evaluate() {
        throw new UnsupportedOperationException();
    }
}
