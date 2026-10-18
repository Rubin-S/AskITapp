# AskIT launch planning: questions and decisions

Last updated: 2026-10-07 (Asia/Kolkata)

This is a working requirements record. The complete design, implementation plan, and release scope have not yet been reviewed. It does not authorize a production release.

The [consolidated written design](launch-design.md) is now available for founder review. It separates locked conversational decisions from proposed detailed defaults. Earlier answers below are historical where later answers supersede them.

## Terminology

Both service seekers and service providers are AskIT customers and should be treated equally. Use "seeker" and "provider" to distinguish their roles; do not use "customer" as an exclusive synonym for seeker. Ownership-based job permissions still apply.

**Current provider-review rule (Q37):** Manual review earns a badge only. Providers may publish service listings, respond to tasks, and accept jobs without that badge. This supersedes the approval-before-job requirement in Q16 and its extension in Q32.

## Founder objectives

- Make development significantly faster and easier.
- Make AskIT a useful, production-ready startup app.
- Include enough useful features and launch as soon as possible.
- Use a question-and-answer process to establish the plan.

## Answers so far

### 1. Primary reasons to install AskIT

**Founder answer:** All three: get local work done, find customers and earn, and connect with the local community. The first two carry more weight.

**Requirement:** Customer and provider journeys are primary. Community remains part of the product; its exact launch features are undecided.

### 2. Geographic scope of the first launch

**Founder answer:** One city or district.

**Requirement:** Plan a focused local launch.

### 3. Candidate launch locations

**Founder answer:** Pondicherry, Chennai, or Bengaluru.

**Requirement:** These are candidates. Subsequent expansion timing has not been agreed.

### 4. Strongest local network or presence

**Founder answer:** Pondicherry.

**Working planning assumption:** Use Pondicherry as the first launch city because of the founder's local network. This was proposed in the Q&A; it is not a separate explicit city-selection answer.

**Subsequent decision:** Q39 selected the invited Pondicherry pilot explicitly.

### 5. Initial service focus

**Founder answer:** Home services, while allowing people from other categories to create accounts.

**Requirement:** Focus launch recruitment and the main experience on home services. Registration must remain open to other categories. Q32 subsequently confirmed listing and hiring for other categories; Q37 made manual review a badge-only process across categories.

### 6. How customers find and hire help

**Founder answer:** Both browsing providers and posting tasks.

**Requirement:** Support finding a provider and sending a request, as well as posting a need and choosing a provider from offers. Detailed pricing, acceptance, communication, and job-completion rules remain undecided.

### 7. Payments at launch

**Founder answer:** Customers pay providers directly.

**Requirement:** Plan discovery, requests, and job management around direct customer-to-provider payments. Payment methods and how users record payment status remain undecided.

### 8. Initial business model

**Founder answer:** Free pilot: attract customers and providers, then introduce revenue after validating demand.

**Requirement:** Plan a free pilot for customers and providers. The later revenue model and criteria for introducing it remain undecided.

### 9. Development resources

**Founder answer:** The founder, working with AI coding tools such as Codex.

**Requirement:** Size development and ongoing operations for one founder using AI tools. No additional developer or development team has been committed.

**Planning direction to evaluate:** Prefer simple operations, repeatable verification, and small implementation steps. Specific backend services and automation choices have not yet been selected.

### 10. Monthly technical running budget

**Founder answer:** Undecided.

**Requirement:** No monthly spending limit has been agreed for hosting, database, media storage, and login SMS. Marketing is outside this budget question.

**Planning direction:** Compare expected pilot costs and maintenance effort before selecting paid services. No paid-service commitment follows from this answer.

### 11. Pilot timing

**Founder answer:** As soon as it is ready, with no fixed date.

**Requirement:** Prioritize the earliest pilot ready for real customers and providers. No calendar deadline has been agreed. Release timing depends on the agreed scope and production-readiness checks.

### 12. Existing backend

**Founder answer:** No backend exists outside the Android project; it needs to be built.

**Requirement:** Include backend implementation and Android integration in the launch plan. Select the architecture after comparing scope, cost, and maintenance requirements for a founder using AI coding tools.

### 13. Requested community features for the first pilot

**Founder answer:** Text/photo posts, questions, polls, comments, likes, recommendations, stories, and before/after content.

