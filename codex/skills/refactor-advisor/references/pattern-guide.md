# 디자인 패턴 적용 가이드

코드에서 패턴 적용이 자연스러운 징후를 발견했을 때 참고한다. 패턴 적용의 목적은 코드를 더 읽기 쉽고 확장하기 쉽게 만드는 것이지, 패턴을 사용하는 것 자체가 아니다.

## Strategy 패턴

**징후**: if/else 또는 switch로 타입별 분기하는 코드가 여러 메서드에 반복됨.

```java
// Before: 할인 정책이 if/else 체인
public BigDecimal calculateDiscount(Order order) {
    if (order.getType() == OrderType.EARLY_BIRD) {
        return order.getPrice().multiply(BigDecimal.valueOf(0.2));
    } else if (order.getType() == OrderType.BULK) {
        return order.getPrice().multiply(BigDecimal.valueOf(0.15));
    } else if (order.getType() == OrderType.VIP) {
        return order.getPrice().multiply(BigDecimal.valueOf(0.25));
    }
    return BigDecimal.ZERO;
}
```

```java
// After: Strategy 패턴으로 분리
public interface DiscountPolicy {
    BigDecimal calculate(Order order);
}

@Component
public class EarlyBirdDiscount implements DiscountPolicy {
    public BigDecimal calculate(Order order) {
        return order.getPrice().multiply(BigDecimal.valueOf(0.2));
    }
}

// Service에서는 Map으로 전략 선택
@Service
public class DiscountService {
    private final Map<OrderType, DiscountPolicy> policies;

    public DiscountService(List<DiscountPolicy> policyList) {
        this.policies = policyList.stream()
            .collect(toMap(DiscountPolicy::getType, identity()));
    }

    public BigDecimal calculateDiscount(Order order) {
        return policies.getOrDefault(order.getType(), DiscountPolicy.NONE)
            .calculate(order);
    }
}
```

**트레이드오프**: 클래스 수 증가. 분기가 2-3개이고 변경 빈도가 낮으면 if/else가 더 간단할 수 있다.

## Template Method 패턴

**징후**: 여러 클래스에서 동일한 절차를 따르되, 일부 단계만 다름.

```java
// Before: 각 Export 클래스가 비슷한 구조를 반복
public class PdfExporter {
    public void export(Report report) {
        validate(report);          // 동일
        String data = formatForPdf(report);  // 다름
        write(data, "report.pdf"); // 동일
        log("PDF 생성 완료");       // 동일
    }
}

public class ExcelExporter {
    public void export(Report report) {
        validate(report);            // 동일
        String data = formatForExcel(report); // 다름
        write(data, "report.xlsx");  // 동일
        log("Excel 생성 완료");       // 동일
    }
}
```

```java
// After: 공통 절차를 추상 클래스로
public abstract class ReportExporter {
    public final void export(Report report) {
        validate(report);
        String data = format(report);
        write(data, getFileName());
        log(getFormat() + " 생성 완료");
    }

    protected abstract String format(Report report);
    protected abstract String getFileName();
    protected abstract String getFormat();
}
```

**트레이드오프**: 상속 기반이므로 조합이 필요한 경우 유연성이 떨어진다. 변형이 2개 이하면 과도한 추상화일 수 있다.

## Factory 패턴

**징후**: 객체 생성 로직이 복잡하거나, 조건에 따라 다른 구현체를 생성.

```java
// Before: Service에서 직접 생성 로직 처리
public Notification createNotification(NotificationType type, String message) {
    if (type == NotificationType.EMAIL) {
        EmailNotification email = new EmailNotification();
        email.setSubject("알림");
        email.setBody(message);
        email.setSmtpServer(smtpConfig.getServer());
        return email;
    } else if (type == NotificationType.SMS) {
        SmsNotification sms = new SmsNotification();
        sms.setMessage(message.substring(0, 80));
        sms.setProvider(smsConfig.getProvider());
        return sms;
    }
    throw new IllegalArgumentException("지원하지 않는 타입: " + type);
}
```

```java
// After: Factory로 생성 로직 캡슐화
@Component
public class NotificationFactory {
    private final Map<NotificationType, NotificationCreator> creators;

    public Notification create(NotificationType type, String message) {
        return creators.getOrDefault(type, t -> { throw new IllegalArgumentException("지원하지 않는 타입: " + t); })
            .create(message);
    }
}
```

## Builder 패턴

**징후**: 생성자 파라미터가 많거나 선택적 파라미터가 있는 객체.

Java에서는 Lombok `@Builder` 또는 record + static factory method를 활용하면 직접 Builder 클래스를 만들 필요가 거의 없다.

## Observer / Event 패턴

**징후**: 한 동작이 여러 후속 작업을 트리거하는데, 그 후속 작업들이 서로 독립적.

Spring의 `ApplicationEventPublisher`와 `@EventListener`를 활용한다. 순환 의존성 해소에도 효과적이다. 코드 예시는 `smell-catalog.md`의 순환 의존성 섹션 참조.

**트레이드오프**: 흐름이 암묵적이 되어 디버깅이 어려워질 수 있다. 이벤트가 2-3개 이하면 직접 호출이 더 명확할 수 있다.

## Decorator 패턴

**징후**: 기존 기능에 부가 기능(로깅, 캐싱, 검증 등)을 추가해야 하는데, 원본 코드를 수정하고 싶지 않음.

Spring AOP(`@Aspect`)가 사실상 Decorator 역할을 하므로, 별도 Decorator 클래스를 만들기 전에 AOP로 해결 가능한지 먼저 검토한다.

## 패턴 적용 판단 기준

| 질문 | "예"면 적용 고려 |
|------|----------------|
| 같은 분기가 3곳 이상에서 반복되는가? | Strategy |
| 같은 절차에서 일부 단계만 다른 클래스가 3개 이상인가? | Template Method |
| 객체 생성이 조건에 따라 복잡하게 달라지는가? | Factory |
| 한 동작의 후속 작업이 3개 이상이고 서로 독립적인가? | Observer/Event |
| 기존 코드 수정 없이 횡단 관심사를 추가해야 하는가? | Decorator/AOP |

**주의**: 위 조건을 만족해도, 패턴 적용 후 코드가 더 읽기 어려워진다면 적용하지 않는다.
