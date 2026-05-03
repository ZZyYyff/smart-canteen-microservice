package com.smartcanteen.menu.controller;

import com.smartcanteen.common.result.Result;
import com.smartcanteen.menu.dto.DailyMenuDTO;
import com.smartcanteen.menu.dto.DishDTO;
import com.smartcanteen.menu.dto.StockOperateDTO;
import com.smartcanteen.menu.service.DailyMenuService;
import com.smartcanteen.menu.service.DishService;
import com.smartcanteen.menu.vo.DailyMenuVO;
import com.smartcanteen.menu.vo.DishVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/**
 * 菜品与菜单控制器，统一前缀 /api/menus
 */
@RestController
@RequestMapping("/api/menus")
@RequiredArgsConstructor
public class MenuController {

    private final DishService dishService;
    private final DailyMenuService dailyMenuService;

    // ==================== 菜品管理 ====================

    /** 新增菜品 */
    @PostMapping("/dishes")
    public Result<DishVO> createDish(@Valid @RequestBody DishDTO dto) {
        return Result.success("新增成功", dishService.create(dto));
    }

    /** 查询菜品列表，支持按名称和状态筛选 */
    @GetMapping("/dishes")
    public Result<List<DishVO>> listDishes(@RequestParam(required = false) String name,
                                           @RequestParam(required = false) String status) {
        return Result.success(dishService.list(name, status));
    }

    /** 查询菜品详情 */
    @GetMapping("/dishes/{id}")
    public Result<DishVO> getDish(@PathVariable Long id) {
        return Result.success(dishService.getById(id));
    }

    /** 修改菜品 */
    @PutMapping("/dishes/{id}")
    public Result<DishVO> updateDish(@PathVariable Long id, @Valid @RequestBody DishDTO dto) {
        return Result.success("修改成功", dishService.update(id, dto));
    }

    /** 删除菜品 */
    @DeleteMapping("/dishes/{id}")
    public Result<Void> deleteDish(@PathVariable Long id) {
        dishService.delete(id);
        return Result.success();
    }

    /** 上架菜品 */
    @PutMapping("/dishes/{id}/on-sale")
    public Result<DishVO> onSale(@PathVariable Long id) {
        return Result.success("上架成功", dishService.onSale(id));
    }

    /** 下架菜品 */
    @PutMapping("/dishes/{id}/off-sale")
    public Result<DishVO> offSale(@PathVariable Long id) {
        return Result.success("下架成功", dishService.offSale(id));
    }

    // ==================== 库存操作（供 order-service 调用） ====================

    /** 扣减库存 */
    @PutMapping("/dishes/{id}/stock/deduct")
    public Result<Void> deductStock(@PathVariable Long id, @Valid @RequestBody StockOperateDTO dto) {
        dishService.deductStock(id, dto.getQuantity());
        return Result.success();
    }

    /** 恢复库存 */
    @PutMapping("/dishes/{id}/stock/restore")
    public Result<Void> restoreStock(@PathVariable Long id, @Valid @RequestBody StockOperateDTO dto) {
        dishService.restoreStock(id, dto.getQuantity());
        return Result.success();
    }

    // ==================== 每日菜单 ====================

    /** 创建每日菜单 */
    @PostMapping("/daily")
    public Result<DailyMenuVO> createDailyMenu(@Valid @RequestBody DailyMenuDTO dto) {
        return Result.success("创建成功", dailyMenuService.create(dto));
    }

    /** 获取今日菜单 */
    @GetMapping("/today")
    public Result<List<DailyMenuVO>> getTodayMenu() {
        return Result.success(dailyMenuService.getTodayMenu());
    }

    /** 查询每日菜单列表（支持按日期、餐次筛选） */
    @GetMapping("/daily")
    public Result<List<DailyMenuVO>> listDailyMenus(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate menuDate,
            @RequestParam(required = false) String mealPeriod) {
        return Result.success(dailyMenuService.list(menuDate, mealPeriod));
    }

    /** 编辑每日菜单 */
    @PutMapping("/daily/{id}")
    public Result<DailyMenuVO> updateDailyMenu(@PathVariable Long id,
                                                @Valid @RequestBody DailyMenuDTO dto) {
        return Result.success("修改成功", dailyMenuService.update(id, dto));
    }

    /** 删除每日菜单 */
    @DeleteMapping("/daily/{id}")
    public Result<Void> deleteDailyMenu(@PathVariable Long id) {
        dailyMenuService.delete(id);
        return Result.success();
    }
}
