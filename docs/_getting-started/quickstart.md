---
title: "Quickstart"
subtitle: "Run your first Notch script"
description: "Run your first Notch script."
order: 2
---

Notch runs two ways, interactively at the REPL and by executing a `.notch` file. If you have not installed Notch yet, start with [Install](../install/).

## Hello, Notch

Put this in a file called `hello.notch`:

```notch
import java.time.LocalDate

today = LocalDate.now()

for name in ['Alice', 'Bob', 'Carol']
  print('Hello, ' + name + '! Today is ' + today)
end
```

Three things are already on display: 

* `import` brings a JVM type into scope (see [Java Interop](../../language/java-interop/))
* `LocalDate.now()` calls a static method
*  `for ... in` walks the list. 

## The REPL

Launch the interactive shell by running `notch` with no arguments:

```
notch
```

The prompt should appear:

```
notch >
```

Type an expression and press Enter, the result prints immediately:

```notch
notch > 1 + 1
2
notch > print('hello')
hello
notch > [1, 2, 3].size()
3
```

When Notch needs more input to finish a block, it will display a continuation prompt
until the matching `end` for example:

```notch
notch > if true
      >   print('foo')
      > else
      >   print('bar')
      > end
foo
```

After typing `if true` the statement is not complete so you will be prompted with `>` to continue in finishing the statement.
