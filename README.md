# Java Low-Level Design Interview Roadmap

This repository is a pattern-based LLD practice plan for an engineer with about
8 years of experience. The goal is not to memorize dozens of solutions. The
goal is to repeatedly exercise the design decisions that recur across interview
problems and to explain their trade-offs clearly.

## What interviewers expect at this level

At 8 YOE, producing classes and applying a few design patterns is only the
starting point. A strong solution should demonstrate:

- Requirement discovery: clarify actors, use cases, constraints, scale,
  consistency, failure behavior, and what is explicitly out of scope.
- Domain modeling: identify entities, value objects, aggregates, invariants,
  ownership, and lifecycle transitions.
- API design: expose intention-revealing operations, validate inputs, return
  useful results, and define errors rather than leaking implementation details.
- SOLID judgment: use abstractions where change is likely, not an interface for
  every class. Prefer composition and cohesive responsibilities.
- Extensibility: accommodate a credible new requirement with localized changes.
  Explain the extension points and their cost.
- Design-pattern fluency: select patterns because they solve a demonstrated
  problem. Do not force patterns into the design.
- Concurrency correctness: identify shared mutable state, atomic operations,
  lock granularity, deadlock risks, visibility, contention, and thread-safe
  collections.
- Data consistency: protect invariants across repositories and discuss
  transactions, idempotency, optimistic locking, and duplicate requests.
- Failure handling: define domain errors, retries, timeouts, partial failure,
  recovery, and degradation where relevant.
- Testability: inject time, randomness, IDs, repositories, and external
  integrations. Test behavior and invariants, not private implementation.
- Operability: identify useful logs, metrics, audit events, and health signals.
- Performance: know the complexity and memory cost of core operations; avoid
  speculative optimization.
- Communication: state assumptions, compare alternatives, and evolve the design
  incrementally instead of silently coding a complete system.

## Core Java topics to apply

- Immutability, records/value objects, enums with behavior, sealed types where
  they improve domain modeling, and defensive copying.
- Generics, collection selection, equality/hash-code contracts, comparators,
  and avoiding accidental `null` semantics.
- Exceptions versus result types, domain-specific exceptions, validation at
  boundaries, and preserving causal information.
- `ExecutorService`, `CompletableFuture`, cancellation, bounded queues, and
  graceful shutdown.
- `synchronized`, `volatile`, atomics, `ReentrantLock`, read/write locks,
  semaphores, conditions, concurrent collections, and the Java Memory Model.
- Dependency inversion, constructor injection, package boundaries, and keeping
  the domain independent of storage or framework concerns.
- Unit tests, parameterized tests, concurrency tests, fakes, and deterministic
  clocks/ID generators.

## Problem set by recurring pattern

Work in the listed order within each phase. `[present]` means the repository
already contains an implementation to revisit; it does not imply that the
solution meets the senior-level review bar. Difficulty reflects an
interview-sized implementation of the core requirements, not a production
implementation containing every listed extension.

### Phase 1: Modeling, responsibilities, and state

| # | Problem | Difficulty | Patterns and design pressure | Key extensions to practice |
|---:|---|---|---|---|
| 1 | Tic-Tac-Toe | Easy | Entities, Strategy, Factory, game state | NxN board, configurable winning rule, bot player |
| 2 | Snake and Ladder `[present]` | Easy | State, Strategy, injected randomness | Multiple dice, special cells, replayable games |
| 3 | Parking Lot `[present]` | Medium | Strategy, Factory, repositories | Multiple floors/gates, pricing policies, reservations |
| 4 | Vending Machine | Medium | State, command validation, money value object | Refunds, inventory alerts, multiple payment modes |
| 5 | Library Management | Medium | Aggregates, policies, repository boundary | Reservations, fines, multiple item types |
| 6 | Elevator System | Hard | State, Strategy, scheduler | Multiple cars, direction policies, emergency mode |

**Coverage:** domain boundaries, object relationships, invariants, state
transitions, and replacing conditional-heavy logic.

### Phase 2: Extensible business rules and workflows

