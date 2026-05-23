package edu.montana.notch.json5;

import edu.montana.notch.chisel.*;
import edu.montana.notch.util.Text;

/**
 * JSON5 Number Tokenizer
 * <p>
 * Supports:
 * - Decimal numbers: 123, 123.456, .456, 123e10, 123e-10
 * - Hexadecimal numbers: 0xDECAF, 0xC0FFEE
 * - Special IEEE 754 values: Infinity, -Infinity, NaN
 * - Optional leading + or - sign on all numeric literals
 */
public class JSON5TokenTypeNumber implements TokenType {
    public static final JSON5TokenTypeNumber JSON5_NUMBER = new JSON5TokenTypeNumber();

    private JSON5TokenTypeNumber() {
    }

    public record NumberValue(String repr, Double decimalValue, Long integerValue) {
        public static NumberValue of(String repr, double value) {
            return new NumberValue(repr, value, null);
        }

        public static NumberValue of(String repr, long integerValue) {
            return new NumberValue(repr, null, integerValue);
        }

        public boolean isInteger() {
            return integerValue != null;
        }

        @Override
        public Double decimalValue() {
            if (decimalValue != null) {
                return decimalValue;
            }
            return (double) integerValue;
        }

        @Override
        public Long integerValue() {
            if (integerValue != null) {
                return integerValue;
            }
            return (long) (double) decimalValue;
        }

        public boolean isNaN() {
            return Double.isNaN(decimalValue());
        }

        public boolean isInfinity() {
            return Double.isInfinite(decimalValue());
        }

        public boolean isNegative() {
            return decimalValue() < 0 || (isNaN() && repr.startsWith("-"));
        }
    }

    @Override
    public TokenData tokenize(Tokenizer t) throws TokenizeException {
        var start = t.location();

        boolean negative = false;
        if (!t.take('+')) negative = t.take('-');

        if (t.take("Infinity")) {
            String repr = t.lex(start, t.location());
            double value = negative ? Double.NEGATIVE_INFINITY : Double.POSITIVE_INFINITY;
            return new TokenData(NumberValue.of(repr, value));
        }

        if (t.take("NaN")) {
            String repr = t.lex(start, t.location());
            return new TokenData(NumberValue.of(repr, Double.NaN));
        }

        if (t.take("0x") || t.take("0X")) {
            return parseHex(t, start, negative);
        }

        return parseDecimal(t, start);
    }

    private TokenData parseHex(Tokenizer t, Location start, boolean negative) throws TokenizeException {
        if (t.atEnd() || !Text.isHexDigit(t.peek())) {
            final var diag = new Diagnostic();
            diag.highlight(new Span(t.source(), start, t.location()));
            diag.note("expected hex digit after 0x");
            throw new TokenizeException(diag);
        }

        while (!t.atEnd() && Text.isHexDigit(t.peek())) {
            t.take();
        }

        String repr = t.lex(start, t.location());

        int hexStart = start.index + (negative ? 3 : 2);  // Skip +/- and 0x
        String hexDigits = t.source().content.subSequence(hexStart, t.location().index).toString();

        try {
            long longValue = Long.parseLong(hexDigits, 16);
            long value = negative ? -longValue : longValue;
            return new TokenData(NumberValue.of(repr, value));
        } catch (NumberFormatException e) {
            final var diag = new Diagnostic();
            diag.highlight(new Span(t.source(), start, t.location()));
            diag.note("invalid hex number: " + hexDigits);
            throw new TokenizeException(diag);
        }
    }

    private TokenData parseDecimal(Tokenizer t, Location start) {
        boolean hasIntegerPart = false;
        boolean hasFractionPart = false;

        // Integer part (optional if fraction follows)
        if (!t.atEnd() && Text.isDigit(t.peek())) {
            hasIntegerPart = true;
            while (!t.atEnd() && Text.isDigit(t.peek())) {
                t.take();
            }
        }

        boolean isDecimal = false;
        if (!t.atEnd() && t.take('.')) {
            isDecimal = true;

            if (!t.atEnd() && Text.isDigit(t.peek())) {
                hasFractionPart = true;
                while (!t.atEnd() && Text.isDigit(t.peek())) {
                    t.take();
                }
            }
            // Note: "123." is valid even without digits after dot
        }

        if (!hasIntegerPart && !hasFractionPart) {
            return null;
        }

        // Exponent part (optional): e or E, optional sign, then digits
        if (!t.atEnd() && t.take('e', 'E')) {
            isDecimal = true;
            t.take('+', '-'); // optional sign

            // Must have at least one digit after exponent marker
            if (t.atEnd() || !Text.isDigit(t.peek())) {
                final var diag = new Diagnostic();
                diag.highlight(new Span(t.source(), start, t.location()));
                diag.note("expected digit in exponent");
                throw new TokenizeException(diag);
            }

            while (!t.atEnd() && Text.isDigit(t.peek())) {
                t.take();
            }
        }

        // Parse the complete number
        String repr = t.lex(start, t.location());

        try {
            NumberValue value;
            if (isDecimal) {
                double val = Double.parseDouble(repr);
                value = NumberValue.of(repr, val);
            } else {
                long val = Long.parseLong(repr);
                value = NumberValue.of(repr, val);
            }
            return new TokenData(value);
        } catch (NumberFormatException e) {
            final var diag = new Diagnostic();
            diag.highlight(new Span(t.source(), start, t.location()));
            diag.note("invalid number: " + repr);
            throw new TokenizeException(diag);
        }
    }
}
