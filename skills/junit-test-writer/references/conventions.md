# 테스트 컨벤션

## 네이밍 규칙

### 메서드명

```
should_[예상결과]_when_[조건]
```

예시:

- `should_returnOrder_when_validId`
- `should_throwException_when_notFound`
- `should_updateStock_when_orderCreated`

### @DisplayName

**비즈니스 관점**으로 작성 (특히 통합 테스트):

```java
// Good
@DisplayName("고객이 상품을 주문하면 재고가 차감된다")
@DisplayName("회원 등급이 VIP면 10% 할인이 적용된다")
@DisplayName("결제 실패 시 주문이 취소되고 재고가 복구된다")

// Bad
@DisplayName("createOrder 메서드 테스트")
@DisplayName("OrderService 테스트")
```

## 테스트 구조

### Given-When-Then 패턴

```java
@Test
void should_calculateDiscount_when_vipMember() {
    // given - 테스트 준비
    var member = Member.createVip("홍길동");
    var order = Order.create(member, 10000);

    // when - 실행
    var result = discountService.calculate(order);

    // then - 검증
    assertThat(result.getDiscountAmount()).isEqualTo(1000);
}
```

### When & Then 합치기 (예외 테스트)

```java
@Test
void should_throwException_when_stockInsufficient() {
    // given
    var product = Product.create("노트북", 0);

    // when & then
    assertThatThrownBy(() -> orderService.order(product, 1))
        .isInstanceOf(InsufficientStockException.class)
        .hasMessage("재고가 부족합니다");
}
```

## Assertion 스타일

### AssertJ 사용

```java
// 기본
assertThat(result).isEqualTo(expected);
assertThat(result).isNotNull();

// 컬렉션
assertThat(list).hasSize(3);
assertThat(list).contains(item1, item2);
assertThat(list).allMatch(item -> item.isActive());

// 예외
assertThatThrownBy(() -> service.execute())
    .isInstanceOf(IllegalArgumentException.class);

assertThatCode(() -> service.execute())
    .doesNotThrowAnyException();
```

## Mock 스타일

### BDDMockito 사용

```java
// Stubbing
given(repository.findById(1L)).willReturn(Optional.of(entity));
given(repository.findById(anyLong())).willReturn(Optional.empty());

// Verification
then(repository).should().save(any(Order.class));
then(repository).should(times(2)).findById(anyLong());
then(repository).shouldHaveNoMoreInteractions();
```

## Fixture 생성

### Instancio 사용 시

```java
// 기본 생성
var order = Instancio.create(Order.class);

// 필드 커스터마이징
var order = Instancio.of(Order.class)
    .set(field(Order::getStatus), OrderStatus.PENDING)
    .set(field(Order::getAmount), 10000)
    .create();

// 컬렉션 생성
var orders = Instancio.ofList(Order.class).size(5).create();
```

### Instancio 없을 때

```java
// Builder 패턴
var order = Order.builder()
    .id(1L)
    .status(OrderStatus.PENDING)
    .amount(10000)
    .build();

// 정적 팩토리
var order = Order.createForTest(1L, PENDING, 10000);

// Test Fixture 클래스
class OrderFixture {
    public static Order pending() {
        return Order.builder()
            .status(PENDING)
            .amount(10000)
            .build();
    }
}
```

## 테스트 격리

### 단위 테스트

- `@ExtendWith(MockitoExtension.class)` 사용
- Spring Context 로드 없음

### 슬라이스/통합 테스트

- `@Transactional`로 롤백 보장
- `@Sql`로 테스트 데이터 준비 가능

```java
@SpringBootTest
@Transactional
class OrderIntegrationTest {
    // 각 테스트 후 자동 롤백
}
```

## 테스트 중복 최소화

### 1. @BeforeEach로 공통 Setup 추출

```java
@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @InjectMocks
    private OrderService orderService;

    @Mock
    private OrderRepository orderRepository;

    // 공통 fixture
    private Member member;
    private Product product;

    @BeforeEach
    void setUp() {
        member = Member.create("홍길동");
        product = Product.create("노트북", 10, 1000000);
    }

    @Test
    void should_createOrder_when_stockAvailable() {
        // given - 테스트별 특수 조건만 추가
        given(orderRepository.save(any())).willReturn(1L);

        // when & then
        ...
    }
}
```

### 2. @Nested로 공통 조건 그룹화

