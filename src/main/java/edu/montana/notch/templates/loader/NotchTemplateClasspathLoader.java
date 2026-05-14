package edu.montana.notch.templates.loader;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.util.Objects;
import java.util.stream.Collectors;

public class NotchTemplateClasspathLoader extends NotchTemplateLoader {
    public final ClassLoader loader;
    public final Path contentRoot;

    public NotchTemplateClasspathLoader(ClassLoader loader, Path contentRoot) {
        this.loader = Objects.requireNonNull(loader);
        this.contentRoot = Objects.requireNonNull(contentRoot);
    }

    public NotchTemplateClasspathLoader(ClassLoader loader) {
        this(loader, Path.of("."));
    }

    public NotchTemplateClasspathLoader(Path contentRoot) {
        this(Thread.currentThread().getContextClassLoader(), contentRoot);
    }

    public NotchTemplateClasspathLoader() {
        this(Path.of("."));
    }

    @Override
    public String loadTemplate(String path) {
        var templatePath = contentRoot.resolve(path);
        try (var is = loader.getResourceAsStream(templatePath.toString())) {
            if (is == null) {
                throw new FileNotFoundException(path);
            }
            try (var isr = new InputStreamReader(is)) {
                try (var br = new BufferedReader(isr)) {
                    return br.lines().collect(Collectors.joining(System.lineSeparator()));
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
