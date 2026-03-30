# 고급 프롬프팅 기법

> **난이도**: 중급 | **선행 문서**: [프롬프트 기초](01-prompt-basics.md)
>
> Chain of Thought, Few-shot, XML 태그 활용 등 Claude의 성능을 극대화하는 고급 기법을 다룬다.

---

## 들어가며

기초 프롬프팅을 익혔다면 다음 질문이 생긴다. "더 복잡한 작업을 더 정확하게 처리할 수는 없을까?" 기초 원칙만으로는 한계가 있는 상황이 반드시 생긴다. 여러 단계를 거치는 추론 문제, 특정 형식이 정확히 지켜져야 하는 출력, 대용량 문서 분석 등이 그런 상황이다.

이 문서에서는 그 간극을 메우는 여섯 가지 고급 기법을 다룬다. 각 기법은 독립적으로 사용할 수 있지만, 조합하면 시너지가 생긴다.

---

## 1. Chain of Thought (단계적 사고 유도)

### 왜 필요한가

수학 문제나 복잡한 논리 추론을 Claude에게 맡겼을 때 틀린 답이 나오는 경험을 해봤다면, 그 원인 중 하나는 Claude가 중간 과정을 건너뛰고 답을 한 번에 내놓으려 했기 때문일 수 있다. Chain of Thought(CoT — 사고 연쇄)는 Claude가 결론에 도달하기 전에 중간 추론 과정을 명시적으로 거치도록 유도하는 기법이다.

이 기법은 특히 수학 계산, 논리 퍼즐, 코드 디버깅, 다단계 분석에서 오류율을 크게 줄인다.

### 방법 1: 명시적 요청

가장 단순한 방법은 "단계별로 생각한 다음 답을 제시하라"고 직접 요청하는 것이다.

```
다음 문제를 단계별로 풀어주세요. 각 단계를 명확히 설명한 후 최종 답을 제시해주세요.

문제: 창고에 사과가 240개 있습니다. 오전에 전체의 1/3을 출고했고, 오후에 남은 것의 25%를 추가 출고했습니다. 현재 남은 사과는 몇 개입니까?
```

이 요청에 Claude는 다음과 같이 응답한다.

```
1단계: 오전 출고량 계산
   240 × 1/3 = 80개 출고
   240 - 80 = 160개 남음

2단계: 오후 출고량 계산
   160 × 25% = 40개 출고
   160 - 40 = 120개 남음

최종 답: 120개
```

### 방법 2: XML 태그로 구조화

추론 과정과 최종 답을 명확히 구분해야 할 때는 XML 태그를 사용한다. 이렇게 하면 응답에서 추론 과정과 결론을 별도로 파싱할 수 있다.

```
다음 Python 코드에서 버그를 찾아주세요.
추론 과정은 <reasoning> 태그 안에, 결론은 <answer> 태그 안에 작성해주세요.

<code>
def calculate_average(numbers):
    total = 0
    for n in numbers:
        total += n
    return total / len(numbers)

result = calculate_average([])
print(result)
</code>
```

Claude의 응답:

```xml
<reasoning>
함수를 호출할 때 빈 리스트 []를 전달하고 있다.
함수 내부에서 len(numbers)로 나누는데, 빈 리스트의 길이는 0이다.
0으로 나누면 ZeroDivisionError가 발생한다.
for 루프는 빈 리스트에서 실행되지 않으므로 total은 0인 상태에서
return total / len(numbers)가 실행된다.
</reasoning>

<answer>
버그: 빈 리스트를 전달할 때 ZeroDivisionError 발생.
수정 방법: 함수 진입 시 리스트가 비어 있는지 확인하고 적절한 값을 반환하거나 예외를 발생시킨다.

def calculate_average(numbers):
    if not numbers:
        return 0  # 또는 raise ValueError("빈 리스트는 평균을 계산할 수 없습니다")
    total = 0
    for n in numbers:
        total += n
    return total / len(numbers)
</answer>
```

### 핵심 정리

- 수학·논리 문제에서 정확도를 높이고 싶을 때 사용한다.
- "단계별로 설명한 후 답변을 제공하세요"라는 한 문장만 추가해도 효과가 있다.
- XML 태그를 쓰면 추론 과정과 결론을 별도로 처리할 수 있다.

