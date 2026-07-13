# REPL
* identifier completion: walk `NotchRuntime` scope in `PicocliCompleter`
* fix unreachable parser fallback: switch `NotchParser.java:498` `RuntimeException` to `ParseException` so errors are `Spanned`

# Website Documentation
* Needs rewrite with proper documentation: installation, overview, etc.
* Top level nav should include notch templates
  * Notch templates are a flagship feature, needs good docs for 431 usage
* Needs syntax highlighting

# Language Features
* Needs to at least support classes/objects
* `Foo.new()` for constructors (may actually already do this)
* Spec on null/undefined behaviors
* The `Notch` class should have an API making it easy to eval/exec notch code, create notch classes, etc.

# Executable
* Need to figure out how to ship a notch.exe that supports the langauge (can't use graal)

# Editor Tooling
* Basic intellij editor plugin w/auto complete
* Stretch goal: type inference w/dot completion for props/funcs
* Stretch goal: suggest idiomatic usages
* Fix jline dumb-terminal loading in REPL

# Online Demo
* Would need to build an entire javascript implementation (doable w/claude, but work and would need to keep in sync)

# Existing TODOs (in source)
* `NotchParser.java:497` — better parse-error recovery
* `NotchParser.java:605` — `else if` chaining detection
* `NotchClosure.java:34` — return-statement support
* `NotchIndexExpression.java:29,49` — coercion + property lookup hardening
* `NotchPropertyAccess.java:83` — move into runtime to support imports
* `chisel/type/coercions/Coercion.java:8` — coercion priority + thread safety
