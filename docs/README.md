# Claude & Claude Code 활용 가이드

이 가이드는 Claude와 Claude Code를 실전에서 효과적으로 활용하기 위한 공식 문서 기반 학습 자료입니다. Anthropic 공식 문서를 바탕으로 정리했으며, 처음 시작하는 사람부터 이미 사용 중인 사람까지 모두를 대상으로 합니다.

---

## 이 가이드를 읽기 전에

Claude는 AI 모델이고, Claude Code는 터미널에서 실행하는 CLI 도구입니다. 둘은 별개이지만 함께 쓸 때 가장 강력합니다. 이 가이드는 두 도구를 모두 다루며, Part 1(Claude)과 Part 2(Claude Code)로 나뉩니다.

---

## 학습 경로

### 경로 A: 처음 시작하는 분

Claude Code를 처음 설치하거나, 프롬프트 작성 경험이 없다면 아래 순서대로 읽을 것을 권장합니다.

| 순서 | 문서 | 이유 |
|------|------|------|
| 1 | [Claude Code 시작하기](claude-code/01-getting-started.md) | 설치와 기본 사용법부터 |
| 2 | [프롬프트 기초](claude/01-prompt-basics.md) | Claude에게 잘 말하는 법 |
| 3 | [CLAUDE.md 작성 가이드](claude-code/02-claude-md.md) | 프로젝트에 Claude 길들이기 |
| 4 | [모델 선택 가이드](claude/04-model-guide.md) | 상황에 맞는 모델 고르기 |
| 5 | [단축키와 슬래시 명령어](claude-code/07-shortcuts-and-commands.md) | 작업 속도 높이기 |
| 6 | 나머지 | 필요한 주제 선택 |

### 경로 B: 이미 사용 중인 분

기본은 알고 있고 특정 주제를 더 깊이 파고 싶다면, 아래 전체 문서 목록에서 관심 있는 항목을 직접 선택하세요.

---

## 전체 문서 목록

### Part 1: Claude 활용

| 문서 | 난이도 | 설명 |
|------|--------|------|
| [프롬프트 기초](claude/01-prompt-basics.md) | 기초 | 명확한 지시, 역할 부여, 구조화된 출력 |
| [고급 프롬프팅 기법](claude/02-advanced-prompting.md) | 중급 | CoT, Few-shot, XML 태그, 프롬프트 체이닝 |
| [시스템 프롬프트 설계](claude/03-system-prompt-design.md) | 중급 | 설계 원칙, 모범 사례, 안티패턴 |
| [모델 선택 가이드](claude/04-model-guide.md) | 기초 | Opus/Sonnet/Haiku 비교, 상황별 선택 기준 |
| [확장 사고](claude/05-extended-thinking.md) | 심화 | 확장 사고 모드 활용, budget_tokens 설정 |

### Part 2: Claude Code 활용

| 문서 | 난이도 | 설명 |
|------|--------|------|
| [시작하기](claude-code/01-getting-started.md) | 기초 | 설치, 인증, 기본 명령어, 권한 모드 |
| [CLAUDE.md 작성 가이드](claude-code/02-claude-md.md) | 기초~중급 | 계층 구조, 작성법, .claude/rules/ 활용 |
| [커스텀 스킬](claude-code/03-custom-skills.md) | 중급 | SKILL.md 구조, 트리거 키워드, 버전 관리 |
| [훅 시스템](claude-code/04-hooks.md) | 심화 | 12가지 이벤트, 4가지 타입, 활용 패턴 |
| [MCP 서버](claude-code/05-mcp-servers.md) | 중급~심화 | MCP 개념, 설정 방법, 주요 서버 소개 |
| [실전 워크플로우](claude-code/06-workflow.md) | 중급~심화 | Git 연동, 멀티에이전트, 컨텍스트 관리 |
| [단축키와 슬래시 명령어](claude-code/07-shortcuts-and-commands.md) | 기초 | 키보드 단축키, 슬래시 명령어 전체 레퍼런스 |

### 부록

| 문서 | 설명 |
|------|------|
| [트러블슈팅](appendix/troubleshooting.md) | 자주 겪는 문제와 해결법 |

---

## 이 프로젝트의 커스텀 스킬

`skills/` 디렉토리에는 실전에서 사용 중인 커스텀 스킬들이 있습니다. 각 스킬은 `SKILL.md`와 필요한 경우 `references/` 디렉토리로 구성됩니다.

| 스킬 | 설명 |
|------|------|
| `code-analyzer` | 코드베이스 구조와 비즈니스 로직을 분석하여 Mermaid 다이어그램 포함 리포트 생성 |
| `claude-docs-reviewer` | 가이드 문서를 Anthropic 공식 문서와 대조하여 최신성과 정확성을 검증하고 검수 리포트 생성 |
| `humanizer` | AI가 생성한 텍스트에서 AI 냄새를 제거하고 자연스러운 사람 문체로 교정 |
| `junit-test-writer` | JUnit 5 기반 단위 테스트, 슬라이스 테스트, 통합 테스트 코드 작성 |
| `performance-profiler` | Java/Spring 코드의 N+1 쿼리, 알고리즘 비효율, 메모리 낭비 등 성능 병목 분석 |
| `refactor-advisor` | 코드 스멜, SOLID 위반, 디자인 패턴 적용 기회를 식별하고 리팩토링 방향 제시 |
| `spec-writer` | 대화형 인터뷰를 통해 IEEE 830 표준 기반 시스템 요구사항 명세서(SRS) 작성 |
| `study-helper` | 기술 학습 노트의 AI 냄새 제거, 오타 수정, 팩트체크 수행 후 검수 리포트 제공 |

커스텀 스킬을 직접 만들고 싶다면 [커스텀 스킬 문서](claude-code/03-custom-skills.md)와 함께 `skills/` 디렉토리의 실제 스킬 파일들을 참고하면 좋습니다.

---

## 참고 자료

### Anthropic 공식 문서

- [Anthropic Docs](https://docs.anthropic.com) - API, 모델, 프롬프팅 전반
- [Anthropic Prompt Library](https://docs.anthropic.com/en/prompt-library/library) - 공식 프롬프트 예시 모음
- [Claude Models Overview](https://docs.anthropic.com/en/docs/about-claude/models/overview) - 최신 모델 목록과 스펙

### Claude Code 공식 문서

- [Claude Code Overview](https://docs.anthropic.com/en/docs/claude-code/overview) - Claude Code 소개
- [Claude Code Quickstart](https://docs.anthropic.com/en/docs/claude-code/quickstart) - 빠른 시작 가이드
- [Claude Code GitHub](https://github.com/anthropics/claude-code) - 소스 및 릴리스 노트