---

## 2. Few-shot 프롬프팅

### 원리

Few-shot(소수 예시) 프롬프팅은 원하는 입력-출력 패턴을 2~5개의 예시로 보여주는 기법이다. Claude는 예시를 보고 형식, 톤, 세부 규칙을 자동으로 추론한다. 복잡한 지시문을 길게 쓰는 것보다 예시를 몇 개 보여주는 것이 더 효과적인 경우가 많다.

### 예시 1: 감정 분석 형식 통일

형식 없이 요청했을 때는 응답 구조가 들쑥날쑥하다. Few-shot을 쓰면 형식을 고정할 수 있다.

```
고객 리뷰를 분석하여 감정, 주요 키워드, 개선 제안을 추출해주세요.

<example>
입력: "배송이 생각보다 빠르게 왔어요. 근데 포장이 좀 허술했고 제품 색이 사진과 달라요."
출력:
- 감정: 혼합 (긍정 + 부정)
- 키워드: 배송속도(긍정), 포장(부정), 색상 불일치(부정)
- 개선 제안: 포장 강화, 제품 이미지 정확도 향상
</example>

<example>
입력: "완벽합니다. 품질도 좋고 가격도 합리적이에요. 재구매 의사 있습니다."
출력:
- 감정: 긍정
- 키워드: 품질(긍정), 가격(긍정), 재구매 의사
- 개선 제안: 없음
</example>

이제 다음 리뷰를 분석해주세요:
"앱 자체는 편리한데 광고가 너무 많이 뜨고, 결제 오류가 두 번 있었어요."
```

### 예시 2: 데이터 추출 형식 지정

비정형 텍스트에서 구조화된 데이터를 뽑아낼 때 Few-shot은 특히 강력하다.

```
다음 형식으로 인물 정보를 추출해주세요.

<example>
입력: "이지은 부장은 마케팅팀에서 SNS 전략을 총괄하고 있으며, 010-1234-5678로 연락 가능합니다."
출력: {"name": "이지은", "title": "부장", "department": "마케팅팀", "contact": "010-1234-5678"}
</example>

<example>
입력: "개발팀 김민준 선임 연구원입니다. 이메일은 minjun@company.com입니다."
출력: {"name": "김민준", "title": "선임 연구원", "department": "개발팀", "contact": "minjun@company.com"}
</example>

입력: "영업1팀 박서연 과장. 내선번호 3421."
```

### 예시 선택 원칙

- **관련성**: 실제 입력과 유사한 유형의 예시를 고른다.
- **다양성**: 엣지 케이스(데이터가 없거나 불완전한 경우)를 예시에 포함한다.
- **일관성**: 모든 예시에서 출력 형식을 동일하게 유지한다.
- **개수**: 2~5개가 적절하다. 너무 많으면 컨텍스트를 낭비하고 오히려 혼란을 줄 수 있다.

### 핵심 정리

- 복잡한 형식 지시보다 예시 2~3개가 더 효과적인 경우가 많다.
- 엣지 케이스를 예시에 포함하면 예외 상황 처리 품질이 높아진다.
- `<example>` 태그로 예시를 명확히 구분한다.

---

## 3. XML 태그 활용

### 구조화 도구로서의 XML 태그

프롬프트가 길어지면 Claude가 지시사항과 데이터, 예시를 혼동할 수 있다. XML 태그(tag — 꺾쇠괄호로 감싼 레이블)는 이 혼동을 없애는 구조화 도구다. Claude는 XML 구조를 잘 이해하며, 이를 통해 프롬프트의 각 영역을 명확히 구분한다.

### 추천 태그 세트

| 태그 | 용도 |
|---|---|
| `<task_definition>` | 수행할 작업의 목적과 범위 |
| `<context>` | 배경 정보, 사용 환경 |
| `<input>` | 처리할 실제 데이터 |
| `<constraints>` | 금지사항, 제약조건, 분량 제한 |
| `<output_format>` | 원하는 출력 구조 |
| `<example>` | 입출력 예시 |

### 실전 예시: 코드 리뷰 프롬프트

