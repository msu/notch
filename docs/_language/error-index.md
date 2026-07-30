---
title: "Error Index"
subtitle: "Every diagnostic code Notch reports, with a cause and a fix"
description: "Reference index of Notch diagnostic codes. Parser errors are EP codes; each entry shows the source that triggers it and how to fix it."
order: 10
permalink: /errors/
exclude_from_search: true
---

Every Notch diagnostic carries a code in its header line:

```plaintext
 ERROR[EP0018]: 'break' outside a loop
   --> example.notch
    |
  1 | break
    | ^^^^^
```

Codes are stable. Once published a code is never renumbered and never reused, so
it is safe to link to, search for, or match on in tooling.

Parser errors use `EP` codes. Tokenizer (`ET`) and runtime (`EE`) codes are being
added. Extensions namespace their own as `extension-name:E0001`.

For the language features behind these errors - `throw`, `try`, `catch` - see
[Errors &amp; Exceptions](../errors/).

{% for err in site.data.error_codes %}
## {{ err.code }}: {{ err.title }}
{: #{{ err.code }} }

{{ err.summary | markdownify }}

{% capture err_example %}```notch
{{ err.example }}```{% endcapture %}
{{ err_example | markdownify }}

{{ err.fix | markdownify }}
{% endfor %}
