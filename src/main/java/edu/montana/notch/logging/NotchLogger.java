package edu.montana.notch.logging;

import org.slf4j.Marker;
import org.slf4j.event.Level;
import org.slf4j.helpers.LegacyAbstractLogger;

import java.io.PrintStream;

public class NotchLogger extends LegacyAbstractLogger {

    private final NotchLoggingConfig config;

    public NotchLogger(String name, NotchLoggingConfig config) {
        this.name = name;
        this.config = config;
    }

    @Override
    protected String getFullyQualifiedCallerName() {
        return null;
    }

    @Override
    protected void handleNormalizedLoggingCall(Level level, Marker marker, String messagePattern, Object[] arguments, Throwable throwable) {
        if (config.shouldLog(this.name, level, marker, messagePattern, arguments, throwable)) {
            String msg = config.formatMessage(this.name, level, marker, messagePattern, arguments, throwable);
            PrintStream stream = config.getStreamFor(level);
            stream.println(msg);
            if(throwable != null) {
                throwable.printStackTrace(stream);
            }
            stream.flush();
        }
    }

    @Override
    public boolean isTraceEnabled() {
        return config.isTraceEnabled(name);
    }

    @Override
    public boolean isDebugEnabled() {
        return config.isDebugEnabled(name);
    }

    @Override
    public boolean isInfoEnabled() {
        return config.isInfoEnabled(name);
    }

    @Override
    public boolean isWarnEnabled() {
        return config.isWarnEnabled(name);
    }

    @Override
    public boolean isErrorEnabled() {
        return config.isErrorEnabled(name);
    }
}
