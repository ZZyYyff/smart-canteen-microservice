package com.smartcanteen.order.feign.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 库存操作请求（与 menu-service 的 StockOperateDTO 对应）
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class StockRequest {

    private Integer quantity;
}
