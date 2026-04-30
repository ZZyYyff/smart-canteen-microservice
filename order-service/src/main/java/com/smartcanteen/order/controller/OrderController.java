package com.smartcanteen.order.controller;

import com.smartcanteen.common.result.Result;
import com.smartcanteen.order.dto.CreateOrderDTO;
import com.smartcanteen.order.service.OrderService;
import com.smartcanteen.order.vo.OrderVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 订单控制器
 */
@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    /** 创建订单 */
    @PostMapping
    public Result<OrderVO> createOrder(@RequestHeader("X-User-Id") Long userId,
                                        @Valid @RequestBody CreateOrderDTO dto) {
        return Result.success("下单成功", orderService.createOrder(userId, dto));
    }

    /** 查询订单详情 */
    @GetMapping("/{id}")
    public Result<OrderVO> getOrder(@PathVariable Long id) {
        return Result.success(orderService.getOrderById(id));
    }

    /** 查询当前用户订单 */
    @GetMapping("/my")
    public Result<List<OrderVO>> getMyOrders(@RequestHeader("X-User-Id") Long userId) {
        return Result.success(orderService.getMyOrders(userId));
    }

    /** 商家查询待处理订单 */
    @GetMapping("/merchant/pending")
    public Result<List<OrderVO>> getMerchantPendingOrders() {
        return Result.success(orderService.getMerchantPendingOrders());
    }

    /** 取消订单 — 仅订单所属用户可取消 */
    @PutMapping("/{id}/cancel")
    public Result<OrderVO> cancelOrder(@PathVariable Long id,
                                        @RequestHeader("X-User-Id") Long userId) {
        return Result.success("订单已取消", orderService.cancelOrder(id, userId));
    }

    /** 商家接单 */
    @PutMapping("/{id}/accept")
    public Result<OrderVO> acceptOrder(@PathVariable Long id) {
        return Result.success("已接单", orderService.acceptOrder(id));
    }

    /** 开始制作 */
    @PutMapping("/{id}/cooking")
    public Result<OrderVO> cookingOrder(@PathVariable Long id) {
        return Result.success("制作中", orderService.cookingOrder(id));
    }

    /** 制作完成 */
    @PutMapping("/{id}/ready")
    public Result<OrderVO> readyOrder(@PathVariable Long id) {
        return Result.success("备餐完成，等待取餐", orderService.readyOrder(id));
    }

    /** 取餐完成 */
    @PutMapping("/{id}/complete")
    public Result<OrderVO> completeOrder(@PathVariable Long id) {
        return Result.success("取餐完成", orderService.completeOrder(id));
    }
}
