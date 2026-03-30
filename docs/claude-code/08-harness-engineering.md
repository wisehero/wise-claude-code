# 하네스 엔지니어링

> **난이도**: 중급~심화 | **선행 문서**: [Claude Code 시작하기](01-getting-started.md)
>
> 프롬프트 엔지니어링과 하네스 엔지니어링의 차이를 이해하고, Claude Code의 하네스를 체계적으로 설계하는 방법을 다룬다.

---

## 1. 프롬프트 엔지니어링의 한계

프롬프트 엔지니어링은 Claude에게 원하는 결과를 얻기 위해 입력 텍스트를 최적화하는 기법이다. 역할 부여, Chain of Thought, Few-shot, XML 태그 활용 — 이 기법들은 강력하고 여전히 유효하다. 하지만 실무에서 반복적으로 쓰다 보면 근본적인 한계에 부딪힌다.

### 반복의 문제

매 세션을 시작할 때마다 같은 지시를 반복해야 한다.

```
"한국어로 응답해줘. 커밋 메시지도 한국어로.
테스트는 npm test로 실행하고, 코드 스타일은 Prettier를 따라.
외부 라이브러리는 번들 크기 때문에 함부로 추가하지 마."
```

이 지시를 어제도 했고 오늘도 해야 하고 내일도 해야 한다. 프롬프트 엔지니어링은 본질적으로 일회성이다. 세션이 끝나면 맥락도 사라진다.

### 일관성의 문제

같은 팀의 두 개발자가 같은 프로젝트에서 Claude Code를 쓴다고 하자. 한 명은 "커밋 메시지를 한국어로 써줘"라고 프롬프트하고, 다른 한 명은 이 지시를 잊는다. 결과적으로 git 로그에 한국어와 영어 커밋 메시지가 뒤섞인다. 프롬프트 엔지니어링은 개인의 기억력에 의존하기 때문에 팀 수준의 일관성을 보장할 수 없다.

### 자동화의 부재

"파일을 수정할 때마다 자동으로 포맷팅해줘" — 이 요청을 매번 프롬프트로 전달하면 Claude가 잊을 수도 있고, 실행 순서가 꼬일 수도 있다. 프롬프트는 요청이지 명령이 아니다. Claude가 요청을 "해석"하는 과정에서 누락이 생길 수 있다. 자동화가 필요한 작업은 프롬프트가 아니라 시스템 수준에서 보장해야 한다.

### 새 직원 비유의 확장

프롬프트 엔지니어링을 설명할 때 흔히 "Claude는 유능한 신입 직원"이라는 비유를 쓴다. 맞는 말이다. 하지만 유능한 신입에게 매일 출근할 때마다 "우리 팀은 코드 리뷰를 이렇게 하고, 배포 절차는 이렇고, 커밋 메시지는 이 형식을 따르고..."라고 반복 설명하는 사람은 없다. 실제 직장에서는 온보딩 문서, 팀 위키, CI/CD 파이프라인, 린트 설정이 이 역할을 대신한다.

하네스 엔지니어링은 Claude Code 환경에서 이 "온보딩 인프라"를 구축하는 것이다.

---

## 2. 하네스 엔지니어링이란

하네스(harness)는 원래 "마구"라는 뜻이다. 말의 힘을 제어하고 방향을 잡아주는 도구다. 하네스 엔지니어링은 Claude의 능력을 프로젝트에 맞게 제어하고 방향을 잡아주는 영구적인 환경을 설계하는 방법론이다.

프롬프트 엔지니어링이 "매번 무엇을 말할 것인가"의 기술이라면, 하네스 엔지니어링은 "한 번 어떻게 세팅할 것인가"의 기술이다.

### 핵심 차이 비교

| 비교 항목 | 프롬프트 엔지니어링 | 하네스 엔지니어링 |
|---|---|---|
| **적용 시점** | 매 요청마다 | 프로젝트 설정 시 한 번 |
| **지속성** | 세션 종료 시 소멸 | 파일로 영구 저장 |
| **팀 공유** | 개인의 노하우에 의존 | git으로 버전 관리·공유 |
| **일관성** | 사람이 기억해야 함 | 시스템이 자동 적용 |
| **자동화** | 불가능 (요청 기반) | 이벤트 기반 자동 실행 |
| **버전 관리** | 불가능 | git diff로 변경 이력 추적 |
| **범위 제어** | 프롬프트 전체에 적용 | 특정 파일·디렉토리에만 선택 적용 |
| **비용** | 매 요청마다 토큰 소비 | 한 번 설정하면 추가 비용 최소화 |

### 둘은 대체 관계가 아니다

하네스 엔지니어링이 프롬프트 엔지니어링을 대체하는 것은 아니다. 둘은 보완 관계다.

하네스는 **반복되는 맥락과 규칙**을 담당한다. "이 프로젝트는 TypeScript를 쓰고, 테스트는 Jest로 실행하며, 커밋 메시지는 한국어로 작성한다" — 이런 것은 한 번 설정하면 된다.

프롬프트는 **일회성 작업 지시**를 담당한다. "이 함수의 성능을 개선해줘", "이 버그를 분석해줘" — 이런 것은 매번 달라지므로 프롬프트로 전달한다.

