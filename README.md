# Notch - Yet Another Scripting Language

## Install

**macOS / Linux:**

```bash
curl -fsSL https://notch.cs.montana.edu/install.sh | bash
```

**Windows (PowerShell):**

```powershell
iwr https://notch.cs.montana.edu/install.ps1 | iex
```

Both scripts download a self-contained build into `~/.notch` (macOS/Linux) or `%LOCALAPPDATA%\notch` (Windows) and add `notch` to your `PATH`. Open a new terminal and run:

```bash
notch
```

### Uninstall

- **macOS / Linux:** `rm -rf ~/.notch`, then remove the `# notch` PATH line from your shell rc (`~/.zshrc`, `~/.bashrc`, `~/.bash_profile`, or `~/.config/fish/config.fish`).
- **Windows:** `Remove-Item -Recurse $env:LOCALAPPDATA\notch`, then remove the `notch` entry from your User `PATH`.

## Semantics

The semantics of the language.

### Variable, Property, & Invocation Semantics

We deliberated for a long time on what we thought were the proper semantics. We want the
language to be easy to iterate on and slow to cause errors. Mistakes should be clear not
disappear silently. The approch: **variables and calls are strict, property reads coalesce.**

An unresolved property read produces the sentinel value `<undefined>` rather than an error, and
once a chain hits `<undefined>` (or `null`) every remaining hop short circuits to `<undefined>`.
An unknown *variable* is still an error, unless you opt out with a trailing `?`.

```notch
foo               # error: unknown variable "foo"
foo?              # --> <undefined>       -- ? makes the lookup lenient
foo?.bar          # --> <undefined>
foo?.bar.baz.bad  # --> <undefined>       -- the whole chain coalesces
```

```notch
# foo is bound to null
foo               # --> null              -- a bound null is a value, not an error
foo.bar           # --> <undefined>

# foo is bound to an object with a baz() method but no bar
foo.bar           # --> <undefined>
foo.bar.baz.bad   # --> <undefined>
```

Invocation is the exception. Calling something that isn't there is almost always a mistake, so
it reports with a near match if it exists:

```notch
foo.bar()   # error: unable to call 'foo.bar', value was null
foo.bar()   # error: no method 'bar' on Foo - Did you mean 'baz'?
```

The `?` suffix only relaxes the variable lookup; it does not make a call lenient
(`foo?.bar()` still errors if `bar` can't be called).

To turn an `<undefined>` back into a real value, use the fallback operator `?:`, and test for
it with `is undefined`:

```notch
foo ?: "default"    # --> "default"
foo? is undefined   # --> true
foo is null         # --> true            (foo is bound to null)
```
