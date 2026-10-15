package edu.montana.notch.templates.loader;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;

import static edu.montana.notch.AssertContains.assertContains;
import static org.junit.jupiter.api.Assertions.*;

public class TemplateLoaderTests {
    @TempDir
    Path tmp;

    private URLClassLoader jarLoader(Map<String, String> entries) throws IOException {
        var jar = tmp.resolve("templates.jar");
        try (var out = new JarOutputStream(Files.newOutputStream(jar))) {
            for (var entry : entries.entrySet()) {
                out.putNextEntry(new JarEntry(entry.getKey()));
                out.write(entry.getValue().getBytes(StandardCharsets.UTF_8));
                out.closeEntry();
            }
        }
        return new URLClassLoader(new URL[]{jar.toUri().toURL()}, null);
    }

    @Test
    public void classpathRootFindsJarResource() throws IOException {
        try (var cl = jarLoader(Map.of("greeting", "Hello"))) {
            var loader = new ClasspathNotchTemplateLoader(cl);
            assertTrue(loader.exists("greeting"));
            assertEquals("Hello", loader.loadSource("greeting").content.toString());
        }
    }

    @Test
    public void classpathNormalizesPaths() throws IOException {
        try (var cl = jarLoader(Map.of("views/a/b.html", "B"))) {
            var loader = new ClasspathNotchTemplateLoader(cl, Path.of("views"));
            assertEquals("views/a/b.html", loader.resourceName("./a/./b.html"));
            assertEquals("views/a/b.html", loader.resourceName("a/c/../b.html"));
            assertEquals("B", loader.loadSource("./a/b.html").content.toString());
        }
    }

    @Test
    public void classpathRejectsTraversal() {
        var loader = new ClasspathNotchTemplateLoader(Path.of("views"));
        assertThrows(IllegalStateException.class, () -> loader.resourceName("../../secret"));
    }

    @Test
    public void defaultSearchesTemplateFoldersInJar() throws IOException {
        try (var cl = jarLoader(Map.of(
                "templates/page.html", "from templates",
                "views/page.html", "from views",
                "views/only.html", "only views"))) {
            var loader = new DefaultNotchTemplateLoader(cl);
            assertEquals("from templates", loader.loadSource("page.html").content.toString());
            assertEquals("only views", loader.loadSource("only.html").content.toString());
        }
    }

    @Test
    public void defaultPrefersClasspathRoot() throws IOException {
        try (var cl = jarLoader(Map.of("page.html", "root", "templates/page.html", "templates"))) {
            assertEquals("root", new DefaultNotchTemplateLoader(cl).loadSource("page.html").content.toString());
        }
    }

    @Test
    public void defaultSearchesFileSystem() throws IOException {
        Files.createDirectories(tmp.resolve("views"));
        Files.writeString(tmp.resolve("views/disk.html"), "on disk");
        try (var cl = jarLoader(Map.of())) {
            var loader = new DefaultNotchTemplateLoader(List.of(
                    new ClasspathNotchTemplateLoader(cl),
                    new FileSystemNotchTemplateLoader(tmp.resolve("templates")),
                    new FileSystemNotchTemplateLoader(tmp.resolve("views"))));
            assertEquals("on disk", loader.loadSource("disk.html").content.toString());
        }
    }

    @Test
    public void defaultErrorListsEveryLocation() throws IOException {
        try (var cl = jarLoader(Map.of())) {
            var loader = new DefaultNotchTemplateLoader(cl);
            var e = assertThrows(RuntimeException.class, () -> loader.loadSource("missing.html"));
            var msg = e.getCause().getMessage();
            assertContains("missing.html", msg, msg);
            assertContains("classpath:/templates", msg, msg);
            assertContains("classpath:/views", msg, msg);
            assertContains(Path.of("templates").toAbsolutePath().toString(), msg, msg);
            assertContains(Path.of("views").toAbsolutePath().toString(), msg, msg);
        }
    }
}
