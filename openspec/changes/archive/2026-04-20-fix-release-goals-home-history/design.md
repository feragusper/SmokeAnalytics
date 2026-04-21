# Design

## Preferences and Goals
Use the existing Firestore document shape and make decoding tolerant at the platform repository boundary. Shared domain keeps the canonical goal mapping so mobile and web do not diverge on accepted goal identifiers.

Home and Settings should fail loudly enough for UI state to show an error. Smoke list fetches used only for secondary progress can still fall back to an empty list.

## Home CTA
Mobile gets a content-level Track action in the goal-first layout. Web keeps the sticky Track action but switches to button tone classes instead of elapsed card classes.

## History Add for Date
The existing `HistoryIntent.AddSmoke(date)` remains the contract. The process holder normalizes the selected day to a timestamp using the current local clock time for historical dates, and now for current-day dates. This avoids adding at the bucket boundary, which looks like a no-op to users.
