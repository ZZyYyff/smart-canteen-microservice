package com.smartcanteen.order.task;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.smartcanteen.common.enums.OrderStatus;
import com.smartcanteen.common.result.Result;
import com.smartcanteen.order.entity.Order;
import com.smartcanteen.order.entity.OrderItem;
import com.smartcanteen.order.feign.MenuFeignClient;
import com.smartcanteen.order.feign.dto.StockRequest;
import com.smartcanteen.order.mapper.OrderItemMapper;
import com.smartcanteen.order.mapper.OrderMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderTimeoutTaskTest {

    @Mock private OrderMapper orderMapper;
    @Mock private OrderItemMapper orderItemMapper;
    @Mock private MenuFeignClient menuFeignClient;
    @InjectMocks private OrderTimeoutTask timeoutTask;

    private Order order;

    @BeforeEach
    void setUp() {
        timeoutTask = new OrderTimeoutTask(orderMapper, orderItemMapper, menuFeignClient);
        // 默认值: createdTimeoutMinutes=15, acceptedTimeoutMinutes=30
        // 测试数据中 createdAt/updatedAt 设置为 20/40 分钟前即可触发超时

        order = new Order();
        order.setId(1L);
        order.setUserId(1L);
        order.setStatus(OrderStatus.CREATED.name());
        order.setCreatedAt(LocalDateTime.now().minusMinutes(20));
        order.setUpdatedAt(LocalDateTime.now().minusMinutes(20));
    }

    // ==================== 超时订单自动取消 ====================

    @Test
    @DisplayName("1. CREATED 超时订单自动取消成功")
    void testAutoCancelCreatedTimeoutOrder() {
        when(orderMapper.selectList(any(LambdaQueryWrapper.class)))
                .thenReturn(List.of(order), List.of()); // CREATED scan has 1, ACCEPTED scan empty
        when(orderMapper.selectById(1L)).thenReturn(order);
        when(orderItemMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(List.of());
        when(orderMapper.updateById(any(Order.class))).thenReturn(1);

        timeoutTask.autoCancelTimeoutOrders();

        verify(orderMapper).updateById(argThat(o ->
                OrderStatus.CANCELLED.name().equals(o.getStatus())));
    }

    @Test
    @DisplayName("2. ACCEPTED 超时订单自动取消成功")
    void testAutoCancelAcceptedTimeoutOrder() {
        order.setStatus(OrderStatus.ACCEPTED.name());
        order.setUpdatedAt(LocalDateTime.now().minusMinutes(40));

        when(orderMapper.selectList(any(LambdaQueryWrapper.class)))
                .thenReturn(List.of(), List.of(order)) // CREATED empty, ACCEPTED has 1
                .thenReturn(List.of());   // default
        when(orderMapper.selectById(1L)).thenReturn(order);
        when(orderItemMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(List.of());
        when(orderMapper.updateById(any(Order.class))).thenReturn(1);

        timeoutTask.autoCancelTimeoutOrders();

        verify(orderMapper).updateById(argThat(o ->
                OrderStatus.CANCELLED.name().equals(o.getStatus())));
    }

    @Test
    @DisplayName("3. COOKING 状态订单不会被自动取消")
    void testCookingOrderNotCancelled() {
        order.setStatus(OrderStatus.COOKING.name());
        when(orderMapper.selectList(any(LambdaQueryWrapper.class)))
                .thenReturn(List.of()); // no CREATED/ACCEPTED timeout orders

        timeoutTask.autoCancelTimeoutOrders();

        verify(orderMapper, never()).updateById(any(Order.class));
    }

    @Test
    @DisplayName("4. COMPLETED 状态订单不会被自动取消")
    void testCompletedOrderNotCancelled() {
        order.setStatus(OrderStatus.COMPLETED.name());
        when(orderMapper.selectList(any(LambdaQueryWrapper.class)))
                .thenReturn(List.of());

        timeoutTask.autoCancelTimeoutOrders();

        verify(orderMapper, never()).updateById(any(Order.class));
    }

    @Test
    @DisplayName("5. 自动取消时调用 menu-service 恢复库存")
    void testAutoCancelRestoresStock() {
        OrderItem item = new OrderItem();
        item.setDishId(10L);
        item.setQuantity(2);

        when(orderMapper.selectList(any(LambdaQueryWrapper.class)))
                .thenReturn(List.of(order), List.of()); // CREATED scan has 1, ACCEPTED scan empty
        when(orderMapper.selectById(1L)).thenReturn(order);
        when(orderItemMapper.selectList(any(LambdaQueryWrapper.class)))
                .thenReturn(List.of(item));
        when(menuFeignClient.restoreStock(eq(10L), any(StockRequest.class)))
                .thenReturn(Result.success());
        when(orderMapper.updateById(any(Order.class))).thenReturn(1);

        timeoutTask.autoCancelTimeoutOrders();

        verify(menuFeignClient).restoreStock(eq(10L), any(StockRequest.class));
        verify(orderMapper).updateById(argThat(o ->
                OrderStatus.CANCELLED.name().equals(o.getStatus())));
    }

    @Test
    @DisplayName("6. 恢复库存失败时不更新订单状态")
    void testStockRestoreFailureSkipsCancel() {
        OrderItem item = new OrderItem();
        item.setDishId(10L);
        item.setQuantity(2);

        // CREATED 扫描找到 1 个, ACCEPTED 扫描找到 0 个
        when(orderMapper.selectList(any(LambdaQueryWrapper.class)))
                .thenReturn(List.of(order), List.of());
        when(orderMapper.selectById(1L)).thenReturn(order);
        when(orderItemMapper.selectList(any(LambdaQueryWrapper.class)))
                .thenReturn(List.of(item));
        when(menuFeignClient.restoreStock(eq(10L), any(StockRequest.class)))
                .thenReturn(Result.fail(500, "服务不可用"));

        timeoutTask.autoCancelTimeoutOrders();

        verify(menuFeignClient).restoreStock(eq(10L), any(StockRequest.class));
        verify(orderMapper, never()).updateById(any(Order.class));
    }

    // ==================== 并发状态校验 ====================

    @Test
    @DisplayName("7. 并发情况下状态已变更，跳过自动取消")
    void testConcurrentStatusChangeSkipped() {
        Order alreadyCooking = new Order();
        alreadyCooking.setId(1L);
        alreadyCooking.setStatus(OrderStatus.COOKING.name());

        when(orderMapper.selectList(any(LambdaQueryWrapper.class)))
                .thenReturn(List.of(order), List.of()); // CREATED scan finds 1, ACCEPTED scan empty
        when(orderMapper.selectById(1L)).thenReturn(alreadyCooking); // but now it's COOKING

        timeoutTask.autoCancelTimeoutOrders();

        verify(orderMapper, never()).updateById(any(Order.class));
    }
}
