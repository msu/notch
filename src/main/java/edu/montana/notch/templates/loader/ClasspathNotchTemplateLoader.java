package edu.montana.notch.templates.loader;

import edu.montana.notch.chisel.Source;
import edu.montana.notch.util.Text;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.util.Objects;
import java.util.stream.Collectors;

public class ClasspathNotchTemplateLoader extends NotchTemplateLoader {
    public final ClassLoader loader;
    public final Path contentRoot;

    public ClasspathNotchTemplateLoader(ClassLoader loader, Path contentRoot) {
        this.loader = Objects.requireNonNull(loader);
        this.contentRoot = Objects.requireNonNull(contentRoot);
    }

    public ClasspathNotchTemplateLoader(ClassLoader loader) {
        this(loader, Path.of(""));
    }

    public ClasspathNotchTemplateLoader(Path contentRoot) {
        this(Thread.currentThread().getContextClassLoader(), contentRoot);
    }

    public ClasspathNotchTemplateLoader() {
        this(Path.of(""));
    }

    /**
     * The resource name for {@code path}: resolved against the content root, normalized, and
     * joined with {@code /} on every platform, since class loaders do not accept {@code ./}
     * segments or the platform separator.
     */
    public String resourceName(String path) {
        var resolved = contentRoot.resolve(path).normalize();
        if (resolved.isAbsolute() || resolved.startsWith("..")) {
            throw new IllegalStateException("refused to load path, suspected path exploit: %s".formatted(Text.repr(path)));
        }
        var parts = new StringBuilder();
        for (var part : resolved) {
            if (!parts.isEmpty()) parts.append('/');
            parts.append(part);
        }
        return parts.toString();
    }

    @Override
    public boolean exists(String path) {
        return loader.getResource(resourceName(path)) != null;
    }

    @Override
    public Source loadSource(String path) {
        var name = resourceName(path);
        try (var is = loader.getResourceAsStream(name)) {
            if (is == null) {
                throw new FileNotFoundException(path);
            }
            try (var isr = new InputStreamReader(is)) {
                try (var br = new BufferedReader(isr)) {
                    final var content = br.lines().collect(Collectors.joining(System.lineSeparator()));
                    return new Source(name, content);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String toString() {
        var root = resourceName("");
        return "classpath:/" + root;
    }
}