| # | Problem | Difficulty | Patterns and design pressure | Key extensions to practice |
|---:|---|---|---|---|
| 7 | Splitwise / Expense Sharing | Medium | Strategy, precision, settlement graph | Equal/exact/percentage splits, groups, simplification |
| 8 | Chess | Hard | Command, State, rule composition | Check/checkmate, undo, move history, clocks |
| 9 | Coffee Machine | Medium | Template Method/Strategy, inventory | New recipes, ingredient refill, concurrent dispensers |
| 10 | Shopping Cart and Promotions | Medium | Specification, Chain/Strategy | Stackable rules, coupons, tax, rule priorities |
| 11 | ATM | Medium | State, Chain of Responsibility | Cash denomination strategy, daily limits, rollback |
| 12 | Ticket Resolution `[present]` | Medium | Strategy, workflow/state machine | Skill-based assignment, SLA escalation, audit history |

**Coverage:** Open/Closed Principle, rule engines, money and rounding,
workflows, auditability, and avoiding large service classes.

### Phase 3: Event-driven and platform components

| # | Problem | Difficulty | Patterns and design pressure | Key extensions to practice |
|---:|---|---|---|---|
| 13 | Notification Service | Medium | Strategy, Adapter, Observer | Email/SMS/push, templates, user preferences, retries |
| 14 | Logging Framework | Medium | Chain, Factory, appenders | Async logging, filters, rotation, structured events |
| 15 | Pub/Sub Event Bus | Hard | Observer, producer-consumer | Topic subscriptions, ordering, retries, dead letters |
| 16 | Job Scheduler | Hard | Command, priority queue, workers | Delayed/recurring jobs, cancellation, misfire policy |
| 17 | File Storage / In-Memory File System | Medium | Composite, permissions | Move/copy, quotas, search, concurrent modification |
| 18 | API Client with Retry | Medium | Decorator, Strategy, Adapter | Backoff/jitter, timeout, circuit breaker, idempotency |

**Coverage:** boundaries around external systems, asynchronous work, delivery
semantics, backpressure, retry safety, and observability.

### Phase 4: Concurrency-intensive components

| # | Problem | Difficulty | Patterns and design pressure | Key extensions to practice |
|---:|---|---|---|---|
| 19 | Thread-safe LRU/LFU Cache `[present]` | Medium | Hash map + linked structure, locking | TTL, eviction listener, stats, bounded cleanup |
| 20 | Multi-level Cache `[present]` | Hard | Strategy, promotion, async composition | Per-level policy, stampede protection, partial failure |
| 21 | Rate Limiter `[present]` | Medium | Strategy, time abstraction, atomics | Token bucket/sliding window, per-client config, metrics |
| 22 | Connection Pool | Hard | Pool, semaphore/condition | Timeout, validation, leak detection, graceful shutdown |
| 23 | Inventory and Seat Booking | Hard | Optimistic/pessimistic concurrency | Holds, expiry, idempotent payment callback, oversell |
| 24 | Producer-Consumer Task Queue `[present]` | Medium | Bounded buffer, worker lifecycle | Priorities, backpressure, poison tasks, shutdown |

**Coverage:** linearizable operations, contention, atomic compound actions,
lock scope, stale reads, starvation, lifecycle management, and deterministic
concurrency testing.

### Phase 5: Senior-level integrated designs

| # | Problem | Difficulty | Patterns and design pressure | Key extensions to practice |
|---:|---|---|---|---|
| 25 | Meeting Scheduler / Calendar | Hard | Interval conflicts, policy, transactions | Recurrence, time zones, rooms, concurrent booking |
| 26 | Cab Booking | Hard | State, Strategy, location abstraction | Driver matching, surge pricing, cancellation, races |
| 27 | Food Delivery | Hard | Workflow, Strategy, event publication | Restaurant assignment, order tracking, compensation |
| 28 | Auction System | Hard | State, Observer, concurrency | Bid ordering, reserve price, anti-sniping, audit |
| 29 | Payment Orchestrator | Hard | Adapter, Strategy, idempotency | Multiple gateways, retries, refund, reconciliation |
| 30 | Collaborative Document Editor | Hard | Command, Composite, versioning | Undo/redo, permissions, concurrent edits, snapshots |

**Coverage:** multi-step consistency, explicit aggregate boundaries, race
conditions, idempotency, compensating actions, events, and operational concerns.

## Pattern coverage map

Do not study patterns in isolation. Use this map to ensure each is implemented
and its trade-offs can be explained.

- **Strategy:** parking allocation, pricing, expense split, scheduling, retry,
  eviction, rate limiting.
- **State:** vending machine, ATM, elevator, games, order/ride/auction lifecycle.
- **Factory / Abstract Factory:** object creation driven by type or provider,
  such as vehicles, channels, payment gateways, and appenders.
