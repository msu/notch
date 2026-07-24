// Wrap each line of a highlighted code block in its own element so CSS can give
// the hovered line a background (see .code-line rules in style.css). Rouge emits
// one <pre><code> whose lines are separated by newlines that may live in bare
// text nodes OR inside whitespace token spans (<span class="w">\n</span>), so we
// split on "\n" wherever it appears, re-wrapping split token runs in a shallow
// clone to keep their styling. Only the dark .highlight blocks are touched;
// copying still works because innerText re-inserts newlines between the
// block-level .code-line spans.
export function init() {
  document.querySelectorAll('main .highlight pre > code').forEach((code) => {
    if (code.querySelector('.code-line')) return; // idempotent

    const makeLine = () => {
      const line = document.createElement('span');
      line.className = 'code-line';
      return line;
    };

    const lines = [];
    let current = makeLine();
    const newLine = () => {
      lines.push(current);
      current = makeLine();
    };

    const append = (node) => {
      const text = node.textContent;
      if (!text.includes('\n')) {
        current.appendChild(node.cloneNode(true)); // no newline inside: keep whole
        return;
      }
      if (node.nodeType === Node.TEXT_NODE) {
        text.split('\n').forEach((part, i) => {
          if (i > 0) newLine();
          if (part) current.appendChild(document.createTextNode(part));
        });
      } else {
        // A token span containing newline(s) (e.g. <span class="w">\n</span> or a
        // multi-line string). Split its text and re-wrap each run in a shallow
        // clone so the token's class/styling survives the split.
        text.split('\n').forEach((part, i) => {
          if (i > 0) newLine();
          if (part) {
            const shallow = node.cloneNode(false);
            shallow.textContent = part;
            current.appendChild(shallow);
          }
        });
      }
    };

    Array.from(code.childNodes).forEach(append);
    lines.push(current);

    // Drop the empty trailing line produced by the source's final newline.
    if (lines.length > 1 && !lines[lines.length - 1].hasChildNodes()) lines.pop();

    code.textContent = '';
    lines.forEach((line) => code.appendChild(line));

    // Tag multi-line notch programs so CSS can add a line-number gutter.
    // Terminal/plaintext transcripts (.language-plaintext) and one-liners
    // are left unnumbered. Numbers are CSS-generated, so they never appear
    // in copied text or selections.
    if (lines.length > 1 && code.closest('.language-notch, figure.highlight')) {
      code.classList.add('with-linenos');
    }
  });
}
