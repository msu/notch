package edu.montana.notch.json5.query;

import edu.montana.notch.chisel.Token;
import edu.montana.notch.json5.JSON5Object;
import edu.montana.notch.json5.JSON5TokenTypeIdent;
import edu.montana.notch.json5.JSON5TokenTypeString;
import edu.montana.notch.json5.JSON5Value;

import java.util.Objects;

public class FieldAccessExpression extends QueryExpression {
    public final QueryExpression root;
    public final Token field;

    public FieldAccessExpression(QueryExpression root, Token field) {
        super(root.fileId, root.start, field.end);
        this.root = Objects.requireNonNull(root);
        this.field = Objects.requireNonNull(field);
    }

    @Override
    public Object query(JSON5Value value) {
        var rootValue = root.query(value);
        if (!(rootValue instanceof JSON5Object obj)) {
            throw new RuntimeException("Root expression is not a JSON5Value");
        }

        String prop;
        if (field.data instanceof JSON5TokenTypeString.StringValue sv) {
            prop = sv.value();
        } else if (field.type instanceof JSON5TokenTypeIdent) {
            prop = field.str();
        } else {
            throw new RuntimeException("field was not a string");
        }

        return obj.get(prop);
    }

    @Override
    public Object evaluate() {
        throw new UnsupportedOperationException();
    }
}