좋은 하네스가 갖춰지면 프롬프트가 짧아진다. "한국어로, Prettier 형식으로, 테스트 통과 후 커밋해줘"라고 매번 덧붙일 필요 없이 "이 함수 성능 개선해줘"만 말하면 된다. 나머지는 하네스가 처리한다.

---

## 3. Claude Code 하네스의 6대 레이어

Claude Code의 하네스는 여섯 개의 레이어로 구성된다. 각 레이어는 서로 다른 역할을 담당하며, 조합해서 사용할 때 가장 강력하다.

```
┌─────────────────────────────────────────────┐
│              MCP 서버 (도구 확장)              │
├─────────────────────────────────────────────┤
│              Skills (워크플로우)               │
├─────────────────────────────────────────────┤
│              Hooks (자동화)                   │
├─────────────────────────────────────────────┤
│           settings.json (설정/권한)            │
├─────────────────────────────────────────────┤
│          .claude/rules/ (조건부 규칙)          │
├─────────────────────────────────────────────┤
│            CLAUDE.md (지침)                   │
└─────────────────────────────────────────────┘
```

아래에서 위로 갈수록 추상화 수준이 높아진다. CLAUDE.md는 "무엇을 해야 하는가"를 텍스트로 전달하고, MCP 서버는 "외부 세계와 어떻게 연결하는가"를 프로토콜로 정의한다.

### 3.1 CLAUDE.md — 지침 레이어

CLAUDE.md는 하네스의 토대다. Claude Code가 세션을 시작할 때 자동으로 읽어 프로젝트 맥락을 파악하는 지침 파일이다. API의 시스템 프롬프트와 같은 역할을 하지만, 파일로 존재하기 때문에 git으로 관리할 수 있다.

#### 무엇을 담는가

**프로젝트 정체성**: 이 프로젝트가 무엇인지 한 줄로 정의한다. Claude가 작업의 맥락을 즉시 파악하는 출발점이 된다.

```markdown
## Project
- 한국 시장 대상 B2B SaaS 결제 플랫폼
- 주 언어: TypeScript (프론트엔드 React, 백엔드 NestJS)
- 주 소통 언어: 한국어 (커밋 메시지, 코드 리뷰, 문서)
```

**빌드·테스트 명령어**: Claude가 추측하지 않고 올바른 명령을 실행하도록 정확한 명령어를 명시한다.

```markdown
## Commands
- 테스트: `npm run test -- --watch=false`
- 린트: `npm run lint`
- 빌드: `npm run build`
- 단일 테스트: `npm run test -- --testPathPattern="파일명"`
```

**코드 컨벤션**: 자동 포매터로 강제되지 않는 팀 고유의 규칙을 적는다. 이 규칙들은 코드를 읽는 것만으로는 Claude가 추론하기 어려운 것들이다.

```markdown
## Code Style
- 함수명은 동사로 시작한다 (get, create, validate, process)
- 에러 처리는 반드시 커스텀 AppError 클래스를 사용한다
- 외부 라이브러리 추가 시 번들 크기 영향을 먼저 확인한다
- DB 쿼리는 ORM 대신 쿼리 빌더(Knex)를 사용한다
```

**금지사항**: Claude가 하면 안 되는 일을 명시한다. 이유를 함께 적으면 Claude가 유사한 상황에서도 올바르게 판단한다.

```markdown
## Don't
- .env, .env.production 파일을 수정하지 않는다 (보안 민감 파일)
- prisma migrate를 직접 실행하지 않는다 (DBA 승인 필요)
- console.log를 프로덕션 코드에 남기지 않는다 (로깅은 winston 사용)
```

#### 계층 구조

CLAUDE.md는 세 계층으로 동작하며, 상위 계층이 우선한다.

| 계층 | 위치 | 관리 주체 | 용도 |
|---|---|---|---|
| 관리 정책 | OS별 시스템 경로 | 조직 관리자 | 보안 정책, 사내 규정 |
| 프로젝트 | `./CLAUDE.md` 또는 `./.claude/CLAUDE.md` | 팀 (git 커밋) | 프로젝트 컨벤션 |
| 사용자 | `~/.claude/CLAUDE.md` | 개인 | 개인 작업 스타일 |

팀 전체에 적용할 규칙은 프로젝트 레벨에, 개인 취향(응답 언어, 출력 형식 등)은 사용자 레벨에 배치한다.

#### 크기 관리

CLAUDE.md는 Claude의 컨텍스트 창을 직접 소비한다. 200줄 미만을 목표로 하되, 긴 참조 문서는 `@문법`으로 외부 파일을 참조한다.

```markdown
# 프로젝트 지침

@README.md
@docs/architecture.md
@package.json
```

`@파일경로`를 쓰면 CLAUDE.md 자체는 간결하게 유지하면서 필요한 맥락을 Claude에게 제공할 수 있다.

### 3.2 .claude/rules/ — 조건부 규칙 레이어

CLAUDE.md가 프로젝트 전체에 적용되는 규칙이라면, `.claude/rules/`는 특정 파일이나 디렉토리에만 적용되는 조건부 규칙이다.

