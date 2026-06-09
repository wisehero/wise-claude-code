---
name: business-flow-analyzer
description: Use when the user asks to analyze an API, feature flow, business logic, or "what happens when this is called"; produce a Korean business-policy style flow report with Phase numbering, physical transaction boundaries, event publish/listener boundaries, and concise code references.
metadata:
  version: 1.2.0
---

# Business Flow Analyzer

Analyze a feature or API from entrypoint to externally visible business result. Prefer a business-policy/process-document tone over a low-level implementation trace.

## When To Use

Use this skill when the user asks questions like:

- "이 API 호출하면 어떤 결과가 나타나?"
- "회원가입 로직 따라가보자"
- "이 기능 흐름 분석해줘"
- "비즈니스 정책서처럼 정리해줘"
- "트랜잭션/이벤트 경계까지 보고 싶어"

For code exploration, combine this with normal repository search/reading or an existing code-analysis skill. This skill mainly controls how the analysis is structured and written.

## Output Style

Write in Korean by default.

Use this structure. Transaction **start** and **commit** are separate boundary lines, with `BEFORE_COMMIT` listeners placed inside the transaction and `AFTER_COMMIT` listeners after the commit line:

```text
[NO-TX] ─────────────────────────────────────

Phase n. 업무 단계명
  n-1. 비즈니스 정책/행위 설명.
       -> FileName.java:line

[TX-1 BEGIN] ────────────────────────────────

Phase n+1. 업무 단계명
  (n+1)-1. 비즈니스 정책/행위 설명.
           -> FileName.java:line

[EVENT PUBLISH: SomethingEvent]
  -> FileName.java:line

[EVENT LISTENER: SomethingEvent / BEFORE_COMMIT]
  설명.
  -> ListenerName.java:line

[TX-1 COMMIT] ───────────────────────────────

[EVENT LISTENER: SomethingEvent / AFTER_COMMIT]
  설명.
  -> ListenerName.java:line
```

## Rules

- Use `Phase 1.`, `Phase 2.`, etc. for major business steps.
- Use `1-1.`, `1-2.`, etc. for substeps.
- Indent substeps by 2 spaces under each `Phase`.
- Indent supporting code references one additional level (2 more spaces) under the substep, visually aligned with the substep body text.
- Do not show full absolute file paths.
- Prefer short references like `CartController.java:75`.
- Use relative paths only when filenames are ambiguous, e.g. `adapter/in/web/api/CartController.java:75`.
- Keep phase text business-oriented and policy-like.
- Phrase each phase as "what business result/policy happens" first, then add code references as supporting evidence.
- Put implementation caveats, storage details, and technical observations in `Implementation Notes` rather than the main phase flow.
- Mark transaction **start** and **commit** as separate boundary lines: `[TX-n BEGIN]` where `@Transactional` opens, `[TX-n COMMIT]` where the method returns successfully and the physical commit fires. Sections outside any transaction use `[NO-TX]`.
- If the transaction uses non-default propagation (`REQUIRES_NEW`, `NESTED`, `MANDATORY`, etc.) or `readOnly = true`, annotate it inline: `[TX-2 BEGIN (REQUIRES_NEW)]`. If a rollback rule is non-default (`rollbackFor`, `noRollbackFor`), note it on the BEGIN line.
- Place `BEFORE_COMMIT` listeners **inside** the transaction block (between BEGIN and COMMIT). They run before the physical commit and a failure here will roll the transaction back.
- Place `AFTER_COMMIT` and `AFTER_ROLLBACK` listeners **after** the COMMIT line. They run on a separate logical phase after the commit has been flushed.
- For `@Async` listeners, append `/ ASYNC` to the listener label, e.g. `[EVENT LISTENER: WelcomeMailEvent / AFTER_COMMIT / ASYNC]`, since they execute outside the publisher's thread and outside any caller transaction.
- Mark event publishing sections explicitly, e.g. `[EVENT PUBLISH: MemberRegisteredEvent]`, and place them where `publishEvent` is actually called in the flow.
- Mark event listener sections explicitly with their commit phase, e.g. `[EVENT LISTENER: EmailVerifiedEvent / BEFORE_COMMIT]`.
- Preserve actual class, method, event, DTO, and domain names.

## Workflow

1. Find the API or feature entrypoint.
2. Trace Controller -> UseCase/Service -> Domain -> Repository/Adapter.
3. Identify externally visible results: response body, status, cookie/session effects, DB-observable outcome, and user-facing side effects.
4. Identify physical transaction boundaries from `@Transactional`: where the transaction **begins** (method entry that opens it), where it **commits** (successful return of the outermost transactional method), and any non-default `propagation` / `isolation` / `readOnly` / `rollbackFor` settings.
5. Identify event publishing call sites and event listeners. For each listener, classify it as `BEFORE_COMMIT` / `AFTER_COMMIT` / `AFTER_ROLLBACK` / no phase (synchronous publish), and note `@Async` separately.
6. Produce the main report in business-policy style.
7. Add `Implementation Notes` only for caveats or important technical details that should not dominate the policy flow.

## Good Tone

Prefer:

```text
Phase 2. 기존 장바구니 연속성 보장
  2-1. 고객의 쿠키에 장바구니 식별자가 있으면 해당 장바구니를 계속 사용한다.
       -> CreateGuestCartService.java:35
```

Avoid:

```text
Phase 2. Service method branch execution
  2-1. if cookieCartId != null && !cookieCartId.isBlank() executes and returns DTO.
```
