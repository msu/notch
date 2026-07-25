package edu.montana.notch.templates.command;

import edu.montana.notch.NotchParser;
import edu.montana.notch.chisel.Diagnostic;
import edu.montana.notch.chisel.ParseException;
import edu.montana.notch.expressions.NotchExpression;
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
        isGlobal = true;
    }

    private NotchExpression templatePath;
    private NotchTemplateContentBlock bodyContent;

    @Override
    public void parseCommand(NotchParser parser) {
        templatePath = parser.parseStringExpression();
        if (templatePath == null) {
            final var diag = new Diagnostic();
            diag.highlight(parser.peek());
            diag.note("expected a template path (string) here");
            throw new ParseException(diag);
        }
    }

    @Override
    public void parseBody(NotchTemplateParser parser) {
        bodyContent = parser.parseContentBlock();
    }

    private NotchTemplateContentBlock layoutContent;
    private BetterMap<String, String> blocks;
    private NotchTemplateRuntime layoutRuntime;
    private NotchTemplateRuntime contentRuntime;

    @Override
    public void preRender(NotchTemplateRuntime runtime) {
        blocks = new BetterMap<>();

        var pathValue = runtime.evaluate(templatePath);
        var pathStr = runtime.cast(String.class, pathValue, templatePath);
        layoutContent = templates.getTemplateContent(pathStr);

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
