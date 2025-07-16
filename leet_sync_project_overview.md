# LeetSync Intelligence Platform – Engineering Blueprint

> **Purpose**\
> A concise, share‑ready document that Claude Code CLI (and any new contributor) can read once and understand how to navigate the repo, run tests, deploy stacks, and extend features – while highlighting the “industrial‑grade” skills the project showcases.

---

## 1  Mission Statement

**LeetSync** is a serverless, multi‑tenant backend that ingests public LeetCode data hourly, materialises daily analytics, and serves AI‑driven problem recommendations — all for < \$5 USD / 100 users / month.

- Key interview‑worthy skills demonstrated:
  - AWS CDK IaC, GitHub Actions CI/CD, OIDC roles
  - Event‑driven ingestion, idempotent writes, DynamoDB data‑modelling
  - Data‑lake partitioning (Parquet) + Athena SQL roll‑ups
  - Retrieval‑Augmented Generation (RAG) micro‑service with LLMs
  - FinOps cost dashboards & circuit‑breaker resilience patterns

---

## 2  High‑Level Architecture

```
                 ┌────────── GitHub Actions (CI/CD) ──────────┐
    git push ▶   │  mvn test   localstack integ   cdk deploy  │
                 └────────────────────────────────────────────┘
                         ↓ (OIDC IAM role)
┌────────────────────────────────────────────────────────────────┐
│                  AWS ACCOUNT : leetsync‑prod                  │
│                                                                │
│  ┌── EventBridge (cron) ─► ingestion‑λ (hourly) ─┐             │
│  │                                              │             │
│  │   PUT PK=USER#u SK=SUB#id                    │             │
│  ▼                                              ▼             │
│ DynamoDB Submissions ◄── Streams ─┐          DynamoDB Insights │
│                                   │                           │
│                                   ▼                           │
│                           Firehose → S3 events/ (parquet)     │
│                                                                │
│  ┌── EventBridge (Mon 05:00) ─► catalog‑λ  → S3 catalog/       │
│  │                                                           │
│  └── EventBridge (01:30) ─► stats‑λ (joins events + catalog)──┘
│                                                                │
│ API Gateway HTTP API ─► api‑λ (Spring Boot)  ─┬─► /stats/*     │
│                                              └─► aiassist‑λ  ─┬► /recommend
│                                                                │
│  OpenAI / Bedrock LLM ◄───────────────────────────────────────┘
└────────────────────────────────────────────────────────────────┘
```

### 2.1  AWS Resources & Tags

| Id   | Resource                  | Purpose                          | Key Tags                     |
| ---- | ------------------------- | -------------------------------- | ---------------------------- |
| DB1  | **DynamoDB Submissions**  | Hourly accepted‑submission store | `stack=leetsync`,`tier=data` |
| DB2  | **DynamoDB Insights**     | Daily + summary metrics          | `stack=leetsync`,`tier=data` |
| S3‑C | `leetsync‑catalog‑bucket` | Weekly problem catalogue         | `lifecycle=365d-ia`          |
| S3‑E | `leetsync‑events‑bucket`  | Raw event log via Firehose       | `lifecycle=425d-glacier`     |
| LH1  | `ingestion‑function`      | Polls GraphQL                    | memory 256 MB                |
| LH2  | `stats‑function`          | Nightly roll‑up                  | memory 512 MB                |
| LH3  | `api‑function`            | Spring Boot 3                    | memory 1024 MB, ARM64        |
| LH4  | `aiassist‑function`       | RAG + cache                      | memory 512 MB                |

---

## 3  Repository Structure

```
leetsync/
├─ .github/workflows/   # ci.yml  cd.yml
├─ infra-cdk/           # CDK app (TypeScript)
│   └─ bin/app.ts       # entrypoint
├─ shared-model/        # POJOs / utils (maven)
├─ ingestion-lambda/
├─ stats-lambda/
├─ aiassist-lambda/
├─ api-service/         # Spring Boot (lambda runtime)
└─ README.md            # project landing page
```

