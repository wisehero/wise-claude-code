---
name: junit-test-writer
version: 1.1.0
description: Java/Spring **전용** JUnit 5 테스트 코드 작성 스킬. 단위 테스트, 슬라이스 테스트(@WebMvcTest, @DataJpaTest), 통합 테스트(@SpringBootTest)를 작성한다. "테스트 코드 작성해줘", "~에 대한 테스트 만들어줘", "단위 테스트 추가해줘", "통합 테스트 작성", "테스트 케이스 추가", "커버리지 높여줘", "테스트 없는 코드에 테스트 추가" 같은 요청에 사용. **Java가 아닌 언어(PHP·Python·JavaScript·Ruby 등)는 범위 밖** — 해당 언어로 테스트 요청이 들어오면 Step 1에서 즉시 거절한다. Kotlin 프로젝트는 기본 거절, 사용자 명시 opt-in 시에만 JUnit 5 + MockK/Kotest 혼용으로 진행.
allowed-tools: [Read, Write, Edit, Glob, Grep, Bash]
---

# JUnit Test Writer

Java/Spring Boot 프로젝트 전용 JUnit 5 테스트 코드 작성 스킬.

> **언어 가드**: 이 스킬은 **Java/Spring** 프로젝트에만 적용된다. PHP·Python·JavaScript·Ruby 등은 Step 1의 첫 서브스텝에서 거절한다. Kotlin은 기본 거절·opt-in 허용. 자세한 판정 기준은 아래 Step 1과 트러블슈팅의 "비대상 언어 감지" 절을 본다.
>
> **편집 경로 안전장치**: 이 스킬은 테스트 파일을 **Write(신규)·Edit(기존 파일 추가)** 한다. 기존 테스트가 있거나 프로덕션 코드까지 함께 수정해야 하는 경우에는 Step 6.5의 **프리뷰 → 확인 응답 프로토콜**을 반드시 거친다. 즉흥 생성 금지.

## 테스트 작성 워크플로우

### Step 1: 대상 분석

**Step 1.0 — 언어·프레임워크 가드 (가장 먼저, 예외 없이 수행)**

다음 3개 증거를 순서대로 확인한다. 하나라도 실패하면 즉시 중단하고 거절 응답을 낸다:

1. **대상 파일 확장자가 `.java`인가?** (Kotlin `.kt`는 기본 거절 — 트러블슈팅 "Kotlin opt-in" 참고)
2. **프로젝트 루트에 `build.gradle`, `build.gradle.kts`, `pom.xml` 중 하나가 존재하는가?**
3. **빌드 파일에서 JUnit(4 또는 5) 또는 `spring-boot-starter-test` 의존성이 식별되는가?** (Glob/Grep으로 확인)

판정 매트릭스:

| 확장자 | 빌드 파일 | JUnit 의존성 | 처리 |
|---|---|---|---|
| `.java` | 있음 | JUnit 5 | **진행** (본 스킬 정상 경로) |
| `.java` | 있음 | JUnit 4만 | **opt-in 요청** — "이 프로젝트는 JUnit 4를 씁니다. JUnit 5 마이그레이션 없이 JUnit 4로 작성할까요? (예/아니오)" |
| `.java` | 없음 | — | **중단** — 빌드 파일이 없어 `Step 7` 실행 검증이 불가능. 사용자에게 상황 확인. |
| `.kt` | 있음 | — | **기본 거절** — "Kotlin은 이 스킬의 기본 범위 밖입니다. JUnit 5 + MockK/Kotest 혼용으로 opt-in 하시겠습니까?" |
| `.php`·`.py`·`.js`·`.ts`·`.rb` 등 | — | — | **즉시 거절** — "이 스킬은 Java/Spring 전용입니다. `<감지된 언어>`는 지원 범위 밖입니다." 더 이상 Step 진행 금지. |

거절 응답 예시는 트러블슈팅 "비대상 언어 감지" 절 참조.

**Step 1.1 — 대상 클래스 구조 분석**

Step 1.0을 통과한 경우에만 수행한다:

