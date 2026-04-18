package bigsky.notch.util;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

public final class FS {
    private FS() {
    }

    public static List<Path> listdir(Path dir) {
        var out = new ArrayList<Path>();
        var files = Objects.requireNonNull(dir.toFile().listFiles(), "not a directory");
        for (var f : files) {
            var path = f.toPath();
            out.add(path);
        }
        return out;
    }

    public static boolean isDir(Path path) {
        return path.toFile().isDirectory();
    }

    public static boolean exists(Path path) {
        return path.toFile().exists();
    }

    public static boolean exists(String path) {
        return exists(Path.of(path));
    }

    public static void mkdirs(Path path) {
        Exceptions.safely(() -> {
            Files.createDirectories(path);
        });
    }

    public static void mkdirs(String path) {
        mkdirs(Path.of(path));
    }

    public static void mkdirsIfNotExists(Path path) {
        if (!exists(path)) {
            mkdirs(path);
        }
    }

    public static void mkdirsIfNotExists(String path) {
        mkdirsIfNotExists(Path.of(path));
    }

    public static Stream<Path> listFilesRecursive(Path path) {
        return listdir(path).stream()
                .flatMap(child -> {
                    if (isDir(child)) return listFilesRecursive(child);
                    return Stream.of(child);
                });
    }

    public static String ext(Path path) {
        var fileName = path.getFileName().toString();
        var dot = fileName.lastIndexOf('.');
        if (dot == -1) return "";
        return fileName.substring(dot + 1);
    }

    public static Path makeRelativeTo(Path root, Path file) {
        Path normalizedRoot = root.normalize().toAbsolutePath();
        Path normalizedFile = file.normalize().toAbsolutePath();
        return normalizedRoot.relativize(normalizedFile);
    }

    public static String read(Path path) {
        return Exceptions.safely(() -> Files.readString(path));
    }

    public static String read(String path) {
        return Exceptions.safely(() -> Files.readString(Path.of(path)));
    }
}
