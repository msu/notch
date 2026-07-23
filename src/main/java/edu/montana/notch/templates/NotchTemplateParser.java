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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Collectors;

import static edu.montana.notch.util.Text.repr;

public class NotchTemplateParser extends BasicParser {
    private final NotchTemplates templates;

    private static TokenStream tokenize(Source source) {
        TokenStream tokens = NotchTemplates.TOKENIZER.tokenize(source);
        return tokens;
    }

    public NotchTemplateParser(NotchTemplates templates, Source source) throws ParseException {
        super(tokenize(source));
        this.templates = Objects.requireNonNull(templates);
    }

    public NotchTemplateParser(NotchTemplates templates, TokenStream tokens) {
        super(tokens);
        this.templates = Objects.requireNonNull(templates);
    }

    public NotchTemplateContentText parseText() {
        if (!peek("text")) return null;
        var token = take();
        var item = new NotchTemplateContentText(token);
        return item;
    }

    public NotchTemplateContentCommand parseCommand(NotchTemplateCommand... extraCommands) {
        if (!peek("command")) return null;

        var token = take();
        var data = ((NotchTemplateTokenTypeCommand.Data) token.data);
        var commandName = data.commandName().str();

        NotchTemplateCommand command = null;
        for (final var cmd : extraCommands) {
            if (cmd.commandName.equals(commandName)) {
                command = cmd;
                break;
            }
        }

        if (command == null) {
            command = templates.commands.get(commandName);
        }

        if (command == null) {
            final var diag = new Diagnostic().note("unknown command %s".formatted(repr(commandName))).highlight(data.commandName());
            throw new ParseException(diag);
        }

        command = command.newInstance();
        command.commandToken = data.commandName();
        command.span = data.commandName().span;
        command.templates = templates;

        var commandParser = new NotchParser(data.notchTokens());
        command.parseCommand(commandParser);
        if (!commandParser.atEnd()) {
            final var diag = new Diagnostic();
            diag.note("unexpected extra tokens here...");
            diag.highlight(commandParser.restSpan());
            throw new ParseException(diag);
        }

        command.parseBody(this);
        command.span = token.span.through(lastToken().end());

        var item = new NotchTemplateContentCommand(command);
        return item;
    }

    public NotchTemplateContentExpression parseExpression() {
        if (!peek("expression")) return null;

        var token = take();
        var data = ((NotchTemplateTokenTypeExpression.Data) token.data);
        var notchParser = new NotchParser(data.notchTokens());
        var expr = notchParser.parseExpression();
        if (expr == null) {
            final var diag = new Diagnostic().note("expected an expression here").highlight(currentToken());
            throw new ParseException(diag);
        }
        notchParser.requireEnd("trailing tokens after expression");

        var item = new NotchTemplateContentExpression(expr);
        return item;
    }

    public NotchTemplateContentBlock parseSterileContentBlock(NotchTemplateCommand... terminalCommands) {
        final var start = currentToken().span;
        var items = new ArrayList<NotchTemplateContentItem>();
        NotchTemplateCommand endCommand = null;

        while (!atEnd()) {
            var text = parseText();
            if (text != null) {
                items.add(text);
                continue;
            }

            if (peek("command")) {
                final var token = peek();
                var data = (NotchTemplateTokenTypeCommand.Data) token.data;

                boolean isTerminal = false;
                for (final var cmd : terminalCommands) {
                    if (data.commandName().str().equals(cmd.commandName)) {
                        isTerminal = true;
                        break;
                    }
                }

                // we convert non-terminal commands into text
                if (isTerminal) {
                    final var cmd = parseCommand(terminalCommands);
                    assert cmd != null;
                    endCommand = cmd.command;
                    break;
                }

                take();
                text = new NotchTemplateContentText(token.span);
                items.add(text);
                continue;
            }

            if (peek("expression")) {
                final var token = take();
                text = new NotchTemplateContentText(token.span);
                items.add(text);
                continue;
            }

            final var diag = new Diagnostic().note("unexpected token %s".formatted(repr(currentToken().type))).highlight(currentToken());
            throw new ParseException(diag);
        }

        if (terminalCommands.length > 0 && endCommand == null) {
            final var names = Arrays.stream(terminalCommands)
                    .map(c -> "#%s".formatted(c.commandName))
                    .collect(Collectors.joining(", "));
            final var diag = new Diagnostic()
                    .note("unterminated block content, expected %s".formatted(names))
                    .highlight(currentToken());
            throw new ParseException(diag);
        }

        return new NotchTemplateContentBlock(start.through(currentToken()), items, endCommand);
    }
    public NotchTemplateContentBlock parseContentBlock(NotchTemplateCommand... terminalCommands) {
        final var start = currentToken().span;
        var items = new ArrayList<NotchTemplateContentItem>();
        NotchTemplateCommand endCommand = null;

        outer:
        while (!atEnd()) {
            var text = parseText();
            if (text != null) {
                items.add(text);
                continue;
            }

            // TODO: terminal commands
            var cmd = parseCommand(terminalCommands);
            if (cmd != null) {
                var cmdClazz = cmd.command.getClass();
                for (var ct : terminalCommands) {
                    if (ct.getClass().isAssignableFrom(cmdClazz)) {
                        endCommand = cmd.command;
                        break outer;
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

            final var diag = new Diagnostic().note("unexpected token %s".formatted(repr(currentToken().type))).highlight(currentToken());
            throw new ParseException(diag);
        }

        if (terminalCommands.length > 0 && endCommand == null) {
            final var names = Arrays.stream(terminalCommands)
                    .map(c -> "#%s".formatted(c.commandName))
                    .collect(Collectors.joining(", "));
            final var diag = new Diagnostic()
                    .note("unterminated block content, expected %s".formatted(names))
                    .highlight(currentToken());
            throw new ParseException(diag);
        }

        return new NotchTemplateContentBlock(start.through(currentToken()), items, endCommand);
    }
}
