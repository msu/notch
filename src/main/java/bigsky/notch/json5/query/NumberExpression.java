package bigsky.notch.json5.query;

import bigsky.notch.chisel.Token;
import bigsky.notch.json5.JSON5TokenTypeNumber;
import bigsky.notch.json5.JSON5Value;

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
