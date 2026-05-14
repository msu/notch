package edu.montana.notch.chisel.type;

import edu.montana.notch.chisel.Token;
import edu.montana.notch.chisel.TokenType;
import edu.montana.notch.chisel.Tokenizer;

import java.util.Comparator;
import java.util.TreeMap;

public enum TokenTypePunct implements TokenType {
    EQ("="),
    EQ2("=="),
    BANG_EQ("!="),

    // Arithmetic
    PLUS("+"),
    DASH("-"),
    STAR("*"),
    SLASH("/"),
    PERCENT("%"),

    // Compound assignment
    PLUS_EQ("+="),
    DASH_EQ("-="),
    STAR_EQ("*="),
    SLASH_EQ("/="),
    PERCENT_EQ("%="),

    // Relational
    LEFT_ARROW("<"),
    RIGHT_ARROW(">"),
    LEFT_ARROW_EQ("<="),
    RIGHT_ARROW_EQ(">="),

    // Logical
    AMPERSAND2("&&"),
    BAR2("||"),
    BANG("!"),

    // Bitwise
    AMPERSAND("&"),
    BAR("|"),
    CARET("^"),
    TILDE("~"),
    LEFT_ARROW2("<<"),
    RIGHT_ARROW2(">>"),
    RIGHT_ARROW3(">>>"),
    AMPERSAND_EQ("&="),
    BAR_EQ("|="),
    CARET_EQ("^="),
    LEFT_ARROW2_EQ("<<="),
    RIGHT_ARROW2_EQ(">>="),

    // nullwise
    QUESTION2("??"),
    QUESTION_COLON("?:"),
    QUESTION_DOT("?."),
    QUESTION_LBRACKET("?["),
    QUESTION_LPAREN("?("),

    // Member access / misc
    DOT("."),
    BACKSLASH("\\"),
    DASH_RIGHT_ARROW("->"),
    HASH("#"),
    QUESTION("?"),
    COLON(":"),
    SEMICOLON(";"),
    COMMA(","),

    // Grouping
    LPAREN("("),
    RPAREN(")"),
    LBRACE("{"),
    RBRACE("}"),
    LBRACKET("["),
    RBRACKET("]");

    public final String lex;

    TokenTypePunct(String lex) {
        this.lex = lex;
    }

    @Override
    public Token tokenize(Tokenizer t) {
        var start = t.location();
        if (!t.take(lex)) {
            return null;
        }
        return new Token(start, t.location(), this, lex);
    }

    private static TokenTypePunct[] common;

    public static TokenTypePunct[] common() {
        if (common == null) {
            var map = new TreeMap<String, TokenTypePunct>(Comparator.comparing(String::length)
                    .thenComparing((a, b) -> 1)
                    .reversed());
            for (var item : TokenTypePunct.values()) {
                map.put(item.lex, item);
            }
            common = new TokenTypePunct[map.size()];
            int i = 0;
            for (TokenTypePunct value : map.values()) {
                common[i++] = value;
            }
        }
        return common;
    }
}
