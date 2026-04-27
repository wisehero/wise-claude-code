---
description: 코드베이스 내부 사실 확인성 질문을 Claude와 Codex CLI에게 독립 리서치시킨 뒤 4축 루브릭(출처/인용/논리/반증)으로 채점·수렴하는 cross-check 모드
argument-hint: <리서치 질문>
allowed-tools: Bash(codex exec:*), Read, Grep, Glob
---

# Parallax — Cross-Check 리서치 모드

`skills/parallax` 스킬의 명시적 호출 경로다. auto-trigger가 발화하지 않을 때 이 커맨드로 강제 호출한다. 워크플로 본문은 `skills/parallax/SKILL.md`의 Step 1~7을 그대로 따른다 — 이 파일은 진입점이고, 실제 절차·루브릭·리포트 형식은 스킬의 SKILL.md / references/rubric.md / references/report-template.md가 가진다.

## 리서치 질문
$ARGUMENTS

## 수행 절차

1. **스킬 본문 로드** — `skills/parallax/SKILL.md`를 Read한다. Step 1~7과 컨벤션 요약 표를 그대로 따른다.
2. **Step 1 (질문 범위 확정)** 실행 — 코드베이스 내부 사실 확인성 질문이 아니면 거절하고 적절한 도구를 안내한다 (예: 외부 지식 → 다른 도구, 수행 작업 리뷰 → `/codex-review`, 전반 구조 분석 → `code-analyzer`).
3. **Step 2~3** — Claude 자체 리서치 + Codex CLI 호출을 독립적으로 진행. Codex 호출 직전에 사용자에게 "Codex 호출 중 — 30~60초 소요" 안내 1줄.
4. **Step 4~6** — diff·인용 검증 → 4축 채점 → 15% 임계값으로 수렴/병기 판정. 채점 기준은 `skills/parallax/references/rubric.md`.
5. **Step 7** — 리포트 출력. 형식은 `skills/parallax/references/report-template.md`와 SKILL.md Step 7의 인라인 명세에 정확히 일치시킨다.

## 주의

- 이 커맨드는 파일을 수정하지 않는다. 리서치·답변만.
- Codex 호출 실패 시 SKILL.md Step 3의 fallback(Claude 단독 답변 + 실패 사유 명시) 그대로 따른다.
- 질문이 모호하면 SKILL.md Step 1의 단일 역질문 패턴(역질문 2회 연속 금지)을 따른다.
