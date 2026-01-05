# Spring Boot 분석 가이드

## 레이어 구조

| 레이어 | 패턴 | 역할 |
|--------|------|------|
| Controller | `*Controller.java` | HTTP 엔드포인트, 요청/응답 처리 |
| Service | `*Service.java`, `*ServiceImpl.java` | 비즈니스 로직 |
| Repository | `*Repository.java`, `*Mapper.java` | 데이터 접근 |
| Domain | `*Entity.java`, `*Aggregate.java` | 도메인 모델 |
| DTO | `*Dto.java`, `*Request.java`, `*Response.java` | 데이터 전송 객체 |
| Config | `*Config.java`, `*Configuration.java` | 설정 |

## 주요 어노테이션

### 진입점 식별
- `@RestController`, `@Controller`: HTTP 엔드포인트
- `@GetMapping`, `@PostMapping`, `@RequestMapping`: 라우팅
- `@Scheduled`: 스케줄러 진입점
- `@KafkaListener`, `@RabbitListener`: 메시지 리스너

### 의존성 추적
- `@Autowired`, `@RequiredArgsConstructor`: 의존성 주입
- `@Transactional`: 트랜잭션 경계
- `@Async`: 비동기 처리

### 데이터 구조
- `@Entity`, `@Table`: JPA 엔티티
- `@Id`, `@GeneratedValue`: 기본키
- `@OneToMany`, `@ManyToOne`, `@ManyToMany`: 관계 매핑

## 분석 시 확인 사항

1. **예외 처리**: `@ExceptionHandler`, `@ControllerAdvice` 존재 여부
2. **트랜잭션**: `@Transactional` 적용 범위와 전파 설정
3. **캐싱**: `@Cacheable`, `@CacheEvict` 사용 여부
4. **보안**: `@PreAuthorize`, `@Secured` 적용 여부
5. **검증**: `@Valid`, `@Validated` 사용 여부

## MyBatis 프로젝트

JPA 대신 MyBatis 사용 시:
- `*Mapper.java`: 인터페이스
- `*Mapper.xml`: SQL 매핑 파일
- `resources/mapper/` 디렉토리 확인