**Requirement:** Include these features in the requested pilot scope and assess their implementation and production requirements in the design. This answer has not yet established a final reviewed release scope.

**Details still open:** Recommendation behavior, poll rules, and the relationship between before/after content and service jobs. Video posts were not explicitly requested in this answer.

**Planning direction to evaluate:** Share publishing and interaction infrastructure across community content types where practical.

### 14. Story features at launch

**Founder answer:** Text and photos, with polls as well.

**Requirement:** Include text stories, photo stories, and story polls in the requested pilot scope. Story video is outside the selected initial story scope. Story expiry, audience, and poll behavior need design decisions.

### 15. Communication about requests and jobs

**Founder answer:** Phone calls or WhatsApp.

**Requirement:** Plan customer/provider job communication around phone calls and WhatsApp. In-app chat was not selected for this workflow. Rules for contact visibility, consent, and external-app handoff remain to be designed.

**Scope boundary:** This answer covers job communication; it does not separately decide community direct messaging or notifications. It does not authorize sending messages or placing calls to anyone.

### 16. Home-service provider approval

**Founder answer:** Manual review and approval before home-service providers start accepting jobs.

**Historical requirement, superseded by Q37:** Keep account registration open to everyone. This answer originally required founder review before a home-service provider could accept jobs, and Q32 extended it across categories. Q37 explicitly replaced the gate: review earns a badge, while unreviewed providers can list, respond, and accept jobs.

### 17. Initial provider recruitment capacity

**Founder answer:** Not assessed yet.

**Requirement:** Do not assume an initial provider count or existing provider roster. Include assessing and recruiting the first home-service providers in the pilot plan before settling category coverage and launch targets.

### 18. Backend approach

**Founder answer:** "C from day one" — a custom backend.

**Requirement:** Build a custom API and AskIT's business rules from the start. Approach C included managed PostgreSQL and separate media storage; choosing a custom backend does not by itself require self-hosting those infrastructure components. No hosting provider, framework, authentication provider, or spending commitment has been selected.

**Design direction to evaluate:** One backend codebase with clearly separated modules, shared contracts for Android integration, and a founder administration interface. Final architecture and implementation steps still need design review.

### 19. Backend language and framework preference

**Founder answer:** No preference; recommend a stack for fast development and manageable maintenance.

**Requirement:** Evaluate and recommend a concrete stack for the selected custom backend, considering a solo founder using AI coding tools, the existing Android HTTP integration, and production operations.

### 20. Backend foundation and scalability requirement

**Founder answer:** The proposed foundation is fine, with the requirement that the backend be highly scalable and future-proof.

**Requirement:** Proceed with Python, Django REST Framework, and PostgreSQL as the accepted foundation for the design, subject to an explicit growth and upgradeability strategy. Preserve the earlier product and launch requirements.

**Meaning to establish in the design:** Add API and worker capacity independently, keep durable state and media outside application instances, evolve modules and API contracts safely, and measure capacity with representative workloads. No numeric capacity or unlimited-growth guarantee has been agreed or demonstrated.

**Details still open:** Growth horizon, daily active users, peak traffic, workload mix, operating budget, and performance/recovery targets. Foundation acceptance does not constitute review of the complete specification or implementation plan.

### 21. Two-year daily active user target

**Founder answer:** No target yet.

**Requirement:** Do not assume a numeric two-year daily-active-user forecast or capacity commitment. Retain the scalability and upgradeability requirement. Establish representative workload assumptions and measurable capacity checks during design, then revisit sizing using pilot traffic and operating costs.

### 22. Agreeing on the price

**Founder answer:** Both: discuss by phone/WhatsApp and record the agreed amount in AskIT, or submit and accept priced offers in AskIT.

**Requirement:** Support both pricing routes, while keeping customer-to-provider payment direct. Define shared quote-confirmation and job rules so the routes lead into a consistent job lifecycle.

### 23. Hiring workflow review and correction-document reference

**Founder answer:** Consult the supplied correction document as reference, not as a hard line; some decisions in it remain open.

**Requirement:** Consider the document when refining the hiring workflow. Do not promote its suggestions to confirmed requirements or treat its imperative wording as authorization to implement them.