---

## 4  CI/CD Pipeline (GitHub Actions)

### 4.1  `ci.yml` (pull‑request)

- Lint → `mvn test` → LocalStack integration → Jacoco coverage gate ≥ 80 %.

### 4.2  `cd.yml` (push main)

- OIDC → assume `LeetSyncCIRole`.
- `cdk synth` – verify no drift warnings.
- `cdk deploy --require-approval never` – Blue/Green alias for API λ.
- Post‑deploy step: run `integration/e2e.sh` hitting `/stats/summary`.

### 4.3  Cost Guardrail Step

- Parse `cdk diff` for `POTENTIAL COST` lines – fail build if monthly Δ > \$2.

---

## 5  Data Model

### 5.1  Submissions Table (DB1)

| PK            | SK         | Attributes             |
| ------------- | ---------- | ---------------------- |
| `USER#<name>` | `SUB#<id>` | `problemSlug`, `epoch` |

- **Idempotent write:** conditional expression `attribute_not_exists(SK)`.

### 5.2  Insights Table (DB2)

| SK prefix           | TTL  | Fields                                                                    |
| ------------------- | ---- | ------------------------------------------------------------------------- |
| `DAY#yyyy-mm-dd`    | 90 d | `solvedTotal`, `byDiff`, `topics[]`, `streak`                             |
| `META#SUMMARY`      | ∞    | `totalSolved`, `weakTopics[3]`, `hardPct`, `longestStreak`, `catalogWeek` |
| `REC#<catalogHash>` | 48 h | cached LLM recommendations                                                |

---

## 6  Distributed‑Systems Safeguards

1. **At‑least‑once delivery** – Streams duplicates handled by idempotent condition writes.
2. **Back‑pressure** – 429 from LeetCode ➜ username pushed to SQS DLQ → retried with exponential back‑off.
3. **Circuit breaker** – resilience4j in aiassist‑λ opens after 3 consecutive LLM failures.
4. **Event replay** – S3 events parquet is canonical; stats‑λ can rebuild yesterday if it crashed.

---

## 7  Observability & FinOps

- **OpenTelemetry** layer auto‑publishes traces → AWS X‑Ray.
- Custom CloudWatch metrics:
  - `LLMTokens`, `AthenaBytesScanned`, `PollerErrors`, `ApproxCostUSD`.
- Alarm thresholds (SNS → email/Slack):
  -
    >  25 k tokens/day
  - Athena > 50 MB/day
  - Lambda error % > 5 %.

---

## 8  Roadmap Milestones

| Sprint | Deliverable                                   |
| ------ | --------------------------------------------- |
|  0     | Repo scaffolding + CDK bootstrap              |
|  1     | Submissions table + ingestion‑λ + hourly cron |
|  2     | Weekly catalog‑λ + S3 upload (JSON → Parquet) |
|  3     | stats‑λ joins + Insights TTL rows             |
|  4     | API λ (`/stats` endpoints)                    |
|  5     | aiassist‑λ + Bedrock integration, cache layer |
|  6     | OpenTelemetry, cost dashboards, README polish |

---

## 9  Required Environment Variables

| Function    | Variable            | Example   |
| ----------- | ------------------- | --------- |
| ingestion‑λ | `POLL_INTERVAL_MIN` | `60`      |
| all         | `LOG_LEVEL`         | `INFO`    |
| aiassist‑λ  | `LLM_PROVIDER`      | `bedrock` |

---

## 10  Local Dev Tips

- `sam local start-lambda` + `aws lambda invoke` for quick handler runs.
- `npm start --prefix infra-cdk` launches `cdk watch` for hot‑reload deployments.
- VSCode tasks `./.vscode/tasks.json` wired to `mvn -pl :api-service spring-boot:run`.

---

*End of Blueprint*

