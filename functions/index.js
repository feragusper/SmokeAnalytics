const admin = require("firebase-admin");
const { defineSecret } = require("firebase-functions/params");
const { onRequest } = require("firebase-functions/v2/https");

admin.initializeApp();

const coachApiKey = defineSecret("COACH_GEMINI_API_KEY");
const modelName = "gemini-2.5-flash-lite";
const maxMessageLength = 240;

exports.coachRelay = onRequest(
  {
    region: "us-central1",
    secrets: [coachApiKey],
    invoker: "public",
    maxInstances: 2,
    timeoutSeconds: 15,
    memory: "256MiB",
  },
  async (req, res) => {
    res.set("Cache-Control", "no-store");

    if (req.method !== "POST") {
      res.status(405).json({ error: "method-not-allowed" });
      return;
    }

    const authHeader = req.get("Authorization") || "";
    const idToken = authHeader.startsWith("Bearer ") ? authHeader.slice(7).trim() : "";
    if (!idToken) {
      res.status(401).json({ error: "auth-required" });
      return;
    }

    try {
      await admin.auth().verifyIdToken(idToken);
    } catch (_error) {
      res.status(401).json({ error: "invalid-auth" });
      return;
    }

    const kind = req.body?.kind;
    const context = normalizeContext(req.body?.context);
    if (!context || (kind !== "initial" && kind !== "message")) {
      res.status(400).json({ error: "invalid-request" });
      return;
    }

    const message = kind === "message" ? normalizeMessage(req.body?.message) : "";
    if (kind === "message" && !message) {
      res.status(400).json({ error: "invalid-message" });
      return;
    }

    const fallbackText =
      kind === "initial"
        ? fallbackInitialCoachMessage(context)
        : fallbackCoachReply(message, context);

    try {
      const prompt =
        kind === "initial"
          ? buildInitialCoachPrompt(context)
          : buildConversationPrompt(message, context);

      const response = await fetch(
        `https://generativelanguage.googleapis.com/v1beta/models/${modelName}:generateContent?key=${coachApiKey.value()}`,
        {
          method: "POST",
          headers: { "Content-Type": "application/json" },
          body: JSON.stringify({
            contents: [{ parts: [{ text: prompt }] }],
            generationConfig: {
              temperature: 0.6,
              topP: 0.9,
              maxOutputTokens: kind === "initial" ? 140 : 220,
            },
          }),
        },
      );

      if (!response.ok) {
        throw new Error(`gemini-${response.status}`);
      }

      const json = await response.json();
      const text =
        json?.candidates?.[0]?.content?.parts
          ?.map((part) => part?.text || "")
          .join(" ")
          .trim() || "";

      if (!text) {
        throw new Error("empty-response");
      }

      res.status(200).json({
        text,
        source: "live",
      });
    } catch (_error) {
      res.status(200).json({
        text: fallbackText,
        source: "fallback",
      });
    }
  },
);

function normalizeContext(input) {
  if (!input || typeof input !== "object") return null;

  const safeName = String(input.name || "Smoker").trim().slice(0, 80) || "Smoker";

  return {
    name: safeName,
    todayCount: clampCount(input.todayCount),
    weekCount: clampCount(input.weekCount),
    monthCount: clampCount(input.monthCount),
    totalCount: clampCount(input.totalCount),
    hoursSinceLastSmoke: clampCount(input.hoursSinceLastSmoke),
    minutesSinceLastSmoke: clampCount(input.minutesSinceLastSmoke),
    currentStreakHours: clampCount(input.currentStreakHours),
    longestStreakHours: clampCount(input.longestStreakHours),
    averageGapMinutes: clampCount(input.averageGapMinutes),
  };
}

function normalizeMessage(input) {
  if (typeof input !== "string") return "";
  return input.trim().replace(/\s+/g, " ").slice(0, maxMessageLength);
}

function clampCount(value) {
  const numeric = Number(value);
  if (!Number.isFinite(numeric)) return 0;
  return Math.max(0, Math.min(Math.round(numeric), 100000));
}