**Review result:** The diagrams distinguish Direct Call and Show interest, including optional quotes and a task-owner response dashboard. Five call slots, a 30-minute missed-call reopening rule, response-mode selection, and assignment/confirmation behavior remain proposals. The diagrams stop at the beginning of job tracking and do not settle completion or review rules.

See [the reference comparison](correction-reference-review.md). The proposed hiring workflow is still under review; this answer did not approve it.

### 24. Task response options and equal treatment

**Founder answer:** Both providers and seekers are customers and should be treated equally; select B, making both response options available on every task.

**Requirement:** Every task offers Direct Call and Show interest. The seeker does not choose an exclusive response mode per task. Providers and seekers are both customers in the product's terminology and service approach.

**Subsequent decisions:** Q25/Q37 settled contact choices, Q26–29 settled the hiring lifecycle, and Q37 permits task responses without the review badge. Contact limits remain open. Availability of both task actions does not by itself approve public phone-number exposure or the reference document's five-slot limit.

### 25. Contact-sharing choice during form filling

**Founder answer:** Support both immediate contact and permission per request, letting either the seeker or provider choose A or B while filling in their forms.

**Requirement:** Offer both contact-sharing preferences to both customer roles during the relevant forms. Support immediate contact with consent and permission-first contact. Keep both Direct Call and Show interest on every task; a contact-sharing preference does not replace those response options.

**Design interpretation to review:** A person's preference governs AskIT's disclosure of their own number. A caller's preference cannot override the number owner's permission-first choice. The Direct Call action requests access when permission is required.

**Details still open:** Exact form placement, account defaults versus task/listing overrides, contact-request expiry/revocation, and notification behavior. The setting governs AskIT's number disclosure; it does not provide anonymous calling or conceal normal phone caller ID or WhatsApp account details.

### 26. Provider confirmation before booking

**Founder answer:** A: the selected provider confirms before the job is booked.

**Requirement:** A seeker's selection creates a pending assignment, not a confirmed booking. Require the selected provider's explicit confirmation before booking. Submitting interest or a priced offer does not by itself replace that confirmation.

**Design details to review:** Keep other responses available until confirmation; handle provider decline or seeker withdrawal without falsely reporting a filled job. Pending-assignment expiry and reassignment rules still need design decisions.

### 27. Seeker confirmation of job completion

**Founder answer:** A: the provider marks the work finished, and the seeker confirms in AskIT.

**Requirement:** The provider's finished-work action requests completion. The job becomes completed only after the seeker confirms in AskIT. A completion request alone does not count as a completed job.

**Details still open:** Reminders, handling unresponsive participants or disputed completion, and review eligibility rules. No automatic completion deadline has been approved.

### 28. Reviews from both customer roles

**Founder answer:** A: seekers review providers, and providers review seekers after a completed job.

**Requirement:** Support reviews from both participants after the job has been completed through seeker confirmation. Attribute each review to its actual author and attach it to the other participant's profile in the context of that job.

**Design details to review:** One review per participant per job, publication/editing rules, reporting/moderation, and how role-specific ratings appear on profiles. Reciprocal reviews are selected; the detailed review policy is not yet approved.

### 29. Consolidated hiring lifecycle review

**Founder answer:** A: approve the presented hiring flow, including its recommended rules.

**Approved section:** Both entry routes lead to a recorded agreement on work, price, and schedule. Seeker selection requires explicit provider confirmation before booking. The provider marks work started and later finished; the seeker confirms completion. Both participants can review the other, once each per completed job.

**Approved handling:** Keep other responses available until provider confirmation. Either participant can cancel before work starts, giving a reason. After work starts, disagreements or cancellation requests go to founder support. Unconfirmed completion stays pending with reminders/support rather than completing automatically.

**Scope:** This approves the hiring section presented in Q29, including contact-sharing choices on task actions. It does not settle the remaining contact form/default/request details, pending-assignment expiry, provider approval evidence or response eligibility, notification/reminder timing, review publication/editing, or administrative resolution powers. The complete written specification and implementation plan remain to be reviewed.

### 30. Community recommendations

**Founder answer:** A: anyone can recommend a provider in a post, linking their AskIT profile. Keep these separate from reviews tied to completed jobs.

