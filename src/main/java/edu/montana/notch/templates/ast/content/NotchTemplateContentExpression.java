package edu.montana.notch.templates.ast.content;

import edu.montana.notch.expressions.NotchExpression;

public final class NotchTemplateContentExpression extends NotchTemplateContentItem {
    public final NotchExpression expression;

    public NotchTemplateContentExpression(NotchExpression expression) {
        super(expression.start, expression.end);
        this.expression = expression;
    }
}
