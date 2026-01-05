---
name: junit-test-writer
description: JUnit 5 테스트 코드 작성 스킬. 단위 테스트, 슬라이스 테스트(@WebMvcTest, @DataJpaTest), 통합 테스트(@SpringBootTest)를 작성한다. "테스트 코드 작성해줘", "~에 대한 테스트 만들어줘", "단위 테스트 추가해줘" 같은 요청에 사용.
---

# JUnit Test Writer

JUnit 5 기반 테스트 코드를 작성하는 스킬.

## 테스트 작성 워크플로우

### 1. 대상 분석

테스트 대상 클래스/메서드 파악:

- 클래스의 의존성 확인 (생성자 주입 필드)
- public 메서드 목록 추출
- 메서드별 분기 조건, 예외 케이스 식별

### 2. 테스트 유형 결정

| 대상                 | 테스트 유형 | 어노테이션                            |
| -------------------- | ----------- | ------------------------------------- |
| Service, Util        | 단위 테스트 | `@ExtendWith(MockitoExtension.class)` |
| Controller           | 슬라이스    | `@WebMvcTest`                         |
| Repository (JPA)     | 슬라이스    | `@DataJpaTest`                        |
| Repository (MyBatis) | 슬라이스    | `@MybatisTest` 또는 `@SpringBootTest` |
| 전체 흐름            | 통합 테스트 | `@SpringBootTest`                     |

### 3. Fixture 전략 결정

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

### 4. 테스트 작성

컨벤션 상세는 `references/conventions.md` 참조.

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
            .isInstanceOf(InsufficientStockException.class);
    }
}
```

### 슬라이스 테스트 - Controller

```java
@WebMvcTest(OrderController.class)
class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

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

| 항목         | 규칙                                    |
| ------------ | --------------------------------------- |
| 메서드명     | `should_결과_when_조건` (영문)          |
| @DisplayName | 비즈니스 언어로 (한글 권장)             |
| 구조         | Given-When-Then 주석 필수               |
| Assertion    | AssertJ 사용 (`assertThat`)             |
| Mock         | BDDMockito 사용 (`given`, `willReturn`) |

상세 컨벤션은 `references/conventions.md` 참조.