**Requirement:** Allow community recommendations without requiring a completed AskIT job. Link the recommended provider's profile. Treat recommendations as community content, with the normal account and moderation rules; they must not be represented as completed-job reviews or included in completed-job rating totals.

**Design details still open:** Recommendation presentation, self-promotion handling, editing/reporting, and linked-profile deletion behavior.

### 31. Community section review

**Founder answer:** A: approve the presented community design, including its defaults.

**Approved section:** A local public feed focused on the chosen city/area, with recent posts first. Text/photo posts, questions, polls, recommendations linking provider profiles, and paired before/after photos support likes and comments. Seekers and providers participate equally.

**Approved poll/story rules:** One poll choice per account, changeable until closing, with results visible after voting and to the author. Text, photo, and poll stories remain visible for 24 hours; story polls close when their story expires.

**Approved controls:** Authors can delete their content. Users can report and block. The founder manages reports through administration tools.

**Scope:** This approves the community experience presented in Q31. Precise area is not a confidentiality boundary for public posts. Detailed poll limits/closing choices, editing, block effects, media consent/retention/deletion, moderation actions, and community direct messaging still need specification. It does not approve the complete written specification or implementation plan.

### 32. Participation outside home-service categories

**Founder answer:** A: people in other categories can join the community, list services, and take jobs, using the same manual provider approval. Recruitment and promotion still focus on home services.

**Requirement:** Do not limit the pilot's listing and hiring journeys to home services. Support other categories through the shared listing/job workflows. Community participation and account registration remain open to both customer roles across categories. The manual-approval gate selected here was subsequently replaced in Q37 by a badge-only review process.

**Scope:** This originally extended provider approval beyond home services; Q37 now permits listing, responses, and jobs without the badge across categories. It does not require separate custom workflows for every profession or establish the exact supported category catalogue or category-specific review evidence.

### 33. Private messaging at launch

**Founder answer:** B: include private messaging between community members from day one.

**Requirement:** Include in-app private messaging in the pilot release scope. This selects community messaging in addition to the previously selected phone/WhatsApp service-contact flow; it does not replace the recorded job agreement, booking, or completion actions.

**Design details still open:** Who can initiate conversations, message requests/consent, supported message types, delivery/read behavior, notifications, blocking/reporting, deletion/retention, and founder moderation access. Group chat, voice/video calls, and end-to-end encryption have not been selected or promised.

**Planning consequence:** Include durable conversation/message storage, participant authorization, retry-safe sends, pagination, and appropriate messaging verification in the proposed design and implementation estimates. Notifications and reporting/blocking are design requirements to work through, not implemented capabilities.

### 34. New-conversation message requests

**Founder answer:** A: the first message goes into a requests inbox. The recipient accepts before the conversation continues.

**Requirement:** Gate new private conversations through recipient acceptance. Keep first-contact messages in a separate requests inbox and prevent further conversation before acceptance. Enforce the pending/accepted state on the backend, including send retries and concurrent actions.

**Design details still open:** First-request content and limits, decline/re-request behavior, supported message types after acceptance, notifications, and report/block handling. Acceptance of an in-app conversation does not automatically authorize disclosure of either person's phone number.

### 35. Private messaging section review

**Founder answer:** A: approve the presented messaging design, including its defaults.

**Approved section:** One-to-one text and photo conversations. A pending request contains one text-only introduction; the recipient accepts before further messages or photos. Recipients can accept, decline, or block. Conversations are saved, with unread counts, notifications, and clear sending/failed/retry indicators.

**Approved controls:** Blocking prevents new messages and requests while preserving access to existing job records under job permissions. Reports can include selected messages for founder review through administration tools.

**Scope:** This approves the messaging experience presented in Q35. Exact repeat-request rules, message limits, notification previews/preferences, retention/deletion, delivery/read indicators beyond those presented, and moderator access boundaries still need specification. It does not approve group chat, in-app calls, or the complete written specification and implementation plan.

### 36. Manual provider approval checks

**Founder answer:** A: verified phone number, service details, and work examples where available. Approval means "Profile reviewed."

**Requirement:** Review the provider's profile and services manually, using phone verification, service details, and available work examples. Describe the approval as "Profile reviewed" and keep it distinct from phone-number verification and completed-job ratings. Apply the same review approach across supported categories, consistent with Q32.

