package edu.montana.notch.templates.command;

import edu.montana.notch.NotchParser;
import edu.montana.notch.runtime.Drain;
import edu.montana.notch.templates.NotchTemplateCommand;
import edu.montana.notch.templates.NotchTemplateParser;
import edu.montana.notch.templates.ast.NotchTemplateContentBlock;
import edu.montana.notch.templates.runtime.NotchTemplateRuntime;

/// # RawCommand
/// Preserves inner content, ignoring sub-commands & expressions
///
/// ```
/// #raw
/// this will be perserved, no ${expressions} will be substituted here
/// #for x in [1, 2, 3]
///   this will do nothing ${x}
/// #end
/// #endraw
/// ```
public class RawCommand extends NotchTemplateCommand {

    public RawCommand() {
        super("raw");
    }

    private NotchTemplateContentBlock body;

    @Override
    public void parseCommand(NotchParser parser) {}

    @Override
    public void parseBody(NotchTemplateParser parser) {
        body = parser.parseSterileContentBlock(new EndCommand("endraw"));
        addChildContent(body);
    }

    @Override
    public void render(NotchTemplateRuntime runtime, Drain out) {
        body.render(runtime, out);
    }
}
