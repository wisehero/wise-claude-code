# 알고리즘 복잡도 패턴 카탈로그

코드 내 비효율적 알고리즘 패턴과 최적화 방법.

## O(n²) 중첩 루프

내부에서 `List.contains`, `indexOf`, `remove` 등 O(n) 연산을 호출하면 전체가 O(n²).

```java
// 문제: O(n*m) — targetIds가 List이면 contains가 O(m)
List<Long> targetIds = getTargetIds();
for (Order order : orders) {
    if (targetIds.contains(order.getId())) {
        processOrder(order);
    }
}
```

```java
// 해결: O(n+m) — Set.contains는 O(1)
Set<Long> targetIdSet = new HashSet<>(targetIds);
for (Order order : orders) {
    if (targetIdSet.contains(order.getId())) {
        processOrder(order);
    }
}
```

## 비효율적 자료구조 선택

```java
// 문제: ID로 자주 조회하는데 List를 사용 — 매번 O(n) 순회
List<Product> products = productRepository.findAll();

Product findById(Long id) {
    return products.stream()
        .filter(p -> p.getId().equals(id))
        .findFirst()
        .orElse(null); // O(n)
}
```

```java
// 해결: Map으로 변환 — O(1) 조회
Map<Long, Product> productMap = productRepository.findAll().stream()
    .collect(toMap(Product::getId, identity()));

Product findById(Long id) {
    return productMap.get(id); // O(1)
}
```

## 반복 계산

루프 내에서 변하지 않는 값을 매번 재계산.

```java
// 문제: getTaxRate()가 매번 DB 조회 또는 복잡한 계산
for (OrderItem item : items) {
    BigDecimal tax = item.getPrice().multiply(getTaxRate()); // 매 반복 호출
    item.setTaxAmount(tax);
}
```

```java
// 해결: 루프 밖에서 한 번만 계산
BigDecimal taxRate = getTaxRate();
for (OrderItem item : items) {
    BigDecimal tax = item.getPrice().multiply(taxRate);
    item.setTaxAmount(tax);
}
```

## 불필요한 정렬

```java
// 문제: 전체 정렬 후 상위 3개만 사용 — O(n log n)
List<Order> sorted = orders.stream()
    .sorted(comparing(Order::getTotalPrice).reversed())
    .collect(toList());
List<Order> top3 = sorted.subList(0, 3);
```

```java
// 해결: PriorityQueue 또는 limit 사용 — O(n log k)
List<Order> top3 = orders.stream()
    .sorted(comparing(Order::getTotalPrice).reversed())
    .limit(3)
    .collect(toList());
```

## 스트림 오용

```java
// 문제: 같은 컬렉션에 여러 번 스트림 — 매번 전체 순회
long totalQuantity = items.stream().mapToLong(OrderItem::getQuantity).sum();
long totalPrice = items.stream().mapToLong(OrderItem::getPrice).sum();
long itemCount = items.stream().count();
```

```java
// 해결: 한 번의 순회로 통합
long totalQuantity = 0, totalPrice = 0, itemCount = 0;
for (OrderItem item : items) {
    totalQuantity += item.getQuantity();
    totalPrice += item.getPrice();
    itemCount++;
}
// 또는 커스텀 Collector 사용
```

**주의**: 데이터가 소량(수백 건 이하)이면 가독성을 위해 스트림을 여러 번 쓰는 게 나을 수 있다.