function buildInitialCoachPrompt(context) {
  return `
You are Smoke Analytics Coach.
Your role is to help the user reduce smoking through calm, concrete, non-judgmental advice.
Keep the response short, practical, and human. Avoid cheesy lines, therapy language, and long disclaimers.

User:
- Name: ${context.name}
- Smokes today: ${context.todayCount}
- Smokes this week: ${context.weekCount}
- Smokes this month: ${context.monthCount}
- Logged smokes in context window: ${context.totalCount}
- Time since last smoke: ${context.hoursSinceLastSmoke}h ${context.minutesSinceLastSmoke}m
- Current streak hours: ${context.currentStreakHours}
- Longest streak hours: ${context.longestStreakHours}
- Average gap between recent smokes: ${context.averageGapMinutes} minutes

Write one compact opening message that:
- explains the coach's purpose in one short sentence
- comments on the user's current pattern using the provided data
- gives one concrete next-step suggestion
- stays below 80 words
`.trim();
}

function buildConversationPrompt(message, context) {
  return `
You are Smoke Analytics Coach.
Keep answers short, specific, and actionable.
Focus on helping the user delay, reduce, or better understand smoking triggers.

User context:
- Name: ${context.name}
- Smokes today: ${context.todayCount}
- Smokes this week: ${context.weekCount}
- Smokes this month: ${context.monthCount}
- Time since last smoke: ${context.hoursSinceLastSmoke}h ${context.minutesSinceLastSmoke}m
- Current streak hours: ${context.currentStreakHours}
- Longest streak hours: ${context.longestStreakHours}
- Average gap between recent smokes: ${context.averageGapMinutes} minutes

User message:
${message}

Reply with:
- empathy first
- one practical suggestion grounded in the user's data
- no fluff
- under 120 words
`.trim();
}

function fallbackInitialCoachMessage(context) {
  const firstLine =
    context.todayCount === 0 && context.totalCount === 0
      ? "No smokes logged yet. Start by tracking honestly, not perfectly."
      : context.hoursSinceLastSmoke >= 8
        ? "Strong start. You're already putting real space between cigarettes."
        : context.todayCount <= 3
          ? "You are keeping the day under control. Protect the next few hours."
          : "Today's pattern is visible now. The next decision matters more than the last one.";

  const secondLine =
    context.hoursSinceLastSmoke >= 4
      ? "Try to stretch this streak a little more before the next one."
      : context.minutesSinceLastSmoke < 45
        ? "If this is a craving window, wait ten minutes and do something physical first."
        : "Aim for one more delayed cigarette today rather than a perfect day.";

  return `${firstLine} ${secondLine}`;
}

function fallbackCoachReply(message, context) {
  const normalized = message.toLowerCase();

  if (normalized.includes("stress") || normalized.includes("ans") || normalized.includes("nerv")) {
    return `If stress is driving this, don't negotiate with the cigarette yet. Take a short walk, water, and give it ten minutes. You've already gone ${context.hoursSinceLastSmoke}h ${context.minutesSinceLastSmoke}m since the last one.`;
  }

  if (normalized.includes("crav") || normalized.includes("want") || normalized.includes("need")) {
    return "Cravings peak and fall. Delay the next cigarette by ten minutes, then decide again. The goal is not magic willpower, it's breaking the automatic loop.";
  }

  if (normalized.includes("progress") || normalized.includes("doing") || normalized.includes("how")) {
    return `Today you're at ${context.todayCount} smokes, ${context.weekCount} this week, with ${context.hoursSinceLastSmoke}h ${context.minutesSinceLastSmoke}m since the last one. The clean win is to make the next gap longer than your recent average of ${context.averageGapMinutes}m.`;
  }

  if (normalized.includes("slip") || normalized.includes("smoked") || normalized.includes("failed")) {
    return "One cigarette is data, not defeat. Log it, reset cleanly, and focus on the next interval. The app is for recovery, not punishment.";
  }

  return `Use the coach for cravings, planning, and pattern checks. Right now the best move is simple: delay the next cigarette and protect your current gap of ${context.hoursSinceLastSmoke}h ${context.minutesSinceLastSmoke}m.`;
}
