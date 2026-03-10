---
name: junit-test-writer
description: JUnit 5 테스트 코드 작성 스킬. 단위 테스트, 슬라이스 테스트(@WebMvcTest, @DataJpaTest), 통합 테스트(@SpringBootTest)를 작성한다. "테스트 코드 작성해줘", "~에 대한 테스트 만들어줘", "단위 테스트 추가해줘", "통합 테스트 작성", "테스트 케이스 추가", "커버리지 높여줘", "테스트 없는 코드에 테스트 추가" 같은 요청에 사용.
---

# JUnit Test Writer

JUnit 5 기반 테스트 코드를 작성하는 스킬.

## 테스트 작성 워크플로우

### Step 1: 대상 분석

테스트 대상 클래스/메서드를 파악한다:
- 클래스의 의존성 확인 (생성자 주입 필드)
- public 메서드 목록 추출
- 메서드별 분기 조건, 예외 케이스 식별
- **기존 테스트 존재 여부 확인** — 있으면 기존 컨벤션을 따름

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

프로젝트 의존성 확인 후 결정:
- **Instancio 있음**: `Instancio.create()` 사용
- **Instancio 없음**: 직접 Builder 또는 생성자로 fixture 생성

```java
// Instancio 사용
var request = Instancio.create(CreateOrderRequest.class);

// 직접 생성
var request = CreateOrderRequest.builder()
    .productId(1L)
    .quantity(2)
    .build();
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

### Step 7: 테스트 실행 및 검증

테스트 작성 후 반드시 실행하여 결과를 확인한다:
- 테스트 실행: `./gradlew test` 또는 `mvn test`
- 실패 시 원인 분석 후 수정
- 가능하면 커버리지 확인: 대상 클래스의 분기/라인 커버리지가 충분한지 점검

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
1. OrderService.java 읽기 — public 메서드, 의존성 파악
2. 기존 OrderServiceTest.java 존재 여부 확인
3. 핵심 비즈니스 로직(주문 생성, 취소) 우선 테스트 대상 선정
4. 단위 테스트로 작성 (MockitoExtension)
5. 정상/예외/경계값 케이스 커버
6. 테스트 실행하여 통과 확인

**사용자**: "이 Controller에 대한 통합 테스트 만들어줘"

**행동**:
1. Controller 코드 읽기 — 엔드포인트, 요청/응답 형식 파악
2. Security 설정 여부 확인
3. `@WebMvcTest`(슬라이스)가 충분한지, `@SpringBootTest`(통합)가 필요한지 판단
4. 사용자가 "통합"을 명시했으므로 `@SpringBootTest` 사용
5. 정상 요청, 유효성 검증 실패, 인증/인가 시나리오 커버

## 트러블슈팅

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

- 테스트 작성 전 코드 구조 개선이 필요하면: "리팩토링 해줘"로 refactor-advisor 활용
- 테스트 중 성능 문제를 발견하면: "성능 분석해줘"로 performance-profiler 활용
