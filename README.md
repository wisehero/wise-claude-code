# wise-claude-code

개인 작업용 커스텀 스킬 모음. **Claude Code 용**과 **Codex 용**을 최상위 디렉토리로 분리해 관리한다.

한국어 중심. 새 스킬 추가·수정 시 반드시 각 플랫폼의 작업 규칙(`claude/CLAUDE.md` · `codex/AGENTS.md`)과 Anthropic 공식 스킬 가이드를 따른다.

> 📚 **Claude / Claude Code 한국어 학습 자료는 별도 레포로 분리되었습니다** → [wise-claude-docs](https://github.com/wisehero/wise-claude-docs) · [웹뷰](https://wisehero.github.io/wise-claude-docs/skill-guide/)

---

## 구성

```
.
├── claude/                       # Claude Code 용
│   ├── CLAUDE.md                 #   작업 규칙 (semver, 템플릿-지시문 일관성)
│   ├── agents/                   #   서브에이전트 (docs-researcher)
│   └── skills/                   #   커스텀 스킬 10개
├── codex/                        # Codex 용
│   ├── AGENTS.md                 #   작업 규칙
│   ├── agents/                   #   Codex 어댑터 (handoff.openai.yaml)
│   └── skills/                   #   동일 스킬 10개 사본
└── README.md
```

> 스킬 본문(`SKILL.md` · `references/`)은 양쪽이 **같은 내용을 중복 보유**한다. 플랫폼별로 갈리는 것은 작업 규칙(`CLAUDE.md`/`AGENTS.md`)과 에이전트 어댑터(`agents/`)뿐이다. 한쪽 스킬을 고치면 다른 쪽에도 반영한다.

---

## 스킬 카탈로그

경로는 Claude Code 기준(`claude/skills/`). Codex는 `codex/skills/`에 동일 사본이 있다.

### 코드 품질 분석

| 스킬 | 버전 | 용도 | 대상 스택 |
|---|---|---|---|
| [`code-analyzer`](claude/skills/code-analyzer/) | 1.4.2 | 구조 + 비즈니스 로직 분석 → mermaid 리포트 | 언어 무관 (PHP/Laravel, Java/Spring 힌트 내장) |
| [`refactor-advisor`](claude/skills/refactor-advisor/) | 1.3.0 | 리팩토링 대안 + 트레이드오프 제시 | Java / Spring |
| [`performance-profiler`](claude/skills/performance-profiler/) | 1.2.0 | 성능 병목 정적 분석 (N+1, 메모리 누수 등) | Java / Spring |
| [`junit-test-writer`](claude/skills/junit-test-writer/) | 1.2.0 | 단위/슬라이스/통합 테스트 작성 | JUnit 5 / Spring Boot |
| [`business-flow-analyzer`](claude/skills/business-flow-analyzer/) | 1.2.0 | API·기능 흐름을 Phase·트랜잭션·이벤트 경계로 분석 | 언어 무관 |

### 문서·글쓰기

| 스킬 | 버전 | 용도 |
|---|---|---|
| [`spec-writer`](claude/skills/spec-writer/) | 2.1.0 | 대화형 인터뷰로 IEEE 830 기반 SRS 작성 |
| [`claude-docs-reviewer`](claude/skills/claude-docs-reviewer/) | 1.2.0 | Claude/Claude Code 공식 문서와 대조하여 학습 자료 오류 검증 |
| [`external-context`](claude/skills/external-context/) | 1.2.0 | 외부 문서·레퍼런스 다면 리서치 |

### 학습 지원

| 스킬 | 버전 | 용도 |
|---|---|---|
| [`study-helper`](claude/skills/study-helper/) | 1.3.1 | 노션에 정리한 학습 노트 검수 (AI투 + 오타 + 팩트체크) |

### 협업·세션

| 스킬 | 버전 | 용도 |
|---|---|---|
| [`handoff`](claude/skills/handoff/) | 1.0.0 | 다음 세션·다른 에이전트가 이어가도록 한국어 핸드오프 작성 |

---

## 사용법

스킬은 이 레포에서 편집하고, 각 도구가 감지하도록 홈 디렉토리에 연결한다.

```bash
# Claude Code — claude/skills 를 ~/.claude/skills 에 심볼릭 링크 (권장)
ln -s "$(pwd)/claude/skills" ~/.claude/skills

# 또는 특정 스킬만
ln -s "$(pwd)/claude/skills/code-analyzer" ~/.claude/skills/code-analyzer

# Codex — codex/skills 를 사용 (어댑터는 codex/agents/)
```

트리거 문구는 각 스킬의 `SKILL.md` frontmatter `description`에 정의되어 있다. 예를 들어 `code-analyzer`는 "코드 분석해줘", "구조 파악해줘", "이 프로젝트 어떻게 되어있어?" 같은 요청에 발동한다.

---

## 스킬 작성·수정 규칙

새 스킬을 만들거나 기존 스킬을 수정할 때 반드시 지킨다 (상세는 [`claude/CLAUDE.md`](claude/CLAUDE.md) · [`codex/AGENTS.md`](codex/AGENTS.md)):

1. **공식 스킬 가이드** — Anthropic 공식 가이드를 먼저 읽는다. `When to Use / When NOT to Use` 섹션 필수, `description`에 trigger 문구 명시, progressive disclosure(`SKILL.md` 본문은 핵심만, 상세는 `references/`)
2. **Semver 버전 관리** — 모든 `SKILL.md`에 `version` 필드. patch(오타) / minor(지시문·기능 개선) / major(목적·워크플로우 변경)
3. **템플릿-지시문 일관성** — `references/`에 템플릿이 있으면 `SKILL.md` 지시문에도 동일한 형식(섹션 순서·컬럼·필수 항목)을 **인라인으로** 명시한다. "템플릿 참조하라"는 한 줄로 끝내면 산출물 품질이 흔들린다.
4. **Claude/Codex 동기화** — 한쪽 스킬 본문을 고치면 다른 쪽 사본에도 동일하게 반영한다.
