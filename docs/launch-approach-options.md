# AskIT: launch approach comparison

Prepared: 2026-10-07 (Asia/Kolkata)

Status: the founder selected **approach C: a custom backend from day one**. This document records the comparison and the current design direction. It is not the final specification or implementation plan. No hosting service has been selected or purchased.

The [consolidated written design](superpowers/specs/2026-10-07-askit-production-launch-design.md) is now the proposed specification for founder review. Keep this comparison as decision history; the written design identifies the locked sections and proposed detailed defaults.

## Understanding of the founder's goal

AskIT should help service seekers get local work done, help service providers find work and earn, and support local community interaction. Both roles are AskIT customers and should be treated equally. The seeker/provider journeys carry the most weight. Development should become substantially faster and easier, with a useful production-ready pilot released as soon as it is ready.

The first launch covers one city or district. Q39 selected a small invited pilot in Pondicherry, where the founder has the strongest local network; Chennai and Bengaluru were also considered.

Home services are the primary recruitment and promotion focus. Registration, community participation, service listings, and hiring are open to other categories too, using shared workflows. Both browsing providers and posting tasks are required. Seekers pay providers directly and coordinate through phone calls or WhatsApp. Q37 superseded the earlier manual-approval gates: providers may list, respond, and accept jobs without a badge; manual profile/service review earns the badge only. The pilot is free.

Requested community scope: text/photo posts, questions, polls, comments, likes, recommendations, before/after content, stories supporting text/photos/polls, and private messaging between community members from day one (Q33).

The founder will build with AI coding tools. There is no existing backend, no fixed deadline, no agreed running budget, and no assessed initial provider count. See [the Q&A record](launch-planning-qa.md) for exact answers and unresolved details.

## What the existing project contributes

The existing Android application and design system are the starting point. Many screens and navigation flows already exist, but the [production audit](production-readiness-audit.md) found core operations dependent on fixtures or in-memory data.

The [authentication contract](auth-backend-contract.md) is explicitly proposed and has not been tested against a live backend. A managed authentication service will require adapter and contract changes. Existing sign-in screens, recovery behavior, credential-storage protections, and account-isolation requirements remain valuable and should be retained where compatible. Selecting a backend does not make the app ready by changing its URL.

## Three approaches

| Approach | Main benefit for this project | Work and tradeoffs |
| --- | --- | --- |
| **A. Supabase managed backend — alternative** | Managed PostgreSQL, authentication, and file storage provide a shared foundation. My judgment is that relational data fits accounts, approvals, listings, offers, jobs, reviews, and polls well. | Implement the domain rules, access policies, Android adapters, and founder review tools. Phone login requires SMS-provider setup. The Kotlin client is community-maintained, so compare its compatibility and session storage with a small HTTP adapter before selecting the integration. |
| **B. Firebase managed backend** | Official Android integration and managed application services make this a credible alternative. | Choose a coherent database approach and adapt the current contracts. Firestore uses documents and collections; Firebase also offers a managed PostgreSQL route through SQL Connect, so Firebase is not limited to document storage. Authentication, data, media, and server execution need a combined cost model. |
| **C. Custom API with managed PostgreSQL and separate media storage — selected** | Maximum control over the proposed HTTP interface and backend rules. Managed hosting such as Render can run the API and database. | Own more API code, deployment, authentication integration, monitoring, and recovery work. My judgment is that this adds maintenance and implementation effort for a founder building with AI tools. Managed authentication can still be used rather than writing the identity system from scratch. |

