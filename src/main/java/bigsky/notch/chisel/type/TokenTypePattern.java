package bigsky.notch.chisel.type;

import bigsky.notch.chisel.Token;
import bigsky.notch.chisel.Tokenizer;
import bigsky.notch.chisel.TokenType;
import bigsky.notch.chisel.TokenizeException;

import java.util.regex.Pattern;

public class TokenTypePattern implements TokenType {
    public final Pattern pattern;

    public TokenTypePattern(Pattern pattern) {
        this.pattern = pattern;
    }

    @Override
    public Token tokenize(Tokenizer t) throws TokenizeException {
        var ss = new Substring(t.source(), t.location().index);
        var matcher = pattern.matcher(ss);
        if (!matcher.find()) return null;
        var match = matcher.toMatchResult();
        if (match.start() != 0) return null;

        var start = t.location();
        var content = match.group();
        t.take(content.length());
        return new Token(start, t.location(), this, content);
    }

    private record Substring(CharSequence seq, int start) implements CharSequence {

        @Override
            public int length() {
                return seq.length() - start;
            }

            @Override
            public char charAt(int index) {
                return seq.charAt(start + index);
            }

            @Override
            public CharSequence subSequence(int start, int end) {
                return seq.subSequence(this.start + start, this.start + end);
            }
        }
}