#### 구조

```
.claude/
  rules/
    backend.md          # 백엔드 코드에만 적용
    frontend.md         # 프론트엔드 코드에만 적용
    testing.md          # 테스트 파일에만 적용
    api-contracts.md    # API 관련 파일에만 적용
```

#### paths 필드로 범위 지정

각 규칙 파일 상단에 YAML frontmatter를 작성해 적용 범위를 지정한다.

```markdown
---
paths:
  - "src/api/**"
  - "src/routes/**"
---

# API 규칙

- 모든 엔드포인트는 입력값 유효성 검사를 포함해야 한다
- 응답 형식은 `{ data, error, meta }` 구조를 따른다
- HTTP 상태 코드는 REST 표준에 맞게 사용한다 (200, 201, 400, 401, 403, 404, 500)
- 페이지네이션은 cursor 기반을 사용한다 (offset 기반 금지)
```

Claude가 `src/api/` 하위 파일을 작업할 때만 이 규칙이 로드된다. 프론트엔드 작업 중에는 컨텍스트를 차지하지 않는다.

#### 왜 CLAUDE.md와 분리하는가

모든 규칙을 CLAUDE.md 하나에 넣으면 파일이 비대해진다. 백엔드 개발자가 프론트엔드 규칙을 매번 로드할 필요도 없다. rules/를 쓰면 두 가지 이점이 생긴다.

첫째, **컨텍스트 효율성**이다. 필요한 규칙만 로드하므로 실제 작업에 쓸 수 있는 컨텍스트가 늘어난다.

둘째, **관리 편의성**이다. API 규칙을 수정할 때 CLAUDE.md 전체를 건드리지 않고 `api-contracts.md`만 수정하면 된다.

### 3.3 settings.json — 설정 레이어

settings.json은 Claude Code의 동작 방식을 제어하는 JSON 설정 파일이다. CLAUDE.md가 "무엇을 하라"는 지침이라면, settings.json은 "어떤 도구를 쓸 수 있고 어떤 권한이 있는가"를 정의한다.

#### 세 계층

```
~/.claude/settings.json          # 글로벌: 모든 프로젝트 공통
.claude/settings.json            # 프로젝트: 팀과 공유 (git 커밋)
.claude/settings.local.json      # 로컬: 개인 전용 (git 제외)
```

#### 핵심 설정 항목

**권한 설정**: Claude가 확인 없이 실행할 수 있는 도구를 정의한다.

```json
{
  "permissions": {
    "allow": [
      "Bash(git:*)",
      "Bash(npm test:*)",
      "Bash(npx jest:*)",
      "Read",
      "Glob",
      "Grep"
    ],
    "deny": [
      "Bash(rm -rf:*)",
      "Bash(git push --force:*)"
    ]
  }
}
```

`allow`에 등록된 도구는 매번 확인 없이 실행된다. `deny`에 등록된 명령은 항상 차단된다. 이 설정이 없으면 Claude가 git status를 실행할 때마다 "허용할까요?"라는 프롬프트가 뜬다.

**환경 변수**: Claude Code 실행 환경에 필요한 변수를 정의한다.

```json
{
  "env": {
    "NODE_ENV": "development",
    "LOG_LEVEL": "debug"
  }
}
```

#### 프로젝트 설정 vs 로컬 설정

팀 전체에 강제할 설정(허용 도구, 금지 명령)은 `.claude/settings.json`에 넣고 git에 커밋한다. API 키처럼 개인마다 다른 값은 `.claude/settings.local.json`에 넣어 git에서 제외한다.

### 3.4 Hooks — 자동화 레이어

Hook은 Claude Code의 특정 이벤트에 반응해 자동으로 실행되는 명령이다. 앞의 세 레이어(CLAUDE.md, rules, settings)가 Claude에게 "이렇게 해라"고 텍스트로 전달하는 지침이라면, Hook은 "이 이벤트가 발생하면 이 명령을 실행하라"는 기계적 자동화다.

지침과 자동화의 차이는 중요하다. CLAUDE.md에 "파일 수정 후 prettier를 실행하라"고 쓰면 Claude가 이 지시를 해석하고 판단해서 실행한다. 가끔 빼먹을 수 있다. Hook으로 설정하면 파일이 수정될 때마다 시스템이 무조건 실행한다. 빼먹을 여지가 없다.

#### 12가지 이벤트

| 이벤트 | 발생 시점 | 차단 가능 |
|---|---|---|
| `SessionStart` | 세션 시작/재개 | 아니오 |
| `PreToolUse` | 도구 실행 직전 | 예 |
| `PostToolUse` | 도구 실행 성공 후 | 아니오 |
| `PermissionRequest` | 권한 확인 프롬프트 표시 시 | 예 |
| `Notification` | 알림 발생 시 | 아니오 |
| `Stop` | 응답 완료 시 | 아니오 |
| `ConfigChange` | 설정 파일 변경 시 | 아니오 |
| `CwdChanged` | 작업 디렉토리 변경 시 | 아니오 |
| `FileChanged` | 파일 변경 감시 이벤트 발생 시 | 아니오 |
| `SubagentStart` | 서브에이전트 시작 시 | 아니오 |
| `SubagentStop` | 서브에이전트 종료 시 | 아니오 |
| `SessionEnd` | 세션 종료 시 | 아니오 |

