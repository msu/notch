import { CONFIG } from './config.js';

export function init() {
  const searchBtn = document.querySelector('.search-btn');
  const overlay = document.querySelector('.search-overlay');
  if (!searchBtn || !overlay) return;

  // Render the platform-appropriate modifier key in the toolbar shortcut
  // hint. Apple platforms use Cmd; everything else uses Ctrl.
  const isMac = /Mac|iPhone|iPad|iPod/.test(navigator.platform || navigator.userAgent);
  const mod = searchBtn.querySelector('.kbd-mod');
  if (mod) mod.textContent = isMac ? '⌘' : 'Ctrl';

  const input = overlay.querySelector('.search-input');
  const resultsList = overlay.querySelector('.search-results');
  const emptyState = overlay.querySelector('.search-empty');
  let searchIndex = null;
  let debounceTimer = null;
  let activeIdx = -1;

  function openSearch() {
    overlay.hidden = false;
    document.body.style.overflow = 'hidden';
    input.focus();
    if (!searchIndex) loadIndex();
  }

  function closeSearch() {
    overlay.hidden = true;
    document.body.style.overflow = '';
    input.value = '';
    resultsList.innerHTML = '';
    emptyState.hidden = true;
    activeIdx = -1;
  }

  function loadIndex() {
    fetch(CONFIG.SEARCH_INDEX_URL)
      .then(response => response.json())
      .then(data => { searchIndex = data; })
      .catch(() => { searchIndex = []; });
  }

  // Scoring: title match for the full query = +20, each phrase hit in
  // content = +10 (capped at 5 per doc); each per-term title hit = +10;
  // each per-term content hit = +1 (capped at 5 per doc per term).
  function search(query) {
    if (!searchIndex || query.length < CONFIG.SEARCH_MIN_CHARS) {
      resultsList.innerHTML = '';
      emptyState.hidden = true;
      return;
    }

    const queryLower = query.toLowerCase().trim();
    const terms = queryLower.split(/\s+/).filter(Boolean);
    const isMultiWord = terms.length > 1;
    const scored = searchIndex.map(doc => {
      let score = 0;
      const titleLower = doc.title.toLowerCase();
      const contentLower = doc.content.toLowerCase();

      if (isMultiWord) {
        if (titleLower.indexOf(queryLower) !== -1) score += 20;
        let phraseIdx = contentLower.indexOf(queryLower);
        let phraseMatches = 0;
        while (phraseIdx !== -1 && phraseMatches < 5) {
          phraseMatches++;
          phraseIdx = contentLower.indexOf(queryLower, phraseIdx + 1);
        }
        score += phraseMatches * 10;
      }

      terms.forEach(term => {
        if (titleLower.indexOf(term) !== -1) score += 10;
        let contentMatches = 0;
        let termIdx = contentLower.indexOf(term);
        while (termIdx !== -1 && contentMatches < 5) {
          contentMatches++;
          termIdx = contentLower.indexOf(term, termIdx + 1);
        }
        score += contentMatches;
      });

      return { doc, score };
    }).filter(entry => entry.score > 0)
      .sort((a, b) => b.score - a.score)
      .slice(0, CONFIG.SEARCH_MAX_RESULTS);

    if (scored.length === 0) {
      resultsList.innerHTML = '';
      emptyState.hidden = false;
      return;
    }

    emptyState.hidden = true;
    activeIdx = -1;
    const highlightParam = '?highlight=' + encodeURIComponent(input.value.trim());
    resultsList.innerHTML = scored.map(result => {
      const snippet = getSnippet(result.doc.content, terms);
      return `<li><a href="${result.doc.url}${highlightParam}">
        <div class="search-result-title">${highlight(result.doc.title, terms)}</div>
        <div class="search-result-snippet">${highlight(snippet, terms)}</div>
        </a></li>`;
    }).join('');
  }

  function getSnippet(content, terms) {
    const lower = content.toLowerCase();
    let bestIdx = -1;
    for (let i = 0; i < terms.length; i++) {
      bestIdx = lower.indexOf(terms[i]);
      if (bestIdx !== -1) break;
    }
    if (bestIdx === -1) return content.substring(0, 120) + '...';
    // Snippet window: 50 chars before the first match + 100 chars after, so
    // the matched term is left-of-center for visual scanning.
    const start = Math.max(0, bestIdx - 50);
    const end = Math.min(content.length, bestIdx + 100);
    return (start > 0 ? '...' : '') + content.substring(start, end) + (end < content.length ? '...' : '');
  }

  function highlight(text, terms) {
    const escaped = terms.map(t => t.replace(/[.*+?^${}()|[\]\\]/g, '\\$&'));
    const re = new RegExp('(' + escaped.join('|') + ')', 'gi');
    return text.replace(/</g, '&lt;').replace(/>/g, '&gt;').replace(re, '<mark>$1</mark>');
  }

  function setActive(idx) {
    const items = resultsList.querySelectorAll('a');
    if (items.length === 0) return;
    items.forEach(link => { link.classList.remove('active'); });
    if (idx >= 0 && idx < items.length) {
      activeIdx = idx;
      items[idx].classList.add('active');
      items[idx].scrollIntoView({ block: 'nearest' });
    }
  }

  searchBtn.addEventListener('click', openSearch);

  overlay.addEventListener('click', e => {
    if (e.target === overlay) closeSearch();
  });

  input.addEventListener('input', () => {
    clearTimeout(debounceTimer);
    debounceTimer = setTimeout(() => {
      search(input.value.trim());
    }, 150);
  });

  overlay.addEventListener('keydown', e => {
    if (e.key === 'Escape') { closeSearch(); return; }

    const items = resultsList.querySelectorAll('a');
    if (e.key === 'ArrowDown') {
      e.preventDefault();
      setActive(activeIdx < items.length - 1 ? activeIdx + 1 : 0);
    } else if (e.key === 'ArrowUp') {
      e.preventDefault();
      setActive(activeIdx > 0 ? activeIdx - 1 : items.length - 1);
    } else if (e.key === 'Enter' && activeIdx >= 0 && items[activeIdx]) {
      items[activeIdx].click();
    }
  });

  document.addEventListener('keydown', e => {
    if ((e.metaKey || e.ctrlKey) && e.key === 'k') {
      e.preventDefault();
      if (overlay.hidden) { openSearch(); } else { closeSearch(); }
    }
  });
}
