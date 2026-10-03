const API='https://xrmljvdyxqyegclkazei.supabase.co/functions/v1/vta-api';
const app=document.getElementById('app');
const toastRoot=document.getElementById('toast-root');
const VTA_LOGO_DATA='data:image/jpeg;base64,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';
const VTA_DASHBOARD_VISUAL='assets/dashboard-visual.svg';
const isInstalled=()=>window.vtaIsInstalled?window.vtaIsInstalled():window.matchMedia?.('(display-mode: standalone)').matches;

const state={
  token:localStorage.getItem('vta_token')||'',
  data:null,tab:'Home',theme:localStorage.getItem('vta_theme')||'light',
  studentId:null,chatStudentId:null,resourceCategory:null,bookingSlot:null,timer:null
};
document.documentElement.dataset.theme=state.theme;

const esc=s=>String(s??'').replace(/[&<>"']/g,m=>({'&':'&amp;','<':'&lt;','>':'&gt;','"':'&quot;',"'":'&#039;'}[m]));
const money=c=>c==null?'Not recorded':'R'+(Number(c)/100).toLocaleString('en-ZA',{minimumFractionDigits:2,maximumFractionDigits:2});
const shortTime=t=>t?String(t).slice(0,5):'Time TBC';
const dateLabel=d=>{if(!d)return'';const x=new Date(d+'T12:00:00');return x.toLocaleDateString('en-ZA',{day:'numeric',month:'short',year:'numeric'})};
const initials=s=>String(s||'?').trim().split(/\s+/).map(x=>x[0]).slice(0,2).join('').toUpperCase();
const studentName=id=>state.data?.students?.find(x=>x.id===id)?.display_name||state.data?.user?.displayName||'Student';
const findStudent=id=>state.data?.students?.find(x=>x.id===id);
const badge=s=>{
  const v=String(s||'');
  const cls=/covered|completed/i.test(v)?'green':/next|revisit|pending/i.test(v)?'orange':/current|confirmed|scheduled/i.test(v)?'blue':/resource/i.test(v)?'purple':/declined|cancelled/i.test(v)?'red':'grey';
  return '<span class="badge '+cls+'">'+esc(v||'—')+'</span>';
};
function toast(msg,error=false){const n=document.createElement('div');n.className='toast'+(error?' error':'');n.textContent=msg;toastRoot.appendChild(n);setTimeout(()=>n.remove(),3200)}
async function api(payload,auth=true){
  const headers={'content-type':'application/json'};
  if(auth&&state.token)headers.authorization='Bearer '+state.token;
  const r=await fetch(API,{method:'POST',headers,body:JSON.stringify(payload)});
  let out={};try{out=await r.json()}catch{}
  if(!r.ok)throw new Error(out.error||'Request failed');
  return out;
}
async function sync(show=true){
  try{
    const oldIds=new Set((state.data?.notifications||[]).map(n=>n.id));
    state.data=await api({action:'sync'});
    if(!state.studentId&&state.data.students?.length)state.studentId=state.data.students[0].id;
    if(!state.chatStudentId&&state.data.students?.length)state.chatStudentId=state.data.students[0].id;
    const fresh=(state.data.notifications||[]).filter(n=>!oldIds.has(n.id)&&!n.read_at);
    if(oldIds.size&&fresh.length&&window.Notification){
      fresh.slice(0,3).forEach(n=>{try{new Notification(n.title||'VTA',{body:n.body||''})}catch{}});
    }
    if(show)renderApp();
  }catch(e){
    if(/Unauthorized/i.test(e.message)){logout();return}
    toast(e.message,true);
  }
}
function logout(){localStorage.removeItem('vta_token');state.token='';state.data=null;clearInterval(state.timer);renderLogin()}
function setTheme(v){state.theme=v;localStorage.setItem('vta_theme',v);document.documentElement.dataset.theme=v;if(state.data)renderApp();else renderLogin()}
function logo(){return '<img src="'+VTA_LOGO_DATA+'" alt="Victoria Tuition Academy logo">'}
function renderLogin(){
  app.innerHTML=`
  <div class="login-page">
    <section class="login-art"><div class="login-brand">
      ${logo()}
      <h1>Learning, organised beautifully.</h1>
      <p>Your lessons, bookings, resources, syllabus progress and tutor conversations in one secure Victoria Tuition Academy workspace.</p>
    </div></section>
    <section class="login-form-wrap"><form id="login-form" class="login-card">
      <div class="actions" style="justify-content:flex-end;margin-bottom:28px"><button type="button" id="theme-login" class="btn secondary">${state.theme==='dark'?'Light mode':'Dark mode'}</button></div>
      <h2>Welcome back</h2><div class="sub">Sign in to your VTA portal. Your role is detected automatically.</div>
      <div class="field"><label>USERNAME</label><input id="username" class="input" autocomplete="username" required></div>
      <div class="field"><label>PASSWORD</label><input id="password" type="password" class="input" autocomplete="current-password" required></div>
      <div id="login-error" class="small" style="color:var(--red);min-height:20px"></div>
      <button class="btn primary" id="login-btn">Sign in</button>
    </form></section>
  </div>`;
  document.getElementById('theme-login').onclick=()=>setTheme(state.theme==='dark'?'light':'dark');
  document.getElementById('login-form').onsubmit=async e=>{
    e.preventDefault();const btn=document.getElementById('login-btn'),err=document.getElementById('login-error');
    btn.disabled=true;btn.textContent='Signing in…';err.textContent='';
    try{
      const r=await api({action:'login',username:document.getElementById('username').value.trim(),password:document.getElementById('password').value},false);
      state.token=r.token;localStorage.setItem('vta_token',state.token);await sync(false);startLive();renderApp();
    }catch(x){err.textContent=x.message}
    finally{btn.disabled=false;btn.textContent='Sign in'}
  };
}
function startLive(){clearInterval(state.timer);state.timer=setInterval(()=>sync(true),45000)}

function navItems(){
  if(state.data.user.role==='ADMIN')return ['Home','Students','Schedule','Messages','Resources','Quizzes','Finance'];
  return ['Home','Learn','Book','Messages','Resources','Quizzes'];
}
function renderApp(){
  const user=state.data.user,items=navItems();if(!items.includes(state.tab))state.tab='Home';
  app.innerHTML=`
  <div class="app-shell">
    <aside class="sidebar">
      <div class="brand">${logo()}<div class="brand-copy"><strong>VTA Portal</strong><span>${esc(user.role)}</span></div></div>
      <nav class="nav">${items.map(x=>`<button data-nav="${x}" class="${x===state.tab?'active':''}"><span class="dot"></span>${x}</button>`).join('')}</nav>
      <div class="sidebar-bottom">
        ${window.desktop?.platform==='web'&&!isInstalled()?'<button id="install-btn">⊞  Install on Windows</button>':''}
        <button id="theme-btn">${state.theme==='dark'?'☀  Light mode':'◐  Dark mode'}</button>
        <button id="logout-btn">↗  Log out</button>
      </div>
    </aside>
    <main class="main">
      <header class="topbar"><h1>${esc(state.tab)}</h1><div class="topbar-right"><span class="status-live">● Live sync</span><button id="refresh-btn" class="icon-btn">↻</button><div class="avatar">${esc(initials(user.displayName))}</div></div></header>
      <div class="content" id="view"></div>
    </main>
  </div>`;
  document.querySelectorAll('[data-nav]').forEach(b=>b.onclick=()=>{state.tab=b.dataset.nav;renderApp()});
  const installBtn=document.getElementById('install-btn');
  if(installBtn)installBtn.onclick=async()=>{
    if(isInstalled()){toast('VTA Portal is already installed on this device.');return;}
    const installed=window.vtaInstall?await window.vtaInstall():false;
    if(installed){toast('VTA Portal installed successfully.');renderApp();}
    else toast('Use the browser menu → Apps → Install VTA Portal if the install prompt is not available yet.');
  };
  document.getElementById('theme-btn').onclick=()=>setTheme(state.theme==='dark'?'light':'dark');
  document.getElementById('logout-btn').onclick=logout;
  document.getElementById('refresh-btn').onclick=()=>sync(true);
  renderView();
}

function pageHead(title,sub,actions=''){return `<div class="page-head"><div><h2>${esc(title)}</h2><p>${esc(sub)}</p></div><div class="actions">${actions}</div></div>`}
function metric(label,value,hint=''){return `<div class="card metric"><div class="label">${esc(label)}</div><div class="value">${esc(value)}</div><div class="hint">${esc(hint)}</div></div>`}
function renderView(){
  const admin=state.data.user.role==='ADMIN';
  if(admin){
    if(state.tab==='Students')return renderStudents();
    if(state.tab==='Schedule')return renderSchedule();
    if(state.tab==='Messages')return renderChat(true);
    if(state.tab==='Resources')return renderResources(true);
    if(state.tab==='Quizzes')return renderQuizzes(true);
    if(state.tab==='Finance')return renderFinance();
    return renderAdminHome();
  }else{
    if(state.tab==='Learn')return renderLearn();
    if(state.tab==='Book')return renderBooking();
    if(state.tab==='Messages')return renderChat(false);
    if(state.tab==='Resources')return renderResources(false);
    if(state.tab==='Quizzes')return renderQuizzes(false);
    return renderStudentHome();
  }
}

function lessonsCompletedValue(){
  return (state.data.lessons||[]).filter(l=>l.status==='Completed').reduce((s,l)=>s+Math.round(Number(l.duration_minutes||0)*150/60),0);
}
function thisWeekLessons(){
  const now=new Date(),start=new Date(now);start.setHours(0,0,0,0);start.setDate(now.getDate()-((now.getDay()+6)%7));
  const end=new Date(start);end.setDate(start.getDate()+7);
  return (state.data.lessons||[]).filter(l=>{const d=new Date(l.lesson_date+'T12:00:00');return d>=start&&d<end&&l.status!=='Cancelled'});
}
function weekPlanner(){
  const now=new Date(),start=new Date(now);start.setDate(now.getDate()-((now.getDay()+6)%7));
  let html='<div class="week">';
  for(let i=0;i<7;i++){const d=new Date(start);d.setDate(start.getDate()+i);const key=d.toISOString().slice(0,10);
    const list=(state.data.lessons||[]).filter(l=>l.lesson_date===key&&l.status!=='Cancelled').sort((a,b)=>(a.start_time||'').localeCompare(b.start_time||''));
    html+=`<div class="day"><div class="day-name">${d.toLocaleDateString('en-ZA',{weekday:'short'})}</div><div class="date">${d.getDate()}</div>${list.length?list.map(l=>`<div class="lesson-block"><b>${esc(shortTime(l.start_time))} · ${esc(studentName(l.student_id))}</b>${esc(l.topic||l.subject||'Lesson')}</div>`).join(''):'<div class="small" style="color:var(--green);font-weight:800">Open</div>'}</div>`;
  }return html+'</div>';
}
function renderAdminHome(){
  const pending=(state.data.bookings||[]).filter(x=>x.status==='Pending').length;
  const unread=(state.data.notifications||[]).filter(x=>!x.read_at).length;
  const next=(state.data.lessons||[]).filter(x=>x.status!=='Completed'&&x.status!=='Cancelled').sort((a,b)=>(a.lesson_date+(a.start_time||'')).localeCompare(b.lesson_date+(b.start_time||''))).slice(0,5);
  document.getElementById('view').innerHTML=
  pageHead('Good morning, '+state.data.user.displayName,'Your academy at a glance.',
    '<button class="btn primary" id="quick-lesson">＋ Add lesson</button><button class="btn secondary" id="quick-student">＋ Student</button>')+
  `<div class="dashboard-banner"><div class="dashboard-banner-copy"><span class="eyebrow">VICTORIA TUITION ACADEMY</span><h2>Your teaching workspace, fully synced.</h2><p>Lessons, bookings, syllabus progress, resources, quizzes, finance and messages stay together across desktop and Android.</p></div><img src="${VTA_DASHBOARD_VISUAL}" alt="Victoria Tuition Academy dashboard visual"></div>
  <div class="grid cols-4">
    ${metric('Students',state.data.students.length,'active portals')}
    ${metric('This week',thisWeekLessons().length+' lessons','confirmed and planned')}
    ${metric('Pending requests',pending,'waiting for your reply')}
    ${metric('Completed value','R'+lessonsCompletedValue().toLocaleString('en-ZA'),unread+' unread notifications')}
  </div>
  <div class="section-title"><h3>This week</h3><span class="small muted">Monday → Sunday</span></div>${weekPlanner()}
  <div class="grid cols-2" style="margin-top:22px">
    <div><div class="section-title"><h3>Upcoming lessons</h3></div><div class="list">${next.length?next.map(l=>lessonRow(l)).join(''):'<div class="empty">No upcoming lessons.</div>'}</div></div>
    <div><div class="section-title"><h3>Needs attention</h3></div>
      <div class="card"><div class="list">
        <div class="list-row"><div class="grow"><h4>Booking requests</h4><p>${pending} pending request(s)</p></div><button class="btn soft" data-goto="Schedule">Open</button></div>
        <div class="list-row"><div class="grow"><h4>Messages</h4><p>Keep student questions in one place</p></div><button class="btn soft" data-goto="Messages">Open</button></div>
        <div class="list-row"><div class="grow"><h4>Tracey payment ledger</h4><p>Only verified amounts are shown</p></div><button class="btn soft" data-goto="Finance">Open</button></div>
      </div></div>
    </div>
  </div>`;
  document.querySelectorAll('[data-goto]').forEach(b=>b.onclick=()=>{state.tab=b.dataset.goto;renderApp()});
  document.getElementById('quick-student').onclick=()=>openStudentEditor(null);
  document.getElementById('quick-lesson').onclick=()=>openLessonEditor(null);
}
function lessonRow(l){return `<div class="list-row"><div class="avatar">${esc(initials(studentName(l.student_id)))}</div><div class="grow"><h4>${esc(studentName(l.student_id))} · ${esc(l.topic||'Lesson')}</h4><p>${esc(dateLabel(l.lesson_date))} · ${esc(shortTime(l.start_time))} · ${esc(l.duration_minutes)} min · ${esc(l.subject||'')}</p></div>${badge(l.status)}</div>`}

function renderStudents(){
  const students=state.data.students||[];
  document.getElementById('view').innerHTML=pageHead('Students','Edit profiles, login details, syllabus progress and learning plans.','<button id="new-student" class="btn primary">＋ Create student</button>')+
  `<div class="grid cols-3">${students.map((s,i)=>{
    const topics=(state.data.topicProgress||[]).filter(t=>t.student_id===s.id),done=topics.filter(t=>/Covered|Completed/.test(t.status)).length;
    return `<div class="card clickable student-card" data-id="${s.id}"><div style="display:flex;gap:12px;align-items:center"><div class="avatar">${esc(initials(s.display_name))}</div><div><div class="strong">${esc(s.display_name)}</div><div class="small muted">${esc(s.grade||'Grade not set')} · ${esc(s.curriculum||'')}</div></div></div><p class="small muted">${esc(s.subjects||'Subjects not set')}</p><div style="display:flex;justify-content:space-between;align-items:center"><span class="badge blue">${done}/${topics.length||0} covered</span><span>›</span></div></div>`;
  }).join('')}</div><div id="student-detail"></div>`;
  document.getElementById('new-student').onclick=()=>openStudentEditor(null);
  document.querySelectorAll('.student-card').forEach(c=>c.onclick=()=>renderStudentDetail(c.dataset.id));
  if(state.studentId)renderStudentDetail(state.studentId);
}
function renderStudentDetail(id){
  state.studentId=id;const s=findStudent(id);if(!s)return;
  const topics=(state.data.topicProgress||[]).filter(t=>t.student_id===id).sort((a,b)=>a.sort_order-b.sort_order);
  const resources=(state.data.resources||[]).filter(r=>r.student_id===id);
  const lessons=(state.data.lessons||[]).filter(l=>l.student_id===id).sort((a,b)=>b.lesson_date.localeCompare(a.lesson_date));
  const plan=(state.data.plans||[]).filter(p=>p.student_id===id).sort((a,b)=>b.month_key.localeCompare(a.month_key))[0];
  document.getElementById('student-detail').innerHTML=`
  <div class="section-title"><h3>${esc(s.display_name)} workspace</h3><div class="actions"><button class="btn secondary" id="edit-profile">Edit profile</button><button class="btn secondary" id="reset-pass">Reset password</button><button class="btn primary" id="add-topic">＋ Topic</button></div></div>
  <div class="grid cols-3">
    <div class="card"><div class="small muted">LOGIN</div><div class="strong" style="margin-top:5px">${esc(s.username)}</div><div class="small muted" style="margin-top:10px">${esc(s.grade||'')} · ${esc(s.curriculum||'')}</div><p class="small">${esc(s.subjects||'')}</p></div>
    <div class="card"><div class="small muted">MONTHLY PLAN</div><div class="strong" style="margin-top:5px">${esc(plan?.lesson_target??0)} lessons</div><p class="small muted">${esc(plan?.goal||'No goal entered yet.')}</p><button class="btn soft" id="edit-plan">Edit plan</button></div>
    <div class="card"><div class="small muted">PROFILE NOTE</div><p class="small">${esc(s.profile_note||'No profile note yet.')}</p><div class="small muted">${resources.length} resources · ${lessons.length} lessons</div></div>
  </div>
  <div class="section-title"><h3>Syllabus & progress</h3><span class="small muted">Click a topic to edit it</span></div>
  <div class="card">${topics.length?topics.map(t=>`<div class="progress-row topic-row" data-id="${t.id}"><div><div class="strong">${esc(t.topic)}</div><div class="small muted">${esc(t.strand||'')} · ${esc(t.term_label||'')}</div></div><div class="small">${esc(t.subject||'')}</div><div>${badge(t.status)}</div><button class="btn soft">Edit</button></div>`).join(''):'<div class="empty">No syllabus topics yet.</div>'}</div>
  <div class="grid cols-2" style="margin-top:18px"><div><div class="section-title"><h3>Recent lessons</h3></div><div class="list">${lessons.slice(0,5).map(lessonRow).join('')||'<div class="empty">No lessons.</div>'}</div></div><div><div class="section-title"><h3>Resources</h3></div><div class="list">${resources.slice(0,5).map(resourceRow).join('')||'<div class="empty">No resources.</div>'}</div></div></div>`;
  document.getElementById('edit-profile').onclick=()=>openStudentEditor(s);
  document.getElementById('reset-pass').onclick=()=>resetPassword(s);
  document.getElementById('add-topic').onclick=()=>openTopicEditor(s,null);
  document.getElementById('edit-plan').onclick=()=>openPlanEditor(s,plan);
  document.querySelectorAll('.topic-row').forEach(r=>r.onclick=()=>openTopicEditor(s,topics.find(t=>t.id===r.dataset.id)));
}

function renderSchedule(){
  const req=(state.data.bookings||[]).filter(x=>x.status==='Pending');
  const lessons=(state.data.lessons||[]).slice().sort((a,b)=>(a.lesson_date+(a.start_time||'')).localeCompare(b.lesson_date+(b.start_time||'')));
  document.getElementById('view').innerHTML=pageHead('Schedule','Manage booking requests, lessons and your weekly planner.','<button class="btn primary" id="add-lesson">＋ Add lesson</button>')+
  '<div class="section-title"><h3>Weekly planner</h3></div>'+weekPlanner()+
  `<div class="section-title"><h3>Booking requests</h3><span class="badge orange">${req.length} pending</span></div><div class="list">${req.length?req.map(b=>`<div class="list-row"><div class="grow"><h4>${esc(studentName(b.student_id))} · ${esc(b.topic)}</h4><p>${esc(dateLabel(b.requested_date))} · ${esc(shortTime(b.requested_time))} · ${esc(b.duration_minutes)} min · ${esc(b.subject)}</p><p>${esc(b.help_text||'')}</p></div><button class="btn primary booking-action" data-id="${b.id}" data-status="Confirmed">Confirm</button><button class="btn danger booking-action" data-id="${b.id}" data-status="Declined">Decline</button></div>`).join(''):'<div class="empty">No pending booking requests.</div>'}</div>
  <div class="section-title"><h3>All lessons</h3></div><div class="list">${lessons.map(lessonRow).join('')}</div>`;
  document.getElementById('add-lesson').onclick=()=>openLessonEditor(null);
  document.querySelectorAll('.booking-action').forEach(b=>b.onclick=async()=>{try{await api({action:'bookingStatus',id:b.dataset.id,status:b.dataset.status});toast('Booking '+b.dataset.status.toLowerCase());await sync()}catch(e){toast(e.message,true)}});
}

function chatMessages(studentId){
  const me=state.data.user.id;
  return (state.data.messages||[]).filter(m=>m.student_id===studentId).slice().reverse().map(m=>{
    const mine=m.sender_id===me;let tm='';try{tm=new Date(m.created_at).toLocaleTimeString('en-ZA',{hour:'2-digit',minute:'2-digit'})}catch{}
    return `<div class="bubble ${mine?'mine':'theirs'}">${esc(m.body)}<time>${esc(tm)}</time></div>`
  }).join('');
}
function renderChat(admin){
  let contacts=admin?(state.data.students||[]):[{id:state.data.user.id,display_name:'Victoria'}];
  if(admin&&!state.chatStudentId&&contacts.length)state.chatStudentId=contacts[0].id;
  const sid=admin?state.chatStudentId:state.data.user.id;
  document.getElementById('view').innerHTML=pageHead('Messages','A compact tutor chat that stays synced with the mobile app.')+
  `<div class="chat-layout">
    <div class="chat-contacts"><h3>${admin?'Students':'Tutor'}</h3>${contacts.map(c=>`<div class="contact ${c.id===sid?'active':''}" data-id="${c.id}"><div class="avatar">${esc(initials(c.display_name))}</div><div><div class="strong">${esc(c.display_name)}</div><div class="small muted">Open conversation</div></div></div>`).join('')}</div>
    <div class="chat-panel"><div class="chat-head"><div class="avatar">${esc(initials(admin?studentName(sid):'Victoria'))}</div><div><div class="strong">${esc(admin?studentName(sid):'Victoria')}</div><div class="tiny">Victoria Tuition Academy</div></div></div><div class="chat-messages" id="chat-messages">${chatMessages(sid)||'<div class="empty">No messages yet.</div>'}</div><form class="chat-compose" id="chat-form"><input id="chat-input" class="input" placeholder="Type a message…" autocomplete="off"><button class="btn primary">Send</button></form></div>
  </div>`;
  document.querySelectorAll('.contact').forEach(c=>c.onclick=()=>{state.chatStudentId=c.dataset.id;renderChat(admin)});
  const cm=document.getElementById('chat-messages');cm.scrollTop=cm.scrollHeight;
  document.getElementById('chat-form').onsubmit=async e=>{e.preventDefault();const input=document.getElementById('chat-input'),msg=input.value.trim();if(!msg)return;input.value='';try{const p={action:'messageSend',message:msg};if(admin)p.studentId=sid;await api(p);await sync(false);renderChat(admin)}catch(x){toast(x.message,true)}};
}

const resourceIcon=t=>/interactive/i.test(t)?'✦':/slide/i.test(t)?'▤':/worksheet/i.test(t)?'✎':/video/i.test(t)?'▶':/quiz/i.test(t)?'✓':'□';
function resourceRow(r){return `<div class="list-row resource-open" data-id="${r.id}"><div class="avatar">${resourceIcon(r.resource_type)}</div><div class="grow"><h4>${esc(r.title)}</h4><p>${esc(r.resource_type)} · ${esc(r.completion_status||'Available')}</p>${r.access_note?'<p><b>Access:</b> '+esc(r.access_note)+'</p>':''}</div><button class="btn soft">Open</button></div>`}
function resourceGroups(resources){
  const types=['Interactive Lesson','Slides','PDF / Notes','Worksheet','Video','Image','Other'];
  return types.map(t=>({type:t,items:resources.filter(r=>String(r.resource_type).toLowerCase().includes(t.split(' ')[0].toLowerCase())||(t==='PDF / Notes'&&/pdf|notes/i.test(r.resource_type)))})).filter(g=>g.items.length);
}
function renderResources(admin){
  const resources=admin?(state.data.resources||[]):state.data.resources||[];
  const groups=resourceGroups(resources);
  document.getElementById('view').innerHTML=pageHead('Resources',admin?'Organised by type instead of one long page.':'Your interactive lessons, notes, slides and worksheets.',admin?'<button class="btn primary" id="add-resource">＋ Add resource</button>':'')+
  `<div class="resource-categories">${groups.length?groups.map(g=>`<div class="card clickable resource-cat" data-type="${esc(g.type)}"><div class="icon">${resourceIcon(g.type)}</div><div class="count">${g.items.length}</div><div class="strong">${esc(g.type)}</div><div class="small muted">Open category</div></div>`).join(''):'<div class="empty">No resources yet.</div>'}</div><div id="resource-list"></div>`;
  if(admin)document.getElementById('add-resource').onclick=()=>openResourceEditor(null);
  document.querySelectorAll('.resource-cat').forEach(c=>c.onclick=()=>showResourceCategory(c.dataset.type,admin));
  if(state.resourceCategory)showResourceCategory(state.resourceCategory,admin);
}
function showResourceCategory(type,admin){
  state.resourceCategory=type;
  const list=(state.data.resources||[]).filter(r=>String(r.resource_type).toLowerCase().includes(type.split(' ')[0].toLowerCase())||(type==='PDF / Notes'&&/pdf|notes/i.test(r.resource_type)));
  const root=document.getElementById('resource-list');if(!root)return;
  root.innerHTML=`<div class="section-title"><h3>${esc(type)}</h3><span class="small muted">${list.length} resource(s)</span></div><div class="list">${list.map(r=>`<div class="list-row"><div class="avatar">${resourceIcon(r.resource_type)}</div><div class="grow"><h4>${esc(r.title)}</h4><p>${admin?esc(studentName(r.student_id))+' · ':''}${esc(r.description||'')}</p>${r.access_note?'<p><b>Access:</b> '+esc(r.access_note)+'</p>':''}</div><button class="btn secondary open-resource" data-id="${r.id}">Open</button>${admin?'<button class="btn soft edit-resource" data-id="'+r.id+'">Edit</button>':''}</div>`).join('')}</div>`;
  root.querySelectorAll('.open-resource').forEach(b=>b.onclick=()=>openResource(b.dataset.id));
  root.querySelectorAll('.edit-resource').forEach(b=>b.onclick=()=>openResourceEditor((state.data.resources||[]).find(r=>r.id===b.dataset.id)));
}

function renderQuizzes(admin){
  const quizzes=state.data.quizzes||[], attempts=state.data.quizAttempts||[];
  document.getElementById('view').innerHTML=pageHead('Quizzes',admin?'Create quick checks and see submitted scores.':'Practice your topics and see instant feedback.',admin?'<button class="btn primary" id="new-quiz">＋ Create quiz</button>':'')+
  `<div class="grid cols-3">${quizzes.length?quizzes.map(q=>{const mine=attempts.filter(a=>a.quiz_id===q.id).sort((a,b)=>String(b.submitted_at).localeCompare(String(a.submitted_at)))[0];return `<div class="card clickable quiz-card" data-id="${q.id}"><div class="small muted">${admin?esc(studentName(q.student_id))+' · ':''}${esc(q.subject)}</div><h3>${esc(q.title)}</h3><p class="small muted">${esc(q.topic)} · ${q.total_marks} marks</p>${mine?'<div class="score">'+mine.score+'/'+mine.total_marks+'</div>':badge(q.status)}</div>`}).join(''):'<div class="empty">No quizzes yet.</div>'}</div>`;
  if(admin)document.getElementById('new-quiz').onclick=()=>openQuizCreator();
  document.querySelectorAll('.quiz-card').forEach(c=>c.onclick=()=>admin?showQuizSummary(c.dataset.id):takeQuiz(c.dataset.id));
}
function showQuizSummary(id){
  const q=(state.data.quizzes||[]).find(x=>x.id===id),questions=(state.data.quizQuestions||[]).filter(x=>x.quiz_id===id),attempts=(state.data.quizAttempts||[]).filter(x=>x.quiz_id===id);
  openDialog(q.title,`<div class="small muted">${esc(studentName(q.student_id))} · ${esc(q.subject)} · ${esc(q.topic)}</div><div class="section-title"><h3>Questions</h3></div>${questions.map((x,i)=>`<div class="card" style="margin-bottom:8px"><div class="strong">${i+1}. ${esc(x.prompt)}</div><div class="small muted">Answer: ${esc(x.answer)} · ${x.marks} mark(s)</div></div>`).join('')}<div class="section-title"><h3>Attempts</h3></div>${attempts.map(a=>`<div class="list-row"><div class="grow"><h4>${esc(studentName(a.student_id))}</h4><p>${esc(new Date(a.submitted_at).toLocaleString('en-ZA'))}</p></div><b>${a.score}/${a.total_marks}</b></div>`).join('')||'<div class="empty">No attempts yet.</div>'}`,'Close');
}

function renderFinance(){
  const completed=(state.data.lessons||[]).filter(l=>l.status==='Completed').sort((a,b)=>b.lesson_date.localeCompare(a.lesson_date));
  document.getElementById('view').innerHTML=pageHead('Finance','Verified completed lesson value, payments and invoices.','<button class="btn primary" id="new-payment">＋ Payment</button><button class="btn secondary" id="new-invoice">Create invoice</button>')+
  `<div class="grid cols-3">${metric('Completed lesson value','R'+lessonsCompletedValue().toLocaleString('en-ZA'),'R150/hour')}${metric('Completed lessons',completed.length,'only recorded completed lessons')}${metric('Payments',state.data.payments.length,'amounts may be blank if unverified')}</div>
  <div class="grid cols-2" style="margin-top:18px"><div><div class="section-title"><h3>Completed lessons</h3></div><div class="list">${completed.map(lessonRow).join('')||'<div class="empty">No completed lessons.</div>'}</div></div><div><div class="section-title"><h3>Payments</h3></div><div class="list">${(state.data.payments||[]).map(p=>`<div class="list-row"><div class="grow"><h4>${esc(p.payer)}</h4><p>${esc(dateLabel(p.payment_date))} · ${esc(p.note||'')}</p></div><b>${money(p.amount_cents)}</b></div>`).join('')||'<div class="empty">No payments.</div>'}</div><div class="section-title"><h3>Invoices</h3></div><div class="list">${(state.data.invoices||[]).map(i=>`<div class="list-row"><div class="grow"><h4>${esc(i.invoice_number)}</h4><p>${esc(i.period_start)} → ${esc(i.period_end)}</p></div><b>${money(i.total_cents)}</b></div>`).join('')||'<div class="empty">No invoices.</div>'}</div></div></div>`;
  document.getElementById('new-payment').onclick=openPayment;
  document.getElementById('new-invoice').onclick=openInvoice;
}

function recommendedTopics(){
  return (state.data.topicProgress||[]).filter(t=>/Next|Current|Revisit|Resource Ready/.test(t.status)).slice(0,6);
}
function renderStudentHome(){
  const next=(state.data.lessons||[]).filter(l=>l.status!=='Completed'&&l.status!=='Cancelled').sort((a,b)=>(a.lesson_date+(a.start_time||'')).localeCompare(b.lesson_date+(b.start_time||'')))[0];
  const topics=state.data.topicProgress||[],covered=topics.filter(t=>t.status==='Covered'||t.status==='Completed').length;
  const plan=state.data.plan;
  document.getElementById('view').innerHTML=pageHead('Hi, '+state.data.user.displayName,'Your learning dashboard.')+
  `<div class="grid cols-3"><div class="card hero student-hero"><div class="student-hero-copy"><div class="eyebrow">${esc(state.data.user.grade||'')} · ${esc(state.data.user.curriculum||'')}</div><h3>${esc(state.data.user.subjects||'Your subjects')}</h3><p>${esc(state.data.user.profileNote||'Keep building confidence one topic at a time.')}</p></div><img src="${VTA_DASHBOARD_VISUAL}" alt="" aria-hidden="true"></div>${metric('Syllabus progress',covered+'/'+topics.length,'topics marked covered')}${metric('Monthly plan',(plan?.lesson_target||0)+' lessons',plan?.goal||'No goal added yet')}</div>
  <div class="grid cols-2" style="margin-top:18px"><div><div class="section-title"><h3>Next lesson</h3></div>${next?lessonRow(next):'<div class="empty">No upcoming lesson yet. Use Book to request one.</div>'}</div><div><div class="section-title"><h3>Recommended next</h3></div><div class="list">${recommendedTopics().map(t=>`<div class="list-row"><div class="grow"><h4>${esc(t.topic)}</h4><p>${esc(t.strand||'')} · ${esc(t.term_label||'')}</p></div>${badge(t.status)}</div>`).join('')||'<div class="empty">No topic recommendations yet.</div>'}</div></div></div>
  <div class="section-title"><h3>Announcements</h3></div><div class="grid cols-3">${(state.data.announcements||[]).slice(0,3).map(a=>`<div class="card"><div class="strong">${esc(a.title)}</div><p class="small muted">${esc(a.body)}</p></div>`).join('')||'<div class="empty">No announcements.</div>'}</div>`;
}
function renderLearn(){
  const groups={};(state.data.topicProgress||[]).forEach(t=>{const k=t.strand||'Other';(groups[k]??=[]).push(t)});
  document.getElementById('view').innerHTML=pageHead('My learning','See what is covered, what needs revisiting and what comes next.')+
  Object.entries(groups).map(([k,items])=>`<div class="section-title"><h3>${esc(k)}</h3></div><div class="card">${items.map(t=>`<div class="progress-row"><div><div class="strong">${esc(t.topic)}</div><div class="small muted">${esc(t.notes||'')}</div></div><div class="small">${esc(t.term_label||'')}</div><div>${badge(t.status)}</div><span></span></div>`).join('')}</div>`).join('');
}
function renderBooking(){
  const rec=recommendedTopics();
  document.getElementById('view').innerHTML=pageHead('Book a lesson','Choose a suggested topic or enter exactly what school is covering.')+
  `<div class="grid cols-2"><div><div class="card"><div class="strong">What do you want help with?</div><p class="small muted">Suggestions come from your syllabus progress.</p><div class="actions">${rec.map(t=>`<button class="btn soft topic-pick" data-topic="${esc(t.topic)}">${esc(t.topic)}</button>`).join('')}</div></div>
  <form id="booking-form" class="card" style="margin-top:12px"><div class="form-grid">
    <div class="field full"><label>TOPIC</label><input id="book-topic" class="input" placeholder="Topic or school-specific section" required></div>
    <div class="field"><label>SUBJECT</label><input id="book-subject" class="input" value="${esc((state.data.user.subjects||'').split(/[·,\/]/)[0].trim())}" required></div>
    <div class="field"><label>DURATION</label><select id="book-duration" class="select"><option value="60">1 hour</option><option value="120">2 hours</option></select></div>
    <div class="field"><label>DATE</label><input id="book-date" type="date" class="input" required></div>
    <div class="field full"><label>WHAT EXACTLY ARE YOU STRUGGLING WITH?</label><textarea id="book-help" class="textarea"></textarea></div>
  </div><div class="dialog-actions"><button class="btn primary" type="button" id="load-slots">Show available times</button></div></form></div>
  <div><div class="card"><div class="strong">Available times</div><p class="small muted">Other students are always anonymous. Busy times only show as unavailable.</p><div id="slot-grid" class="slot-grid"><div class="empty" style="grid-column:1/-1">Choose a date, then load availability.</div></div></div></div></div>`;
  document.querySelectorAll('.topic-pick').forEach(b=>b.onclick=()=>document.getElementById('book-topic').value=b.dataset.topic);
  document.getElementById('load-slots').onclick=loadSlots;
}
async function loadSlots(){
  const date=document.getElementById('book-date').value,duration=Number(document.getElementById('book-duration').value),grid=document.getElementById('slot-grid');if(!date){toast('Choose a date first.',true);return}
  grid.innerHTML='<div class="empty" style="grid-column:1/-1">Checking live availability…</div>';
  try{const r=await api({action:'availability',date,duration});grid.innerHTML=r.slots.map(s=>`<button class="slot ${s.available?'available':'unavailable'}" ${s.available?'':'disabled'} data-time="${s.time}">${esc(s.time)}<br><span class="tiny">${s.available?'Available':'Unavailable'}</span></button>`).join('');document.querySelectorAll('.slot.available').forEach(b=>b.onclick=()=>{state.bookingSlot=b.dataset.time;document.querySelectorAll('.slot').forEach(x=>x.classList.remove('selected'));b.classList.add('selected');submitBookingReady()})}catch(e){grid.innerHTML='<div class="empty" style="grid-column:1/-1">'+esc(e.message)+'</div>'}
}
function submitBookingReady(){
  if(document.getElementById('book-submit'))return;
  const form=document.getElementById('booking-form'),acts=document.createElement('div');acts.className='dialog-actions';acts.innerHTML='<button type="button" id="book-submit" class="btn primary">Request '+esc(state.bookingSlot)+'</button>';form.appendChild(acts);
  document.getElementById('book-submit').onclick=async()=>{const topic=document.getElementById('book-topic').value.trim(),date=document.getElementById('book-date').value;if(!topic||!date||!state.bookingSlot){toast('Complete the booking details.',true);return}try{await api({action:'bookingCreate',date,time:state.bookingSlot,duration:Number(document.getElementById('book-duration').value),subject:document.getElementById('book-subject').value.trim(),topic,help:document.getElementById('book-help').value.trim()});toast('Booking request sent');state.bookingSlot=null;await sync()}catch(e){toast(e.message,true)}};
}

function openDialog(title,body,close='Cancel'){
  const d=document.createElement('dialog');d.innerHTML=`<div class="dialog-body"><div class="dialog-head"><h3>${esc(title)}</h3><button class="icon-btn dlg-close">×</button></div>${body}<div class="dialog-actions"><button class="btn secondary dlg-close">${esc(close)}</button></div></div>`;document.body.appendChild(d);d.querySelectorAll('.dlg-close').forEach(b=>b.onclick=()=>{d.close();d.remove()});d.showModal();return d;
}
function formDialog(title,body,onSave,saveLabel='Save'){
  const d=document.createElement('dialog');d.innerHTML=`<form class="dialog-body"><div class="dialog-head"><h3>${esc(title)}</h3><button type="button" class="icon-btn dlg-close">×</button></div>${body}<div class="dialog-actions"><button type="button" class="btn secondary dlg-close">Cancel</button><button class="btn primary">${esc(saveLabel)}</button></div></form>`;document.body.appendChild(d);d.querySelectorAll('.dlg-close').forEach(b=>b.onclick=()=>{d.close();d.remove()});d.querySelector('form').onsubmit=async e=>{e.preventDefault();try{await onSave(d);d.close();d.remove()}catch(x){toast(x.message,true)}};d.showModal();return d;
}
function openStudentEditor(s){
  if(!s){
    return formDialog('Create student',`<div class="form-grid"><div class="field"><label>NAME</label><input name="name" class="input" required></div><div class="field"><label>USERNAME</label><input name="username" class="input" required></div><div class="field"><label>GRADE</label><input name="grade" class="input"></div><div class="field"><label>CURRICULUM</label><input name="curriculum" class="input"></div><div class="field full"><label>SUBJECTS</label><input name="subjects" class="input"></div><div class="field full"><label>TEMPORARY PASSWORD</label><input name="password" type="password" class="input" required minlength="8"></div></div>`,async d=>{const f=new FormData(d.querySelector('form'));const o=Object.fromEntries(f);await api({action:'studentCreate',...o});toast('Student created');await sync()},'Create student');
  }
  formDialog('Edit '+s.display_name,`<div class="form-grid"><div class="field"><label>NAME</label><input name="name" value="${esc(s.display_name)}" class="input" required></div><div class="field"><label>LOGIN USERNAME</label><input name="username" value="${esc(s.username)}" class="input" required></div><div class="field"><label>GRADE</label><input name="grade" value="${esc(s.grade||'')}" class="input"></div><div class="field"><label>CURRICULUM</label><input name="curriculum" value="${esc(s.curriculum||'')}" class="input"></div><div class="field full"><label>SUBJECTS</label><input name="subjects" value="${esc(s.subjects||'')}" class="input"></div><div class="field full"><label>PROFILE NOTE</label><textarea name="profileNote" class="textarea">${esc(s.profile_note||'')}</textarea></div></div>`,async d=>{const o=Object.fromEntries(new FormData(d.querySelector('form')));await api({action:'studentUpdate',studentId:s.id,...o,active:true});toast('Student updated');await sync(false);renderStudents()});
}
function resetPassword(s){
  formDialog('Reset '+s.display_name+' password',`<div class="field"><label>NEW TEMPORARY PASSWORD</label><input name="password" type="password" class="input" minlength="8" required></div>`,async d=>{const p=new FormData(d.querySelector('form')).get('password');await api({action:'studentPasswordReset',studentId:s.id,password:p});toast('Password updated')},'Reset password');
}
function openTopicEditor(s,t){
  const statuses=['Covered','Current','Next','Revisit','Future','Resource Ready'];
  formDialog((t?'Edit':'Add')+' syllabus topic',`<div class="form-grid"><div class="field"><label>SUBJECT</label><input name="subject" class="input" value="${esc(t?.subject||s.subjects||'')}"></div><div class="field"><label>STRAND / SECTION</label><input name="strand" class="input" value="${esc(t?.strand||'')}"></div><div class="field"><label>TERM</label><input name="termLabel" class="input" value="${esc(t?.term_label||'')}"></div><div class="field"><label>STATUS</label><select name="status" class="select">${statuses.map(x=>`<option ${t?.status===x?'selected':''}>${x}</option>`).join('')}</select></div><div class="field full"><label>TOPIC</label><input name="topic" class="input" value="${esc(t?.topic||'')}" required></div><div class="field full"><label>TUTOR NOTES / WHAT IS NEXT</label><textarea name="notes" class="textarea">${esc(t?.notes||'')}</textarea></div><div class="field"><label>SORT ORDER</label><input name="sortOrder" type="number" class="input" value="${esc(t?.sort_order||0)}"></div></div>`,async d=>{const o=Object.fromEntries(new FormData(d.querySelector('form')));await api({action:'topicUpsert',id:t?.id,studentId:s.id,...o,sortOrder:Number(o.sortOrder)});toast('Topic saved');await sync(false);renderStudents()});
}
function openPlanEditor(s,p){
  formDialog('Monthly learning plan',`<div class="form-grid"><div class="field"><label>MONTH</label><input name="monthKey" type="month" class="input" value="${esc(p?.month_key||new Date().toISOString().slice(0,7))}"></div><div class="field"><label>LESSON TARGET</label><input name="lessonTarget" type="number" class="input" value="${esc(p?.lesson_target||4)}"></div><div class="field full"><label>GOAL</label><textarea name="goal" class="textarea">${esc(p?.goal||'')}</textarea></div><div class="field full"><label>FOCUS TOPICS (comma separated)</label><input name="topics" class="input" value="${esc((p?.focus_topics||[]).join(', '))}"></div></div>`,async d=>{const o=Object.fromEntries(new FormData(d.querySelector('form')));await api({action:'planUpdate',studentId:s.id,monthKey:o.monthKey,lessonTarget:Number(o.lessonTarget),goal:o.goal,focusTopics:o.topics.split(',').map(x=>x.trim()).filter(Boolean)});toast('Plan saved');await sync(false);renderStudents()});
}
function openLessonEditor(l){
  const students=state.data.students||[];
  formDialog(l?'Edit lesson':'Add lesson',`<div class="form-grid"><div class="field"><label>STUDENT</label><select name="studentId" class="select">${students.map(s=>`<option value="${s.id}" ${l?.student_id===s.id?'selected':''}>${esc(s.display_name)}</option>`).join('')}</select></div><div class="field"><label>DATE</label><input name="date" type="date" class="input" value="${esc(l?.lesson_date||'')}" required></div><div class="field"><label>TIME</label><input name="time" type="time" class="input" value="${esc(shortTime(l?.start_time||''))}"></div><div class="field"><label>DURATION</label><select name="duration" class="select"><option value="60">1 hour</option><option value="120">2 hours</option></select></div><div class="field"><label>SUBJECT</label><input name="subject" class="input" value="${esc(l?.subject||'')}"></div><div class="field"><label>STATUS</label><select name="status" class="select">${['Planned','Confirmed','Completed','Cancelled'].map(x=>`<option ${l?.status===x?'selected':''}>${x}</option>`).join('')}</select></div><div class="field full"><label>TOPIC</label><input name="topic" class="input" value="${esc(l?.topic||'')}"></div><div class="field full"><label>NOTES</label><textarea name="notes" class="textarea">${esc(l?.notes||'')}</textarea></div></div>`,async d=>{const o=Object.fromEntries(new FormData(d.querySelector('form')));await api({action:'lessonUpsert',id:l?.id,...o,duration:Number(o.duration)});toast('Lesson saved');await sync()});
}
function openResourceEditor(r){
  const students=state.data.students||[];
  formDialog(r?'Edit resource':'Add resource',`<div class="form-grid"><div class="field"><label>STUDENT</label><select name="studentId" class="select" ${r?'disabled':''}>${students.map(s=>`<option value="${s.id}" ${r?.student_id===s.id?'selected':''}>${esc(s.display_name)}</option>`).join('')}</select></div><div class="field"><label>TYPE</label><select name="resourceType" class="select">${['Interactive Lesson','Slides','PDF / Notes','Worksheet','Video','Image','Other'].map(x=>`<option ${r?.resource_type===x?'selected':''}>${x}</option>`).join('')}</select></div><div class="field full"><label>TITLE</label><input name="title" class="input" value="${esc(r?.title||'')}" required></div><div class="field full"><label>DESCRIPTION</label><textarea name="description" class="textarea">${esc(r?.description||'')}</textarea></div><div class="field full"><label>PASSWORD / ACCESS NOTE</label><input name="accessNote" class="input" value="${esc(r?.access_note||'')}" placeholder="e.g. Password: VTA9"></div><div class="field"><label>STATUS</label><select name="completionStatus" class="select">${['Available','Completed','Current','Future'].map(x=>`<option ${r?.completion_status===x?'selected':''}>${x}</option>`).join('')}</select></div>${r?'':`<div class="field full"><label>EXTERNAL LINK (optional)</label><input name="externalUrl" class="input" placeholder="https://..."></div><div class="field full"><label>OR UPLOAD A FILE (max 8 MB)</label><input name="file" type="file" class="input"></div>`}</div>`,async d=>{
    const form=d.querySelector('form'),o=Object.fromEntries(new FormData(form));
    if(r){await api({action:'resourceUpdate',id:r.id,title:o.title,resourceType:o.resourceType,description:o.description,accessNote:o.accessNote,completionStatus:o.completionStatus,featured:true});}
    else{
      const file=form.querySelector('[name=file]').files[0];
      if(file){if(file.size>8*1024*1024)throw new Error('File is over 8 MB');const base64=await fileToBase64(file);await api({action:'resourceUpload',studentId:o.studentId,title:o.title,resourceType:o.resourceType,description:o.description,accessNote:o.accessNote,completionStatus:o.completionStatus,featured:true,fileName:file.name,mimeType:file.type||'application/octet-stream',base64});}
      else if(o.externalUrl){await api({action:'resourceCreateLink',studentId:o.studentId,title:o.title,resourceType:o.resourceType,description:o.description,accessNote:o.accessNote,completionStatus:o.completionStatus,featured:true,externalUrl:o.externalUrl,fileName:o.title,mimeType:'text/html'});}
      else throw new Error('Choose a file or enter a link');
    }
    toast('Resource saved');await sync(false);renderResources(true);
  });
}
function fileToBase64(file){return new Promise((resolve,reject)=>{const fr=new FileReader();fr.onload=()=>resolve(String(fr.result).split(',')[1]);fr.onerror=reject;fr.readAsDataURL(file)})}
async function openResource(id){
  try{const r=await api({action:'resourceDownload',id});if(r.signedUrl)window.desktop.openExternal(r.signedUrl)}catch(e){toast(e.message,true)}
}
function openQuizCreator(){
  const students=state.data.students||[];
  const d=formDialog('Create quiz',`<div class="form-grid"><div class="field"><label>STUDENT</label><select name="studentId" class="select">${students.map(s=>`<option value="${s.id}">${esc(s.display_name)}</option>`).join('')}</select></div><div class="field"><label>SUBJECT</label><input name="subject" class="input"></div><div class="field full"><label>TITLE</label><input name="title" class="input" required></div><div class="field full"><label>TOPIC</label><input name="topic" class="input"></div><div class="field full"><label>INSTRUCTIONS</label><textarea name="instructions" class="textarea"></textarea></div></div><div class="section-title"><h3>Questions</h3><button type="button" id="add-q" class="btn soft">＋ Question</button></div><div id="questions"></div>`,async dlg=>{
    const f=dlg.querySelector('form'),o=Object.fromEntries(new FormData(f)),questions=[...dlg.querySelectorAll('.q-block')].map(q=>({type:q.querySelector('[name=qtype]').value,prompt:q.querySelector('[name=qprompt]').value,options:q.querySelector('[name=qoptions]').value.split('|').map(x=>x.trim()).filter(Boolean),answer:q.querySelector('[name=qanswer]').value,explanation:q.querySelector('[name=qexplain]').value,marks:Number(q.querySelector('[name=qmarks]').value||1)})).filter(q=>q.prompt);
    if(!questions.length)throw new Error('Add at least one question');
    await api({action:'quizCreate',studentId:o.studentId,title:o.title,subject:o.subject,topic:o.topic,instructions:o.instructions,status:'Published',questions});toast('Quiz published');await sync(false);renderQuizzes(true);
  },'Publish quiz');
  const add=()=>{const q=document.createElement('div');q.className='card q-block';q.style.marginBottom='9px';q.innerHTML=`<div class="form-grid"><div class="field"><label>TYPE</label><select name="qtype" class="select"><option>MCQ</option><option>Short</option></select></div><div class="field"><label>MARKS</label><input name="qmarks" type="number" value="1" class="input"></div><div class="field full"><label>QUESTION</label><input name="qprompt" class="input"></div><div class="field full"><label>OPTIONS separated by |</label><input name="qoptions" class="input" placeholder="A | B | C | D"></div><div class="field"><label>CORRECT ANSWER</label><input name="qanswer" class="input"></div><div class="field"><label>EXPLANATION</label><input name="qexplain" class="input"></div></div>`;d.querySelector('#questions').appendChild(q)};
  d.querySelector('#add-q').onclick=add;add();add();
}
async function takeQuiz(id){
  try{
    const r=await api({action:'quizGet',id}),q=r.quiz,questions=r.questions||[];
    const d=formDialog(q.title,`<p class="small muted">${esc(q.instructions||'Answer all questions.')}</p>${questions.map((x,i)=>`<div class="card" style="margin-bottom:9px"><div class="strong">${i+1}. ${esc(x.prompt)}</div><div class="small muted">${x.marks} mark(s)</div>${x.question_type==='MCQ'?(x.options||[]).map(o=>`<label style="display:block;margin-top:7px"><input type="radio" name="ans_${x.id}" value="${esc(o)}"> ${esc(o)}</label>`).join(''):`<input name="ans_${x.id}" class="input" style="margin-top:9px">`}</div>`).join('')}`,async dlg=>{
      const answers={};questions.forEach(x=>{const el=dlg.querySelector('[name="ans_'+x.id+'"]:checked')||dlg.querySelector('[name="ans_'+x.id+'"]');answers[x.id]=el?.value||''});
      const res=await api({action:'quizSubmit',quizId:id,answers});toast('Score: '+res.score+'/'+res.total);await sync(false);renderQuizzes(false);
    },'Submit quiz');d.querySelector('.dlg-close:last-child')?.remove();
  }catch(e){toast(e.message,true)}
}
function openPayment(){
  formDialog('Record payment',`<div class="form-grid"><div class="field"><label>PAYER</label><input name="payer" value="Tracey" class="input"></div><div class="field"><label>DATE</label><input name="date" type="date" class="input"></div><div class="field"><label>AMOUNT (RAND)</label><input name="amount" type="number" step="0.01" class="input"></div><div class="field full"><label>NOTE</label><textarea name="note" class="textarea"></textarea></div></div>`,async d=>{const o=Object.fromEntries(new FormData(d.querySelector('form')));await api({action:'paymentCreate',payer:o.payer,date:o.date,amountCents:o.amount?Math.round(Number(o.amount)*100):null,note:o.note});toast('Payment recorded');await sync()});
}
function openInvoice(){
  const today=new Date(),first=new Date(today.getFullYear(),today.getMonth(),1).toISOString().slice(0,10);
  formDialog('Create invoice draft',`<div class="form-grid"><div class="field"><label>INVOICE NUMBER</label><input name="invoiceNumber" class="input" placeholder="VTA-1005"></div><div class="field"><label>PAYER</label><input name="payer" value="Tracey" class="input"></div><div class="field"><label>PERIOD START</label><input name="periodStart" type="date" value="${first}" class="input"></div><div class="field"><label>PERIOD END</label><input name="periodEnd" type="date" value="${today.toISOString().slice(0,10)}" class="input"></div></div>`,async d=>{const o=Object.fromEntries(new FormData(d.querySelector('form')));const r=await api({action:'invoiceCreate',...o,hourlyRateCents:15000});toast('Invoice '+r.invoiceNumber+' created');await sync()});
}

if(state.token){sync(false).then(()=>{startLive();renderApp()}).catch(()=>renderLogin())}else renderLogin();
