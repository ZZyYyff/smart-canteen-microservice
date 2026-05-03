package com.smartcanteen.menu.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.smartcanteen.common.enums.DishStatus;
import com.smartcanteen.common.exception.BusinessException;
import com.smartcanteen.menu.dto.DailyMenuDTO;
import com.smartcanteen.menu.dto.DishDTO;
import com.smartcanteen.menu.entity.DailyMenu;
import com.smartcanteen.menu.entity.DailyMenuItem;
import com.smartcanteen.menu.entity.Dish;
import com.smartcanteen.menu.mapper.DailyMenuItemMapper;
import com.smartcanteen.menu.mapper.DailyMenuMapper;
import com.smartcanteen.menu.mapper.DishMapper;
import com.smartcanteen.menu.service.impl.DailyMenuServiceImpl;
import com.smartcanteen.menu.service.impl.DishServiceImpl;
import com.smartcanteen.menu.vo.DailyMenuVO;
import com.smartcanteen.menu.vo.DishVO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * 菜品与菜单服务单元测试
 */
@ExtendWith(MockitoExtension.class)
class MenuServiceTest {

    @Mock private DishMapper dishMapper;
    @Mock private DailyMenuMapper dailyMenuMapper;
    @Mock private DailyMenuItemMapper dailyMenuItemMapper;

    @InjectMocks private DishServiceImpl dishService;
    @InjectMocks private DailyMenuServiceImpl dailyMenuService;

    // ==================== 菜品 CRUD 测试 ====================

    @Test
    @DisplayName("1. 新增菜品成功")
    void testCreateDish() {
        DishDTO dto = new DishDTO();
        dto.setName("红烧肉");
        dto.setPrice(BigDecimal.valueOf(25.00));
        dto.setDescription("香甜可口");
        dto.setStock(50);
        dto.setWarningStock(10);

        when(dishMapper.insert(any(Dish.class))).thenReturn(1);

        DishVO result = dishService.create(dto);
        assertNotNull(result);
        assertEquals("红烧肉", result.getName());
        assertEquals(0, BigDecimal.valueOf(25.00).compareTo(result.getPrice()));
        assertEquals(50, result.getStock());
        assertEquals(DishStatus.OFF_SALE.name(), result.getStatus());
        assertNotNull(result.getStatusDesc());
    }

    @Test
    @DisplayName("2. 上架菜品成功")
    void testOnSale() {
        Dish dish = new Dish();
        dish.setId(1L);
        dish.setName("红烧肉");
        dish.setPrice(BigDecimal.valueOf(25.00));
        dish.setStatus(DishStatus.OFF_SALE.name());
        dish.setStock(50);
        dish.setWarningStock(10);

        when(dishMapper.selectById(1L)).thenReturn(dish);
        when(dishMapper.updateById(any(Dish.class))).thenReturn(1);

        DishVO result = dishService.onSale(1L);
        assertEquals(DishStatus.ON_SALE.name(), result.getStatus());
        assertEquals("上架中", result.getStatusDesc());
    }

    @Test
    @DisplayName("3. 下架菜品")
    void testOffSale() {
        Dish dish = new Dish();
        dish.setId(1L);
        dish.setStatus(DishStatus.ON_SALE.name());
        dish.setStock(50);
        dish.setWarningStock(10);

        when(dishMapper.selectById(1L)).thenReturn(dish);
        when(dishMapper.updateById(any(Dish.class))).thenReturn(1);

        DishVO result = dishService.offSale(1L);
        assertEquals(DishStatus.OFF_SALE.name(), result.getStatus());
    }

