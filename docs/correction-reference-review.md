# Correction document reference review

Reviewed: 2026-10-07 (Asia/Kolkata)

Source: [Askit App Correction Priority 26 09 2026](<Askit App Correction_Priority_26_09_2026(1).docx>).

The founder asked for this document to be consulted as reference, explicitly leaving decisions open. Its imperative wording is not an instruction to implement every item. Confirmed conversation decisions remain requirements; document-only suggestions need assessment and review. The source DOCX was read without editing it.

The review covered extracted text, the priority table, embedded reference images, and the two hiring diagrams. The document rendered to 18 pages; the hiring diagrams were inspected on rendered pages 15 and 16. No tracked insertions/deletions or Word comments were found. Render files remain temporary inspection material.

## What changes our understanding of hiring

**Direct Call, page 15:** After a task is published, a provider can claim a contact slot and reveal the seeker's number. The diagram suggests five slots, a dashboard of providers who called, reopening a missed-call slot after 30 minutes, customer assignment, notifications, and then job tracking.

**Show interest, page 16:** A provider submits interest with a note and optional quote. The customer sees interested providers, ratings, and note previews, views profiles, shortlists and calls a provider, assigns the job, and then job tracking begins. The selected provider and other respondents receive different notifications.

These are response/contact patterns, distinct from the already confirmed two pricing routes. Interest need not include a price at the point of first response. They support a useful proposed task-owner dashboard rather than making a numeric bid the only way to respond.

The document does not clearly decide whether each task uses one response mode or permits both. The diagrams end at the beginning of job tracking, so they do not settle completion, cancellations, disputes, or review eligibility. A general service-provider booking heading follows the second diagram, but no separate booking workflow was supplied before the Form A/Form B material.

## Comparison with our working design

| Topic | Reference suggestion | Treatment in planning |
| --- | --- | --- |
| Task responses | Direct Call and Show interest; optional quote on interest | The founder subsequently selected both response actions on every task. Optional quote on interest remains a design proposal supporting the selected in-app pricing route. |
| Task-owner dashboard | Respondents, profiles, ratings, notes, selection/assignment | Recommend one response dashboard feeding the shared job workflow. |
| Contact-slot quota | Five provider slots; hide contact button when full | Open proposal. Decide whether a quota is useful, its scope, concurrency behavior, and whether it can be configured. Do not hard-code five as an approved requirement. |
| Call outcomes | Log calls and reopen missed-call slots after 30 minutes | Needs redesign or an appropriate verified telephony integration. Opening the ordinary Android dialer is not evidence that a call occurred or was answered. Keep contact requests/handoffs distinct from verified call outcomes. |
| Assignment | Seeker assigns; provider is notified | The founder subsequently required provider confirmation before booking and approved the consolidated hiring lifecycle in Q29. Detailed pending expiry and reassignment after booking remain open. |
| Job tracking | Starts after assignment | The founder subsequently selected provider marking work finished followed by seeker confirmation in AskIT, then reviews from both participants. Detailed review rules and stalled/disputed completion handling remain open. |
| Notifications | Push and in-app bell for interest, assignment, and closure | Valuable proposed scope. Delivery, visibility, and notification preferences need real implementation, not success-only messages. |
| Service pricing | Daily labour, piece, square-foot, and Other units | Open design input, not a fixed restriction. Check practical home-service pricing and the confirmed flexible quote routes. |
| Subscription/profile score | Subscription contributes to a 120% profile-strength total | Does not supersede the confirmed free pilot. Subscription features and this scoring approach remain undecided. |
| Service detail | Marked for later discussion | Requires a usable launch design for the confirmed provider-browsing journey. Do not interpret the note as permission to ship a broken or placeholder detail flow. |

Android's ordinary `ACTION_DIAL` opens the phone UI for the user to initiate a call and supplies no call-result output. It does not establish answered/missed status. This finding concerns the ordinary external-dialer handoff; other telephony approaches would require their own feasibility, permission, privacy, and cost assessment. [Android Intent reference](https://developer.android.com/reference/android/content/Intent#ACTION_DIAL), [phone intents](https://developer.android.com/guide/components/intents-common#Phone).

## Other useful design inputs

- Form A describes shared basic account information; Form B describes provider service details. Keep registration open to everyone. Q37 superseded the earlier provider approval gate: manual review earns a badge, while unreviewed providers can publish listings, respond, and accept jobs.
- The notes flag system-bar overlaps, consistent inputs/buttons, task photos, carousel sizing, post ownership actions, reporting, and discovery/profile improvements. Treat these as review candidates, not evidence that every issue still reproduces in the current code.
- Stories, polls, and showcase content align with the requested community scope. Highlights, reposting, pinning, alternate sign-in, preference filters, and additional profile features remain proposals rather than automatically added launch requirements.
- The document's priority labels describe implementation ownership: 1 backend, 2 backend and frontend, 3 frontend. They are not urgency/severity rankings and must not replace the production audit's priorities.

## Response-mode decision and remaining questions

The founder subsequently selected both Direct Call and Show interest on every task, and clarified that seekers and providers are both customers who should be treated equally. Both customer roles can choose immediate or permission-first contact during form filling. The selected provider must confirm before a job is booked. The provider marks work finished and the seeker confirms completion in AskIT; both participants can then review each other. In Q29 the founder approved the consolidated lifecycle, including keeping other responses until confirmation, cancellation with a reason before starting, founder support for disagreements after starting, pending completion with reminders/support, and one review each per completed job. Q37 locked one-account/both-roles, signup contact preferences with task/service-form overrides, number-owner control of disclosure, and assigned-provider access to exact job address after booking. Q37 also superseded the earlier manual-review gate: providers can list, respond, and accept jobs without a badge; review earns the badge only. Detailed contact grants, pending expiry, support resolution powers, and review publication/editing remain open. Section approval does not approve every source-document suggestion or the complete written specification and implementation plan.
