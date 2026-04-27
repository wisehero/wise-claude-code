# Parallax 리포트 템플릿

이 문서는 Step 7에서 출력할 리포트의 **전체 형식**을 보여준다. SKILL.md의 인라인 명세(Step 7)와 정확히 일치한다 — 둘이 어긋나면 안 된다.

> **사용 시점**: SKILL.md의 Step 7에서 이 문서를 본다. 인라인 명세와 형식이 다르면 **인라인 명세가 우선**이고 이 템플릿을 갱신해야 한다.

---

## 스키마 요약

```yaml
---
question: <Step 1에서 확정된 질문>
asked_at: YYYY-MM-DD
scope: <working directory 경로>
verdict: converged | split
score_gap: <|Claude 합계 − Codex 합계|>
---

## 1. 질문
## 2. 양측 답변 요약
   ### Claude의 답변
   ### Codex의 답변
## 3. 비교
   ### 합의점
   ### 차이점
   ### 인용 정확성 검증
## 4. 채점표
## 5. 결론
   (verdict에 따라 형식이 달라짐 — 아래 참조)
```

---

## 예시 1: split (양측 병기) — 점수 차 < 6점

```markdown
---
question: 주문 생성 시 트랜잭션 경계가 어디인지
asked_at: 2026-04-27
scope: /Users/example/repo
verdict: split
score_gap: 3
---

## 1. 질문

**원문**: "주문 생성 시 트랜잭션 경계가 어디인지 Claude랑 Codex 둘 다 봐줘"

(해석 없이 그대로 사용)

## 2. 양측 답변 요약

### Claude의 답변

#### 답변
주문 생성 시 트랜잭션 경계는 `OrderService.createOrder()` 메서드에 부착된 `@Transactional`로 설정된다. 메서드 시작 시 트랜잭션이 열리고, 메서드 정상 종료 시 commit, RuntimeException 발생 시 rollback 된다.

#### 근거 (코드 인용)
- `OrderService.java:43` — `@Transactional` 애노테이션
- `OrderService.java:45` — `createOrder` 메서드 시그니처
- `OrderService.java:47-62` — 메서드 본문, 내부에서 `OrderRepository.save()` 호출

#### 한계·반증
`createOrder()` 내부에서 호출되는 `StockService.decrease()`도 `@Transactional`이지만 propagation 설정이 명시되지 않아 기본값(REQUIRED)으로 동일 트랜잭션에 참여하는 것으로 추정된다. 명시적 설정이 없으므로 실제 propagation을 단정하지는 않음.

---

### Codex의 답변

#### 답변
주문 생성의 트랜잭션 경계는 `OrderService.createOrder()`에 선언된 `@Transactional(propagation = REQUIRED)`이다. 이 설정은 호출 컨텍스트에 활성 트랜잭션이 있으면 참여하고, 없으면 새로 생성한다. 본 코드베이스에서는 외부에서 `createOrder()`를 직접 호출하므로 새 트랜잭션이 시작된다.

#### 근거 (코드 인용)
- `OrderService.java:43` — `@Transactional(propagation = Propagation.REQUIRED)`
- `OrderService.java:45` — 메서드 시그니처
- `OrderController.java:28` — 외부 호출 진입점

#### 한계·반증
특이사항 없음.

## 3. 비교

### 합의점
- 트랜잭션 경계 위치: 양측 모두 `OrderService.createOrder()`(`OrderService.java:45`) 메서드의 `@Transactional`(`:43`)이라고 답함.

### 차이점
- **propagation 명시성**: Codex는 `Propagation.REQUIRED`가 명시적으로 선언되어 있다고 주장. Claude는 명시적 선언이 없어 추정이라고 답함. → 검증 필요.
- **외부 호출자 언급**: Codex만 `OrderController.java:28`을 진입점으로 언급. Claude는 다루지 않음.
- **중첩 트랜잭션 (StockService)**: Claude만 언급. Codex는 다루지 않음.

### 인용 정확성 검증
- Claude 인용 3건 → 3건 모두 존재·연결·정확 통과.
- Codex 인용 3건 → 2건 통과. 1건 부정확: `OrderService.java:43`의 실제 코드는 `@Transactional`만 있고 `propagation = REQUIRED` 명시는 없음. Codex가 기본값을 명시 선언으로 잘못 옮긴 것으로 판단.

## 4. 채점표

| 축 | Claude | Codex | 사유 |
|---|---|---|---|
| 출처 신뢰도 | 9 | 8 | Claude: 모든 주장에 인용. Codex: REQUIRED 명시 부분이 추정 → 인용 부족 |
| 인용 정확성 | 9 | 6 | Claude: 3/3 통과. Codex: 2/3 통과 (`:43`의 propagation 부정확) |
| 논리 일관성 | 8 | 8 | 양측 모두 흐름 자연스러움 |
| 반증 고려 | 8 | 3 | Claude: StockService 중첩 트랜잭션의 한계 짚음. Codex: "특이사항 없음" — 실제로는 propagation 가정의 한계 존재 |
| **합계** | **34/40** | **25/40** | |

## 5. 결론

> 점수 차 9점 ≥ 6점(15%) → `verdict: converged` 채택 후 검증 결과 — 그러나 이 예시는 split를 보여주기 위한 것이므로 아래 split 예시로 본다.

(이 예시는 점수 차 9점이라 실제로는 converged가 맞다. 양측 병기 설명을 위한 임의 예시이므로 실제 출력에서는 아래 "예시 2"의 형식을 따른다.)
```

