import type { Context, Config } from "@netlify/functions";
import { getStore } from "@netlify/blobs";
import crypto from "node:crypto";

const stateStore = () => getStore("vta-state", { consistency: "strong" });
const fileStore = () => getStore("vta-resources", { consistency: "strong" });
const reply = (x:any,s=200) => new Response(JSON.stringify(x), { status:s, headers:{ "content-type":"application/json" } });
const sha = (x:string) => crypto.createHash("sha256").update(x).digest("hex");
const passHash = (p:string,salt:string) => crypto.scryptSync(p,Buffer.from(salt,"hex"),64,{N:16384,r:8,p:1}).toString("hex");
const id = () => crypto.randomBytes(12).toString("hex");
const now = () => new Date().toISOString();
const safeUser=(u:any)=>({id:u.id,username:u.username,role:u.role,name:u.name,grade:u.grade,curriculum:u.curriculum,subjects:u.subjects,planMonth:u.planMonth,lessonTarget:u.lessonTarget,planGoal:u.planGoal,topics:u.topics});

function seedState(){
  return {
    version:1,
    users:[
      {id:"admin",username:"victoria.admin",passwordHash:"b7c9d8bab3fadc7669607e201c7795650300ec88adea88edb2286946716c68b5cc93413726333d3c349f185aaa696e0b29bf5a89376d4aed5056cdd1b2e705ff",salt:"3e0bb4726fa8015de7aa1c47540537b1",role:"ADMIN",name:"Victoria",grade:"",curriculum:"",subjects:"",active:true},
      {id:"mitchell",username:"mitchell.vta",passwordHash:"cc77328c84dbc67c8e1c2091284c0c9eac2eb2f25b45933cecc520d248419809c82c67ca4e8a8d49b7ceb26f14292f27812068fd06f2af127acf715599751df8",salt:"7ccfb4bc022e94f4d12ef03ceaa83b8c",role:"STUDENT",name:"Mitchell",grade:"Grade 9",curriculum:"IEB",subjects:"Natural Sciences",active:true,planMonth:"October 2026",lessonTarget:4,planGoal:"Prepare confidently for Grade 9 IEB exams by understanding the science and applying it to exam questions.",topics:["Scientific Method & Variables","Forces & Motion","Electric Cells & Circuits","Magnetism & Electrostatics","Motion graphs"]},
      {id:"lateya",username:"lateya.vta",passwordHash:"d0eeca5a7dd6049f58d6001c38fbbbb9f91c527798aba88d3ec972f54e47634ae3cbdcab57e7f178ffbd2ce24b95309744e0041eb19ec7e42cb78600e87763a1",salt:"65ac7d5ade51a3f5a59272075a2e5210",role:"STUDENT",name:"Lateya",grade:"Grade 10",curriculum:"CAPS",subjects:"Mathematics",active:true,planMonth:"October 2026",lessonTarget:4,planGoal:"Move from relying on notes to independently solving Grade 10 exam questions.",topics:["Functions & Graphs","Algebra","Trigonometry","Exam technique"]},
      {id:"phelandi",username:"phelandi.vta",passwordHash:"c7b15f3a71fe2f465c9a11ba8281630bf61540fffb326da5f888200c759d29cdccc927791029c29393133c0ddfbb7a3e3dfa6474a28ea148ffb2e59bd0ee3e90",salt:"b32643d22397447ed7af31b40d79ae8c",role:"STUDENT",name:"Phelandi",grade:"Grade 9",curriculum:"Not recorded",subjects:"Natural Sciences / Physics · Mathematics",active:true,planMonth:"October 2026",lessonTarget:6,planGoal:"Strengthen Physics/Natural Sciences and Mathematics through worked examples and exam-style practice.",topics:["Factors affecting resistance","Equations of motion","Mathematics Term 1–3 revision","Circuit problem solving"]},
      {id:"asimtusi",username:"asimtusi.vta",passwordHash:"90e912b331a0f2ab6584946bef152cda8febaf25b0dd9fc0693bd868e1cb2b6c38ba6ad0e763333b16096a6cd639bc398cdf90da9c9a51765f1bbba786645b7e",salt:"6fe4c2cd14982460e8b15010ee5294fe",role:"STUDENT",name:"Asimtusi",grade:"Grade 10",curriculum:"Not recorded",subjects:"Physics",active:true,planMonth:"October 2026",lessonTarget:4,planGoal:"Build confidence in Grade 10 Physics through concept mastery and exam-style application.",topics:["Motion","Equations of motion","Graphs","Physics exam practice"]}
    ],
    sessions:[],
    lessons:[
      {id:"m1",studentId:"mitchell",date:"2026-09-12",time:"",duration:60,subject:"Natural Sciences",topic:"Forces & Motion Masterclass",status:"Completed",notes:"Verified lesson record."},
      {id:"m2",studentId:"mitchell",date:"2026-09-20",time:"",duration:60,subject:"Natural Sciences",topic:"Electric Circuits",status:"Completed",notes:"Exact start time not retained."},
      {id:"m3",studentId:"mitchell",date:"2026-09-22",time:"",duration:60,subject:"Natural Sciences",topic:"Electric Cells as Energy Systems",status:"Completed",notes:"Cells, batteries, polarity and investigation skills."},
      {id:"l1",studentId:"lateya",date:"2026-09-19",time:"",duration:60,subject:"Mathematics",topic:"Trigonometry",status:"Completed",notes:"Exact start time not retained."},
      {id:"l2",studentId:"lateya",date:"2026-09-28",time:"17:00",duration:60,subject:"Mathematics",topic:"Functions",status:"Confirmed",notes:"Confirmed Grade 10 Functions lesson."},
      {id:"p1",studentId:"phelandi",date:"2026-09-19",time:"",duration:120,subject:"Natural Sciences / Physics",topic:"Physics / Natural Sciences lesson",status:"Completed",notes:"Exact topic and start time not retained."},
      {id:"p2",studentId:"phelandi",date:"2026-09-22",time:"14:00",duration:120,subject:"Physics / Natural Sciences",topic:"Factors Affecting Resistance",status:"Completed",notes:"Verified 14:00–16:00 lesson."},
      {id:"p3",studentId:"phelandi",date:"2026-09-23",time:"",duration:60,subject:"Physics / Natural Sciences",topic:"Physics follow-up",status:"Completed",notes:"Exact start time/topic not retained."},
      {id:"p4",studentId:"phelandi",date:"2026-09-29",time:"12:00",duration:120,subject:"Mathematics + Physics",topic:"Maths + Physics revision",status:"Planned",notes:"Confirmed planned lesson."},
      {id:"a1",studentId:"asimtusi",date:"2026-09-23",time:"",duration:60,subject:"Physics",topic:"Physics lesson",status:"Completed",notes:"Exact topic/start time not retained."},
      {id:"a2",studentId:"asimtusi",date:"2026-09-30",time:"09:00",duration:120,subject:"Physics",topic:"Grade 10 Physics",status:"Planned",notes:"Confirmed planned lesson."}
    ],
    bookings:[],messages:[],
    announcements:[{id:"ann1",title:"Welcome to your VTA portal",message:"Your lessons, plan, resources, bookings and messages live in one place.",audience:"ALL",studentId:null,createdAt:now()}],
    payments:[{id:"pay1",payer:"Tracey",date:"2026-09-15",amountCents:null,note:"Payment received on the 15th; exact amount not verified."}],
    schedule:[],
    resources:[],
    settings:{slotInterval:30,lessonFormat:"Detailed cards",resourceFormat:"Preview cards"},
    events:[]
  };
}
async function load(){
  const s=await stateStore().get("state",{type:"json"});
  if(s)return s;
  const fresh=seedState(); await stateStore().setJSON("state",fresh); return fresh;
}
async function save(s:any){s.updatedAt=now();await stateStore().setJSON("state",s);}
function userByToken(s:any,req:Request){
  const h=req.headers.get("authorization")||""; if(!h.startsWith("Bearer "))return null;
  const tokenHash=sha(h.slice(7)); const sess=s.sessions.find((x:any)=>x.tokenHash===tokenHash&&new Date(x.expiresAt)>new Date());
  if(!sess)return null; return s.users.find((u:any)=>u.id===sess.userId&&u.active);
}
function login(s:any,b:any){
  const u=s.users.find((x:any)=>x.active&&x.username.toLowerCase()===String(b.username||"").toLowerCase());
  if(!u)return null; const h=passHash(String(b.password||""),u.salt);
  if(h!==u.passwordHash)return null;
  const token=crypto.randomBytes(32).toString("hex"); s.sessions=s.sessions.filter((x:any)=>new Date(x.expiresAt)>new Date());
  s.sessions.push({tokenHash:sha(token),userId:u.id,expiresAt:new Date(Date.now()+30*86400000).toISOString()});
  return {token,user:safeUser(u)};
}
function mins(t:string){const p=t.split(":");return Number(p[0])*60+Number(p[1]);}
function fmt(m:number){return String(Math.floor(m/60)).padStart(2,"0")+":"+String(m%60).padStart(2,"0");}
function availableSlots(s:any,date:string,duration:number){
  const busy:any[]=[];
  s.schedule.filter((e:any)=>e.date===date).forEach((e:any)=>busy.push([mins(e.start),mins(e.end)]));
  s.lessons.filter((l:any)=>l.date===date&&l.time&&["Planned","Confirmed"].includes(l.status)).forEach((l:any)=>busy.push([mins(l.time),mins(l.time)+Number(l.duration)]));
  const out:any[]=[]; const step=Number(s.settings.slotInterval||30);
  for(let x=540;x+duration<=1140;x+=step){const end=x+duration;out.push({time:fmt(x),available:!busy.some(b=>x<b[1]&&end>b[0])});}
  return out;
}
function event(s:any,userId:string,type:string,text:string){s.events.push({id:id(),userId,type,text,createdAt:now(),read:false});}
function syncFor(s:any,u:any){
  if(u.role==="ADMIN"){
    return {user:safeUser(u),students:s.users.filter((x:any)=>x.role==="STUDENT").map(safeUser),lessons:s.lessons,bookings:s.bookings,messages:s.messages,announcements:s.announcements,payments:s.payments,resources:s.resources,schedule:s.schedule,settings:s.settings,events:s.events.filter((e:any)=>e.userId==="admin")};
  }
  return {user:safeUser(u),lessons:s.lessons.filter((x:any)=>x.studentId===u.id),bookings:s.bookings.filter((x:any)=>x.studentId===u.id),messages:s.messages.filter((x:any)=>x.studentId===u.id),announcements:s.announcements.filter((x:any)=>x.audience==="ALL"||x.studentId===u.id),resources:s.resources.filter((x:any)=>x.studentId===u.id),settings:s.settings,events:s.events.filter((e:any)=>e.userId===u.id)};
}
export default async (req:Request,context:Context)=>{
  if(req.method!=="POST")return reply({error:"POST required"},405);
  let b:any={}; try{b=await req.json()}catch{return reply({error:"Invalid JSON"},400)}
  const s=await load();
  if(b.action==="login"){const result=login(s,b);if(!result)return reply({error:"Invalid login"},401);await save(s);return reply(result);}
  const u=userByToken(s,req); if(!u)return reply({error:"Unauthorized"},401);
  if(b.action==="sync")return reply(syncFor(s,u));
  if(b.action==="availability"){
    const d=String(b.date||"");const dur=Number(b.duration||60);if(!/^\d{4}-\d{2}-\d{2}$/.test(d)||![60,120].includes(dur))return reply({error:"Invalid request"},400);
    return reply({date:d,duration:dur,slots:availableSlots(s,d,dur)});
  }
  if(b.action==="bookingCreate"&&u.role!=="ADMIN"){
    const dur=Number(b.duration||60);const slots=availableSlots(s,String(b.date),dur);if(!slots.some((x:any)=>x.time===b.time&&x.available))return reply({error:"That time is no longer available"},409);
    const r={id:id(),studentId:u.id,date:String(b.date),time:String(b.time),duration:dur,subject:String(b.subject),topic:String(b.topic||""),help:String(b.help||""),status:"Pending",createdAt:now()};
    s.bookings.push(r);event(s,"admin","booking",u.name+" requested "+r.date+" at "+r.time);await save(s);return reply({ok:true,id:r.id});
  }
  if(b.action==="messageSend"){
    const studentId=u.role==="ADMIN"?String(b.studentId):u.id;const r={id:id(),studentId,senderId:u.id,senderName:u.name,body:String(b.message||""),createdAt:now()};
    s.messages.push(r);event(s,u.role==="ADMIN"?studentId:"admin","message",u.name+": "+r.body);await save(s);return reply({ok:true});
  }
  if(b.action==="resourceDownload"){
    const r=s.resources.find((x:any)=>x.id===String(b.id));if(!r)return reply({error:"Not found"},404);if(u.role!=="ADMIN"&&r.studentId!==u.id)return reply({error:"Forbidden"},403);
    const ab=await fileStore().get(r.fileKey,{type:"arrayBuffer"});if(!ab)return reply({error:"File missing"},404);
    return reply({fileName:r.fileName,mimeType:r.mimeType,base64:Buffer.from(ab).toString("base64")});
  }
  if(u.role!=="ADMIN")return reply({error:"Forbidden"},403);
  if(b.action==="bookingStatus"){
    const r=s.bookings.find((x:any)=>x.id===String(b.id));if(!r)return reply({error:"Not found"},404);r.status=String(b.status);
    if(r.status==="Confirmed"&&!s.lessons.some((x:any)=>x.bookingId===r.id)){s.lessons.push({id:id(),bookingId:r.id,studentId:r.studentId,date:r.date,time:r.time,duration:r.duration,subject:r.subject,topic:r.topic,status:"Confirmed",notes:"Confirmed from booking request."});event(s,r.studentId,"booking","Your lesson request for "+r.date+" at "+r.time+" was confirmed.");}
    await save(s);return reply({ok:true});
  }
  if(b.action==="lessonUpsert"){
    let r=b.id?s.lessons.find((x:any)=>x.id===String(b.id)):null;
    if(!r){r={id:id()};s.lessons.push(r);}
    Object.assign(r,{studentId:String(b.studentId),date:String(b.date),time:String(b.time||""),duration:Number(b.duration||60),subject:String(b.subject||""),topic:String(b.topic||""),status:String(b.status||"Planned"),notes:String(b.notes||"")});event(s,r.studentId,"lesson","Your lesson schedule was updated.");await save(s);return reply({ok:true,id:r.id});
  }
  if(b.action==="announcementCreate"){s.announcements.push({id:id(),title:String(b.title||""),message:String(b.message||""),audience:String(b.audience||"ALL"),studentId:b.studentId?String(b.studentId):null,createdAt:now()});await save(s);return reply({ok:true});}
  if(b.action==="paymentCreate"){s.payments.push({id:id(),payer:String(b.payer||""),date:String(b.date||""),amountCents:b.amountCents==null?null:Number(b.amountCents),note:String(b.note||"")});await save(s);return reply({ok:true});}
  if(b.action==="settingUpdate"){s.settings[String(b.key)]=b.value;await save(s);return reply({ok:true});}
  if(b.action==="studentCreate"){
    if(s.users.some((x:any)=>x.username.toLowerCase()===String(b.username).toLowerCase()))return reply({error:"Username already exists"},409);
    const salt=crypto.randomBytes(16).toString("hex");const uid=id();s.users.push({id:uid,username:String(b.username),passwordHash:passHash(String(b.password),salt),salt,role:"STUDENT",name:String(b.name),grade:String(b.grade||""),curriculum:String(b.curriculum||""),subjects:String(b.subjects||""),active:true,planMonth:"October 2026",lessonTarget:4,planGoal:"Build confidence and make measurable progress this month.",topics:[]});await save(s);return reply({ok:true,id:uid});
  }
  if(b.action==="resourceUpload"){
    const raw=Buffer.from(String(b.base64||""),"base64");if(raw.length>8*1024*1024)return reply({error:"File too large"},413);
    const rid=id();const key=String(b.studentId)+"/"+rid+"-"+String(b.fileName||"file").replace(/[^a-zA-Z0-9._-]/g,"_");
    await fileStore().set(key,raw.buffer.slice(raw.byteOffset,raw.byteOffset+raw.byteLength));
    s.resources.push({id:rid,studentId:String(b.studentId),title:String(b.title||b.fileName||"Resource"),type:String(b.resourceType||"Other"),fileKey:key,fileName:String(b.fileName||"file"),mimeType:String(b.mimeType||"application/octet-stream"),description:String(b.description||""),createdAt:now()});
    event(s,String(b.studentId),"resource","Victoria added a new resource: "+String(b.title||b.fileName||"Resource"));await save(s);return reply({ok:true,id:rid});
  }
  return reply({error:"Unknown action"},400);
};
export const config: Config = { path: "/api" };
