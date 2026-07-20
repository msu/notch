---
layout: docs
title: Reference Sheet
subtitle: "Quick Guide"
permalink: /reference-sheet/
docs_nav: true
order: 1
---

##  [Literals]({{ '/syntax/overview/' | relative_url }})

<div class="code-file"><div class="code-file-title">integerLiteral.notch</div>
{% highlight notch %}
42
0xff   // 255
0b101  // 5
0o77   // 63
{% endhighlight %}
</div>

<div class="code-file"><div class="code-file-title">booleanLiteral.notch</div>
{% highlight notch %}
true
false
{% endhighlight %}
</div>

<div class="code-file"><div class="code-file-title">nullLiteral.notch</div>
{% highlight notch %}
null
{% endhighlight %}
</div>

<div class="code-file"><div class="code-file-title">stringLiteral.notch</div>
{% highlight notch %}
'hello'
"hello"
:hello   // terse string
{% endhighlight %}
</div>

<div class="code-file"><div class="code-file-title">listLiteral.notch</div>
{% highlight notch %}
[1, 2, 3]
{% endhighlight %}
</div>

<div class="code-file"><div class="code-file-title">mapLiteral.notch</div>
{% highlight notch %}
{'foo' -> 1, :bar -> 2}  // keys are expressions
{}  // empty map
{% endhighlight %}
</div>

<div class="code-file"><div class="code-file-title">setLiteral.notch</div>
{% highlight notch %}
{1, 2, 3}
{,}  // empty set
{% endhighlight %}
</div>

<div class="code-file"><div class="code-file-title">grouping.notch</div>
{% highlight notch %}
(1 + 2) * 3
{% endhighlight %}
</div>

## [Operators]({{ '/syntax/operators/' | relative_url }})

<div class="code-file"><div class="code-file-title">access.notch</div>
{% highlight notch %}
items[0].trim()
{% endhighlight %}
</div>

<div class="code-file"><div class="code-file-title">unary.notch</div>
{% highlight notch %}
-n       // negate
!ok      // inverse
not ok
{% endhighlight %}
</div>

<div class="code-file"><div class="code-file-title">arithmetic.notch</div>
{% highlight notch %}
2 * 3 / 4 % 5
1 + 2 - 4
{% endhighlight %}
</div>

<div class="code-file"><div class="code-file-title">comparison.notch</div>
{% highlight notch %}
2 < 3
2 <= 3
2 > 3
2 >= 3
{% endhighlight %}
</div>

<div class="code-file"><div class="code-file-title">stringTests.notch</div>
{% highlight notch %}
'notch' starts with 'no'
'notch' ends with 'ch'
'notch' contains 'otc'
{% endhighlight %}
</div>

<div class="code-file"><div class="code-file-title">equality.notch</div>
{% highlight notch %}
1 == 1
1 != 2
1 is 1
1 is not 2
{% endhighlight %}
</div>

<div class="code-file"><div class="code-file-title">emptiness.notch</div>
{% highlight notch %}
'' is empty  // empty string, list, map, or null
[1] is not empty
{% endhighlight %}
</div>

<div class="code-file"><div class="code-file-title">logicalOps.notch</div>
{% highlight notch %}
true && true
true and true
done || failed
done or failed
{% endhighlight %}
</div>

<div class="code-file"><div class="code-file-title">fallback.notch</div>
{% highlight notch %}
x ?: 'default'  // right side if left is null
{% endhighlight %}
</div>

<div class="code-file"><div class="code-file-title">conditional.notch</div>
{% highlight notch %}
'yes' if true else 'no'
{% endhighlight %}
</div>

## [Statements]({{ '/syntax/statements/' | relative_url }})

<div class="code-file"><div class="code-file-title">assignment.notch</div>
{% highlight notch %}
x = 1
list[0] = 9
p.x = 42
{% endhighlight %}
</div>

<div class="code-file"><div class="code-file-title">print.notch</div>
{% highlight notch %}
print(x)
{% endhighlight %}
</div>

<div class="code-file"><div class="code-file-title">ifStatement.notch</div>
{% highlight notch %}
if true print('foo') else print('bar') end
if true print('foo') end  // else optional
{% endhighlight %}
</div>

<div class="code-file"><div class="code-file-title">forStatement.notch</div>
{% highlight notch %}
for x in 'foo' print(x) end  // lists, strings, maps
for x in 'foo' index i print(i) end  // index binds position
{% endhighlight %}
</div>

<div class="code-file"><div class="code-file-title">repeatStatement.notch</div>
{% highlight notch %}
repeat 3 times print(it) end  // 'it' counts 1..3
repeat while x < 3 x = x + 1 end
repeat until x >= 3 x = x + 1 end
break
continue
{% endhighlight %}
</div>

<div class="code-file"><div class="code-file-title">function.notch</div>
{% highlight notch %}
function add(a, b)
  return a + b
end
{% endhighlight %}
</div>

<div class="code-file"><div class="code-file-title">class.notch</div>
{% highlight notch %}
class Point
  field x
  function getX()
    return this.x
  end
end
p = new Point()
{% endhighlight %}
</div>

<div class="code-file"><div class="code-file-title">import.notch</div>
{% highlight notch %}
import java.time.LocalDate
{% endhighlight %}
</div>

<div class="code-file"><div class="code-file-title">exceptions.notch</div>
{% highlight notch %}
throw 'boom'
try
  throw 'boom'
catch RuntimeException as e
  print(e)
end
{% endhighlight %}
</div>

## [Closures]({{ '/syntax/closures/' | relative_url }})

<div class="code-file"><div class="code-file-title">closure.notch</div>
{% highlight notch %}
\-> 1  // zero args
\ s -> s.length  // one arg
\ x, y -> x == y  // multiple args
{% endhighlight %}
</div>

<div class="code-file"><div class="code-file-title">closureBlock.notch</div>
{% highlight notch %}
\ x -> {
  y = x + 1
  return y  // block value; without a return, yields <undefined>
}
{% endhighlight %}
</div>

<div class="code-file"><div class="code-file-title">closureInterop.notch</div>
{% highlight notch %}
['a', 'ab', 'abc'].map(\ s -> s.length)  // [1, 2, 3]
(\ x, y -> x == y).toBiFunction()  // java.util.function interop
{% endhighlight %}
</div>

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
