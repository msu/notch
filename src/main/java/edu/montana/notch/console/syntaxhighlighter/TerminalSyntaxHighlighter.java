package edu.montana.notch.console.syntaxhighlighter;

import edu.montana.notch.console.palettes.NerdFont;
import edu.montana.notch.console.token.TokenTypeInteger;
import edu.montana.notch.console.token.TokenTypePlain;
import edu.montana.notch.console.token.TokenTypeString;
import edu.montana.notch.console.token.TokenTypeTerminalKeyword;
import edu.montana.notch.chisel.Token;
import edu.montana.notch.chisel.TokenType;
import edu.montana.notch.chisel.Tokenizer;
import edu.montana.notch.chisel.type.TokenTypeBoolean;
import edu.montana.notch.chisel.type.TokenTypeIdentifier;
import edu.montana.notch.chisel.type.TokenTypeWhitespace;
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
