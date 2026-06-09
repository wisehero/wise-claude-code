# DB 쿼리 성능 패턴 카탈로그

JPA와 MyBatis에서 자주 발견되는 성능 병목 패턴과 해결 방법.

## JPA 패턴

### N+1 문제

LAZY 로딩된 연관 엔티티를 루프에서 접근하면, 부모 1회 + 자식 N회 쿼리가 발생한다.

```java
// 문제: 주문 100건이면 101회 쿼리 (1 + 100)
List<Order> orders = orderRepository.findAll();
for (Order order : orders) {
    order.getItems().size(); // 각 주문마다 SELECT 발생
}
```

```java
// 해결 A: Fetch Join — 1회 쿼리로 해결
@Query("SELECT o FROM Order o JOIN FETCH o.items")
List<Order> findAllWithItems();

// 해결 B: @EntityGraph — 선언적 방식
@EntityGraph(attributePaths = {"items"})
List<Order> findAll();

// 해결 C: Batch Size — 글로벌 설정 (N+1을 1+ceil(N/size)로 줄임)
// application.yml
spring.jpa.properties.hibernate.default_batch_fetch_size: 100
```

**선택 기준**:
- Fetch Join: 특정 쿼리에서만 필요할 때. 컬렉션 2개 이상 JOIN 시 MultipleBagFetchException 주의
- @EntityGraph: 다양한 조합이 필요할 때
- Batch Size: 전역적으로 N+1을 줄이고 싶을 때

### 불필요한 EAGER 로딩

사용하지 않는 연관 관계를 항상 로딩.

```java
// 문제: Order 조회할 때마다 member, items, payment 전부 로딩
@Entity
public class Order {
    @ManyToOne(fetch = FetchType.EAGER)  // 항상 JOIN
    private Member member;

    @OneToMany(fetch = FetchType.EAGER)  // 항상 JOIN
    private List<OrderItem> items;
}
```

```java
// 해결: LAZY가 기본, 필요할 때만 Fetch Join
@ManyToOne(fetch = FetchType.LAZY)
private Member member;

@OneToMany(fetch = FetchType.LAZY)
private List<OrderItem> items;
```

### 페이징 없는 목록 조회

```java
// 문제: 데이터 100만 건이면 전부 메모리에 올림
List<Order> orders = orderRepository.findAll();

// 해결: 페이징 적용
Page<Order> orders = orderRepository.findAll(PageRequest.of(0, 20));
```

### 벌크 연산 누락

```java
// 문제: 1000건 저장 시 INSERT 1000회
for (Order order : orders) {
    orderRepository.save(order);
}

// 해결: saveAll로 배치 처리 (+ hibernate.jdbc.batch_size 설정)
orderRepository.saveAll(orders);
```

### 카운트 쿼리 비효율

```java
// 문제: 존재 여부만 확인하는데 전체 COUNT
long count = orderRepository.countByMemberId(memberId);
if (count > 0) { ... }

// 해결: exists 사용 — 1건만 확인하고 종료
boolean exists = orderRepository.existsByMemberId(memberId);
```

### 인덱스 누락 가능성

WHERE/ORDER BY에서 자주 사용하는 컬럼에 인덱스가 없으면 Full Table Scan 발생.

```java
// Repository에서 자주 호출되는 쿼리
List<Order> findByStatusAndCreatedAtBetween(OrderStatus status, LocalDateTime from, LocalDateTime to);

// Entity에 인덱스 추가 권장
@Table(indexes = {
    @Index(name = "idx_order_status_created", columnList = "status, created_at")
})
@Entity
public class Order { ... }
```

### 쿼리 내 함수 사용

WHERE 절에서 컬럼에 함수를 적용하면 인덱스를 타지 못한다.

```sql
-- 문제: 인덱스 무력화
SELECT * FROM orders WHERE YEAR(created_at) = 2024;

-- 해결: 범위 조건으로 변경
SELECT * FROM orders WHERE created_at >= '2024-01-01' AND created_at < '2025-01-01';
```

---

## MyBatis 패턴

### foreach 과도한 반복

```xml
<!-- 문제: IN 절에 10000개 파라미터 — 쿼리 파싱 비용 + DB 부하 -->
<select id="findByIds">
    SELECT * FROM orders WHERE id IN
    <foreach collection="ids" item="id" open="(" separator="," close=")">
        #{id}
    </foreach>
</select>
```

```java
// 해결: 청크 단위로 분할 호출
Lists.partition(ids, 1000).stream()
    .flatMap(chunk -> orderMapper.findByIds(chunk).stream())
    .collect(toList());
```

### 불필요한 동적 쿼리 분기

```xml
<!-- 문제: 항상 같은 조건인데 불필요한 if 분기가 매번 파싱됨 -->
<select id="findOrders">
    SELECT * FROM orders WHERE 1=1
    <if test="status != null">AND status = #{status}</if>
    <if test="memberId != null">AND member_id = #{memberId}</if>
    <!-- 20개 이상의 조건... -->
</select>
```

```xml
<!-- 해결: 자주 쓰이는 조합은 별도 쿼리로 분리 -->
<select id="findByMemberAndStatus">
    SELECT * FROM orders WHERE member_id = #{memberId} AND status = #{status}
</select>
```

### SELECT * 사용

```xml
<!-- 문제: 불필요한 BLOB/CLOB 컬럼까지 로딩 -->
<select id="findAll" resultType="Order">
    SELECT * FROM orders
</select>

<!-- 해결: 필요한 컬럼만 명시 -->
<select id="findAll" resultType="OrderSummary">
    SELECT id, status, total_price, created_at FROM orders
</select>
```