**Scope:** This selects profile/service review rather than identity-document checks. Do not label this review as identity verification, a background check, or a guarantee of work quality. Q37 subsequently made review badge-only. The detailed submission checklist, rejection/resubmission flow, and suspension/re-review rules remain to be specified.

### 37. Account/contact section locked with badge-only provider review

**Founder answer:** "B provider do can use service listing responding to tasks or accept job, but he won't have verified batch that is it. Rest all fine and lock"

**Controlling requirement:** Providers may publish service listings, respond to tasks, and accept jobs without manual review. Successful manual review earns the badge; absent review means no badge. Replace the prior review gates in Q16/Q32 and the proposed broader gate in Q37's presented section. Phone verification, ordinary account permissions, moderation, and mutual booking confirmation remain separate rules.

**Locked remainder:** One account can both seek and provide services. Both roles choose immediate or permission-first contact during signup, with an override per task/service form. The number owner's preference controls phone-number access. Public tasks show the area; the exact job address is available to the assigned provider after booking confirmation.

**Badge meaning:** The founder called it the verified badge in this correction. Retain Q36's selected "Profile reviewed" wording and actual phone/profile/service checks unless the founder changes the label. Do not infer identity/background checks from the badge.

**Scope:** This locks the revised account/contact section. Exact grant scope/expiry/revocation, review resubmission/suspension, retention/deletion, and the complete written specification and implementation plan still need specification/review.

### 38. Backend and development approach locked

**Founder answer:** A: lock the presented backend and development approach.

**Locked section:** Retain the Android app and integrate complete durable journeys with the custom backend. Use clear modules for accounts, services, jobs, community, messaging, and administration. Store records in PostgreSQL and photos separately; scale web requests and background processing independently. Maintain shared API rules, repeatable local setup, and development data. Automate meaningful permission, persistence, and retry checks, with staging and real-device verification before release. Measure representative performance/load capacity to guide growth.

**Cost rule:** Compare hosting costs before choosing paid services. No vendor, monthly budget, numeric capacity guarantee, or spending commitment was selected by this approval.

**Scope:** This locks the architecture/development approach presented in Q38. Exact dependencies, infrastructure and messaging transport, capacity/recovery targets, and the complete written specification and implementation plan remain to be reviewed.

### 39. Delivery and launch approach locked

**Founder answer:** "AA", interpreted as approval of option A: lock the presented delivery and launch approach.

**Locked sequence:** Accounts/services and optional review badges; both hiring routes through completion/reviews; all selected community and messaging features; production remediation and release verification.

**Locked launch approach:** Recruit providers and seekers alongside development. Begin with a small invited pilot in Pondicherry, measuring completed jobs, repeat use, response times, support needs, and running costs before wider promotion. Pondicherry is now selected for the first pilot, replacing the earlier working assumption.

**Readiness and cost rules:** Resolve launch-blocking audit findings and verify privacy, report/block controls, account deletion, backups, monitoring, and the signed production app on real devices. Compare infrastructure costs before paid commitments. No fixed date, participant count, running budget, or production-readiness claim follows from this answer.

**Next artifact:** Consolidate the agreed sections and clearly identified detailed design proposals into the written specification for founder review before writing the implementation plan.

## Questions still to resolve

- Future revenue model after the selected free pilot.
- Review the proposed detailed defaults in the written design: contact grants, support resolution, review editing/publication, limits, retention, tooling, and capacity/recovery targets.
- Owner confirmation of the initial supported service category catalogue and practical recruitment coverage.
- Founder resources: an agreed budget after cost comparison.
- Definition of a successful pilot and initial recruitment capacity.
- Growth targets remain unforecast; review the synthetic workload and recovery targets proposed in the written design.
- Detailed infrastructure/tooling choices within the locked backend and development approach.
- Launch scope, staged implementation, testing, operations, and release acceptance.

## Audit baseline

See [the production readiness audit](production-readiness-audit.md) for evidence and release requirements. It found that core user journeys currently rely on fixtures or in-memory state and that production account, data, safety, and release work remains.

The implementation plan should address the audit findings relevant to the agreed launch scope and explicitly resolve or remove unsupported launch behaviors before release.
