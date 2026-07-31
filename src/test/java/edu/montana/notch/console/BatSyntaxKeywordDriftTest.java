package edu.montana.notch.console;

import edu.montana.notch.token.NotchTokenTypeKeyword;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIf;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Guards the bat syntax grammar against drifting out of sync with the real keyword list.
 * The grammar in local/editors/bat/notch.sublime-syntax hard-codes the keyword alternation for
 * highlighting; this test asserts that alternation matches
 * {@link NotchTokenTypeKeyword}'s authoritative set, so adding a keyword there fails the
 * build until the grammar is updated too.
 *
 * <p>Only the hard keywords are guarded. Soft/contextual keywords (and, or, starts, ends,
 * with, contains, empty, index, from) are parser-driven and have no single list to compare
 * against; they are documented in the grammar instead.
 */
class BatSyntaxKeywordDriftTest {

    // Tests run with the working directory at the repo root (see NotchShellEndToEndTest).
    private static final Path GRAMMAR = Path.of("local/editors/bat/notch.sublime-syntax");

    // The keyword.control alternation line: '\b(if|for|...|then)\b'
    private static final Pattern KEYWORD_ALTERNATION =
            Pattern.compile("'\\\\b\\(([a-z|]+)\\)\\\\b'\\s*\\n\\s*scope: keyword\\.control\\.notch");

    static boolean grammarExists() {
        return Files.exists(GRAMMAR);
    }

    @Test
    @EnabledIf("grammarExists")
    void grammarKeywordsMatchTokenizer() throws Exception {
        String grammar = Files.readString(GRAMMAR);

        Matcher matcher = KEYWORD_ALTERNATION.matcher(grammar);
        assertTrue(matcher.find(),
                "Could not find the hard-keyword alternation in " + GRAMMAR
                        + ". Expected a match rule of the form '\\b(if|for|...)\\b' scoped "
                        + "keyword.control.notch. Did the grammar's keyword rule change shape?");

        Set<String> grammarKeywords = new LinkedHashSet<>(Arrays.asList(matcher.group(1).split("\\|")));

        // NotchTokenTypeKeyword.keywords is the authoritative set the tokenizer uses.
        Set<String> tokenizerKeywords = NotchTokenTypeKeyword.NOTCH_KEYWORD.keywords;

        assertEquals(tokenizerKeywords, grammarKeywords,
                "Bat grammar keyword list has drifted from NotchTokenTypeKeyword. Update the "
                        + "keyword.control alternation in " + GRAMMAR + " to match "
                        + "NotchTokenTypeKeyword.keywords (missing from grammar / extra in grammar "
                        + "shown by the set diff above).");
    }
}
