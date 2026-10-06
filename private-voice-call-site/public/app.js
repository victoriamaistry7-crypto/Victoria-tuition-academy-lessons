const $ = (id) => document.getElementById(id);
const VOICE_KEY = "v_private_custom_voice_id";

const state = {
  recorder:null,chunks:[],consentBlob:null,pc:null,dc:null,stream:null,
  remoteAudio:null,muted:false,callStartedAt:0,timerHandle:null
};

function show(view){
  ["loadingView","setupView","callView"].forEach(id => $(id).classList.add("hidden"));
  $(view).classList.remove("hidden");
}
function msg(text, good=false){
  $("setupMessage").textContent=text;
  $("setupMessage").className=`message${good?" good":""}`;
}
function blobToDataURL(blob){
  return new Promise((resolve,reject)=>{
    const r=new FileReader();
    r.onload=()=>resolve(r.result);
    r.onerror=reject;
    r.readAsDataURL(blob);
  });
}
function fmt(seconds){
  const m=Math.floor(seconds/60).toString().padStart(2,"0");
  const s=Math.floor(seconds%60).toString().padStart(2,"0");
  return `${m}:${s}`;
}
function startTimer(){
  state.callStartedAt=Date.now();
  state.timerHandle=setInterval(()=>{$("timer").textContent=fmt((Date.now()-state.callStartedAt)/1000)},500);
}
function stopTimer(){clearInterval(state.timerHandle);state.timerHandle=null;$("timer").textContent="00:00"}

async function init(){
  try{
    const status=await fetch("/api/status").then(r=>r.json());
    if(!status.ready) throw new Error("The private voice server is not ready.");
    const voiceId=localStorage.getItem(VOICE_KEY);
    if(voiceId){
      show("callView");
    }else{
      show("setupView");
      await loadPhrase();
    }
  }catch(e){
    show("setupView");
    $("consentPhrase").textContent="The voice service could not start.";
    $("recordBtn").disabled=true;
    msg(e.message);
  }
}
async function loadPhrase(){
  const res=await fetch("/api/consent-phrase");
  const data=await res.json();
  if(!res.ok) throw new Error(data.error||"Could not load the consent phrase.");
  $("consentPhrase").textContent=data.phrase;
}

$("recordBtn").addEventListener("click", async()=>{
  if(state.recorder?.state==="recording"){state.recorder.stop();return}
  try{
    const stream=await navigator.mediaDevices.getUserMedia({audio:true});
    const types=["audio/webm;codecs=opus","audio/ogg;codecs=opus","audio/webm","audio/ogg"];
    const type=types.find(t=>MediaRecorder.isTypeSupported(t));
    state.chunks=[];
    state.recorder=type?new MediaRecorder(stream,{mimeType:type}):new MediaRecorder(stream);
    state.recorder.ondataavailable=e=>{if(e.data.size)state.chunks.push(e.data)};
    state.recorder.onstop=()=>{
      state.consentBlob=new Blob(state.chunks,{type:state.recorder.mimeType||"audio/webm"});
      $("preview").src=URL.createObjectURL(state.consentBlob);
      $("preview").classList.remove("hidden");
      $("recordLabel").textContent="Record again";
      $("recordStatus").textContent="Recorded — listen before continuing";
      $("createVoiceBtn").disabled=false;
      stream.getTracks().forEach(t=>t.stop());
    };
    state.recorder.start();
    $("recordLabel").textContent="Stop recording";
    $("recordStatus").textContent="Recording… read only the sentence shown";
  }catch(e){msg(`Microphone error: ${e.message}`)}
});

$("createVoiceBtn").addEventListener("click", async()=>{
  if(!state.consentBlob)return;
  $("createVoiceBtn").disabled=true;
  $("createVoiceBtn").textContent="Creating voice…";
  $("setupMessage").classList.add("hidden");
  try{
    const consentAudio=await blobToDataURL(state.consentBlob);
    const res=await fetch("/api/create-voice",{
      method:"POST",
      headers:{"Content-Type":"application/json"},
      body:JSON.stringify({consentAudio})
    });
    const data=await res.json();
    if(!res.ok){
      if(data.code==="custom_voice_access"){
        throw new Error("This OpenAI API project does not currently have custom-voice access. The website itself is working, but OpenAI has not enabled exact voice cloning for this project.");
      }
      throw new Error(data.error||"Could not create the voice.");
    }
    localStorage.setItem(VOICE_KEY,data.voiceId);
    msg("Voice created successfully.",true);
    setTimeout(()=>show("callView"),650);
  }catch(e){
    msg(e.message);
  }finally{
    $("createVoiceBtn").disabled=false;
    $("createVoiceBtn").textContent="Create voice";
  }
});

