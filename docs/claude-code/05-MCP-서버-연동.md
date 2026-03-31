# MCP 서버 연동

> **난이도**: 중급~심화 | **선행 문서**: [Claude Code 시작하기](01-시작하기.md)
>
> Model Context Protocol(MCP)의 개념과 Claude Code에서 외부 서비스를 연동하는 방법을 다룬다.

---

## MCP란?

MCP(Model Context Protocol, 모델 컨텍스트 프로토콜)는 Claude가 외부 서비스와 상호작용하는 방식을 표준화한 오픈 프로토콜이다. Anthropic이 설계하고 공개한 이 프로토콜은 Claude가 웹 브라우저, 데이터베이스, 협업 도구, 버전 관리 시스템 등 다양한 외부 시스템과 일관된 방식으로 통신할 수 있도록 한다.

Claude Code는 기본적으로 파일 읽기, 셸 명령 실행, git 작업 등 로컬 환경에서의 작업을 수행한다. MCP를 연동하면 Claude Code가 할 수 있는 일의 범위가 외부 서비스로 확장된다. GitHub에서 이슈를 가져오거나, PostgreSQL 데이터베이스에 쿼리를 실행하거나, Notion 페이지를 생성하는 작업을 Claude Code 세션 안에서 직접 처리할 수 있다.

### MCP 서버가 제공하는 것

MCP 서버는 세 가지 요소를 Claude에게 제공한다.

- **도구(tools)**: Claude가 직접 호출할 수 있는 함수다. GitHub의 이슈 생성, 데이터베이스 쿼리 실행, 브라우저 탐색 등이 이에 해당한다.
- **리소스(resources)**: Claude가 읽을 수 있는 데이터 소스다. 파일, URL, 데이터베이스 레코드처럼 컨텍스트에 포함할 수 있는 정보다.
- **프롬프트(prompts)**: 특정 작업에 최적화된 프롬프트 템플릿이다. MCP 서버가 자주 쓰는 작업 패턴을 미리 정의해 제공한다.

실제 사용에서는 도구가 가장 자주 쓰인다. MCP 서버를 연동하면 Claude가 해당 서버의 도구를 자연어 요청에 따라 자동으로 호출한다.

---

## MCP 서버 추가 방법

### CLI로 추가

가장 빠른 방법이다. 터미널에서 아래 형식으로 명령을 실행한다.

```bash
claude mcp add <서버-이름> -- <실행-명령> [인자...]
```

예를 들어 GitHub MCP 서버를 추가하려면 다음과 같이 입력한다.

```bash
claude mcp add github -- npx @anthropic-ai/github-mcp
```

CLI로 추가하면 설정이 `~/.claude/.mcp.json`(글로벌) 또는 `.mcp.json`(프로젝트)에 자동으로 기록된다. 범위를 지정하려면 `-s` 플래그를 사용한다.

```bash
claude mcp add --scope local github -- npx @anthropic-ai/github-mcp   # 현재 프로젝트에만 적용
claude mcp add --scope user github -- npx @anthropic-ai/github-mcp    # 모든 프로젝트에 적용
```

### 설정 파일로 추가

`.mcp.json` 파일을 직접 작성해 서버를 등록할 수 있다. 팀 전체에 동일한 MCP 설정을 배포하거나, 여러 서버를 한 번에 구성할 때 유용하다.

```json
{
  "mcpServers": {
    "github": {
      "command": "npx",
      "args": ["@anthropic-ai/github-mcp"],
      "env": {
        "GITHUB_TOKEN": "${GITHUB_TOKEN}"
      }
    },
    "postgres": {
      "command": "npx",
      "args": ["@modelcontextprotocol/server-postgres"],
      "env": {
        "DATABASE_URL": "${DATABASE_URL}"
      }
    }
  }
}
```

`env` 필드의 `${변수명}` 형식은 실행 환경의 환경 변수를 참조한다. 민감한 값을 파일에 직접 넣지 않고 환경 변수로 관리할 수 있다.

---

## 설정 범위

MCP 서버 설정은 두 범위로 나뉜다.

### 글로벌 설정

```
~/.claude.json
```

