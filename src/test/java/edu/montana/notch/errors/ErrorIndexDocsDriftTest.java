package edu.montana.notch.errors;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIf;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Guards docs/_data/error_codes.yml against drifting out of sync with the error enums.
 *
 * <p>Adding a {@link ParserError} constant without documenting it fails the build, as does
 * deleting a constant while leaving its entry behind, reordering either list, or editing a
 * documented title away from the message the parser actually emits.
 *
 * <p>The enum is the source of truth for *which* codes exist; the YAML is the source of
 * truth for the prose, which no generator could write. This test is what keeps the two
 * honest. Same shape as
 * {@code edu.montana.notch.console.BatSyntaxKeywordDriftTest}.
 */
class ErrorIndexDocsDriftTest {

    // Tests run with the working directory at the repo root (see NotchShellEndToEndTest).
    private static final Path DATA = Path.of("docs/_data/error_codes.yml");

    // Entries keep a fixed two-line shape so a YAML parser is not a test dependency:
    //   - code: "EP0001"
    //     title: "expected condition after 'if' operator"
    private static final Pattern ENTRY = Pattern.compile(
            "^- code: \"([A-Z]{2}\\d{4})\"[ \\t]*\\R  title: \"(.*)\"[ \\t]*$",
            Pattern.MULTILINE);

    static boolean dataFileExists() {
        return Files.exists(DATA);
    }

    private static Map<String, String> documentedEntries() throws IOException {
        var entries = new LinkedHashMap<String, String>();
        ENTRY.matcher(Files.readString(DATA))
                .results()
                .forEach(match -> entries.put(match.group(1), match.group(2)));
        assertTrue(!entries.isEmpty(),
                "No entries parsed from " + DATA.toAbsolutePath()
                        + ". Did the two-line '- code:' / '  title:' entry shape change?");
        return entries;
    }

    @Test
    void parserCodeNamesAreWellFormedAndAscending() {
        int previous = 0;
        for (var error : ParserError.values()) {
            assertTrue(error.name().matches("EP\\d{4}"),
                    "ParserError constant names must be the code itself, e.g. EP0001. Got: "
                            + error.name());
            int number = Integer.parseInt(error.name().substring(2));
            assertTrue(number > previous,
                    "Codes are allocate-only: declare them in ascending order and never reuse "
                            + "a number. " + error.name() + " follows EP"
                            + "%04d".formatted(previous));
            previous = number;
        }
    }

    @Test
    @EnabledIf("dataFileExists")
    void everyParserErrorHasADocsEntryInDeclarationOrder() throws IOException {
        var expected = new ArrayList<String>();
        for (var error : ParserError.values()) {
            expected.add(error.name());
        }
        var documented = List.copyOf(documentedEntries().keySet());

        assertEquals(expected, documented,
                DATA + " must list every ParserError constant exactly once, in declaration "
                        + "order. Add the new code's entry (code, title, summary, example, fix).");
    }

    @Test
    @EnabledIf("dataFileExists")
    void nonTemplatedTitlesMatchTheEnumVerbatim() throws IOException {
        var documented = documentedEntries();
        for (var error : ParserError.values()) {
            // Templated messages interpolate a token, so their documented title has to be
            // a readable generalisation rather than the raw template. Fixed messages must
            // match exactly, or the page would describe an error in words the parser never
            // prints.
            if (error.template().contains("%")) {
                continue;
            }
            assertEquals(error.template(), documented.get(error.name()),
                    "Docs title for " + error.name() + " has drifted from the message "
                            + "ParserError emits.");
        }
    }
}
