---
name: humanizer
description: AI가 생성한 한국어/영어 텍스트에서 AI투(AI 냄새)를 제거하고 자연스러운 사람 문체로 교정하는 스킬. "AI투 제거해줘", "사람처럼 다듬어줘", "AI 냄새 빼줘", "humanize 해줘", "문체 교정해줘" 같은 요청에 사용. "이거 AI가 쓴 티 나는데", "좀 자연스럽게 바꿔줘", "글이 너무 딱딱해", "AI 느낌 좀 없애줘" 같은 간접적 요청에도 반드시 사용한다. 단, 단순 맞춤법 교정이나 번역 요청에는 사용하지 않는다.
---

# AI 글쓰기 패턴 카탈로그

이 카탈로그는 AI가 생성한 텍스트에서 자주 나타나는 패턴을 분류한 것이다.
각 패턴에는 우선순위(P1~P3)가 있으며, P1이 가장 먼저 수정해야 할 항목이다.

- **P1**: 즉시 수정 — 읽는 사람이 "AI가 쓴 글"이라고 바로 느끼는 패턴
- **P2**: 권장 수정 — 글의 자연스러움을 떨어뜨리는 패턴
- **P3**: 선택 수정 — 사소하지만 신경 쓰이는 패턴

---

## 한국어 패턴 (K)

### K1. 도입부 상투어 [P1]

**감지 표현:** "오늘날", "현대 사회에서", "~에 대해 알아보겠습니다", "최근 들어", "급변하는 디지털 시대에"

**원칙:** 전부 삭제하고 본론으로 바로 시작.

**수정 전:**
> 오늘날 빠르게 변화하는 디지털 시대에 접어들면서 AI 코딩 도구에 대한 관심이 날로 높아지고 있습니다. 이번 글에서는 AI 코딩 도구의 다양한 측면에 대해 자세히 알아보겠습니다.

**수정 후:**
> AI 코딩 도구가 생산성을 올려준다는 건 반쯤 맞다.

---

### K2. 과장 수식어 [P1]

**감지 표현:** 혁신적인, 획기적인, 체계적인, 효과적인, 핵심적인, 다양한 (의미 없이 반복), 심층적인, 종합적인

**원칙:** 구체적인 설명으로 교체하거나 삭제. "다양한 기능"→"자동 완성, 리팩토링, 테스트 생성 기능".

**수정 전:**
> AI 코딩 도구는 혁신적인 기술을 통해 개발자들의 생산성을 획기적으로 향상시키는 핵심적인 역할을 하고 있다.

**수정 후:**
> AI 코딩 도구를 쓰면 보일러플레이트 작성이 빨라진다.

---

### K3. 회피 어미 [P1]

**감지 표현:** "~라고 할 수 있습니다", "~것으로 보입니다", "~하다는 점에서 의의가 있다", "~인 것으로 알려져 있다"

**원칙:** 확신이 있으면 단정, 없으면 근거를 제시.

---

### K4. "이를 통해" 연쇄 [P1]

**감지 표현:** "이를 통해", "이를 바탕으로", "이를 기반으로", "이러한 관점에서 볼 때"

**원칙:** 앞 문장과 합치거나, 구체적인 연결어로 교체.

---

### K5. 무의미한 중요성 강조 [P1]

**감지 표현:** "~의 중요성은 아무리 강조해도 지나치지 않습니다", "~에서 매우 중요한 역할을 하고 있습니다"

**원칙:** 중요하다고 말하지 말고, 왜 중요한지 보여주기.

---

### K6. "~뿐만 아니라" [P2]

**감지 표현:** "~뿐만 아니라 ~도", "~뿐 아니라 ~까지"

**원칙:** 나열로 교체하거나 문장을 분리.

---

### K7. 자문자답 [P1]

**감지 표현:** "그렇다면 왜~", "그렇다면 어떻게~", "왜 그럴까요?", "이유는 간단합니다"

**원칙:** 질문 없이 바로 설명.

---

### K8. 셋 법칙 (삼분 구조) [P2]

**문제:** AI가 항상 세 가지로 나누는 경향.

