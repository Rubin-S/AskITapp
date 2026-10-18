# Task privacy requirements

Derived from the current task form and preview, not a new product policy. The UI promises
that exact location and access instructions remain private, with provider access only
after the owner accepts that provider's offer.

## Public versus restricted data

Public task responses may contain title, description, category, public area, budget,
schedule, public item requirements, and intentionally published photos. They must not
contain exact coordinates, private addresses/landmarks, or access instructions. Apply
the same restriction to feed, search, public detail, notifications, analytics, and logs.
Client-side filtering is not an authorization boundary.

Task creation must send restricted fields separately from public requirements. The server
must retain them outside public response projections. An authenticated restricted-details
operation must authorize the task owner or its currently assigned provider after offer
acceptance. Knowing a task ID or having applied to the task is not sufficient. Assignment
changes must revoke the previous provider's access; clients must clear stale cached details.

The server derives the author identity from the authenticated session, not a submitted
display name. Responses need a stable author ID and display name so chat recipients and
ownership checks do not depend on mutable names.

## Current implementation and unfinished work

- `Task` is the public model used by Home, Explore, and Task Detail.
- Local task creation no longer copies access instructions into its requirements.
- Local creation receives the current profile's display name instead of a demo name.
- The creation draft contains restricted fields, but the in-memory repository does not
  retain or provide an authorized private-details workflow. Backend persistence and the
  corresponding frontend integration remain unfinished; the privacy copy is not proof
  that this workflow exists.

Before handoff, verify public-response exclusion, owner access, pre-acceptance denial,
assigned-provider access after acceptance, unrelated-user denial, reassignment revocation,
logout/account-switch cache clearing, and that no credentials or restricted location/access
details enter telemetry. The backend contract and frontend integration must cover these
cases together; the current local regression test covers only public task projection.
