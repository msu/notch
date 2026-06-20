package edu.montana.notch.templates.command;

import edu.montana.notch.NotchParser;
import edu.montana.notch.chisel.Diagnostic;
import edu.montana.notch.chisel.ParseException;
import edu.montana.notch.chisel.Token;
import edu.montana.notch.runtime.Drain;
import edu.montana.notch.templates.NotchTemplateCommand;
import edu.montana.notch.templates.NotchTemplateParser;
import edu.montana.notch.templates.ast.NotchTemplateContentBlock;
import edu.montana.notch.templates.runtime.NotchTemplateRuntime;

import static edu.montana.notch.util.Text.repr;

/// this command is used both as a placeholder for content and as a defining place for it
/// in a base template it can be used to define a spot for content to be filled in
/// like so:
/// (base.html)
/// ```html
/// <head>
///#block title
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
///#content e with
///   <title>Home | MyApp</title>
///#end
///
///#content with
///   <main>...</main>
///#end
///```
public class ContentCommand extends NotchTemplateCommand {
    public ContentCommand() {
        super("content");
        isGlobal = true;
    }

    private Token blockName;
    private Token withToken;
    private NotchTemplateContentBlock content;

    @Override
    public void parseCommand(NotchParser parser) {
        if (!parser.take("ident")) return;
        final var token = parser.lastToken();
        if (token.str().equals("with")) {
            withToken = token;
        } else {
            blockName = token;
            if (parser.takeIdent("with")) {
                withToken = parser.lastToken();
            }
        }
    }

    @Override
    public void parseBody(NotchTemplateParser parser) {
        if (withToken != null) {
            content = parser.parseContentBlock(new EndCommand());
        }
    }

    @Override
    public void preRender(NotchTemplateRuntime runtime) {
        final var mode = runtime.storage(LayoutCommand.MODE);
        final var blocks = runtime.storage(LayoutCommand.BLOCKS);
        final var blockName = blockName();
        if (mode == LayoutCommand.Mode.ContentFile) {
            if (blockName == null) {
                final var diag = new Diagnostic()
                        .highlight(commandToken)
                        .note("this block must have a name");
                throw new ParseException(diag);
            }

            if (withToken == null) {
                final var diag = new Diagnostic()
                        .highlight(commandToken)
                        .note("'with' keyword required in laid-out templates");
                throw new ParseException(diag);
            }

            if (!blocks.containsKey(blockName)) {
                final var diag = new Diagnostic()
                        .highlight(commandToken)
                        .note("no block named %s in the layout template".formatted(repr(blockName)));
                throw new ParseException(diag);
            }

            // it will be null if the layout added it in layout pre-render
            if ("".equals(blocks.get(blockName))) {
                final var diag = new Diagnostic()
                        .highlight(this.blockName)
                        .note("block %s was already defined".formatted(repr(blockName)));
                throw new ParseException(diag);
            }
            // mark this block as already filled in content pre-render
            blocks.put(blockName, "");

        } else if (mode == LayoutCommand.Mode.LayoutFile) {
            if (blocks.containsKey(blockName)) {
                final var diag = new Diagnostic()
                        .highlight(this.blockName)
                        .note("block %s was already defined".formatted(repr(blockName)));
                throw new ParseException(diag);
            }
            blocks.put(blockName, null);
        }
    }

    @Override
    public void render(NotchTemplateRuntime runtime, Drain out) {
        final var mode = runtime.storage(LayoutCommand.MODE);
        final var blocks = runtime.storage(LayoutCommand.BLOCKS);

        if (mode == LayoutCommand.Mode.ContentFile) {
            final var s = new StringBuilder();
            content.render(runtime, new Drain(s));
            blocks.put(blockName.str(), s.toString());
        } else if (mode == LayoutCommand.Mode.LayoutFile) {
            final var blockContent = blocks.get(blockName == null ? null : blockName.str());
            if (blockContent != null) {
                out.append(blockContent);
            } else if (content != null) {
                content.render(runtime, out);
            }
        } else {
            throw new UnsupportedOperationException("unknown mode: " + mode);
        }
    }

    public String blockName() {
        if (blockName == null) return LayoutCommand.BODY_BLOCK_NAME;
        return blockName.str();
    }
}
