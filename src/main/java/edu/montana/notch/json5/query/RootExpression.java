package edu.montana.notch.json5.query;

import edu.montana.notch.chisel.Token;
import edu.montana.notch.json5.JSON5Value;

public class RootExpression extends QueryExpression {

    public RootExpression(Token token) {
        super(token.span);
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
