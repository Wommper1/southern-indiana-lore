// Read-aloud with the browser's built-in speech (Web Speech API). Nothing here records or sends audio or text.
// Some browsers provide online voices; this page prefers a voice that runs on the device when one is available.

export const speechSupported =
  typeof window !== "undefined" && "speechSynthesis" in window && "SpeechSynthesisUtterance" in window;

let unlocked = false;
let speaking = false;
let generation = 0; // bumped on every speak/stop so late events from cancelled speech are ignored
const listeners = new Set();

export function onSpeakingChange(listener) {
  listeners.add(listener);
  return () => listeners.delete(listener);
}

function setSpeaking(value) {
  if (speaking === value) return;
  speaking = value;
  for (const listener of listeners) listener(value);
}

export function isSpeaking() {
  return speaking;
}

function pickVoice() {
  const voices = window.speechSynthesis.getVoices();
  const english = voices.filter((voice) => /^en([-_]|$)/i.test(voice.lang));
  return (
    english.find((v) => v.localService && /^en[-_]US$/i.test(v.lang)) ||
    english.find((v) => v.localService) ||
    english.find((v) => /^en[-_]US$/i.test(v.lang)) ||
    english[0] ||
    null
  );
}

/** iOS only allows speech that starts from a tap. Call this inside a tap handler to allow later automatic speech. */
export function unlock() {
  if (!speechSupported || unlocked) return;
  try {
    const silent = new SpeechSynthesisUtterance(" ");
    silent.volume = 0;
    window.speechSynthesis.speak(silent);
    unlocked = true;
  } catch (error) {
    /* ignore: speech simply stays locked */
  }
}

function chunk(text) {
  const sentences = text.match(/[^.!?]+[.!?]+["')\]]*\s*|[^.!?]+$/g) || [text];
  const out = [];
  let current = "";
  for (const sentence of sentences) {
    if (current && (current + sentence).length > 220) {
      out.push(current.trim());
      current = "";
    }
    current += sentence;
  }
  if (current.trim()) out.push(current.trim());
  return out;
}

export function speak(text) {
  if (!speechSupported || !text) return;
  window.speechSynthesis.cancel();
  const mine = ++generation;
  const voice = pickVoice();
  const parts = chunk(text);
  parts.forEach((part, index) => {
    const utterance = new SpeechSynthesisUtterance(part);
    if (voice) {
      utterance.voice = voice;
      utterance.lang = voice.lang;
    } else {
      utterance.lang = "en-US";
    }
    utterance.rate = 0.95;
    if (index === parts.length - 1) {
      utterance.onend = () => mine === generation && setSpeaking(false);
    }
    utterance.onerror = () => mine === generation && setSpeaking(false);
    window.speechSynthesis.speak(utterance);
  });
  setSpeaking(true);
}

export function stop() {
  if (!speechSupported) return;
  generation += 1;
  window.speechSynthesis.cancel();
  setSpeaking(false);
}
