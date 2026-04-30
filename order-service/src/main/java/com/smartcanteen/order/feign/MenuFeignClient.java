package com.smartcanteen.order.feign;

import com.smartcanteen.common.result.Result;
import com.smartcanteen.order.feign.dto.StockRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * 调用 menu-service 的 Feign 客户端
 */
@FeignClient(name = "menu-service")
public interface MenuFeignClient {

    /** 扣减库存 */
    @PutMapping("/api/menus/dishes/{dishId}/stock/deduct")
    Result<Void> deductStock(@PathVariable("dishId") Long dishId, @RequestBody StockRequest request);

    /** 恢复库存 */
    @PutMapping("/api/menus/dishes/{dishId}/stock/restore")
    Result<Void> restoreStock(@PathVariable("dishId") Long dishId, @RequestBody StockRequest request);
}
