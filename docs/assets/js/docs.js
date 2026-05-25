// `<script type="module">` is auto-deferred per spec, so by the time this
// file executes the DOM is already parsed. No DOMContentLoaded guard needed.
// Sidebar collapse, sidebar resize, and the mobile hamburger are wired via
// hyperscript on their elements; only the search engine, TOC builder, and
// post-navigation highlight handler remain in JS.
import { init as initDocsSearch } from './modules/docs-search.js';
import { init as initDocsToc } from './modules/docs-toc.js';
import { init as initSearchHighlight } from './modules/search-highlight.js';

initDocsSearch();
initDocsToc();
initSearchHighlight();
