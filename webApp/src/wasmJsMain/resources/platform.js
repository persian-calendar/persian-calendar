(() => {
  const prefix = 'persian-calendar.';
  let listener;
  const status = text => { document.getElementById('status').textContent = text; };
  const parts = (time, zone) => Object.fromEntries(new Intl.DateTimeFormat('en-CA', {
    timeZone: zone, year: 'numeric', month: '2-digit', day: '2-digit',
    hour: '2-digit', minute: '2-digit', second: '2-digit', hourCycle: 'h23',
  }).formatToParts(time).filter(part => part.type !== 'literal').map(part => [part.type, Number(part.value)]));
  globalThis.calendarPlatform = {
    read(key) { try { return localStorage.getItem(prefix + key); } catch { return null; } },
    write(key, value) {
      try { localStorage.setItem(prefix + key, value); }
      catch { status('تنظیمات در این مرورگر ذخیره نشد. Browser storage is unavailable.'); }
    },
    today() {
      const d = new Date();
      return Math.floor(Date.UTC(d.getFullYear(), d.getMonth(), d.getDate()) / 86400000) + 2440588;
    },
    zones() {
      return [...new Set(['UTC', 'Asia/Tehran', Intl.DateTimeFormat().resolvedOptions().timeZone,
        ...(Intl.supportedValuesOf ? Intl.supportedValuesOf('timeZone') : ['Asia/Kabul', 'Asia/Kathmandu', 'Europe/London', 'America/New_York'])])].sort().join('|');
    },
    time(time, zone) { return new Intl.DateTimeFormat('en-GB', { timeZone:zone, dateStyle:'medium', timeStyle:'short' }).format(time); },
    offset(jdn, zone) {
      const time = (jdn - 2440588) * 86400000 + 12 * 3600000;
      const p = parts(time, zone);
      return (Date.UTC(p.year, p.month - 1, p.day, p.hour, p.minute, p.second) - time) / 3600000;
    },
    async copy(text) {
      try { await navigator.clipboard.writeText(text); status('کپی شد · Copied'); }
      catch { status('کپی خودکار ممکن نیست؛ متن را انتخاب و کپی کنید. Select the text to copy it.'); }
    },
    async share(text) {
      if (!navigator.share) return this.copy(text);
      try { await navigator.share({ title:document.title, text }); }
      catch (error) { if (error.name !== 'AbortError') this.copy(text); }
    },
    save(name, mime, content) {
      const url = URL.createObjectURL(new Blob([content], { type:mime }));
      const link = document.createElement('a'); link.href = url; link.download = name;
      document.body.append(link); link.click(); link.remove();
      setTimeout(() => URL.revokeObjectURL(url), 10000);
    },
    subscribe(callback) {
      this.unsubscribe(); listener = () => callback(location.hash.slice(1) || 'calendar');
      window.addEventListener('hashchange', listener);
    },
    unsubscribe() { if (listener) window.removeEventListener('hashchange', listener); listener = undefined; },
    loadError() {
      const loading = document.getElementById('loading');
      if (loading) loading.textContent = 'بارگذاری نشد. اتصال را بررسی و از مرورگر به‌روز با پشتیبانی WebAssembly استفاده کنید. Unable to load. Check your connection and use a current browser with WebAssembly support, then reload.';
    },
  };
})();
