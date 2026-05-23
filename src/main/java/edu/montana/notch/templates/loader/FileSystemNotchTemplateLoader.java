package edu.montana.notch.templates.loader;

import edu.montana.notch.chisel.Source;
import edu.montana.notch.util.Text;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class FileSystemNotchTemplateLoader extends NotchTemplateLoader {
    public final Path rootDir;

    public FileSystemNotchTemplateLoader(Path rootDir) {
        this.rootDir = rootDir.toAbsolutePath();
    }

    @Override
    public Source loadSource(String path) {
        var templatePath = rootDir.resolve(path).toAbsolutePath();
        if (!templatePath.startsWith(rootDir)) {
            throw new IllegalStateException("refused to load path, suspected path exploit: %s".formatted(Text.repr(templatePath)));
        }
        try {
            final var content = Files.readString(templatePath);
            return new Source(templatePath.toString(), content);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
