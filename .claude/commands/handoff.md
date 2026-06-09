---
description: 다음 세션이나 다른 에이전트가 작업을 그대로 이어갈 수 있도록 한국어 핸드오프를 작성
argument-hint: "[핸드오프 범위 또는 메모]"
allowed-tools: Read, Grep, Glob, Bash(git status:*), Bash(git diff:*), Bash(git log:*)
---

# Handoff — 세션 재개용 인수인계

`skills/handoff` 스킬의 명시적 호출 경로다. auto-trigger가 발화하지 않을 때 이 커맨드로 강제 호출한다. 워크플로와 출력 형식은 `skills/handoff/SKILL.md`를 따른다.

## 요청

$ARGUMENTS

## 수행 절차

1. `skills/handoff/SKILL.md`를 Read한다.
2. 현재 대화와 작업 상태를 기준으로 핸드오프 범위를 정한다.
3. 필요한 경우 `git status`, `git diff`, 최근 명령/검증 결과, 주요 파일 경로를 확인한다.
4. 별도 언어 요청이 없으면 한국어로 작성한다.
5. `# 핸드오프` 형식으로 출력하고, 다음 세션에서 바로 붙여넣을 수 있는 `재개 프롬프트`를 반드시 포함한다.

## 주의

- 검증하지 않은 내용을 검증했다고 쓰지 않는다.
- 완료 여부가 불확실한 작업은 "완료"가 아니라 "확인 필요"로 둔다.
- 관련 없는 사용자 변경이나 dirty worktree 변경을 되돌리라고 지시하지 않는다.
