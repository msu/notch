export function init() {
  const params = new URLSearchParams(window.location.search);
  const terms = params.get('highlight');
  if (!terms) return;

  history.replaceState(null, '', window.location.pathname + (window.location.hash || ''));

  const words = terms.trim().toLowerCase().split(/\s+/).filter(Boolean);
  if (words.length === 0) return;

  const body = document.querySelector('.docs-body');
  if (!body) return;

  const escaped = words.map(w => w.replace(/[.*+?^${}()|[\]\\]/g, '\\$&'));
  const re = new RegExp('(' + escaped.join('|') + ')', 'gi');

  // Recursively walk the prose tree: for each text node that contains a
  // match, replace it with a fragment that wraps matches in <mark>. Skip
  // script/style/pre/mark so code blocks and existing marks aren't mangled.
  (function walk(node) {
    if (node.nodeType === Node.TEXT_NODE) {
      const text = node.textContent;
      // Fast path: skip text nodes with no match (re.test mutates lastIndex
      // on global regexes, so reset before the exec-loop below either way).
      if (!re.test(text)) { re.lastIndex = 0; return; }
      re.lastIndex = 0;
      const frag = document.createDocumentFragment();
      let last = 0;
      let match;
      while ((match = re.exec(text)) !== null) {
        if (match.index > last) frag.appendChild(document.createTextNode(text.slice(last, match.index)));
        const mark = document.createElement('mark');
        mark.className = 'search-highlight';
        mark.textContent = match[0];
        frag.appendChild(mark);
        last = match.index + match[0].length;
      }
      if (last < text.length) frag.appendChild(document.createTextNode(text.slice(last)));
      node.parentNode.replaceChild(frag, node);
      return;
    }
    if (node.nodeType === Node.ELEMENT_NODE) {
      const tag = node.tagName.toLowerCase();
      if (tag === 'script' || tag === 'style' || tag === 'pre' || tag === 'mark') return;
    }
    Array.from(node.childNodes).forEach(walk);
  })(body);

  const first = body.querySelector('mark.search-highlight');
  if (first) first.scrollIntoView({ behavior: 'smooth', block: 'center' });

  document.addEventListener('click', () => {
    document.querySelectorAll('mark.search-highlight').forEach(mark => {
      const parent = mark.parentNode;
      if (parent) {
        parent.replaceChild(document.createTextNode(mark.textContent), mark);
        parent.normalize();
      }
    });
  }, { once: true });
}
