package com.example.order.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * OrderService 단위 테스트 예시
 *
 * 이 테스트는 다음 패턴을 보여줍니다:
 * - Given-When-Then 구조
 * - BDDMockito 사용
 * - @Nested로 테스트 그룹화
 * - @ParameterizedTest로 반복 케이스 처리
 * - Happy Path / Edge Case / Error Case 커버
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("OrderService 테스트")
class OrderServiceTest {

    @InjectMocks
    private OrderService orderService;

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private StockService stockService;

    // 공통 fixture
    private Product product;
    private Member member;

    @BeforeEach
    void setUp() {
        product = Product.builder()
            .id(1L)
            .name("노트북")
            .price(1_000_000)
            .stock(10)
            .build();

        member = Member.builder()
            .id(1L)
            .name("홍길동")
            .grade(MemberGrade.REGULAR)
            .build();
    }

    @Nested
    @DisplayName("주문 생성 시")
    class CreateOrder {

        @Test
        @DisplayName("유효한 요청이면 주문이 생성된다")
        void should_createOrder_when_validRequest() {
            // given
            var request = new CreateOrderRequest(product.getId(), 2);
            given(productRepository.findById(anyLong())).willReturn(Optional.of(product));
            given(stockService.decrease(anyLong(), any())).willReturn(true);
            given(orderRepository.save(any(Order.class))).willReturn(1L);

            // when
            Long orderId = orderService.createOrder(request);

            // then
            assertThat(orderId).isEqualTo(1L);
            then(stockService).should().decrease(product.getId(), 2);
            then(orderRepository).should().save(any(Order.class));
        }

        @Test
        @DisplayName("상품이 존재하지 않으면 예외가 발생한다")
        void should_throwException_when_productNotFound() {
            // given
            var request = new CreateOrderRequest(999L, 2);
            given(productRepository.findById(999L)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> orderService.createOrder(request))
                .isInstanceOf(ProductNotFoundException.class)
                .hasMessage("상품을 찾을 수 없습니다: 999");
        }

        @Test
        @DisplayName("재고가 부족하면 예외가 발생한다")
        void should_throwException_when_stockInsufficient() {
            // given
            var request = new CreateOrderRequest(product.getId(), 100);
            given(productRepository.findById(anyLong())).willReturn(Optional.of(product));
            given(stockService.decrease(anyLong(), any())).willReturn(false);

            // when & then
            assertThatThrownBy(() -> orderService.createOrder(request))
                .isInstanceOf(InsufficientStockException.class);
        }

        @ParameterizedTest
        @ValueSource(ints = {0, -1, -100})
        @DisplayName("수량이 0 이하면 예외가 발생한다")
        void should_throwException_when_invalidQuantity(int quantity) {
            // given
            var request = new CreateOrderRequest(product.getId(), quantity);

            // when & then
            assertThatThrownBy(() -> orderService.createOrder(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("수량");
        }

        @Test
        @DisplayName("요청이 null이면 예외가 발생한다")
        void should_throwException_when_requestIsNull() {
            // when & then
            assertThatThrownBy(() -> orderService.createOrder(null))
                .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("주문 취소 시")
    class CancelOrder {

        @Test
        @DisplayName("대기 상태 주문은 취소된다")
        void should_cancelOrder_when_pendingStatus() {
            // given
            var order = Order.builder()
                .id(1L)
                .status(OrderStatus.PENDING)
                .productId(product.getId())
                .quantity(2)
                .build();
            given(orderRepository.findById(1L)).willReturn(Optional.of(order));

            // when
            orderService.cancelOrder(1L);

            // then
            assertThat(order.getStatus()).isEqualTo(OrderStatus.CANCELLED);
            then(stockService).should().increase(product.getId(), 2);
        }

        @Test
        @DisplayName("배송 완료된 주문은 취소할 수 없다")
        void should_throwException_when_alreadyDelivered() {
            // given
            var order = Order.builder()
                .id(1L)
                .status(OrderStatus.DELIVERED)
                .build();
            given(orderRepository.findById(1L)).willReturn(Optional.of(order));

            // when & then
            assertThatThrownBy(() -> orderService.cancelOrder(1L))
                .isInstanceOf(OrderCancelException.class)
                .hasMessageContaining("취소할 수 없는 상태");
        }
    }

    @Nested
    @DisplayName("주문 조회 시")
    class GetOrder {

        @Test
        @DisplayName("존재하는 주문 ID로 조회하면 주문이 반환된다")
        void should_returnOrder_when_validId() {
            // given
            var order = Order.builder().id(1L).build();
            given(orderRepository.findById(1L)).willReturn(Optional.of(order));

            // when
            var result = orderService.getOrder(1L);

            // then
            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(1L);
        }

        @Test
        @DisplayName("존재하지 않는 주문 ID로 조회하면 예외가 발생한다")
        void should_throwException_when_orderNotFound() {
            // given
            given(orderRepository.findById(999L)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> orderService.getOrder(999L))
                .isInstanceOf(OrderNotFoundException.class);
        }
    }
}
