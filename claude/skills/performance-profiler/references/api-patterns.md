# API 응답 시간 패턴 카탈로그

요청-응답 흐름에서 지연을 유발하는 패턴과 해결 방법.

## 트랜잭션 내 외부 호출

외부 API 호출이 `@Transactional` 안에 있으면 DB 커넥션을 불필요하게 점유한다. 외부 서비스 타임아웃 시 트랜잭션까지 롤백될 수 있다.

```java
// 문제: 외부 호출이 트랜잭션 안에 있음
@Transactional
public void processOrder(Order order) {
    orderRepository.save(order);
    notificationService.sendEmail(order);  // 외부 호출 — DB 커넥션 점유
    paymentService.charge(order);          // 외부 호출 — 타임아웃 시 롤백
}
```

```java
// 해결 A: 트랜잭션과 외부 호출 분리
@Transactional
public void saveOrder(Order order) {
    orderRepository.save(order);
}

public void processOrder(Order order) {
    saveOrder(order);
    notificationService.sendEmail(order);  // 트랜잭션 밖
    paymentService.charge(order);          // 트랜잭션 밖
}

// 해결 B: 이벤트 기반 비동기 처리
@Transactional
public void saveOrder(Order order) {
    orderRepository.save(order);
    eventPublisher.publishEvent(new OrderSavedEvent(order.getId()));
}

@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
public void handleOrderSaved(OrderSavedEvent event) {
    notificationService.sendEmail(event.getOrderId());
}
```

**트레이드오프**: 분리하면 원자성이 깨진다. 외부 호출 실패 시 보상 트랜잭션이나 재시도 로직이 필요할 수 있다.

## 동기 외부 호출 직렬 실행

독립적인 외부 API를 순차적으로 호출하면 총 응답시간 = 각 호출 시간의 합.

```java
// 문제: 3개 외부 API 순차 호출 — 총 300ms+200ms+150ms = 650ms
public OrderDetail getOrderDetail(Long orderId) {
    Order order = orderRepository.findById(orderId).orElseThrow();
    UserInfo user = userServiceClient.getUser(order.getUserId());       // 300ms
    PaymentInfo payment = paymentClient.getPayment(order.getPaymentId()); // 200ms
    ShippingInfo shipping = shippingClient.getTracking(order.getTrackingId()); // 150ms
    return OrderDetail.of(order, user, payment, shipping);
}
```

```java
// 해결: CompletableFuture로 병렬 호출 — 총 max(300,200,150) = 300ms
public OrderDetail getOrderDetail(Long orderId) {
    Order order = orderRepository.findById(orderId).orElseThrow();

    CompletableFuture<UserInfo> userFuture = CompletableFuture
        .supplyAsync(() -> userServiceClient.getUser(order.getUserId()));
    CompletableFuture<PaymentInfo> paymentFuture = CompletableFuture
        .supplyAsync(() -> paymentClient.getPayment(order.getPaymentId()));
    CompletableFuture<ShippingInfo> shippingFuture = CompletableFuture
        .supplyAsync(() -> shippingClient.getTracking(order.getTrackingId()));

    CompletableFuture.allOf(userFuture, paymentFuture, shippingFuture).join();

    return OrderDetail.of(order, userFuture.join(), paymentFuture.join(), shippingFuture.join());
}
```

**트레이드오프**: 스레드 풀 관리가 필요하다. 에러 핸들링이 복잡해진다.

## 캐싱 부재

자주 조회되지만 변경이 적은 데이터에 캐시를 적용하지 않으면 매 요청마다 DB 조회.

```java
// 문제: 매 요청마다 설정 테이블 조회
public SystemConfig getConfig() {
    return configRepository.findByKey("system-config").orElseThrow();
}
```

```java
// 해결: Spring Cache 적용
@Cacheable("systemConfig")
public SystemConfig getConfig() {
    return configRepository.findByKey("system-config").orElseThrow();
}
```

**캐시 전략 선택 기준**:

| 기준 | Caffeine (로컬) | Redis (분산) |
|------|----------------|-------------|
| 인스턴스 | 단일 서버 | 멀티 서버 |
| 일관성 | 서버별 다를 수 있음 | 모든 서버 동일 |
| 속도 | 나노초 수준 | 밀리초 수준 (네트워크) |
| 데이터 크기 | 힙 메모리 제약 | 별도 메모리 |
| 적합한 데이터 | 설정값, 코드 테이블 | 세션, 사용자 데이터 |

**TTL 설정 원칙**: 데이터 변경 빈도와 허용 가능한 불일치 시간으로 결정한다.
- 거의 변경 없음 (코드 테이블): 1시간~24시간
- 가끔 변경 (상품 정보): 5분~30분
- 자주 변경 (재고): 30초~5분 또는 캐시 사용 안 함

## 응답에 불필요한 데이터

거대한 Entity를 그대로 JSON으로 반환하면 직렬화 비용 + 네트워크 전송량 증가.

```java
// 문제: Order Entity를 그대로 반환 — 연관 객체까지 전부 직렬화
@GetMapping("/orders")
public List<Order> getOrders() {
    return orderRepository.findAll(); // Entity 직접 반환
}
```

```java
// 해결: 필요한 필드만 DTO로 변환
@GetMapping("/orders")
public List<OrderSummaryResponse> getOrders() {
    return orderRepository.findAll().stream()
        .map(OrderSummaryResponse::from)
        .collect(toList());
}

// 또는 JPQL Projection으로 DB에서부터 필요한 필드만 조회
@Query("SELECT new com.example.dto.OrderSummary(o.id, o.status, o.totalPrice) FROM Order o")
List<OrderSummary> findAllSummaries();
```

## 트랜잭션 범위 과대

넓은 트랜잭션은 DB 커넥션을 오래 점유하고 락 범위를 넓힌다.

```java
// 문제: 전체 메서드가 하나의 트랜잭션 — 읽기와 쓰기가 혼재
@Transactional
public ReportResponse generateReport(Long reportId) {
    Report report = reportRepository.findById(reportId).orElseThrow(); // 읽기
    List<ReportData> data = dataRepository.findByReportId(reportId);    // 읽기
    ReportResult result = calculateReport(data);                         // 계산 (DB 불필요)
    report.setResult(result);                                            // 쓰기
    reportRepository.save(report);
    return ReportResponse.from(report);
}
```

```java
// 해결: 읽기와 쓰기 트랜잭션 분리
@Transactional(readOnly = true)
public ReportData loadReportData(Long reportId) {
    Report report = reportRepository.findById(reportId).orElseThrow();
    List<ReportData> data = dataRepository.findByReportId(reportId);
    return new ReportData(report, data);
}

@Transactional
public void saveReportResult(Long reportId, ReportResult result) {
    Report report = reportRepository.findById(reportId).orElseThrow();
    report.setResult(result);
}

public ReportResponse generateReport(Long reportId) {
    ReportData data = loadReportData(reportId);        // 읽기 트랜잭션
    ReportResult result = calculateReport(data);        // 트랜잭션 밖
    saveReportResult(reportId, result);                 // 쓰기 트랜잭션
    return ReportResponse.from(result);
}
```

## 로깅 과다

반복문 안에서 매번 로그를 출력하면 I/O 비용 누적.

```java
// 문제: 10000건 처리 시 10000줄 로그
for (Order order : orders) {
    log.info("Processing order: {}", order.getId());
    processOrder(order);
}
```

```java
// 해결: 요약 로그 또는 샘플링
log.info("Processing {} orders", orders.size());
int processed = 0;
for (Order order : orders) {
    processOrder(order);
    processed++;
}
log.info("Processed {} orders successfully", processed);
```