XML 태그 없이 긴 프롬프트를 쓰면 Claude가 어느 부분이 지시이고 어느 부분이 코드인지 혼동할 수 있다. 태그를 쓰면 이 문제가 해결된다.

```xml
<task_definition>
다음 Python 코드를 리뷰하고 개선안을 제시해주세요.
</task_definition>

<context>
이 코드는 스타트업의 프로덕션 환경에서 실행됩니다.
트래픽이 갑자기 늘어날 수 있으며, 팀 구성원의 Python 숙련도가 다양합니다.
</context>

<constraints>
- 로직을 바꾸지 말고 코드 품질만 개선해주세요
- 외부 라이브러리를 추가하지 마세요
- 각 개선 항목마다 이유를 한 줄로 설명해주세요
</constraints>

<output_format>
1. 발견된 문제점 목록 (심각도: 상/중/하)
2. 개선된 코드
3. 개선 사항 요약
</output_format>

<input>
def get_user(id):
    db = connect_database()
    result = db.query("SELECT * FROM users WHERE id=" + str(id))
    return result
</input>
```

### 중첩 태그 활용

여러 문서를 다룰 때는 태그를 중첩해서 각 문서를 구분한다.

```xml
<documents>
  <document index="1">
    <title>2024년 4분기 실적 보고서</title>
    <content>
    [보고서 내용]
    </content>
  </document>
  <document index="2">
    <title>2023년 4분기 실적 보고서</title>
    <content>
    [보고서 내용]
    </content>
  </document>
</documents>

두 보고서를 비교하여 매출 증감률과 주요 변화 요인을 분석해주세요.
```

### 핵심 정리

- 프롬프트가 500자를 넘거나 데이터와 지시사항이 섞이면 XML 태그를 쓴다.
- 태그 이름은 용도를 명확히 표현하는 단어를 쓴다.
- 여러 문서를 처리할 때는 중첩 태그로 각 문서를 구분한다.

---

## 4. 프롬프트 체이닝 (Prompt Chaining)

### 개념

프롬프트 체이닝(Prompt Chaining — 프롬프트 연쇄)은 복잡한 작업을 여러 개의 순차적 API 호출로 분해하는 기법이다. 각 단계의 출력이 다음 단계의 입력이 된다. 한 번의 긴 프롬프트로 모든 것을 처리하는 것보다 각 단계에서 품질을 검증할 수 있다는 장점이 있다.

전형적인 패턴은 드래프트 생성 → 검토 → 개선이다.

### Python anthropic SDK 예시

```python
import anthropic

client = anthropic.Anthropic()

# 1단계: 초안 작성
draft_response = client.messages.create(
    model="claude-sonnet-4-6",
    max_tokens=2000,
    messages=[
        {
            "role": "user",
            "content": """다음 주제로 블로그 포스트 초안을 작성해주세요.
            주제: Python에서 비동기 프로그래밍을 사용해야 하는 시점
            분량: 600~800단어
            독자: 중급 Python 개발자"""
        }
    ]
)

draft = draft_response.content[0].text

# 2단계: 초안 검토
review_response = client.messages.create(
    model="claude-sonnet-4-6",
    max_tokens=1000,
    messages=[
        {
            "role": "user",
            "content": f"""다음 블로그 포스트 초안을 검토하고 개선이 필요한 부분을 지적해주세요.
            검토 기준: 기술적 정확성, 흐름, 중급 독자 적합성

            <draft>
            {draft}
            </draft>

            개선 필요 항목만 간결하게 나열해주세요."""
        }
    ]
)

review = review_response.content[0].text

# 3단계: 최종 개선
final_response = client.messages.create(
    model="claude-sonnet-4-6",
    max_tokens=2000,
    messages=[
        {
            "role": "user",
            "content": f"""다음 초안을 검토 의견을 반영하여 개선해주세요.

            <draft>
            {draft}
            </draft>

            <review_feedback>
            {review}
            </review_feedback>"""
        }
    ]
)

final_post = final_response.content[0].text
```

### 체이닝이 효과적인 상황

- 중간 결과를 사람이 검토해야 하는 경우
- 각 단계에서 다른 모델이나 설정을 쓰는 경우 (예: 초안은 Haiku, 검토는 Opus)
- 긴 작업에서 오류 발생 시 전체를 재실행하지 않고 특정 단계만 재처리하고 싶은 경우

