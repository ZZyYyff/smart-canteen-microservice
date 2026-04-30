package com.smartcanteen.order.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.smartcanteen.common.enums.OrderStatus;
import com.smartcanteen.common.exception.BusinessException;
import com.smartcanteen.common.result.Result;
import com.smartcanteen.order.dto.CreateOrderDTO;
import com.smartcanteen.order.dto.OrderItemDTO;
import com.smartcanteen.order.entity.Order;
import com.smartcanteen.order.entity.OrderItem;
import com.smartcanteen.order.feign.MenuFeignClient;
import com.smartcanteen.order.feign.PickupFeignClient;
import com.smartcanteen.order.feign.dto.PickupQueueRequest;
import com.smartcanteen.order.feign.dto.StockRequest;
import com.smartcanteen.order.mapper.OrderItemMapper;
import com.smartcanteen.order.mapper.OrderMapper;
import com.smartcanteen.order.service.impl.OrderServiceImpl;
import com.smartcanteen.order.vo.OrderVO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * 订单服务单元测试
 */
@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock private OrderMapper orderMapper;
    @Mock private OrderItemMapper orderItemMapper;
    @Mock private MenuFeignClient menuFeignClient;
    @Mock private PickupFeignClient pickupFeignClient;
    @InjectMocks private OrderServiceImpl orderService;

    private CreateOrderDTO createOrderDTO;
    private Order mockOrder;

    @BeforeEach
    void setUp() {
        OrderItemDTO item = new OrderItemDTO();
        item.setDishId(10L);
        item.setDishName("红烧肉");
        item.setPrice(BigDecimal.valueOf(25.00));
        item.setQuantity(2);

        createOrderDTO = new CreateOrderDTO();
        createOrderDTO.setWindowId(1L);
        createOrderDTO.setItems(List.of(item));

        mockOrder = new Order();
        mockOrder.setId(100L);
        mockOrder.setUserId(1L);
        mockOrder.setWindowId(1L);
        mockOrder.setTotalAmount(BigDecimal.valueOf(50.00));
        mockOrder.setStatus(OrderStatus.CREATED.name());
        mockOrder.setPickupNo(123);
        mockOrder.setPickupCode("567890");
    }

    // ==================== 下单测试 ====================

    @Test
    @DisplayName("1. 下单成功")
    void testCreateOrderSuccess() {
        when(menuFeignClient.deductStock(eq(10L), any(StockRequest.class)))
                .thenReturn(Result.success());
        when(orderMapper.insert(any(Order.class))).thenReturn(1);
        when(orderItemMapper.insert(any(OrderItem.class))).thenReturn(1);

        OrderVO result = orderService.createOrder(1L, createOrderDTO);
        assertNotNull(result);
        assertEquals(OrderStatus.CREATED.name(), result.getStatus());
        assertNotNull(result.getPickupNo());
        assertNotNull(result.getPickupCode());
        assertEquals(1, result.getItems().size());
        assertEquals("红烧肉", result.getItems().get(0).getDishName());

        verify(menuFeignClient).deductStock(eq(10L), any(StockRequest.class));
        verify(orderMapper).insert(any(Order.class));
        verify(orderItemMapper).insert(any(OrderItem.class));
    }

    @Test
    @DisplayName("2. 库存不足下单失败，并回滚已扣库存")
    void testCreateOrderStockInsufficient() {
        // 两个菜品的订单
        OrderItemDTO item1 = new OrderItemDTO();
        item1.setDishId(10L); item1.setDishName("红烧肉");
        item1.setPrice(BigDecimal.valueOf(25)); item1.setQuantity(2);

        OrderItemDTO item2 = new OrderItemDTO();
        item2.setDishId(20L); item2.setDishName("青菜");
        item2.setPrice(BigDecimal.valueOf(10)); item2.setQuantity(5);

        CreateOrderDTO dto = new CreateOrderDTO();
        dto.setWindowId(1L);
        dto.setItems(List.of(item1, item2));

        // item1 扣减成功，item2 扣减失败
        when(menuFeignClient.deductStock(eq(10L), any(StockRequest.class)))
                .thenReturn(Result.success());
        when(menuFeignClient.deductStock(eq(20L), any(StockRequest.class)))
                .thenReturn(Result.fail(3002, "库存不足"));
        // 回滚 item1 的库存
        when(menuFeignClient.restoreStock(eq(10L), any(StockRequest.class)))
                .thenReturn(Result.success());

        BusinessException ex = assertThrows(BusinessException.class,
                () -> orderService.createOrder(1L, dto));
        assertThat(ex.getMessage()).contains("库存不足");

        // 验证 item1 被回滚
        verify(menuFeignClient).restoreStock(eq(10L), any(StockRequest.class));
        // 订单未创建
        verify(orderMapper, never()).insert(any(Order.class));
    }

    @Test
    @DisplayName("3. 多菜品下单，总金额正确")
    void testCreateOrderTotalAmount() {
        OrderItemDTO item1 = new OrderItemDTO();
        item1.setDishId(10L); item1.setDishName("红烧肉");
        item1.setPrice(BigDecimal.valueOf(25)); item1.setQuantity(2);

        OrderItemDTO item2 = new OrderItemDTO();
        item2.setDishId(20L); item2.setDishName("青菜");
        item2.setPrice(BigDecimal.valueOf(10)); item2.setQuantity(3);

        CreateOrderDTO dto = new CreateOrderDTO();
        dto.setWindowId(1L);
        dto.setItems(List.of(item1, item2));

        when(menuFeignClient.deductStock(anyLong(), any(StockRequest.class)))
                .thenReturn(Result.success());
        when(orderMapper.insert(any(Order.class))).thenReturn(1);
        when(orderItemMapper.insert(any(OrderItem.class))).thenReturn(1);

        OrderVO result = orderService.createOrder(1L, dto);
        // 25*2 + 10*3 = 50 + 30 = 80
        assertEquals(0, BigDecimal.valueOf(80.00).compareTo(result.getTotalAmount()));
    }

    // ==================== 查询测试 ====================

    @Test
    @DisplayName("4. 查询订单详情成功")
    void testGetOrderById() {
        when(orderMapper.selectById(100L)).thenReturn(mockOrder);
        when(orderItemMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(List.of());

        OrderVO result = orderService.getOrderById(100L);
        assertNotNull(result);
        assertEquals(100L, result.getId());
        assertEquals(OrderStatus.CREATED.name(), result.getStatus());
    }

    @Test
    @DisplayName("5. 查询不存在的订单")
    void testGetOrderNotFound() {
        when(orderMapper.selectById(999L)).thenReturn(null);
        assertThrows(BusinessException.class, () -> orderService.getOrderById(999L));
    }

    // ==================== 取消订单测试 ====================

    @Test
    @DisplayName("6. 取消订单成功并恢复库存")
    void testCancelOrderSuccess() {
        OrderItem item = new OrderItem();
        item.setDishId(10L);
        item.setQuantity(2);

        when(orderMapper.selectById(100L)).thenReturn(mockOrder);
        when(orderMapper.updateById(any(Order.class))).thenReturn(1);
        when(orderItemMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(List.of(item));
        when(menuFeignClient.restoreStock(eq(10L), any(StockRequest.class)))
                .thenReturn(Result.success());

        OrderVO result = orderService.cancelOrder(100L, 1L);
        assertEquals(OrderStatus.CANCELLED.name(), result.getStatus());

        // 验证恢复库存被调用
        verify(menuFeignClient).restoreStock(eq(10L), any(StockRequest.class));
    }

    @Test
    @DisplayName("7. 已完成订单不可取消")
    void testCancelCompletedOrderFails() {
        mockOrder.setStatus(OrderStatus.COMPLETED.name());
        when(orderMapper.selectById(100L)).thenReturn(mockOrder);

        BusinessException ex = assertThrows(BusinessException.class,
                () -> orderService.cancelOrder(100L, 1L));
        assertThat(ex.getMessage()).contains("不允许取消");
    }

    // ==================== 状态流转测试 ====================

    @Test
    @DisplayName("8. 接单成功：CREATED → ACCEPTED")
    void testAcceptOrder() {
        when(orderMapper.selectById(100L)).thenReturn(mockOrder);
        when(orderMapper.updateById(any(Order.class))).thenReturn(1);
        when(orderItemMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(List.of());

        OrderVO result = orderService.acceptOrder(100L);
        assertEquals(OrderStatus.ACCEPTED.name(), result.getStatus());
    }

    @Test
    @DisplayName("9. 制作完成进入待取餐，并调用 pickup 服务")
    void testReadyOrder() {
        mockOrder.setStatus(OrderStatus.COOKING.name());
        when(orderMapper.selectById(100L)).thenReturn(mockOrder);
        when(orderMapper.updateById(any(Order.class))).thenReturn(1);
        when(orderItemMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(List.of());
        when(pickupFeignClient.addToQueue(any(PickupQueueRequest.class)))
                .thenReturn(Result.success());

        OrderVO result = orderService.readyOrder(100L);
        assertEquals(OrderStatus.WAIT_PICKUP.name(), result.getStatus());

        // 验证 pickup 服务被调用
        verify(pickupFeignClient).addToQueue(any(PickupQueueRequest.class));
    }

    @Test
    @DisplayName("10. 非法状态流转：CREATED 直接跳到 COOKING 失败")
    void testInvalidTransition() {
        // mockOrder status is CREATED by default, can't go directly to cooking
        when(orderMapper.selectById(100L)).thenReturn(mockOrder);

        BusinessException ex = assertThrows(BusinessException.class,
                () -> orderService.cookingOrder(100L));
        assertThat(ex.getMessage()).contains("不允许此操作");
    }

    @Test
    @DisplayName("11. 正确流转：CREATED → ACCEPTED → COOKING → WAIT_PICKUP → COMPLETED")
    void testFullStatusFlow() {
        // Step 1: ACCEPTED
        when(orderMapper.selectById(100L)).thenReturn(mockOrder);
        when(orderMapper.updateById(any(Order.class))).thenReturn(1);
        when(orderItemMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(List.of());
        assertEquals(OrderStatus.ACCEPTED.name(), orderService.acceptOrder(100L).getStatus());

        // Step 2: COOKING
        mockOrder.setStatus(OrderStatus.ACCEPTED.name());
        when(orderMapper.selectById(100L)).thenReturn(mockOrder);
        assertEquals(OrderStatus.COOKING.name(), orderService.cookingOrder(100L).getStatus());

        // Step 3: WAIT_PICKUP
        mockOrder.setStatus(OrderStatus.COOKING.name());
        when(orderMapper.selectById(100L)).thenReturn(mockOrder);
        when(pickupFeignClient.addToQueue(any(PickupQueueRequest.class))).thenReturn(Result.success());
        assertEquals(OrderStatus.WAIT_PICKUP.name(), orderService.readyOrder(100L).getStatus());

        // Step 4: COMPLETED
        mockOrder.setStatus(OrderStatus.WAIT_PICKUP.name());
        when(orderMapper.selectById(100L)).thenReturn(mockOrder);
        assertEquals(OrderStatus.COMPLETED.name(), orderService.completeOrder(100L).getStatus());
    }

    @Test
    @DisplayName("12. 查询我的订单列表")
    void testGetMyOrders() {
        when(orderMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(List.of(mockOrder));
        when(orderItemMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(List.of());

        List<OrderVO> results = orderService.getMyOrders(1L);
        assertEquals(1, results.size());
        assertEquals(100L, results.get(0).getId());
    }
}
