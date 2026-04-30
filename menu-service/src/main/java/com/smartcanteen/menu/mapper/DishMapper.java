package com.smartcanteen.menu.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.smartcanteen.menu.entity.Dish;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

/**
 * 菜品 Mapper，含原子库存操作
 */
@Mapper
public interface DishMapper extends BaseMapper<Dish> {

    /**
     * 原子扣减库存，WHERE stock >= quantity 保证不会扣成负数。
     * @return 影响行数：1 表示扣减成功，0 表示库存不足
     */
    @Update("UPDATE dishes SET stock = stock - #{quantity}, updated_at = NOW() "
            + "WHERE id = #{dishId} AND stock >= #{quantity}")
    int deductStock(@Param("dishId") Long dishId, @Param("quantity") Integer quantity);

    /**
     * 恢复库存（取消订单时调用）
     */
    @Update("UPDATE dishes SET stock = stock + #{quantity}, updated_at = NOW() WHERE id = #{dishId}")
    int restoreStock(@Param("dishId") Long dishId, @Param("quantity") Integer quantity);
}
