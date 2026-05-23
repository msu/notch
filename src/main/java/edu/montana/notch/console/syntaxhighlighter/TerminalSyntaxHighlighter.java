package edu.montana.notch.console.syntaxhighlighter;

import edu.montana.notch.chisel.Source;
import edu.montana.notch.chisel.type.WhitespaceTokenType;
import edu.montana.notch.console.palettes.NerdFont;
import edu.montana.notch.chisel.Token;
import edu.montana.notch.chisel.Tokenizer;
import edu.montana.notch.console.token.CStringTokenType;
import edu.montana.notch.console.token.IntegerTokenType;
import edu.montana.notch.console.token.PlainTokenType;
import edu.montana.notch.console.token.TerminalKeywordTokenType;
import org.jline.reader.Highlighter;
import org.jline.reader.LineReader;
import org.jline.utils.AttributedString;
import org.jline.utils.AttributedStringBuilder;
import org.jline.utils.AttributedStyle;

import java.util.Map;

public class TerminalSyntaxHighlighter implements Highlighter {

    private final Map<String, AttributedStyle> styles = Map.of(
            "bool", AttributedStyle.DEFAULT.foregroundRgb(NerdFont.CONSTANT),
            "ident", AttributedStyle.DEFAULT.foregroundRgb(NerdFont.VARIABLE),
            "int", AttributedStyle.DEFAULT.foregroundRgb(NerdFont.NUMBER),
            "string", AttributedStyle.DEFAULT.foregroundRgb(NerdFont.STRING),
            "plain", AttributedStyle.DEFAULT.foregroundRgb(NerdFont.FOREGROUND),
            "_ws", AttributedStyle.DEFAULT.backgroundRgb(NerdFont.BACKGROUND),
            "terminal_keyword", AttributedStyle.DEFAULT.foregroundRgb(NerdFont.TYPE)
    );

    private Tokenizer getTokenizer(String src) {
        final var source = new Source("notch-cli", src);
        return new Tokenizer()
                .withTokenType("string", CStringTokenType.STR)
                .withTokenType("int", IntegerTokenType.INT)
                .withTokenType("_ws", WhitespaceTokenType.WHITESPACE)
                .withTokenType("terminal_keyword", TerminalKeywordTokenType.TERMINAL_KEYWORD)
                .withTokenType("plain", PlainTokenType.PLAIN) // must be last
                .create(source);
    }

    @Override
    public AttributedString highlight(LineReader reader, String buffer) {
        Tokenizer tokenizer = getTokenizer(buffer);
        AttributedStringBuilder builder = new AttributedStringBuilder();

        while (!tokenizer.atEnd()) {
            Token token = tokenizer.peekToken();
            AttributedStyle style = styles.get(token.type);
            String substring = buffer.substring(token.start().index, token.end().index);
            builder.styled(style, substring);
            tokenizer.nextToken();
        }
        return builder.toAttributedString();
    }

    @Override
    public void setErrorPattern(java.util.regex.Pattern errorPattern) {}

    @Override
    public void setErrorIndex(int errorIndex) {}
}
