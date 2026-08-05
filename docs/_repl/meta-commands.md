---
title: "Meta Commands"
subtitle: "Built-in REPL subcommands"
description: "The Notch REPL's built-in meta commands: subcommands available by name at the prompt for controlling your interactive session."
order: 2
---

The Notch REPL ships with a small set of built-in commands accessible by name at the prompt.

## Built-in commands

| Command         | Purpose                                          |
|-----------------|--------------------------------------------------|
| help            | Show available commands.                         |
| exit            | Quit the REPL.                                   |
| clear           | Clear the screen.                                |
| logs            | Show recent log output.                          |
| reset           | Wipe the runtime and start fresh.                |
| history         | Show input history.                              |
| save &lt;FILE&gt; | Save session inputs to a file.                   |
| write &lt;FILE&gt; | Toggle live recording of successful evaluations. |
| load &lt;FILE&gt; | Read and execute a Notch file against the live runtime. |

Type the command name at the `notch >` prompt and press Enter. `save` takes a file path argument, e.g. `save mySession.notch`.

## Tab completion

Tab completion is wired for the built-in subcommands above. Identifier completion (your own bindings and JVM types) is planned.

## History

The REPL keeps a command history that persists across sessions in `~/.notch_history`. Use the up/down arrow keys to navigate, or type `history` to see the full buffer with line numbers. Use `save <file>` to write a polished version of the session to a `.notch` file (subcommand invocations like `history` and `save` are filtered out so the resulting file is runnable).

## Session recording

`save` and `write` look similar but capture different things:

- `save <file>` walks the whole input history and writes it to `<file>`, filtering out meta-command invocations (`help`, `clear`, `save`, etc.) so the resulting file is runnable notch source.
- `write <file>` toggles live recording: every successful evaluation after the first `write` is appended to `<file>` until you type `write` again with no argument. Nothing is filtered. Only one `write` session can be active at a time.

Use `save` to snapshot a polished session for re-running. Use `write` to capture a live coding sequence as you type it.

## Syntax highlighting

The REPL colorizes input in real time. Tokens map to colors via the terminal's color palette:

- booleans and other constant keywords - constant color
- identifiers - variable color
- integers - number color
- strings - string color
- type keywords (e.g. `int`) - type color

## Error display

Parse and runtime errors are rendered with a source line and a caret underlining the failing expression, followed by an explanatory note:

```notch
notch > [1, 2, 3].nope()
   --> jackknife-cli:1:1
    |
  1 | [1, 2, 3].nope()
    | ^^^^^^^^^^^^^^
    |
note: no method 'nope' on BetterList - Did you mean 'copy'?
```

## See also

- [Getting Started](../getting-started/) for installation and first use.
