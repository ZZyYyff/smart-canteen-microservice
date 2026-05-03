package com.smartcanteen.menu.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartcanteen.common.enums.DishStatus;
import com.smartcanteen.menu.dto.DailyMenuDTO;
import com.smartcanteen.menu.dto.DishDTO;
import com.smartcanteen.menu.dto.StockOperateDTO;
import com.smartcanteen.menu.service.DailyMenuService;
import com.smartcanteen.menu.service.DishService;
import com.smartcanteen.menu.vo.DailyMenuVO;
import com.smartcanteen.menu.vo.DishVO;
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
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(MenuController.class)
class MenuControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @MockBean private DishService dishService;
    @MockBean private DailyMenuService dailyMenuService;

    // ==================== 菜品管理 ====================

    @Test
    @DisplayName("1. 新增菜品成功")
    void testCreateDish() throws Exception {
        DishDTO dto = new DishDTO();
        dto.setName("红烧肉");
        dto.setPrice(BigDecimal.valueOf(25));
        dto.setStock(50);

        DishVO vo = DishVO.builder().id(1L).name("红烧肉").build();
        when(dishService.create(any())).thenReturn(vo);

        mockMvc.perform(post("/api/menus/dishes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.name").value("红烧肉"));
    }

    @Test
    @DisplayName("2. 查询菜品列表")
    void testListDishes() throws Exception {
        DishVO vo = DishVO.builder().id(1L).name("红烧肉")
                .status(DishStatus.ON_SALE.name()).build();
        when(dishService.list(null, null)).thenReturn(List.of(vo));

        mockMvc.perform(get("/api/menus/dishes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(1));
    }

    @Test
    @DisplayName("3. 上架菜品")
    void testOnSale() throws Exception {
        DishVO vo = DishVO.builder().id(1L).status(DishStatus.ON_SALE.name()).build();
        when(dishService.onSale(1L)).thenReturn(vo);

        mockMvc.perform(put("/api/menus/dishes/1/on-sale"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("ON_SALE"));
    }

    @Test
    @DisplayName("4. 下架菜品")
    void testOffSale() throws Exception {
        DishVO vo = DishVO.builder().id(1L).status(DishStatus.OFF_SALE.name()).build();
        when(dishService.offSale(1L)).thenReturn(vo);

        mockMvc.perform(put("/api/menus/dishes/1/off-sale"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("OFF_SALE"));
    }

    // ==================== 库存操作 ====================

    @Test
    @DisplayName("5. 扣减库存")
    void testDeductStock() throws Exception {
        StockOperateDTO dto = new StockOperateDTO();
        dto.setQuantity(5);

        mockMvc.perform(put("/api/menus/dishes/1/stock/deduct")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @DisplayName("6. 恢复库存")
    void testRestoreStock() throws Exception {
        StockOperateDTO dto = new StockOperateDTO();
        dto.setQuantity(5);

        mockMvc.perform(put("/api/menus/dishes/1/stock/restore")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    // ==================== 每日菜单 ====================

    private DailyMenuVO dailyMenuVo(Long id) {
        return DailyMenuVO.builder()
                .id(id).menuDate(java.time.LocalDate.now())
                .mealPeriod("LUNCH").mealPeriodDesc("午餐")
                .status("ACTIVE").dishCount(0).dishes(List.of()).build();
    }

    @Test
    @DisplayName("7. 查询每日菜单列表")
    void testListDailyMenus() throws Exception {
        when(dailyMenuService.list(null, null)).thenReturn(List.of(dailyMenuVo(1L)));

        mockMvc.perform(get("/api/menus/daily"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(1));
    }

    @Test
    @DisplayName("8. 按日期查询每日菜单")
    void testListDailyMenusByDate() throws Exception {
        when(dailyMenuService.list(java.time.LocalDate.now(), null))
                .thenReturn(List.of(dailyMenuVo(1L)));

        mockMvc.perform(get("/api/menus/daily")
                        .param("menuDate", java.time.LocalDate.now().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(1));
    }

    @Test
    @DisplayName("9. 删除每日菜单成功")
    void testDeleteDailyMenu() throws Exception {
        mockMvc.perform(delete("/api/menus/daily/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }
}
