---
title: "Reference Sheet"
subtitle: "The language at a glance"
description: "A one-page reference to Notch syntax: literals, operators, variables, control flow, functions, classes, errors, and Java interop."
permalink: /getting-started/cheat-sheet/
order: 4
---

A page of the most common Notch forms. Each section links to the full reference under [Language](../../language/values/).

## [Values & literals](../../language/values/)

```notch
42            0xff  0b101  0o77  # integers (hex, binary, octal)
true  false
null                              
'hello'  "hello"                 
[1, 2, 3]                          # list
{'foo' -> 1, 'bar' -> 2}           # map
(1 + 2) * 3                        # grouping
```

## [Operators & expressions](../../language/operators/)

```notch
items[0].trim()                    # index and call
-n   !ok   not ok                  # unary
2 * 3 / 4 % 5    1 + 2 - 4         
2 < 3   2 <= 3   2 > 3   2 >= 3    
1 == 1   1 != 2                    # equality
'notch' starts with 'no'           
'notch' ends with 'ch'
'notch' contains 'otc'
'' is empty     [1] is not empty   # emptiness
true && true    done || failed     
x ?: 'default'                     # fallback
'yes' if cond else 'no'            # conditional
```

## [Variables & scope](../../language/variables/)

```notch
x = 1
list[0] = 9
p.x = 42
for i in [1, 2]
  print(i)
end
i                                  # out of scope here
```

## [Control flow](../../language/control-flow/)

```notch
print(x)

if cond
  print('foo')
else
  print('bar')
end

for x in 'foo'
  print(x)
end

for x in 'foo' index i
  print(i)
end

repeat 3 times
  print(it)
end

repeat while x < 3
  x = x + 1
end

repeat until x >= 3
  x = x + 1
end

break   continue
```

## [Functions & closures](../../language/functions/)

```notch
function add(a, b)
  return a + b
end

\-> 1                              # zero-arg closure
\ s -> s.length                    # one arg
\ x, y -> x == y                   # multiple args
['a', 'ab', 'abc'].map(\ s -> s.length)
```

## [Classes & objects](../../language/classes/)

```notch
class Point
  field x
  function getX()
    return this.x
  end
end
p = new Point()
```

## [Errors & exceptions](../../language/errors/)

```notch
throw 'boom'
try
  throw 'boom'
catch RuntimeException as e
  print(e)
end
```

## [Java interop](../../language/java-interop/)

```notch
import java.time.LocalDate         # import gives the short name
import java.util.List as JavaList  # 'as' aliases it

LocalDate.now()
JavaList.of(1, 2, 3)               # static method, via alias
list = java.util.ArrayList()       # fully-qualified works too
list.size                          # getter
list.size()                        # method
java.lang.System.out.println("hi") # deep static access

for v in java.time.DayOfWeek.values
  print(v)
end
```

## See also

- [Language: Values & Literals](../../language/values/) and the rest of the Language reference.
- [Quickstart](../quickstart/) to run these examples.
