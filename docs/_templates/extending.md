---
title: "Custom Commands"
subtitle: "Adding your own template commands"
order: 5
---

Template commands are subclasses of `NotchTemplateCommand`. Register a command with `NotchTemplateRegistry.addCommand(name, command)` and templates can invoke it just like the built-ins.

## Anatomy of a command

A command implementation does two things:

1. Decides which template syntax it accepts (a single line, or a block bounded by a matching `end`).
2. Provides a render hook that runs at output time and writes to the rendering context.

The canonical reference is `BasicNotchTemplateCommands.java`, which registers all 16 built-in commands.

## Registration

```notch
registry = new NotchTemplateRegistry(loader)
BasicNotchTemplateCommands.registerAll(registry)
registry.addCommand("upper", new MyUpperCommand())
```

Templates can then use the new command:

```notch
# upper
hello
# end
```

Renders `HELLO`.

## See also

- [Built-in Commands](../commands/) for the 16 shipped commands; useful as a worked example.
- [Overview](../overview/) for where commands fit in the broader engine.
