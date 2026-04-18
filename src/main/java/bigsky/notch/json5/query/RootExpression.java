package bigsky.notch.json5.query;

import bigsky.notch.chisel.Token;
import bigsky.notch.json5.JSON5Value;

public class RootExpression extends QueryExpression {

    public RootExpression(String fileId, Token token) {
        super(fileId, token.span());
    }

    @Override
    public Object query(JSON5Value value) {
        return value;
    }

    @Override
    public Object evaluate() {
        return null;
    }
}
