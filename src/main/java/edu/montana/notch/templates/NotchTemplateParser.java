package edu.montana.notch.templates;

import edu.montana.notch.NotchParser;
import edu.montana.notch.chisel.*;
import edu.montana.notch.templates.ast.NotchTemplateContentBlock;
import edu.montana.notch.templates.ast.content.NotchTemplateContentCommand;
import edu.montana.notch.templates.ast.content.NotchTemplateContentExpression;
import edu.montana.notch.templates.ast.content.NotchTemplateContentItem;
import edu.montana.notch.templates.ast.content.NotchTemplateContentText;
import edu.montana.notch.templates.token.NotchTemplateTokenTypeCommand;
import edu.montana.notch.templates.token.NotchTemplateTokenTypeExpression;
import bigsky.notch.chisel.*;

import java.util.ArrayList;
import java.util.Objects;

import static edu.montana.notch.templates.token.NotchTemplateTokenTypeCommand.COMMAND;
import static edu.montana.notch.templates.token.NotchTemplateTokenTypeExpression.EXPRESSION;
import static edu.montana.notch.templates.token.NotchTemplateTokenTypeText.TEXT;
import static edu.montana.notch.util.Text.repr;

public class NotchTemplateParser extends BasicParser {
    private final NotchTemplateRegistry templates;

    private static TokenStream tokenize(String fileId, String content) {
        TokenStream tokens;
        try {
            tokens = NotchTemplates.TOKENIZER.tokenize(fileId, content);
        } catch (TokenizeException e) {
            throw new ParseException("failed to tokenize source", e, fileId, e.span());
        }
        return tokens;
    }

    public NotchTemplateParser(NotchTemplateRegistry templates, String fileId, String content) throws ParseException {
        super(tokenize(fileId, content));
        this.templates = Objects.requireNonNull(templates);
    }

    public NotchTemplateParser(NotchTemplateRegistry templates, TokenStream tokens) {
        super(tokens);
        this.templates = Objects.requireNonNull(templates);
    }

    public NotchTemplateContentText parseText() {
        if (!peek(TEXT)) return null;
        var token = take();
        var item = new NotchTemplateContentText(token);
        return item;
    }

    public NotchTemplateContentCommand parseCommand() {
        if (!peek(COMMAND)) return null;

        var token = take();
        var data = ((NotchTemplateTokenTypeCommand.Data) token.data);
        var commandName = data.commandName().str();
        var command = templates.commands.get(commandName);
        if (command == null) {
            throw new ParseException("unknown command " + repr(commandName), fileId(), new Span(token.start, token.end));
        }

        command = command.newInstance();
        command.fileId = fileId();
        command.start = token.start;
        var parser = new NotchParser(data.notchTokens());
        command.parse(data.commandName(), this, parser);
        command.end = parser.location();

        var item = new NotchTemplateContentCommand(command);
        return item;
    }

    public NotchTemplateContentExpression parseExpression() {
        if (!peek(EXPRESSION)) return null;

        var token = take();
        var data = ((NotchTemplateTokenTypeExpression.Data) token.data);
        var notchParser = new NotchParser(data.notchTokens());
        var expr = notchParser.parseExpression();
        if (expr == null) {
            throw new ParseException("expected expression", fileId(), location());
        }
        notchParser.requireEnd("trailing tokens after expression");

        var item = new NotchTemplateContentExpression(expr);
        return item;
    }

    public NotchTemplateContentBlock parseContentBlock(Class<?>... commandTypes) {
        var items = new ArrayList<NotchTemplateContentItem>();

        NotchTemplateCommand endCommand = null;
        var start = location();
        while (endCommand == null && !atEnd()) {
            var text = parseText();
            if (text != null) {
                items.add(text);
                continue;
            }

            var cmd = parseCommand();
            if (cmd != null) {
                var cmdClazz = cmd.command.getClass();
                for (var ct : commandTypes) {
                    if (ct.isAssignableFrom(cmdClazz)) {
                        endCommand = cmd.command;
                        break;
                    }
                }

                items.add(cmd);
                continue;
            }

            var expr = parseExpression();
            if (expr != null) {
                items.add(expr);
                continue;
            }

            throw new ParseException("unexpected token " + repr(peek().type), fileId(), location());
        }

        if (commandTypes.length > 0 && endCommand == null) {
            throw new ParseException("unterminated content block", fileId(), start);
        }

        return new NotchTemplateContentBlock(items);
    }
}