모든 프로젝트에서 공통으로 사용할 MCP 서버를 등록한다. 개인 개발 환경에서 자주 쓰는 서버(GitHub, Notion 등)는 글로벌로 등록해두면 프로젝트마다 반복 설정할 필요가 없다.

### 프로젝트 설정

```
.mcp.json         # 프로젝트 루트
.claude/.mcp.json # .claude 디렉토리 안
```

특정 프로젝트에서만 사용할 MCP 서버를 등록한다. 이 파일은 git에 커밋해 팀과 공유한다. 프로젝트에 필요한 데이터베이스 서버나 특수 도구를 여기에 정의한다.

### 환경 변수로 민감한 값 관리

API 토큰, 데이터베이스 연결 문자열 같은 값은 `.mcp.json`에 직접 넣지 않는다. 대신 환경 변수로 관리하고 파일에서는 참조만 한다.

운영 체제의 환경 변수로 설정하는 방법은 다음과 같다.

```bash
# macOS / Linux (.bashrc 또는 .zshrc에 추가)
export GITHUB_TOKEN=ghp_xxxxxxxxxxxxxxxx
export DATABASE_URL=postgresql://user:pass@localhost:5432/mydb
```

```powershell
# Windows (PowerShell 프로필에 추가)
$env:GITHUB_TOKEN = "ghp_xxxxxxxxxxxxxxxx"
$env:DATABASE_URL = "postgresql://user:pass@localhost:5432/mydb"
```

---

## 주요 MCP 서버 소개

### GitHub MCP

GitHub의 이슈, PR(Pull Request, 풀 리퀘스트), 코드 리뷰, 리포지토리 관리를 Claude에서 처리할 수 있다.

```bash
claude mcp add github -- npx @anthropic-ai/github-mcp
```

환경 변수 `GITHUB_TOKEN`에 GitHub Personal Access Token(개인 접근 토큰)을 설정해야 한다. 주요 도구는 다음과 같다.

- `create_issue`: 이슈 생성
- `list_pull_requests`: PR 목록 조회
- `create_review`: PR에 리뷰 추가
- `get_file_contents`: 리포지토리 파일 내용 조회

Claude Code 세션에서 "이 버그를 이슈로 등록해줘"라고 요청하면 GitHub MCP가 자동으로 이슈를 생성한다.

### Playwright MCP

Playwright(플레이라이트)는 웹 브라우저 자동화 라이브러리다. Playwright MCP를 연동하면 Claude가 브라우저를 직접 제어해 웹 페이지를 탐색하고 테스트를 실행할 수 있다.

```bash
claude mcp add playwright -- npx @playwright/mcp@latest
```

주요 도구는 다음과 같다.

- `browser_navigate`: URL로 이동
- `browser_take_screenshot`: 현재 화면 캡처
- `browser_fill_form`: 폼 입력
- `browser_click`: 요소 클릭
- `browser_snapshot`: 접근성 트리(Accessibility Tree) 스냅샷 캡처

"로그인 페이지에서 유효하지 않은 이메일을 입력했을 때 어떤 오류 메시지가 나오는지 확인해줘"와 같은 요청을 Claude Code에서 직접 처리할 수 있다.

### Notion MCP

Notion(노션) 워크스페이스의 페이지와 데이터베이스를 Claude에서 읽고 편집할 수 있다.

```bash
claude mcp add notion -- npx @anthropic-ai/notion-mcp
```

환경 변수 `NOTION_API_KEY`에 Notion Integration Token(통합 토큰)을 설정해야 한다. 주요 도구는 다음과 같다.

- `search_pages`: 페이지 검색
- `get_page`: 특정 페이지 내용 조회
- `create_page`: 새 페이지 생성
- `update_page`: 페이지 내용 수정
- `query_database`: 데이터베이스 레코드 조회

회의록 정리, 작업 현황 문서 업데이트, 팀 위키 작성 등의 작업을 Claude Code 세션에서 처리할 수 있다.

### Context7 MCP

Context7(컨텍스트세븐)은 라이브러리의 공식 문서를 실시간으로 가져오는 MCP 서버다. Claude의 학습 데이터 기준일 이후 변경된 API나 새 버전의 문서를 정확하게 참조할 때 유용하다.

```bash
claude mcp add context7 -- npx @context7/mcp
```

