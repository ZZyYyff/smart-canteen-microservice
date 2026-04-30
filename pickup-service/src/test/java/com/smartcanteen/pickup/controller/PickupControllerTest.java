package com.smartcanteen.pickup.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartcanteen.common.enums.PickupQueueStatus;
import com.smartcanteen.pickup.dto.CreateWindowDTO;
import com.smartcanteen.pickup.dto.VerifyRequest;
import com.smartcanteen.pickup.service.PickupService;
import com.smartcanteen.pickup.vo.PickupQueueVO;
import com.smartcanteen.pickup.vo.PickupWindowVO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PickupController.class)
class PickupControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @MockBean private PickupService pickupService;

    // ==================== 窗口管理 ====================

    @Test
    @DisplayName("1. 新增窗口成功")
    void testCreateWindow() throws Exception {
        CreateWindowDTO dto = new CreateWindowDTO();
        dto.setName("1号窗口");
        dto.setLocation("一楼A区");

        PickupWindowVO vo = new PickupWindowVO();
        vo.setId(1L);
        vo.setName("1号窗口");
        when(pickupService.createWindow(any())).thenReturn(vo);

        mockMvc.perform(post("/api/pickup/windows")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.name").value("1号窗口"));
    }

    @Test
    @DisplayName("2. 查询窗口列表")
    void testListWindows() throws Exception {
        PickupWindowVO vo = new PickupWindowVO();
        vo.setId(1L);
        vo.setName("1号窗口");
        when(pickupService.listWindows()).thenReturn(List.of(vo));

        mockMvc.perform(get("/api/pickup/windows"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(1));
    }

    // ==================== 取餐队列 ====================

    @Test
    @DisplayName("3. 查询窗口队列")
    void testGetWindowQueue() throws Exception {
        PickupQueueVO q = new PickupQueueVO();
        q.setId(1L);
        q.setPickupNo(123);
        q.setStatus(PickupQueueStatus.WAITING.name());
        when(pickupService.getWindowQueue(1L)).thenReturn(List.of(q));

        mockMvc.perform(get("/api/pickup/windows/1/queue"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].pickupNo").value(123));
    }

    @Test
    @DisplayName("4. 叫号成功")
    void testCallNext() throws Exception {
        PickupQueueVO q = new PickupQueueVO();
        q.setId(1L);
        q.setPickupNo(123);
        q.setStatus(PickupQueueStatus.CALLED.name());
        when(pickupService.callNext(1L)).thenReturn(q);

        mockMvc.perform(post("/api/pickup/windows/1/call-next"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("CALLED"));
    }

    // ==================== 核销 ====================

    @Test
    @DisplayName("5. 取餐核销成功")
    void testVerifyPickup() throws Exception {
        VerifyRequest req = new VerifyRequest();
        req.setPickupNo(123);
        req.setPickupCode("567890");

        PickupQueueVO q = new PickupQueueVO();
        q.setId(1L);
        q.setPickupNo(123);
        q.setStatus(PickupQueueStatus.FINISHED.name());
        when(pickupService.verifyPickup(any())).thenReturn(q);

        mockMvc.perform(post("/api/pickup/verify")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("FINISHED"));
    }

    @Test
    @DisplayName("6. 核销缺少取餐号返回 400")
    void testVerifyMissingPickupNo() throws Exception {
        VerifyRequest req = new VerifyRequest();
        req.setPickupCode("567890");

        mockMvc.perform(post("/api/pickup/verify")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }
}
