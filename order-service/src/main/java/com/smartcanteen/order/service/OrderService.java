package com.smartcanteen.order.service;

import com.smartcanteen.order.dto.CreateOrderDTO;
import com.smartcanteen.order.vo.OrderVO;

import java.util.List;

/**
 * 订单服务接口
 */
public interface OrderService {

    /** 创建订单 */
    OrderVO createOrder(Long userId, CreateOrderDTO dto);

    /** 查询订单详情 */
    OrderVO getOrderById(Long orderId);

    /** 查询当前用户的订单列表 */
    List<OrderVO> getMyOrders(Long userId);

    /** 商家查询待处理订单（CREATED 或 ACCEPTED 状态） */
    List<OrderVO> getMerchantPendingOrders();

    /** 取消订单 — userId 用于校验订单归属 */
    OrderVO cancelOrder(Long orderId, Long userId);

    /** 商家接单：CREATED → ACCEPTED */
    OrderVO acceptOrder(Long orderId);

    /** 开始制作：ACCEPTED → COOKING */
    OrderVO cookingOrder(Long orderId);

    /** 制作完成：COOKING → WAIT_PICKUP，同时调用 pickup-service 加入取餐队列 */
    OrderVO readyOrder(Long orderId);

    /** 取餐完成：WAIT_PICKUP → COMPLETED */
    OrderVO completeOrder(Long orderId);
}
