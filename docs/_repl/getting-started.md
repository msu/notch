---
title: "Getting Started"
subtitle: "Install, launch the REPL, run a first script"
order: 1
---

Notch ships with an interactive REPL backed by JLine 3.

## Install

Clone and build:

```plaintext
git clone https://github.com/msu/notch.git
cd notch
mvn package
```

## Launch the REPL

```plaintext
java -jar target/notch-1.0-SNAPSHOT.jar
```

You should see:

```plaintext
notch >
```

## First script

Type an expression and press Enter:

```plaintext
notch > 1 + 1
2
notch > print('hello')
hello
notch > [1, 2, 3].size()
3
```

## Multi-line expressions

When Notch needs more input to complete an expression, it shows a continuation prompt:

```plaintext
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