`PreToolUse`와 `PermissionRequest`만 차단이 가능하다. 이 두 이벤트에서 Hook이 종료 코드 `2`를 반환하면 해당 작업이 실행되지 않는다.

#### 4가지 Hook 타입

**command**: 셸 명령어를 직접 실행한다. 가장 일반적인 타입이다.

```json
{
  "type": "command",
  "command": "npx prettier --write \"$TOOL_INPUT_FILE_PATH\" 2>/dev/null || true"
}
```

**prompt**: Claude(Haiku 모델)에게 판단을 맡긴다. 규칙 기반으로 처리하기 어려운 맥락적 판단에 적합하다.

```json
{
  "type": "prompt",
  "prompt": "이 파일 편집이 보안에 영향을 줄 수 있는지 확인하라."
}
```

**agent**: 서브에이전트를 별도로 실행해 복잡한 검증 작업을 맡긴다.

```json
{
  "type": "agent",
  "agent": "security-reviewer",
  "prompt": "변경된 파일에서 SQL 인젝션 취약점을 검토하라."
}
```

**http**: HTTP 엔드포인트를 호출한다. 외부 서비스 연동이나 웹훅 전송에 사용한다.

```json
{
  "type": "http",
  "url": "https://hooks.slack.com/services/...",
  "method": "POST"
}
```

#### 실전 패턴

**자동 포맷팅** — 파일 편집 후 prettier가 자동으로 실행된다.

```json
{
  "hooks": {
    "PostToolUse": [
      {
        "matcher": "Edit|Write",
        "hooks": [
          {
            "type": "command",
            "command": "npx prettier --write \"$TOOL_INPUT_FILE_PATH\" 2>/dev/null || true"
          }
        ]
      }
    ]
  }
}
```

**보호 파일 차단** — .env 파일 수정을 사전에 막는다.

```json
{
  "hooks": {
    "PreToolUse": [
      {
        "matcher": "Edit|Write",
        "hooks": [
          {
            "type": "command",
            "command": "python3 -c \"\nimport json, sys\ndata = json.load(sys.stdin)\npath = data.get('tool_input', {}).get('file_path', '')\nprotected = ['.env', '.env.production', 'secrets.yaml']\nif any(p in path for p in protected):\n    print(f'차단: {path}는 보호된 파일입니다.')\n    sys.exit(2)\n\""
          }
        ]
      }
    ]
  }
}
```

**안전한 명령 자동 승인** — git status, npm test 같은 읽기 전용 명령은 확인 없이 실행한다.

```json
{
  "hooks": {
    "PermissionRequest": [
      {
        "matcher": "Bash",
        "hooks": [
          {
            "type": "command",
            "command": "python3 -c \"\nimport json, sys\ndata = json.load(sys.stdin)\ncmd = data.get('tool_input', {}).get('command', '')\nsafe = ['git status', 'git log', 'git diff', 'npm test', 'npx jest']\nif any(cmd.startswith(p) for p in safe):\n    sys.exit(0)\nelse:\n    sys.exit(1)\n\""
          }
        ]
      }
    ]
  }
}
```

### 3.5 Skills — 워크플로우 레이어

스킬은 반복적인 작업 절차를 캡슐화한 재사용 단위다. CLAUDE.md가 "어떤 규칙을 따르라"고 지시한다면, 스킬은 "이 작업을 이 절차로 수행하라"는 구체적인 워크플로우를 정의한다.

#### 스킬이 해결하는 문제

"SRS를 IEEE 830 기준으로 작성해줘. FR에는 선행 조건과 예외 흐름을 포함하고, NFR에는 성능 수치를 명시하고, 인터뷰는 6단계로 진행하고..." — 이 프롬프트를 매번 쓸 수는 없다. 스킬로 만들면 `/spec-writer`라고 입력하는 것만으로 동일한 품질의 작업이 시작된다.

#### 구조

```
skill-name/
├── SKILL.md          # 메인 지시문 (필수)
└── references/       # 템플릿, 카탈로그 등 보조 파일
```

`SKILL.md`는 frontmatter와 본문으로 구성된다.

```yaml
---
name: spec-writer
version: 1.3.0
description: 시스템 요구사항 명세서(SRS)를 대화형 인터뷰를 통해 작성하는 스킬.
  "요구사항 명세서 작성해줘", "SRS 만들어줘" 같은 요청에 사용.
  단, 단순 기획서 작성이나 API 문서 자동생성 요청에는 사용하지 않는다.
---

## 핵심 원칙
...

## 워크플로우
요청 입력 → 인터뷰 6단계 → SRS 생성 → 리뷰
...
```

#### 호출 방식

**슬래시 명령으로 직접 호출**: `/spec-writer`라고 입력하면 즉시 실행된다.

**자동 감지**: "요구사항 명세서 작성해줘"라고 자연어로 요청하면 Claude가 `description`을 읽고 적합한 스킬을 자동으로 선택한다.