```java
@DisplayName("OrderService 테스트")
class OrderServiceTest {

    @Nested
    @DisplayName("VIP 회원일 때")
    class WhenVipMember {

        private Member vipMember;

        @BeforeEach
        void setUp() {
            vipMember = Member.createVip("VIP고객");
        }

        @Test
        @DisplayName("10% 할인이 적용된다")
        void should_applyDiscount() { ... }

        @Test
        @DisplayName("무료 배송이 적용된다")
        void should_applyFreeShipping() { ... }
    }

    @Nested
    @DisplayName("일반 회원일 때")
    class WhenRegularMember {

        private Member regularMember;

        @BeforeEach
        void setUp() {
            regularMember = Member.createRegular("일반고객");
        }

        @Test
        @DisplayName("할인이 적용되지 않는다")
        void should_notApplyDiscount() { ... }
    }
}
```

### 3. Test Fixture 클래스 분리

```java
// test/../fixture/OrderFixture.java
public class OrderFixture {

    public static Order pending() {
        return Order.builder()
            .status(PENDING)
            .amount(10000)
            .build();
    }

    public static Order completed() {
        return Order.builder()
            .status(COMPLETED)
            .amount(10000)
            .build();
    }

    public static Order withAmount(int amount) {
        return Order.builder()
            .status(PENDING)
            .amount(amount)
            .build();
    }
}

// 테스트에서 사용
@Test
void should_cancel_when_pendingOrder() {
    var order = OrderFixture.pending();
    ...
}
```

### 4. @ParameterizedTest로 반복 케이스 통합

```java
// 여러 입력값에 대해 동일한 검증
@ParameterizedTest
@ValueSource(ints = {0, -1, -100})
@DisplayName("수량이 0 이하면 예외가 발생한다")
void should_throwException_when_invalidQuantity(int quantity) {
    assertThatThrownBy(() -> Order.create(product, quantity))
        .isInstanceOf(IllegalArgumentException.class);
}

// 입력-기대값 쌍으로 테스트
@ParameterizedTest
@CsvSource({
    "REGULAR, 0",
    "SILVER, 5",
    "GOLD, 10",
    "VIP, 15"
})
@DisplayName("회원 등급별 할인율이 적용된다")
void should_applyDiscountRate_by_memberGrade(MemberGrade grade, int expectedRate) {
    var member = Member.create(grade);
    assertThat(discountService.getRate(member)).isEqualTo(expectedRate);
}

// 복잡한 객체는 @MethodSource
@ParameterizedTest
@MethodSource("provideInvalidRequests")
@DisplayName("잘못된 요청은 검증에 실패한다")
void should_failValidation_when_invalidRequest(CreateOrderRequest request, String expectedField) {
    var violations = validator.validate(request);
    assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals(expectedField));
}

static Stream<Arguments> provideInvalidRequests() {
    return Stream.of(
        Arguments.of(new CreateOrderRequest(null, 1), "productId"),
        Arguments.of(new CreateOrderRequest(1L, 0), "quantity"),
        Arguments.of(new CreateOrderRequest(1L, -1), "quantity")
    );
}
```

### 5. 헬퍼 메서드 추출

```java
class OrderControllerTest {

    // 공통 요청 생성
    private MockHttpServletRequestBuilder createOrderRequest(CreateOrderRequest request) {
        return post("/orders")
            .contentType(APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request));
    }

    // 공통 검증
    private void assertOrderResponse(ResultActions result, Long expectedId) throws Exception {
        result.andExpect(status().isCreated())
              .andExpect(jsonPath("$.orderId").value(expectedId));
    }

    @Test
    void should_createOrder_when_validRequest() throws Exception {
        given(orderService.createOrder(any())).willReturn(1L);

        var result = mockMvc.perform(createOrderRequest(new CreateOrderRequest(1L, 2)));

        assertOrderResponse(result, 1L);
    }
}
```

### 중복 최소화 체크리스트

| 신호                             | 해결 방법                         |
| -------------------------------- | --------------------------------- |
| 여러 테스트에서 동일한 객체 생성 | `@BeforeEach` 또는 Fixture 클래스 |
| 비슷한 조건의 테스트가 나열됨    | `@Nested`로 그룹화                |
| 입력값만 다른 동일한 테스트      | `@ParameterizedTest`              |
| 반복되는 assertion 패턴          | 헬퍼 메서드 추출                  |
| 테스트 클래스가 500줄 이상       | 클래스 분리 고려                  |
