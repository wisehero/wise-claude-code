# wise-claude-code

개인 작업용 커스텀 스킬 모음. **Claude Code 용**과 **Codex 용**을 최상위 디렉토리로 분리해 관리한다.

한국어 중심. 새 스킬 추가·수정 시 반드시 각 플랫폼의 작업 규칙(루트 `CLAUDE.md` · `AGENTS.md`)과 Anthropic 공식 스킬 가이드를 따른다.

> 📚 **Claude / Claude Code 한국어 학습 자료는 별도 레포로 분리되었습니다** → [wise-claude-docs](https://github.com/wisehero/wise-claude-docs) · [웹뷰](https://wisehero.github.io/wise-claude-docs/skill-guide/)

---

## 구성

```
.
├── CLAUDE.md                     # Claude Code 작업 규칙 — 루트에 두어 Claude Code가 레포 전역에서 자동 로드
├── AGENTS.md                     # Codex 작업 규칙 — 루트에 두어 Codex가 레포 전역에서 자동 로드
├── claude/                       # Claude Code 용
│   ├── agents/                   #   서브에이전트 (docs-researcher)
│   └── skills/                   #   커스텀 스킬 10개
├── codex/                        # Codex 용
│   ├── agents/                   #   Codex 어댑터 (handoff.openai.yaml)
│   └── skills/                   #   동일 스킬 10개 사본 (Codex 환경에 맞게 일부 적응)
└── README.md
```

> 작업 규칙은 도구가 자동 인식하도록 루트에 둔다 — Claude Code는 `CLAUDE.md`, Codex는 `AGENTS.md`. 스킬은 같은 목적과 산출물 형식을 공유하되, Codex 사본은 실행 환경(`rg`/`sed`, Codex 파일 편집, context7/웹 검색 등)에 맞게 별도 적응될 수 있다. 한쪽 스킬의 정책·템플릿·참조 자료를 바꾸면 다른 쪽에도 의도적으로 반영하거나, 플랫폼 차이로 분기한 이유를 남긴다.

---

## 스킬 카탈로그

경로는 Claude Code 기준(`claude/skills/`)과 Codex 기준(`codex/skills/`)을 함께 관리한다.

### 코드 품질 분석

| 스킬 | Claude | Codex | 용도 | 대상 스택 |
|---|---:|---:|---|---|
| [`code-analyzer`](claude/skills/code-analyzer/) / [`codex`](codex/skills/code-analyzer/) | 1.5.0 | 1.5.0 | 구조 + 비즈니스 로직 분석 → mermaid 리포트 | 언어 무관 (PHP/Laravel, Java/Spring 힌트 내장) |
| [`refactor-advisor`](claude/skills/refactor-advisor/) / [`codex`](codex/skills/refactor-advisor/) | 1.3.0 | 1.3.1 | 리팩토링 대안 + 트레이드오프 제시 | Java / Spring |
| [`performance-profiler`](claude/skills/performance-profiler/) / [`codex`](codex/skills/performance-profiler/) | 1.2.0 | 1.2.1 | 성능 병목 정적 분석 (N+1, 메모리 누수 등) | Java / Spring |
| [`junit-test-writer`](claude/skills/junit-test-writer/) / [`codex`](codex/skills/junit-test-writer/) | 1.2.0 | 1.2.1 | 단위/슬라이스/통합 테스트 작성 | JUnit 5 / Spring Boot |
| [`business-flow-analyzer`](claude/skills/business-flow-analyzer/) / [`codex`](codex/skills/business-flow-analyzer/) | 1.2.0 | 1.2.0 | API·기능 흐름을 Phase·트랜잭션·이벤트 경계로 분석 | 언어 무관 |

### 문서·글쓰기

| 스킬 | Claude | Codex | 용도 |
|---|---:|---:|---|
| [`spec-writer`](claude/skills/spec-writer/) / [`codex`](codex/skills/spec-writer/) | 2.1.0 | 2.1.1 | 대화형 인터뷰로 IEEE 830 기반 SRS 작성 |
| [`claude-docs-reviewer`](claude/skills/claude-docs-reviewer/) / [`codex`](codex/skills/claude-docs-reviewer/) | 1.2.0 | 1.2.1 | Claude/Claude Code 공식 문서와 대조하여 학습 자료 오류 검증 |
| [`external-context`](claude/skills/external-context/) / [`codex`](codex/skills/external-context/) | 1.2.0 | 1.3.0 | 외부 문서·레퍼런스 다면 리서치 |

### 학습 지원

| 스킬 | Claude | Codex | 용도 |
|---|---:|---:|---|
| [`study-helper`](claude/skills/study-helper/) / [`codex`](codex/skills/study-helper/) | 1.3.1 | 1.4.0 | 노션에 정리한 학습 노트 검수 (AI투 + 오타 + 팩트체크) |

### 협업·세션

| 스킬 | Claude | Codex | 용도 |
|---|---:|---:|---|
| [`handoff`](claude/skills/handoff/) / [`codex`](codex/skills/handoff/) | 1.1.0 | 1.1.1 | 다음 세션·다른 에이전트가 이어가도록 한국어 핸드오프 작성 |

---

## 사용법

스킬은 이 레포에서 편집하고, 각 도구가 감지하도록 홈 디렉토리에 연결한다.

`~/.claude/skills`·`~/.codex/skills`에는 이 레포와 무관한 스킬(예: `agent-browser`)이 함께 있을 수 있으므로 디렉토리를 통째로 링크하지 말고 **스킬별 심볼릭 링크**로 연결한다.

```bash
# Claude Code — claude/skills/* 를 스킬별로 심볼릭 링크
mkdir -p ~/.claude/skills
for skill in "$(pwd)"/claude/skills/*; do ln -sfn "$skill" ~/.claude/skills/"$(basename "$skill")"; done

# Codex — codex/skills/* 를 스킬별로 심볼릭 링크 (어댑터는 codex/agents/)
mkdir -p ~/.codex/skills
for skill in "$(pwd)"/codex/skills/*; do ln -sfn "$skill" ~/.codex/skills/"$(basename "$skill")"; done
```

> 스킬을 추가·삭제하면 위 루프를 다시 돌려 링크를 갱신하고, 삭제된 스킬의 깨진 링크는 직접 제거한다.

트리거 문구는 각 스킬의 `SKILL.md` frontmatter `description`에 정의되어 있다. 예를 들어 `code-analyzer`는 "코드 분석해줘", "구조 파악해줘", "이 프로젝트 어떻게 되어있어?" 같은 요청에 발동한다.

---

## 스킬 작성·수정 규칙

새 스킬을 만들거나 기존 스킬을 수정할 때 반드시 지킨다 (상세는 [`CLAUDE.md`](CLAUDE.md) · [`AGENTS.md`](AGENTS.md)):

1. **공식 스킬 가이드** — Anthropic 공식 가이드를 먼저 읽는다. `When to Use / When NOT to Use` 섹션 필수, `description`에 trigger 문구 명시, progressive disclosure(`SKILL.md` 본문은 핵심만, 상세는 `references/`)
2. **Semver 버전 관리** — Claude 스킬은 top-level `version`, Codex 스킬은 `metadata.version`. patch(오타) / minor(지시문·기능 개선) / major(목적·워크플로우 변경)
3. **템플릿-지시문 일관성** — `references/`에 템플릿이 있으면 `SKILL.md` 지시문에도 동일한 형식(섹션 순서·컬럼·필수 항목)을 **인라인으로** 명시한다. "템플릿 참조하라"는 한 줄로 끝내면 산출물 품질이 흔들린다.
4. **Claude/Codex 의도 동기화** — 정책·템플릿·참조 자료 변경은 양쪽에 반영한다. 단, 도구 호출 방식과 실행 환경은 플랫폼별로 분기할 수 있다.
