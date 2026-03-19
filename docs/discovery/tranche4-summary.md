# Tranche 4 Summary

This tranche is discovery-first.

## Decisions
- Coach:
  - continue on Gemini for mobile
  - move to `gemini-2.5-flash-lite`
  - require a backend relay before shipping web coach
- Fitbit:
  - feasible
  - backend and privacy work required first
  - do not implement yet
- Alexa:
  - feasible
  - backend and account linking required first
  - do not implement yet

## Deliverables in this tranche
- explicit technical decision records
- a safer mobile model default for the coach
- a concrete implementation order for the next slice

## Suggested next slice after this tranche
1. Ship a summary-first coach on mobile using the bounded prompt contract.
2. Add a secure relay so web can share the same coaching engine.
3. Revisit Fitbit only after auth/token infrastructure exists.
4. Revisit Alexa only after the same backend surface exists.
