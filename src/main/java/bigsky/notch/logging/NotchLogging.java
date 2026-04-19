package bigsky.notch.logging;

import org.slf4j.ILoggerFactory;
import org.slf4j.Logger;
import org.slf4j.event.Level;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class NotchLogging implements ILoggerFactory {

    public enum Mode {
        /** Default. Delegate to another SLF4J provider if one is present; otherwise log through Notch. */
        AUTO,
        /** Opt-out: if no other provider is present, fall back to the SLF4J NOP logger instead of Notch. */
        DISABLED,
        /** Force Notch to be the active backend, even when another provider is present. */
        FORCE,
    }

    /** System property alternative to {@link #disable()}, for cases where static-init ordering is hard. */
    public static final String DISABLE_PROPERTY = "notch.logging.disabled";

    private final ConcurrentMap<String, Logger> loggerMap = new ConcurrentHashMap<>();
    private final NotchLoggingConfig config = new NotchLoggingConfig();
    private static final NotchLogging INSTANCE = new NotchLogging();
    private static volatile Mode mode = Mode.AUTO;

    private NotchLogging() {}

    public static NotchLogging get() {
        return INSTANCE;
    }

    public static NotchLoggingConfig getConfig() {
        return get().config;
    }

    public static void runAtLevel(Level level, Runnable runnable) {
        getConfig().runAtLevel(level, runnable);
    }

    /**
     * Opt out of Notch-as-default. When no other SLF4J provider is present,
     * the binding falls back to the standard NOP logger instead of Notch.
     * When another provider is present, it wins either way.
     *
     * Must be called before the first {@code LoggerFactory.getLogger(...)} call
     * — typically at the top of {@code main()} or in a static initializer of
     * the entry-point class. The {@value #DISABLE_PROPERTY} system property
     * has the same effect and is safer when static-init ordering is tricky.
     */
    public static NotchLoggingConfig disable() {
        mode = Mode.DISABLED;
        return getConfig();
    }

    /**
     * Force Notch to be the active backend, even when another SLF4J provider
     * is on the classpath. Same timing requirements as {@link #disable()}.
     */
    public static NotchLoggingConfig install() {
        mode = Mode.FORCE;
        return getConfig();
    }

    /** Resolved mode after honoring the {@value #DISABLE_PROPERTY} system property. */
    static Mode effectiveMode() {
        if (mode == Mode.AUTO && "true".equalsIgnoreCase(System.getProperty(DISABLE_PROPERTY))) {
            return Mode.DISABLED;
        }
        return mode;
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

