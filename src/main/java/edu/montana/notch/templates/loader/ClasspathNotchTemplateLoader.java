package edu.montana.notch.templates.loader;

import edu.montana.notch.chisel.Source;

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
        this(loader, Path.of("."));
    }

    public ClasspathNotchTemplateLoader(Path contentRoot) {
        this(Thread.currentThread().getContextClassLoader(), contentRoot);
    }

    public ClasspathNotchTemplateLoader() {
        this(Path.of("."));
    }

    @Override
    public Source loadSource(String path) {
        var templatePath = contentRoot.resolve(path);
        try (var is = loader.getResourceAsStream(templatePath.toString())) {
            if (is == null) {
                throw new FileNotFoundException(path);
            }
            try (var isr = new InputStreamReader(is)) {
                try (var br = new BufferedReader(isr)) {
                    final var content = br.lines().collect(Collectors.joining(System.lineSeparator()));
                    return new Source(templatePath.toString(), content);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