**감지 표현:** "크게 세 가지로 나눌 수 있습니다", "첫째, 둘째, 셋째"

**원칙:** 실제 내용에 맞는 개수로 조정. 두 개면 두 개, 다섯 개면 다섯 개.

---

### K9. 균형잡기 함정 [P2]

**감지 표현:** "한편으로는~, 다른 한편으로는~", "물론 ~도 있지만"

**원칙:** 입장을 정하고 근거를 대거나, 양측의 구체적인 차이를 설명.

---

### K10. 결론 상투어 [P1]

**감지 표현:** "결론적으로", "앞으로 ~할 것입니다", "~가 기대됩니다", "~에 주목할 필요가 있습니다", "~해야 할 것입니다"

**원칙:** 구체적인 다음 행동으로 교체하거나 삭제.

---

### K11. 대화형 잔류물 [P1]

**감지 표현:** "도움이 되셨길 바랍니다", "궁금한 점이 있으시면", "이상으로 ~에 대해 알아보았습니다"

**원칙:** 전부 삭제.

---

### K12. 격식체 혼용 [P2]

**문제:** 같은 글에서 "~합니다"와 "~이다"를 섞어 쓰는 것.

**원칙:** 하나로 통일. 기술 문서는 "~이다" 체, 블로그는 자유.

---

### K13. 장단점 대칭 구조 [P2]

**문제:** AI가 기계적으로 장점과 단점을 같은 수만큼 나열.

**원칙:** 실제로 중요한 것만 남기고, 개수를 맞추려고 억지로 늘리지 않기.

---

## 영어 패턴 (E)

### E1. 도입부 과대 포장 [P1]

**감지 표현:** importance, reflects broader, symbolizing its enduring, setting the stage for, marks a shift, key turning point, evolving landscape, indelible mark

**수정 전:**
> The Statistical Institute of Catalonia was officially established in 1989, marking a pivotal moment in the evolution of regional statistics in Spain.

**수정 후:**
> The Statistical Institute of Catalonia was established in 1989 to collect and publish regional statistics independently from Spain's national statistics office.

---

### E2. 주목도/미디어 언급 과시 [P1]

**감지 표현:** independent coverage, local/regional/national media outlets, leading expert, active social media presence

**수정 전:**
> Her views have been cited in The New York Times, BBC, Financial Times, and The Hindu. She maintains an active social media presence with over 500,000 followers.

**수정 후:**
> In a 2024 New York Times interview, she argued that AI regulation should focus on outcomes rather than methods.

---

### E3. ~ing 접미 분석 [P1]

**감지 표현:** highlighting/underscoring/emphasizing..., ensuring..., reflecting/symbolizing..., contributing to..., cultivating/fostering..., showcasing...

**수정 전:**
> The temple's color palette resonates with the region's natural beauty, symbolizing Texas bluebonnets, reflecting the community's deep connection to the land.

**수정 후:**
> The temple uses blue, green, and gold. The architect said these reference local bluebonnets and the Gulf coast.

---

### E4. 홍보성 언어 [P1]

**감지 표현:** boasts, vibrant, rich (비유), profound, showcasing, exemplifies, commitment to, nestled, in the heart of, groundbreaking, renowned, breathtaking, must-visit, stunning, robust, leverage, streamline, seamless, cutting-edge, state-of-the-art, game-changing

**수정 전:**
> Nestled within the breathtaking region of Gonder, Alamata stands as a vibrant town with a rich cultural heritage.

**수정 후:**
> Alamata is a town in the Gonder region, known for its weekly market and 18th-century church.

---

### E5. 모호한 출처/Weasel Words [P1]

**감지 표현:** Industry reports, Observers have cited, Experts argue, Some critics argue, several sources

**수정 전:**
> Experts believe it plays a crucial role in the regional ecosystem.

**수정 후:**
> The river supports several endemic fish species, according to a 2019 survey by the Chinese Academy of Sciences.

---

### E6. "Challenges and Future Prospects" 공식 [P1]

