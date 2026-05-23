package edu.montana.notch;

import org.opentest4j.AssertionFailedError;

import java.util.Collection;
import java.util.Objects;
import java.util.StringJoiner;
import java.util.function.Supplier;

/**
 * Custom JUnit 5 assertion utilities providing rich, readable failure messages
 * for "contains" checks on Strings and Collections.
 *
 * <p>Mirrors JUnit 5's own assertion overload convention:
 * <ol>
 *   <li>{@code assertContains(expected, actual)}</li>
 *   <li>{@code assertContains(expected, actual, String message)}</li>
 *   <li>{@code assertContains(expected, actual, Supplier<String> messageSupplier)}</li>
 * </ol>
 *
 * <p>All methods throw {@link AssertionFailedError} on failure so that IDEs
 * and CI runners that understand JUnit 5 / OpenTest4J can display the expected
 * vs. actual values in the diff view.
 *
 * <p>Usage — static-import for maximum readability:
 * <pre>{@code
 *   import static org.example.testing.AssertContains.*;
 *
 *   assertContains("world", "hello world");
 *   assertContains("world", "hello world", "greeting should contain 'world'");
 *   assertContains("world", "hello world", () -> "greeting should contain 'world'");
 *
 *   assertContains(42, List.of(1, 42, 99));
 *   assertContains(42, List.of(1, 42, 99), "list must contain the answer");
 *   assertContains(42, List.of(1, 42, 99), () -> "list must contain the answer");
 * }</pre>
 */
public final class AssertContains {

    private AssertContains() {
        // Utility class — not instantiable.
    }

    // =========================================================================
    // String overloads
    // =========================================================================

    /**
     * Asserts that {@code content} contains {@code substring}.
     *
     * @param substring the expected substring (must not be {@code null})
     * @param content   the string to search within (must not be {@code null})
     * @throws AssertionFailedError if {@code content} does not contain {@code substring},
     *                              or if either argument is {@code null}
     */
    public static void assertContains(String substring, String content) {
        assertContains(substring, content, (String) null);
    }

    /**
     * Asserts that {@code content} contains {@code substring}.
     *
     * @param substring the expected substring (must not be {@code null})
     * @param content   the string to search within (must not be {@code null})
     * @param message   optional failure message prefix; {@code null} is silently ignored
     * @throws AssertionFailedError if {@code content} does not contain {@code substring},
     *                              or if either argument is {@code null}
     */
    public static void assertContains(String substring, String content, String message) {
        assertContains(substring, content, message == null ? null : () -> message);
    }

    /**
     * Asserts that {@code content} contains {@code substring}.
     *
     * <p>The {@code messageSupplier} is only evaluated on failure, making it
     * safe to use for expensive message-construction without a performance cost
     * on the happy path.
     *
     * @param substring       the expected substring (must not be {@code null})
     * @param content         the string to search within (must not be {@code null})
     * @param messageSupplier optional lazy failure-message supplier; {@code null} is silently ignored
     * @throws AssertionFailedError if {@code content} does not contain {@code substring},
     *                              or if either argument is {@code null}
     */
    public static void assertContains(
            String substring,
            String content,
            Supplier<String> messageSupplier) {

        // Validate parameters with dedicated, clear null messages.
        if (substring == null) {
            throw new AssertionFailedError(
                    buildHeader(messageSupplier) + "substring must not be null");
        }
        if (content == null) {
            throw new AssertionFailedError(
                    buildHeader(messageSupplier) + "content must not be null");
        }

        if (!content.contains(substring)) {
            String header = buildHeader(messageSupplier);

            // Build a visually distinct "expected to find / but was" message.
            String expected = quote(substring);
            String actual = truncate(content, 200);

            // Find where the strings diverge to help narrow the search.
            String hint = findClosestMatch(substring, content);

            String detail = String.format(
                    "%s%nExpected to find : %s%nIn string        : %s%s",
                    header.isEmpty() ? "assertContains failed" : header.stripTrailing(),
                    expected,
                    quote(actual),
                    hint.isEmpty() ? "" : "%n" + hint);

            throw new AssertionFailedError(detail, substring, content);
        }
    }

    // =========================================================================
    // Collection overloads
    // =========================================================================

