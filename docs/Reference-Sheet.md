---
layout: docs
title: Reference Sheet
subtitle: "Quick Guide"
permalink: /reference-sheet/
docs_nav: true
order: 1
---

##  [Literals]({{ '/syntax/overview/' | relative_url }})

```notch
42                        // integer
0xff                      // 255 (hex)
0b101                     // 5 (binary)
0o77                      // 63 (octal)
true                      // boolean
false
null                      // null literal
'hello'                   // string, single or
"hello"                   // double quoted
:hello                    // terse string
[1, 2, 3]                 // list
{'foo' -> 1, :bar -> 2}   // map; keys are expressions
{}                        // empty map
{1, 2, 3}                 // set
{,}                       // empty set
(1 + 2) * 3               // parens group
```

## [Operators]({{ '/syntax/operators/' | relative_url }})

```notch
items[0].trim()           // a call, a index, or property access
-n                        // negate
!ok                       // inverse 'not ok' also works
2 * 3 % 4              
1 + 2 - 4                 
2 <= 3                  
'notch' starts with 'no' 
'notch' ends with 'ch' 
'notch' contains 'otc'   
1 == 1                    // equality: == != is, is not
'' is empty               // empty string/list/map or null
[1] is not empty
true && true              // logical and; 'and' also works
done || failed            // logical or; 'or' also works
x ?: 'default'            // fallback to right side if left is null/undefined
'yes' if true else 'no'     
```







## [Statements]({{ '/syntax/statements/' | relative_url }})

```notch
if true print('foo') else print('bar') end
if true print('foo') end                    // else optional

for x in 'foo' print(x) end                 // iterates lists, strings, maps
for x in 'foo' index i print(i) end         // index clause binds position
```

## [Closures]({{ '/syntax/closures/' | relative_url }})

```notch
\-> 1                     // zero args
\ s -> s.length           // one arg
\ x, y -> x == y          // multiple args
\ x -> {                  // block body: value is the
  y = x + 1               // last expression statement
  y
}
['a', 'ab', 'abc'].map(\ s -> s.length)     // [1, 2, 3]
(\ x, y -> x == y).toBiFunction()           // java.util.function interop
```

## [JVM Overview]({{ '/jvm/overview/' | relative_url }})

```notch
import java.time.LocalDate                  // import a class
java.lang.System.out.println("hello!")      // call methods
list = java.util.ArrayList()                // constructors: invoke the type
list.size                                   // property access via getters
list.size()                                 // same thing
java.util.List.of(1, 2, 3)                  // static methods
java.lang.Character.TYPE                    // static properties
for v in java.time.DayOfWeek.values         // enums iterate via .values
  print(v)
end
```

Overloads resolve by parameter count, then assignability, then registered
[coercions]({{ '/jvm/coercions/' | relative_url }}) (boxed ↔ primitive,
`BigDecimal` ↔ `int`). Calls on `null` produce a diagnostic pointing at the
failing expression — guard with `is empty` or a `null` comparison.

## REPL — [Getting Started]({{ '/repl/getting-started/' | relative_url }})

Launch with `notch`. Unfinished `if`/`for` blocks get a continuation prompt
until the matching `end`. History persists in `~/.notch_history`.

| Command | Purpose |
|---------|---------|
| `help` | Show available commands. |
| `exit` | Quit the REPL. |
| `clear` | Clear the screen. |
| `logs` | Show recent log output. |
| `reset` | Wipe the runtime and start fresh. |
| `history` | Show input history. |
| `save <FILE>` | Save session inputs to a runnable file (meta-commands filtered). |
| `write <FILE>` | Toggle live recording of successful evaluations. |
| `load <FILE>` | Read and execute a Notch file against the live runtime. |

## More

- [Notch Templates]({{ '/templates/overview/' | relative_url }}) — render text with embedded Notch expressions.
- [JSON5]({{ '/json5/overview/' | relative_url }}) — JSON5 parser and a small query language.
- [Chisel]({{ '/chisel/overview/' | relative_url }}) — the language-agnostic foundation Notch is built on.