**감지 표현:** Despite its... faces challenges..., Despite these challenges, Future Outlook

**수정 전:**
> Despite its industrial prosperity, the area faces challenges typical of urban areas. Despite these challenges, it continues to thrive.

**수정 후:**
> Traffic congestion increased after 2015. The corporation began a drainage project in 2022 to address recurring floods.

---

### E7. AI 빈출 어휘 [P1]

**감지 표현:** Additionally, align with, crucial, delve, emphasizing, enduring, enhance, fostering, garner, interplay, intricate/intricacies, landscape (추상), pivotal, showcase, tapestry (추상), testament, underscore, vibrant, nuanced, multifaceted, realm, paradigm, synergy

**수정 전:**
> Additionally, a distinctive feature is the intricate interplay between tradition and innovation, showcasing the vibrant tapestry of local culture.

**수정 후:**
> Local dishes blend Italian pasta with traditional Somali spices, a leftover from colonization.

---

### E8. 계사 회피 (Copula Avoidance) [P2]

**감지 표현:** serves as [a], stands as [a], marks [a], represents [a], boasts [a], features [a], offers [a]

**수정 전:**
> Gallery 825 serves as LAAA's exhibition space. The gallery features four rooms and boasts 3,000 square feet.

**수정 후:**
> Gallery 825 is LAAA's exhibition space. It has four rooms totaling 3,000 square feet.

---

### E9. 부정 병렬 구조 [P2]

**감지 표현:** Not only...but..., It's not just about..., it's..., It's not merely..., it's...

**수정 전:**
> It's not just about the beat; it's part of the aggression. It's not merely a song, it's a statement.

**수정 후:**
> The heavy beat adds to the aggressive tone.

---

### E10. False Ranges [P2]

**감지 표현:** from X to Y, from A to B (의미 있는 스케일 아닌 경우)

**수정 전:**
> Our journey has taken us from the singularity of the Big Bang to the grand cosmic web, from the birth of stars to the dance of dark matter.

**수정 후:**
> The book covers the Big Bang, star formation, and current dark matter theories.

---

### E11. Em dash 과용 [P2]

**문제:** AI가 em dash(—)를 세일즈 문체처럼 남발.

**원칙:** 한 단락에 em dash 1개 이하. 나머지는 쉼표나 괄호로 교체.

---

### E12. 볼드체 과용 / 인라인 헤더 목록 [P2]

**문제:** 기계적으로 핵심 용어에 볼드를 적용하거나, `- **Header:** Description` 패턴을 반복.

**수정 전:**
> - **User Experience:** Significantly improved with a new interface.
> - **Performance:** Enhanced through optimized algorithms.
> - **Security:** Strengthened with end-to-end encryption.

**수정 후:**
> The update improves the interface, speeds up load times through optimized algorithms, and adds end-to-end encryption.

**원칙:** 볼드는 정말 강조가 필요한 곳에만. 문단당 1-2개 이하.

---

### E13. 대화형 잔류물 / 아첨 어조 / 최신 상투어 [P1]

**감지 표현:** I hope this helps, Of course!, Certainly!, You're absolutely right!, Would you like..., let me know, here is a..., Great question!, That's an excellent point!, Absolutely!, "Let's dive in", "Let's break this down", "Here's the thing", "It's worth noting that", "This is where X comes in", "The key takeaway here is", "At the end of the day", "In a world where...", "Here's the reality:", "The bottom line:"

**문제:** 챗봇 대화 흔적과 2024년 이후 급증한 AI 상투어. 전부 삭제.

**수정 전:**
> Great question! Let's dive in. Here's the thing — in a world where AI is rapidly evolving, it's worth noting that the key takeaway here is adaptability. At the end of the day, this is where human creativity comes in. I hope this helps!

**수정 후:**
> AI tools change fast. The useful skill isn't mastering any one tool — it's learning to evaluate new ones quickly.

---

### E14. Filler Phrases [P2]

| 수정 전 | 수정 후 |
|---------|---------|
| In order to | To |
| Due to the fact that | Because |
| At this point in time | Now |
| In the event that | If |
| has the ability to | can |
| It is important to note that | (삭제) |
| It goes without saying that | (삭제) |

