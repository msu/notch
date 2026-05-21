import { CONFIG } from './config.js';

function initToggle() {
  const btn = document.querySelector('.sidebar-toggle');
  if (!btn) {
    document.documentElement.classList.remove('sidebar-collapsed');
    return;
  }

  const html = document.documentElement;

  function setCollapsed(isCollapsed) {
    html.classList.toggle('sidebar-collapsed', isCollapsed);
    btn.setAttribute('aria-expanded', !isCollapsed);
    btn.setAttribute('aria-label', isCollapsed ? 'Show sidebar' : 'Hide sidebar');
    localStorage.setItem('sidebar-collapsed', isCollapsed);
  }

  // The inline head-script applies the persisted class before this module
  // loads, so reading the DOM here equivalent to reading localStorage.
  setCollapsed(html.classList.contains('sidebar-collapsed'));

  btn.addEventListener('click', () => {
    setCollapsed(!html.classList.contains('sidebar-collapsed'));
  });

  window.addEventListener('resize', () => {
    if (window.innerWidth <= CONFIG.BREAKPOINT) {
      html.classList.remove('sidebar-collapsed');
    } else if (localStorage.getItem('sidebar-collapsed') === 'true') {
      html.classList.add('sidebar-collapsed');
    }
  });
}

function initResize() {
  const handle = document.querySelector('.sidebar-resize-indicator');
  if (!handle || window.innerWidth <= CONFIG.BREAKPOINT) return;

  const MIN = 160, MAX = 400;
  const clamp = x => Math.min(MAX, Math.max(MIN, x));

  const saved = parseInt(localStorage.getItem('nav-width'));
  if (saved && saved >= MIN && saved <= MAX) {
    document.documentElement.style.setProperty('--nav-width', saved + 'px');
  }

  handle.addEventListener('mousedown', e => {
    e.preventDefault();
    handle.classList.add('dragging');
    document.body.style.userSelect = 'none';

    const onMove = e => {
      document.documentElement.style.setProperty('--nav-width', clamp(e.clientX) + 'px');
    };
    const onUp = e => {
      handle.classList.remove('dragging');
      document.body.style.userSelect = '';
      localStorage.setItem('nav-width', clamp(e.clientX));
      document.removeEventListener('mousemove', onMove);
      document.removeEventListener('mouseup', onUp);
    };
    document.addEventListener('mousemove', onMove);
    document.addEventListener('mouseup', onUp);
  });
}

export function init() {
  initToggle();
  initResize();
}
