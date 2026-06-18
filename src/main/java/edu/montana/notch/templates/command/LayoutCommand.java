package edu.montana.notch.templates.command;

import edu.montana.notch.NotchParser;
import edu.montana.notch.chisel.Token;
import edu.montana.notch.runtime.Drain;
import edu.montana.notch.templates.NotchTemplateCommand;
import edu.montana.notch.templates.NotchTemplateParser;
import edu.montana.notch.templates.ast.NotchTemplateContentBlock;
import edu.montana.notch.templates.runtime.NotchTemplateRuntime;
import edu.montana.notch.util.BetterMap;
import edu.montana.notch.util.Key;

public class LayoutCommand extends NotchTemplateCommand {
    public static final Key<Mode> MODE = new Key<>("layout.mode");
    public static final Key<BetterMap<String, String>> BLOCKS = new Key<>("layout.blocks");
    public static final String BODY_BLOCK_NAME = "<body>";

    public LayoutCommand() {
        super("layout");
    }

    Token templateName;
    NotchTemplateContentBlock bodyContent;

    @Override
    public void parseCommand(NotchParser parser) {
        templateName = parser.expect("string", "expected template name");
    }

    @Override
    public void parseBody(NotchTemplateParser parser) {
        bodyContent = parser.parseContentBlock();
    }

    @Override
    public void render(NotchTemplateRuntime runtime, Drain out) {
        final var layoutContent = runtime.templates().getTemplateContent(templateName.str());
        final var contentGlobals = bodyContent.collectGlobals();
        final var layoutGlobals = layoutContent.collectGlobals();

        final var blocks = new BetterMap<String, String>();
        final var layoutRuntime = new NotchTemplateRuntime(layoutContent.source(), runtime);
        layoutRuntime.storage(MODE, Mode.LayoutFile);
        layoutRuntime.storage(BLOCKS, blocks);

        for (final var global : layoutGlobals) {
            global.preRender(layoutRuntime);
        }

        {
            final var contentRuntime = new NotchTemplateRuntime(source(), layoutRuntime);
            contentRuntime.storage(MODE, Mode.ContentFile);
            contentRuntime.storage(BLOCKS, blocks);
            for (final var global : contentGlobals) {
                global.preRender(contentRuntime);
            }

            final var contentSb = new StringBuilder();
            bodyContent.render(contentRuntime, new Drain(contentSb));
            blocks.put(null, contentSb.toString());

            for (final var global : contentGlobals) {
                global.postRender(contentRuntime);
            }
        }

        layoutContent.render(layoutRuntime, out);

        for (final var global : contentGlobals) {
            global.postRender(layoutRuntime);
        }
    }

    public enum Mode {
        LayoutFile,
        ContentFile,
    }
}