---

### E15. 과도한 Hedging [P2]

**수정 전:**
> It could potentially possibly be argued that the policy might have some effect.

**수정 후:**
> The policy may affect outcomes.

---

### E16. Curly 따옴표 [P3]

**문제:** AI가 curly quotes(\u201c...\u201d)를 사용. 코드나 기술 문서에서 문제 유발.

**원칙:** 모두 straight quotes("...")로 교체.

---

### E17. 제목 Title Case [P3]

**수정 전:** `## Strategic Negotiations And Global Partnerships`

**수정 후:** `## Strategic negotiations and global partnerships`

---

## 공통 패턴 (C)

### C1. 동의어 순환 (Elegant Variation) [P2]

**문제:** AI가 반복 회피를 위해 같은 대상을 다른 단어로 계속 바꿔 부름.

**수정 전:**
> 주인공은 많은 도전에 직면한다. 이 인물은 장애물을 극복해야 한다. 해당 캐릭터는 결국 승리한다. 우리의 영웅은 집으로 돌아간다.

**수정 후:**
> 주인공은 많은 도전에 직면하지만 결국 이겨내고 집으로 돌아간다.

---

### C2. 지식 한계 면책 [P1]

**감지 표현 (한):** "정확한 정보는 확인이 필요합니다", "최신 정보와 다를 수 있습니다"
**감지 표현 (영):** "as of [date]", "Up to my last training update", "based on available information"

**원칙:** 전부 삭제. 정확한 출처를 찾거나, 모르면 모른다고 쓰기.

---

### C3. 긍정적 결론 공식 [P1]

**감지 표현 (한):** "밝은 미래가 기대됩니다", "무한한 가능성이 열려 있습니다", "함께 노력해야 할 것입니다"
**감지 표현 (영):** "The future looks bright", "Exciting times lie ahead", "a major step in the right direction"

**원칙:** 구체적인 다음 단계로 교체하거나 삭제.

---

### C4. 이모지 장식 [P1]

**문제:** 제목이나 목록에 이모지를 붙이는 것.

**원칙:** 사용자가 명시적으로 요청하지 않으면 전부 제거.

---

### C5. 균일한 문단 길이 [P2]

**문제:** AI가 모든 문단을 비슷한 길이(3-4문장)로 맞추는 경향. 사람은 한 문장짜리 문단도 쓰고, 긴 문단도 쓴다.

**원칙:** 문단 길이가 기계적으로 균일하면, 일부를 합치거나 쪼개서 리듬감을 만든다.

---

### C6. 강제 3단 구조 [P2]

**문제:** AI가 글 유형에 관계없이 항상 "도입 → 본론 → 결론"을 강제. 짧은 글이나 메모에도 도입부와 결론을 넣는다.

**원칙:** 글의 길이와 목적에 맞지 않는 도입부/결론은 삭제. 500자 미만의 글에 "결론적으로"는 거의 항상 불필요.

---

## 전체 예시

### 수정 전 (한국어 AI 글):

