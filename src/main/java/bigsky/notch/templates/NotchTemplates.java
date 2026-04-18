package bigsky.notch.templates;

import bigsky.notch.chisel.Tokenizer;

import static bigsky.notch.templates.token.NotchTemplateTokenTypeCommand.COMMAND;
import static bigsky.notch.templates.token.NotchTemplateTokenTypeExpression.EXPRESSION;
import static bigsky.notch.templates.token.NotchTemplateTokenTypeText.TEXT;

public class NotchTemplates {
    private NotchTemplates() {}

    public static final Tokenizer TOKENIZER = new Tokenizer()
                .withTokenType(COMMAND)
                .withTokenType(EXPRESSION)
                .withTokenType(TEXT);
}

