import test from 'node:test';
import assert from 'node:assert/strict';
import { readFileSync } from 'node:fs';
import vm from 'node:vm';

function environment({ storageDenied = false, clipboardDenied = false } = {}) {
  const store = new Map();
  const elements = { status: { textContent:'' }, loading: { textContent:'' } };
  const handlers = new Map();
  const location = { hash:'#calendar' };
  const context = vm.createContext({
    Date, Intl, URL, Blob, setTimeout,
    location,
    document: { title:'Persian Calendar', getElementById:id => elements[id] },
    navigator: { clipboard: { async writeText(text) { if (clipboardDenied) throw new Error('Denied'); store.set('clipboard', text); } } },
    localStorage: {
      getItem(key) { if (storageDenied) throw new Error('Denied'); return store.get(key) ?? null; },
      setItem(key, value) { if (storageDenied) throw new Error('Denied'); store.set(key, value); },
    },
    window: {
      location,
      addEventListener:(type, listener) => handlers.set(type, listener),
      removeEventListener:(type, listener) => { if (handlers.get(type) === listener) handlers.delete(type); },
    },
  });
  vm.runInContext(readFileSync(new URL('./src/wasmJsMain/resources/platform.js', import.meta.url), 'utf8'), context);
  return { platform:context.calendarPlatform, store, elements, handlers, location };
}

test('manual time zones use the selected date and DST rules', () => {
  const { platform } = environment();
  const jdn = (year, month, day) => Date.UTC(year, month - 1, day) / 86400000 + 2440588;
  assert.equal(platform.offset(jdn(2026, 1, 15), 'Asia/Tehran'), 3.5);
  assert.equal(platform.offset(jdn(2026, 1, 15), 'America/New_York'), -5);
  assert.equal(platform.offset(jdn(2026, 7, 15), 'America/New_York'), -4);
  assert.equal(platform.offset(jdn(2026, 7, 15), 'Asia/Kathmandu'), 5.75);
});

test('preferences persist under a dedicated prefix and handle denied storage', () => {
  const { platform, store } = environment();
  platform.write('language', 'fa');
  assert.equal(platform.read('language'), 'fa');
  assert.equal(store.get('persian-calendar.language'), 'fa');
  const denied = environment({ storageDenied:true });
  assert.equal(denied.platform.read('language'), null);
  assert.doesNotThrow(() => denied.platform.write('language', 'en'));
  assert.match(denied.elements.status.textContent, /storage is unavailable/);
});

test('browser history subscriptions are removed cleanly', () => {
  const { platform, handlers, location } = environment();
  let route;
  platform.subscribe(value => { route = value; });
  location.hash = '#astronomy';
  handlers.get('hashchange')();
  assert.equal(route, 'astronomy');
  platform.unsubscribe();
  assert.equal(handlers.size, 0);
});

test('sharing falls back to clipboard and clipboard denial is reported', async () => {
  const { platform, store } = environment();
  await platform.share('۱۴۰۵');
  assert.equal(store.get('clipboard'), '۱۴۰۵');
  const denied = environment({ clipboardDenied:true });
  await denied.platform.copy('text');
  assert.match(denied.elements.status.textContent, /Select the text/);
});