> 오늘날 급변하는 디지털 시대에 접어들면서, AI 코딩 도구에 대한 관심이 날로 높아지고 있습니다. 이번 글에서는 AI 코딩 도구의 다양한 측면에 대해 자세히 알아보겠습니다.
>
> AI 코딩 도구는 혁신적인 기술을 통해 개발자들의 생산성을 획기적으로 향상시키는 핵심적인 역할을 하고 있다고 할 수 있습니다. 이를 통해 효과적인 코드 작성이 가능해지며, 이를 바탕으로 체계적인 개발 환경을 구축할 수 있습니다.
>
> 그렇다면 왜 AI 코딩 도구가 필요할까요? 그 이유는 크게 세 가지로 나눌 수 있습니다.
>
> - 🚀 **생산성 향상:** AI 코딩 도구는 코드 자동 완성뿐만 아니라 리팩토링, 테스트 생성까지 지원하여 개발 속도를 혁신적으로 높여줍니다.
> - 💡 **품질 개선:** 다양한 코드 분석 기능을 통해 버그를 사전에 탐지하고 효과적으로 해결할 수 있습니다.
> - ✅ **학습 효과:** 초보 개발자들에게 체계적인 학습 경험을 제공하여 빠른 성장을 도울 수 있습니다.
>
> 물론 AI 코딩 도구에는 장점만 있는 것은 아닙니다. 한편으로는 생산성을 높여주지만, 다른 한편으로는 코드의 정확성 문제, 보안 취약점, 저작권 이슈 등 다양한 과제가 존재합니다. 이러한 관점에서 볼 때, AI 코딩 도구의 중요성은 아무리 강조해도 지나치지 않습니다.
>
> 결론적으로, AI 코딩 도구는 소프트웨어 개발의 미래를 밝게 만들어 줄 것으로 기대됩니다. 앞으로 AI 기술의 지속적인 발전과 함께 개발자 커뮤니티의 관심이 더욱 높아질 것입니다. 도움이 되셨길 바랍니다!

### 수정 후 (사람이 쓴 글):

> AI 코딩 도구가 생산성을 올려준다는 건 반쯤 맞다.
>
> 자동 완성이 빨라지는 건 체감된다. 보일러플레이트, 테스트 스캐폴딩, 반복 리팩토링 같은 건 확실히 빠르다. 다만 디버깅이나 설계 판단에서는 차이를 못 느꼈다. 자동 완성이 빨라지는 거지, 사고가 빨라지는 건 아닌 셈이다.
>
> 솔직히 말하면 가장 위험한 순간은 제안을 무비판적으로 수락할 때다. 컴파일 되고 린트 통과하고 그런데 틀린 코드, 나도 몇 번 당했다. 집중력이 떨어질 때 특히 그렇다.
>
> 보안 쪽은 더 걱정된다. AI가 생성한 코드의 취약점을 체계적으로 추적한 연구가 아직 많지 않다.
>
> 쓸 거면 쓰되, 모든 제안을 리뷰하고, 테스트를 먼저 쓰고, AI 제안은 그 테스트를 통과할 때만 수락하라.

### 변경 사항:
- [K1] 도입부 상투어 제거 ("오늘날", "알아보겠습니다")
- [K2] 과장 수식어 제거 ("혁신적인", "획기적인", "체계적인", "효과적인", "핵심적인", "다양한")
- [K3] 회피 어미 제거 ("~라고 할 수 있습니다")
- [K4] "이를 통해/바탕으로" 연쇄 제거
- [K5] 무의미한 중요성 강조 제거
- [K6] "~뿐만 아니라" 제거
- [K7] "그렇다면 왜~" 자문자답 제거
- [K8] 셋 법칙 해체
- [K10] 결론 상투어 제거 → 실용적 조언으로 대체
- [K11] 대화형 흔적 제거 ("도움이 되셨길 바랍니다!")
- [K13] 장단점 대칭 구조 해체 → 구체적 의견으로
- [C4] 이모지 제거
- [C5] 균일한 문단 길이 해체 → 리듬감 추가
- [C6] 강제 결론 삭제
- [E12] 볼드체 과용 / 인라인 헤더 목록 해체
- 영혼 주입: 1인칭 시점, 개인 경험, 솔직한 의견 (블로그/에세이 유형)

---

## 레퍼런스

이 스킬은 다음 자료를 기반으로 한다:
- [Wikipedia:Signs of AI writing](https://en.wikipedia.org/wiki/Wikipedia:Signs_of_AI_writing) — WikiProject AI Cleanup이 유지보수하는 AI 글쓰기 징후 가이드
- 한국어 AI 텍스트 분석에서 관찰된 패턴

핵심 인사이트: "LLM은 통계적 알고리즘으로 다음에 올 내용을 추측한다. 결과는 가장 통계적으로 가능성 높은, 가장 넓은 범위에 적용되는 결과로 수렴한다." 한국어에서도 동일한 원리가 작동하며, "다양한", "혁신적인", "이를 통해" 같은 고빈도 표현으로 나타난다.