### 핵심 정리

- 복잡한 작업을 명확한 경계를 가진 단계로 쪼갠다.
- 각 단계의 출력을 다음 단계의 입력에 그대로 넣는다.
- 단계별 검증 포인트를 두면 최종 품질이 높아진다.

---

## 5. Prefilling (응답 시작 지정)

### 개념

Prefilling(응답 선행 지정)은 API 호출 시 assistant 메시지의 시작 부분을 미리 지정하는 기법이다. Claude는 그 이후부터 응답을 이어서 생성한다. JSON 출력 강제, 특정 형식 유도, 불필요한 서두 제거에 유용하다.

### API 예시

```python
import anthropic

client = anthropic.Anthropic()

response = client.messages.create(
    model="claude-sonnet-4-6",
    max_tokens=500,
    messages=[
        {
            "role": "user",
            "content": "다음 제품 설명에서 이름, 가격, 카테고리를 추출해주세요: '프리미엄 무선 이어폰 AX-500, 129,000원, 오디오/음향기기'"
        },
        {
            "role": "assistant",
            "content": "{"  # JSON 형식 시작을 강제
        }
    ]
)
```

이 방식을 쓰면 Claude의 응답이 `{`에 이어서 시작되므로 반드시 JSON 형식으로 출력된다. "물론이죠, 추출해 드리겠습니다" 같은 서두 없이 바로 데이터가 나온다.

### 활용 패턴

**JSON 출력 강제**

```python
# assistant 시작을 { 로 설정하면 JSON만 나온다
{"role": "assistant", "content": "{"}
```

**특정 섹션으로 바로 시작**

```python
# 분석 결과만 바로 원할 때
{"role": "assistant", "content": "분석 결과:\n\n"}
```

**긍정적 응답 유도**

```python
# 거절 가능성이 있는 요청에서 수행을 유도할 때
{"role": "assistant", "content": "네, 진행하겠습니다."}
```

주의: Prefilling은 Claude.ai 채팅 인터페이스에서는 사용할 수 없고 API 호출에서만 작동한다. 또한 Claude의 판단을 우회하는 용도로 남용하면 의도치 않은 결과를 낳을 수 있다.

### 핵심 정리

- JSON이나 특정 형식의 출력을 강제할 때 가장 유용하다.
- 서두 없이 바로 본론으로 시작하게 만든다.
- API 전용 기법이며 채팅 인터페이스에서는 작동하지 않는다.

---

## 6. 장문서 처리 전략

### 20,000 토큰 이상의 입력 최적화

Claude는 최대 1M 토큰의 컨텍스트를 처리할 수 있지만, 긴 문서를 다룰 때는 몇 가지 전략을 쓰면 성능이 크게 향상된다.

Anthropic 공식 문서에 따르면, 긴 문서를 프롬프트에 넣을 때는 **문서를 맨 위에, 질문이나 지시사항을 맨 아래에** 배치해야 한다. 이 순서만 바꿔도 성능이 30% 향상되는 경우가 있다.

```
# 잘못된 순서 (지시사항이 먼저)
다음 계약서에서 해지 조항을 찾아주세요.

[500페이지 계약서 내용]

# 올바른 순서 (문서가 먼저)
[500페이지 계약서 내용]

위 계약서에서 해지 조항을 찾아주세요.
```

### 관련 인용구 먼저 추출 전략

긴 문서에서 특정 정보를 찾을 때 바로 답을 요구하는 것보다 관련 인용구를 먼저 추출하게 하면 정확도가 올라간다. 이 방법은 Chain of Thought와 같은 원리다.

```
다음 계약서를 바탕으로 질문에 답해주세요.

[계약서 내용]

질문에 답하기 전에, 먼저 <quotes> 태그 안에 관련 조항을 그대로 인용해주세요.
그 다음 <answer> 태그 안에 답을 작성해주세요.

질문: 계약 해지 시 위약금 조건은 무엇인가요?
```

### 여러 문서 관리: documents 태그

여러 문서를 동시에 다룰 때는 `<documents>` 태그로 구조화한다. 각 문서에 인덱스와 제목을 붙이면 Claude가 참조할 문서를 혼동하지 않는다.

