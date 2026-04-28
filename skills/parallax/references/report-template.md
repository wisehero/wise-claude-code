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
score_gap: <|Claude 합계 − Codex 합계|>     # 채점표 양측 합계 차이와 반드시 일치
judge: explore_subagent | self              # Step 5 채점자 — 외부 서브에이전트 / 메인 자체
anonymized: true | false                    # Step 4.5 라벨 익명화 적용 여부
---

## 1. 질문
## 2. 양측 답변 요약
   ### Claude의 답변
   ### Codex의 답변
## 3. 비교
   ### 합의점
   ### 차이점
   ### 인용 자동 검증 결과
   ### Red-team 시나리오 비교
## 4. 채점표
## 5. 결론
   (verdict에 따라 형식이 달라짐 — 아래 참조)
```

---

## 예시 1: split (양측 병기) — 점수 차 < 6점

```markdown
---
question: 주문 생성 시 트랜잭션 경계가 어디인지
asked_at: 2026-04-28
scope: /Users/example/repo
verdict: split
score_gap: 2
judge: explore_subagent
anonymized: true
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

#### 한계·반증 (Step 2.5 red-team 시나리오 포함)
- 시나리오 1: 조건 — Facade 레이어에 별도 createOrder 경로가 있다면 결론이 달라짐 / 검증 — Grep "createOrder" 전체.
- 시나리오 2: 조건 — `StockService.decrease()` propagation이 NESTED라면 트랜잭션 경계가 부분 분리됨 / 검증 — `StockService.java` 클래스 레벨 어노테이션 Read.
- 시나리오 3: 조건 — AOP 프록시가 추가로 가로챈다면 메서드 위치가 무관해짐 / 검증 — Grep "@Aspect" 전체.

---

### Codex의 답변

#### 답변
주문 생성의 트랜잭션 경계는 `OrderService.createOrder()`에 선언된 `@Transactional`이다. 호출 컨텍스트에 활성 트랜잭션이 있으면 참여하고 없으면 새로 생성한다(기본 propagation REQUIRED). 본 코드베이스에서는 외부에서 `createOrder()`를 직접 호출하므로 새 트랜잭션이 시작된다.

#### 근거 (코드 인용)
- `OrderService.java:43` — `@Transactional`
- `OrderService.java:45` — 메서드 시그니처
- `OrderController.java:28` — 외부 호출 진입점

#### 한계·반증
- 시나리오 1: 조건 — propagation이 클래스 레벨에서 재정의되면 결론 변경 / 검증 — `OrderService.java:1-30` Read.
- 시나리오 2: 조건 — Async 호출 경로가 있다면 트랜잭션이 분리됨 / 검증 — Grep "@Async" 주변.
- 시나리오 3: 조건 — `OrderController` 외 또 다른 진입점이 있다면 호출 흐름이 다를 수 있음 / 검증 — Grep "OrderService" 사용처 전체.

## 3. 비교

### 합의점
- 트랜잭션 경계 위치: 양측 모두 `OrderService.createOrder()`(`OrderService.java:45`) 메서드의 `@Transactional`(`:43`)이라고 답함.

### 차이점
- **propagation 명시성**: Codex는 기본값 REQUIRED를 명시적으로 언급. Claude는 propagation 자체를 다루지 않음.
- **외부 호출자 언급**: Codex만 `OrderController.java:28`을 진입점으로 언급.
- **중첩 트랜잭션 (StockService)**: Claude만 한계·반증 시나리오로 언급.

### 인용 자동 검증 결과

| 인용 | 출처(라벨링 후) | 추출 줄 (앞 80자) | 상태 |
|---|---|---|---|
| `OrderService.java:43` | A | `@Transactional` | ✅ |
| `OrderService.java:45` | A | `public OrderResult createOrder(...)` | ✅ |
| `OrderService.java:47-62` | A | `Order order = new Order(...); ...` | ✅ |
| `OrderService.java:43` | B | `@Transactional` | ✅ |
| `OrderService.java:45` | B | `public OrderResult createOrder(...)` | ✅ |
| `OrderController.java:28` | B | `orderService.createOrder(req)` | ✅ |

(통과율: A 3/3, B 3/3)

### Red-team 시나리오 비교
- **겹치는 시나리오**: 클래스 레벨 propagation 가정의 한계 (양측 모두 짚음, 표현은 다름)
- **A만 짚은 시나리오**: 중첩 트랜잭션(StockService), AOP 프록시
- **B만 짚은 시나리오**: Async 분리, 추가 진입점

## 4. 채점표

> 채점자: Explore 서브에이전트 (격리 컨텍스트, 라벨 익명화 상태)

