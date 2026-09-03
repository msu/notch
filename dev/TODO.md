# REPL
* identifier completion: walk `NotchRuntime` scope in `PicocliCompleter`
* fix unreachable parser fallback: switch `NotchParser.java:498` `RuntimeException` to `ParseException` so errors are `Spanned`

# Website Documentation
* The site was rewritten into four collections (Language, Notch Templates, REPL) that still need review. 
* Codes for the 56 uncoded sites (EP0027+) Takes `/errors/` from 26 to 71 entries and
  retires the `UNCODED` markers in the parser tests.

# Language Features
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