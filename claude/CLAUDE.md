# wise-claude-code

## Project
- Claude Code 커스텀 스킬 레포지토리
- 주 언어: 한국어 (커밋 메시지, 스킬 내용, 소통)
- 스킬 위치: `skills/` 디렉토리

## Skill 작성·수정·개선 규칙

스킬을 작성, 수정, 검토할 때는 반드시 Anthropic 공식 가이드 — <https://resources.anthropic.com/hubfs/The-Complete-Guide-to-Building-Skill-for-Claude.pdf> — 를 먼저 읽고 해당 가이드의 규칙을 따른다. WebFetch 도구로 가져올 수 있다.

### 버전 관리 규칙

모든 스킬은 SKILL.md frontmatter에 `version` 필드를 가진다. 스킬의 내용(SKILL.md, references/ 등)이 변경되면 반드시 버전을 올린다.

- **patch** (1.0.0 → 1.0.1): 오타 수정, 문구 다듬기 등 동작에 영향 없는 변경
- **minor** (1.0.0 → 1.1.0): 기능 추가, 템플릿 구조 변경, 지시문 개선 등 동작에 영향 있는 변경
- **major** (1.0.0 → 2.0.0): 스킬의 목적이나 워크플로우가 근본적으로 바뀌는 변경

### 템플릿-지시문 일관성 규칙

스킬이 `references/` 디렉토리에 템플릿 파일을 가지고 있을 경우, SKILL.md의 지시문과 템플릿의 형식이 **정확히 일치**해야 한다.

- SKILL.md에서 산출물 형식을 설명할 때는 템플릿의 구조(섹션 순서, 테이블 컬럼, 항목 배치)를 그대로 인라인으로 보여준다.
- "템플릿을 참조하라"는 지시만으로는 부족하다 — 반드시 SKILL.md 안에 구체적인 형식을 함께 명시하여 LLM이 헷갈리지 않도록 한다.
- 템플릿을 수정하면 SKILL.md의 관련 지시문도 반드시 함께 수정한다. 둘이 어긋나면 산출물의 통일성이 깨진다.
