package com.smartcanteen.menu.service;

import com.smartcanteen.menu.dto.DishDTO;
import com.smartcanteen.menu.vo.DishVO;

import java.util.List;

/**
 * 菜品服务接口
 */
public interface DishService {

    /** 新增菜品，默认状态为下架 */
    DishVO create(DishDTO dto);

    /** 修改菜品信息 */
    DishVO update(Long id, DishDTO dto);

    /** 删除菜品 */
    void delete(Long id);

    /** 查询菜品列表，可按名称模糊搜索和状态筛选 */
    List<DishVO> list(String name, String status);

    /** 查询菜品详情 */
    DishVO getById(Long id);

    /** 上架菜品 */
    DishVO onSale(Long id);

    /** 下架菜品 */
    DishVO offSale(Long id);

    /** 扣减库存（供 order-service 调用），库存不足时抛 BusinessException */
    void deductStock(Long dishId, Integer quantity);

    /** 恢复库存（取消订单时调用） */
    void restoreStock(Long dishId, Integer quantity);
}
