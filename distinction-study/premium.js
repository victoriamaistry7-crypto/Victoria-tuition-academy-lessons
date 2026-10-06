
/* ===== VIC DISTINCTION V6 · INTERACTION POLISH ===== */
const V6_TOOL_META={
 papers:{icon:'▣',label:'Past Paper Practice'},mock:{icon:'◫',label:'Timed Topic Tests'},flash:{icon:'◇',label:'Flashcards'},mistakes:{icon:'↺',label:'Mistake Book'},progress:{icon:'▥',label:'Progress & Reports'},planner:{icon:'▦',label:'Study Planner'},focus:{icon:'◷',label:'Focus Mode'},vault:{icon:'▤',label:'Formula & Exam Vault'},resources:{icon:'▰',label:'Resources'},settings:{icon:'⚙',label:'Settings'}
};
function v6LearnerName(){try{return v5ActiveProfile().name||'Learner'}catch(e){return 'Learner'}}
function v6ToolContext(kind){
 const d=dxLoad(),m=V6_TOOL_META[kind]||{icon:'✦',label:'Study Tool'};
 const topic=S.d?(S.topic||S.d.name):'No active topic';
 const subject=S.d?S.sub:'Choose a topic to connect this tool';
 return {m,d,topic,subject};
}
function v6DecorateFeature(kind){
 const head=document.querySelector('#featureView .featureHead'); if(!head)return;
 head.dataset.kind=kind;
 let icon=head.querySelector('.v6ToolIcon'); if(!icon){icon=document.createElement('div');icon.className='v6ToolIcon';head.prepend(icon)}
 const ctx=v6ToolContext(kind);icon.textContent=ctx.m.icon;
 const first=head.querySelector(':scope > div:not(.v6ToolIcon):not(.featureHeadActions)'); if(!first)return;
 let row=first.querySelector('.v6Context');if(!row){row=document.createElement('div');row.className='v6Context';first.appendChild(row)}
 const target=ctx.d?.settings?.target||80;
 row.innerHTML=`<span>👤 <b>${esc(v6LearnerName())}</b></span><span>🎯 Target <b>${target}%</b></span><span>📚 <b>${esc(ctx.topic)}</b></span><span>${S.d?'● Active session':'○ No session'}</span>`;
}
const _v6RenderFeature=renderFeature;
renderFeature=function(kind=featureKind){_v6RenderFeature(kind);v6DecorateFeature(kind)};
const _v6OpenFeature=openFeature;
openFeature=function(kind){_v6OpenFeature(kind);v6DecorateFeature(kind)};
featureNeedTopic=function(title='Choose a topic first'){
 return `<div class="featureCard full"><div class="featureEmpty"><strong>${esc(title)}</strong><span>Choose a grade, subject and topic once. Vic will connect this tool to that active session and keep your work linked across Notes, Practice, Flashcards, Mistakes and Progress.</span><div class="dxActions"><button class="btn primary" onclick="nav('setupView')">Choose a topic</button><button class="btn secondary" onclick="nav('homeView')">Quick Start</button></div></div></div>`
};
function v6Toast(message,type='ok'){
 let t=document.getElementById('v6Toast');if(!t){t=document.createElement('div');t.id='v6Toast';t.style.cssText='position:fixed;right:22px;bottom:22px;z-index:9999;max-width:340px;padding:13px 15px;border-radius:13px;background:#111b2d;border:1px solid rgba(181,122,255,.28);color:#f7f7fb;font:700 11px/1.45 Inter,Segoe UI,sans-serif;box-shadow:0 18px 50px #0008;opacity:0;transform:translateY(8px);transition:.2s';document.body.appendChild(t)}
 t.textContent=message;t.style.borderColor=type==='ok'?'rgba(85,215,160,.32)':'rgba(251,113,133,.32)';t.style.opacity='1';t.style.transform='none';clearTimeout(v6Toast._t);v6Toast._t=setTimeout(()=>{t.style.opacity='0';t.style.transform='translateY(8px)'},2600)
}
if(typeof featureSaveSettings==='function'){
 const _v6SaveSettings=featureSaveSettings;featureSaveSettings=function(){_v6SaveSettings();v6Toast('Study settings saved for '+v6LearnerName()+'.')}
}
document.addEventListener('keydown',e=>{
 if((e.ctrlKey||e.metaKey)&&e.key.toLowerCase()==='k'){e.preventDefault();const q=document.getElementById('globalSearch');if(q){nav('homeView');setTimeout(()=>q.focus(),50)}}
 if(e.key==='Escape'){document.querySelectorAll('.picker.open').forEach(p=>p.classList.remove('open'));const modal=document.getElementById('modal');if(modal?.classList.contains('show'))modal.classList.remove('show')}
});
const v6Titles={home:'Home dashboard',hub:'Distinction Hub',topics:'Topics & Notes',practice:'Practice Questions',papers:'Past Paper Practice',mock:'Timed Topic Tests',flash:'Flashcards',mistakes:'Mistake Book',progress:'Progress & Reports',planner:'Study Planner',focus:'Focus Mode',vault:'Formula & Exam Vault',resources:'Resources',settings:'Settings'};
document.querySelectorAll('#sideMenu [data-side]').forEach(b=>{b.title=v6Titles[b.dataset.side]||b.textContent.trim()});
const v6KpiRoutes=['progress','progress','planner','progress','focus'];
document.querySelectorAll('.homeKpi').forEach((el,i)=>{el.style.cursor='pointer';el.tabIndex=0;el.setAttribute('role','button');el.onclick=()=>openFeature(v6KpiRoutes[i]||'progress');el.onkeydown=e=>{if(e.key==='Enter'||e.key===' '){e.preventDefault();el.click()}}});
document.querySelectorAll('.achievement').forEach(el=>{el.style.cursor='pointer';el.onclick=()=>openFeature('progress')});
if(typeof featureKind!=='undefined'&&document.getElementById('featureView')?.classList.contains('active'))v6DecorateFeature(featureKind);
