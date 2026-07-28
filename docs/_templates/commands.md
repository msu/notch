---
title: "Built-in Commands"
subtitle: "The 16 commands wired by BasicNotchTemplateCommands"
description: "The 16 built-in Notch template commands wired by BasicNotchTemplateCommands: control flow, loops, includes, and more."
order: 4
---

Calling `BasicNotchTemplateCommands.registerAll(registry)` on a fresh registry adds these commands. Each is invoked on its own line, prefixed with `#`.

## Control flow

- **`if <expr>` / `elseif <expr>` / `else` / `end`** - conditional blocks. The contents between `if` (or a passing `elseif` branch) and the matching `end` render only when the predicate is true.

```notch
# if user.admin
  Admin panel
# else
  Welcome, ${ user.name }
# end
```

- **`for <name> in <expr>` / `end`** - iterate over an iterable. The current item is bound to `<name>` inside the block.

```notch
# for item in items
  - ${ item }
# end
```

## Layout and composition

- **`layout <name>`** - declare that this template fills slots defined by another (parent) template. The parent's `content` slot is filled with whatever this template emits below the `layout` line.

- **`content <name>`** - inside a parent template, mark a slot to be filled by child templates.

- **`include <name>`** - inline-render another template at this point.

- **`import <name>`** - load another template's macros/helpers into the current scope without rendering it.

- **`expand <name>`** - render a named fragment.

- **`fragment <name>` / `end`** - define a named section of a template that can be expanded individually.

## Variables and reuse

- **`set <name> = <expr>`** - bind `<name>` to the result of `<expr>` for the rest of the template's scope.

- **`macro <name>(<params>)` / `end`** - define a reusable template snippet that takes parameters and emits output.

- **`helper <name> = <expr>`** - bind a helper (typically a closure) for use elsewhere in the template.

## Utilities

- **`require <name>`** - assert that a binding is present in the rendering context; fail rendering with a clear message if not.

- **`comment` / `end`** - everything between is ignored. Comments do not appear in output.

## See also

- [Custom Commands](../extending/) to add your own commands alongside these 16.
