---
title: "Getting Started"
subtitle: "Load and render your first template"
order: 2
---

Templates are loaded by a `NotchTemplateLoader` and rendered through a `NotchTemplateRegistry`. Pick a loader, build a registry, register the built-in commands, and call `render`.

## Picking a loader

Two loaders ship with the engine:

- `NotchTemplateClasspathLoader` - loads templates as classpath resources. Good for templates bundled inside a JAR.
- `NotchTemplateFilesystemLoader` - loads templates from a directory on disk. Good for templates the user can edit at runtime.

Both implement the `NotchTemplateLoader` interface so you can also write your own.

## Building a registry

```notch
loader = new NotchTemplateFilesystemLoader("/path/to/templates")
registry = new NotchTemplateRegistry(loader)
BasicNotchTemplateCommands.registerAll(registry)
```

`BasicNotchTemplateCommands.registerAll` wires the 16 built-in commands into the registry. Without it your templates can use expressions and literal text but no `#` commands.

## Rendering

Suppose `/path/to/templates/greeting.nt` contains:

```notch
Hello, ${ name }!
```

Then:

```notch
output = registry.render("greeting", {name = "world"})
print(output)
```

prints `Hello, world!`. The second argument is a map of bindings the template can reference by name.

## See also

- [Template Syntax](../syntax/) for the literal-text / expression / command distinction.
- [Built-in Commands](../commands/) once you want loops, conditionals, or layout.