자동 감지의 정확도는 `description`의 품질이 결정한다. 긍정 예시("SRS 만들어줘")와 부정 예시("Swagger 문서 생성은 사용하지 않는다")를 모두 포함해야 오작동을 줄일 수 있다.

#### 설치 위치

| 위치 | 범위 |
|---|---|
| `~/.claude/skills/` | 모든 프로젝트에서 사용하는 글로벌 스킬 |
| `.claude/skills/` | 현재 프로젝트에서만 사용하는 프로젝트 스킬 |
| `skills/` (프로젝트 루트) | 스킬을 관리하는 레포지토리에서 사용 |

프로젝트 스킬이 글로벌 스킬보다 우선한다. 같은 이름의 스킬이 두 곳에 있으면 프로젝트 스킬이 사용된다.

### 3.6 MCP 서버 — 도구 확장 레이어

MCP(Model Context Protocol)는 Claude가 외부 서비스와 상호작용하는 방식을 표준화한 프로토콜이다. 앞의 다섯 레이어가 Claude의 행동과 규칙을 제어한다면, MCP는 Claude가 접근할 수 있는 도구의 범위 자체를 확장한다.

#### Claude Code의 기본 도구

Claude Code는 기본적으로 파일 읽기/쓰기, 셸 명령 실행, git 작업을 수행할 수 있다. 이것이 Claude Code의 기본 도구 세트다. MCP 서버를 연동하면 이 세트에 새로운 도구가 추가된다.

#### 주요 MCP 서버

**GitHub MCP**: 이슈 생성, PR 리뷰, 코드 조회를 Claude Code 세션 안에서 처리한다.

```bash
claude mcp add github -- npx @anthropic-ai/github-mcp
```

**Playwright MCP**: 웹 브라우저를 직접 제어해 페이지 탐색, 스크린샷, 폼 입력을 자동화한다.

```bash
claude mcp add playwright -- npx @playwright/mcp@latest
```

**Notion MCP**: 노션 페이지를 읽고 편집한다.

```bash
claude mcp add notion -- npx @anthropic-ai/notion-mcp
```

**Context7 MCP**: 외부 라이브러리의 공식 문서를 실시간으로 가져온다. Claude의 학습 데이터 기준일 이후 변경된 API를 정확히 참조할 때 유용하다.

```bash
claude mcp add context7 -- npx @context7/mcp
```

#### 설정 파일

`.mcp.json`에 서버를 등록한다. 환경 변수로 민감한 값을 관리한다.

```json
{
  "mcpServers": {
    "github": {
      "command": "npx",
      "args": ["@anthropic-ai/github-mcp"],
      "env": {
        "GITHUB_TOKEN": "${GITHUB_TOKEN}"
      }
    }
  }
}
```

글로벌 설정(`~/.claude/.mcp.json`)은 모든 프로젝트에, 프로젝트 설정(`.mcp.json`)은 해당 프로젝트에만 적용된다.

---

## 4. 하네스 설계 원칙

여섯 개 레이어를 개별적으로 이해하는 것과 이들을 조합해 효과적인 하네스를 설계하는 것은 다른 문제다. 아래 네 가지 원칙이 설계의 기본이다.

### 원칙 1: 레이어 간 역할 분리

각 레이어가 담당하는 질문이 다르다. 이 구분을 명확히 하면 같은 규칙이 여러 곳에 분산되는 것을 방지한다.

| 질문 | 담당 레이어 |
|---|---|
| 무엇을 해야 하는가? | CLAUDE.md |
| 이 파일을 다룰 때 특별히 지켜야 할 것은? | .claude/rules/ |
| 어떤 도구를 쓸 수 있는가? | settings.json |
| 이벤트 발생 시 무엇을 자동으로 실행하는가? | Hooks |
| 이 작업을 어떤 절차로 수행하는가? | Skills |
| 어떤 외부 서비스와 연결하는가? | MCP |

예를 들어 "API 응답 형식은 `{ data, error, meta }` 구조를 따른다"는 규칙은 `.claude/rules/api-contracts.md`에 둔다. "파일 수정 후 prettier를 실행한다"는 Hooks에 둔다. "SRS를 6단계 인터뷰로 작성한다"는 Skills에 둔다. 세 가지를 CLAUDE.md에 모두 넣으면 파일이 비대해지고 역할이 뒤섞인다.

### 원칙 2: 변경 빈도에 따른 배치

하네스의 각 레이어는 변경 빈도가 다르다. 자주 바뀌는 것과 거의 바뀌지 않는 것을 분리하면 관리가 쉬워진다.

| 변경 빈도 | 레이어 | 예시 |
|---|---|---|
| 거의 안 바뀜 | CLAUDE.md, settings.json | 프로젝트 정체성, 권한 정책 |
| 가끔 바뀜 | .claude/rules/, Hooks | 코딩 규칙 추가, 자동화 패턴 변경 |
| 자주 바뀜/발전 | Skills, MCP | 새 워크플로우 추가, 외부 도구 연동 |

