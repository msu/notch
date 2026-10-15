package edu.montana.notch.templates.loader;

import edu.montana.notch.chisel.Source;
import edu.montana.notch.util.Text;

import java.io.FileNotFoundException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * The loader used when none is configured. Searches, in order:
 * <ol>
 *     <li>the classpath root</li>
 *     <li>{@code templates/} on the classpath</li>
 *     <li>{@code views/} on the classpath</li>
 *     <li>{@code templates/} relative to the working directory</li>
 *     <li>{@code views/} relative to the working directory</li>
 * </ol>
 * The first location that has the template wins. If none do, the error lists every location searched.
 */
public class DefaultNotchTemplateLoader extends NotchTemplateLoader {
    public static final List<String> DEFAULT_FOLDERS = List.of("templates", "views");

    public final List<NotchTemplateLoader> locations;

    public DefaultNotchTemplateLoader(ClassLoader classLoader) {
        var locations = new ArrayList<NotchTemplateLoader>();
        locations.add(new ClasspathNotchTemplateLoader(classLoader));
        for (var folder : DEFAULT_FOLDERS) {
            locations.add(new ClasspathNotchTemplateLoader(classLoader, Path.of(folder)));
        }
        for (var folder : DEFAULT_FOLDERS) {
            locations.add(new FileSystemNotchTemplateLoader(Path.of(folder)));
        }
        this.locations = List.copyOf(locations);
    }

    public DefaultNotchTemplateLoader() {
        this(Thread.currentThread().getContextClassLoader());
    }

    public DefaultNotchTemplateLoader(List<NotchTemplateLoader> locations) {
        this.locations = List.copyOf(locations);
    }

    @Override
    public boolean exists(String path) {
        return locations.stream().anyMatch(location -> location.exists(path));
    }

    @Override
    public Source loadSource(String path) {
        for (var location : locations) {
            if (location.exists(path)) {
                return location.loadSource(path);
            }
        }
        var searched = locations.stream()
                .map(location -> "  - " + location)
                .collect(Collectors.joining("\n"));
        throw new RuntimeException(new FileNotFoundException(
                "template %s not found, searched:\n%s".formatted(Text.repr(path), searched)));
    }
}
