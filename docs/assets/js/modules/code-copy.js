const COPY_ICON = '<svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true" focusable="false"><rect x="9" y="9" width="13" height="13" rx="2"></rect><path d="M5 15H4a2 2 0 0 1-2-2V4a2 2 0 0 1 2-2h9a2 2 0 0 1 2 2v1"></path></svg>';
const CHECK_ICON = '<svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true" focusable="false"><polyline points="20 6 9 17 4 12"></polyline></svg>';

// Inject a copy button into every rendered code block. Buttons go on the
// block's wrapper (kramdown: div.highlight, {% highlight %}: figure.highlight)
// rather than the <pre> so they stay pinned when the code scrolls
// horizontally. Copying reads the <code> element's text, so what lands on
// the clipboard is exactly the source text.
export function init() {
  document.querySelectorAll('main pre').forEach((pre) => {
    const code = pre.querySelector('code');
    const host = pre.parentElement;
    if (!code || !host) return;

    host.classList.add('code-copy-host');

    const btn = document.createElement('button');
    btn.type = 'button';
    btn.className = 'code-copy';
    btn.setAttribute('aria-label', 'Copy code');
    btn.innerHTML = COPY_ICON;

    let timer = null;
    btn.addEventListener('click', async () => {
      try {
        await navigator.clipboard.writeText(code.innerText);
      } catch {
        return; // clipboard unavailable; the block is still selectable
      }
      btn.innerHTML = CHECK_ICON;
      btn.classList.add('copied');
      btn.setAttribute('aria-label', 'Copied');
      clearTimeout(timer);
      timer = setTimeout(() => {
        btn.innerHTML = COPY_ICON;
        btn.classList.remove('copied');
        btn.setAttribute('aria-label', 'Copy code');
      }, 1600);
    });

    host.appendChild(btn);
  });
}