- **Observer / Pub-Sub:** notifications, auctions, order updates, and event bus.
- **Command:** moves, jobs, undo/redo, and auditable user actions.
- **Chain of Responsibility:** cash dispensing, validation, promotions, logging.
- **Decorator:** retrying/caching/metrics around clients without subclassing.
- **Adapter:** isolate payment, messaging, storage, and notification providers.
- **Composite:** file systems, document structure, and nested rules.
- **Template Method:** stable workflow with replaceable steps; use sparingly
  because inheritance couples implementations.
- **Builder:** constructing valid complex requests or configurations; do not use
  it to hide invalid intermediate states that a better model could prevent.
- **Repository:** persistence boundary for aggregates, not a generic abstraction
  added mechanically to every class.
- **Specification:** composable eligibility, filtering, and promotion rules.

## Checklist for every solution

### Before coding

- Restate the core use cases and ask high-value clarifying questions.
- Separate must-have requirements from extensions.
- State expected scale and whether the process is single-node or distributed.
- Identify actors, domain vocabulary, invariants, and critical race conditions.
- Sketch the main API and object relationships before implementation.

### During design and implementation

- Keep entities responsible for their invariants; avoid an anemic domain model
  and a single "manager" or "service" containing all behavior.
- Separate orchestration, domain rules, persistence, and external integrations.
- Make invalid states hard to construct.
- Introduce interfaces only at actual variation or infrastructure boundaries.
- Use composition by default; justify inheritance.
- Make time, IDs, random behavior, and external dependencies injectable.
- Define concurrency semantics for every shared mutable component.
- Preserve backward-compatible APIs where a later requirement can reasonably be
  added without breaking callers.

### Before presenting the solution

- Walk through one happy path and at least two failure/race paths.
- State time and space complexity for important operations.
- Demonstrate one extension without modifying unrelated classes.
- Explain rejected alternatives and when they would become preferable.
- Cover tests for invariants, state transitions, boundaries, and concurrency.
- Mention logs, metrics, and audit events that would matter in production.
- Explicitly separate an interview-sized implementation from production gaps.

## Critique rubric for each submission

Each completed problem will be reviewed out of 100:

| Area | Weight | Senior-level evidence |
|---|---:|---|
| Requirements and assumptions | 10 | Finds ambiguity and scopes deliberately |
| Domain model and invariants | 20 | Behavior-rich, cohesive model with valid lifecycle |
| API and class design | 15 | Clear contracts, ownership, errors, and boundaries |
| SOLID and extensibility | 15 | Localized changes for credible new requirements |
| Correctness and edge cases | 15 | Handles invalid input, failures, and state transitions |
| Concurrency and consistency | 10 | Identifies and correctly protects atomic operations |
| Testing | 10 | Deterministic tests cover behavior and difficult paths |
| Communication and trade-offs | 5 | Explains choices without pattern name-dropping |

Score interpretation:

- **85-100:** strong senior/staff-leaning interview performance.
- **70-84:** acceptable solution with specific senior-level gaps.
- **55-69:** works for the happy path but needs design depth.
- **Below 55:** remodel before adding more features.

A review should also classify findings as **must fix**, **design improvement**, or
**production consideration**, so interview-critical issues are not mixed with
nice-to-have production features.

## Recommended practice loop

For each problem:

1. Spend 5-10 minutes clarifying requirements and writing assumptions.
2. Spend 10 minutes on APIs, core objects, relationships, and invariants.
3. Implement the smallest end-to-end flow in 35-45 minutes.
4. Add one extension and one concurrency or failure scenario.
5. Write focused tests and a short trade-off note.
6. Submit the problem for critique using the rubric above.
7. Refactor only after reviewing concrete design weaknesses.

Prefer depth over raw count. A useful target is:

- **First pass:** problems 1-12 to establish modeling and pattern fluency.
- **Second pass:** problems 13-24 to strengthen infrastructure and concurrency.
- **Final pass:** choose any four from 25-30 and solve them under a 60-minute
  interview constraint.
- **Revision:** redo the weakest three designs from scratch without consulting
  the first implementation.

## Suggested repository layout for new solutions

```text
src/main/java/lld/designproblems/<problem>/
  api/             # Public contracts and request/result types
  model/           # Entities, value objects, and state
  service/         # Use-case orchestration
  strategy/        # Genuine policy variation
  repository/      # Persistence contracts and in-memory adapters

src/test/java/lld/designproblems/<problem>/
```

The layout is guidance, not a requirement. Small problems should remain small;
do not create empty layers or one-class packages merely to match the template.