CLAUDE.md를 매주 고쳐야 한다면 해당 내용은 rules/로 분리하는 것이 맞다. 반대로 settings.json을 자주 바꿔야 한다면 설계에 문제가 있을 가능성이 높다.

### 원칙 3: 팀 공유 vs 개인 설정 분리

하네스의 모든 요소를 팀에 공유할 필요는 없다.

**git에 커밋해 팀과 공유하는 것:**

| 파일 | 이유 |
|---|---|
| `CLAUDE.md` | 팀 컨벤션은 전원이 따라야 한다 |
| `.claude/settings.json` | 허용/금지 도구는 팀 정책이다 |
| `.claude/rules/` | 코딩 규칙도 팀 전체에 적용된다 |
| `.mcp.json` | 프로젝트에 필요한 외부 도구는 공유한다 |

**개인 환경에 두는 것:**

| 파일 | 이유 |
|---|---|
| `~/.claude/CLAUDE.md` | 개인 작업 스타일 |
| `.claude/settings.local.json` | API 키, 개인 토큰 |
| `~/.claude/settings.json` | 개인 선호 모델, 권한 모드 |

### 원칙 4: 컨텍스트 예산 관리

하네스도 토큰을 소비한다. CLAUDE.md, rules, 스킬 본문은 모두 Claude의 컨텍스트 창에 로드된다. 하네스가 비대해지면 실제 작업에 쓸 수 있는 공간이 줄어든다.

- CLAUDE.md는 200줄 미만을 목표로 한다
- 긴 참조 문서는 `@문법`으로 분리한다
- rules/의 `paths` 필드로 필요한 규칙만 로드한다
- 스킬 본문은 간결하게, 상세 지식은 `references/`로 분리한다

컨텍스트 효율성은 단순히 토큰을 아끼는 문제가 아니다. 불필요한 정보가 많으면 Claude가 핵심 지시를 놓칠 확률도 높아진다.

---

## 5. 프롬프트에서 하네스로: 전환 패턴

프롬프트 엔지니어링에서 하네스 엔지니어링으로 전환하는 것은 한 번에 일어나지 않는다. "이 프롬프트를 또 쓰고 있네"라는 순간이 하네스로 옮길 신호다.

### 패턴 1: 반복 지시 → CLAUDE.md

**Before** — 매 세션 시작마다:

```
한국어로 응답해줘. 커밋 메시지도 한국어로.
이 프로젝트는 TypeScript + React야.
테스트는 npm run test로 실행해.
```

**After** — `CLAUDE.md`에 한 번 작성:

```markdown
## Project
- 주 언어: 한국어 (응답, 커밋 메시지, 문서)
- 기술 스택: TypeScript, React, NestJS
- 테스트: `npm run test`
```

이제 세션을 시작하면 Claude가 자동으로 한국어로 응답하고, 올바른 테스트 명령을 사용한다.

### 패턴 2: 형식 검증 → Hooks

**Before** — 파일 수정 후 매번:

```
방금 수정한 파일에 prettier 돌려줘
```

**After** — `settings.json`에 Hook 설정:

```json
{
  "hooks": {
    "PostToolUse": [
      {
        "matcher": "Edit|Write",
        "hooks": [
          {
            "type": "command",
            "command": "npx prettier --write \"$TOOL_INPUT_FILE_PATH\" 2>/dev/null || true"
          }
        ]
      }
    ]
  }
}
```

파일이 수정될 때마다 시스템이 자동으로 prettier를 실행한다. Claude에게 요청할 필요도, Claude가 잊을 가능성도 없다.

### 패턴 3: 복잡한 절차 → Skills

**Before** — SRS 작성을 요청할 때마다:

```
시스템 요구사항 명세서를 작성해줘. IEEE 830 기준으로.
기능 요구사항에는 선행 조건, 입력 데이터, 기본 흐름, 예외 흐름, 후행 조건을 포함해.
비기능 요구사항에는 성능, 보안, 가용성 수치를 명시해.
인터뷰는 프로젝트 개요 → 기능 요구사항 → 비기능 요구사항 → 인터페이스 순으로 진행해.
모호한 답변이 오면 구체적인 선택지를 제시해서 명확한 답을 받아내.
```

**After** — `/spec-writer` 한 줄:

```
/spec-writer
```

100줄이 넘는 프롬프트가 세 글자로 줄어든다. 작업 품질은 스킬이 보장한다.

### 패턴 4: 외부 데이터 접근 → MCP

**Before** — GitHub 이슈를 확인하려면:

```
https://github.com/our-team/project/issues/42 이 이슈 내용을 확인하고 분석해줘
```

**After** — GitHub MCP 연동 후:

```
이슈 #42 분석해줘
```

Claude가 GitHub MCP 도구를 통해 직접 이슈를 조회한다. URL을 복사해서 붙여넣을 필요가 없다.

### 패턴 5: 보안 정책 → Hooks + rules

**Before** — 매 세션마다 경고:

```
.env 파일이나 secrets.yaml은 절대 수정하지 마. 프로덕션 DB에 직접 쿼리도 하지 마.
```

**After** — PreToolUse Hook으로 시스템 수준 차단:

