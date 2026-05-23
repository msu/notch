package edu.montana.notch.chisel.type;

<<<<<<<< HEAD:src/main/java/edu/montana/notch/chisel/type/CIdentifierTokenType.java
<<<<<<<< HEAD:src/main/java/edu/montana/notch/chisel/type/TokenTypeIdentifier.java
import edu.montana.notch.chisel.Token;
import edu.montana.notch.chisel.Tokenizer;
import edu.montana.notch.chisel.TokenType;
========
import bigsky.notch.chisel.TokenData;
import bigsky.notch.chisel.Tokenizer;
import bigsky.notch.chisel.TokenType;

public class CIdentifierTokenType implements TokenType {
    public static final CIdentifierTokenType IDENT = new CIdentifierTokenType();
>>>>>>>> 00d0fae (new tokenizer api):src/main/java/edu/montana/notch/chisel/type/CIdentifierTokenType.java
========
import edu.montana.notch.chisel.TokenData;
import edu.montana.notch.chisel.TokenType;
import edu.montana.notch.chisel.Tokenizer;

public class IdentTokenType implements TokenType {
    public static final IdentTokenType IDENT = new IdentTokenType();
>>>>>>>> e3ae36d (new tokenizer api, move namespace, & template updates):src/main/java/edu/montana/notch/chisel/type/IdentTokenType.java

    private IdentTokenType() {
    }

    protected boolean isStartChar(char c) {
        if (Character.isLetter(c)) return true;
        if (c == '_') return true;
        if (c == '$') return true;
        return false;
    }

    protected boolean isChar(char c) {
        if (Character.isLetterOrDigit(c)) return true;
        if (c == '_') return true;
        if (c == '$') return true;
        return false;
    }

    @Override
    public TokenData tokenize(Tokenizer t) {
        if (!isStartChar(t.peek())) return null;

        var content = new StringBuilder();
        do {
            content.append(t.take());
        } while (isChar(t.peek()));

        return new TokenData(content.toString());
    }
}
