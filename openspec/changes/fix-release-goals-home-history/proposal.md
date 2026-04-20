# Fix release regressions in goals, home track CTA, and history add-for-date

## Why
The latest release regressed core daily use: active goals can disappear from Home, goal save failures are not visible, mobile Home lacks an obvious track action, the web track button can lose contrast in light theme, and History's add-for-date action does not produce an understandable result.

## What
- Preserve and surface persisted active goals on mobile and web.
- Stop treating preferences fetch failures as "no goal".
- Restore a visible mobile Home track CTA and fix web track button contrast.
- Make History add-for-date create a smoke in the selected day at an expected visible time.

## Success Criteria
- A saved goal remains visible after returning to Home on mobile and web.
- Preference load/save failures show an error instead of silently falling back to empty preferences.
- Mobile Home has a visible Track button wired to the existing add-smoke intent.
- Web Home primary Track button has high contrast in light and dark themes.
- History Add for Date adds an entry to the selected day and refreshes the list.
