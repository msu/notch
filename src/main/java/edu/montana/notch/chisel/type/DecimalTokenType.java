package edu.montana.notch.chisel.type;

import edu.montana.notch.chisel.Location;
import edu.montana.notch.chisel.TokenData;
import edu.montana.notch.chisel.TokenType;
import edu.montana.notch.chisel.Tokenizer;

/**
 * Tokenizes decimal (floating point) literals. Must be registered before {@link IntegerTokenType}.
 * <p>
 * Supported forms:
 * <ul>
 *     <li>{@code 1.5}, {@code 0.25}</li>
 *     <li>{@code .5} (leading dot, only where a member access couldn't occur)</li>
 *     <li>{@code 1e10}, {@code 1.5e-3}, {@code 2E+4}</li>
 *     <li>{@code 1.5f} / {@code 3F} for {@link Float}, {@code 1.5d} / {@code 3D} for {@link Double}</li>
 * </ul>
 * Plain integers ({@code 42}) and radix-prefixed integers ({@code 0x1f}) are left to {@link IntegerTokenType}.
 * A trailing dot ({@code 1.}) is not a decimal, so that {@code 1.foo()} still parses as member access.
 */
public class DecimalTokenType implements TokenType {
    public static final DecimalTokenType DECIMAL = new DecimalTokenType();

    protected DecimalTokenType() {
    }

    @Override
    public TokenData tokenize(Tokenizer t) {
        var content = new StringBuilder();
        boolean isDecimal = false;

        if (t.peek() == '.') {
            if (!canStartWithDot(t)) return null;
            t.take();
            if (!takeDigits(t, content.append("0."))) return null;
            isDecimal = true;
        } else {
            if (t.peek("0x") || t.peek("0b") || t.peek("0o")) return null;
            if (!takeDigits(t, content)) return null;

            Location beforeDot = t.location();
            if (t.take('.')) {
                content.append('.');
                if (takeDigits(t, content)) {
                    isDecimal = true;
                } else {
                    // `1.foo` -- not a decimal, leave the dot for member access
                    t.location(beforeDot);
                    content.setLength(content.length() - 1);
                }
            }
        }

        Location beforeExponent = t.location();
        if (t.take('e', 'E')) {
            var exponent = new StringBuilder("e");
            if (t.peek('+', '-')) exponent.append(t.take());
            if (takeDigits(t, exponent)) {
                content.append(exponent);
                isDecimal = true;
            } else {
                t.location(beforeExponent);
            }
        }

        boolean isFloat = false;
        if (t.take('f', 'F')) {
            isFloat = true;
            isDecimal = true;
        } else if (t.take('d', 'D')) {
            isDecimal = true;
        }

        if (!isDecimal || isIdentPart(t.peek())) return null;

        String literal = content.toString();
        return new TokenData(isFloat ? (Object) Float.parseFloat(literal) : (Object) Double.parseDouble(literal));
    }

    private static boolean takeDigits(Tokenizer t, StringBuilder out) {
        int len = out.length();
        while (IntegerTokenType.isDigit(t.peek(), 10)) {
            out.append(t.take());
        }
        return out.length() > len;
    }

    private static boolean isIdentPart(char c) {
        return Character.isLetterOrDigit(c) || c == '_';
    }

    // `.5` is only a decimal when the dot can't be a member access, e.g. not `foo.5` or `(x).5`
    private static boolean canStartWithDot(Tokenizer t) {
        int index = t.location().index;
        if (index == 0) return true;
        char prev = t.content().charAt(index - 1);
        return !(isIdentPart(prev) || prev == ')' || prev == ']' || prev == '}' || prev == '.' || prev == '?' || prev == '"' || prev == '\'');
    }
}
