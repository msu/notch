# REPL
* identifier completion: walk `NotchRuntime` scope in `PicocliCompleter`
* fix unreachable parser fallback: switch `NotchParser.java:498` `RuntimeException` to `ParseException` so errors are `Spanned`

# Website Documentation

# Language Features

# Plugin
* fix jline dumb-terminal loading in REPL

# Executable

# Editor Tooling

# Online Demo

# Existing TODOs (in source)
* `NotchParser.java:497` — better parse-error recovery
* `NotchParser.java:605` — `else if` chaining detection
* `NotchClosure.java:34` — return-statement support
* `NotchIndexExpression.java:29,49` — coercion + property lookup hardening
* `NotchPropertyAccess.java:83` — move into runtime to support imports
* `chisel/type/coercions/Coercion.java:8` — coercion priority + thread safety
