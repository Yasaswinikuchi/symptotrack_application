// sw.js – Service Worker v2 – busts old v1 cache
const CACHE_NAME = 'symptomtrack-v2';
const ASSETS = [
  '/index.html',
  '/login.html',
  '/signup.html',
  '/dashboard.html',
  '/symptoms.html',
  '/chatbot.html',
  '/reminders.html',
  '/family.html',
  '/reports.html',
  '/payment.html',
  '/profile.html',
  '/alerts.html',
  '/history.html',
  '/history_result.html',
  '/nearby.html',
  '/scan.html',
  '/voice.html',
  '/timeline.html',
  '/heatmap.html',
  '/style.css',
  '/dashboard.js',
  '/chatbot.js',
  '/reminders.js',
  '/family.js',
  '/timeline.js',
  '/heatmap.js',
  '/payment.js',
  '/nearby.js',
  '/profile.js',
  '/symptoms.js',
  '/ml.js',
  '/utils.js'
];

self.addEventListener('install', event => {
  event.waitUntil(
    caches.open(CACHE_NAME).then(cache => {
      return Promise.allSettled(ASSETS.map(url => cache.add(url)));
    })
  );
  self.skipWaiting(); // activate immediately
});

self.addEventListener('activate', event => {
  event.waitUntil(
    caches.keys().then(keys =>
      Promise.all(keys.filter(k => k !== CACHE_NAME).map(k => caches.delete(k)))
    )
  );
  self.clients.claim(); // take control of all tabs
});

self.addEventListener('fetch', event => {
  const url = new URL(event.request.url);
  // Always network-first for API and PHP calls
  if (url.pathname.includes('.php') || url.pathname.includes('/api/')) {
    event.respondWith(
      fetch(event.request).catch(() => new Response('{}', {headers:{'Content-Type':'application/json'}}))
    );
    return;
  }
  // Cache-first for static assets
  event.respondWith(
    caches.match(event.request).then(cached => cached || fetch(event.request).then(res => {
      // Also cache newly fetched assets
      if (res && res.status === 200) {
        const clone = res.clone();
        caches.open(CACHE_NAME).then(c => c.put(event.request, clone));
      }
      return res;
    }))
  );
});