사용 패턴은 두 단계로 이뤄진다.

1. `resolve-library-id`: 라이브러리 이름으로 고유 ID를 조회한다.
2. `query-docs`: 조회한 ID를 사용해 특정 주제의 문서를 가져온다.

예를 들어 "Next.js App Router에서 서버 컴포넌트를 사용하는 방법"을 물으면 Context7이 Next.js 공식 문서에서 최신 정보를 가져와 답변에 반영한다. 빠르게 업데이트되는 프레임워크를 다룰 때 특히 효과적이다.

### PostgreSQL MCP

PostgreSQL(포스트그레스큐엘) 데이터베이스에 쿼리를 실행하고 스키마를 탐색할 수 있다.

```bash
claude mcp add postgres -- npx @modelcontextprotocol/server-postgres
```

환경 변수 `DATABASE_URL`에 연결 문자열을 설정한다. 주요 도구는 다음과 같다.

- `query`: SQL 쿼리 실행
- `list_tables`: 테이블 목록 조회
- `describe_table`: 테이블 스키마 확인

"users 테이블에서 지난 30일 동안 로그인하지 않은 계정 수를 알려줘"처럼 자연어로 데이터를 요청하면 Claude가 SQL을 작성하고 결과를 반환한다.

---

## MCP 서버 관리

등록된 MCP 서버는 다음 명령으로 관리한다.

```bash
# 등록된 서버 목록 확인
claude mcp list

# 특정 서버 상세 정보 확인
claude mcp get github

# 서버 제거
claude mcp remove github
```

### 서버 상태 확인 및 디버깅

MCP 서버가 정상적으로 연결되지 않을 때는 Claude Code 세션 안에서 `/mcp` 명령을 실행한다.

```
/mcp
```

연결된 서버 목록과 각 서버의 상태(연결됨 / 오류)를 표시한다. 오류가 있는 서버는 실행 명령과 환경 변수 설정을 다시 확인한다.

디버깅이 필요하면 `--mcp-debug` 플래그를 사용해 Claude Code를 실행한다.

```bash
claude --mcp-debug
```

MCP 서버와의 통신 로그를 출력해 어떤 요청을 보내고 어떤 응답을 받았는지 추적할 수 있다.

---

## 보안 고려 사항

### 환경 변수로 토큰과 API 키 관리

`.mcp.json` 파일에 API 키나 토큰을 직접 입력하면 git에 커밋될 때 유출 위험이 생긴다. 반드시 환경 변수로 관리하고 파일에서는 `${변수명}` 형식으로 참조한다.

프로젝트의 `.gitignore`에 민감한 값이 담긴 파일(`.env`, `.env.local` 등)이 포함되어 있는지 확인한다.

### MCP 서버의 권한 범위 이해

MCP 서버는 등록한 도구를 통해 외부 서비스에 실제 작업을 수행한다. GitHub MCP가 PR을 생성하거나 이슈를 닫는 것은 실제 리포지토리에 적용된다. 연동하기 전에 다음을 확인한다.

- 해당 서버가 어떤 도구를 제공하는지
- 각 도구가 읽기 전용인지 쓰기 작업을 포함하는지
- 사용하는 토큰의 권한 범위가 최소한으로 설정되어 있는지

GitHub Personal Access Token을 발급할 때 리포지토리 읽기만 필요하다면 쓰기 권한은 부여하지 않는다. 최소 권한 원칙(Principle of Least Privilege)을 적용한다.

### 출처를 알 수 없는 MCP 서버 주의

공식 Anthropic MCP 서버와 잘 알려진 커뮤니티 서버는 대체로 안전하다. 그러나 출처를 확인하기 어려운 MCP 서버를 연동할 때는 소스 코드를 검토하거나 샌드박스(격리된 환경) 환경에서 먼저 테스트한다.

---

## 커스텀 MCP 서버 만들기

자주 사용하는 내부 API나 사내 시스템을 MCP로 래핑하면 Claude Code에서 직접 연동할 수 있다. MCP 서버는 Python(FastMCP 라이브러리)이나 Node.js(공식 MCP SDK)로 구현한다.

### Python(FastMCP)으로 구현하는 예시