- 클래스의 의존성 확인 (생성자 주입 필드)
- public 메서드 목록 추출
- 메서드별 분기 조건, 예외 케이스 식별
- DTO/요청 객체가 **record** 인지 일반 class/Builder 인지 구분 (접근자가 `.field()` 인지 `.getField()` 인지 결정됨)

**Step 1.2 — 기존 테스트 탐색과 충돌 결정 규칙**

Glob으로 `src/test/**/<ClassName>Test.java` 탐색. 결과에 따라 다음 결정 규칙을 적용한다:

| 상황 | 결정 규칙 |
|---|---|
| 기존 테스트 **없음** | 새 파일 생성. 컨벤션은 하단 "컨벤션 요약"과 `references/conventions.md`의 canonical 스타일(`should_*_when_*` / AssertJ / BDDMockito / `@Nested`)을 따른다. |
| 기존 테스트 **있고 canonical 스타일** | 동일 파일에 **Edit으로 추가** — 기존 스타일을 그대로 유지. |
| 기존 테스트 **있으나 non-canonical 스타일** (예: 한글 메서드명, 수동 `mock()`, JUnit assertions) | **파일 내부 일관성 원칙**: 해당 파일 안에서는 기존 스타일을 그대로 답습해 추가한다. 새 파일/새 클래스로 분리할 때만 canonical을 쓴다. 단, 이 결정은 **반드시 Step 6.5 프리뷰 단계에서 사용자에게 명시적으로 보고**하고 확인받는다. |
| 기존 테스트 파일 내부에 두 스타일이 **이미 혼재** | 신규 추가분은 다수파 스타일을 따른다. 모호하면 Step 6.5에서 사용자에게 선택지를 제시. |

**금지**: 기존 파일을 Write로 덮어쓰지 않는다. 반드시 Edit만 사용한다. 기존 메서드를 삭제하거나 리네임하지 않는다(리팩토링은 refactor-advisor의 영역).

### Step 2: 테스트 전략 결정

무엇을 테스트할지 우선순위를 정한다:

**우선 테스트 대상** (비즈니스 가치가 높고 버그 위험이 큰 코드):
- 핵심 비즈니스 로직 (금액 계산, 상태 변경, 권한 검증)
- 복잡한 분기가 있는 메서드
- 외부 시스템 연동 지점
- 과거에 버그가 발생했던 코드

**테스트 불필요 대상**:
- 단순 위임 메서드 (getter/setter, 단순 래퍼)
- 프레임워크가 보장하는 동작 (Spring DI, JPA 기본 CRUD)

### Step 3: 테스트 유형 결정

| 대상 | 테스트 유형 | 어노테이션 |
|------|------------|-----------|
| Service, Util | 단위 테스트 | `@ExtendWith(MockitoExtension.class)` |
| Controller | 슬라이스 | `@WebMvcTest` |
| Repository (JPA) | 슬라이스 | `@DataJpaTest` |
| Repository (MyBatis) | 슬라이스 | `@MybatisTest` 또는 `@SpringBootTest` |
| 전체 흐름 | 통합 테스트 | `@SpringBootTest` |

**선택 원칙**: 슬라이스로 충분하면 `@SpringBootTest`를 사용하지 않는다. 테스트가 빠를수록 자주 실행하게 되고, 자주 실행할수록 버그를 빨리 잡는다.

### Step 4: Fixture 전략 결정

프로젝트 의존성과 도메인 객체의 생성 가능성을 함께 본다:

| 조건 | 선택 |
|---|---|
| Instancio/Fixture Monkey 등 랜덤 생성기가 있고, 대상 도메인이 **값 객체·record·POJO** 라 생성이 안전함 | 랜덤 생성기 사용 (`Instancio.create(...)`, `FixtureMonkey.create(...)`) |
| 랜덤 생성기가 있지만 도메인이 **복잡한 invariant**(예: 생성자에서 상태 검증)를 가져 랜덤 값으로 생성하면 깨짐 | 직접 Builder/생성자 사용 |
| 랜덤 생성기 없고 Builder 있음 | Builder 사용 |
| Builder도 없고 생성자만 있음 | 생성자 직접 호출 (setter 주입 POJO면 `new + setField` 조합) |
| 대상 도메인이 **순수 mock 대상**이고 행위만 stub하면 충분 | `mock(Class.class)` + `given(...)` — fixture 자체를 만들지 않음 |

