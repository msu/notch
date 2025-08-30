package bigsky.notch;

import bigsky.utils.chisel.Tokenizer;
import bigsky.utils.chisel.type.TokenTypePunct;

import static bigsky.notch.token.NotchTokenTypeKeyword.NOTCH_KEYWORD;
import static bigsky.notch.token.TokenTypeTerseString.TERSE_STRING;
import static bigsky.utils.chisel.type.TokenTypeBoolean.BOOL;
import static bigsky.utils.chisel.type.TokenTypeIdentifier.IDENT;
import static bigsky.utils.chisel.type.TokenTypeInteger.INT;
import static bigsky.utils.chisel.type.TokenTypeString.STR;
import static bigsky.utils.chisel.type.TokenTypeWhitespace.WHITESPACE;

public class Notch {
    private Notch() {
    }

    public static Tokenizer tokenizer(CharSequence source) {
        var out = new Tokenizer(source)
                .with(WHITESPACE)
                .with(BOOL)
                .with(NOTCH_KEYWORD)
                .with(IDENT)
                .with(INT)
                .with(STR)
                .with(TERSE_STRING)
                .with(TokenTypePunct.common());
        return out;
    }
}