Sources: [Supabase pricing and included services](https://supabase.com/pricing), [Supabase phone login](https://supabase.com/docs/guides/auth/phone-login), [Supabase Kotlin integration](https://supabase.com/docs/reference/kotlin/introduction), [Firebase Android setup](https://firebase.google.com/docs/android/setup), [Firestore data model](https://firebase.google.com/docs/firestore/data-model), [Firebase SQL Connect](https://firebase.google.com/docs/sql-connect), and [Render pricing](https://render.com/pricing).

## Selected direction: custom backend from day one

Build and own AskIT's API and business rules. Q38 locked one modular backend codebase for accounts, provider review/listings, tasks/offers/jobs, community, messaging, media, and administration. Detailed interfaces and dependencies belong in the written specification.

Use PostgreSQL for application records and separate object storage for photos. Managed hosting, databases, and storage remain compatible with this choice; there is no requirement to operate database servers manually. Django REST Framework is the selected foundation; vendors and detailed authentication integration remain infrastructure/design inputs.

Keep provider requests and posted tasks connected to one job model. Share publishing, media, comments, likes, polls, reports, and blocking across relevant community formats. Important writes should be controlled on the server: provider approval, assigning a job, completing or cancelling it, recording a review, and voting. Preserve the public/private task separation in the [task privacy requirements](task-privacy-handoff.md).

Define the API contract alongside the Android adapters, preserving the useful existing contracts and agreeing changes explicitly. Use a founder administration interface for approvals and moderation. The aim is to reduce duplicated code and operating work while retaining control of the backend.

The custom backend requires API, deployment, monitoring, and recovery work to be included in the plan. Keep those responsibilities explicit when estimating effort and running costs.

## Accepted backend foundation and design details

Status: the founder accepted this foundation with an explicit requirement for high future scalability and future-proofing. The complete architecture, specification, and implementation plan have not yet been reviewed.

**Use Python, Django, Django REST Framework, and PostgreSQL as the design foundation.** The proposed Django release line is 5.2 LTS, with compatible current patches and dependencies pinned when implementation begins. The official support table lists security/data-loss support for that line through April 2028, and REST Framework lists Django 5.2 as supported. [Django support schedule](https://www.djangoproject.com/download/), [REST Framework requirements](https://www.django-rest-framework.org/).

The project-specific reason for this choice is the founder's need for approval and moderation tools alongside a large set of data-backed application features. Django includes an internal administration interface, database models, and migrations; REST Framework provides API serialization and validation. These capabilities should reduce bespoke platform code, although AskIT's actual rules and workflows still need implementation. [Django overview](https://docs.djangoproject.com/en/5.2/intro/overview/), [Django administration](https://docs.djangoproject.com/en/5.2/ref/contrib/admin/), [API serializers](https://www.django-rest-framework.org/api-guide/serializers/).

TypeScript with NestJS is a credible alternative with structured application code and OpenAPI tooling. My recommendation favors Django here because its internal administration tools directly fit the founder's selected manual provider approval and community moderation needs. This is a fit judgment, not a claim that NestJS cannot implement those features. [NestJS](https://docs.nestjs.com/), [NestJS OpenAPI](https://docs.nestjs.com/openapi/introduction).

Proposed structure:

- Keep the existing Kotlin/Compose Android app. Add the custom backend alongside it, with its own dependencies and configuration.
- Organize one backend application into identity/profiles, provider review/listings, tasks/offers/jobs, community, messaging, media, and administration modules. Share the underlying domain rules between customer APIs and administrative actions.
- Use PostgreSQL for durable records and transactions. Use separate S3-compatible object storage for uploaded photos; the provider is undecided.
- Customize Django administration for approval queues, reports, account support, and configuration. Sensitive job/approval changes must use authorized domain actions and audit records, rather than arbitrary table editing.
- Preserve the useful `/v1` HTTP contracts and Android session protections. Define required contract additions and deliberate changes before connecting each journey. Phone OTP and rotating sessions need explicit implementation and testing; Django's administration login does not provide the app's phone-login system.
- Maintain an OpenAPI description with drf-spectacular and use it for documentation and compatibility checks. Evaluate generated Android models where they reduce work without replacing the tested session lifecycle. REST Framework recommends drf-spectacular instead of its deprecated built-in schema generator. [Schema tooling](https://www.django-rest-framework.org/api-guide/schemas/), [drf-spectacular](https://drf-spectacular.readthedocs.io/en/latest/).
- Keep background work in the same backend codebase, with durable retry behavior for operations such as media processing and notifications. The worker/queue implementation and deployment shape will be settled in the complete design.

Proposed development workflow:

- Repeatable local setup with isolated development data and test doubles for SMS and other paid external services.
- Small complete changes connecting Android, the API, persistence, permissions, failure states, and relevant tests.
- Automated backend/API permission and contract checks alongside the existing Android checks. REST Framework provides API testing utilities. [API testing](https://www.django-rest-framework.org/api-guide/testing/).
- Separate development, staging, and production configuration; no demonstration accounts or privileged credentials in the release app.

Hosting, storage/SMS vendors, operating costs, exact dependency versions, and full product rules remain open. Foundation acceptance is separate from review of the completed specification and implementation plan.

## Scalability and evolution requirements

Translate the founder's future-proofing requirement into an architecture that can evolve and add capacity incrementally. Actual capacity depends on the implementation, workload, infrastructure, and budget; it must be measured. No user-count guarantee has been established.

Proposed design requirements:

- **Replicated API instances:** Run stateless API instances behind a load balancer. Keep sessions, OTP attempt limits, approvals, job state, and uploads in shared services rather than authoritative process memory or local application disks. Add API instances without changing the Android contract.
- **Independent background capacity:** Run background workers from the same backend codebase with durable queues, bounded retries, and idempotent processing. Scale notifications and photo processing independently of interactive requests. Queue technology remains a design decision.
- **Separate media delivery:** Store photos in object storage and serve appropriate renditions through a CDN. Use access-controlled upload and delivery paths; restricted media and addresses must retain their authorization rules. Storage/CDN vendors are undecided. [Object-storage performance guidance](https://docs.aws.amazon.com/AmazonS3/latest/userguide/optimizing-performance.html).
- **Bounded database work:** Use pagination, appropriate indexes, efficient related-record queries, and bounded connection pools. Profile representative queries and measure changes. Avoid fetching an entire feed or user history in one response. [Django database optimization](https://docs.djangoproject.com/en/5.2/topics/db/optimization/).
- **Database growth path:** Begin with a correctly sized PostgreSQL primary and tested recovery. Evaluate additional resources, read replicas, caching, and partitioning against measured bottlenecks. Job transitions and authorization need correct consistency; replicas cannot be treated as interchangeable for every operation. PostgreSQL supports multiple replication and availability approaches with different tradeoffs. [PostgreSQL availability and replication](https://www.postgresql.org/docs/current/high-availability.html).
- **Safe concurrent writes:** Enforce assignments, votes, and other uniqueness rules in the database and transactional domain operations. Verify that competing accepts, retries, and worker restarts cannot create duplicate jobs or side effects.
- **Evolvable boundaries:** Keep identity, jobs, community, and media responsibilities explicit. Define service interfaces and durable event boundaries where needed so a busy subsystem can be separated later. Keep cross-module database access controlled and documented.
- **Compatible upgrades:** Version API contracts, favor compatible additions, and use staged schema migrations that allow old and new application versions to overlap. Lock dependencies, apply security patches, and review framework support before end of life.
- **Geographic growth:** Model cities and service coverage as data so expansion beyond Pondicherry does not require rewriting core flows or introducing fixed city assumptions throughout the application.
- **Evidence for scaling:** Record request latency, error rate, database saturation, and worker backlog. Load-test representative discovery/feed reads, publishing/uploads, and competing job actions against explicit targets. Include burst and sustained workloads. Set measurable triggers for adding capacity and repeat tests after relevant changes.

The founder has no two-year daily-active-user target yet. Peak concurrency, traffic assumptions, latency targets, and recovery objectives still need to be established. Daily active users alone cannot determine server sizing. Define initial workload-based capacity checks during design, then revisit sizing against observed pilot traffic and operating costs. Later scaling may require additional infrastructure and operational staffing; no associated spending has been committed.

## Backend and development section locked in Q38

Status: Q38 locked the presented modular backend, independently scalable web/background processing, durable database/media storage, shared API rules, repeatable setup, and automated/staging/device verification approach. Exact dependencies, vendors, transports, and numeric targets remain detailed design inputs.

1. Retain the existing native Kotlin/Compose Android app. Replace demonstration/in-memory behavior with complete durable journeys, connecting screens, backend records, permissions, and failure/retry handling together.
2. Build one modular custom backend with clear boundaries for accounts, provider review/listings, jobs, community, messaging, media, and founder tools. Keep shared business rules consistent across the Android API and administrative actions.
3. Store durable structured records in PostgreSQL and photos in separate object storage. Keep API instances stateless so API capacity can be added independently; use durable background workers for media/notifications and scale them separately. Shared infrastructure must retain authorization and retry/concurrency guarantees across instances.
4. Define one versioned API contract and repeatable local setup with isolated sample data and external-service test substitutes. Deliver small complete journeys so AI-assisted development follows stable rules and reduces duplicated work.
5. Automate meaningful contract, permission, persistence, and concurrent/retried-action checks. Use staging and physical-device acceptance before production release. Profile/load-test representative workloads and track latency, errors, database pressure, and queue backlog to decide when to increase capacity.
6. Select hosting, storage, SMS, queue/live messaging transport, and exact dependency versions in the detailed design with costs and operational tradeoffs. No paid-service commitment is made by section approval. Numeric capacity and recovery targets remain design inputs to establish, rather than guarantees inferred from the stack.

This section approval covers the presented modular structure, independently scalable application/background capacity, durable storage, and repeatable development/verification approach. The complete written specification and implementation plan still require their own reviews.

## Hiring workflow: approved lifecycle and remaining design details

Status: the founder approved the consolidated hiring lifecycle in Q29, including its recommended pending-response, cancellation, completion, and review rules. The consolidated subsection below identifies that approval's scope; other detailed rules remain proposals where indicated. The founder supplied a correction document as reference, explicitly leaving decisions open. See [the reference comparison](correction-reference-review.md).

### Enter through either hiring route

- **Browse a provider:** The seeker sends a service request. The provider responds, and the parties agree on scope, timing, and price. Q37 permits providers across categories to accept jobs without the review badge.
- **Post a task:** The seeker posts the work needed with a public area and optional budget. Both Direct Call and Show interest are available on every task. The reference suggests an optional quote on interest. Recommend a task-owner response dashboard for viewing providers, notes, any quotes, shortlisting, and assignment. Q37 permits responses without manual review; successful review earns a badge only. Both participants are customers; "seeker" and "provider" describe their roles.
- **Direct Call details:** Both roles choose immediate contact or permission-first contact during signup, with task/service-form overrides. Q37 locks the rule that the number owner's preference governs disclosure, so Direct Call requests access if permission is required. Detailed grant scope/expiry/revocation remain to be specified. Five contact slots and reopening a missed-call slot after 30 minutes are document-only proposals. The ordinary phone handoff does not verify call outcomes; distinguish contact requests from actual answered/missed calls. Contact preferences do not provide anonymous phone calls or conceal normal WhatsApp account details.

### Agree and confirm

- Support priced offers inside AskIT and negotiations by phone/WhatsApp. Record the agreed scope, amount, and timing in AskIT and require both parties' confirmation before treating the job as booked.
- The founder requires the selected provider to confirm before booking. A seeker's selection creates a pending assignment; neither prior interest nor a priced offer substitutes for explicit confirmation of that assignment. Confirm the recorded scope, price, and timing as part of the booking agreement, including quotes discussed outside AskIT.
- Approved handling: retain other responses while the assignment is pending. If the provider declines or the seeker withdraws, allow another selection. Mark the job filled only after booking confirmation. Notification details, expiry, and reassignment after booking still need design decisions.
- Keep a versioned record of the agreement. Scope or price changes after confirmation require both parties to confirm the new version.
- Create one canonical job regardless of the entry route. Use server-side ownership checks and transactional assignment rules so concurrent accepts and retries cannot book the same task twice.
- Enforce the selected contact-sharing preference on the server and avoid including phone numbers in public discovery responses. Q37 locks the rule that a caller's immediate-contact preference cannot override the number owner's permission-first preference. Public task responses contain a public area only; exact address and access instructions are restricted to the seeker and currently assigned provider after booking confirmation, consistent with the existing [task privacy requirements](task-privacy-handoff.md).

### Track and complete

Approved shared lifecycle: request/open task, agreement and assignment pending provider confirmation, confirmed job, in progress, completion requested, completed. Keep cancellation and dispute states separate from successful completion. Exact wire-state names belong in the final API specification.

- Either side can cancel before work starts, with a reason recorded and the other side notified.
- The provider marks work started. For completion, the founder selected the provider marking work finished followed by the seeker confirming in AskIT. Only that seeker confirmation completes the job.
- A completion request alone is not a completed job and does not enable a completed-job review. Pending confirmation stays visible; reminders and founder assistance handle stalled cases. No automatic completion deadline is proposed yet.
- After work starts, a disagreement or cancellation request can be raised for founder review. The app does not claim to collect, refund, or verify direct payments.
- The founder selected reviews from both participants after completion: seekers review providers and providers review seekers, with one review per participant per completed job. Reviews are attached to the counterpart's profile with the real author identity and job association. Publication, editing, reporting, and moderation rules still need design review.
- Record job actions and founder interventions for traceability. Administrative actions follow explicit permission and state-transition rules.

The seeker/provider screens should show real server state, failures, and recovery paths. Notifications, reminder timing, contact-sharing form/default/request details, provider approval evidence, and dispute-handling limits remain to be specified.

### Consolidated lifecycle approved in Q29

Status: approved in Q29. This section approval does not constitute approval of the complete written specification or implementation plan.

1. Either a direct service request or a posted task starts the hiring journey. Posted tasks offer both Direct Call and Show interest, subject to the number owner's contact preference.
2. The participants agree on the work, price, and schedule, using either an in-app offer or an agreement discussed by phone/WhatsApp and recorded in AskIT.
3. The seeker selects the provider. The provider explicitly confirms the recorded agreement before booking. Q37 superseded the earlier founder-approval gate: providers may accept jobs without the review badge.
4. Keep other task responses available while confirmation is pending. A decline or withdrawn selection lets the seeker choose another provider; only a confirmed booking marks the task filled.
5. The provider marks work started. The provider later marks work finished; the seeker confirms completion in AskIT.
6. Both participants can review the other after completion, with one review each per completed job.
7. Either participant can cancel before work starts, with a reason and notification. After work starts, disagreements or cancellation requests go to founder support. Stalled completion stays pending for reminders/support; elapsed time alone does not complete the job. Payments stay direct between participants.

This review covers the shared hiring lifecycle and these proposed exception rules. Contact form/default/request details, approval evidence and response eligibility, notification/reminder timing, review publication/editing, and other feature sections still need specification. Do not interpret section approval as approval of document-only contact quotas or automatic call-outcome tracking.

## Account, provider review, and contact section locked in Q37

Status: the founder locked this section in Q37 with one correction: review earns a badge only and cannot gate service listings, task responses, or job acceptance. Q36 selected verified-phone/profile/service review with available work examples, labelled "Profile reviewed."

- **One account for both roles:** A person can seek services and provide services from the same account, using shared profile information and the same community access. Provider review is a badge process within that account.
- **Review earns a badge only:** Providers may publish service listings, respond to tasks, and accept bookings before review. The review badge appears only after successful manual review. Keep badge status separate from normal account permissions, moderation restrictions, and transactional job rules.
- **Clear approval meaning:** Check phone verification, profile/service details, and work examples where available. Display "Profile reviewed" after founder approval. Keep phone ownership, profile review, and completed-job ratings separate; do not imply identity-document or background verification.
- **Contact choices in forms:** Ask both roles to choose immediate contact or permission-first contact during account setup. Let task/service-listing forms inherit that preference with an explicit per-item override. The number owner's setting governs access; a caller's setting cannot override it. Exact grant scope, expiry, and revocation semantics still need specification.
- **Private job details:** Public discovery/task responses expose only the chosen public area. Exact job address and access instructions are available to the seeker and currently assigned provider after booking confirmation, consistent with the existing task privacy requirements. Phone-number disclosure remains governed separately by the selected contact-sharing rule.

This section approval locks account roles, badge-only review, contact form placement and overrides, and job-detail visibility. Detailed review resubmission/suspension, contact grant scope/expiry/revocation, account-deletion/retention, and the complete written specification and implementation plan still need specification/review.

## Community design: section approved in Q31

Status: the founder approved the community experience presented in Q31, including the local public feed, publishing types, interactions, poll/story defaults, and author/report/block controls. Remaining implementation and policy details are identified below; the complete written specification and implementation plan remain to be reviewed.

- **Local public feed:** Focus the feed on the selected city/area and show recent content first. Location is a discovery filter, not a confidentiality boundary. Use the chosen public area rather than exposing precise device or job locations. Registered seekers and providers can participate on equal terms, including people outside the initial home-service focus. Public visibility is the approved pilot audience rule; private/follower-only publishing has not been selected.
- **Shared publishing:** Support text/photo posts, questions with replies through comments, polls, recommendations linking provider profiles, and paired before/after photos with captions. Share authorship, media, likes, comments, reporting, and deletion rules across these types where practical. Recommendations and before/after content do not establish completion of an AskIT job or alter completed-job rating totals.
- **Polls:** Single-choice voting with one active vote per account; votes may be changed until closing. Show results after voting and to the author. Use an explicit closing time and enforce voting/closing on the server. Detailed option limits and closing-time choices belong in the written design.
- **Stories:** Support text, photos, and polls. Use 24-hour visibility from publication, with story polls closing when the story expires. Enforce expiry on the server; distinguish visibility expiry from the separate media-retention/deletion policy.
- **Safety and ownership:** Authors can remove their content; accounts can report content or profiles and block other accounts. Founder moderation tools handle reports. Before/after photos follow the content consent rules and must not expose private job details. Exact block effects, content editing, deletion/retention, and moderation actions need explicit specification.

This section approval covers the presented community experience and defaults. Q33 subsequently selected community private messaging for launch, and Q35 approved the messaging section below. Remaining privacy/retention details and the complete written specification and implementation plan still need review.

### Private messaging: section approved in Q35

The founder selected private messaging between community members from day one. Preserve the phone/WhatsApp service-contact flow and explicit job agreement/booking/completion actions. Private messages do not implicitly change job state or establish a completed-job review entitlement.

Q34 selected message requests: a first message goes into a separate requests inbox and the recipient must accept before the conversation continues. Conversation acceptance does not grant phone-number disclosure. Q35 approved one-to-one text/photos, request limits, send/failure/retry presentation, notifications/unread counts, blocking, and report evidence as presented. Remaining notification preferences, deletion/retention, and moderation access details need specification. Group conversations, in-app voice/video calls, and end-to-end encryption have not been selected or promised.

Pilot messaging section approved in Q35:

- One-to-one conversations supporting text and photos after acceptance.
- One text-only introduction per pending request. The recipient can accept, decline, or block. Further messages and photos require acceptance. Declining closes that request; exact repeat-request rules still need specification.
- Persist accepted conversations and messages across app restarts and devices. Clearly distinguish a message still sending, a successful send, and a failed send with retry. Do not infer delivery or reading from a successful send alone.
- Provide unread counts and new-message notifications, with notification controls and report/block actions. Blocking prevents new messages and new requests between the pair; existing job records remain accessible under their separate job permissions and support flow.
- Let a report include selected messages as evidence for founder review. Proposed access boundary, still requiring specification: founder access is to reported messages and explicitly submitted context, rather than general browsing of private conversations. Record moderator access and actions. Retention and account-deletion behavior still require specification.

Status: Q35 approves the presented messaging experience, including one text-only introduction before acceptance, text/photos afterward, saved conversations, unread counts, notifications, send/failure/retry indicators, blocking, and reports with selected messages. Repeat-request rules, precise limits, notification controls/previews, retention/deletion, and moderator access boundaries remain to be specified. This section does not select group chat, calls, typing indicators, or read receipts for the pilot.

Proposed technical requirements: persist conversations and messages on the backend; authorize every conversation read and send for the current participant; use retry-safe message identifiers, bounded pagination, and reconnect/catch-up behavior. Treat notification delivery as separate from durable message delivery. Enforce blocks and report controls on the server. The live-update transport and detailed verification belong in the written design after product rules are reviewed.

## Running cost considerations

As checked on 2026-10-07, Supabase Pro starts at **US$25 per month** for the included initial project setup and provides daily database backups retained for seven days. Extra environments, usage, and optional services can increase that amount. Its free tier is useful for development, but has inactivity pausing and lacks automatic database backups. [Supabase pricing](https://supabase.com/pricing).

Supabase phone sign-in requires an SMS provider. Message costs must be added to the backend budget, and the production delivery setup must be confirmed for India before launch. [Supabase phone-login requirements](https://supabase.com/docs/guides/auth/phone-login).

Firebase uses usage-based billing for paid services, including phone authentication SMS; estimating a pilot requires assumptions about login volume, data access, media, and server operations. [Firebase pricing](https://firebase.google.com/pricing).

Render separates workspace fees, compute, and metered resources. A custom stack also needs a separate authentication/SMS and media-storage estimate. Paid Render PostgreSQL includes point-in-time recovery. [Render pricing](https://render.com/pricing), [database recovery](https://render.com/docs/postgresql-backups).

These are comparison inputs, not an agreed budget or total-cost quote. A budget should account for subscription/compute, SMS attempts and resends, photo storage and delivery, development versus production environments, monitoring, and applicable taxes. No currency-conversion estimate is assumed.

## Proposed development habits to reduce repeated work

- Keep one current specification and decision record so AI-assisted changes follow the same product rules.
- Use shared identity, job, media, and community models instead of separate stores for each screen.
- Work in small complete journeys, connecting the screen, backend, permissions, failure states, and relevant verification together.
- Provide repeatable local setup and development data, with a clear separation from production user data.
- Automate relevant build, backend, contract, and authorization checks. Retain physical-device acceptance for behavior automation cannot establish.
- Extract shared code from large screens when integration needs it; keep refactoring tied to the feature being delivered.

These are design recommendations. Specific tools, scripts, tasks, and execution steps belong in the later reviewed implementation plan.

## Delivery and launch approach locked in Q39

Status: Q39 locked the presented delivery sequence and invited Pondicherry pilot. All selected pilot features, including private messaging, remain in the release scope. Development milestones below are sequencing, not authorization to release an incomplete milestone as the pilot.

1. **Real accounts and provider foundation:** durable signup/profile data, badge review, service listings, photo upload, and founder review tools. Listing, responses, and booking must work without the review badge.
2. **Complete hiring journeys:** provider requests and posted tasks/offers feeding one job lifecycle, phone/WhatsApp handoff, private job details, and legitimate reviews.
3. **Requested community experience:** posts, questions, polls, comments, likes, recommendations, before/after content, text/photo/poll stories, and private messaging, using shared infrastructure where practical. Q33 includes private messaging in the pilot, so its complete delivery and controls must be accounted for before release.
4. **Pilot acceptance and release:** complete applicable audit remediation, moderation and account deletion, approved policy/support destinations, recovery and monitoring checks, real-device validation, production configuration, signing, and staged release.

Privacy, authorization, moderation, and failure behavior belong in each stage; the final stage verifies the complete product. Provider recruitment and service-coverage assessment should proceed alongside development. Release readiness cannot be inferred from the number of screens or a passing build alone.

### Launch/readiness rules and remaining detailed proposals

- Q39 selected a small invited pilot in Pondicherry, using the founder's strongest network. Broader promotion follows evidence from the pilot rather than a fixed date or invented provider count.
- Recruit initial providers and seekers alongside development. Check practical category/area coverage and founder support capacity before inviting participants; no initial roster has been established.
- Before pilot release, demonstrate persistent accounts/listings, both hiring routes through real completion and reviews, the selected community formats, and request-gated text/photo messaging. Resolve relevant audit findings and remove unsupported release paths.
- Verify authorization/privacy, contact sharing, report/block and founder support tools, account deletion, and usable policy/support destinations. Demonstrate backup restoration, monitoring/alerts, and a recovery/rollback procedure. Validate the signed production app on real devices with production configuration and compatible backend behavior.
- Measure successful job completion, repeat use, response times, unresolved reports/support load, and operating costs during the pilot. Establish quantitative workload/recovery and acceptance targets in the written design; do not claim numeric capacity, uptime, or a launch date from this section approval.
- Compare hosting, media, SMS, and operational costs before paid infrastructure selection. Keep the budget and infrastructure commitment as explicit owner decisions before paid setup. Expand promotion/coverage once pilot evidence and support capacity justify it.

Status: Q39 locks the presented sequence, city/pilot approach, measurement categories, audit remediation, privacy/report/block/deletion checks, backups, monitoring, signed-device validation, and cost comparison. Detailed recovery/rollback and quantitative thresholds still need written design review. The audit baseline remains NO-GO until remediation and verification provide new evidence. This section approval does not authorize publication or paid setup.

## Decisions to address in the design

- Request/offer pricing, provider selection, cancellations, completion, disputes, and contact-sharing rules.
- Provider review implementation details and category catalogue: Q36 selected phone/profile/service review with available work examples and the label "Profile reviewed." Q37 superseded the review gates across categories: providers can list, respond, and accept jobs without the badge. Resubmission/suspension and detailed review tools still need specification.
- Community implementation details: poll limits/closing choices, before/after ownership and consent, editing, block effects, and retention. Local feed/audience, story expiry, and poll voting/results defaults were approved in Q31. Community recommendations linking provider profiles remain separate from completed-job reviews.
- Private messaging implementation details: Q35 approved the messaging experience alongside phone/WhatsApp job communication; precise limits, repeat-request rules, notification controls/previews, retention/deletion, and moderator access boundaries remain to be specified.
- Founder moderation/support workflow and practical operating capacity.
- Pilot coverage, recruitment targets, and success measures once recruitment capacity has been assessed.
- Hosting/database/storage selection, SMS setup, budget, and release acceptance mapped to the audit within the Q38-locked backend/development approach.

The next step is to consolidate the agreed design and clearly identified remaining detailed proposals into the written specification for founder review before the implementation plan.