선택 근거는 테스트 파일 상단 주석에 한 줄로 남기지 않아도 된다(소음). 단, Step 6.5 프리뷰 단계에서 사용자에게 "왜 Instancio 대신 mock을 골랐는지"를 1문장으로 설명한다.

```java
// Instancio 사용
var request = Instancio.create(CreateOrderRequest.class);

// 직접 생성
var request = CreateOrderRequest.builder()
    .productId(1L)
    .quantity(2)
    .build();

// mock으로 대체 (invariant가 복잡해서)
var product = mock(Product.class);
given(product.getId()).willReturn(1L);
given(product.getStock()).willReturn(10);
```

### Step 5: 테스트 작성

컨벤션 상세는 `references/conventions.md` 참조.

### Step 6: 경계값과 엣지케이스 커버

비즈니스 로직의 경계에서 버그가 발생하기 쉽다. 의식적으로 커버한다:

```java
// 수량 경계값
@ParameterizedTest
@ValueSource(ints = {0, -1, Integer.MIN_VALUE})
@DisplayName("수량이 0 이하면 예외가 발생한다")
void should_throwException_when_invalidQuantity(int quantity) {
    assertThatThrownBy(() -> Order.create(product, quantity))
        .isInstanceOf(IllegalArgumentException.class);
}

// null 입력
@Test
@DisplayName("상품 ID가 null이면 예외가 발생한다")
void should_throwException_when_productIdNull() {
    var request = new CreateOrderRequest(null, 1);
    assertThatThrownBy(() -> orderService.createOrder(request))
        .isInstanceOf(IllegalArgumentException.class);
}

// 빈 컬렉션
@Test
@DisplayName("주문 항목이 비어있으면 예외가 발생한다")
void should_throwException_when_emptyItems() {
    var request = CreateOrderRequest.builder()
        .items(Collections.emptyList())
        .build();
    assertThatThrownBy(() -> orderService.createOrder(request))
        .isInstanceOf(IllegalArgumentException.class);
}
```

### Step 6.5: 프리뷰 & 확인 응답 프로토콜 (Write/Edit 직전에 필수)

테스트 파일을 실제로 Write 또는 Edit 하기 **직전에** 다음 4단계를 거친다. study-helper v1.3.0 / refactor-advisor v1.2.0이 확립한 canonical 안전장치 패턴을 따른다.

**① 프리뷰 생성**

사용자에게 다음 형식의 요약을 터미널에 출력한다:

```
## 테스트 작성 프리뷰 — <ClassName>Test

### 대상
- 파일: <경로> (신규 / 기존 파일에 추가)
- 테스트 유형: 단위 / 슬라이스(@WebMvcTest|@DataJpaTest) / 통합(@SpringBootTest)

### 작성할 테스트 케이스 (번호로 선별 가능)
1. should_createOrder_when_validRequest — happy path
2. should_throwException_when_productNotFound — 예외 케이스
3. should_throwException_when_stockInsufficient — 예외 케이스
4. should_throwException_when_invalidQuantity — 파라미터라이즈 (0, -1, MIN_VALUE)
5. should_cancelOrder_when_pendingStatus — cancelOrder happy path
6. should_throwException_when_alreadyDelivered — cancelOrder 예외

### Fixture 전략
<Instancio 사용 / Builder / mock() + 그 근거 한 줄>

### 기존 테스트 상황 (있을 때만)
- 기존 파일 스타일: <canonical / 한글 메서드명 / BDDMockito 미사용 / ...>
- 결정: <파일 내부 기존 스타일 보존 / canonical로 새 파일 분리 / ...>

### Java/Spring 가드 통과
- build file: <build.gradle / pom.xml>
- JUnit 버전: <5.x / 4.x opt-in>
```

