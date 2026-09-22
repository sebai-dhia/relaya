# Relaya

> **Status: Demonstration project — not deployed, not production**

Relaya converts unstructured client intake text into a structured, source-backed project handoff — then writes one approved card to a Trello board.

## What it does

1. Paste raw client intake text (email, form, notes)
2. AI extracts objectives, deliverables, constraints, timeline, budget and proposed tasks
3. Every fact is traced back to a source excerpt or flagged as unknown
4. Reviewer edits and explicitly approves the draft
5. One Trello card + checklist is created — never before approval

## Stack

| Layer | Technology |
|-------|-----------|
| Frontend | Angular + TypeScript |
| Backend | Java Spring Boot |
| Database | PostgreSQL |
| AI Provider | Groq (dev/free tier) — swappable via provider interface |
| Board Connector | Trello API (stub until credentials available) |

## Project structure

```
relaya/
├── frontend/        # Angular UI
├── backend/         # Spring Boot API
├── docs/
│   ├── architecture/  # Architecture decisions and diagrams
│   └── evaluation/    # Test results, evaluation rubric
├── fixtures/
│   ├── complete/          # 8 complete synthetic intakes
│   ├── incomplete/        # 8 incomplete / contradictory intakes
│   ├── edge-adversarial/  # 8 edge / adversarial intakes
│   └── holdout/           # 8 held-out intakes (not used during dev)
└── .github/         # CI config (future)
```

## Service scope

- **Service type**: Website delivery (initial)
- **Input format**: Plain text / structured paste — no attachments
- **Board**: Dedicated Trello demo board only

## Data notice

All fixtures are **synthetic or redacted**. No real client data is stored in this repository.
Real data handling, retention and deletion controls must be defined before any production use.

## Open decisions

- [ ] Trello board credentials and dedicated board setup
- [ ] Auth strategy for reviewer login
- [ ] Hosting and environment config
- [ ] Cost cap / token budget per intake
- [ ] Retention / deletion policy

---
*Started: 2026-09-20 | Owner review only — not a published product*