    /**
     * Asserts that {@code collection} contains {@code value}.
     *
     * <p>Containment is determined by {@link Collection#contains}, which in turn
     * relies on {@link Objects#equals}. For custom equality semantics, ensure
     * your element type overrides {@code equals}/{@code hashCode} correctly.
     *
     * @param <T>        the element type
     * @param value      the value to look for (may be {@code null})
     * @param collection the collection to search within (must not be {@code null})
     * @throws AssertionFailedError if {@code collection} does not contain {@code value},
     *                              or if {@code collection} is {@code null}
     */
    public static <T> void assertContains(T value, Collection<T> collection) {
        assertContains(value, collection, (String) null);
    }

    /**
     * Asserts that {@code collection} contains {@code value}.
     *
     * @param <T>        the element type
     * @param value      the value to look for (may be {@code null})
     * @param collection the collection to search within (must not be {@code null})
     * @param message    optional failure message prefix; {@code null} is silently ignored
     * @throws AssertionFailedError if {@code collection} does not contain {@code value},
     *                              or if {@code collection} is {@code null}
     */
    public static <T> void assertContains(T value, Collection<T> collection, String message) {
        assertContains(value, collection, message == null ? null : () -> message);
    }

    /**
     * Asserts that {@code collection} contains {@code value}.
     *
     * <p>The {@code messageSupplier} is only evaluated on failure.
     *
     * @param <T>             the element type
     * @param value           the value to look for (may be {@code null})
     * @param collection      the collection to search within (must not be {@code null})
     * @param messageSupplier optional lazy failure-message supplier; {@code null} is silently ignored
     * @throws AssertionFailedError if {@code collection} does not contain {@code value},
     *                              or if {@code collection} is {@code null}
     */
    public static <T> void assertContains(
            T value,
            Collection<T> collection,
            Supplier<String> messageSupplier) {

        if (collection == null) {
            throw new AssertionFailedError(
                    buildHeader(messageSupplier) + "collection must not be null");
        }

        if (!collection.contains(value)) {
            String header = buildHeader(messageSupplier);

            String renderedCollection = renderCollection(collection, 10);
            String detail = String.format(
                    "%s%nExpected to find : <%s>%nIn collection    : %s%nCollection size  : %d",
                    header.isEmpty() ? "assertContains failed" : header.stripTrailing(),
                    value,
                    renderedCollection,
                    collection.size());

            throw new AssertionFailedError(detail, value, collection);
        }
    }

    // =========================================================================
    // Private helpers
    // =========================================================================

    /**
     * Evaluates the message supplier (if any) and returns a formatted header
     * string that ends with a newline when non-empty.
     */
    private static String buildHeader(Supplier<String> messageSupplier) {
        if (messageSupplier == null) {
            return "";
        }
        try {
            String msg = messageSupplier.get();
            if (msg == null || msg.isBlank()) {
                return "";
            }
            return msg.stripTrailing() + " ==> ";
        } catch (Exception e) {
            // Never let message construction crash the assertion framework.
            return "[message supplier threw " + e.getClass().getSimpleName() + "] ==> ";
        }
    }

    /**
     * Wraps a string in double-quotes for display in failure messages.
     */
    private static String quote(String s) {
        return "\"" + s + "\"";
    }

    /**
     * Truncates {@code s} to at most {@code maxLen} characters, appending an
     * ellipsis when the string is cut short.
     */
    private static String truncate(String s, int maxLen) {
        if (s.length() <= maxLen) {
            return s;
        }
        return s.substring(0, maxLen) + "… [" + (s.length() - maxLen) + " more chars]";
    }

    /**
     * Attempts a case-insensitive match to produce a helpful hint when an exact
     * (case-sensitive) match fails. Returns an empty string when no hint applies.
     */
    private static String findClosestMatch(String substring, String content) {
        // Case-insensitive hit → the most common "oops" scenario.
        if (content.toLowerCase().contains(substring.toLowerCase())) {
            return "Hint             : a case-insensitive match WAS found; check casing.";
        }

        // Leading/trailing whitespace hint.
        if (content.contains(substring.strip())) {
            return "Hint             : a match was found after stripping whitespace from the substring.";
        }

        return "";
    }

    /**
     * Renders up to {@code limit} elements of {@code collection} as a bracketed
     * list, appending an ellipsis when the collection is larger.
     */
    private static <T> String renderCollection(Collection<T> collection, int limit) {
        StringJoiner sj = new StringJoiner(", ", "[", "]");
        int count = 0;
        for (T element : collection) {
            if (count++ >= limit) {
                sj.add("… " + (collection.size() - limit) + " more");
                break;
            }
            sj.add(String.valueOf(element));
        }
        return sj.toString();
    }
}
