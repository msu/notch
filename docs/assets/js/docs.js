// `<script type="module">` is auto-deferred per spec, so by the time this
// file executes the DOM is already parsed. No DOMContentLoaded guard needed.
import { init as initDocsSidebar } from './modules/docs-sidebar.js';
import { init as initDocsSearch } from './modules/docs-search.js';
import { init as initDocsToc } from './modules/docs-toc.js';
import { init as initSearchHighlight } from './modules/search-highlight.js';
import { init as initMobileMenu } from './modules/mobile-menu.js';

initDocsSidebar();
initDocsSearch();
initDocsToc();
initSearchHighlight();
initMobileMenu();
