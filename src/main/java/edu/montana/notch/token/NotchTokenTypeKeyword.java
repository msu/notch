package edu.montana.notch.token;

import edu.montana.notch.chisel.TokenData;
import edu.montana.notch.chisel.TokenType;
import edu.montana.notch.chisel.TokenizeException;
import edu.montana.notch.chisel.Tokenizer;
import edu.montana.notch.util.BetterSet;

import java.util.Arrays;
import java.util.Set;

import static edu.montana.notch.chisel.type.IdentTokenType.IDENT;

public class NotchTokenTypeKeyword implements TokenType {
    public static final NotchTokenTypeKeyword NOTCH_KEYWORD = new NotchTokenTypeKeyword();

    public final BetterSet<String> keywords = new BetterSet<>();

    private NotchTokenTypeKeyword(String... keywords) {
        this.keywords.addAll(Set.of(
                "if", "for", "else", "end", "in", "is", "not", "null", "as",
                "repeat", "while", "until", "times", "break", "continue",
                "print",
                "function", "return", "class", "new", "this", "field",
                "throw", "try", "catch", "rethrow", "recover", "then"
        ));
        this.keywords.addAll(Arrays.asList(keywords));
    }

    @Override
    public TokenData tokenize(Tokenizer t) throws TokenizeException {
        var ident = IDENT.tokenize(t);
        if (ident == null) return null;
        final String kw = ident.str();
        if (keywords.contains(kw)) {
            return new TokenData(kw);
        } else {
            return null;
        }
    }
}
