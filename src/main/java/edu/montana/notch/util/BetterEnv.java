package edu.montana.notch.util;

import edu.montana.notch.json5.JSON5Array;
import edu.montana.notch.json5.JSON5Object;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Modifier;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.logging.Logger;

public class BetterEnv {
    private static final org.slf4j.Logger log = LoggerFactory.getLogger(BetterEnv.class);

    protected <T> Option<T> option(String name, Class<T> clazz) {
        return new Option<>(name, clazz);
    }

    private static final Logger LOGGER = Logger.getLogger(BetterEnv.class.getName());

    private static <T> void loadOption(Option<T> option, EnvProvider... providers) {
        for (var provider : providers) {
            var rawValue = provider.getEnvValue(option.name);

            if (rawValue == null) continue;

            T value;
            if (option.converter != null) {
                try {
                    value = option.converter.apply(rawValue, option.type);
                } catch (Exception e) {
                    var msg = "unable to convert value for option %s : %s".formatted(Text.repr(option.name), Text.repr(rawValue));
                    throw new RuntimeException(msg, e);
                }
            } else {
                value = convert(option, rawValue);
            }

            option.provider = provider.name();
            option.value = value;

            LOGGER.fine("Loaded option " + Text.repr(option.name) + " from " + provider.name());
            return;
        }

        if (!option.hasDefault) {
            var msg = "failed to load config option %s, no value or default-value specified".formatted(Text.repr(option.name));
            LOGGER.severe(msg);
            throw new RuntimeException(msg);
        } else {
            option.provider = "[Default Value]";
            option.value = option.defaultValue;
        }
    }

    private BetterList<Option<?>> options;
    public BetterList<Option<?>> getOptions() {
        if (options == null) {
            options = new BetterList<>();
            var clazz = getClass();
            for (var field : clazz.getFields()) {
                int mods = field.getModifiers();

                if (Modifier.isStatic(mods)) {
                    log.warn("{}#{}: option is static, it will not be parsed!", getClass().getName(), field.getName());
                    continue;
                }

                if (!Modifier.isPublic(mods)) {
                    log.warn("{}#{}: option is not public, it will not be parsed!", getClass().getName(), field.getName());
                    continue;
                }


                var option = Exceptions.safely(() -> ((Option<?>) field.get(this)));
                if (option == null) {
                    throw new RuntimeException("%s#%s: option is null, it will not be parsed!".formatted(getClass().getName(), field.getName()));
                }

                options.add(option);
            }
        }
        return options;
    }

    protected void load(EnvProvider... providers) {
        for (var option : getOptions()) {
            loadOption(option, providers);
        }
    }

    private static <T> T convert(Option<T> opt, String rawValue) {
        if (opt.type == String.class) {
            return opt.type.cast(rawValue);
        } else if (opt.type == Integer.class) {
            return opt.type.cast(Integer.valueOf(rawValue));
        } else if (opt.type == Long.class) {
            return opt.type.cast(Long.valueOf(rawValue));
        } else if (opt.type == Short.class) {
            return opt.type.cast(Short.valueOf(rawValue));
        } else if (opt.type == Byte.class) {
            return opt.type.cast(Byte.valueOf(rawValue));
        } else if (opt.type == Boolean.class) {
            return opt.type.cast(Boolean.valueOf(rawValue));
        } else if (opt.type == Double.class) {
            return opt.type.cast(Double.valueOf(rawValue));
        } else if (opt.type == Float.class) {
            return opt.type.cast(Float.valueOf(rawValue));
        } else if (opt.type == JSON5Object.class) {
            var value = JSON5.parseObject(opt.name, rawValue);
            return opt.type.cast(value);
        } else if (opt.type == JSON5Array.class) {
            var value = JSON5.parseArray(opt.name, rawValue);
            return opt.type.cast(value);
        } else if (opt.type.isEnum()) {
            return Exceptions.safely(() -> {
                var valueOfMethod = opt.type.getMethod("valueOf", String.class);
                Object value = valueOfMethod.invoke(null, rawValue);
                return opt.type.cast(value);
            });
        } else {
            throw new IllegalArgumentException(opt.name);
        }
    }

