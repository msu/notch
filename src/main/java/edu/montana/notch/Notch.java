package edu.montana.notch;

import edu.montana.notch.chisel.Source;
import edu.montana.notch.chisel.TokenStream;
import edu.montana.notch.chisel.Tokenizer;
import edu.montana.notch.chisel.type.LiteralTokenType;
import edu.montana.notch.console.NotchShell;
import edu.montana.notch.expressions.NotchExpression;
import edu.montana.notch.types.coercions.Coercion;

import java.io.StringWriter;

import static edu.montana.notch.chisel.type.BooleanTokenType.BOOL;
import static edu.montana.notch.chisel.type.CStringTokenType.STR;
import static edu.montana.notch.chisel.type.IdentTokenType.IDENT;
import static edu.montana.notch.chisel.type.IntegerTokenType.INT;
import static edu.montana.notch.chisel.type.WhitespaceTokenType.WHITESPACE;
import static edu.montana.notch.token.NotchTokenTypeKeyword.NOTCH_KEYWORD;
import static edu.montana.notch.token.TerseStringTokenType.TERSE_STRING;

public class Notch {
    private Notch() {
    }

    public static final Tokenizer TOKENIZER = new Tokenizer()
            .withTokenType("_ws", WHITESPACE)
            .withTokenType("bool", BOOL)
            .withTokenType("keyword", NOTCH_KEYWORD)
            .withTokenType("ident", IDENT)
            .withTokenType("int", INT)
            .withTokenType("string", STR)
            .withTokenType("string", TERSE_STRING)
            .withTokenTypes(LiteralTokenType.COMMON);

    public static <T> T eval(String code) {
        return (T) eval(code, Object.class);
    }

    public static <T> T eval(String code, Class<T> targetType) {
        TokenStream tokens = Notch.TOKENIZER.tokenize(new Source("<eval>", code));
        NotchParser notchParser = new NotchParser(tokens);
        NotchExpression notchExpression = notchParser.parseExpression();
        Object result = notchExpression.evaluate();
        if (!targetType.isInstance(result)) {
            Coercion coercer = Coercion.resolve(result.getClass(), targetType);
            if (coercer != null) {
                result = coercer.coerce(result);
            } else {
                throw new IllegalArgumentException("Result of type " + result.getClass() + " could not be coerced to " + targetType);
            }
        }
        return (T) result;
    }

    public static void main(String[] args) {
        NotchShell.start(true);
    }
}
