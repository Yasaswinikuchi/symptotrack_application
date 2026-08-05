// utils.js – shared helpers for performance optimization
// Debounce utility – prevents a function from being called too frequently
export function debounce(func, wait) {
  let timeout;
  return function (...args) {
    const later = () => {
      timeout = null;
      func.apply(this, args);
    };
    clearTimeout(timeout);
    timeout = setTimeout(later, wait);
  };
}

// Simple wrapper to retrieve and cache JSON from localStorage
export function getLocalStorageJSON(key, defaultValue = null) {
  if (!getLocalStorageJSON._cache) getLocalStorageJSON._cache = {};
  const cache = getLocalStorageJSON._cache;
  if (cache[key] !== undefined) return cache[key];
  try {
    const raw = localStorage.getItem(key);
    const parsed = raw ? JSON.parse(raw) : defaultValue;
    cache[key] = parsed;
    return parsed;
  } catch (e) {
    console.error('Failed to parse localStorage key', key, e);
    return defaultValue;
  }
}

// Simple wrapper to store JSON in localStorage
export function setLocalStorageJSON(key, value) {
  try {
    const str = JSON.stringify(value);
    localStorage.setItem(key, str);
    // Invalidate cache
    if (getLocalStorageJSON._cache) delete getLocalStorageJSON._cache[key];
  } catch (e) {
    console.error('Failed to set localStorage key', key, e);
  }
}
