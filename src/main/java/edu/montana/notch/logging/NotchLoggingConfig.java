package edu.montana.notch.logging;

import org.jline.jansi.Ansi;
import org.slf4j.MDC;
import org.slf4j.Marker;
import org.slf4j.event.Level;
import org.slf4j.helpers.MessageFormatter;
import org.slf4j.spi.SLF4JServiceProvider;
import java.io.PrintStream;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.TreeMap;

import static org.jline.jansi.Ansi.ansi;

public class NotchLoggingConfig {

    public static final String REQUEST_ID_KEY = "request_id";
    public static final String IP_KEY = "ip";

    private static final int LEVEL_SIZE = 8;
    private static final int NEWLINE_INDENT = 2;
    private Level configuredGlobalLogLevel;
    SLF4JServiceProvider overrideService;
    private boolean showTimestamp = false;
    private boolean showFullName= false;
    private boolean showShortName = true;
    private boolean noColors = false;
    private boolean flatten = false;
    private boolean includeRequestId = false;
    private boolean includeServerIP = false;
    boolean logIndentSupported = false;
    private boolean sanitizeLogging = true;

    // background logging support
    volatile Runnable onFirstBackgroundLog = null;
    boolean backgroundLogs = false;
    StringBuffer backgroundBuffer = new StringBuffer();

    static ThreadLocal<Integer> LOG_INDENT = new ThreadLocal<>();
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSX");

    // ========================================================
    // configuration helpers
    // ========================================================

    public NotchLoggingConfig defaultLevel(Level level) {
        configuredGlobalLogLevel = level;
        return this;
    }

    public NotchLoggingConfig showTimestamp() {
        this.showTimestamp = true;
        return this;
    }

    public NotchLoggingConfig showFullLoggerName() {
        showFullName = true;
        return this;
    }

    public NotchLoggingConfig noColors() {
        noColors = true;
        return this;
    }

    public NotchLoggingConfig flatten() {
        flatten = true;
        return this;
    }

    public NotchLoggingConfig includeRequestId() {
        includeRequestId = true;
        return this;
    }

    public NotchLoggingConfig includeServerIP() {
        includeServerIP = true;
        return this;
    }

    public NotchLoggingConfig enableSanitization() {
        sanitizeLogging = true;
        return this;
    }

    public NotchLoggingConfig disableSanitization() {
        sanitizeLogging = false;
        return this;
    }

    void runAtLevel(Level level, Runnable runnable) {
        if(configuredGlobalLogLevel == null) {
            Level tmp = configuredGlobalLogLevel;
            try {
                configuredGlobalLogLevel = level;
                runnable.run();
            } finally {
                configuredGlobalLogLevel = tmp;
            }
        } else {
            runnable.run();
        }
    }

    public void captureLogsInBackground(Runnable onFirstLog) {
        backgroundLogs = true;
        onFirstBackgroundLog = onFirstLog;
        backgroundBuffer = new StringBuffer();
    }

    public String takeBackgroundLogs() {
        String currentLogs = backgroundBuffer.toString();
        backgroundBuffer.delete(0, backgroundBuffer.length());
        return currentLogs;
    }

    // ========================================================
    // logger helpers
    // ========================================================
    public boolean isTraceEnabled(String name) {
        return getGlobalLogLevel().toInt() <= Level.TRACE.toInt();
    }

    public boolean isDebugEnabled(String name) {
        return getGlobalLogLevel().toInt() <= Level.DEBUG.toInt();
    }

    public boolean isInfoEnabled(String name) {
        return getGlobalLogLevel().toInt() <= Level.INFO.toInt();
    }

    public boolean isWarnEnabled(String name) {
        return getGlobalLogLevel().toInt() <= Level.WARN.toInt();
    }

    public boolean isErrorEnabled(String name) {
        return getGlobalLogLevel().toInt() <= Level.ERROR.toInt();
    }

    public NotchLoggingConfig overrideService(SLF4JServiceProvider slf4JServiceProvider) {
        this.overrideService = slf4JServiceProvider;
        return this;
    }

    public boolean shouldLog(String name, Level level, Marker marker, String messagePattern, Object[] arguments, Throwable throwable) {
        if(backgroundLogs) {
            String msg = formatMessage(name, level, marker, messagePattern, arguments, throwable);
            synchronized (this) {
                if (backgroundBuffer.isEmpty()) {
                    onFirstBackgroundLog.run();
                }
            }
            backgroundBuffer.append(msg + '\n');
            return false;
        } else {
            return level.toInt() >= getGlobalLogLevel().toInt();
        }
    }

