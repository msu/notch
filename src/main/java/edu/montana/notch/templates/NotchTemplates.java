package edu.montana.notch.templates;

import edu.montana.notch.chisel.Tokenizer;

import static edu.montana.notch.templates.token.NotchTemplateTokenTypeCommand.COMMAND;
import static edu.montana.notch.templates.token.NotchTemplateTokenTypeExpression.EXPRESSION;
import static edu.montana.notch.templates.token.NotchTemplateTokenTypeText.TEXT;

public class NotchTemplates {
    private NotchTemplates() {}

    public static final Tokenizer TOKENIZER = new Tokenizer()
                .withTokenType(COMMAND)
                .withTokenType(EXPRESSION)
                .withTokenType(TEXT);
}

