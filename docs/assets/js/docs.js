// `<script type="module">` is auto-deferred per spec, so by the time this
// file executes the DOM is already parsed. No DOMContentLoaded guard needed.
// Side-panel collapse, sidebar resize, and the mobile hamburger are wired via
// hyperscript on their elements; only the search engine, TOC builder,
// post-navigation highlight handler, and the panel shortcut remain in JS.
import { init as initDocsSearch } from './modules/docs-search.js';
import { init as initDocsToc } from './modules/docs-toc.js';
import { init as initSearchHighlight } from './modules/search-highlight.js';
import { init as initCodeCopy } from './modules/code-copy.js';

initDocsSearch();
initDocsToc();
initSearchHighlight();
initCodeCopy();

// '[' toggles the side panels -- a labeled, documented second route to the
// Contents-header button. Clicking it keeps aria/localStorage in one place.
document.addEventListener('keydown', (e) => {
  if (e.key !== '[' || e.metaKey || e.ctrlKey || e.altKey) return;
  if (e.target.closest?.('input, textarea, select, [contenteditable]')) return;
  const tab = document.getElementById('navs-toggle');
  if (tab) tab.click();
});

// Freeze the article column at its DESTINATION width for the duration
// of the panel slide: the text re-wraps once at the start, then the
// block translates into a landing position that is already correct for
// its final width (locking to the current width made it drift past its
// landing spot and visibly correct at release). The destination width
// is measured invisibly — transitions disabled, state class flipped and
// flipped back within one script turn, so nothing paints. Capture phase
// so this runs before the toggle's own hyperscript flips the class.
let unlockTimer;
document.addEventListener('click', (e) => {
  if (!e.target.closest?.('#navs-toggle, .toolbar-panels-toggle')) return;
  const content = document.querySelector('.docs-content');
  if (!content || window.innerWidth <= 768) return;
  clearTimeout(unlockTimer);
  const html = document.documentElement;
  content.style.width = '';
  content.style.maxWidth = '';
  content.style.flex = '';
  html.classList.add('navs-anim-off');
  html.classList.toggle('navs-collapsed');
  const finalWidth = content.getBoundingClientRect().width;
  html.classList.toggle('navs-collapsed');
  void content.offsetWidth;
  html.classList.remove('navs-anim-off');
  content.style.width = finalWidth + 'px';
  content.style.maxWidth = 'none';
  content.style.flex = 'none';
  document.body.style.overflowX = 'hidden';
  unlockTimer = setTimeout(() => {
    content.style.width = '';
    content.style.maxWidth = '';
    content.style.flex = '';
    document.body.style.overflowX = '';
  }, 400);
}, true);
