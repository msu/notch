package edu.montana.notch.templates.command;

import edu.montana.notch.NotchParser;
import edu.montana.notch.chisel.Source;
import edu.montana.notch.chisel.Token;
import edu.montana.notch.runtime.Drain;
import edu.montana.notch.runtime.NotchRuntime;
import edu.montana.notch.templates.NotchTemplateCommand;
import edu.montana.notch.templates.NotchTemplateParser;
import edu.montana.notch.templates.ast.NotchTemplateContentBlock;
import edu.montana.notch.templates.runtime.NotchTemplateRuntime;
import edu.montana.notch.util.BetterList;
import edu.montana.notch.util.BetterMap;
import edu.montana.notch.util.Key;

public class LayoutCommand extends NotchTemplateCommand {
    public static final Key<Mode> MODE = new Key<>("layout.mode");
    public static final Key<BetterMap<String, String>> BLOCKS = new Key<>("layout.blocks");
    public static final String BODY_BLOCK_NAME = "<body>";

    public LayoutCommand() {
        super("layout");
        isGlobal = true;
    }

    Token templateName;
    private NotchTemplateContentBlock layoutContent;
    private NotchTemplateContentBlock bodyContent;

    @Override
    public void parseCommand(NotchParser parser) {
        templateName = parser.expect("string", "expected template name");
        layoutContent = templates.getTemplateContent(templateName.str());
    }

    @Override
    public void parseBody(NotchTemplateParser parser) {
        bodyContent = parser.parseContentBlock();
    }

    private BetterMap<String, String> blocks;
    private NotchTemplateRuntime layoutRuntime;
    private NotchTemplateRuntime contentRuntime;

    @Override
    public void preRender(NotchTemplateRuntime runtime) {
        blocks = new BetterMap<>();

        layoutRuntime = new NotchTemplateRuntime(layoutContent.source(), runtime);
        layoutRuntime.storage(MODE, Mode.LayoutFile);
        layoutRuntime.storage(BLOCKS, blocks);
        layoutContent.preRender(layoutRuntime);

        contentRuntime = new NotchTemplateRuntime(source(), layoutRuntime);
        contentRuntime.storage(MODE, Mode.ContentFile);
        contentRuntime.storage(BLOCKS, blocks);
        bodyContent.preRender(contentRuntime);
    }

    @Override
    public void render(NotchTemplateRuntime runtime, Drain out) {
        final var contentSb = new StringBuilder();
        bodyContent.render(contentRuntime, new Drain(contentSb));
        blocks.put(null, contentSb.toString());

        layoutContent.render(layoutRuntime, out);
    }

    @Override
    public void postRender(NotchTemplateRuntime runtime) {
        bodyContent.postRender(contentRuntime);
        layoutContent.postRender(layoutRuntime);
    }

    public enum Mode {
        LayoutFile,
        ContentFile,
    }
}
