package bigsky.notch;

import static bigsky.utils.BigSkyUtils.repr;

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
}
