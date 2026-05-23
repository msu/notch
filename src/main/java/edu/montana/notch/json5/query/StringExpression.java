package edu.montana.notch.json5.query;

import edu.montana.notch.chisel.Token;
import edu.montana.notch.json5.JSON5TokenTypeString;
import edu.montana.notch.json5.JSON5Value;

public class StringExpression extends QueryExpression {
    public final Token value;

    public StringExpression(Token token) {
        super(token.span);
        this.value = token;
    }

    @Override
    public Object query(JSON5Value value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Object evaluate() {
        var sv = ((JSON5TokenTypeString.StringValue) value.data);
        return sv.value();
    }
}
