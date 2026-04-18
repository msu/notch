package bigsky.notch.templates.ast.content;

import bigsky.notch.expressions.NotchExpression;

public final class NotchTemplateContentExpression extends NotchTemplateContentItem {
    public final NotchExpression expression;

    public NotchTemplateContentExpression(NotchExpression expression) {
        super(expression.start, expression.end);
        this.expression = expression;
    }
}
