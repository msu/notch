package bigsky.notch.token;

import bigsky.utils.chisel.Token;
import bigsky.utils.chisel.TokenType;
import bigsky.utils.chisel.TokenizeException;
import bigsky.utils.chisel.Tokenizer;
import bigsky.utils.chisel.type.TtIdent;

import java.util.Set;

public class Keyword implements TokenType {
    public static final Set<String> KEYWORDS = Set.of("if", "for", "else", "end");

    @Override
    public String label() {
        return "keyword";
    }

    @Override
    public Token tokenize(Tokenizer t) throws TokenizeException {
        var ident = new TtIdent().tokenize(t);
        if (ident == null) return null;
        if (KEYWORDS.contains(ident.str())) {
            return ident.withType(this);
        } else {
            return null;
        }
    }
}
