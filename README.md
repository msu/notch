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

Both scripts download a self-contained build (no JDK required) into `~/.notch` (macOS/Linux) or `%LOCALAPPDATA%\notch` (Windows) and add `notch` to your `PATH`. Open a new terminal and run:

```bash
notch
```

### Uninstall

- **macOS / Linux:** `rm -rf ~/.notch`, then remove the `# notch` PATH line from your shell rc (`~/.zshrc`, `~/.bashrc`, `~/.bash_profile`, or `~/.config/fish/config.fish`).
- **Windows:** `Remove-Item -Recurse $env:LOCALAPPDATA\notch`, then remove the `notch` entry from your User `PATH` (System Properties -> Environment Variables).

### Antivirus on Windows

Windows Defender occasionally flags `notch.exe` as a low-prevalence executable (`Trojan:Win32/Wacatac.B!ml` or similar) because the binary is unsigned and bundles its own JRE. This is a false positive. If you hit it:

1. Ask your IT department to whitelist `%LOCALAPPDATA%\notch\`.
2. Submit a false-positive report at https://www.microsoft.com/wdsi/filesubmission -- Microsoft usually clears these within a day or two.

## Semantics

The semantics of the language.

### Variable, Property, & Invocation Semantics

We deliberated for a long time on what we thought were the proper semantics. We want the
language to be easy to iterate on and slow to cause errors. We decided to implement
null-coalescence instead of "undefined variable/property/method on ..."

```notch
foo             # --> <UNDEFINED>
foo!            # error: no variable "foo"
foo.bar         # --> <UNDEFINED>
foo.bar!        # error: no property "bar" on <UNDEFINED>
foo.bar()       # --> <UNDFINED>
foo.bar!()      # error: no method "bar" on <UNDEFINED>
foo.bar.baz.bad # --> <UNDEFINED>
```
