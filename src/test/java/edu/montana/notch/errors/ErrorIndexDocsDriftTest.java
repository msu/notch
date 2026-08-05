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

import static edu.montana.notch.NotchTestUtils.execDiagnostics;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ErrorIndexDocsDriftTest {

    private static final Path DATA = Path.of("docs/_data/error_codes.yml");

    private static final Pattern ENTRY = Pattern.compile(
            "^- code: \"([A-Z]{2}\\d{4})\"[ \\t]*\\R  title: \"(.*)\"[ \\t]*$",
            Pattern.MULTILINE);

    private static final Pattern EXAMPLE = Pattern.compile(
            "^- code: \"([A-Z]{2}\\d{4})\"[\\s\\S]*?^  example: \\|\\R([\\s\\S]*?)(?=^  fix:)",
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
    void parserCodeNamesAreTheCodeItself() {
        for (var error : ParserError.values()) {
            assertTrue(error.name().matches("EP\\d{4}"),
                    "ParserError constant names must be the code itself, e.g. EP0001. Got: "
                            + error.name());
        }
    }

    @Test
    void parserCodesAreDeclaredInAscendingOrder() {
        int previous = 0;
        for (var error : ParserError.values()) {
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
            if (error.template().contains("%")) {
                continue;
            }
            assertEquals(error.template(), documented.get(error.name()),
                    "Docs title for " + error.name() + " has drifted from the message "
                            + "ParserError emits.");
        }
    }

    @Test
    @EnabledIf("dataFileExists")
    void everyCodeHasAnExampleBlock() throws IOException {
        int found = 0;
        var matcher = EXAMPLE.matcher(Files.readString(DATA));
        while (matcher.find()) {
            found++;
        }
        assertEquals(ParserError.values().length, found,
                "expected one example block per code - has the '  example: |' shape changed?");
    }

    @Test
    @EnabledIf("dataFileExists")
    void everyDocumentedExampleStillProducesItsCode() throws IOException {
        var matcher = EXAMPLE.matcher(Files.readString(DATA));
        while (matcher.find()) {
            var code = matcher.group(1);
            var example = matcher.group(2).stripTrailing().replaceAll("(?m)^    ", "");
            var produced = new ArrayList<String>();
            for (var diagnostic : execDiagnostics(example)) {
                if (diagnostic.getCode() != null) produced.add(diagnostic.getCode().id());
            }
            assertTrue(produced.contains(code),
                    "The documented example for " + code + " no longer produces it. Emitted "
                            + produced + " for:\n" + example);
        }
    }
}
