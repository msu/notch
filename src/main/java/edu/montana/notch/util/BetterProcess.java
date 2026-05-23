package edu.montana.notch.util;

import java.io.IOException;

public final class BetterProcess {
    private BetterProcess() {
    }

    public static void run(String... args) {
        try {
            var pb = new ProcessBuilder(args);
            pb.redirectErrorStream(true);
            var proc = pb.start();
            String output = new String(proc.getInputStream().readAllBytes());
            int exitCode = proc.waitFor();

            if (exitCode != 0) {
                throw new RuntimeException(output);
            }
        } catch (IOException | InterruptedException e) {
            throw Exceptions.rethrow(e);
        }
    }

    public static Process start(String... args) {
        return Exceptions.safely(() -> {
            var pb = new ProcessBuilder(args);
            pb.inheritIO();
            return pb.start();
        });
    }

    public static boolean succeeds(String... args) {
        try {
            var pb = new ProcessBuilder(args);
            var proc = pb.start();
            int exitCode = proc.waitFor();
            return exitCode == 0;
        } catch (IOException | InterruptedException e) {
            throw Exceptions.rethrow(e);
        }
    }

    public static String getOutput(String... args) {
        try {
            var pb = new ProcessBuilder(args);
            pb.redirectErrorStream(true);
            var proc = pb.start();
            String output = new String(proc.getInputStream().readAllBytes());
            int exitCode = proc.waitFor();

            if (exitCode != 0) {
                throw new RuntimeException(output);
            }
            return output;
        } catch (IOException | InterruptedException e) {
            throw Exceptions.rethrow(e);
        }
    }
}
