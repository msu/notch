package edu.montana.notch.chisel.type;

import edu.montana.notch.chisel.TokenData;
import edu.montana.notch.chisel.TokenType;
import edu.montana.notch.chisel.TokenizeException;
import edu.montana.notch.chisel.Tokenizer;

import java.util.Set;

public class KeywordTokenType implements TokenType {
    public final TokenType identType;
    public final Set<String> keywords;

    public KeywordTokenType(TokenType identType, Set<String> keywords) {
        this.identType = identType;
        this.keywords = keywords;
    }

    @Override
    public TokenData tokenize(Tokenizer t) throws TokenizeException {
        final var ident = identType.tokenize(t);
        if (ident == null || !keywords.contains(ident.str())) return null;
        return new TokenData(ident.str());
    }
}
