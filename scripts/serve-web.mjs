import http from 'node:http';
import { createReadStream, existsSync, statSync } from 'node:fs';
import { resolve, extname, sep } from 'node:path';

const root = resolve(process.argv[2] || 'webApp/build/dist/wasmJs/productionExecutable');
const port = Number(process.env.PORT || 8080);
if (!existsSync(resolve(root, 'index.html'))) throw new Error(`Build the website first: ${root}/index.html is missing`);
const types = { '.html':'text/html; charset=utf-8', '.js':'text/javascript; charset=utf-8', '.mjs':'text/javascript; charset=utf-8', '.wasm':'application/wasm', '.json':'application/json; charset=utf-8', '.css':'text/css; charset=utf-8', '.ttf':'font/ttf', '.woff2':'font/woff2', '.svg':'image/svg+xml', '.png':'image/png', '.txt':'text/plain; charset=utf-8' };
http.createServer((request, response) => {
  let path;
  try { path = resolve(root, '.' + decodeURIComponent(new URL(request.url, 'http://localhost').pathname)); }
  catch { response.writeHead(400).end(); return; }
  if (path !== root && !path.startsWith(root + sep)) { response.writeHead(403).end(); return; }
  if (path === root) path = resolve(root, 'index.html');
  if (!existsSync(path) || !statSync(path).isFile()) { response.writeHead(404).end(); return; }
  response.writeHead(200, { 'Content-Type':types[extname(path)] || 'application/octet-stream', 'Cache-Control':'no-cache' });
  createReadStream(path).pipe(response);
}).listen(port, '127.0.0.1', () => console.log(`Persian Calendar: http://127.0.0.1:${port}`));