**② 확인 응답 프로토콜**

사용자의 응답을 다음 4가지 명시적 키워드로만 해석한다:

| 응답 | 해석 |
|---|---|
| `진행` | 프리뷰 전체를 그대로 적용 |
| `1,3,5만 진행` (번호 선별) | 지정한 번호만 작성 |
| `Fixture는 mock으로` 같은 한 줄 수정 | 해당 항목만 반영 후 재프리뷰 |
| `취소` | Write/Edit 하지 않고 종료 |

**모호한 동의는 전체 승인으로 해석하지 않는다.** "응", "오케이", "좋아", "ㅇㅇ" 같은 응답이 오면 위 4가지 키워드 중 하나로 명시해 달라고 **한 번만** 재요청한다. 재요청에도 모호하면 `취소`로 처리한다.

**③ 편집 적용**

- 신규 파일: `Write`
- 기존 파일 추가: `Edit` (old_string을 파일 말미의 `}` 마지막 줄로 잡거나, 기존 `@Nested` 블록 직후로 잡아 안전하게 삽입)
- **절대 기존 파일을 Write로 덮어쓰지 않는다.** 기존 메서드·import·클래스 선언을 보존한다.

**④ 사후 요약**

편집 적용 후 다음 형식으로 1회 출력하고 Step 7로 넘어간다:

```
## 작성 완료
- 파일: <경로>
- 추가된 테스트 메서드: <목록>
- 전체 메서드 수: <N>개 (기존 M + 신규 K)
- 다음: Step 7 — ./gradlew test 또는 mvn test 실행
```

### Step 7: 테스트 실행 및 검증

테스트 작성 후 반드시 실행하여 결과를 확인한다. 이 Step은 Bash 권한을 사용한다(`allowed-tools`에 선언되어 있음).

**실행 경로 결정**:

1. `./gradlew`(또는 `gradlew.bat`)이 존재하면 `./gradlew test --tests <FQCN>`으로 **해당 클래스만** 실행
2. `mvnw`이 존재하면 `./mvnw test -Dtest=<ClassName>`
3. 둘 다 없고 전역 `gradle`/`mvn`만 있으면 사용자에게 실행 여부를 먼저 확인
4. 빌드 wrapper도 전역 CLI도 없으면 "실행 검증 불가 — 사용자가 직접 실행 필요"라고 명시하고 Step 7 종료

**실패 시 대응 프로토콜** (최대 2회 재시도, 그 이상은 사용자에게 에스컬레이션):

| 실패 원인 | 대응 |
|---|---|
| 테스트 코드의 assertion/stub 오류 | 테스트만 수정하고 재실행 |
| 테스트 코드의 컴파일 오류 (타입·import) | 테스트만 수정하고 재실행 |
| 프로덕션 코드 버그로 보이는 실패 | **프로덕션 코드를 수정하지 말 것.** 실패 재현 증거(스택 트레이스 요약)와 함께 사용자에게 보고하고 refactor-advisor 또는 버그 수정 작업으로 핸드오프 |
| 빌드 환경 문제 (Gradle wrapper 권한, 네트워크, JDK 버전) | 1회 재시도 후 사용자에게 환경 문제로 보고 |
| Spring Context 로드 실패 | `@WebMvcTest` / 단위 테스트로 전환 가능한지 재검토 후 프리뷰 다시 |

**실행 금지 상황**: 아래 중 하나라도 해당하면 Step 7을 자동 실행하지 않고 사용자에게 먼저 확인한다.
- 통합 테스트(`@SpringBootTest`) + 실제 DB/외부 시스템 연결이 있을 때
- CI 전용 테스트 프로파일이 별도로 존재할 때
- `./gradlew test` 전체 실행이 분 단위로 오래 걸리는 대형 프로젝트 (`--tests` 필터가 필수)

가능하면 커버리지도 확인한다: 대상 클래스의 분기/라인 커버리지가 충분한지 점검 (JaCoCo가 설정되어 있을 때만).

## 테스트 유형별 가이드

### 단위 테스트

