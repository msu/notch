---
title: "Variables & Scope"
subtitle: "Binding names to values, and where those names live"
description: "Bind names to values in Notch with assignment, including index and property assignment, plus how variable scope works in loops."
order: 2
---

A variable is a name bound to a value with `=`. Assignment is a statement and it runs
for its effect on the scopes the name is bound in not for a value like when evaluating an expression.

## Quick reference

```notch
x = 1              # bind a name
x?                 # read leniently, <undefined> if unbound
list[0] = 9        # assign into a list element
map['key'] = 9     # insert or replace a map key
p.x = 42           # assign a property
this.x = 42        # assign a property inside a method
```

## Binding a variable

The left hand side is a name, the right-hand side is any expression:

```notch
x = 1
greeting = 'hello'
total = x + 10
```

* There is no compound assignment write `count = count + 1` not `count += 1`. 

Because assignment is a statement `print(x = 1)` is an error not a print of `1`. The target must be a name, a property, or an index. `f() = 1` and `x? = 1` are
errors.

## Index assignment

Index assignment targets a list, a map, or a JVM array of objects.

```notch
list = [1, 2, 3]
list[0] = 9

map = {'a' -> 1}
map['b'] = 2
```

On a map the index is the key, and assigning inserts it if it is not already
there.

Targets chain `grid[0][1] = 42` and `o.kid.v = 7` are both legal. 

* A call anywhere in the chain, as in `s.split(',')[0] = 'z'`, is an error.
* A string can be read by index but not assigned into`'foo'[0]` is `f`, while
  `s[0] = 'b'` is a runtime error. 
* A set is neither `{1, 2, 3}[0]` is `null` rather than the first member, and assigning into a set is a runtime error.

## Property assignment

Assign to a property with dot access on the left hand side. Target must
be an instance of a Notch class and the class must declare the field.

```notch
class Point
  field x = 0
end

p = new Point()
p.x = 42
```

Inside a method `this` is the root:

```notch
class Counter
  field n = 0
  function bump()
    this.n = this.n + 1
  end
end
```

* Assigning a property on a JVM object is a runtime error even though reading one
  works. The same `Point` shape from a Java class rejects the write:

```notch
import java.awt.Point as Pt
p = new Pt(5, 6)
print(p.x)     # 5.0
p.x = 9        # ERROR: cannot assign a property on a non-instance value
```

## Scope

A name is visible from its binding onward within the enclosing scope.

```notch
n = 1
function bump()
  n = n + 1
end
bump()
print(n)
# prints 2
```

A scope is opened by a loop body, a function or closure body, a class method
body, and a `catch` clause. An `if` or `else` body and a `try` body do not open
one, so a name bound inside an `if` is still bound after its `end`.

Function declarations follow the same rule a function declared inside any of those scopes is not callable after that body ends and reports as `undefined function` rather than unknown variable. 

* `?` cannot rescue it.

```notch
for n in [1]
  function hidden()
    print('hi')
  end
end
hidden()    # undefined function 'hidden'
hidden?()   # same error '?' does not rescue a call
```

Contrast an `if` body, which opens no scope, so a function declared inside one is callable after its `end`.

A loop body is a single scope covering all iterations and any name *first* bound inside a loop is unbound once the loop ends.

```notch
total = 0
for n in [1, 2, 3]
  total = total + n
  x = "unbound"
end
print(x?) # prints undefined
print(total) # prints 0
```

A parameter hides an outer name while the body only reads it but assigning to it writes through to any binding in an enclosing scope:

```notch
n = 1
function f(n)
  n = n + 1
end
f(99)
print(n) # prints 100
```
