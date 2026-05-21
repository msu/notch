import { CONFIG } from './config.js';

export function init() {
  const hamburger = document.querySelector('.hamburger');
  const nav = document.querySelector('#main-nav');
  const navOverlay = document.querySelector('.nav-overlay');

  if (!hamburger || !nav) return;

  function setState(isOpen) {
    nav.classList.toggle('open', isOpen);
    hamburger.classList.toggle('active', isOpen);
    navOverlay?.classList.toggle('visible', isOpen);
    hamburger.setAttribute('aria-expanded', isOpen);
    navOverlay?.setAttribute('aria-hidden', !isOpen);
    document.body.style.overflow = isOpen ? 'hidden' : '';
  }

  hamburger.addEventListener('click', () => {
    setState(!nav.classList.contains('open'));
  });

  navOverlay?.addEventListener('click', () => setState(false));

  nav.addEventListener('click', e => {
    const link = e.target.closest('a');
    if (link && window.innerWidth <= CONFIG.BREAKPOINT) {
      setState(false);
    }
  });

  document.addEventListener('keydown', e => {
    if (e.key === 'Escape' && nav.classList.contains('open')) {
      setState(false);
    }
  });

  window.addEventListener('resize', () => {
    if (window.innerWidth > CONFIG.BREAKPOINT) {
      setState(false);
    }
  });
}