function closeCall(){
  try{state.dc?.close()}catch{}
  try{state.pc?.close()}catch{}
  state.stream?.getTracks().forEach(t=>t.stop());
  if(state.remoteAudio){state.remoteAudio.srcObject=null;state.remoteAudio.remove()}
  state.pc=state.dc=state.stream=state.remoteAudio=null;
  state.muted=false;
  stopTimer();
  $("orb").classList.remove("live");
  $("callTitle").textContent="Ready to call";
  $("callStatus").textContent="Tap below and allow microphone access.";
  $("startBtn").disabled=false;$("muteBtn").disabled=true;$("endBtn").disabled=true;
  $("muteBtn").textContent="🎙";
}

$("startBtn").addEventListener("click", async()=>{
  const voiceId=localStorage.getItem(VOICE_KEY);
  if(!voiceId){show("setupView");return}
  $("startBtn").disabled=true;
  $("callTitle").textContent="Connecting…";
  $("callStatus").textContent="Opening the private voice session.";
  try{
    const tokenRes=await fetch("/api/realtime-token",{
      method:"POST",headers:{"Content-Type":"application/json"},body:JSON.stringify({voiceId})
    });
    const token=await tokenRes.json();
    if(!tokenRes.ok)throw new Error(token.error||"Could not start the call.");

    const pc=new RTCPeerConnection();
    state.pc=pc;

    const remote=document.createElement("audio");
    remote.autoplay=true;remote.playsInline=true;document.body.appendChild(remote);
    state.remoteAudio=remote;
    pc.ontrack=e=>{remote.srcObject=e.streams[0]};

    const stream=await navigator.mediaDevices.getUserMedia({
      audio:{echoCancellation:true,noiseSuppression:true,autoGainControl:true}
    });
    state.stream=stream;
    pc.addTrack(stream.getAudioTracks()[0]);

    const dc=pc.createDataChannel("oai-events");
    state.dc=dc;
    dc.onopen=()=>{
      $("callTitle").textContent="Connected";
      $("callStatus").textContent="Talk normally — you can interrupt naturally.";
      $("orb").classList.add("live");
      $("muteBtn").disabled=false;$("endBtn").disabled=false;startTimer();
    };
    dc.onmessage=e=>{
      try{
        const m=JSON.parse(e.data);
        if(m.type==="input_audio_buffer.speech_started")$("callStatus").textContent="Listening…";
        if(m.type==="input_audio_buffer.speech_stopped")$("callStatus").textContent="Thinking…";
        if(m.type==="response.created")$("callStatus").textContent="Replying…";
        if(m.type==="response.done")$("callStatus").textContent="Listening…";
        if(m.type==="error")$("callStatus").textContent=m.error?.message||"Realtime error.";
      }catch{}
    };
    pc.onconnectionstatechange=()=>{
      if(["failed","closed","disconnected"].includes(pc.connectionState))closeCall()
    };

    const offer=await pc.createOffer();
    await pc.setLocalDescription(offer);
    const sdp=await fetch("https://api.openai.com/v1/realtime/calls",{
      method:"POST",
      headers:{Authorization:`Bearer ${token.value}`,"Content-Type":"application/sdp"},
      body:offer.sdp
    });
    if(!sdp.ok)throw new Error(await sdp.text());
    await pc.setRemoteDescription({type:"answer",sdp:await sdp.text()});
  }catch(e){
    $("callTitle").textContent="Couldn’t connect";
    $("callStatus").textContent=e.message;
    $("startBtn").disabled=false;
  }
});

$("muteBtn").addEventListener("click",()=>{
  const track=state.stream?.getAudioTracks()?.[0];if(!track)return;
  state.muted=!state.muted;track.enabled=!state.muted;
  $("muteBtn").textContent=state.muted?"🔇":"🎙";
  $("callStatus").textContent=state.muted?"Microphone muted":"Listening…";
});
$("endBtn").addEventListener("click",closeCall);

init();
