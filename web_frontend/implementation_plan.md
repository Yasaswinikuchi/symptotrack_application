# Optimize SymtoTrack Pro Performance

## Goal
Reduce page load times, improve UI responsiveness, and eliminate noticeable lag across all web‑frontend pages.

## User Review Required
> [!IMPORTANT]
> This plan introduces several structural changes:
> - Consolidated and minified CSS (`style.min.css`).
> - New utility module `utils.js` (debounce, cached localStorage access).
> - Deferred and async script loading for heavy third‑party libraries.
> - Lazy‑loading of Chart.js, html2canvas, jsPDF, etc.
> - Service‑worker (`sw.js`) for caching static assets.
> - Minified JavaScript files (`*.min.js`).
> - Refactor DOM updates to use `DocumentFragment` and `requestAnimationFrame`.
> - Updated HTML pages to reference the new assets.
>
> Please confirm you want to proceed with these changes.

## Open Questions
1. **Build tooling:** Should we set up a simple bundler (e.g., Vite) for automatic minification, or keep the manual approach we are using now?
2. **Service worker caching:** Do you want a full offline‑first experience (Cache‑First for all static assets) or just a lightweight Cache‑First for CSS/JS/HTML?
3. **Image assets:** Should we convert existing PNG/SVG icons to data‑URIs or keep them as separate files?

## Proposed Changes
---
### Styles
- **[NEW] [style.min.css](file:///c:/Users/yasaswini%20kuchi/Downloads/SymtoTrack_Source/web_frontend/style.min.css)** – minified version of `style.css` (already created).
- Update each HTML `<link rel="stylesheet">` to load `style.min.css` with `rel="preload" as="style" onload="this.rel='stylesheet'"`.

---
### Utilities
- **[NEW] [utils.js](file:///c:/Users/yasaswini%20kuchi/Downloads/SymtoTrack_Source/web_frontend/utils.js)** – debouncing and cached localStorage helpers (already created).

---
### Script Loading
- Add `defer` attribute to all custom script tags.
- Replace direct third‑party script imports with dynamic `import()` blocks that load only when needed (e.g., Timeline, Heatmap, Reports).
- Example for Timeline page:
  ```html
  <script type="module" defer>
    import('https://cdn.jsdelivr.net/npm/chart.js').then(() => import('./timeline.min.js'));
  </script>
  ```

---
### Minified JavaScript
- Run `terser` on existing custom scripts and create corresponding `.min.js` files:
  - `login.min.js`, `dashboard.min.js`, `timeline.min.js`, `heatmap.min.js`, `chatbot.min.js`, `reminders.min.js`, `family.min.js`, `reports.min.js`, `payment.min.js`, etc.
- Update HTML to reference the `.min.js` versions.

---
### DOM Performance Refactors
- In `timeline.js` and `heatmap.js`, build UI elements inside a `DocumentFragment` before appending to the DOM.
- Wrap animation‑heavy code in `requestAnimationFrame`.
- Use the `debounce` utility from `utils.js` for resize/scroll listeners.
- Cache parsed localStorage data using `getLocalStorageJSON` from `utils.js`.

---
### Service Worker
- **[NEW] [sw.js](file:///c:/Users/yasaswini%20kuchi/Downloads/SymtoTrack_Source/web_frontend/sw.js)** – registers a Service Worker that:
  - Caches `style.min.css`, all `*.min.js`, all HTML pages, fonts, and SVG assets.
  - Uses **Cache‑First** for static assets and **Network‑First** for API calls (`login.php`, `analyze.php`).
  - Updates cache version on each new deployment.
- Add registration script to a small inline `<script>` in `index.html` (and optionally other pages).

---
### Font Load Optimisation
- Add `font-display: swap;` to Google Font `@font-face` declarations (already present in `style.css`).
- Add `<link rel="preconnect" href="https://fonts.googleapis.com">` and `<link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>` in the `<head>` of each HTML page.

---
### Performance Metrics (optional)
- Insert a tiny non‑blocking script that logs `performance.timing` to the console for future profiling.

## Verification Plan
- **Load‑time test:** Use Chrome DevTools throttling (3G) and measure `loadEventEnd - navigationStart`. Target ≤ 1.5 s.
- **Interaction lag:** Record main‑thread activity while opening Timeline chart and Heatmap; ensure < 30 ms per frame.
- **Service Worker:** Verify assets are served from cache on subsequent loads (no network requests for CSS/JS).
- **Mobile test:** Open the app on the phone (via local IP) with throttled network; confirm smooth scrolling and instant button response.
- **Console:** Ensure zero errors/warnings after changes.

---
*Once you approve the plan, I will execute the changes across the repository, generate the minified assets, and update all HTML references.*