> **위 예시의 score_gap이 9인데 frontmatter에 3으로 적힌 것은 이 문서가 형식 설명용이기 때문**. 실제 리포트에서는 score_gap이 frontmatter와 채점표 합계 차이가 일치해야 한다. 이 템플릿을 본 LLM은 자기 출력에서 둘을 일치시킬 것.

---

## 예시 2: converged (수렴) — 점수 차 ≥ 6점

`verdict: converged`일 때 섹션 5는 다음 형식을 따른다:

```markdown
## 5. 결론

**채택**: Claude의 답변 (합계 34/40)

**채택 사유**:
- 출처 신뢰도와 인용 정확성에서 명백한 우위 (각 +1, +3)
- 반증 고려에서 큰 차이 (8 vs 3) — Codex의 "특이사항 없음"이 실제로는 propagation 가정의 한계를 놓침

**채택된 답** (Claude 답변 본문 그대로 재현):

> 주문 생성 시 트랜잭션 경계는 `OrderService.createOrder()` 메서드에 부착된 `@Transactional`로 설정된다. ... (이하 Claude 답변 그대로)

**부록 — 폐기된 답의 차이점**:

Codex는 추가로 `OrderController.java:28`을 진입점으로 언급. 채택된 답에는 없는 정보이지만 인접 컨텍스트로 유용. 단, Codex의 propagation 명시 주장은 인용 검증에서 부정확으로 판명되었으므로 부록에 포함하지 않음.

(필요 시 사용자가 폐기된 답 전체를 보고 싶다면 "Codex 답 전체 보여줘"로 추가 요청 가능)
```

---

## 예시 3: split (양측 병기) — 점수 차 < 6점

`verdict: split`일 때 섹션 5는 다음 형식을 따른다:

```markdown
## 5. 결론

**판정**: 양측 병기 (점수 차 3점 < 6점 임계값)

점수 차가 작아 단일 답으로 수렴하지 않는다. 두 답변 모두 코드 근거에 기반하고 있으며, 차이점은 강조 지점·범위에 있다. **사용자가 최종 판단**한다.

**핵심 차이를 다시 한 번 강조**:
- Claude는 중첩 트랜잭션(StockService)의 한계를 더 명시적으로 다룸
- Codex는 외부 호출자(OrderController)와 propagation을 더 구체적으로 다룸 (단, propagation 인용에 부정확 1건)

**선택 가이드**:
- "트랜잭션 경계 자체"가 궁금했다면 → 둘 다 같은 결론, 어느 쪽이든 무방
- "중첩 트랜잭션의 동작"이 궁금했다면 → Claude
- "호출 진입점부터의 흐름"이 궁금했다면 → Codex (단, propagation 부분은 부정확)
```

---

## 출력 시 체크리스트

리포트를 출력하기 전에 다음을 점검:

- [ ] frontmatter의 `score_gap`이 채점표의 양측 합계 차이와 일치하는가
- [ ] frontmatter의 `verdict`가 점수 차 6점 임계값과 정합하는가
- [ ] 양측 답변 본문에 인용 형식(`file_path:line_number`)이 일관되게 쓰였는가
- [ ] 비교 섹션의 "인용 정확성 검증"이 실제로 Read로 확인한 결과인가 (추정 금지)
- [ ] 채점표의 각 점수 옆에 1줄 사유가 있는가
- [ ] converged인 경우 부록에 폐기된 답의 차이점이 1~2 단락으로 보존되어 있는가
- [ ] split인 경우 "사용자 최종 판단" 안내가 있는가
- [ ] 더 높은 점수의 답에 명백한 결함이 있다면 converged → split 강등 사유가 명시되었는가

체크 항목 중 하나라도 실패하면 리포트를 수정한다. 출력 후 사용자에게 "선택 / 추가 분석 / 종료" 중 무엇을 원하는지 묻는다.
