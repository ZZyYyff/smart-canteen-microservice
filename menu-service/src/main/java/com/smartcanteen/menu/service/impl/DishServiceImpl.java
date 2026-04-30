package com.smartcanteen.menu.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.smartcanteen.common.enums.DishStatus;
import com.smartcanteen.common.enums.ErrorCode;
import com.smartcanteen.common.exception.BusinessException;
import com.smartcanteen.menu.dto.DishDTO;
import com.smartcanteen.menu.entity.Dish;
import com.smartcanteen.menu.mapper.DishMapper;
import com.smartcanteen.menu.service.DishService;
import com.smartcanteen.menu.vo.DishVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 菜品服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DishServiceImpl implements DishService {

    private final DishMapper dishMapper;

    @Override
    @Transactional
    public DishVO create(DishDTO dto) {
        Dish dish = new Dish();
        dish.setName(dto.getName());
        dish.setPrice(dto.getPrice());
        dish.setDescription(dto.getDescription());
        dish.setImageUrl(dto.getImageUrl());
        dish.setStock(dto.getStock());
        dish.setWarningStock(dto.getWarningStock() != null ? dto.getWarningStock() : 0);
        dish.setStatus(DishStatus.OFF_SALE.name());
        dish.setCreatedAt(LocalDateTime.now());
        dish.setUpdatedAt(LocalDateTime.now());

        dishMapper.insert(dish);
        log.info("菜品创建成功: id={}, name={}", dish.getId(), dish.getName());
        return DishVO.fromEntity(dish);
    }

    @Override
    @Transactional
    public DishVO update(Long id, DishDTO dto) {
        Dish dish = dishMapper.selectById(id);
        if (dish == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "菜品不存在");
        }

        if (dto.getName() != null) {
            dish.setName(dto.getName());
        }
        if (dto.getPrice() != null) {
            dish.setPrice(dto.getPrice());
        }
        if (dto.getDescription() != null) {
            dish.setDescription(dto.getDescription());
        }
        if (dto.getImageUrl() != null) {
            dish.setImageUrl(dto.getImageUrl());
        }
        if (dto.getStock() != null) {
            dish.setStock(dto.getStock());
        }
        if (dto.getWarningStock() != null) {
            dish.setWarningStock(dto.getWarningStock());
        }
        dish.setUpdatedAt(LocalDateTime.now());

        dishMapper.updateById(dish);
        log.info("菜品更新成功: id={}", id);
        return DishVO.fromEntity(dish);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        Dish dish = dishMapper.selectById(id);
        if (dish == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "菜品不存在");
        }
        dishMapper.deleteById(id);
        log.info("菜品删除成功: id={}", id);
    }

    @Override
    public List<DishVO> list(String name, String status) {
        LambdaQueryWrapper<Dish> wrapper = new LambdaQueryWrapper<>();
        if (name != null && !name.isBlank()) {
            wrapper.like(Dish::getName, name);
        }
        if (status != null && !status.isBlank()) {
            wrapper.eq(Dish::getStatus, status);
        }
        wrapper.orderByDesc(Dish::getCreatedAt);

        return dishMapper.selectList(wrapper).stream()
                .map(DishVO::fromEntity)
                .toList();
    }

    @Override
    public DishVO getById(Long id) {
        Dish dish = dishMapper.selectById(id);
        if (dish == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "菜品不存在");
        }
        return DishVO.fromEntity(dish);
    }

    @Override
    @Transactional
    public DishVO onSale(Long id) {
        Dish dish = dishMapper.selectById(id);
        if (dish == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "菜品不存在");
        }
        dish.setStatus(DishStatus.ON_SALE.name());
        dish.setUpdatedAt(LocalDateTime.now());
        dishMapper.updateById(dish);
        log.info("菜品上架: id={}", id);
        return DishVO.fromEntity(dish);
    }

    @Override
    @Transactional
    public DishVO offSale(Long id) {
        Dish dish = dishMapper.selectById(id);
        if (dish == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "菜品不存在");
        }
        dish.setStatus(DishStatus.OFF_SALE.name());
        dish.setUpdatedAt(LocalDateTime.now());
        dishMapper.updateById(dish);
        log.info("菜品下架: id={}", id);
        return DishVO.fromEntity(dish);
    }

    @Override
    @Transactional
    public void deductStock(Long dishId, Integer quantity) {
        // 原子扣减：SQL 层面保证 stock >= quantity
        int rows = dishMapper.deductStock(dishId, quantity);
        if (rows == 0) {
            Dish dish = dishMapper.selectById(dishId);
            if (dish == null) {
                throw new BusinessException(ErrorCode.NOT_FOUND, "菜品不存在");
            }
            throw new BusinessException(3002,
                    "库存不足，当前库存: " + dish.getStock() + "，需要: " + quantity);
        }
        log.info("库存扣减成功: dishId={}, quantity={}", dishId, quantity);
    }

    @Override
    @Transactional
    public void restoreStock(Long dishId, Integer quantity) {
        dishMapper.restoreStock(dishId, quantity);
        log.info("库存恢复成功: dishId={}, quantity={}", dishId, quantity);
    }
}