```java
@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @InjectMocks
    private OrderService orderService;

    @Mock
    private OrderRepository orderRepository;

    @Test
    @DisplayName("주문 생성 시 재고가 부족하면 예외가 발생한다")
    void should_throwException_when_stockInsufficient() {
        // given
        var request = createOrderRequest();
        given(orderRepository.findStock(anyLong())).willReturn(0);

        // when & then
        assertThatThrownBy(() -> orderService.createOrder(request))
            .isInstanceOf(InsufficientStockException.class)
            .hasMessage("재고가 부족합니다");
    }
}
```

### 슬라이스 테스트 - Controller

```java
@WebMvcTest(OrderController.class)
class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private OrderService orderService;

    @Test
    @DisplayName("POST /orders 요청 시 주문이 생성된다")
    void should_createOrder_when_validRequest() throws Exception {
        // given
        var request = new CreateOrderRequest(1L, 2);
        given(orderService.createOrder(any())).willReturn(1L);

        // when & then
        mockMvc.perform(post("/orders")
                .contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.orderId").value(1L));
    }
}
```

### 슬라이스 테스트 - Controller (Security 적용)

Spring Security가 적용된 경우 `@WithMockUser`, `@WithAnonymousUser` 등으로 인증/인가 시나리오를 테스트한다.

```java
@WebMvcTest(AdminController.class)
class AdminControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AdminService adminService;

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("관리자는 주문을 삭제할 수 있다")
    void should_deleteOrder_when_admin() throws Exception {
        mockMvc.perform(delete("/admin/orders/1"))
            .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("일반 사용자는 관리자 API에 접근할 수 없다")
    void should_return403_when_notAdmin() throws Exception {
        mockMvc.perform(delete("/admin/orders/1"))
            .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("인증되지 않은 사용자는 401을 받는다")
    void should_return401_when_unauthenticated() throws Exception {
        mockMvc.perform(get("/admin/orders"))
            .andExpect(status().isUnauthorized());
    }
}
```

### 슬라이스 테스트 - Repository

```java
@DataJpaTest
class OrderRepositoryTest {

    @Autowired
    private OrderRepository orderRepository;

    @Test
    @DisplayName("주문 상태로 조회하면 해당 상태의 주문만 반환된다")
    void should_returnOrders_when_filterByStatus() {
        // given
        orderRepository.save(Order.create(PENDING));
        orderRepository.save(Order.create(COMPLETED));

        // when
        var result = orderRepository.findByStatus(PENDING);

        // then
        assertThat(result).hasSize(1)
            .allMatch(order -> order.getStatus() == PENDING);
    }
}
```

### 통합 테스트

```java
@SpringBootTest
@Transactional
class OrderIntegrationTest {

    @Autowired
    private OrderService orderService;

    @Autowired
    private ProductRepository productRepository;

    @Test
    @DisplayName("고객이 상품을 주문하면 재고가 차감되고 주문 내역이 생성된다")
    void 고객이_상품을_주문하면_재고가_차감되고_주문내역이_생성된다() {
        // given
        var product = productRepository.save(Product.create("노트북", 10));

        // when
        var orderId = orderService.createOrder(new CreateOrderRequest(product.getId(), 2));

        // then
        var savedProduct = productRepository.findById(product.getId()).orElseThrow();
        assertThat(savedProduct.getStock()).isEqualTo(8);

        var order = orderService.getOrder(orderId);
        assertThat(order.getQuantity()).isEqualTo(2);
    }
}
```

## 컨벤션 요약

| 항목 | 규칙 |
|------|------|
| 메서드명 | `should_결과_when_조건` (영문) |
| @DisplayName | 비즈니스 언어로 (한글 권장) |
| 구조 | Given-When-Then 주석 필수 |
| Assertion | AssertJ 사용 (`assertThat`) |
| Mock | BDDMockito 사용 (`given`, `willReturn`) |
| 검증 원칙 | 하나의 테스트에 하나의 논리적 검증 |
| 안티패턴 | 구현이 아닌 행위를 테스트. `verify()`는 외부 부수효과에만 사용 |
| 성능 | 슬라이스로 충분하면 `@SpringBootTest` 사용 금지 |
| 경계값 | 0, null, 빈 값, max 등 경계값을 의식적으로 커버 |
| 예외 검증 | 타입뿐 아니라 메시지, 에러 코드까지 검증 |

