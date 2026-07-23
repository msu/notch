---
title: "Add to your project (Maven)"
subtitle: "Embed Notch as a library on the JVM"
description: "Embed Notch as a library on the JVM to run user-supplied Notch scripts from a Java app or Minecraft plugin, added via Maven Central."
order: 3
---

This page is for **embedding Notch as a library** inside your own program. If you only want to write `.notch` files and run them yourself use the [command-line](../install/) instead.

## Why embed Notch

Embedding lets a **already shipped application run logic that was not
built into it**. You deploy your app and notch scripts are supplied later.

- **Minecraft plugins and mods.** A Bukkit plugin among others is
  Java, and a server owner cannot recompile the server to change behavior. Embed
  Notch and the owner writes a script - "when a player joins for the first time,
  give them a starter kit" the plugin can run this script live.
- **Rules and formulas as data.** Evaluate a pricing rule or filter that lives in
  a database or config file, not in your source: `order.total > 100`.
- **Configuration with logic.** Config values that are computed expressions
  instead of static strings.
- **Templating.** Generate emails, reports, code, or HTML from templates that can
  call into Notch.

## Add the dependency

Notch is published to **Maven Central**. Use whichever build tool your project
already uses. The published version may be newer than `0.1.1`; prefer the latest stable release.

### Maven

```xml
<dependency>
  <groupId>edu.montana.cs.notch</groupId>
  <artifactId>notch</artifactId>
  <version>0.1.1</version>
</dependency>
```

### Gradle

Make sure `mavenCentral()` is in your repositories.

```groovy
// build.gradle (Groovy DSL)
repositories { mavenCentral() }

dependencies {
  implementation 'edu.montana.cs.notch:notch:0.1.1'
}
```

```kotlin
// build.gradle.kts (Kotlin DSL)
repositories { mavenCentral() }

dependencies {
  implementation("edu.montana.cs.notch:notch:0.1.1")
}
```

## Run Notch from Java

There are two entry points what you want depends on whether you need **a
value** or **a program**.

### A value: `Notch.eval` and `Notch.render`

* `Notch.eval` runs a single expression and hands the result back as a Java object
* `Notch.render` runs a template and returns a `String`. This is the common case evaluating a formula, a rule, or a config value.

```java
import edu.montana.notch.Notch;

Object v = Notch.eval("1 + 2");                 // 3
int    n = Notch.eval("1 + 2", Integer.class);  // coerced to int
String s = Notch.render("greeting", "name", "Ada");
```

- `Notch.eval(String)` evaluates one expression and returns the result.
- `Notch.eval(String, Class<T>)` coerces the result to the requested type, or
  throws if no coercion applies.
- `Notch.render(path, key, value, ...)` renders a template with key/value
  bindings.

### A whole program: `NotchRuntime`

When the script is a **multi-statement program** - assignments, loops, side
effects - rather than a single expression, use the lower-level path the CLI uses:
tokenize with `Notch.TOKENIZER`, parse, then drive a `NotchRuntime`.

```java
import edu.montana.notch.Notch;
import edu.montana.notch.NotchParser;
import edu.montana.notch.NotchElement;
import edu.montana.notch.chisel.Source;
import edu.montana.notch.chisel.TokenStream;
import edu.montana.notch.expressions.NotchExpression;
import edu.montana.notch.runtime.NotchRuntime;
import edu.montana.notch.statements.NotchStatement;

Source source = new Source("<embed>", "x = 1\nprint(x + 1)");
TokenStream tokens = Notch.TOKENIZER.create(source).tokenize();
NotchElement element = new NotchParser(tokens).parse();

NotchRuntime runtime = new NotchRuntime(source);
if (element instanceof NotchStatement stmt) {
  runtime.execute(stmt);
} else if (element instanceof NotchExpression expr) {
  runtime.evaluate(expr);
}
```

Rule of thumb: **`eval` when you want a value back; the runtime path when you want
to run a program.**

## Keeping the version current

The `0.1.1` above tracks `pom.xml`; bump it to match the release you depend on.
The pom is the single source of truth for the current version.

## See also

- [Install](../install/) for the command-line runtime.
- [Java Interop](../../language/java-interop/) for how Notch calls into the JVM.
