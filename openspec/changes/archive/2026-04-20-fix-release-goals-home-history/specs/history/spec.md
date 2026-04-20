# history Specification Delta

## MODIFIED Requirements

### Requirement: Add smoke for selected date
History SHALL add a smoke to the selected day when Add for Date is pressed.

#### Scenario: Add smoke to selected historical date
- GIVEN the user is viewing a historical day
- WHEN the user presses Add for Date
- THEN a smoke is created on that day
- AND the History list refreshes showing the new entry.

#### Scenario: Add smoke to current day
- GIVEN the user is viewing the current day
- WHEN the user presses Add for Date
- THEN a smoke is created near the current time
- AND the Home/widget snapshot can refresh from the new entry.