    public static void loadEnvFile(String filename, Map<String, String> dest) {
        var filepath = Path.of(filename);
        if (!Files.exists(filepath)) return;
        var lines = Exceptions.safely(() -> Files.readAllLines(filepath));

        for (int i = 0; i < lines.size(); i++) {
            var line = lines.get(i).trim();
            if (line.isEmpty()) return; // skip blank lines
            if (line.startsWith("#")) return; // skip commented lines

            var parts = line.split("=", 2);
            if (parts.length != 2) {
                throw new IllegalArgumentException("Invalid line format (%s:%d), expected `key = value` (whitespace agnostic): ".formatted(filepath, 1 + i));
            }

            var key = parts[0].trim();
            var value = parts[1].trim();
            dest.put(key, value);
        }
    }

    public static EnvFileProvider envFile(String filepath) {
        return new EnvFileProvider(filepath);
    }

    public static final class EnvFileProvider implements EnvProvider {
        public final Path path;
        private final BetterMap<String, String> values = new BetterMap<>();

        public EnvFileProvider(String filepath) {
            this(Paths.get(filepath));
        }

        public EnvFileProvider(Path path) {
            this.path = Objects.requireNonNull(path);
            load();
        }

        public void load() {
            values.clear();

            if (!Files.exists(path)) return;
            var lines = Exceptions.safely(() -> Files.readAllLines(path));

            for (int i = 0; i < lines.size(); i++) {
                var line = lines.get(i).trim();
                if (line.isEmpty()) continue; // skip blank lines
                if (line.startsWith("#")) continue; // skip commented lines

                var parts = line.split("=", 2);
                if (parts.length != 2) {
                    throw new IllegalArgumentException("Invalid line format (%s:%d), expected `key = value` (whitespace agnostic): ".formatted(path, 1 + i));
                }

                var key = parts[0].trim();
                var value = parts[1].trim();
                values.put(key, value);
            }
        }

        @Override
        public String name() {
            return Text.repr(path) + " file";
        }

        @Override
        public String getEnvValue(String key) {
            return values.get(key);
        }
    }

    public static SystemProvider system() {
        return SystemProvider.INSTANCE;
    }

    public static final class SystemProvider implements EnvProvider {
        private static final SystemProvider INSTANCE = new SystemProvider();

        private SystemProvider() {}

        @Override
        public String name() {
            return "System.getenv (System Environment)";
        }

        @Override
        public String getEnvValue(String key) {
            return System.getenv(key);
        }
    }

    public static class Option<T> {
        public final String name;
        public final Class<T> type;
        private String description;
        private final BiFunction<String, Class<T>, T> converter;

        private T value;
        private String provider;

        private boolean hasDefault = false;
        private T defaultValue = null;

        public Option(String name, Class<T> type) {
            this.name = Objects.requireNonNull(name);
            this.type = type;
            this.converter = null;
        }

        public Option(String name, Class<T> type, BiFunction<String, Class<T>, T> converter) {
            this.name = Objects.requireNonNull(name);
            this.type = type;
            this.converter = converter;
        }

        public Option<T> withDefault(T defaultValue) {
            if (this.hasDefault) throw new AssertionError("default value already set for option " + Text.repr(name));
            this.defaultValue = defaultValue;
            this.hasDefault = true;
            return this;
        }

        public Option<T> withNullDefault() {
            return withDefault(null);
        }

        public Option<T> withDescription(String description) {
            if (this.description != null) throw new AssertionError("description already set for option " + Text.repr(name));
            this.description = description;
            return this;
        }

        public void override(T value) {
            StackTraceElement[] trace = Thread.currentThread().getStackTrace();
            var caller = trace[trace.length - 2];

            this.provider = "[OVERRIDE %s#%s:%d]".formatted(caller.getClassName(), caller.getMethodName(), caller.getLineNumber());
            this.value = value;
        }

        public String getProvider() {
            return provider;
        }

        public T value() {
            return value;
        }

        public boolean hasDefault() {
            return hasDefault;
        }

        public T defaultValue() {
            return defaultValue;
        }

        public String getDescription() {
            return description;
        }
    }

    public interface EnvProvider {
        String name();
        String getEnvValue(String key);
    }
}