```xml
<documents>
  <document index="1">
    <source>privacy_policy_v2.pdf</source>
    <content>
    [개인정보처리방침 내용]
    </content>
  </document>
  <document index="2">
    <source>terms_of_service_v3.pdf</source>
    <content>
    [이용약관 내용]
    </content>
  </document>
</documents>

위 두 문서에서 사용자 데이터 삭제 권리에 관한 조항을 각각 찾아 비교해주세요.
인용 시 문서 번호를 명시해주세요.
```

### 핵심 정리

- 긴 문서는 프롬프트 맨 위에, 질문은 맨 아래에 배치한다.
- 바로 답을 요구하기 전에 관련 인용구를 먼저 추출하게 한다.
- 여러 문서는 `<documents>` 태그와 인덱스로 구분한다.

---

## 7. 핵심 정리

### 기법별 한 줄 요약

| 기법 | 언제 쓰는가 |
|---|---|
| Chain of Thought | 수학, 논리 추론, 다단계 분석에서 오류를 줄이고 싶을 때 |
| Few-shot | 형식, 톤, 스타일을 정확히 통일해야 할 때 |
| XML 태그 | 프롬프트가 길거나 데이터와 지시가 섞여 있을 때 |
| 프롬프트 체이닝 | 작업이 복잡하고 중간 결과를 검증해야 할 때 |
| Prefilling | JSON 등 특정 형식 출력을 API 수준에서 강제할 때 |
| 장문서 처리 | 20,000 토큰 이상의 문서를 정확하게 분석해야 할 때 |

### 언제 어떤 기법을 써야 하는가 — 빠른 참조

**형식이 중요한 경우**: Few-shot + XML 태그 조합이 가장 효과적이다. 예시로 형식을 보여주고 태그로 입출력을 구분한다.

**정확도가 중요한 경우**: Chain of Thought를 기본으로 쓴다. 특히 수치 계산이나 논리 추론에서 "단계별로"라는 한 마디가 오류율을 크게 낮춘다.

**복잡한 작업 전체를 자동화하는 경우**: 프롬프트 체이닝으로 단계를 분리하고 각 단계의 출력을 검증한다. 한 번의 프롬프트로 해결하려 하지 않는다.

**API로 구조화된 데이터를 뽑아내는 경우**: Prefilling으로 JSON 형식을 강제한다. 응답 파싱 코드가 단순해진다.

**대용량 문서를 분석하는 경우**: 문서를 먼저, 질문을 나중에 배치하고 인용 먼저 추출 전략을 쓴다.

이 기법들은 서로 배타적이지 않다. 예를 들어 여러 계약서를 분석하는 작업이라면 `documents` 태그(장문서 처리) + 인용 추출(Chain of Thought) + 출력 형식 지정(XML 태그)을 한 프롬프트에 조합할 수 있다. 각 기법을 단독으로 익힌 뒤 상황에 맞게 조합하는 것이 숙련의 방향이다.

---

## 다음 단계

이 문서에서 다룬 기법들은 모두 단일 대화 턴 안에서 작동하는 기법들이다. 시스템 프롬프트를 활용해 Claude의 행동과 성격을 정의하는 방법은 `03-system-prompt-design.md`에서, Claude Code의 실전 워크플로우는 `claude-code/06-workflow.md`에서 다룬다.

## 참고 문서

이 문서는 아래 Anthropic 공식 문서를 기반으로 작성되었다.

- [Chain of Thought](https://docs.anthropic.com/en/docs/build-with-claude/prompt-engineering/chain-of-thought)
- [Use Examples (Multishot)](https://docs.anthropic.com/en/docs/build-with-claude/prompt-engineering/multishot-prompting)
- [Use XML Tags](https://docs.anthropic.com/en/docs/build-with-claude/prompt-engineering/use-xml-tags)
- [Prompt Chaining](https://docs.anthropic.com/en/docs/build-with-claude/prompt-engineering/chain-prompts)
- [Prefill Claude's Response](https://docs.anthropic.com/en/docs/build-with-claude/prompt-engineering/prefill-claudes-response)
- [Long Context Tips](https://docs.anthropic.com/en/docs/build-with-claude/prompt-engineering/long-context-tips)
