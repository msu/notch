package bigsky.notch.templates;

import bigsky.notch.templates.ast.NotchTemplateContentBlock;
import bigsky.notch.templates.command.FragmentCommand;
import bigsky.notch.templates.loader.NotchTemplateClasspathLoader;
import bigsky.notch.templates.loader.NotchTemplateLoader;
import bigsky.notch.templates.runtime.RenderException;
import bigsky.notch.templates.runtime.NotchTemplateRuntime;
import bigsky.notch.util.Text;
import bigsky.notch.chisel.Location;
import bigsky.notch.chisel.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import static bigsky.notch.util.Text.repr;

public class NotchTemplateRegistry {
    private static Logger LOGGER = LoggerFactory.getLogger(NotchTemplateRegistry.class);
    protected NotchTemplateLoader loader;
    protected final LinkedHashMap<String, NotchTemplateCommand> commands = new LinkedHashMap<>();

    public NotchTemplateRegistry() {
        this(new NotchTemplateClasspathLoader(Thread.currentThread().getContextClassLoader()));
    }

    public NotchTemplateRegistry(NotchTemplateLoader loader) {
        this.loader = loader;
    }

    public void addCommand(NotchTemplateCommand command) {
        Objects.requireNonNull(command);
        if (commands.containsKey(command.name)) {
            throw new IllegalArgumentException("a command named " + repr(command.name) + " is already registered");
        }
        commands.put(command.name, command);
    }

    public boolean hasCommand(NotchTemplateCommand command) {
        if (!commands.containsKey(command.name)) return false;
        var cmd = commands.get(command.name);
        return cmd.getClass().equals(command.getClass());
    }

    public String loadTemplate(String path) {
        try {
            String content = loader.loadTemplate(path);
            return content;
        } catch (Exception e) {
            throw new RenderException(Location.SOF, "failed to load template %s".formatted(Text.repr(path)), e);
        }
    }

    record TemplatePath(String path, String fragment) {}
    public TemplatePath parsePath(String path) {
        int pos = path.indexOf('#');
        if (pos == -1) {
            return new TemplatePath(path, null);
        }
        String fragment = path.substring(pos + 1);
        return new TemplatePath(path.substring(0, pos), fragment);
    }


    public String renderTemplate(String intentStr, Map<String, Object> vars) {
        Long initialTime = null;
        if (LOGGER.isInfoEnabled()) {
            LOGGER.info("Rendering {} with symbols [{}]", intentStr, vars.keySet().stream().sorted().collect(Collectors.joining(", ")));
            initialTime = System.currentTimeMillis();
        }
        try {
            var path = parsePath(intentStr);
            var content = loadTemplate(path.path);
            var runtime = new NotchTemplateRuntime(path.path, this, vars);
            return renderFromString(path.path, path.fragment, content, runtime);
        } finally {
            if (LOGGER.isInfoEnabled()) {
                LOGGER.info("Rendered {} in {}ms", intentStr, System.currentTimeMillis() - initialTime);
            }
        }
    }

    public String renderTemplate(String intentStr, NotchTemplateRuntime parent) {
        Long initialTime = null;
        if (LOGGER.isInfoEnabled()) {
            LOGGER.info("Rendering {} with symbols [{}]", intentStr, parent.getInScopeSymbols().stream().sorted().collect(Collectors.joining(", ")));
            initialTime = System.currentTimeMillis();
        }
        try {
            var intent = parsePath(intentStr);
            var content = loadTemplate(intent.path);
            var runtime = new NotchTemplateRuntime(intent.path, parent);
            return renderFromString(intent.path, intent.fragment, content, runtime);
        } finally {
            if (LOGGER.isInfoEnabled()) {
                LOGGER.info("Rendered {} in {}ms", intentStr, System.currentTimeMillis() - initialTime);
            }
        }
    }

    public String renderString(String id, String content, Map<String, Object> vars) {
        var path = parsePath(id);
        var runtime = new NotchTemplateRuntime(path.path, this, vars);
        return renderFromString(path.path, path.fragment, content, runtime);
    }

    public NotchTemplateContentBlock parseContentBlock(String fileId, String content) {
        var parser = new NotchTemplateParser(this, fileId, content);
        try {
            return parser.parseContentBlock();
        } catch (ParseException e) {
            throw new RenderException(e.span, "in " + Text.repr(fileId), e);
        }
    }

    public String renderFromString(String path, String fragment, String content, NotchTemplateRuntime runtime) {
        var contentBlock = parseContentBlock(path, content);
        if (fragment != null) {
            return renderFragment(contentBlock, fragment, runtime);
        } else {
            return renderContent(contentBlock, runtime);
        }
    }

    public String renderFragment(NotchTemplateContentBlock content, String fragment, NotchTemplateRuntime runtime) throws RenderException {
        var out = new StringBuilder();
        var globalInfo = content.collectGlobalCommands();

        var fragments = globalInfo.globals(FragmentCommand.class);
        FragmentCommand chosenCmd = null;
        for (var cmd : fragments) {
            if (cmd.getFragmentName().equals(fragment)) {
                chosenCmd = cmd;
                break;
            }
        }

        if (chosenCmd == null) {
            throw new RenderException(Location.SOF, "no fragment named %s in %s".formatted(fragment, runtime.fileId));
        }

        for (var command : globalInfo.globals()) {
            command.preRender(runtime);
        }

        chosenCmd.render(runtime, out);

        for (var command : globalInfo.globals()) {
            command.postRender(runtime, out);
        }

        return out.toString();
    }

    public String renderContent(NotchTemplateContentBlock content, NotchTemplateRuntime runtime) throws RenderException {
        var out = new StringBuilder();
        var globalInfo = content.collectGlobalCommands();

        for (var command : globalInfo.globals()) {
            command.preRender(runtime);
        }

        content.render(runtime, out);

        for (var command : globalInfo.globals()) {
            command.postRender(runtime, out);
        }

        return out.toString();
    }

    public NotchTemplateLoader getLoader() {
        return loader;
    }
}
