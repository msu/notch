package edu.montana.notch.templates.javalin;

import edu.montana.notch.templates.NotchTemplates;
import edu.montana.notch.util.Key;
import io.javalin.http.Context;
import io.javalin.rendering.FileRenderer;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * A Javalin {@link FileRenderer} backed by {@link NotchTemplates}.
 *
 * <p>Register it with Javalin's config so that {@code ctx.render(path, model)} resolves
 * templates through the Notch template engine:
 *
 * <pre>{@code
 * Javalin.create(config ->
 *     config.fileRenderer(new NotchTemplateFileRenderer()));
 * }</pre>
 *
 * <p>The {@code filePath} passed to {@link #render} is forwarded verbatim as a Notch
 * template URI (it may include a {@code #fragment} suffix), and the model entries become
 * the template's top-level variables.
 */
public class NotchJavalinRenderer implements FileRenderer {
    private final NotchTemplates templates;

    /** Renders against a fresh {@link NotchTemplates} using the classpath loader. */
    public NotchJavalinRenderer() {
        this(new NotchTemplates());
    }

    /** Renders against the supplied template engine. */
    public NotchJavalinRenderer(NotchTemplates templates) {
        this.templates = Objects.requireNonNull(templates, "templates");
    }

    @Override
    public String render(String filePath, Map<String, ?> model, Context context) {
        // NotchTemplates expects a Map<String, Object>; copy the model since the engine
        // does not need to retain a reference to Javalin's map.
        final var vars = new LinkedHashMap<String, Object>(model);
        // TODO: put context into runtime storage
        vars.put("JAVALIN_CONTEXT", context);
        return templates.renderTemplate(filePath, vars);
    }
}
