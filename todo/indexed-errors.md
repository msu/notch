# Indexed Errors

**Parser codes are done** (2026-07-29). See `local/futureFixs/error-index-plan.md` for the design and
the remaining stages.

Notation settled as `E` + subsystem letter + 4 digits, not the `PXXXX`/`TXXXX`/`EXXXX`
sketched below:
- **EP0001-EP0026** - parse errors. Implemented in `errors/ParserError.java`, rendered in
  the diagnostic header, documented in `docs/_data/error_codes.yml` and published at
  `/errors/`. Kept honest by `ErrorIndexDocsDriftTest`.
- **ETXXXX** - tokenization errors. Not started; the ~14 sites are inline with no handler
  class, so this begins by extracting a `TokenizerErrorHandler`.
- **EEXXXX** - logic/runtime errors. Not started.

Codes are allocate-only: never renumbered, never reused.

## Still open

- An API for extensions to register new errors, namespaced per extension
  (`myextension:E0001`). The `DiagnosticCode` interface is deliberately unsealed to admit
  this; no registry is built yet. See `local/futureFixs/extension-codes-todo.md` for the test that
  should lock the extension point down.

