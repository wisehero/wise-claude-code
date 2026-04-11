---
name: external-context
version: 1.1.0
description: 쿼리를 2-5개 facet으로 분해한 뒤 병렬 docs-researcher 서브에이전트를 호출하여 외부 문서·레퍼런스를 통합 수집하는 스킬. "공식 문서 찾아줘", "~에 대해 조사해줘", "베스트 프랙티스 찾아줘", "버전별 차이 알려줘", "~랑 ~ 비교해줘" 같은 요청에 사용. 팩트체크, 라이브러리 선택, 패턴 리서치, 버전 호환성 확인 등에 활용한다.
argument-hint: <검색 쿼리 또는 주제>
---
<!-- level: 4 (내부 복잡도 티어 메타, 런타임 키 아님) -->


# External Context

외부 문서·레퍼런스·컨텍스트를 수집하는 스킬. 쿼리를 2-5개의 독립적인 facet으로 분해하고, 각 facet을 `docs-researcher` 서브에이전트에게 병렬로 디스패치한 뒤, 결과를 통합된 형식으로 제시한다.

> **참고**: 본 스킬은 [oh-my-claudecode](https://github.com/Yeachan-Heo/oh-my-claudecode/blob/main/skills/external-context/SKILL.md)의 `external-context`(MIT License)를 한국어 레포 컨벤션에 맞춰 재작성한 것이다. Copyright (c) Yeachan-Heo.

## 사용법

```
/external-context <주제 또는 질문>
```

### 예시

```
/external-context Node.js JWT 토큰 rotation 베스트 프랙티스
/external-context Prisma vs Drizzle ORM PostgreSQL 비교
/external-context React Server Components 최신 패턴과 관례
/external-context Spring Boot 3.x @Transactional의 REQUIRES_NEW 동작
```

## 프로토콜

### 1단계: Facet 분해

쿼리를 받으면 2-5개의 독립적인 검색 facet으로 분해한다. 각 facet은 병렬 검색 가능한 독립 관점이어야 한다 — 서로 내용이 겹치면 병렬화의 이득이 사라진다.

출력 형식:

```markdown
## 검색 분해

**쿼리:** <원본 쿼리>

### Facet 1: <facet 이름>
- **검색 초점:** 무엇을 찾을 것인가
- **소스:** 공식 문서, GitHub, 블로그 등

### Facet 2: <facet 이름>
...
```

### 2단계: 병렬 서브에이전트 호출

각 facet을 `Agent` 도구로 `docs-researcher` 서브에이전트에게 병렬 디스패치한다. 모든 호출은 **단일 메시지 내 병렬 tool_use 블록**으로 묶어야 한다 — 순차 호출하면 병렬화의 이득이 사라진다.

```
Agent(subagent_type="docs-researcher", description="facet 1 리서치", prompt="검색 대상: <facet 1 설명>. context7 MCP와 WebSearch/WebFetch로 공식 문서와 예시를 찾고, 모든 출처를 URL과 함께 인용하라. 버전 호환성과 출처 신선도를 플래그하라.")

Agent(subagent_type="docs-researcher", description="facet 2 리서치", prompt="검색 대상: <facet 2 설명>. context7 MCP와 WebSearch/WebFetch로 공식 문서와 예시를 찾고, 모든 출처를 URL과 함께 인용하라. 버전 호환성과 출처 신선도를 플래그하라.")
```

**최대 병렬 서브에이전트 수: 5**

### 3단계: 통합 출력 형식

서브에이전트들이 반환한 결과를 아래 형식으로 통합하여 사용자에게 제시한다:

```markdown
## External Context: <쿼리>

### 핵심 발견
1. **<발견 사항>** — 출처: [제목](URL)
2. **<발견 사항>** — 출처: [제목](URL)

### 상세 결과

#### Facet 1: <이름>
<인용 포함 통합 발견>

#### Facet 2: <이름>
<인용 포함 통합 발견>

### 출처
- [출처 1](URL)
- [출처 2](URL)
```

통합 시 중복된 출처는 하나로 합치되, 어느 facet에서 발견되었는지 교차 참조가 필요한 경우에는 "Facet 1, 3에서 확인됨" 식으로 표기한다.

## 설정

- **최대 병렬 서브에이전트 수**: 5
- **트리거**: 매직 키워드 없음 — 명시적 `/external-context` 호출만
- **facet 최소 수**: 2 (단일 검색으로 충분하면 이 스킬을 쓰지 않고 바로 WebSearch 호출)
- **facet 최대 수**: 5 (과잉 분해는 통합 비용을 키운다)

## 다른 스킬과의 연동

### `/study-helper`에서의 호출

`study-helper`의 3단계(팩트체크)에서 이 스킬을 호출한다. 학습 노트의 기술적 정확성 검증이 핵심 용도이므로, 팩트체크 모드에서는 다음 원칙을 따른다:

- **버전 기준**: 현재 널리 사용되는 최신 안정 버전(Latest Stable)을 기본으로 검증한다.
- **레거시 보완**: 노트에 이미 언급된 과거 버전만 한 줄로 간략히 언급한다. 노트에 없는 레거시 내용을 새로 추가하지 않는다.
- **수정 제안 근거**: 팩트체크로 수정을 제안할 때는 반드시 공식 문서 URL을 함께 제시한다.

### 독립 사용

`study-helper`와 무관하게 라이브러리 선택, API 사용법 조회, 패턴 비교, 버전 차이 확인 같은 일반 리서치 용도로도 사용 가능하다.

## Troubleshooting

**facet이 1개로 환원되는 경우**
- 원인: 쿼리가 이미 충분히 좁아서 분해할 관점이 없음
- 해결: 이 스킬을 쓰지 말고 직접 `docs-researcher` 에이전트를 1회 호출하거나 WebSearch를 직접 사용

**docs-researcher 에이전트를 찾을 수 없는 경우**
- 원인: `docs-researcher.md`가 설치되지 않았거나 Claude Code가 재시작되지 않아 로드되지 않음
- 해결: 아래 두 경로 중 하나에 파일이 있는지 확인 후 Claude Code 재시작
  - 레포-로컬 설치: `<repo>/agents/docs-researcher.md` (이 레포의 기본 위치)
  - 글로벌 설치: `~/.claude/agents/docs-researcher.md`

**context7 MCP가 응답하지 않는 경우**
- 원인: MCP 서버 연결 실패
- 해결: docs-researcher가 자동으로 WebSearch/WebFetch로 fallback하므로 별도 조치 불필요. 단, 결과 품질은 context7 사용 시보다 낮을 수 있음

**서브에이전트 결과가 서로 충돌하는 경우**
- 원인: 출처마다 버전이나 관점이 다름
- 해결: 통합 단계에서 "출처 A는 X라고 하고, 출처 B는 Y라고 함 — 버전/맥락 차이로 추정"처럼 충돌을 명시적으로 드러낸다. 임의로 한쪽을 택하지 말 것
