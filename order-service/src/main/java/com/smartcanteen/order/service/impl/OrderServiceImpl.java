package com.smartcanteen.order.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.smartcanteen.common.enums.ErrorCode;
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
import com.smartcanteen.order.service.OrderService;
import com.smartcanteen.order.vo.OrderVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 订单服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderMapper orderMapper;
    private final OrderItemMapper orderItemMapper;
    private final MenuFeignClient menuFeignClient;
    private final PickupFeignClient pickupFeignClient;

    private static final SecureRandom RANDOM = new SecureRandom();

    @Override
    @Transactional
    public OrderVO createOrder(Long userId, CreateOrderDTO dto) {
        // 记录已扣减的库存，用于失败回滚
        List<OrderItemDTO> deductedItems = new ArrayList<>();

        try {
            // 逐个扣减库存
            for (OrderItemDTO item : dto.getItems()) {
                Result<Void> result = menuFeignClient.deductStock(
                        item.getDishId(), new StockRequest(item.getQuantity()));
                if (result.getCode() != 200) {
                    throw new BusinessException(result.getCode(), result.getMessage());
                }
                deductedItems.add(item);
            }

            // 计算总金额
            BigDecimal total = dto.getItems().stream()
                    .map(item -> item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            // 创建订单
            Order order = new Order();
            order.setUserId(userId);
            order.setWindowId(dto.getWindowId());
            order.setTotalAmount(total);
            order.setStatus(OrderStatus.CREATED.name());
            order.setPickupNo(100 + RANDOM.nextInt(900));
            order.setPickupCode(String.format("%06d", RANDOM.nextInt(1000000)));
            order.setCreatedAt(LocalDateTime.now());
            order.setUpdatedAt(LocalDateTime.now());
            orderMapper.insert(order);

            // 创建订单明细
            List<OrderItem> orderItems = new ArrayList<>();
            for (OrderItemDTO item : dto.getItems()) {
                OrderItem oi = new OrderItem();
                oi.setOrderId(order.getId());
                oi.setDishId(item.getDishId());
                oi.setDishName(item.getDishName());
                oi.setPrice(item.getPrice());
                oi.setQuantity(item.getQuantity());
                orderItemMapper.insert(oi);
                orderItems.add(oi);
            }

            log.info("订单创建成功: orderId={}, userId={}, total={}", order.getId(), userId, total);
            return OrderVO.fromEntity(order, orderItems);

        } catch (BusinessException e) {
            // 业务异常（如库存不足），回滚已扣减的库存
            rollbackStock(deductedItems);
            throw e;
        } catch (Exception e) {
            // 其他异常，也回滚库存
            rollbackStock(deductedItems);
            log.error("下单失败", e);
            throw new BusinessException("下单失败，请稍后重试");
        }
    }

    /** 回滚已扣减的库存 */
    private void rollbackStock(List<OrderItemDTO> deductedItems) {
        for (OrderItemDTO item : deductedItems) {
            try {
                menuFeignClient.restoreStock(item.getDishId(), new StockRequest(item.getQuantity()));
                log.info("库存回滚成功: dishId={}, quantity={}", item.getDishId(), item.getQuantity());
            } catch (Exception ex) {
                log.error("库存回滚失败: dishId={}, quantity={}", item.getDishId(), item.getQuantity(), ex);
            }
        }
    }

    @Override
    public OrderVO getOrderById(Long orderId) {
        Order order = orderMapper.selectById(orderId);
        if (order == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "订单不存在");
        }
        List<OrderItem> items = getOrderItems(orderId);
        return OrderVO.fromEntity(order, items);
    }

    @Override
    public List<OrderVO> getMyOrders(Long userId) {
        List<Order> orders = orderMapper.selectList(
                new LambdaQueryWrapper<Order>()
                        .eq(Order::getUserId, userId)
                        .orderByDesc(Order::getCreatedAt));
        return orders.stream().map(order -> {
            List<OrderItem> items = getOrderItems(order.getId());
            return OrderVO.fromEntity(order, items);
        }).toList();
    }

    @Override
    public List<OrderVO> getMerchantPendingOrders() {
        List<Order> orders = orderMapper.selectList(
                new LambdaQueryWrapper<Order>()
                        .in(Order::getStatus, List.of(OrderStatus.CREATED.name(),
                                OrderStatus.ACCEPTED.name(), OrderStatus.COOKING.name(),
                                OrderStatus.WAIT_PICKUP.name()))
                        .orderByAsc(Order::getCreatedAt));
        return orders.stream().map(order -> {
            List<OrderItem> items = getOrderItems(order.getId());
            return OrderVO.fromEntity(order, items);
        }).toList();
    }

    @Override
    @Transactional
    public OrderVO cancelOrder(Long orderId, Long userId) {
        Order order = orderMapper.selectById(orderId);
        if (order == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "订单不存在");
        }

        // 校验订单归属
        if (!order.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "无权操作他人订单");
        }

        // 只有 CREATED 和 ACCEPTED 状态可取消
        String currentStatus = order.getStatus();
        if (!OrderStatus.CREATED.name().equals(currentStatus)
                && !OrderStatus.ACCEPTED.name().equals(currentStatus)) {
            throw new BusinessException(ErrorCode.ORDER_STATUS_ERROR,
                    "当前状态【" + getStatusDesc(currentStatus) + "】不允许取消");
        }

        order.setStatus(OrderStatus.CANCELLED.name());
        order.setUpdatedAt(LocalDateTime.now());
        orderMapper.updateById(order);

        // 恢复库存
        List<OrderItem> items = getOrderItems(orderId);
        for (OrderItem item : items) {
            try {
                menuFeignClient.restoreStock(item.getDishId(), new StockRequest(item.getQuantity()));
                log.info("取消订单恢复库存: dishId={}, quantity={}", item.getDishId(), item.getQuantity());
            } catch (Exception e) {
                log.error("取消订单恢复库存失败: dishId={}", item.getDishId(), e);
            }
        }

        log.info("订单已取消: orderId={}", orderId);
        return OrderVO.fromEntity(order, items);
    }

    @Override
    @Transactional
    public OrderVO acceptOrder(Long orderId) {
        return transitionStatus(orderId, OrderStatus.CREATED, OrderStatus.ACCEPTED);
    }

    @Override
    @Transactional
    public OrderVO cookingOrder(Long orderId) {
        return transitionStatus(orderId, OrderStatus.ACCEPTED, OrderStatus.COOKING);
    }

    @Override
    @Transactional
    public OrderVO readyOrder(Long orderId) {
        OrderVO vo = transitionStatus(orderId, OrderStatus.COOKING, OrderStatus.WAIT_PICKUP);

        // 通知 pickup-service 加入取餐队列
        Order order = orderMapper.selectById(orderId);
        try {
            PickupQueueRequest req = new PickupQueueRequest();
            req.setOrderId(order.getId());
            req.setUserId(order.getUserId());
            req.setWindowId(order.getWindowId());
            req.setPickupNo(order.getPickupNo());
            req.setPickupCode(order.getPickupCode());
            Result<Void> result = pickupFeignClient.addToQueue(req);
            if (result.getCode() != 200) {
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, "取餐队列同步失败: " + result.getMessage());
            }
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("调用取餐服务失败: orderId={}", orderId, e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "取餐队列同步失败，请重试");
        }

        return vo;
    }

    @Override
    @Transactional
    public OrderVO completeOrder(Long orderId) {
        return transitionStatus(orderId, OrderStatus.WAIT_PICKUP, OrderStatus.COMPLETED);
    }

    /**
     * 带校验的状态流转：确保当前状态等于 expectedCurrent，再更新为 target
     */
    private OrderVO transitionStatus(Long orderId, OrderStatus expectedCurrent, OrderStatus target) {
        Order order = orderMapper.selectById(orderId);
        if (order == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "订单不存在");
        }

        if (!expectedCurrent.name().equals(order.getStatus())) {
            throw new BusinessException(ErrorCode.ORDER_STATUS_ERROR,
                    "当前状态【" + getStatusDesc(order.getStatus()) + "】不允许此操作，"
                            + "期望状态【" + expectedCurrent.getDescription() + "】");
        }

        order.setStatus(target.name());
        order.setUpdatedAt(LocalDateTime.now());
        orderMapper.updateById(order);

        List<OrderItem> items = getOrderItems(orderId);
        log.info("订单状态流转: orderId={}, {} → {}", orderId, expectedCurrent, target);
        return OrderVO.fromEntity(order, items);
    }

    /** 获取订单的明细列表 */
    private List<OrderItem> getOrderItems(Long orderId) {
        return orderItemMapper.selectList(
                new LambdaQueryWrapper<OrderItem>().eq(OrderItem::getOrderId, orderId));
    }

    private String getStatusDesc(String status) {
        try {
            return OrderStatus.valueOf(status).getDescription();
        } catch (IllegalArgumentException e) {
            return status;
        }
    }
}
