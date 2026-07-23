package edu.montana.notch.chisel.type;

import edu.montana.notch.chisel.TokenData;
import edu.montana.notch.chisel.TokenType;
import edu.montana.notch.chisel.TokenizeException;
import edu.montana.notch.chisel.Tokenizer;
import edu.montana.notch.util.Pair;
import edu.montana.notch.util.Text;

import static edu.montana.notch.util.Pair.pair;

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
            pair("~", new LiteralTokenType("~")),
            pair("}", new LiteralTokenType("}")),
            pair("||", new LiteralTokenType("||")),
            pair("|=", new LiteralTokenType("|=")),
            pair("|", new LiteralTokenType("|")),
            pair("{", new LiteralTokenType("{")),
            pair("^=", new LiteralTokenType("^=")),
            pair("^", new LiteralTokenType("^")),
            pair("]", new LiteralTokenType("]")),
            pair("\\", new LiteralTokenType("\\")),
            pair("[", new LiteralTokenType("[")),
            pair("?[", new LiteralTokenType("?[")),
            pair("??", new LiteralTokenType("??")),
            pair("?:", new LiteralTokenType("?:")),
            pair("?", new LiteralTokenType("?")),
            pair(">>>", new LiteralTokenType(">>>")),
            pair(">>=", new LiteralTokenType(">>=")),
            pair(">>", new LiteralTokenType(">>")),
            pair(">=", new LiteralTokenType(">=")),
            pair(">", new LiteralTokenType(">")),
            pair("==", new LiteralTokenType("==")),
            pair("=", new LiteralTokenType("=")),
            pair("<=", new LiteralTokenType("<=")),
            pair("<<=", new LiteralTokenType("<<=")),
            pair("<<", new LiteralTokenType("<<")),
            pair("<", new LiteralTokenType("<")),
            pair(";", new LiteralTokenType(";")),
            pair(":", new LiteralTokenType(":")),
            pair("/=", new LiteralTokenType("/=")),
            pair("/", new LiteralTokenType("/")),
            pair(".", new LiteralTokenType(".")),
            pair("->", new LiteralTokenType("->")),
            pair("-=", new LiteralTokenType("-=")),
            pair("-", new LiteralTokenType("-")),
            pair(",", new LiteralTokenType(",")),
            pair("+=", new LiteralTokenType("+=")),
            pair("+", new LiteralTokenType("+")),
            pair("*=", new LiteralTokenType("*=")),
            pair("*", new LiteralTokenType("*")),
            pair(")", new LiteralTokenType(")")),
            pair("(", new LiteralTokenType("(")),
            pair("&=", new LiteralTokenType("&=")),
            pair("&&", new LiteralTokenType("&&")),
            pair("&", new LiteralTokenType("&")),
            pair("%=", new LiteralTokenType("%=")),
            pair("%", new LiteralTokenType("%")),
            pair("#", new LiteralTokenType("#")),
            pair("!=", new LiteralTokenType("!=")),
            pair("!", new LiteralTokenType("!")),
    };
}
