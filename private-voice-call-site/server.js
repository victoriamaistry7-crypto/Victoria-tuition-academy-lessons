import express from "express";

const app = express();
const PORT = process.env.PORT || 10000;
const OPENAI_API_KEY = process.env.OPENAI_API_KEY;
const SAMPLE_B64 = process.env.VOICE_SAMPLE_B64;
const SAMPLE_MIME = process.env.VOICE_SAMPLE_MIME || "audio/ogg";
const VOICE_NAME = "Authorized Boyfriend Voice";
const MODEL = "gpt-realtime-2";

app.use(express.json({ limit: "18mb" }));
app.use(express.static("public"));

function requireServerConfig(res) {
  if (!OPENAI_API_KEY) {
    res.status(500).json({ error: "Voice backend is not configured." });
    return false;
  }
  if (!SAMPLE_B64) {
    res.status(500).json({ error: "Reference voice sample is not configured." });
    return false;
  }
  return true;
}

async function openai(url, options = {}) {
  const response = await fetch(url, {
    ...options,
    headers: {
      Authorization: `Bearer ${OPENAI_API_KEY}`,
      ...(options.headers || {})
    }
  });
  const text = await response.text();
  let data = {};
  try { data = text ? JSON.parse(text) : {}; } catch { data = { raw: text }; }

  if (!response.ok) {
    const err = new Error(data?.error?.message || data?.message || text || `OpenAI error ${response.status}`);
    err.status = response.status;
    err.data = data;
    throw err;
  }
  return data;
}

function decodeDataUrl(dataUrl) {
  if (typeof dataUrl !== "string") throw new Error("Missing audio.");
  const match = dataUrl.match(/^data:([^;,]+)(?:;[^,]*)?;base64,(.+)$/);
  if (!match) throw new Error("Invalid audio payload.");
  const mime = match[1].split(";")[0].trim();
  const bytes = Buffer.from(match[2], "base64");
  return { mime, bytes };
}

app.get("/api/status", (req, res) => {
  res.json({
    ready: Boolean(OPENAI_API_KEY && SAMPLE_B64),
    sampleReady: Boolean(SAMPLE_B64),
    model: MODEL
  });
});

app.get("/api/consent-phrase", async (req, res) => {
  if (!requireServerConfig(res)) return;
  try {
    const data = await openai("https://api.openai.com/v1/audio/consent_phrases");
    const list = Array.isArray(data) ? data : (data.data || data.phrases || []);
    let found = null;
    for (const item of list) {
      const language = String(item.language || item.lang || item.locale || "").toLowerCase();
      const phrase = item.phrase || item.text || item.consent_phrase;
      if (phrase && (language === "en" || language.startsWith("en-"))) {
        found = { language: item.language || item.locale || "en", phrase };
        break;
      }
    }
    if (!found) throw new Error("English consent phrase was not returned.");
    res.json(found);
  } catch (err) {
    res.status(err.status || 500).json({ error: err.message });
  }
});

app.post("/api/create-voice", async (req, res) => {
  if (!requireServerConfig(res)) return;

  try {
    const { mime, bytes } = decodeDataUrl(req.body?.consentAudio);
    if (bytes.length > 10 * 1024 * 1024) {
      return res.status(413).json({ error: "Consent recording is too large." });
    }

    const baseMime = mime.split(";")[0];
    const allowed = new Set([
      "audio/mpeg","audio/wav","audio/x-wav","audio/ogg",
      "audio/aac","audio/flac","audio/webm","audio/mp4"
    ]);
    const consentMime = allowed.has(baseMime) ? baseMime : "audio/webm";

    const consentForm = new FormData();
    consentForm.append("name", `boyfriend-consent-${Date.now()}`);
    consentForm.append("language", "en");
    consentForm.append("recording", new Blob([bytes], { type: consentMime }), "consent.webm");

    const consentResp = await fetch("https://api.openai.com/v1/audio/voice_consents", {
      method: "POST",
      headers: { Authorization: `Bearer ${OPENAI_API_KEY}` },
      body: consentForm
    });
    const consentText = await consentResp.text();
    let consentData = {};
    try { consentData = JSON.parse(consentText); } catch {}
    if (!consentResp.ok) {
      const message = consentData?.error?.message || consentText || "Consent upload failed.";
      return res.status(consentResp.status).json({
        error: message,
        code: "consent_failed"
      });
    }

    const sample = Buffer.from(SAMPLE_B64, "base64");
    const voiceForm = new FormData();
    voiceForm.append("type", "audio_sample");
    voiceForm.append("name", VOICE_NAME);
    voiceForm.append("consent", consentData.id);
    voiceForm.append("audio_sample", new Blob([sample], { type: SAMPLE_MIME }), "reference.ogg");

    const voiceResp = await fetch("https://api.openai.com/v1/audio/voices", {
      method: "POST",
      headers: { Authorization: `Bearer ${OPENAI_API_KEY}` },
      body: voiceForm
    });
    const voiceText = await voiceResp.text();
    let voiceData = {};
    try { voiceData = JSON.parse(voiceText); } catch {}
    if (!voiceResp.ok) {
      const message = voiceData?.error?.message || voiceText || "Voice creation failed.";
      return res.status(voiceResp.status).json({
        error: message,
        code: voiceResp.status === 403 || voiceResp.status === 404 ? "custom_voice_access" : "voice_failed"
      });
    }

    res.json({ ok: true, voiceId: voiceData.id, voiceName: voiceData.name || VOICE_NAME });
  } catch (err) {
    res.status(500).json({ error: err.message || "Voice setup failed." });
  }
});

app.post("/api/realtime-token", async (req, res) => {
  if (!requireServerConfig(res)) return;
  const voiceId = String(req.body?.voiceId || "").trim();
  if (!voiceId.startsWith("voice_")) {
    return res.status(400).json({ error: "A valid custom voice is required." });
  }

  const session = {
    type: "realtime",
    model: MODEL,
    output_modalities: ["audio"],
    instructions: [
      "You are speaking with Victoria in a private conversational voice experience.",
      "You are an AI and not her actual boyfriend.",
      "Never claim to literally be him, never claim to know his private thoughts, and never invent real-world actions he took.",
      "Speak warmly, naturally and affectionately when the user's tone invites it.",
      "Keep most replies short, conversational and emotionally natural, like a relaxed voice call.",
      "The authorized voice likeness may sound like someone she knows, but maintain the distinction that you are an AI.",
      "Do not pressure, manipulate, isolate, guilt, threaten, or encourage dependence."
    ].join(" "),
    audio: {
      input: {
        turn_detection: {
          type: "server_vad",
          threshold: 0.5,
          prefix_padding_ms: 300,
          silence_duration_ms: 650,
          create_response: true,
          interrupt_response: true
        }
      },
      output: { voice: { id: voiceId } }
    }
  };

  try {
    const data = await openai("https://api.openai.com/v1/realtime/client_secrets", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({
        expires_after: { anchor: "created_at", seconds: 300 },
        session
      })
    });
    res.json({ value: data.value, expiresAt: data.expires_at, model: MODEL });
  } catch (err) {
    res.status(err.status || 500).json({ error: err.message });
  }
});

app.get("/health", (req, res) => res.type("text").send("ok"));

app.listen(PORT, "0.0.0.0", () => {
  console.log(`V Private Voice Call running on ${PORT}`);
});
