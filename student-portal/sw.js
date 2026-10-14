const CACHE_NAME = 'vta-shell-v41';
const SHELL = ['./', './index.html'];
self.addEventListener('install',event=>{
  event.waitUntil(caches.open(CACHE_NAME).then(cache=>cache.addAll(SHELL)).then(()=>self.skipWaiting()));
});
self.addEventListener('activate',event=>{
  event.waitUntil(caches.keys().then(keys=>Promise.all(keys.filter(key=>key.startsWith('vta-shell-')&&key!==CACHE_NAME).map(key=>caches.delete(key)))).then(()=>self.clients.claim()));
});
self.addEventListener('fetch',event=>{
  if(event.request.method!=='GET')return;
  const url=new URL(event.request.url);
  if(url.origin!==self.location.origin)return;
  if(event.request.mode==='navigate'){
    event.respondWith(fetch(event.request,{cache:'no-store'}).then(response=>{
      if(response.ok){const copy=response.clone();event.waitUntil(caches.open(CACHE_NAME).then(cache=>cache.put('./index.html',copy)))}
      return response;
    }).catch(()=>caches.match('./index.html')));
    return;
  }
  if(['style','script'].includes(event.request.destination)){
    event.respondWith(fetch(event.request,{cache:'no-cache'}).catch(()=>caches.match(event.request)));
  }
});