```python
from fastmcp import FastMCP

mcp = FastMCP("my-internal-api")

@mcp.tool()
def get_deployment_status(service_name: str) -> dict:
    """배포 상태를 조회한다."""
    # 내부 API 호출 로직
    return {"service": service_name, "status": "running", "version": "1.2.3"}

if __name__ == "__main__":
    mcp.run()
```

### Node.js(MCP SDK)로 구현하는 예시

```typescript
import { Server } from "@modelcontextprotocol/sdk/server/index.js";
import { StdioServerTransport } from "@modelcontextprotocol/sdk/server/stdio.js";

const server = new Server({ name: "my-internal-api", version: "1.0.0" });

server.setRequestHandler("tools/list", async () => ({
  tools: [
    {
      name: "get_deployment_status",
      description: "배포 상태를 조회한다",
      inputSchema: {
        type: "object",
        properties: { service_name: { type: "string" } },
        required: ["service_name"],
      },
    },
  ],
}));

const transport = new StdioServerTransport();
await server.connect(transport);
```

커스텀 MCP 서버 구현의 상세한 내용은 [MCP 공식 문서](https://modelcontextprotocol.io/docs)를 참고한다.

---

## 핵심 정리

### MCP 도입 체크리스트

- [ ] 연동할 서비스의 API 토큰을 발급하고 환경 변수로 설정했는가?
- [ ] `.mcp.json`에 민감한 값이 직접 입력되어 있지 않은가?
- [ ] 글로벌 설정과 프로젝트 설정의 범위를 올바르게 구분했는가?
- [ ] 팀과 공유할 프로젝트 `.mcp.json`을 git에 커밋했는가?
- [ ] `/mcp` 명령으로 서버 연결 상태를 확인했는가?
- [ ] 사용하는 토큰에 최소 필요 권한만 부여했는가?

### 어떤 MCP 서버를 먼저 설정할 것인가

MCP 서버를 처음 도입할 때 모든 서버를 한꺼번에 설정하려 하면 관리가 복잡해진다. 아래 기준으로 우선순위를 정한다.

**1순위 — GitHub MCP**: 코드 작업과 가장 밀접하다. 이슈 확인, PR 리뷰, 브랜치 관리 등을 Claude Code 세션에서 처리할 수 있다. 대부분의 개발자에게 즉각적인 생산성 향상을 제공한다.

**2순위 — Context7 MCP**: 외부 라이브러리 문서를 실시간으로 참조한다. 새 프레임워크를 학습하거나 빠르게 변하는 API를 다룰 때 Claude의 답변 정확도가 눈에 띄게 높아진다.

**3순위 — Playwright MCP**: 프론트엔드 개발이나 웹 테스트 자동화 작업이 많다면 초기부터 도입할 가치가 있다. 수동으로 브라우저를 확인해야 했던 작업을 Claude에게 위임할 수 있다.

**4순위 — 데이터베이스 MCP**: 백엔드 개발이나 데이터 분석 업무가 많다면 PostgreSQL MCP나 유사한 서버를 추가한다. 단, 프로덕션 데이터베이스에 직접 연결하는 것은 위험하다. 개발 또는 읽기 전용 복제본에 연결하는 것을 권장한다.

**5순위 — Notion MCP 등 협업 도구**: 문서화나 프로젝트 관리 작업이 많을 때 추가한다. 코딩 작업보다는 관리 업무에 직접적인 영향을 준다.

---

## 다음 단계

- Claude Code hooks 설정으로 작업 자동화 -> [04-훅-시스템.md](./04-훅-시스템.md)
- 커스텀 MCP 서버 구현 상세 가이드 -> [MCP 공식 문서](https://modelcontextprotocol.io/docs)
- MCP 서버 목록과 커뮤니티 서버 탐색 -> [MCP 서버 레지스트리](https://github.com/modelcontextprotocol/servers)

## 참고 문서

이 문서는 아래 Anthropic 공식 문서를 기반으로 작성되었다.

- [Claude Code MCP](https://docs.anthropic.com/en/docs/claude-code/mcp)
- [Model Context Protocol](https://modelcontextprotocol.io)
- [MCP Servers Registry](https://github.com/modelcontextprotocol/servers)
