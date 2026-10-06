const CACHE='vic-distinction-v2';
const ASSETS=['./','./index.html','./styles.css','./app.js','./premium.js','./pwa.js','./vic-logo.svg','./vic-hero.svg','./vic-hero-girl.png','./manifest.webmanifest'];
self.addEventListener('install',event=>{
  self.skipWaiting();
  event.waitUntil(caches.open(CACHE).then(cache=>cache.addAll(ASSETS)));
});
self.addEventListener('activate',event=>{
  event.waitUntil((async()=>{
    const keys=await caches.keys();
    await Promise.all(keys.filter(k=>k!==CACHE).map(k=>caches.delete(k)));
    await self.clients.claim();
  })());
});
self.addEventListener('fetch',event=>{
  if(event.request.method!=='GET') return;
  const url=new URL(event.request.url);
  if(url.origin!==self.location.origin) return;
  event.respondWith((async()=>{
    try{
      const fresh=await fetch(event.request,{cache:'no-store'});
      const copy=fresh.clone();
      const cache=await caches.open(CACHE);
      cache.put(event.request,copy);
      return fresh;
    }catch(err){
      const cached=await caches.match(event.request);
      if(cached) return cached;
      if(event.request.mode==='navigate') return caches.match('./index.html');
      throw err;
    }
  })());
});