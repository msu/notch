package edu.montana.notch.templates.command;

import edu.montana.notch.NotchParser;
import edu.montana.notch.templates.NotchTemplateCommand;
import edu.montana.notch.templates.NotchTemplateParser;
import edu.montana.notch.templates.ast.NotchTemplateContentBlock;
import edu.montana.notch.templates.layout.LayoutContentItemCommand;
import edu.montana.notch.templates.runtime.RenderException;
import edu.montana.notch.templates.runtime.NotchTemplateRuntime;
import edu.montana.notch.chisel.Token;

/// this command is used both as a placeholder for content and as a defining place for it
/// in a base template it can be used to define a spot for content to be filled in
/// like so:
/// (base.html)
/// ```html
/// <head>
///#content for title default
///   <title>My App</title>
///#end
/// </head>
/// <body>
///#content
/// </body>
///```
///
/// then these things can be filled in later, like so:
/// (index.html)
/// ```
///#layout "base.html"
///
///#content for title with
/// <title>Home | MyApp</title>
///#end
///
///#content with
/// <main>...</main>
///#end
///```
public class ContentCommand extends NotchTemplateCommand {
    public ContentCommand() {
        super("content");
    }

    private Token blockName;
    private NotchTemplateContentBlock defaultContent;
    private NotchTemplateContentBlock blockContent;

    @Override
    public void parse(Token commandName, NotchTemplateParser tmplParser, NotchParser commandParser) {
        if (commandParser.takeKeyword("for")) {
            blockName = commandParser.requireIdent("expected block name after 'for'");
        }

        if (commandParser.takeIdent("default")) {
            end = commandParser.location();
            defaultContent = tmplParser.parseContentBlock(EndCommand.class);
            addChildContent(defaultContent);
        } else if (commandParser.takeIdent("with")) {
            end = commandParser.location();
            blockContent = tmplParser.parseContentBlock(EndCommand.class);
            addChildContent(blockContent);
        } else {
            end = commandParser.location();
        }
        commandParser.requireEnd("expected end of line");
    }

    @Override
    public void render(NotchTemplateRuntime runtime, StringBuilder sb) {
        var blocks = runtime.storage(LayoutCommand.KEY_LAYOUT_CONTENT);
        var mode = runtime.storage(LayoutCommand.KEY_LAYOUT_MODE);

        if (mode == LayoutCommand.Mode.CHILD) {
            blocks.contentBlocks().put(blockName(), new LayoutContentItemCommand(this));
        } else {
            var blockName = blockName();
            var cmd = blocks.contentBlocks().get(blockName());
            if (cmd == null) {
                if (defaultContent == null) {
                    throw new RenderException(start, "no content for block " + blockName);
                } else {
                    defaultContent.render(runtime, sb);
                }
            } else {
                cmd.render(runtime, sb);
            }
        }
    }

    public String blockName() {
        if (blockName == null) return "";
        return blockName.str();
    }

    public NotchTemplateContentBlock blockContent() {
        return blockContent;
    }

    public NotchTemplateContentBlock defaultContent() {
        return defaultContent;
    }
}
