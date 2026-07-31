# REPL
* identifier completion: walk `NotchRuntime` scope in `PicocliCompleter`
* fix unreachable parser fallback: switch `NotchParser.java:498` `RuntimeException` to `ParseException` so errors are `Spanned`

# Website Documentation
The site was rewritten into four collections (Language, Notch
Templates, REPL) that still need review.

Remaining:
* ~~Error index page~~ — done 2026-07-29, published at `/errors/`. Follow-ups and the
  `ET`/`EE` roadmap are in `local/futureFixs/error-index-plan.md`.
* Codes for the 56 uncoded `NotchParser` `require*` sites (EP0027+) — plan in
  `local/futureFixs/uncoded-parser-errors-plan.md`. Takes `/errors/` from 26 to 71 entries and
  retires the `UNCODED` markers in the parser tests.

# Language Features
* ~~Needs to at least support classes/objects~~ — done (`statements/NotchClassDeclaration.java`)
* `Foo.new()` for constructors (may actually already do this)
* Spec on null/undefined behaviors
* The `Notch` class should have an API making it easy to eval/exec notch code, create notch classes, etc.
  * Partly done: `edu.montana.notch.Notch` exposes `eval` / `render`

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
*Refreshed 2026-07-28. Regenerate with:*
`grep -rn "TODO" src/main/java/edu/montana/notch --include="*.java" | grep -v picocli`

* `NotchParser.java:1208` — `else if` chaining detection
* `token/FStringTokenData.java:50,58` — embed a recoverable error so tokenization continues
* `types/coercions/Coercion.java:8` — coercion priority + thread safety
* `runtime/NotchClass.java:54` — check arg count vs header fields, diagnostic-throw
* `statements/NotchIndexAssignment.java:45` — replace with proper coercions
* `expressions/NotchIndexExpression.java:29,49` — flexibility + proper coercions
* `expressions/NotchPropertyAccessExpression.java:113` — move into runtime to support imports
* `console/commands/LoadCommand.java:26` — temp fix, dig into the jline parser
* Templates: `javalin/NotchJavalinRenderer.java:45`, `NotchTemplateParser.java:184`,
  `runtime/NotchTemplateHelper.java:81`, `ast/QualifiedIdent.java:45`

Gone since the last pass: the `NotchParser` parse-error-recovery TODO and the
`NotchClosure` return-statement TODO.
