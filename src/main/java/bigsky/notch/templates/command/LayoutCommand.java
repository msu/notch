package bigsky.notch.templates.command;

import bigsky.notch.NotchParser;
import bigsky.notch.templates.NotchTemplateCommand;
import bigsky.notch.templates.NotchTemplateParser;
import bigsky.notch.templates.ast.NotchTemplateContentBlock;
import bigsky.notch.templates.layout.LayoutContentItem;
import bigsky.notch.templates.layout.LayoutContentItemText;
import bigsky.notch.templates.runtime.RenderException;
import bigsky.notch.templates.runtime.NotchTemplateRuntime;
import bigsky.notch.util.Exceptions;
import bigsky.notch.util.Text;
import bigsky.notch.chisel.Token;

import java.util.HashMap;
import java.util.Map;

import static bigsky.notch.chisel.type.TokenTypeString.STR;

public class LayoutCommand extends NotchTemplateCommand implements NotchTemplateCommand.Global {
    public LayoutCommand() {
        super("layout");
    }

    private Token path;
    private NotchTemplateContentBlock childContent;

    @Override
    public void parse(Token commandName, NotchTemplateParser tmplParser, NotchParser commandParser) {
        path = commandParser.require(STR, "expected template parent path string");
        end = commandParser.location();

        childContent = tmplParser.parseContentBlock();
        // addChildContent(content); // <-- NO
        // this is intentional cuz it isn't meant to be walked
    }

    public record Blocks(Map<String, LayoutContentItem> contentBlocks) {
    }

    public static final NotchTemplateRuntime.StorageKey<Blocks> KEY_LAYOUT_CONTENT = new NotchTemplateRuntime.StorageKey<>(Blocks.class, "layout.content");

    public static final NotchTemplateRuntime.StorageKey<Mode> KEY_LAYOUT_MODE = new NotchTemplateRuntime.StorageKey<>(Mode.class, "layout.mode");

    private NotchTemplateContentBlock parseParentTemplate(NotchTemplateRuntime runtime) {
        try {
            var templates = runtime.templates();
            var src = templates.getLoader().loadTemplate(path.str());
            var parser = new NotchTemplateParser(templates, path.str(), src);
            var content = parser.parseContentBlock();
            return content;
        } catch (RenderException e) {
            throw Exceptions.rethrow(e);
        } catch (Exception e) {
            throw new RenderException(path.start, path.end, "failed to load template %s".formatted(Text.repr(path.str())), e);
        }
    }

    private Blocks collectContentItems(NotchTemplateContentBlock.GlobalCommands parentGlobals) {
        var out = new HashMap<String, LayoutContentItem>();
        for (var cmd : parentGlobals.globals()) {
            if (!(cmd instanceof ContentCommand contentCmd)) {
                continue;
            }

            var blockName = contentCmd.blockName();
            if (out.containsKey(blockName)) {
                throw new RenderException(contentCmd.getStart(), contentCmd.getEnd(), "duplicate block with name " + Text.repr(blockName));
            }
            out.put(blockName, null);
        }
        return new Blocks(out);
    }

    @Override
    public void render(NotchTemplateRuntime runtime, StringBuilder sb) {
        var parentContent = parseParentTemplate(runtime);
        var parentGlobals = parentContent.collectGlobalCommands();
        var blocks = collectContentItems(parentGlobals);

        var childSb = new StringBuilder();
        try {
            runtime.storage(KEY_LAYOUT_CONTENT, blocks);
            runtime.storage(KEY_LAYOUT_MODE, Mode.CHILD);

            var childGlobals = childContent.collectGlobalCommands();
            for (var childCommand : childGlobals.globals()) {
                childCommand.preRender(runtime);
            }
            childContent.render(runtime, childSb);
            for (var childCommand : childGlobals.globals()) {
                childCommand.postRender(runtime, childSb);
            }

            blocks.contentBlocks.put("", new LayoutContentItemText(childSb.toString()));

            runtime.storage(KEY_LAYOUT_CONTENT, null);
            runtime.storage(KEY_LAYOUT_MODE, null);
        } catch (RenderException e) {
            throw Exceptions.rethrow(e);
        } catch (Exception e) {
            throw new RenderException(path.start, path.end, "failed to render template %s".formatted(Text.repr(path.str())), e);
        }

        try { // let's render the parent;
            var parentRuntime = new NotchTemplateRuntime(path.str(), runtime);
            parentRuntime.storage(KEY_LAYOUT_CONTENT, blocks);
            parentRuntime.storage(KEY_LAYOUT_MODE, Mode.PARENT);

            for (var cmd : parentGlobals.globals()) {
                cmd.preRender(parentRuntime);
            }
            parentContent.render(parentRuntime, sb);
            for (var cmd : parentGlobals.globals()) {
                cmd.postRender(parentRuntime, sb);
            }

            parentRuntime.storage(KEY_LAYOUT_CONTENT, null);
            parentRuntime.storage(KEY_LAYOUT_MODE, null);
        } catch (RenderException e) {
            throw Exceptions.rethrow(e);
        }
    }

    public enum Mode {
        PARENT, CHILD
    }
}