상세 컨벤션은 `references/conventions.md` 참조.

## 예시 시나리오

**사용자**: "OrderService에 대한 테스트 작성해줘"

**행동**:
1. **Step 1.0 가드**: `OrderService.java` 확인, `build.gradle` 발견, `spring-boot-starter-test` 의존성 확인 → 진행
2. **Step 1.1/1.2**: OrderService.java 읽기 → public 메서드·의존성 파악. 기존 OrderServiceTest.java 없음 → 새 파일 + canonical 스타일
3. **Step 2~4**: 핵심 비즈니스 로직(주문 생성, 취소) 우선, 단위 테스트(MockitoExtension), Instancio 유무에 따라 Fixture 전략 결정
4. **Step 5~6**: Given-When-Then + 정상/예외/경계값 작성안 구성
5. **Step 6.5 프리뷰**: 작성할 테스트 케이스 목록을 사용자에게 보여주고 `진행`/`번호 선별`/`취소` 대기
6. **Step 6.5 적용**: 사용자 응답에 따라 Write
7. **Step 7**: `./gradlew test --tests com.example.order.OrderServiceTest` 실행, 실패 시 대응 프로토콜

**사용자**: "이 Controller에 대한 통합 테스트 만들어줘"

**행동**:
1. **Step 1.0 가드** 통과
2. Controller 코드 읽기 — 엔드포인트, 요청/응답 형식 파악
3. Security 설정 여부 확인. `@WebMvcTest`(슬라이스)가 충분한지, `@SpringBootTest`(통합)가 필요한지 판단. 사용자가 "통합"을 명시했으므로 `@SpringBootTest` 사용
4. 정상 요청, 유효성 검증 실패, 인증/인가 시나리오 나열
5. **Step 6.5 프리뷰**: 케이스 목록 + "통합 테스트라 실제 DB가 뜨므로 Step 7 자동 실행을 건너뛸지" 확인 요청
6. Step 6.5 적용 → Step 7 (사용자 승인 시에만 실행)

**사용자 (거절 경로)**: PHP Laravel 프로젝트에서 "UserService 테스트 작성해줘"

**행동**:
1. **Step 1.0 가드**: `.java` 파일 없음, `composer.json` 발견 → 매트릭스 최하단 행 매칭 → **즉시 거절**
2. 트러블슈팅 "비대상 언어 감지" 템플릿으로 응답
3. Step 1.1 이후로 넘어가지 않음. 파일 생성 금지.

## 트러블슈팅

### 비대상 언어 감지 (Step 1.0 거절 경로)

**증상**: 사용자가 "테스트 작성해줘"라고 요청했는데 프로젝트가 PHP/Python/JavaScript/TypeScript/Ruby/Go 등.

**판정**: Step 1.0 매트릭스 최하단 — **즉시 거절**. Step 1.1 이후로 넘어가지 않는다.

**거절 응답 템플릿**:

```
이 스킬은 Java/Spring 전용 JUnit 5 테스트 작성 스킬입니다.
감지된 언어/프레임워크: <감지된 것>

본 스킬로는 처리할 수 없습니다. 다음 중 원하시는 경로를 알려주세요:
- 해당 언어의 전용 테스트 작성 방법을 원하시면 다른 도구를 이용해 주세요
  (예: PHP → PHPUnit, Python → pytest, JavaScript/TypeScript → Jest/Vitest)
- 프로젝트에 Java 모듈이 별도로 있다면 해당 모듈 경로를 지정해 주세요
```

**절대 하지 말 것**: Java/JUnit 코드를 PHP 프로젝트의 `tests/` 폴더에 작성하지 않는다. 파일 확장자를 `.java`로 맞춰도 빌드되지 않고 사용자를 오도한다.

### Kotlin 프로젝트 opt-in

