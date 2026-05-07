# wise-claude-code

개인 작업용 Claude Code 커스텀 스킬 모음.

한국어 중심. 새 스킬 추가·수정 시 반드시 루트의 `The-Complete-Guide-to-Building-Skill-for-Claude.pdf`와 `CLAUDE.md`의 규칙을 따른다.

> 📚 **Claude / Claude Code 한국어 학습 자료는 별도 레포로 분리되었습니다** → [wise-claude-docs](https://github.com/wisehero/wise-claude-docs) · [웹뷰](https://wisehero.github.io/wise-claude-docs/skill-guide/)

---

## 구성

```
.
├── skills/                                        # 커스텀 스킬 10개
├── CLAUDE.md                                      # 이 레포 작업 규칙 (semver, 템플릿-지시문 일관성)
└── The-Complete-Guide-to-Building-Skill-for-Claude.pdf   # 앤트로픽 공식 스킬 가이드
```

---

## 스킬 카탈로그

### 코드 품질 분석

| 스킬 | 버전 | 용도 | 대상 스택 |
|---|---|---|---|
| [`code-analyzer`](skills/code-analyzer/) | 1.4.2 | 구조 + 비즈니스 로직 분석 → mermaid 리포트 | 언어 무관 (PHP/Laravel, Java/Spring 힌트 내장) |
| [`refactor-advisor`](skills/refactor-advisor/) | 1.3.0 | 리팩토링 대안 + 트레이드오프 제시 | Java / Spring |
| [`performance-profiler`](skills/performance-profiler/) | 1.2.0 | 성능 병목 정적 분석 (N+1, 메모리 누수 등) | Java / Spring |
| [`junit-test-writer`](skills/junit-test-writer/) | 1.2.0 | 단위/슬라이스/통합 테스트 작성 | JUnit 5 / Spring Boot |
| [`parallax`](skills/parallax/) | 1.1.0 | 코드베이스 내부 사실 확인성 질문 교차 검증 | 현재 working directory 내부 |

### 문서·글쓰기

| 스킬 | 버전 | 용도 |
|---|---|---|
| [`spec-writer`](skills/spec-writer/) | 1.4.0 | 대화형 인터뷰로 IEEE 830 기반 SRS 작성 |
| [`humanizer-korean`](skills/humanizer-korean/) | 3.2.0 | 한국어 AI투 제거, 자연스러운 문체로 교정 |
| [`claude-docs-reviewer`](skills/claude-docs-reviewer/) | 1.2.0 | Claude/Claude Code 공식 문서와 대조하여 학습 자료 오류 검증 |
| [`external-context`](skills/external-context/) | 1.2.0 | 외부 문서·레퍼런스 다면 리서치 |

### 학습 지원

| 스킬 | 버전 | 용도 |
|---|---|---|
| [`study-helper`](skills/study-helper/) | 1.3.1 | 노션에 정리한 학습 노트 검수 (AI투 + 오타 + 팩트체크) |

---

## 사용법

스킬은 이 레포에서 편집하고, Claude Code가 감지하도록 `~/.claude/skills/`에 연결한다.

```bash
# 전체 skills/ 디렉토리를 심볼릭 링크 (권장)
ln -s "$(pwd)/skills" ~/.claude/skills

# 또는 특정 스킬만
ln -s "$(pwd)/skills/code-analyzer" ~/.claude/skills/code-analyzer
```

트리거 문구는 각 스킬의 `SKILL.md` frontmatter `description`에 정의되어 있다. 예를 들어 `code-analyzer`는 "코드 분석해줘", "구조 파악해줘", "이 프로젝트 어떻게 되어있어?" 같은 요청에 발동한다.

---

## 스킬 작성·수정 규칙

새 스킬을 만들거나 기존 스킬을 수정할 때 반드시 지킨다 (상세는 [`CLAUDE.md`](CLAUDE.md)):

1. **앤트로픽 공식 가이드** (루트 PDF) — `When to Use / When NOT to Use` 섹션 필수, `description`에 trigger 문구 명시, progressive disclosure(`SKILL.md` 본문은 핵심만, 상세는 `references/`)
2. **Semver 버전 관리** — 모든 `SKILL.md`에 `version` 필드. patch(오타) / minor(지시문·기능 개선) / major(목적·워크플로우 변경)
3. **템플릿-지시문 일관성** — `references/`에 템플릿이 있으면 `SKILL.md` 지시문에도 동일한 형식(섹션 순서·컬럼·필수 항목)을 **인라인으로** 명시한다. "템플릿 참조하라"는 한 줄로 끝내면 산출물 품질이 흔들린다.