    private Level getGlobalLogLevel() {
        if(configuredGlobalLogLevel != null) {
            return configuredGlobalLogLevel;
        } else {
            return Level.INFO;
        }
    }

    // ========================================================
    // core logging methods
    // ========================================================

    public String formatMessage(String name, Level level, Marker marker, String messagePattern, Object[] arguments, Throwable throwable) {
        String formattedMessage = MessageFormatter.basicArrayFormat(messagePattern, arguments);
        if (sanitizeLogging) {
            formattedMessage = SensitiveDataSanitizer.sanitize(formattedMessage);
        }
        return formatDebugMessage(name, level, marker, formattedMessage, throwable);
    }

    private String formatDebugMessage(String name, Level level, Marker marker, String message, Throwable throwable) {
        String levelString = String.format("%8s", "[" + level + "] ");
        Ansi msg = getColorizer();

        int firstLineOffset = 0;
        int nextLineOffsets = 0;
        if (logIndentSupported) {
            Integer offset = LOG_INDENT.get();
            if(offset != null) {
                firstLineOffset = offset;
                nextLineOffsets = offset;
            }
        }

        if(showTimestamp) {
            ZonedDateTime now = LocalDateTime.now().atZone(ZoneOffset.systemDefault());
            String formatted = formatter.format(now);
            msg.a(formatted).a(" ");
            nextLineOffsets += formatted.length();
        }

        if (level == Level.INFO) {
            msg.fgBrightBlue();
        } else if (level == Level.DEBUG || level == Level.TRACE) {
            msg.fgGreen();
        } else if (level == Level.WARN) {
            msg.fgBrightYellow();
        } else if (level == Level.ERROR) {
            msg.fgBrightRed();
        }
        msg.a(levelString).reset();

        msg.a(" ".repeat(firstLineOffset));

        if(showFullName) {
            msg.a(name).a(" | ");
        } else if(showShortName) {
            msg.a(shortenName(name)).a(" | ");
        }

        TreeMap<String, String> map = null;
        if (includeRequestId) {
            map = new TreeMap<>();
            String value = MDC.get(REQUEST_ID_KEY);
            if(value != null) {
                map.put(REQUEST_ID_KEY, value);
            }
        }
        if (includeServerIP) {
            if(map == null) {
                map = new TreeMap<>();
            }
            String ip = MDC.get(IP_KEY);
            if(ip != null) {
                map.put(IP_KEY, ip);
            }
        }
        if(map != null && !map.isEmpty()) {
            msg.append(map.toString()).append(" | ");
        }

        msg.a(normalize(message, nextLineOffsets));
        return msg.toString();
    }

    public Ansi getColorizer() {
        return noColors ? new NoColorsAnsi() : ansi();
    }

    private String shortenName(String name) {
        int i = name.lastIndexOf(".");
        if (i >= 0) {
            return name.substring(i + 1);
        } else {
            return name;
        }
    }

    private String normalize(String formattedMessage, int timestampOffset) {
        if (formattedMessage.indexOf("\n") > 0) {
            String[] split = formattedMessage.split("\n");
            StringBuilder normalized = new StringBuilder();
            for (int i = 0; i < split.length; i++) {
                String s = split[i];
                if (i == 0) {
                    normalized.append(s);
                } else {
                    if(flatten) {
                        normalized.append(" ").append(s);
                    } else {
                        normalized.append("\n");
                        normalized.append(" ".repeat(getNextLineOffset(timestampOffset))).append(s);
                    }
                }
            }
            return normalized.toString();
        } else {
            return formattedMessage;
        }
    }

    private int getNextLineOffset(int timestampOffset) {
        int offset = LEVEL_SIZE;
        offset += timestampOffset;
        offset += NEWLINE_INDENT;
        return offset;
    }

    public PrintStream getStreamFor(Level level) {
        if (level.toInt() <= Level.INFO.toInt()) {
            return System.out;
        } else {
            return System.err;
        }
    }

    public void pushLogIndent() {
        if(logIndentSupported) {
            Integer indent = LOG_INDENT.get();
            if(indent == null) {
                indent = 0;
            }
            indent += 2;
            LOG_INDENT.set(indent);
        }
    }

    public void popLogIndent() {
        if(logIndentSupported) {
            Integer indent = LOG_INDENT.get();
            if(indent == null) {
                indent = 0;
            }
            indent -= 2;
            LOG_INDENT.set(indent);
        }
    }

    public NotchLoggingConfig supportLogIndents() {
        logIndentSupported = true;
        return this;
    }

    private static class NoColorsAnsi extends Ansi {
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
