// One-off admin script: retroactively tag smokes by time-of-day rules.
//
// Reads every smoke under users/<uid>/smokes and, for each one that has NO
// relationship yet, assigns triggers based on the local-time hour it happened.
// Safe by default: dry-run unless --apply is passed, and never overwrites a smoke
// that already has triggers or was marked "no relation".
//
// SETUP
//   1. Firebase console → Project settings → Service accounts → Generate new
//      private key. Save the JSON (do NOT commit it).
//   2. Get your UID: Firebase console → Authentication → your user → User UID.
//   3. npm i firebase-admin
//
// RUN (dry-run first — prints what it would do, changes nothing):
//   node scripts/bulk-tag-smokes.mjs \
//     --service-account ./service-account.json \
//     --uid <YOUR_UID> \
//     --tz America/Argentina/Buenos_Aires
//
//   Then, to actually write, add --apply:
//   node scripts/bulk-tag-smokes.mjs --service-account ./service-account.json \
//     --uid <YOUR_UID> --tz America/Argentina/Buenos_Aires --apply
//
// Tag keys must match the app's built-in SmokeTrigger keys (or any custom string):
//   coffee, alcohol, boredom, anxiety, stress, after_meal, social, break, driving, phone

import { readFileSync } from "node:fs";
import { initializeApp, cert } from "firebase-admin/app";
import { getFirestore } from "firebase-admin/firestore";

// ─── EDIT THESE RULES ──────────────────────────────────────────────────────────
// startHour is inclusive, endHour exclusive, both 0–24 in local time.
// A window may wrap past midnight (e.g. { startHour: 22, endHour: 2 }).
// First matching rule wins. Hours with no matching rule are left untouched.
const RULES = [
  { startHour: 6, endHour: 11, tags: ["coffee"] },
  { startHour: 12, endHour: 15, tags: ["after_meal"] },
  { startHour: 15, endHour: 19, tags: ["break"] },
  { startHour: 19, endHour: 24, tags: ["alcohol", "social"] },
  // { startHour: 22, endHour: 2, tags: ["alcohol"] }, // example wrap-around
];
// ────────────────────────────────────────────────────────────────────────────────

function arg(name, fallback = undefined) {
  const i = process.argv.indexOf(`--${name}`);
  if (i !== -1 && i + 1 < process.argv.length) return process.argv[i + 1];
  return fallback;
}
const hasFlag = (name) => process.argv.includes(`--${name}`);

const serviceAccountPath = arg("service-account");
const uid = arg("uid");
const timeZone = arg("tz", "UTC");
const apply = hasFlag("apply");

if (!serviceAccountPath || !uid) {
  console.error("Missing --service-account <path> and/or --uid <uid>. See header for usage.");
  process.exit(1);
}

initializeApp({ cert: cert(JSON.parse(readFileSync(serviceAccountPath, "utf8"))) });
const db = getFirestore();

// Local hour (0–23) for an epoch-millis timestamp, in the given time zone.
function localHour(epochMillis) {
  const parts = new Intl.DateTimeFormat("en-US", {
    timeZone,
    hour: "2-digit",
    hour12: false,
  }).formatToParts(new Date(epochMillis));
  const h = Number(parts.find((p) => p.type === "hour").value);
  return h === 24 ? 0 : h;
}

function tagsForHour(hour) {
  for (const r of RULES) {
    const inWindow =
      r.startHour <= r.endHour
        ? hour >= r.startHour && hour < r.endHour
        : hour >= r.startHour || hour < r.endHour; // wrap past midnight
    if (inWindow) return r.tags;
  }
  return null;
}

function isUntracked(data) {
  if (data.relationshipSkipped === true) return false;
  const triggers = data.triggers;
  const hasTriggers = Array.isArray(triggers) && triggers.length > 0;
  const hasNote = typeof data.triggerNote === "string" && data.triggerNote.trim() !== "";
  return !hasTriggers && !hasNote;
}

async function main() {
  const col = db.collection(`users/${uid}/smokes`);
  const snap = await col.get();
  console.log(`Found ${snap.size} smokes for uid=${uid} (tz=${timeZone}, apply=${apply}).`);

  let toUpdate = [];
  let skippedTagged = 0;
  let skippedNoRule = 0;
  const perTag = {};

  snap.forEach((doc) => {
    const data = doc.data();
    if (!isUntracked(data)) {
      skippedTagged++;
      return;
    }
    const millis = Number(data.timestampMillis ?? data.a); // 'a' = legacy field
    if (!Number.isFinite(millis)) return;
    const tags = tagsForHour(localHour(millis));
    if (!tags) {
      skippedNoRule++;
      return;
    }
    tags.forEach((t) => (perTag[t] = (perTag[t] || 0) + 1));
    toUpdate.push({ ref: doc.ref, tags });
  });

  console.log(
    `Would tag ${toUpdate.length} smokes. Skipped ${skippedTagged} already-tagged, ${skippedNoRule} with no matching rule.`,
  );
  console.log("Tag distribution:", perTag);

  if (!apply) {
    console.log("\nDry-run only. Re-run with --apply to write these changes.");
    return;
  }

  // Firestore batches are capped at 500 writes.
  let written = 0;
  for (let i = 0; i < toUpdate.length; i += 450) {
    const batch = db.batch();
    for (const { ref, tags } of toUpdate.slice(i, i + 450)) {
      batch.set(ref, { triggers: tags, relationshipSkipped: false }, { merge: true });
    }
    await batch.commit();
    written += Math.min(450, toUpdate.length - i);
    console.log(`Committed ${written}/${toUpdate.length}…`);
  }
  console.log(`Done. Tagged ${written} smokes.`);
}

main().catch((e) => {
  console.error(e);
  process.exit(1);
});
