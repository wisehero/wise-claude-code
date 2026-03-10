# 코드 스멜 카탈로그

Java/Spring 프로젝트에서 자주 발견되는 코드 스멜과 리팩토링 예시.

## 메서드 수준

### Long Method

한 메서드가 여러 추상화 수준의 작업을 수행한다.

```java
// Before: 주문 생성 메서드가 검증, 계산, 저장, 알림까지 모두 처리
public OrderResponse createOrder(CreateOrderRequest request) {
    // 검증 (10줄)
    if (request.getItems().isEmpty()) {
        throw new IllegalArgumentException("주문 항목이 비어있습니다");
    }
    for (OrderItem item : request.getItems()) {
        Product product = productRepository.findById(item.getProductId())
            .orElseThrow(() -> new ProductNotFoundException(item.getProductId()));
        if (product.getStock() < item.getQuantity()) {
            throw new InsufficientStockException(product.getName());
        }
    }

    // 가격 계산 (15줄)
    BigDecimal totalPrice = BigDecimal.ZERO;
    for (OrderItem item : request.getItems()) {
        Product product = productRepository.findById(item.getProductId()).orElseThrow();
        BigDecimal itemPrice = product.getPrice().multiply(BigDecimal.valueOf(item.getQuantity()));
        if (item.getQuantity() >= 10) {
            itemPrice = itemPrice.multiply(BigDecimal.valueOf(0.9));
        }
        totalPrice = totalPrice.add(itemPrice);
    }

    // 저장 (5줄)
    Order order = Order.builder()
        .memberId(request.getMemberId())
        .totalPrice(totalPrice)
        .build();
    orderRepository.save(order);

    // 알림 (5줄)
    notificationService.sendOrderConfirmation(order);

    return OrderResponse.from(order);
}
```

```java
// After: 각 추상화 수준을 별도 메서드로 분리
public OrderResponse createOrder(CreateOrderRequest request) {
    validateOrderItems(request.getItems());
    BigDecimal totalPrice = calculateTotalPrice(request.getItems());

    Order order = Order.create(request.getMemberId(), totalPrice);
    orderRepository.save(order);

    notificationService.sendOrderConfirmation(order);
    return OrderResponse.from(order);
}

private void validateOrderItems(List<OrderItem> items) {
    if (items.isEmpty()) {
        throw new IllegalArgumentException("주문 항목이 비어있습니다");
    }
    items.forEach(this::validateStock);
}

private BigDecimal calculateTotalPrice(List<OrderItem> items) {
    return items.stream()
        .map(this::calculateItemPrice)
        .reduce(BigDecimal.ZERO, BigDecimal::add);
}
```

**왜 개선인가**: 메서드명만 읽으면 흐름이 파악된다. 각 메서드를 독립적으로 이해, 테스트, 수정할 수 있다.

### Parameter Overload

파라미터 3개 초과, 특히 같은 타입이 연속되면 호출 시 순서를 실수하기 쉽다.

```java
// Before: String 4개가 연속 — 어떤 값이 어떤 파라미터인지 헷갈림
public void createMember(String name, String email, String phone, String address,
                         int age, boolean isActive) { ... }

// 호출부에서 실수 발생 가능
createMember("홍길동", "010-1234-5678", "hong@email.com", "서울시", 30, true);
//                     phone과 email이 뒤바뀜!
```

```java
// After: 파라미터 객체로 묶기
public void createMember(CreateMemberRequest request) { ... }

@Builder
public record CreateMemberRequest(
    String name,
    String email,
    String phone,
    String address,
    int age,
    boolean isActive
) {}
```

### Flag Argument

boolean 파라미터로 메서드 내부에서 분기하면, 호출부에서 true/false가 무슨 의미인지 알 수 없다.

```java
// Before
orderService.processOrder(order, true);  // true가 뭔지 모름
orderService.processOrder(order, false);

public void processOrder(Order order, boolean isExpress) {
    if (isExpress) {
        // 빠른 배송 로직
    } else {
        // 일반 배송 로직
    }
}
```

```java
// After: 별도 메서드로 분리
orderService.processExpressOrder(order);
orderService.processStandardOrder(order);
```

### 복잡한 조건문

중첩 if/else, 긴 조건 체인.

