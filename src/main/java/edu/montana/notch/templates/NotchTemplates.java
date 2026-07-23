package edu.montana.notch.templates;

import edu.montana.notch.chisel.Source;
import edu.montana.notch.chisel.Tokenizer;
import edu.montana.notch.runtime.Drain;
import edu.montana.notch.templates.ast.NotchTemplateContentBlock;
import edu.montana.notch.templates.command.FragmentCommand;
import edu.montana.notch.templates.loader.ClasspathNotchTemplateLoader;
import edu.montana.notch.templates.loader.NotchTemplateLoader;
import edu.montana.notch.templates.runtime.NotchTemplateRuntime;
import edu.montana.notch.templates.runtime.RenderException;
import edu.montana.notch.templates.token.NotchTemplateTokenTypeCommand;
import edu.montana.notch.templates.token.NotchTemplateTokenTypeExpression;
import edu.montana.notch.templates.token.NotchTemplateTokenTypeText;
import edu.montana.notch.util.BetterList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import static edu.montana.notch.util.Text.repr;

public class NotchTemplates {
    private static Logger LOGGER = LoggerFactory.getLogger(NotchTemplates.class);
    public static final Tokenizer TOKENIZER = new Tokenizer()
            .withTokenType("command", NotchTemplateTokenTypeCommand.COMMAND)
            .withTokenType("expression", NotchTemplateTokenTypeExpression.EXPRESSION)
            .withTokenType("text", NotchTemplateTokenTypeText.TEXT);

    protected NotchTemplateLoader loader;
    protected final LinkedHashMap<String, NotchTemplateCommand> commands = new LinkedHashMap<>();

    public NotchTemplates() {
        this(new ClasspathNotchTemplateLoader());
    }

    public NotchTemplates(NotchTemplateLoader loader) {
        this.loader = loader;
    }

    public void addCommand(NotchTemplateCommand command) {
        Objects.requireNonNull(command);
        if (commands.containsKey(command.commandName)) {
            throw new IllegalArgumentException("a command named " + repr(command.commandName) + " is already registered");
        }
        commands.put(command.commandName, command);
    }

    public boolean hasCommand(NotchTemplateCommand command) {
        if (!commands.containsKey(command.commandName)) return false;
        var cmd = commands.get(command.commandName);
        return cmd.getClass().equals(command.getClass());
    }

    record TemplateUri(String path, String fragment) {
    }

    public TemplateUri parseUri(String uri) {
        int pos = uri.indexOf('#');
        if (pos == -1) {
            return new TemplateUri(uri, null);
        }
        String fragment = uri.substring(pos + 1);
        return new TemplateUri(uri.substring(0, pos), fragment);
    }


    public String renderTemplate(String uriStr, Map<String, Object> vars) {
        final var sb = new StringBuilder();
        renderTemplate(uriStr, vars, new Drain(sb));
        return sb.toString();
    }

    public void renderTemplate(String uriStr, Map<String, Object> vars, Drain out) {
        Long initialTime = null;
        if (LOGGER.isInfoEnabled()) {
            LOGGER.info("Rendering {} with symbols [{}]", uriStr, vars.keySet().stream().sorted().collect(Collectors.joining(", ")));
            initialTime = System.currentTimeMillis();
        }
        try {
            var uri = parseUri(uriStr);
            var source = loader.loadSource(uri.path);
            renderFromSource(source, uri.fragment, vars, out);
        } finally {
            if (LOGGER.isInfoEnabled()) {
                LOGGER.info("Rendered {} in {}ms", uriStr, System.currentTimeMillis() - initialTime);
            }
        }
    }

    public String renderTemplate(String uri, NotchTemplateRuntime parent) {
        final var sb = new StringBuilder();
        renderTemplate(uri, parent, new Drain(sb));
        return sb.toString();
    }

    public void renderTemplate(String uri, NotchTemplateRuntime parent, Drain out) {
        Long initialTime = null;
        if (LOGGER.isInfoEnabled()) {
            LOGGER.info("Rendering {} with symbols [{}]", uri, parent.getInScopeSymbols().stream().sorted().collect(Collectors.joining(", ")));
            initialTime = System.currentTimeMillis();
        }
        try {
            var intent = parseUri(uri);
            var source = loader.loadSource(intent.path);
            var runtime = new NotchTemplateRuntime(source, parent);
            renderFromSource(source, intent.fragment, runtime, out);
        } finally {
            if (LOGGER.isInfoEnabled()) {
                LOGGER.info("Rendered {} in {}ms", uri, System.currentTimeMillis() - initialTime);
            }
        }
    }

    public String renderFromSource(Source source, String fragment, Map<String, Object> vars) {
        final var sb = new StringBuilder();
        renderFromSource(source, fragment, vars, new Drain(sb));
        return sb.toString();
    }

    public void renderFromSource(Source source, String fragment, Map<String, Object> vars, Drain out) {
        final var runtime = new NotchTemplateRuntime(source, this, vars);
        renderFromSource(source, fragment, runtime, out);
    }

    private NotchTemplateContentBlock parseSource(Source source) {
        try {
            final var parser = new NotchTemplateParser(this, source);
            final var contentBlock = parser.parseContentBlock();
            return contentBlock;
        } catch (Exception e) {
            throw new RenderException(e);
        }
    }

    public String renderString(String template, Map<String, Object> vars) {
        final var sb = new StringBuilder();
        renderString(template, vars, new Drain(sb));
        return sb.toString();
    }

    public void renderString(String template, Map<String, Object> vars, Drain out) {
        final var source = new Source("template", template);
        renderFromSource(source, null, vars, out);
    }


    public NotchTemplateContentBlock getTemplateContent(String templatePath) {
        final var source = getLoader().loadSource(templatePath);
        final var contentBlock = parseSource(source);
        return contentBlock;
    }

    private FragmentCommand.Fragment findFragment(String fragment, NotchTemplateRuntime runtime) {
        final var fragments = runtime.globals(FragmentCommand.class);
        for (var cmd : fragments) {
            if (!(cmd instanceof FragmentCommand fragmentCmd)) continue;
            if (fragmentCmd.getFragmentName().equals(fragment)) {
                return fragmentCmd.fragment;
            }
        }
        throw new RenderException("Fragment " + fragment + " not found");
    }

    public String renderFromSource(Source source, String fragmentName, NotchTemplateRuntime runtime) {
        final var sb = new StringBuilder();
        renderFromSource(source, fragmentName, runtime, new Drain(sb));
        return sb.toString();
    }

    public void renderFromSource(Source source, String fragmentName, NotchTemplateRuntime runtime, Drain out) {
        final var contentBlock = parseSource(source);
        if (fragmentName != null) {
            contentBlock.preRender(runtime);
            FragmentCommand.Fragment fragment = findFragment(fragmentName, runtime);
            fragment.render(new BetterList<>(), runtime, out);
            contentBlock.postRender(runtime);
        } else {
            contentBlock.fullRender(runtime, out);
        }
    }

    public NotchTemplateLoader getLoader() {
        return loader;
    }
}
