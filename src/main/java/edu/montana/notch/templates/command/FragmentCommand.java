package edu.montana.notch.templates.command;

import edu.montana.notch.NotchParser;
import edu.montana.notch.templates.NotchTemplateCommand;
import edu.montana.notch.templates.NotchTemplateParser;
import edu.montana.notch.templates.ast.NotchTemplateContentBlock;
import edu.montana.notch.templates.runtime.NotchTemplateImportable;
import edu.montana.notch.templates.runtime.NotchTemplateRenderable;
import edu.montana.notch.templates.runtime.NotchTemplateRuntime;
import edu.montana.notch.chisel.Token;

import java.util.List;
import java.util.Map;

public class FragmentCommand extends NotchTemplateCommand implements NotchTemplateCommand.Global, NotchTemplateImportable {
    public FragmentCommand() {
        super("fragment");
    }

    private Token name;
    private NotchTemplateContentBlock content;

    public final Fragment fragment = new Fragment();

    @Override
    public void parse(Token commandName, NotchTemplateParser tmplParser, NotchParser commandParser) {
        name = commandParser.requireIdent("expected fragment name");
        commandParser.requireEnd("expected end of command: `#fragment name`");

        // note: we don't add this as child content!
        content = tmplParser.parseContentBlock(EndCommand.class);
    }

    @Override
    public void render(NotchTemplateRuntime runtime, StringBuilder sb) {
        runtime.defineOrUpdate(name.str(), fragment);
        try (var ignored = runtime.pushScope(fileId, span())) {
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
            return fileId + ":" + name.str();
        }

        @Override
        public void render(List<Object> args, NotchTemplateRuntime runtime, StringBuilder sb) {
            var child = new NotchTemplateRuntime(fileId, runtime);
            try (var ignoreScope = child.pushScope(fileId, span())) {
                content.render(child, sb);
            }
        }
    }
}
