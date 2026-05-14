package edu.montana.notch.templates.command;

import edu.montana.notch.NotchParser;
import edu.montana.notch.expressions.NotchExpression;
import edu.montana.notch.templates.NotchTemplateCommand;
import edu.montana.notch.templates.NotchTemplateParser;
import edu.montana.notch.templates.ast.NotchTemplateContentBlock;
import edu.montana.notch.templates.runtime.NotchTemplateRuntime;
import edu.montana.notch.chisel.ParseException;
import edu.montana.notch.chisel.Token;

import java.util.ArrayList;
import java.util.List;

public class IfCommand extends NotchTemplateCommand {
    private List<NotchExpression> conditions = new ArrayList<>();
    private List<NotchTemplateContentBlock> conditionalBlocks = new ArrayList<>();
    private NotchTemplateContentBlock elseBlock;

    public IfCommand() {
        super("if");
    }

    @Override
    public void parse(Token commandName, NotchTemplateParser tmplParser, NotchParser commandParser) {
        NotchTemplateContentBlock conditionalBlock;
        NotchExpression condition = commandParser.requireExpression("expected condition after #if");
        commandParser.requireEnd("extra tokens after if condition");
        conditionalBlock = tmplParser.parseContentBlock(EndCommand.class, ElseCommand.class, ElseIfCommand.class);
        addChildContent(conditionalBlock);
        conditions.add(condition);
        conditionalBlocks.add(conditionalBlock);
        var endCmd = conditionalBlock.lastCommand();
        if (endCmd == null) {
            throw new ParseException("unterminated if command, expected #end, #else or #elseif", fileId, commandName.span());
        }
        while (conditionalBlock.lastCommand() instanceof ElseIfCommand elseif) {
            condition = elseif.condition;
            conditionalBlock = tmplParser.parseContentBlock(EndCommand.class, ElseCommand.class, ElseIfCommand.class);
            addChildContent(conditionalBlock);
            conditions.add(condition);
            conditionalBlocks.add(conditionalBlock);
            endCmd = conditionalBlock.lastCommand();
            if (endCmd == null) {
                throw new ParseException("unterminated if command, expected #end, #else or #elseif", fileId, commandName.span());
            }
        }
        if (conditionalBlock.lastCommand() instanceof ElseCommand) {
            elseBlock = tmplParser.parseContentBlock(EndCommand.class);
            addChildContent(elseBlock);
        }
    }

    @Override
    public void render(NotchTemplateRuntime runtime, StringBuilder sb) {
        for (int i = 0; i < conditions.size(); i++) {
            NotchExpression condition = conditions.get(i);
            if (runtime.isTruthy(condition.evaluate(runtime))) {
                NotchTemplateContentBlock sunnyContentBlock = conditionalBlocks.get(i);
                sunnyContentBlock.render(runtime, sb);
                return;
            }
        }
        if (elseBlock != null) {
            elseBlock.render(runtime, sb);
        }
    }

}