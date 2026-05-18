package bigsky.notch.chisel.type;

import bigsky.notch.chisel.TokenData;
import bigsky.notch.chisel.TokenType;
import bigsky.notch.chisel.TokenizeException;
import bigsky.notch.chisel.Tokenizer;
import bigsky.notch.util.Pair;
import bigsky.notch.util.Text;

import static bigsky.notch.util.Pair.pair;

public class LiteralTokenType implements TokenType {
    final String lex;

    public LiteralTokenType(String lex) {
        this.lex = lex;
    }

    @Override
    public TokenData tokenize(Tokenizer t) throws TokenizeException {
        if (!t.take(lex)) return null;
        return new TokenData(lex);
    }

    @Override
    public String toString() {
        return "Literal(" + Text.repr(lex) + ")";
    }

    public static Pair<String, TokenType>[] COMMON = new Pair[]{
            pair("=", new LiteralTokenType("=")),
            pair("==", new LiteralTokenType("==")),
            pair("!=", new LiteralTokenType("!=")),

            // Arithmetic
            pair("+", new LiteralTokenType("+")),
            pair("-", new LiteralTokenType("-")),
            pair("*", new LiteralTokenType("*")),
            pair("/", new LiteralTokenType("/")),
            pair("%", new LiteralTokenType("%")),

            // Compound assignment
            pair("+=", new LiteralTokenType("+=")),
            pair("-=", new LiteralTokenType("-=")),
            pair("*=", new LiteralTokenType("*=")),
            pair("/=", new LiteralTokenType("/=")),
            pair("%=", new LiteralTokenType("%=")),

            // Relational
            pair("<", new LiteralTokenType("<")),
            pair(">", new LiteralTokenType(">")),
            pair("<=", new LiteralTokenType("<=")),
            pair(">=", new LiteralTokenType(">=")),

            // Logical
            pair("&&", new LiteralTokenType("&&")),
            pair("||", new LiteralTokenType("||")),
            pair("!", new LiteralTokenType("!")),

            // Bitwise
            pair("&", new LiteralTokenType("&")),
            pair("|", new LiteralTokenType("|")),
            pair("^", new LiteralTokenType("^")),
            pair("~", new LiteralTokenType("~")),
            pair("<<", new LiteralTokenType("<<")),
            pair(">>", new LiteralTokenType(">>")),
            pair(">>>", new LiteralTokenType(">>>")),
            pair("&=", new LiteralTokenType("&=")),
            pair("|=", new LiteralTokenType("|=")),
            pair("^=", new LiteralTokenType("^=")),
            pair("<<=", new LiteralTokenType("<<=")),
            pair(">>=", new LiteralTokenType(">>=")),

            // nullwise
            pair("??", new LiteralTokenType("??")),
            pair("?:", new LiteralTokenType("?:")),
            pair("?.", new LiteralTokenType("?.")),
            pair("?[", new LiteralTokenType("?[")),
            pair("?(", new LiteralTokenType("?(")),

            // Member access / misc
            pair(".", new LiteralTokenType(".")),
            pair("\\", new LiteralTokenType("\\")),
            pair("->", new LiteralTokenType("->")),
            pair("#", new LiteralTokenType("#")),
            pair("?", new LiteralTokenType("?")),
            pair(":", new LiteralTokenType(":")),
            pair(";", new LiteralTokenType(";")),
            pair(",", new LiteralTokenType(",")),

            // Grouping
            pair("(", new LiteralTokenType("(")),
            pair(")", new LiteralTokenType(")")),
            pair("{", new LiteralTokenType("{")),
            pair("}", new LiteralTokenType("}")),
            pair("[", new LiteralTokenType("[")),
            pair("]", new LiteralTokenType("]"))
    };
}