```java
// Before: 중첩 3단계
public BigDecimal calculateDiscount(Order order) {
    if (order.getMember() != null) {
        if (order.getMember().getGrade() == Grade.VIP) {
            if (order.getTotalPrice().compareTo(BigDecimal.valueOf(100000)) > 0) {
                return order.getTotalPrice().multiply(BigDecimal.valueOf(0.15));
            } else {
                return order.getTotalPrice().multiply(BigDecimal.valueOf(0.10));
            }
        } else if (order.getMember().getGrade() == Grade.GOLD) {
            return order.getTotalPrice().multiply(BigDecimal.valueOf(0.05));
        }
    }
    return BigDecimal.ZERO;
}
```

```java
// After: Early return + Guard clause
public BigDecimal calculateDiscount(Order order) {
    if (order.getMember() == null) {
        return BigDecimal.ZERO;
    }

    Grade grade = order.getMember().getGrade();
    BigDecimal price = order.getTotalPrice();

    if (grade == Grade.VIP && isHighValueOrder(price)) {
        return price.multiply(BigDecimal.valueOf(0.15));
    }
    if (grade == Grade.VIP) {
        return price.multiply(BigDecimal.valueOf(0.10));
    }
    if (grade == Grade.GOLD) {
        return price.multiply(BigDecimal.valueOf(0.05));
    }
    return BigDecimal.ZERO;
}
```

### 부수효과 숨김

메서드명에 드러나지 않는 상태 변경.

```java
// Before: getOrder인데 내부에서 조회수를 증가시킴
public Order getOrder(Long orderId) {
    Order order = orderRepository.findById(orderId).orElseThrow();
    order.incrementViewCount(); // 숨겨진 부수효과
    orderRepository.save(order);
    return order;
}
```

```java
// After: 메서드명으로 부수효과를 드러냄
public Order getOrder(Long orderId) {
    return orderRepository.findById(orderId).orElseThrow();
}

public Order getOrderAndTrackView(Long orderId) {
    Order order = orderRepository.findById(orderId).orElseThrow();
    order.incrementViewCount();
    orderRepository.save(order);
    return order;
}
```

---

## 클래스 수준

### God Class

너무 많은 책임을 가진 클래스. 필드 10개 이상, 메서드 15개 이상이 징후.

```java
// Before: OrderService가 주문, 결제, 배송, 알림, 통계를 모두 처리
@Service
public class OrderService {
    private final OrderRepository orderRepository;
    private final PaymentGateway paymentGateway;
    private final ShippingService shippingService;
    private final NotificationService notificationService;
    private final StatisticsService statisticsService;
    private final MemberRepository memberRepository;
    private final ProductRepository productRepository;
    private final CouponService couponService;

    public Order createOrder(...) { ... }
    public void cancelOrder(...) { ... }
    public void processPayment(...) { ... }
    public void refundPayment(...) { ... }
    public void arrangeShipping(...) { ... }
    public void trackShipping(...) { ... }
    public void sendNotification(...) { ... }
    public OrderStatistics getStatistics(...) { ... }
    // ... 20개 이상의 메서드
}
```

```java
// After: 책임별로 분리
@Service
public class OrderService {
    private final OrderRepository orderRepository;
    private final OrderValidator orderValidator;

    public Order createOrder(CreateOrderRequest request) {
        orderValidator.validate(request);
        Order order = Order.create(request);
        return orderRepository.save(order);
    }

    public void cancelOrder(Long orderId) {
        Order order = orderRepository.findById(orderId).orElseThrow();
        order.cancel();
        orderRepository.save(order);
    }
}

@Service
public class OrderPaymentService {
    // 결제 관련 책임만
}

@Service
public class OrderShippingService {
    // 배송 관련 책임만
}
```

**왜 개선인가**: 결제 로직을 수정할 때 배송 코드를 읽을 필요가 없다. 각 클래스를 독립적으로 테스트할 수 있다.

### Feature Envy

다른 객체의 데이터를 지나치게 사용하는 메서드. 로직이 데이터가 있는 곳에 있어야 한다.

```java
// Before: OrderService가 Order의 내부를 너무 많이 알고 있음
public boolean isEligibleForFreeShipping(Order order) {
    return order.getTotalPrice().compareTo(BigDecimal.valueOf(50000)) > 0
        && order.getItems().size() >= 3
        && order.getMember().getGrade() != Grade.BASIC;
}
```

