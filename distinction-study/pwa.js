
(()=>{
  let deferredPrompt=null;
  const b=document.getElementById('vicInstallBtn');
  if(!b)return;
  window.addEventListener('beforeinstallprompt',e=>{e.preventDefault();deferredPrompt=e;b.style.display='inline-flex';});
  b.addEventListener('click',async()=>{if(!deferredPrompt)return;deferredPrompt.prompt();await deferredPrompt.userChoice;deferredPrompt=null;b.style.display='none';});
  window.addEventListener('appinstalled',()=>{b.style.display='none';});
  if('serviceWorker' in navigator){window.addEventListener('load',()=>navigator.serviceWorker.register('./sw.js').catch(()=>{}));}
})();
