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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderTimeoutTask {

    private final OrderMapper orderMapper;
    private final OrderItemMapper orderItemMapper;
    private final MenuFeignClient menuFeignClient;

    @Value("${order.timeout.created-minutes:15}")
    private int createdTimeoutMinutes;

    @Value("${order.timeout.accepted-minutes:30}")
    private int acceptedTimeoutMinutes;

    /** 每 60 秒扫描一次超时订单 */
    @Scheduled(fixedDelayString = "${order.timeout.scan-interval-ms:60000}")
    public void autoCancelTimeoutOrders() {
        log.debug("开始扫描超时订单...");
        cancelCreatedTimeoutOrders();
        cancelAcceptedTimeoutOrders();
    }

    /** 取消超时未接单的 CREATED 订单 */
    private void cancelCreatedTimeoutOrders() {
        LocalDateTime deadline = LocalDateTime.now().minusMinutes(createdTimeoutMinutes);
        List<Order> timeoutOrders = orderMapper.selectList(
                new LambdaQueryWrapper<Order>()
                        .eq(Order::getStatus, OrderStatus.CREATED.name())
                        .lt(Order::getCreatedAt, deadline));

        for (Order order : timeoutOrders) {
            processTimeoutOrder(order, "CREATED 超时未接单（超过 " + createdTimeoutMinutes + " 分钟）");
        }
    }

    /** 取消接单后超时未制作的 ACCEPTED 订单 */
    private void cancelAcceptedTimeoutOrders() {
        LocalDateTime deadline = LocalDateTime.now().minusMinutes(acceptedTimeoutMinutes);
        List<Order> timeoutOrders = orderMapper.selectList(
                new LambdaQueryWrapper<Order>()
                        .eq(Order::getStatus, OrderStatus.ACCEPTED.name())
                        .lt(Order::getUpdatedAt, deadline));

        for (Order order : timeoutOrders) {
            processTimeoutOrder(order, "ACCEPTED 超时未制作（超过 " + acceptedTimeoutMinutes + " 分钟）");
        }
    }

    /** 处理单个超时订单，包含库存恢复 */
    @Transactional
    public void processTimeoutOrder(Order order, String reason) {
        // 再次查询并加锁校验状态，避免并发问题
        Order current = orderMapper.selectById(order.getId());
        if (current == null) {
            return;
        }
        String status = current.getStatus();
        if (!OrderStatus.CREATED.name().equals(status)
                && !OrderStatus.ACCEPTED.name().equals(status)) {
            log.info("订单 {} 状态已变更为 {}, 跳过自动取消", order.getId(), status);
            return;
        }

        // 恢复库存
        List<OrderItem> items = orderItemMapper.selectList(
                new LambdaQueryWrapper<OrderItem>().eq(OrderItem::getOrderId, order.getId()));
        boolean stockRestored = true;
        for (OrderItem item : items) {
            try {
                Result<Void> result = menuFeignClient.restoreStock(
                        item.getDishId(), new StockRequest(item.getQuantity()));
                if (result.getCode() != 200) {
                    log.error("自动取消恢复库存失败: orderId={}, dishId={}, msg={}",
                            order.getId(), item.getDishId(), result.getMessage());
                    stockRestored = false;
                } else {
                    log.info("自动取消恢复库存成功: orderId={}, dishId={}, quantity={}",
                            order.getId(), item.getDishId(), item.getQuantity());
                }
            } catch (Exception e) {
                log.error("自动取消恢复库存异常: orderId={}, dishId={}", order.getId(), item.getDishId(), e);
                stockRestored = false;
            }
        }

        if (!stockRestored) {
            log.error("订单 {} 库存恢复部分失败，跳过自动取消状态更新，保留原状态 {}", order.getId(), status);
            return;
        }

        // 更新订单状态
        current.setStatus(OrderStatus.CANCELLED.name());
        current.setUpdatedAt(LocalDateTime.now());
        orderMapper.updateById(current);

        log.info("订单超时自动取消: orderId={}, userId={}, 原状态={}, 原因={}",
                order.getId(), current.getUserId(), status, reason);
    }
}
