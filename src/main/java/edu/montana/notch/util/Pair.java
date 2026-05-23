package edu.montana.notch.util;

public record Pair<A, B>(A first, B second) {
    public static <A, B> Pair<A, B> pair(A a, B b) {
        return new Pair<>(a, b);
    }

    @Override
    public String toString() {
        return "(" + first + ", " + second + ")";
    }
}
