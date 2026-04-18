package bigsky.notch.json5.query;

import bigsky.notch.chisel.Token;
import bigsky.notch.json5.JSON5Array;
import bigsky.notch.json5.JSON5Value;

public class IndexExpression extends QueryExpression {
    public final QueryExpression root;
    public final QueryExpression index;

    public IndexExpression(QueryExpression root, QueryExpression index, Token rBracket) {
        super(root.fileId, root.start, rBracket.end);
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
