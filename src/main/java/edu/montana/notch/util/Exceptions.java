package edu.montana.notch.util;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.concurrent.Callable;
import java.util.function.Predicate;

public class Exceptions {

    public static RuntimeException rethrow(Throwable t) {
        Exceptions.<RuntimeException>sneakyThrow(t);
        throw new RuntimeException("Never hit");
    }

    @SuppressWarnings("unchecked")
    private static <E extends Throwable> void sneakyThrow(Throwable e) throws E {
        throw (E) e;
    }

    public static void safely(RunnableWithException runnable) {
        try {
            runnable.run();
        } catch (Throwable t) {
            throw rethrow(t);
        }
    }

    public static <T> T safely(ProducerWithException<T> producer) {
        try {
            return producer.produce();
        } catch (Throwable t) {
            throw rethrow(t);
        }
    }


    public static void ignore(Runnable runnable) {
        try {
            runnable.run();
        } catch (Throwable ignored) {
        }
    }

    public static <T> T safelyEval(Callable<T> callable) {
        try {
            return callable.call();
        } catch (Exception e) {
            throw rethrow(e);
        }
    }

    public static <T> T safelyEval(Callable<T> callable, Throwable alt) {
        try {
            return callable.call();
        } catch (Exception e) {
            throw rethrow(alt);
        }
    }

    public static StackTraceElement[] replaceInStackTrace(StackTraceElement[] original,
                                                          Predicate<StackTraceElement> filter,
                                                          Exception baseTemplate,
                                                          StackTraceElement... replacements) {
        StackTraceElement[] baseStackTrace = baseTemplate.getStackTrace();
        LinkedList<StackTraceElement> newStackTrace = new LinkedList<>(Arrays.asList(baseStackTrace));
        newStackTrace.pop(); // remove top of stack trace to eliminate callsite
        int startingIndex = original.length - baseStackTrace.length;
        int index = startingIndex;
        boolean filtered = true;
        boolean replaced = false;
        while (index >= 0) {
            StackTraceElement currentElement = original[index];
            filtered = filtered && filter.test(currentElement);
            if (!filtered) {
                // if this is the first non-filtered element, stitch in the replacement
                if (!replaced) {
                    for (int i = 0; i < replacements.length; i++) {
                        StackTraceElement replacement = replacements[i];
                        newStackTrace.push(replacement);
                    }
                    replaced = true;
                }
                // push the non-filtered element
                newStackTrace.push(currentElement);
            }
            index--;
        }
        return newStackTrace.toArray(StackTraceElement[]::new);
    }

    public static BetterList<StackTraceElement> localizeStackTrace(StackTraceElement[] stackTrace) {
        final var current = Thread.currentThread().getStackTrace();
        int off = 0;
        while (
                off < stackTrace.length &&
                        off < current.length &&
                        current[current.length - off - 1].equals(stackTrace[stackTrace.length - off - 1])
        ) {
            off += 1;
        }
        return BetterList.better(Arrays.asList(stackTrace).subList(0, stackTrace.length - off));
    }

    public interface ProducerWithException<T> {
        T produce() throws Exception;
    }

    public interface RunnableWithException {
        void run() throws Exception;
    }
}
