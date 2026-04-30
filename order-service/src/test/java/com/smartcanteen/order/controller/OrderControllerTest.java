package com.smartcanteen.order.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartcanteen.common.enums.OrderStatus;
import com.smartcanteen.order.dto.CreateOrderDTO;
import com.smartcanteen.order.dto.OrderItemDTO;
import com.smartcanteen.order.service.OrderService;
import com.smartcanteen.order.vo.OrderItemVO;
import com.smartcanteen.order.vo.OrderVO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(OrderController.class)
class OrderControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @MockBean private OrderService orderService;

    private OrderVO mockOrder() {
        OrderItemVO item = OrderItemVO.builder().dishName("红烧肉").build();
        return OrderVO.builder()
                .id(100L).userId(1L).totalAmount(BigDecimal.valueOf(50))
                .status(OrderStatus.CREATED.name()).items(List.of(item))
                .build();
    }

    @Test
    @DisplayName("1. 创建订单成功")
    void testCreateOrder() throws Exception {
        OrderItemDTO item = new OrderItemDTO();
        item.setDishId(10L);
        item.setDishName("红烧肉");
        item.setPrice(BigDecimal.valueOf(25));
        item.setQuantity(2);

        CreateOrderDTO dto = new CreateOrderDTO();
        dto.setWindowId(1L);
        dto.setItems(List.of(item));

        when(orderService.createOrder(eq(1L), any())).thenReturn(mockOrder());

        mockMvc.perform(post("/api/orders")
                        .header("X-User-Id", "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.status").value("CREATED"));
    }

    @Test
    @DisplayName("2. 创建订单缺少 items 返回 400")
    void testCreateOrderMissingItems() throws Exception {
        CreateOrderDTO dto = new CreateOrderDTO();
        dto.setWindowId(1L);

        mockMvc.perform(post("/api/orders")
                        .header("X-User-Id", "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("3. 查询订单详情")
    void testGetOrder() throws Exception {
        when(orderService.getOrderById(100L)).thenReturn(mockOrder());

        mockMvc.perform(get("/api/orders/100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(100))
                .andExpect(jsonPath("$.data.status").value("CREATED"));
    }

    @Test
    @DisplayName("4. 查询我的订单")
    void testGetMyOrders() throws Exception {
        when(orderService.getMyOrders(1L)).thenReturn(List.of(mockOrder()));

        mockMvc.perform(get("/api/orders/my")
                        .header("X-User-Id", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(1));
    }

    @Test
    @DisplayName("5. 取消订单")
    void testCancelOrder() throws Exception {
        OrderVO vo = OrderVO.builder()
                .id(100L).status(OrderStatus.CANCELLED.name()).build();
        when(orderService.cancelOrder(eq(100L), eq(1L))).thenReturn(vo);

        mockMvc.perform(put("/api/orders/100/cancel")
                        .header("X-User-Id", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("CANCELLED"));
    }

    @Test
    @DisplayName("6. 商家接单")
    void testAcceptOrder() throws Exception {
        OrderVO vo = OrderVO.builder()
                .id(100L).status(OrderStatus.ACCEPTED.name()).build();
        when(orderService.acceptOrder(100L)).thenReturn(vo);

        mockMvc.perform(put("/api/orders/100/accept"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("ACCEPTED"));
    }
}
