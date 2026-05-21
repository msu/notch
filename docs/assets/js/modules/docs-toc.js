export function init() {
  const tocList = document.querySelector('.docs-toc-list');
  if (!tocList) return;

  const headings = document.querySelectorAll('.docs-body h2');
  if (headings.length === 0) return;

  headings.forEach(h2 => {
    if (!h2.id) {
      h2.id = h2.textContent.trim().toLowerCase()
        .replace(/[^\w\s-]/g, '')
        .replace(/\s+/g, '-');
    }
    const li = document.createElement('li');
    const a = document.createElement('a');
    a.href = '#' + h2.id;
    a.textContent = h2.textContent.trim();
    li.appendChild(a);
    tocList.appendChild(li);
  });

  const tocLinks = tocList.querySelectorAll('a');

  // 120 = docs toolbar height (52px) + breathing room so the next heading
  // becomes "active" slightly before it reaches the very top of the viewport.
  const SCROLL_OFFSET = 120;

  function updateActiveHeading() {
    const scrollTop = window.scrollY + SCROLL_OFFSET;
    let activeId = null;
    headings.forEach(h => {
      if (h.offsetTop <= scrollTop) activeId = h.id;
    });
    tocLinks.forEach(link => {
      link.classList.toggle('active', activeId && link.getAttribute('href') === '#' + activeId);
    });
  }

  window.addEventListener('scroll', updateActiveHeading, { passive: true });
  updateActiveHeading();
}
