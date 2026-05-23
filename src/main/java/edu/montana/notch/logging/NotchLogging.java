package edu.montana.notch.logging;

import org.slf4j.ILoggerFactory;
import org.slf4j.Logger;
import org.slf4j.event.Level;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class NotchLogging implements ILoggerFactory {

    private final ConcurrentMap<String, Logger> loggerMap = new ConcurrentHashMap<>();
    private final NotchLoggingConfig config = new NotchLoggingConfig();
    private static final NotchLogging INSTANCE = new NotchLogging();

    private NotchLogging() {
    }

    public static NotchLogging get() {
        return INSTANCE;
    }

    public static NotchLoggingConfig getConfig() {
        return get().config;
    }

    public static void runAtLevel(Level level, Runnable runnable) {
        getConfig().runAtLevel(level, runnable);
    }

    public Logger getLogger(String name) {
        if (config.overrideService != null) {
            return config.overrideService.getLoggerFactory().getLogger(name);
        } else {
            return loggerMap.computeIfAbsent(name, this::createLogger);
        }
    }

    /**
     * Actually creates the logger for the given name.
     */
    protected Logger createLogger(String name) {
        return new NotchLogger(name, this.getConfig());
    }
}

