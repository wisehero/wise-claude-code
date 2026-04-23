---
description: 작업을 먼저 수행한 뒤 codex CLI에게 2차 의견을 받아 비교 선택지를 제시
argument-hint: <작업 설명>
allowed-tools: Bash(codex exec:*), Read, Edit, Write, Grep, Glob
---

# codex 2차 의견 리뷰 모드

사용자가 이 커맨드로 넘긴 작업을 수행한 뒤, `codex` CLI에게 같은 문제에 대한 의견을 받아 두 접근을 비교해 사용자가 고를 수 있도록 제시한다.

## 작업 내용
$ARGUMENTS

## 수행 절차

1. **내 작업 수행**: 위 작업을 평소대로 수행한다. 파일 수정이 필요하면 수정하고, 설계 질문이면 설계안을 작성한다. 이 단계는 평소처럼 도구를 자유롭게 사용해 완결까지 간다.

2. **수행 요약 작성**: 내가 한 작업을 다음 형태로 간결히 정리한다.
   - 문제 이해
   - 내가 택한 접근 (핵심 결정과 이유)
   - 변경한 파일/코드 혹은 제안한 설계의 요지
   - 트레이드오프 / 남은 의문점

3. **codex에게 2차 의견 요청**: 아래 형태로 `codex exec`를 호출한다. `workspace-write` 샌드박스로 실행하되, codex가 직접 파일을 수정하지 않도록 프롬프트에서 명시적으로 금지한다 (리뷰 의견만 받는다).

   ```bash
   codex exec --sandbox read-only --skip-git-repo-check "$(cat <<'EOF'
   You are giving a second opinion on another AI assistant's approach. DO NOT modify any files. Only respond with your review in Korean.

   ## 원래 작업
   <여기에 $ARGUMENTS 원문>

   ## 다른 AI가 택한 접근 (요약)
   <2단계에서 작성한 요약>

   ## 요청
   1. 이 접근의 강점과 약점을 간단히
   2. 네가 같은 작업을 받았다면 어떻게 다르게 했을지 (다르게 할 게 없으면 "동의"라고 답)
   3. 반드시 짚어야 할 리스크나 놓친 점
   500자 이내, bullet 형식.
   EOF
   )"
   ```

   > 주의: `$ARGUMENTS`와 요약은 heredoc 안에 그대로 박지 말고, 값이 주입된 최종 문자열을 만들어 넘긴다. 특수문자 escape에 유의한다. 길면 `/tmp/codex-review-$$.txt`에 쓰고 `codex exec - < /tmp/codex-review-$$.txt` 형태로 stdin 주입도 가능하다.

4. **비교 선택지 제시**: 다음 형태로 사용자에게 보여준다.

   ```
   ## 내 접근 (Claude)
   <핵심 요약 3~5줄>

   ## codex 2차 의견
   <codex 출력 그대로 또는 핵심만>

   ## 선택지
   A. 내 접근 그대로 진행 (이미 수행한 변경 유지)
   B. codex 의견 반영해 수정
   C. 하이브리드 — 특정 부분만 수정 (어느 부분을 반영할지 사용자가 지정)
   D. 둘 다 폐기하고 다시 논의
   ```

5. **사용자 응답 대기**: 선택 후 해당 방향으로 진행한다. 사용자가 B/C를 택하면 이미 적용한 변경을 되돌리거나 조정할지 먼저 확인한다.

## 주의

- codex 호출이 실패하면 (network / auth / timeout) 에러 원문과 함께 A단계 결과만 제시하고 사용자에게 codex 없이 계속할지 묻는다.
- codex가 파일을 수정하려 하면 즉시 그 부분을 무시하고 의견 텍스트만 추출한다.
- 이미 수행한 변경이 크면 (많은 파일 수정 등) codex를 부르기 전에 커밋을 권한다 — B/C 선택 시 롤백이 쉬워진다.
