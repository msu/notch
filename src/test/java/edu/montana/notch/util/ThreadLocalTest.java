package edu.montana.notch.util;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

public class ThreadLocalTest {

    static boolean useThreadLocal = true;
    static ThreadLocal<String> threadLocal = useThreadLocal ? new ThreadLocal<>() : null;
    static String foo = "foo";

    public static void main(String[] args) {
        if (threadLocal != null) {
            threadLocal.set(foo);
        }
        PrintStream out = new PrintStream(new ByteArrayOutputStream(), true, StandardCharsets.UTF_8);
        long start = System.currentTimeMillis();
        for (int i = 0; i < 10_000_000; i++) {
            String s = getString();
            out.println("This is a reasonable log message " + s);
        }
        long end = System.currentTimeMillis();
        System.out.println("Time: " + (end - start));
    }

    private static String getString() {
        if (threadLocal == null) {
            return foo;
        } else {
            return threadLocal.get();
        }
    }

}
