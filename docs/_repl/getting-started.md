---
title: "Getting Started"
subtitle: "Working with notch from the terminal"
order: 1
---



## Install Notch first

See the [install page]({{ '/install/' | relative_url }}) for the one-line install on macOS, Linux, and Windows. Once installed, `notch` is on your `PATH`.

## Launch the REPL

```
notch
```

You should see:

```
notch >
```

## First script

Type an expression and press Enter:

```notch
notch > 1 + 1
2
notch > print('hello')
hello
notch > [1, 2, 3].size()
3
```

## Multi-line expressions

When Notch needs more input to complete an expression, it shows a continuation prompt:

```notch
notch > if true
      >   print('foo')
      > else
      >   print('bar')
      > end
foo
```

Continuation kicks in when the line parser sees an unmatched `if` or `for` keyword and waits for the matching `end`.

## See also

- [Meta Commands](../meta-commands/) for the built-in subcommands.
- [Syntax Overview](../../syntax/overview/) for what to type at the prompt.