    @Test
    @DisplayName("4. 查询菜品列表（按状态筛选）")
    void testListDishes() {
        Dish d1 = new Dish();
        d1.setId(1L); d1.setName("红烧肉"); d1.setPrice(BigDecimal.valueOf(25));
        d1.setStock(50); d1.setWarningStock(10); d1.setStatus("ON_SALE");

        when(dishMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(List.of(d1));

        List<DishVO> result = dishService.list(null, "ON_SALE");
        assertEquals(1, result.size());
        assertEquals("红烧肉", result.get(0).getName());
        assertFalse(result.get(0).getLowStock());
    }

    @Test
    @DisplayName("5. 低库存预警标识正确")
    void testLowStockWarning() {
        Dish dish = new Dish();
        dish.setId(1L);
        dish.setName("限量菜");
        dish.setPrice(BigDecimal.valueOf(50));
        dish.setStock(3);
        dish.setWarningStock(5);
        dish.setStatus("ON_SALE");

        when(dishMapper.selectById(1L)).thenReturn(dish);

        DishVO result = dishService.getById(1L);
        assertTrue(result.getLowStock());
    }

    // ==================== 库存操作测试 ====================

    @Test
    @DisplayName("6. 库存充足扣减成功")
    void testDeductStockSuccess() {
        when(dishMapper.deductStock(1L, 5)).thenReturn(1);

        assertDoesNotThrow(() -> dishService.deductStock(1L, 5));
        verify(dishMapper).deductStock(1L, 5);
    }

    @Test
    @DisplayName("7. 库存不足扣减失败")
    void testDeductStockInsufficient() {
        Dish dish = new Dish();
        dish.setId(1L);
        dish.setName("红烧肉");
        dish.setStock(3);

        when(dishMapper.deductStock(1L, 5)).thenReturn(0);
        when(dishMapper.selectById(1L)).thenReturn(dish);

        BusinessException ex = assertThrows(BusinessException.class,
                () -> dishService.deductStock(1L, 5));
        assertThat(ex.getMessage()).contains("库存不足");
    }

    @Test
    @DisplayName("8. 恢复库存成功")
    void testRestoreStock() {
        when(dishMapper.restoreStock(1L, 5)).thenReturn(1);

        assertDoesNotThrow(() -> dishService.restoreStock(1L, 5));
        verify(dishMapper).restoreStock(1L, 5);
    }

    // ==================== 每日菜单测试 ====================

    @Test
    @DisplayName("9. 查询今日菜单成功")
    void testGetTodayMenu() {
        DailyMenu menu = new DailyMenu();
        menu.setId(1L);
        menu.setMenuDate(LocalDate.now());
        menu.setMealPeriod("LUNCH");
        menu.setStartTime(LocalTime.of(11, 0));
        menu.setEndTime(LocalTime.of(13, 0));
        menu.setStatus("ACTIVE");

        DailyMenuItem item = new DailyMenuItem();
        item.setId(1L);
        item.setMenuId(1L);
        item.setDishId(10L);

        Dish dish = new Dish();
        dish.setId(10L);
        dish.setName("红烧肉");
        dish.setPrice(BigDecimal.valueOf(25.00));
        dish.setStock(50);
        dish.setWarningStock(10);
        dish.setStatus("ON_SALE");

        when(dailyMenuMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(List.of(menu));
        when(dailyMenuItemMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(List.of(item));
        when(dishMapper.selectBatchIds(List.of(10L))).thenReturn(List.of(dish));

        List<DailyMenuVO> result = dailyMenuService.getTodayMenu();
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("LUNCH", result.get(0).getMealPeriod());
        assertEquals("午餐", result.get(0).getMealPeriodDesc());
        assertEquals(1, result.get(0).getDishes().size());
        assertEquals("红烧肉", result.get(0).getDishes().get(0).getName());
    }

    @Test
    @DisplayName("10. 今日无菜单返回空列表")
    void testGetTodayMenuEmpty() {
        when(dailyMenuMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(List.of());

        List<DailyMenuVO> result = dailyMenuService.getTodayMenu();
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("11. 创建每日菜单")
    void testCreateDailyMenu() {
        DailyMenuDTO dto = new DailyMenuDTO();
        dto.setMenuDate(LocalDate.now());
        dto.setMealPeriod("LUNCH");
        dto.setStartTime(LocalTime.of(11, 0));
        dto.setEndTime(LocalTime.of(13, 0));
        dto.setDishIds(List.of(10L, 20L));

        Dish d1 = new Dish(); d1.setId(10L); d1.setName("菜A");
        d1.setPrice(BigDecimal.TEN); d1.setStock(20); d1.setWarningStock(0);
        d1.setStatus("ON_SALE");
        Dish d2 = new Dish(); d2.setId(20L); d2.setName("菜B");
        d2.setPrice(BigDecimal.valueOf(15)); d2.setStock(30); d2.setWarningStock(0);
        d2.setStatus("ON_SALE");

        when(dishMapper.selectBatchIds(dto.getDishIds())).thenReturn(List.of(d1, d2));
        when(dailyMenuMapper.insert(any(DailyMenu.class))).thenReturn(1);
        when(dailyMenuItemMapper.insert(any(DailyMenuItem.class))).thenReturn(1);

        DailyMenuVO result = dailyMenuService.create(dto);
        assertNotNull(result);
        assertEquals("LUNCH", result.getMealPeriod());
        assertEquals("ACTIVE", result.getStatus());
        assertEquals(2, result.getDishes().size());

        verify(dailyMenuMapper).insert(any(DailyMenu.class));
        verify(dailyMenuItemMapper, times(2)).insert(any(DailyMenuItem.class));
    }

    @Test
    @DisplayName("12. 菜品不存在时查询详情报错")
    void testGetDishNotFound() {
        when(dishMapper.selectById(999L)).thenReturn(null);

        BusinessException ex = assertThrows(BusinessException.class,
                () -> dishService.getById(999L));
        assertEquals("菜品不存在", ex.getMessage());
    }

    // ==================== 每日菜单 CRUD 测试 ====================

    private DailyMenu createTestMenu(Long id, LocalDate date, String period) {
        DailyMenu menu = new DailyMenu();
        menu.setId(id);
        menu.setMenuDate(date);
        menu.setMealPeriod(period);
        menu.setStartTime(LocalTime.of(11, 0));
        menu.setEndTime(LocalTime.of(13, 0));
        menu.setStatus("ACTIVE");
        return menu;
    }

    private Dish createTestDish(Long id, String name) {
        Dish d = new Dish();
        d.setId(id); d.setName(name); d.setPrice(BigDecimal.TEN);
        d.setStock(20); d.setWarningStock(0); d.setStatus("ON_SALE");
        return d;
    }

    @Test
    @DisplayName("13. 查询每日菜单列表成功")
    void testListDailyMenus() {
        DailyMenu menu = createTestMenu(1L, LocalDate.now(), "LUNCH");
        when(dailyMenuMapper.selectList(any(LambdaQueryWrapper.class)))
                .thenReturn(List.of(menu));
        when(dailyMenuItemMapper.selectList(any(LambdaQueryWrapper.class)))
                .thenReturn(List.of());
        when(dishMapper.selectBatchIds(anyList())).thenReturn(List.of());

        List<DailyMenuVO> results = dailyMenuService.list(null, null);
        assertEquals(1, results.size());
        assertEquals("LUNCH", results.get(0).getMealPeriod());
    }

    @Test
    @DisplayName("14. 按日期查询每日菜单成功")
    void testListDailyMenusByDate() {
        DailyMenu menu = createTestMenu(1L, LocalDate.now(), "BREAKFAST");
        when(dailyMenuMapper.selectList(any(LambdaQueryWrapper.class)))
                .thenReturn(List.of(menu));
        when(dailyMenuItemMapper.selectList(any(LambdaQueryWrapper.class)))
                .thenReturn(List.of());
        when(dishMapper.selectBatchIds(anyList())).thenReturn(List.of());

        List<DailyMenuVO> results = dailyMenuService.list(LocalDate.now(), null);
        assertEquals(1, results.size());
    }

    @Test
    @DisplayName("15. 按餐段查询每日菜单成功")
    void testListDailyMenusByPeriod() {
        DailyMenu menu = createTestMenu(1L, LocalDate.now(), "DINNER");
        when(dailyMenuMapper.selectList(any(LambdaQueryWrapper.class)))
                .thenReturn(List.of(menu));
        when(dailyMenuItemMapper.selectList(any(LambdaQueryWrapper.class)))
                .thenReturn(List.of());
        when(dishMapper.selectBatchIds(anyList())).thenReturn(List.of());

        List<DailyMenuVO> results = dailyMenuService.list(null, "DINNER");
        assertEquals(1, results.size());
    }

    @Test
    @DisplayName("16. 编辑每日菜单成功")
    void testUpdateDailyMenu() {
        DailyMenu menu = createTestMenu(1L, LocalDate.now(), "LUNCH");
        Dish d1 = createTestDish(10L, "菜A");
        Dish d2 = createTestDish(20L, "菜B");

        when(dailyMenuMapper.selectById(1L)).thenReturn(menu);
        when(dishMapper.selectBatchIds(List.of(10L, 20L))).thenReturn(List.of(d1, d2));
        when(dailyMenuMapper.updateById(any(DailyMenu.class))).thenReturn(1);
        when(dailyMenuItemMapper.delete(any(LambdaQueryWrapper.class))).thenReturn(1);
        when(dailyMenuItemMapper.insert(any(DailyMenuItem.class))).thenReturn(1);

        DailyMenuDTO dto = new DailyMenuDTO();
        dto.setMenuDate(LocalDate.now());
        dto.setMealPeriod("LUNCH");
        dto.setStartTime(LocalTime.of(11, 0));
        dto.setEndTime(LocalTime.of(13, 0));
        dto.setDishIds(List.of(10L, 20L));

        DailyMenuVO result = dailyMenuService.update(1L, dto);
        assertNotNull(result);
        assertEquals("LUNCH", result.getMealPeriod());
        assertEquals(2, result.getDishCount());
        assertEquals(2, result.getDishes().size());

        verify(dailyMenuMapper).updateById(any(DailyMenu.class));
        verify(dailyMenuItemMapper).delete(any(LambdaQueryWrapper.class));
        verify(dailyMenuItemMapper, times(2)).insert(any(DailyMenuItem.class));
    }

    @Test
    @DisplayName("17. 编辑不存在的每日菜单失败")
    void testUpdateDailyMenuNotFound() {
        when(dailyMenuMapper.selectById(999L)).thenReturn(null);

        DailyMenuDTO dto = new DailyMenuDTO();
        dto.setMenuDate(LocalDate.now());
        dto.setMealPeriod("LUNCH");
        dto.setStartTime(LocalTime.of(11, 0));
        dto.setEndTime(LocalTime.of(13, 0));
        dto.setDishIds(List.of(10L));

        BusinessException ex = assertThrows(BusinessException.class,
                () -> dailyMenuService.update(999L, dto));
        assertEquals("每日菜单不存在", ex.getMessage());
    }

    @Test
    @DisplayName("18. 删除每日菜单成功")
    void testDeleteDailyMenu() {
        DailyMenu menu = createTestMenu(1L, LocalDate.now(), "LUNCH");
        when(dailyMenuMapper.selectById(1L)).thenReturn(menu);
        when(dailyMenuItemMapper.delete(any(LambdaQueryWrapper.class))).thenReturn(1);
        when(dailyMenuMapper.deleteById(1L)).thenReturn(1);

        assertDoesNotThrow(() -> dailyMenuService.delete(1L));
        verify(dailyMenuItemMapper).delete(any(LambdaQueryWrapper.class));
        verify(dailyMenuMapper).deleteById(1L);
    }

    @Test
    @DisplayName("19. 删除不存在的每日菜单失败")
    void testDeleteDailyMenuNotFound() {
        when(dailyMenuMapper.selectById(999L)).thenReturn(null);

        BusinessException ex = assertThrows(BusinessException.class,
                () -> dailyMenuService.delete(999L));
        assertEquals("每日菜单不存在", ex.getMessage());
    }
}
