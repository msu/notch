package bigsky.notch.json5.query;

import bigsky.notch.chisel.Token;
import bigsky.notch.json5.JSON5TokenTypeString;
import bigsky.notch.json5.JSON5Value;

public class StringExpression extends QueryExpression {
    public final Token value;

    public StringExpression(String fileId, Token value) {
        super(fileId, value.span());
        this.value = value;
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
