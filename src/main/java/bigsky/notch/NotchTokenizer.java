package bigsky.notch;

import bigsky.notch.token.Keyword;
import bigsky.utils.chisel.Location;
import bigsky.utils.chisel.Tokenizer;
import bigsky.utils.chisel.type.*;

import java.util.Arrays;
import java.util.Comparator;
import java.util.Set;
import java.util.TreeMap;

public class NotchTokenizer extends Tokenizer {
    private static TtPunct[] punct(String... puncts) {
        var out = new TreeMap<String, TtPunct>(Comparator.comparingInt(String::length)
                .reversed()
                .thenComparing((ignored1, ignored2) -> 1));
        for (String punct : puncts) {
            out.put(punct, new TtPunct(punct, punct));
        }
        return out.values().toArray(TtPunct[]::new);
    }


    public static final TtPunct[] PUNCTUATION = punct(
            "=",
            "==",
            "!=",

            // Arithmetic
            "+",
            "-",
            "*",
            "/",
            "%",

            // Compound assignment
            "+=",
            "-=",
            "*=",
            "/=",
            "%=",

            // Relational
            "<",
            ">",
            "<=",
            ">=",

            // Logical
            "&&",
            "||",
            "!",

            // Bitwise
            "&",
            "|",
            "^",
            "~",
            "<<",
            ">>",
            ">>>",
            "&=",
            "|=",
            "^=",
            "<<=",
            ">>=",
            
            // nullwise
            "??",
            "?:",
            "?.",
            "?[",
            "?(",

            // Member access / misc
            ".",
            "->",
            "#",
            "?",
            ":",
            ";",
            ",",

            // Grouping
            "(",
            ")",
            "{",
            "}",
            "[",
            "]"
    );

    public NotchTokenizer(CharSequence src, Location start) {
        super(src, start);
        addTokenType(new TtWhitespace());
        addTokenType(new TtBool());
        addTokenType(new Keyword());
        addTokenType(new TtIdent());
        addTokenType(new TtInt());
        addTokenType(new TtString());
        addTokenType(new TtTerseString());
        for (var punct : PUNCTUATION) {
            addTokenType(punct);
        }
    }

    public NotchTokenizer(CharSequence src) {
        this(src, new Location());
    }
}
