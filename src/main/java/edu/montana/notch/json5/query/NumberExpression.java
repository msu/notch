package edu.montana.notch.json5.query;

import edu.montana.notch.chisel.Token;
import edu.montana.notch.json5.JSON5TokenTypeNumber;
import edu.montana.notch.json5.JSON5Value;

public class NumberExpression extends QueryExpression {
    public final Token token;

    public NumberExpression(String fileId, Token token) {
        super(fileId, token.span());
        this.token = token;
    }

    @Override
    public Object query(JSON5Value value) {
        return new UnsupportedOperationException();
    }

    @Override
    public Object evaluate() {
        var value = ((JSON5TokenTypeNumber.NumberValue) this.token.data);
        if (value.isInteger()) return (int) (long) value.integerValue();
        return value.decimalValue();
    }
}
