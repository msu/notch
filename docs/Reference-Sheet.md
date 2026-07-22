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
0xff
0b101
0o77
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
:hello
{% endhighlight %}
</div>

<div class="code-file"><div class="code-file-title">listLiteral.notch</div>
{% highlight notch %}
[1, 2, 3]
{% endhighlight %}
</div>

<div class="code-file"><div class="code-file-title">mapLiteral.notch</div>
{% highlight notch %}
{'foo' -> 1, :bar -> 2}
{}
{% endhighlight %}
</div>

<div class="code-file"><div class="code-file-title">setLiteral.notch</div>
{% highlight notch %}
{1, 2, 3}
{,}
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
-n
!ok
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
'' is empty
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
x ?: 'default'
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
if true print('foo') end
{% endhighlight %}
</div>

<div class="code-file"><div class="code-file-title">forStatement.notch</div>
{% highlight notch %}
for x in 'foo' print(x) end
for x in 'foo' index i print(i) end
{% endhighlight %}
</div>

<div class="code-file"><div class="code-file-title">repeatStatement.notch</div>
{% highlight notch %}
repeat 3 times print(it) end
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
\-> 1
\ s -> s.length
\ x, y -> x == y
{% endhighlight %}
</div>

<div class="code-file"><div class="code-file-title">closureBlock.notch</div>
{% highlight notch %}
\ x -> {
  y = x + 1
  return y
}
{% endhighlight %}
</div>

<div class="code-file"><div class="code-file-title">closureInterop.notch</div>
{% highlight notch %}
['a', 'ab', 'abc'].map(\ s -> s.length)
(\ x, y -> x == y).toBiFunction()
{% endhighlight %}
</div>

## [JVM Overview]({{ '/jvm/overview/' | relative_url }})

```notch
import java.time.LocalDate
java.lang.System.out.println("hello!")
list = java.util.ArrayList()
list.size
list.size()
java.util.List.of(1, 2, 3)
java.lang.Character.TYPE
for v in java.time.DayOfWeek.values
  print(v)
end
```

Line by line: import a class; call a method; construct by invoking the type;
property access via getters; the same call written explicitly; a static method;
a static property; enums iterate via `.values`.

Overloads resolve by parameter count, then assignability, then registered
[coercions]({{ '/jvm/coercions/' | relative_url }}) (boxed and primitive,
`BigDecimal` and `int`). Calls on `null` produce a diagnostic pointing at the
failing expression - guard with `is empty` or a `null` comparison.

## REPL - [Getting Started]({{ '/repl/getting-started/' | relative_url }})

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

- [Notch Templates]({{ '/templates/overview/' | relative_url }}) - render text with embedded Notch expressions.
- [JSON5]({{ '/json5/overview/' | relative_url }}) - JSON5 parser and a small query language.
- [Chisel]({{ '/chisel/overview/' | relative_url }}) - the language-agnostic foundation Notch is built on.
