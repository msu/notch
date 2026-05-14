package edu.montana.notch.templates.loader;

import edu.montana.notch.util.Text;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class NotchTemplateFilesystemLoader extends NotchTemplateLoader {
    public final Path rootDir;

    public NotchTemplateFilesystemLoader(Path rootDir) {
        this.rootDir = rootDir.toAbsolutePath();
    }

    @Override
    public String loadTemplate(String path) {
        var templatePath = rootDir.resolve(path).toAbsolutePath();
        if (!templatePath.startsWith(rootDir)) {
            throw new IllegalStateException("refused to load path, suspected path exploit: %s".formatted(Text.repr(templatePath)));
        }
        try {
            return Files.readString(templatePath);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
