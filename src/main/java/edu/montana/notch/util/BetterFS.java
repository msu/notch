package edu.montana.notch.util;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public class BetterFS {
    private BetterFS() {}

    public static String read(String file, String encoding) {
        Charset charset;
        if (encoding == null) {
            charset = StandardCharsets.UTF_8;
        } else {
            charset = Charset.forName(encoding);
        }
        return Exceptions.safely(() -> Files.readString(Path.of(file), charset));
    }

    public static String read(String file) {
        return read(file, null);
    }
}
