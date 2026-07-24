---
title: "Variables & Scope"
subtitle: "Binding names to values, and where those names live"
description: "Bind names to values in Notch with assignment, including index and property assignment, plus how variable scope works in loops."
order: 2
---

A variable is a name bound to a value with `=`. Assignment is a statement: it runs
for its effect on the current scope, not for a value.

## Quick reference

```notch
x = 1              # bind a name
list[0] = 9        # assign into a list element
p.x = 42           # assign a property
```

## Binding a variable

The left-hand side is a name, the right-hand side is any expression:

```notch
x = 1
greeting = 'hello'
total = x + 10
```

Reusing a name rebinds it to the new value.

## Index assignment

Assign into a list element (or any indexable value) by targeting the index:

```notch
list = [1, 2, 3]
list[0] = 9
```

## Property assignment

Assign to a property with dotted access on the left-hand side:

```notch
p.x = 42
```

## Scope

A name is visible from its binding onward within the enclosing scope. Loop
variables are scoped to their loop - once the loop ends the loop variable reads
as `<undefined>`:

```notch
for x in 'foo' index i print(i) print(x) end
print(x)
```

The trailing `print(x)` sees `x` as `<undefined>`, because the loop binding does
not escape the loop.

## See also

- [Values & Literals](../values/) for the right-hand sides you can assign.
- [Control Flow](../control-flow/) for `for` and its loop variable.
- [Java Interop](../java-interop/) for assigning to JVM object properties.
