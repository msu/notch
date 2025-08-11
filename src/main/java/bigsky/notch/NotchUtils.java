package bigsky.notch;

public class NotchUtils {
    public static boolean isIdentifierStartChar(char first) {
        if (Character.isAlphabetic(first)) return true;
        if (first == '_') return true;
        if (first == '$') return true;
        return false;
    }

    public static boolean isIdentifierChar(char first) {
        if (Character.isAlphabetic(first)) return true;
        if (Character.isDigit(first)) return true;
        if (first == '_') return true;
        if (first == '$') return true;
        return false;
    }

    public static boolean isIdentifier(String lex) {
        if (lex == null || lex.isEmpty()) return false;
        char[] chars = lex.toCharArray();
        if (!isIdentifierStartChar(chars[0])) return false;
        for (int i = 1; i < chars.length; i++) {
            char c = chars[i];
            if (!isIdentifierChar(c)) return false;
        }
        return true;
    }

    public static boolean isTerseCharacter(char c) {
        return Character.isAlphabetic(c) || Character.isDigit(c) || c == '_' || c == '-';
    }

    public static void requireIdentifier(String token, String message) throws IllegalArgumentException {
        if (!isIdentifier(token)) {
            throw new IllegalArgumentException(message + ": the token " + repr(token) + " is not an identifier");
        }
    }

    public static String repr(String s) {
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
}