**증상**: 대상 파일이 `.kt`.

**기본 처리**: 거절 + opt-in 요청. 응답 템플릿:

```
이 스킬의 기본 범위는 순수 Java입니다. Kotlin은 MockK/Kotest 생태계가
Mockito와 다르게 작동하여 본 스킬의 컨벤션(BDDMockito, AssertJ)을 그대로
적용하면 부자연스러울 수 있습니다.

그래도 JUnit 5 + MockK/Kotest 혼용으로 진행하시겠습니까? (예/아니오)
```

opt-in 승인 시: JUnit 5 runner는 유지하되 Mockito → MockK, AssertJ → Kotest matcher로 컨벤션을 대체. `references/conventions.md`의 Java 예제를 그대로 복사하지 말 것.

### JUnit 4 레거시 프로젝트

**증상**: `pom.xml`/`build.gradle`에 `junit:junit:4.x`만 있고 `junit-jupiter`가 없음.

**처리**: Step 1.0 매트릭스의 "JUnit 4만" 행 — **opt-in 요청**. 응답 템플릿:

```
이 프로젝트는 JUnit 4를 사용 중입니다. 본 스킬의 canonical 컨벤션은 JUnit 5
(`@ExtendWith`, `@DisplayName`, `assertThrows` 등) 기반이지만, 다음 중 하나를
선택하실 수 있습니다:

1. JUnit 4로 작성 (기존 환경 유지) — `@RunWith(MockitoJUnitRunner.class)`,
   `@Rule ExpectedException` 등 JUnit 4 관용구 사용
2. JUnit 5로 마이그레이션 제안 (스킬 범위 밖 — refactor-advisor나 별도 작업 필요)

어느 쪽을 원하십니까?
```

### 테스트가 단독으로는 성공하지만 함께 실행하면 실패

**원인**: 테스트 간 상태 공유. Static 필드, 캐시, 또는 DB 데이터가 정리되지 않음.
**해결**: `@Transactional`로 롤백 보장, `@DirtiesContext` 사용(최후의 수단), Static 상태 의존 제거.

### @MockBean이 주입되지 않음

**원인**: `@WebMvcTest`에 테스트 대상 Controller가 지정되지 않음.
**해결**: `@WebMvcTest(OrderController.class)`처럼 대상을 명시한다.

### Spring Context 로드가 느림

**원인**: `@SpringBootTest`를 과도하게 사용하거나, `@MockBean` 조합이 매번 달라 Context가 재생성됨.
**해결**: 슬라이스 테스트로 전환. `@MockBean` 조합을 클래스 단위로 통일. 공통 테스트 베이스 클래스 사용.

### Lazy 로딩 예외 (LazyInitializationException)

**원인**: 트랜잭션 밖에서 LAZY 연관 객체에 접근.
**해결**: 테스트에 `@Transactional` 추가, 또는 Fetch Join 쿼리 사용.

## 연관 스킬 안내

이 스킬은 **테스트 작성** 축에만 집중한다. 아래 축은 다른 스킬로 핸드오프:

| 상황 | 핸드오프 대상 | 트리거 문구 |
|---|---|---|
| 테스트 작성 전 코드 구조 개선·리팩토링 필요 | **refactor-advisor** | "리팩토링 해줘" |
| Step 7 실행 중 N+1·느린 쿼리·메모리 문제 감지 | **performance-profiler** | "성능 분석해줘" |
| 테스트 대상 클래스의 비즈니스 로직 이해가 선행되어야 함 | **code-analyzer** | "코드 분석해줘" |
| 프로덕션 코드 버그로 판정된 Step 7 실패 (본 스킬은 프로덕션 수정 금지) | refactor-advisor 또는 수동 수정 | Step 7 "프로덕션 코드 버그" 분기 참조 |

**원칙**: 본 스킬이 프로덕션 코드를 수정하지 않는다. 테스트 파일(`src/test/**`)만 Write/Edit 한다. 프로덕션 파일 수정이 필요하면 반드시 사용자에게 에스컬레이션 후 별도 작업으로 분리한다.
