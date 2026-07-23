package edu.montana.notch.util;

import java.util.Arrays;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class Text {
    public static String repr(Object obj) {
        return repr("" + obj);
    }

    public static String repr(String s) {
        if (s == null) return "null";
        final var out = new StringBuilder(s.length() + 2);
        out.append('"');
        for (char c : s.toCharArray()) {
            repr(out, c);
        }
        out.append('"');
        return out.toString();
    }

    public static String repr(char c) {
        var out = new StringBuilder(3);
        out.append("'");
        repr(out, c);
        out.append("'");
        return out.toString();
    }

    private static void repr(StringBuilder sb, char c) {
        if (c == '"') sb.append("\\\"");
        else if (c == '\'') sb.append("\\'");
        else if (c == '\\') sb.append("\\\\");
        else if (c == '\b') sb.append("\\b");
        else if (c == '\n') sb.append("\\n");
        else if (c == '\t') sb.append("\\t");
        else if (c == '\f') sb.append("\\f");
        else if (c == '\r') sb.append("\\r");
        else if (c < 32 || c > 0x7F) {
            sb.append("\\u{").append(Integer.toHexString(c)).append("}");
        } else {
            sb.append(c);
        }
    }


    public static String decapitalize(String name) {
        if (name == null || name.isEmpty()) {
            return name;
        }
        char[] chars = name.toCharArray();
        chars[0] = Character.toLowerCase(chars[0]);
        return new String(chars);
    }

    public static String toKebabCase(String name) {
        if (name == null || name.isEmpty()) {
            return name;
        }
        String result = name.replace('_', '-');

        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < result.length(); i++) {
            char c = result.charAt(i);

            if (Character.isUpperCase(c) && i > 0) {
                char prev = result.charAt(i - 1);
                boolean nextIsLower = (i + 1 < result.length()) && Character.isLowerCase(result.charAt(i + 1));

                if (Character.isLowerCase(prev) || Character.isDigit(prev) ||
                        (Character.isUpperCase(prev) && nextIsLower)) {
                    builder.append('-');
                }
            }
            builder.append(Character.toLowerCase(c));
        }

        return builder.toString();
    }

    public static String indent(int spaces, String str) {
        return Arrays.stream(str.split("\n"))
                .map(s -> " ".repeat(spaces) + s)
                .collect(Collectors.joining("\n"));
    }

    public static String capitalize(String name) {
        if (name == null || name.isEmpty()) {
            return name;
        }
        char[] chars = name.toCharArray();
        chars[0] = Character.toUpperCase(chars[0]);
        return new String(chars);
    }

    public static String snakeCase(String camelCaseString) {
        StringBuilder result = new StringBuilder();
        char[] charArray = camelCaseString.toCharArray();
        for (int i = 0; i < charArray.length; i++) {
            char c = charArray[i];
            if (Character.isUpperCase(c)) {
                if (i != 0) {
                    result.append("_");
                }
                result.append(Character.toLowerCase(c));
            } else {
                result.append(c);
            }
        }
        return result.toString();
    }

    public static String camelCase(String snakeCaseString) {
        StringBuilder result = new StringBuilder();
        char[] charArray = snakeCaseString.toCharArray();
        for (int i = 0; i < charArray.length; i++) {
            char c = charArray[i];
            if (i == 0) {
                result.append(c);
            } else {
                if (c == '_') {
                    if (i + 1 < charArray.length && charArray[i + 1] != '_') {
                        i++;
                        result.append(Character.toUpperCase(charArray[i]));
                    } else {
                        result.append(c);
                    }
                } else {
                    result.append(c);
                }
            }
        }
        return result.toString();
    }

    // an extremely simplified english pluralization algorithm based
    // on https://blob.perl.org/tpc/1998/User_Applications/Algorithmic%20Approach%20Plurals/Algorithmic_Plurals.html
    static LinkedHashMap<Pattern, String> INFLECTIONS = new LinkedHashMap<>();

    private static void addInflection(String suffix, String replacement) {
        INFLECTIONS.put(Pattern.compile(".*" + suffix + "$"), replacement);
    }

    static {
        addInflection("s(h)", "hes");
        addInflection("[ch](h)", "hes");
        addInflection("(ss)", "sses");
        addInflection("[aeo]l(f)", "ves");
        addInflection("[^d]ea(f)", "ves");
        addInflection("ar(f)", "ves");
        addInflection("[nlw]i(fe)", "ves");
        addInflection("[aeiou](y)", "ys");
        addInflection("(y)", "ies");
    }

    public static String pluralize(String noun) {
        for (Map.Entry<Pattern, String> inflection : INFLECTIONS.entrySet()) {
            Matcher matcher = inflection.getKey().matcher(noun);
            if (matcher.matches()) {
                StringBuilder result = new StringBuilder(noun);
                result.replace(matcher.start(1), matcher.end(1), inflection.getValue());
                return result.toString();
            }
        }
        return noun + "s"; // default to appending 's'
    }

    public static String deSnakeCase(String str) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < str.length(); i++) {
            char ch = str.charAt(i);
            if ('_' == ch) {
                if (i + 1 < str.length()) {
                    i = i + 1;
                    sb.append(Character.toUpperCase(str.charAt(i)));
                } else {
                    sb.append(ch);
                }
            } else {
                sb.append(ch);
            }
        }
        return sb.toString();
    }

    public static boolean isLowerCase(String str) {
        for (int i = 0; i < str.length(); i++) {
            if (!Character.isLowerCase(str.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    public static boolean isDigit(char c) {
        return c >= '0' && c <= '9';
    }

    public static boolean isHexDigit(char c) {
        return isDigit(c) || (c >= 'a' && c <= 'f') || (c >= 'A' && c <= 'F');
    }

    public static int hexValue(char c) {
        assert isHexDigit(c);
        return Integer.parseInt(c + "", 16);
    }

    public static void mapLines(StringBuilder sb, String content, Function<String, String> mapper) {
        var it = content.lines().iterator();
        while (!it.hasNext()) {
            var line = it.next();
            String updatedContent = mapper.apply(line);
            sb.append(updatedContent).append('\n');
        }
    }

    public static String center(int n, Object v) {
        String inner = "" + v;
        if (inner.length() >= n) return inner;

        int padding = n - inner.length();
        int right = padding / 2;
        int left = padding - right;
        return " ".repeat(left) + inner + " ".repeat(right);
    }

    public static BetterList<String> getLines(String content, int startLine, int endLine) {
        var out = new BetterList<String>();

        var it = content.lines().iterator();
        for (int i = 0; i < startLine; i++) {
            if (!it.hasNext()) {
                return out;
            }
            it.next();
        }

        for (int i = 0; i < endLine - startLine + 1; i++) {
            if (!it.hasNext()) {
                return out;
            }

            String line = it.next();
            out.add(line);
        }
        return out;
    }

    private static final Pattern EMAIL_PATTERN = Pattern.compile("(?:[a-z0-9!#$%&'*+/=?^_`{|}~-]+(?:\\.[a-z0-9!#$%&'*+/=?^_`{|}~-]+)*|\"(?:[\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x21\\x23-\\x5b\\x5d-\\x7f]|\\\\[\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f])*\")@(?:(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\\.)+[a-z0-9](?:[a-z0-9-]*[a-z0-9])?|\\[(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?|[a-z0-9-]*[a-z0-9]:(?:[\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x21-\\x5a\\x53-\\x7f]|\\\\[\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f])+)\\])");

    public static boolean isValidEmailAddress(String emailAddress) {
        return EMAIL_PATTERN.matcher(emailAddress).matches();
    }

    public static String simpleClassName(Object obj) {
        return obj == null ? "<null>" : obj.getClass().getSimpleName();
    }

    public static String base64Encode(String value) {
        return Base64.getEncoder().encodeToString(value.getBytes());
    }

    public static String closest(String target, Iterable<String> candidates, int maxDistance) {
        String t = target.toLowerCase();
        String best = null;
        int min = Integer.MAX_VALUE;
        for (String c : candidates) {
            int d = levenshteinDistance(t, c.toLowerCase());
            if (d < min) {
                min = d;
                best = c;
            }
        }
        return min <= maxDistance ? best : null;
    }

    public static int levenshteinDistance(String s1, String s2) {
        int[][] dp = new int[s1.length() + 1][s2.length() + 1];

        for (int i = 0; i <= s1.length(); i++) {
            dp[i][0] = i;
        }
        for (int j = 0; j <= s2.length(); j++) {
            dp[0][j] = j;
        }

        for (int i = 1; i <= s1.length(); i++) {
            for (int j = 1; j <= s2.length(); j++) {
                if (s1.charAt(i - 1) == s2.charAt(j - 1)) {
                    dp[i][j] = dp[i - 1][j - 1];
                } else {
                    dp[i][j] = 1 + Math.min(dp[i - 1][j - 1],
                            Math.min(dp[i - 1][j], dp[i][j - 1]));
                }
            }
        }

        return dp[s1.length()][s2.length()];
    }
}