Hook이 보호 파일 수정을 감지하면 종료 코드 2를 반환해 작업을 원천 차단한다. Claude가 지시를 "잊어도" 시스템이 막는다. CLAUDE.md에도 이유와 함께 명시해두면 이중 안전장치가 된다.

---

## 6. 실전 사례: 팀 하네스 구성 예시

스타트업의 4인 웹 개발팀이 결제 플랫폼을 개발하는 상황을 가정한다. 이 팀의 하네스 구성 전체를 살펴본다.

### 디렉토리 구조

```
project-root/
├── CLAUDE.md                     # 프로젝트 지침
├── .mcp.json                     # MCP 서버 설정
├── .claude/
│   ├── settings.json             # 팀 공유 설정
│   ├── settings.local.json       # 개인 설정 (git 제외)
│   ├── rules/
│   │   ├── backend.md            # 백엔드 규칙
│   │   ├── frontend.md           # 프론트엔드 규칙
│   │   └── testing.md            # 테스트 규칙
│   └── skills/
│       └── deploy-checklist/
│           └── SKILL.md          # 배포 전 체크리스트 스킬
```

### CLAUDE.md

```markdown
# Payment Platform

## Project
- 한국 시장 B2B SaaS 결제 플랫폼
- TypeScript (프론트: React 18, 백엔드: NestJS)
- 주 소통 언어: 한국어

## Commands
- 테스트: `npm run test`
- 린트: `npm run lint`
- 빌드: `npm run build`
- DB 마이그레이션: `npm run migration:run` (DBA 승인 후에만)

## Rules
- 커밋 메시지: 한국어, conventional commits 형식
- 외부 라이브러리 추가 시 번들 크기 영향 확인 필수
- console.log 금지 — 로깅은 winston 사용
- .env, secrets.yaml 수정 금지

@docs/architecture.md
```

### .claude/rules/backend.md

```markdown
---
paths:
  - "src/api/**"
  - "src/services/**"
  - "src/repositories/**"
---

# 백엔드 규칙

- 모든 서비스 메서드는 트랜잭션을 명시적으로 관리한다
- DB 쿼리는 Knex 쿼리 빌더를 사용한다 (raw SQL 금지)
- 결제 관련 금액은 반드시 정수(원 단위)로 처리한다 (부동소수점 금지)
- API 응답은 { data, error, meta } 구조를 따른다
- 에러는 커스텀 AppError 클래스를 사용한다
```

### .claude/rules/testing.md

```markdown
---
paths:
  - "**/*.spec.ts"
  - "**/*.test.ts"
  - "test/**"
---

# 테스트 규칙

- 단위 테스트는 의존성을 모두 모킹한다
- 통합 테스트는 실제 DB 연결을 사용한다 (테스트 DB)
- 결제 로직 테스트는 성공, 실패, 타임아웃, 중복 결제 케이스를 반드시 포함한다
- 테스트 데이터는 팩토리 패턴으로 생성한다
```

### .claude/settings.json

```json
{
  "permissions": {
    "allow": [
      "Bash(git:*)",
      "Bash(npm test:*)",
      "Bash(npm run lint:*)",
      "Read",
      "Glob",
      "Grep"
    ],
    "deny": [
      "Bash(rm -rf:*)",
      "Bash(git push --force:*)"
    ]
  },
  "hooks": {
    "PostToolUse": [
      {
        "matcher": "Edit|Write",
        "hooks": [
          {
            "type": "command",
            "command": "npx prettier --write \"$TOOL_INPUT_FILE_PATH\" 2>/dev/null || true"
          }
        ]
      }
    ],
    "PreToolUse": [
      {
        "matcher": "Edit|Write",
        "hooks": [
          {
            "type": "command",
            "command": "python3 -c \"\nimport json, sys\ndata = json.load(sys.stdin)\npath = data.get('tool_input', {}).get('file_path', '')\nprotected = ['.env', 'secrets.yaml', 'docker-compose.prod.yml']\nif any(p in path for p in protected):\n    print(f'차단: {path}는 보호된 파일입니다.')\n    sys.exit(2)\n\""
          }
        ]
      }
    ]
  }
}
```

이 하네스가 갖춰지면 팀원 4명 모두가 동일한 환경에서 작업한다. 새 팀원이 합류해도 `git clone` 한 번이면 하네스가 함께 설치된다.

---

## 7. 다른 플랫폼의 하네스 메커니즘

하네스 엔지니어링은 Claude Code에만 존재하는 개념이 아니다. 다른 LLM 도구들도 유사한 메커니즘을 제공한다. 비교하면 Claude Code의 하네스가 어떤 위치에 있는지 더 잘 이해할 수 있다.

### OpenAI Custom GPTs

OpenAI의 Custom GPTs는 세 가지 구성 요소로 하네스를 제공한다.

- **Instructions**: 시스템 프롬프트에 해당한다. Claude Code의 CLAUDE.md와 유사하지만 파일이 아니라 웹 UI에서 설정하므로 git 관리가 불가능하다.
- **Actions**: 외부 API를 OpenAPI 스펙으로 연동한다. Claude Code의 MCP 서버와 유사한 역할이지만, 프로토콜 표준화 수준에서 차이가 있다.
- **Knowledge**: 참조 문서를 업로드한다. Claude Code의 `@문법` 파일 참조와 비슷하다.

