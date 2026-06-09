# 메모리 사용 패턴 카탈로그

불필요한 객체 생성과 메모리 낭비 패턴.

## 대량 데이터 인메모리 로딩

DB에서 전체를 조회한 후 Java에서 필터링하면, 데이터 증가 시 OOM 위험.

```java
// 문제: 100만 건 전부 메모리에 올린 후 Java에서 필터링
List<Order> allOrders = orderRepository.findAll();
List<Order> filtered = allOrders.stream()
    .filter(o -> o.getStatus() == OrderStatus.PENDING)
    .filter(o -> o.getCreatedAt().isAfter(yesterday))
    .collect(toList());
```

```java
// 해결: DB에서 필터링
List<Order> filtered = orderRepository.findByStatusAndCreatedAtAfter(
    OrderStatus.PENDING, yesterday);
```

## String 연결

루프 내 `+` 연산자는 매번 새 String 객체를 생성한다.

```java
// 문제: 1000회 반복이면 1000개의 중간 String 객체 생성
String result = "";
for (OrderItem item : items) {
    result += item.getName() + ", "; // 매번 새 String 생성
}
```

```java
// 해결: StringBuilder 사용
StringBuilder sb = new StringBuilder();
for (OrderItem item : items) {
    sb.append(item.getName()).append(", ");
}
String result = sb.toString();

// 또는 Stream joining
String result = items.stream()
    .map(OrderItem::getName)
    .collect(Collectors.joining(", "));
```

## Autoboxing 반복

루프 내에서 primitive ↔ wrapper 변환이 반복되면 불필요한 객체 생성.

```java
// 문제: long → Long autoboxing이 매 반복 발생
List<Long> ids = new ArrayList<>();
for (long i = 0; i < 100000; i++) {
    ids.add(i); // autoboxing: long → Long 객체 생성
}
```

```java
// 해결: primitive 전용 컬렉션 사용 (Eclipse Collections, Trove 등)
// 또는 배열 사용
long[] ids = LongStream.range(0, 100000).toArray();
```

## 불필요한 객체 복사

```java
// 문제: 매번 새로운 리스트를 생성하여 반환
public List<OrderItem> getItems() {
    return new ArrayList<>(this.items); // 방어적 복사가 불필요한 경우
}
```

```java
// 해결: 불변 뷰 반환 (복사 없이 수정 방지)
public List<OrderItem> getItems() {
    return Collections.unmodifiableList(this.items);
}
```

## Static 컬렉션 누적

Static 필드에 데이터가 계속 쌓이면 GC가 수거하지 못해 메모리 누수 발생.

```java
// 문제: 요청마다 캐시가 쌓이고 절대 제거되지 않음
@Component
public class PriceCache {
    private static final Map<Long, BigDecimal> cache = new HashMap<>();

    public BigDecimal getPrice(Long productId) {
        return cache.computeIfAbsent(productId, id ->
            productRepository.findById(id).orElseThrow().getPrice()
        ); // 계속 쌓임, 만료 없음
    }
}
```

```java
// 해결 A: Spring Cache + TTL 설정
@Cacheable(value = "prices", key = "#productId")
public BigDecimal getPrice(Long productId) {
    return productRepository.findById(productId).orElseThrow().getPrice();
}

// 해결 B: Caffeine 캐시 (크기 제한 + 만료)
private final Cache<Long, BigDecimal> cache = Caffeine.newBuilder()
    .maximumSize(10000)
    .expireAfterWrite(Duration.ofMinutes(30))
    .build();
```

## 큰 객체 캐싱 누락

비용이 높은 계산 결과를 매번 재생성.

```java
// 문제: 매 요청마다 전체 카테고리 트리를 DB에서 조회 + 구성
public CategoryTree getCategoryTree() {
    List<Category> all = categoryRepository.findAll(); // 매번 DB 조회
    return buildTree(all); // 매번 트리 구성
}
```

```java
// 해결: 캐시 적용 (카테고리는 변경 빈도가 낮음)
@Cacheable(value = "categoryTree")
public CategoryTree getCategoryTree() {
    List<Category> all = categoryRepository.findAll();
    return buildTree(all);
}

// 카테고리 변경 시 캐시 무효화
@CacheEvict(value = "categoryTree", allEntries = true)
public void updateCategory(Category category) { ... }
```

## 스트림 toList() 남용

중간 결과를 불필요하게 리스트로 수집한 후 다시 스트림으로 처리.

```java
// 문제: 불필요한 중간 리스트 생성
List<OrderItem> filtered = items.stream()
    .filter(item -> item.getQuantity() > 0)
    .collect(toList()); // 중간 리스트 생성

List<String> names = filtered.stream() // 다시 스트림
    .map(OrderItem::getName)
    .collect(toList());
```

```java
// 해결: 스트림 체이닝
List<String> names = items.stream()
    .filter(item -> item.getQuantity() > 0)
    .map(OrderItem::getName)
    .collect(toList());
```
