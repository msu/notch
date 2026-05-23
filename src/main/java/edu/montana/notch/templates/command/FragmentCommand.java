package edu.montana.notch.templates.command;

import edu.montana.notch.NotchParser;
import edu.montana.notch.chisel.Token;
import edu.montana.notch.templates.NotchTemplateCommand;
import edu.montana.notch.templates.NotchTemplateParser;
import edu.montana.notch.templates.ast.NotchTemplateContentBlock;
import edu.montana.notch.templates.runtime.NotchTemplateImportable;
import edu.montana.notch.templates.runtime.NotchTemplateRenderable;
import edu.montana.notch.templates.runtime.NotchTemplateRuntime;
import edu.montana.notch.util.BetterList;

import java.util.Map;

/// A macro-like command that inherits it's parents environment
///
public class FragmentCommand extends NotchTemplateCommand implements NotchTemplateImportable {
    public FragmentCommand() {
        super("fragment");
        isGlobal = true;
    }

    private Token name;
    private NotchTemplateContentBlock content;

    public final Fragment fragment = new Fragment();

    @Override
    public void parseCommand(NotchParser commandParser) {
        name = commandParser.requireIdent("expected fragment name");
        commandParser.requireEnd("expected end of command: `#fragment name`");
    }

    @Override
    public void parseBody(NotchTemplateParser parser) {
        // note: we don't add this as child content!
        content = parser.parseContentBlock(new EndCommand());
    }

    @Override
    public void render(NotchTemplateRuntime runtime, StringBuilder sb) {
        runtime.defineOrUpdate(name.str(), fragment);
        try (var ignored = runtime.pushScope()) {
            content.render(runtime, sb);
        }
    }

    public String getFragmentName() {
        return name.str();
    }

    @Override
    public Map<String, Object> getExportedValues() {
        return Map.of(getFragmentName(), fragment);
    }

    public class Fragment implements NotchTemplateRenderable {
        @Override
        public String getName() {
            return name.str();
        }

        @Override
        public String getQualifiedName() {
            return sourceId() + ":" + name.str();
        }

        @Override
        public void render(BetterList<Object> args, NotchTemplateRuntime runtime, StringBuilder sb) {
            var child = new NotchTemplateRuntime(source(), runtime);
            try (var ignoreScope = child.pushScope()) {
                content.render(child, sb);
            }
        }
    }
}
