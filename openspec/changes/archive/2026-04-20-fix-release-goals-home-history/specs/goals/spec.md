# goals Specification Delta

## MODIFIED Requirements

### Requirement: Active goal persistence
The system SHALL preserve an authenticated user's active smoking goal across Settings, Home, mobile, and web.

#### Scenario: Saved daily cap appears on Home
- GIVEN a logged-in user saves a daily cap goal
- WHEN Home refreshes preferences
- THEN Home shows an active goal state
- AND goal progress is evaluated from the saved goal.

#### Scenario: Preferences cannot be fetched
- GIVEN preferences fetch fails
- WHEN Home or Settings loads
- THEN the UI shows an error state
- AND the UI MUST NOT present the fallback as "no active goal".

#### Scenario: Legacy goal identifiers
- GIVEN a stored goal type uses a legacy or case-varied identifier
- WHEN preferences are decoded
- THEN the app maps it to the canonical goal type when unambiguous.
