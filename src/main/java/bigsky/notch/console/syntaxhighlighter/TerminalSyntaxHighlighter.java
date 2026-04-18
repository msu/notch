package bigsky.notch.console.syntaxhighlighter;

import bigsky.notch.console.palettes.NerdFont;
import bigsky.notch.console.token.TokenTypeInteger;
import bigsky.notch.console.token.TokenTypePlain;
import bigsky.notch.console.token.TokenTypeString;
import bigsky.notch.console.token.TokenTypeTerminalKeyword;
import bigsky.notch.chisel.Token;
import bigsky.notch.chisel.TokenType;
import bigsky.notch.chisel.Tokenizer;
import bigsky.notch.chisel.type.TokenTypeBoolean;
import bigsky.notch.chisel.type.TokenTypeIdentifier;
import bigsky.notch.chisel.type.TokenTypeWhitespace;
import org.jline.reader.Highlighter;
import org.jline.reader.LineReader;
import org.jline.utils.AttributedString;
import org.jline.utils.AttributedStringBuilder;
import org.jline.utils.AttributedStyle;

import java.util.Map;

public class TerminalSyntaxHighlighter implements Highlighter {

    private final Map<TokenType, AttributedStyle> styles = Map.of(
            TokenTypeBoolean.BOOL, AttributedStyle.DEFAULT.foregroundRgb(NerdFont.CONSTANT),
            TokenTypeIdentifier.IDENT, AttributedStyle.DEFAULT.foregroundRgb(NerdFont.VARIABLE),
            TokenTypeInteger.NUM, AttributedStyle.DEFAULT.foregroundRgb(NerdFont.NUMBER),
            TokenTypeString.STR, AttributedStyle.DEFAULT.foregroundRgb(NerdFont.STRING),
            TokenTypePlain.PLAIN, AttributedStyle.DEFAULT.foregroundRgb(NerdFont.FOREGROUND),
            TokenTypeWhitespace.WHITESPACE, AttributedStyle.DEFAULT.backgroundRgb(NerdFont.BACKGROUND),
            TokenTypeTerminalKeyword.TERMINAL_KEYWORD, AttributedStyle.DEFAULT.foregroundRgb(NerdFont.TYPE)
    );

    private Tokenizer getTokenizer(String src) {
        return new Tokenizer()
                .withTokenType(TokenTypeString.STR)
                .withTokenType(TokenTypeInteger.NUM)
                .withTokenType(TokenTypeWhitespace.WHITESPACE)
                .withTokenType(TokenTypeTerminalKeyword.TERMINAL_KEYWORD)
                .withTokenType(TokenTypePlain.PLAIN) // must be last
                .create("notch-cli", src);
    }

    @Override
    public AttributedString highlight(LineReader reader, String buffer) {
        Tokenizer tokenizer = getTokenizer(buffer);
        AttributedStringBuilder builder = new AttributedStringBuilder();

        while (!tokenizer.atEnd()) {
            Token token = tokenizer.peekToken();
            AttributedStyle style = styles.get(token.type);
            String substring = buffer.substring(token.start.index, token.end.index);
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