| 축 | Claude | Codex | 사유 |
|---|---|---|---|
| 출처 신뢰도 | 9 | 8 | Claude: 매핑률 100%. Codex: propagation 기본값 언급에 일반론 1회. |
| 인용 정확성 | 10 | 10 | 자동 검증 양측 모두 ✅ 100%. |
| 논리 일관성 | 8 | 8 | 양측 흐름 자연스러움. |
| 반증 고려 | 8 | 7 | 양측 시나리오 3개. Claude의 AOP 시나리오가 결론 영향력에서 더 강함. |
| **합계** | **35/40** | **33/40** | |

(score_gap = 2)

## 5. 결론

**판정**: 양측 병기 (점수 차 2점 < 6점 임계값)

점수 차가 작아 단일 답으로 수렴하지 않는다. 두 답변 모두 코드 근거에 기반하고 있으며, 차이점은 강조 지점·범위에 있다. **사용자가 최종 판단**한다.

**핵심 차이를 다시 한 번 강조**:
- Claude는 중첩 트랜잭션(StockService)·AOP의 한계를 더 명시적으로 다룸.
- Codex는 외부 호출자(OrderController)와 propagation 기본값을 더 구체적으로 다룸.

**선택 가이드**:
- "트랜잭션 경계 자체"가 궁금했다면 → 둘 다 같은 결론, 어느 쪽이든 무방.
- "중첩 트랜잭션·AOP의 동작"이 궁금했다면 → Claude.
- "호출 진입점부터의 흐름"이 궁금했다면 → Codex.
```

> 위 예시에서 `score_gap` 값(2)은 채점표 합계 차이(35-33)와 정확히 일치해야 한다. 일치하지 않으면 출력 직전 체크리스트(아래)에서 잡힌다.

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

Codex는 추가로 `OrderController.java:28`을 진입점으로 언급. 채택된 답에는 없는 정보이지만 인접 컨텍스트로 유용. 단, Codex의 propagation 명시 주장은 자동 검증에서 ⚠️로 판명되었으므로 부록에서도 해당 부분은 제외.

(필요 시 사용자가 폐기된 답 전체를 보고 싶다면 "Codex 답 전체 보여줘"로 추가 요청 가능)
```

---

## 예시 3: 외부 채점자 실패 fallback

Step 5의 Explore 서브에이전트가 실패해 메인 Claude가 자체 채점한 경우:

```markdown
---
question: ...
asked_at: 2026-04-28
scope: ...
verdict: split
score_gap: 4
judge: self
anonymized: true
---

> ⚠️ **외부 채점자 호출 실패** — Step 5에서 Explore 서브에이전트가 응답하지 않아 메인 Claude가 자체 채점했습니다. 라벨 익명화는 유지되었으나 메인 컨텍스트가 셔플 매핑을 알고 있으므로 self-grading bias 가능성이 있습니다. 결과를 신뢰도 낮춰 받아주세요.

(이하 일반 split 리포트와 동일)
```

---

## 출력 시 체크리스트

리포트를 출력하기 전에 다음을 점검:

- [ ] frontmatter의 `score_gap`이 채점표 양측 합계 차이와 **정확히** 일치하는가
- [ ] frontmatter의 `verdict`가 점수 차 6점 임계값과 정합하는가
- [ ] frontmatter의 `judge` 필드가 실제 채점 경로(외부 / 자체)와 일치하는가
- [ ] frontmatter의 `anonymized` 필드가 Step 4.5 적용 여부와 일치하는가
- [ ] 양측 답변 본문에 인용 형식(`file_path:line_number`)이 일관되게 쓰였는가
- [ ] 비교 섹션의 "인용 자동 검증 결과"가 Step 4-B의 sed 추출 결과를 반영했는가
- [ ] 비교 섹션에 "Red-team 시나리오 비교"가 포함되어 있는가 (Step 2.5 + Step 3 강제 시나리오)
- [ ] 채점표 상단에 "채점자: explore_subagent | self" 표기가 있는가
- [ ] 채점표의 각 점수 옆에 1줄 사유가 있는가
- [ ] `converged`인 경우 부록에 폐기된 답의 차이점이 1~2 단락으로 보존되어 있는가
- [ ] `split`인 경우 "사용자 최종 판단" 안내가 있는가
- [ ] 더 높은 점수의 답에 명백한 결함이 있다면 `converged` → `split` 강등 사유가 명시되었는가
- [ ] 외부 채점자 실패 시 frontmatter `judge: self` + 상단 경고 박스가 있는가

체크 항목 중 하나라도 실패하면 리포트를 수정한다. 출력 후 사용자에게 "선택 / 추가 분석 / 종료" 중 무엇을 원하는지 묻는다.
