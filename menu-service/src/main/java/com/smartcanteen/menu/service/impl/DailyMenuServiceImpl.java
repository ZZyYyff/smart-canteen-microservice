package com.smartcanteen.menu.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.smartcanteen.common.enums.ErrorCode;
import com.smartcanteen.common.exception.BusinessException;
import com.smartcanteen.menu.dto.DailyMenuDTO;
import com.smartcanteen.menu.entity.DailyMenu;
import com.smartcanteen.menu.entity.DailyMenuItem;
import com.smartcanteen.menu.entity.Dish;
import com.smartcanteen.menu.enums.MealPeriod;
import com.smartcanteen.menu.mapper.DailyMenuItemMapper;
import com.smartcanteen.menu.mapper.DailyMenuMapper;
import com.smartcanteen.menu.mapper.DishMapper;
import com.smartcanteen.menu.service.DailyMenuService;
import com.smartcanteen.menu.vo.DailyMenuVO;
import com.smartcanteen.menu.vo.DishVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 每日菜单服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DailyMenuServiceImpl implements DailyMenuService {

    private final DailyMenuMapper dailyMenuMapper;
    private final DailyMenuItemMapper dailyMenuItemMapper;
    private final DishMapper dishMapper;

    @Override
    @Transactional
    public DailyMenuVO create(DailyMenuDTO dto) {
        // 校验 dishIds 对应的菜品是否存在
        List<Dish> dishes = dishMapper.selectBatchIds(dto.getDishIds());
        if (dishes.size() != dto.getDishIds().size()) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "部分菜品 ID 不存在");
        }

        // 检查同日期+餐次是否已有活跃菜单
        List<DailyMenu> existing = dailyMenuMapper.selectList(
                new LambdaQueryWrapper<DailyMenu>()
                        .eq(DailyMenu::getMenuDate, dto.getMenuDate())
                        .eq(DailyMenu::getMealPeriod, dto.getMealPeriod())
                        .eq(DailyMenu::getStatus, "ACTIVE"));
        if (!existing.isEmpty()) {
            throw new BusinessException(ErrorCode.BUSINESS_ERROR, "该日期+餐次已存在每日菜单");
        }

        // 创建菜单
        DailyMenu menu = new DailyMenu();
        menu.setMenuDate(dto.getMenuDate());
        menu.setMealPeriod(dto.getMealPeriod());
        menu.setStartTime(dto.getStartTime());
        menu.setEndTime(dto.getEndTime());
        menu.setStatus("ACTIVE");
        menu.setCreatedAt(LocalDateTime.now());
        dailyMenuMapper.insert(menu);

        // 关联菜品
        for (Long dishId : dto.getDishIds()) {
            DailyMenuItem item = new DailyMenuItem();
            item.setMenuId(menu.getId());
            item.setDishId(dishId);
            dailyMenuItemMapper.insert(item);
        }

        log.info("每日菜单创建成功: id={}, date={}, period={}", menu.getId(), dto.getMenuDate(), dto.getMealPeriod());
        return assembleVO(menu, dishes);
    }

    @Override
    public List<DailyMenuVO> getTodayMenu() {
        // 查询今日所有菜单
        List<DailyMenu> menus = dailyMenuMapper.selectList(
                new LambdaQueryWrapper<DailyMenu>()
                        .eq(DailyMenu::getMenuDate, LocalDate.now())
                        .eq(DailyMenu::getStatus, "ACTIVE")
        );

        if (menus.isEmpty()) {
            return List.of();
        }

        // 批量获取菜单-菜品关联
        List<Long> menuIds = menus.stream().map(DailyMenu::getId).toList();
        List<DailyMenuItem> allItems = dailyMenuItemMapper.selectList(
                new LambdaQueryWrapper<DailyMenuItem>().in(DailyMenuItem::getMenuId, menuIds)
        );

        // 批量获取菜品
        List<Long> dishIds = allItems.stream()
                .map(DailyMenuItem::getDishId)
                .distinct()
                .toList();
        Map<Long, Dish> dishMap = dishMapper.selectBatchIds(dishIds).stream()
                .collect(Collectors.toMap(Dish::getId, d -> d));

        // 组装 VO
        return menus.stream().map(menu -> {
            List<Dish> menuDishes = allItems.stream()
                    .filter(item -> item.getMenuId().equals(menu.getId()))
                    .map(item -> dishMap.get(item.getDishId()))
                    .collect(Collectors.toList());
            return assembleVO(menu, menuDishes);
        }).toList();
    }

    @Override
    public List<DailyMenuVO> list(LocalDate menuDate, String mealPeriod) {
        LambdaQueryWrapper<DailyMenu> wrapper = new LambdaQueryWrapper<DailyMenu>();
        if (menuDate != null) {
            wrapper.eq(DailyMenu::getMenuDate, menuDate);
        }
        if (mealPeriod != null && !mealPeriod.isEmpty()) {
            wrapper.eq(DailyMenu::getMealPeriod, mealPeriod);
        }
        wrapper.orderByDesc(DailyMenu::getMenuDate)
               .orderByDesc(DailyMenu::getCreatedAt);

        List<DailyMenu> menus = dailyMenuMapper.selectList(wrapper);
        if (menus.isEmpty()) {
            return List.of();
        }

        // 批量获取关联
        List<Long> menuIds = menus.stream().map(DailyMenu::getId).toList();
        List<DailyMenuItem> allItems = dailyMenuItemMapper.selectList(
                new LambdaQueryWrapper<DailyMenuItem>().in(DailyMenuItem::getMenuId, menuIds));

        List<Long> dishIds = allItems.stream()
                .map(DailyMenuItem::getDishId).distinct().toList();
        Map<Long, Dish> dishMap = dishMapper.selectBatchIds(dishIds).stream()
                .collect(Collectors.toMap(Dish::getId, d -> d));

        return menus.stream().map(menu -> {
            List<Dish> menuDishes = allItems.stream()
                    .filter(item -> item.getMenuId().equals(menu.getId()))
                    .map(item -> dishMap.get(item.getDishId()))
                    .collect(Collectors.toList());
            return assembleVO(menu, menuDishes);
        }).toList();
    }

    @Override
    @Transactional
    public DailyMenuVO update(Long id, DailyMenuDTO dto) {
        DailyMenu menu = dailyMenuMapper.selectById(id);
        if (menu == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "每日菜单不存在");
        }

        // 校验 dishIds 对应的菜品存在，且为 ON_SALE + stock > 0
        List<Dish> dishes = dishMapper.selectBatchIds(dto.getDishIds());
        if (dishes.size() != dto.getDishIds().size()) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "部分菜品 ID 不存在");
        }

        // 更新主表
        menu.setMenuDate(dto.getMenuDate());
        menu.setMealPeriod(dto.getMealPeriod());
        menu.setStartTime(dto.getStartTime());
        menu.setEndTime(dto.getEndTime());
        dailyMenuMapper.updateById(menu);

        // 删除旧关联
        dailyMenuItemMapper.delete(
                new LambdaQueryWrapper<DailyMenuItem>()
                        .eq(DailyMenuItem::getMenuId, id));

        // 插入新关联
        for (Long dishId : dto.getDishIds()) {
            DailyMenuItem item = new DailyMenuItem();
            item.setMenuId(id);
            item.setDishId(dishId);
            dailyMenuItemMapper.insert(item);
        }

        log.info("每日菜单更新成功: id={}, date={}, period={}", id, dto.getMenuDate(), dto.getMealPeriod());
        return assembleVO(menu, dishes);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        DailyMenu menu = dailyMenuMapper.selectById(id);
        if (menu == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "每日菜单不存在");
        }

        dailyMenuItemMapper.delete(
                new LambdaQueryWrapper<DailyMenuItem>()
                        .eq(DailyMenuItem::getMenuId, id));
        dailyMenuMapper.deleteById(id);

        log.info("每日菜单已删除: id={}, date={}, period={}", id, menu.getMenuDate(), menu.getMealPeriod());
    }

    /** 将实体+菜品列表组装为 VO */
    private DailyMenuVO assembleVO(DailyMenu menu, List<Dish> dishes) {
        String periodDesc = "";
        try {
            periodDesc = MealPeriod.valueOf(menu.getMealPeriod()).getDescription();
        } catch (IllegalArgumentException ignored) {
        }

        return DailyMenuVO.builder()
                .id(menu.getId())
                .menuDate(menu.getMenuDate())
                .mealPeriod(menu.getMealPeriod())
                .mealPeriodDesc(periodDesc)
                .startTime(menu.getStartTime())
                .endTime(menu.getEndTime())
                .status(menu.getStatus())
                .dishCount(dishes.size())
                .dishes(dishes.stream().map(DishVO::fromEntity).toList())
                .build();
    }
}
