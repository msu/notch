package edu.montana.notch.logging;

import org.jline.jansi.Ansi;

public class LoggingUtils {

    // a no-op Ansi for non-colored output, taken from jansi
    private static class NoAnsi extends Ansi {
        public NoAnsi() {
        }
        public Ansi fg(Color color) {
            return this;
        }

        public Ansi bg(Color color) {
            return this;
        }

        public Ansi fgBright(Color color) {
            return this;
        }

        public Ansi bgBright(Color color) {
            return this;
        }

        public Ansi fg(int color) {
            return this;
        }

        public Ansi fgRgb(int r, int g, int b) {
            return this;
        }

        public Ansi bg(int color) {
            return this;
        }

        public Ansi bgRgb(int r, int g, int b) {
            return this;
        }

        public Ansi a(Attribute attribute) {
            return this;
        }

        public Ansi cursor(int row, int column) {
            return this;
        }

        public Ansi cursorToColumn(int x) {
            return this;
        }

        public Ansi cursorUp(int y) {
            return this;
        }

        public Ansi cursorRight(int x) {
            return this;
        }

        public Ansi cursorDown(int y) {
            return this;
        }

        public Ansi cursorLeft(int x) {
            return this;
        }

        public Ansi cursorDownLine() {
            return this;
        }

        public Ansi cursorDownLine(int n) {
            return this;
        }

        public Ansi cursorUpLine() {
            return this;
        }

        public Ansi cursorUpLine(int n) {
            return this;
        }

        public Ansi eraseScreen() {
            return this;
        }

        public Ansi eraseScreen(Erase kind) {
            return this;
        }

        public Ansi eraseLine() {
            return this;
        }

        public Ansi eraseLine(Erase kind) {
            return this;
        }

        public Ansi scrollUp(int rows) {
            return this;
        }

        public Ansi scrollDown(int rows) {
            return this;
        }

        public Ansi saveCursorPosition() {
            return this;
        }

        public Ansi restoreCursorPosition() {
            return this;
        }

        public Ansi reset() {
            return this;
        }
    }

}