```java
// After: 로직을 Order 도메인 객체로 이동
// Order.java
public boolean isEligibleForFreeShipping() {
    return totalPrice.compareTo(BigDecimal.valueOf(50000)) > 0
        && items.size() >= 3
        && member.getGrade() != Grade.BASIC;
}
```

### Data Class

getter/setter만 있고 행위가 없는 클래스. 도메인 로직이 Service에 흩어지는 원인.

```java
// Before: Order는 데이터만 들고 있고, 모든 로직이 Service에 있음
@Entity
public class Order {
    private OrderStatus status;
    private BigDecimal totalPrice;
    // getter, setter만 존재
}

// Service에서 Order의 상태를 직접 조작
order.setStatus(OrderStatus.CANCELLED);
order.setTotalPrice(BigDecimal.ZERO);
```

```java
// After: Order가 자신의 상태를 스스로 관리
@Entity
public class Order {
    private OrderStatus status;
    private BigDecimal totalPrice;

    public void cancel() {
        if (this.status != OrderStatus.PENDING) {
            throw new IllegalStateException("대기 상태의 주문만 취소할 수 있습니다");
        }
        this.status = OrderStatus.CANCELLED;
    }
}
```

### Inappropriate Intimacy / Middle Man

두 클래스가 서로의 내부를 과도하게 참조하거나, 위임만 하는 클래스가 존재하는 경우. 각각 캡슐화 강화 또는 중간 클래스 제거로 해결.

---

## 구조 수준

### 중복 코드

유사한 로직이 여러 곳에 산재. 단, 세 줄 이하 중복은 추상화보다 중복이 나을 수 있다.

### Shotgun Surgery

하나의 변경이 여러 클래스 수정을 요구. 관련 로직을 한 곳으로 모아야 한다.

### Divergent Change

하나의 클래스가 여러 이유로 변경. SRP 위반의 징후. God Class 분리로 해결.

### 순환 의존성

패키지/클래스 간 순환 참조. 인터페이스 추출 또는 이벤트 기반으로 끊는다.

```java
// Before: OrderService ↔ PaymentService 순환 참조
@Service
public class OrderService {
    private final PaymentService paymentService; // Order → Payment
}

@Service
public class PaymentService {
    private final OrderService orderService; // Payment → Order (순환!)
}
```

```java
// After: 이벤트로 의존성 방향 통일
@Service
public class OrderService {
    private final ApplicationEventPublisher eventPublisher;

    public void completeOrder(Long orderId) {
        Order order = orderRepository.findById(orderId).orElseThrow();
        order.complete();
        eventPublisher.publishEvent(new OrderCompletedEvent(order.getId()));
    }
}

@Service
public class PaymentService {
    @EventListener
    public void handleOrderCompleted(OrderCompletedEvent event) {
        // 결제 처리 — OrderService에 대한 의존성 없음
    }
}
```

---

## Spring 특화

### Controller 비대화

Controller에 비즈니스 로직이 존재. Controller는 요청/응답 변환만 담당해야 한다.

### 트랜잭션 경계 오류 (구조적 관점)

- Service가 아닌 곳(Controller, Repository)에 `@Transactional` 존재
- 하나의 트랜잭션에서 무관한 여러 도메인을 변경

### Repository 로직 누수

Service에서 여러 Repository 호출을 조합하여 직접 쿼리를 구성하는 경우.

### 빈 스코프 오용

Singleton 빈에 상태(mutable 필드)를 저장하면 동시성 문제 발생.

```java
// Before: Singleton인데 상태를 가짐 — 멀티스레드에서 버그
@Service
public class PriceCalculator {
    private BigDecimal lastCalculatedPrice; // 위험!

    public BigDecimal calculate(Order order) {
        this.lastCalculatedPrice = order.getTotalPrice().multiply(TAX_RATE);
        return this.lastCalculatedPrice;
    }
}
```

```java
// After: 상태를 제거하거나 지역 변수로 처리
@Service
public class PriceCalculator {
    public BigDecimal calculate(Order order) {
        return order.getTotalPrice().multiply(TAX_RATE);
    }
}
```

### 예외 처리 부재/과잉

- catch로 예외를 삼키기 (로그만 찍고 무시)
- 무분별한 `throws Exception`
- 비즈니스 예외와 시스템 예외를 구분하지 않음