Custom GPTs는 설정이 간편하지만 이벤트 기반 자동화(Hooks)나 조건부 규칙(rules/)에 해당하는 기능이 없다.

### Cursor (.cursorrules)

Cursor IDE는 `.cursorrules` 파일로 프로젝트 수준 지침을 제공한다. Claude Code의 CLAUDE.md와 유사한 역할이다. 단, Cursor는 단일 파일 방식이므로 Claude Code의 `.claude/rules/`처럼 파일별 조건부 규칙을 분리하는 기능은 지원하지 않는다.

### Claude Code의 차별점

| 기능 | Claude Code | Custom GPTs | Cursor |
|---|---|---|---|
| 프로젝트 지침 | CLAUDE.md (git 관리) | Instructions (웹 UI) | .cursorrules (파일) |
| 조건부 규칙 | .claude/rules/ + paths | 없음 | 없음 |
| 이벤트 자동화 | Hooks (12가지 이벤트) | 없음 | 없음 |
| 재사용 워크플로우 | Skills | 없음 | 없음 |
| 외부 도구 연동 | MCP (표준 프로토콜) | Actions (OpenAPI) | 없음 |
| 설정 계층 | 3계층 (글로벌/프로젝트/로컬) | 단일 | 단일 |
| 버전 관리 | git 네이티브 | 불가 | git 네이티브 |

Claude Code의 하네스가 가장 풍부한 레이어 구조를 제공한다. 특히 Hooks에 의한 이벤트 기반 자동화와 Skills에 의한 워크플로우 캡슐화는 다른 도구에서 찾기 어려운 기능이다.

---

## 8. 핵심 정리

### 한 줄 요약

프롬프트 엔지니어링이 "매번 무엇을 말할 것인가"의 기술이라면, 하네스 엔지니어링은 "한 번 어떻게 세팅할 것인가"의 기술이다.

### 하네스 도입 체크리스트

**기본 (당장 시작)**
- [ ] CLAUDE.md에 프로젝트 정체성, 주 언어, 빌드/테스트 명령을 적었는가?
- [ ] 반복적으로 프롬프트하던 지시를 CLAUDE.md로 옮겼는가?
- [ ] settings.json에 자주 쓰는 도구를 allow 목록에 추가했는가?

**중급 (익숙해지면)**
- [ ] .claude/rules/에 영역별 규칙을 분리했는가?
- [ ] PostToolUse Hook으로 자동 포맷팅을 설정했는가?
- [ ] PreToolUse Hook으로 보호 파일 수정을 차단했는가?
- [ ] GitHub MCP 서버를 연동했는가?

**심화 (팀 운영)**
- [ ] 반복 워크플로우를 Skills로 캡슐화했는가?
- [ ] 팀 공유 설정과 개인 설정을 분리했는가?
- [ ] 하네스 전체를 git에 커밋해 팀과 공유하고 있는가?
- [ ] 새 팀원이 git clone만으로 동일한 환경을 갖추는가?

### 레이어별 한 줄 요약

| 레이어 | 한 줄 요약 | 핵심 질문 |
|---|---|---|
| CLAUDE.md | 프로젝트의 신분증 | 이 프로젝트에서 어떻게 행동해야 하는가? |
| .claude/rules/ | 상황별 규칙집 | 이 파일을 다룰 때 추가로 지켜야 할 것은? |
| settings.json | 권한증과 출입증 | 어떤 도구를 쓸 수 있고 어떤 것이 금지인가? |
| Hooks | 자동 실행 장치 | 이 이벤트가 발생하면 무엇을 자동으로 하는가? |
| Skills | 업무 매뉴얼 | 이 작업을 어떤 절차로 수행하는가? |
| MCP 서버 | 외부 연결 통로 | 어떤 외부 서비스에 접근할 수 있는가? |

---

## 참고 문서

이 문서는 아래 공식 문서와 커뮤니티 자료를 기반으로 작성되었다.

### Anthropic 공식 문서

- [Claude Code Overview](https://docs.anthropic.com/en/docs/claude-code/overview)
- [Claude Code Memory](https://docs.anthropic.com/en/docs/claude-code/memory)
- [Claude Code Settings](https://docs.anthropic.com/en/docs/claude-code/settings)
- [Claude Code Hooks](https://docs.anthropic.com/en/docs/claude-code/hooks)
- [Claude Code Skills](https://docs.anthropic.com/en/docs/claude-code/skills)
- [Claude Code MCP](https://docs.anthropic.com/en/docs/claude-code/mcp)
- [Prompt Engineering Overview](https://docs.anthropic.com/en/docs/build-with-claude/prompt-engineering/overview)

### 기타 참고

- [OpenAI Custom GPTs Documentation](https://platform.openai.com/docs/guides/gpts)
- [Model Context Protocol](https://modelcontextprotocol.io)
- [Cursor .cursorrules Documentation](https://docs.cursor.com/context/rules-for-ai)
