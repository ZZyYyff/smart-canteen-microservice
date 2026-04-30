package com.smartcanteen.order.feign;

import com.smartcanteen.common.result.Result;
import com.smartcanteen.order.feign.dto.PickupQueueRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * 调用 pickup-service 的 Feign 客户端
 */
@FeignClient(name = "pickup-service")
public interface PickupFeignClient {

    /** 将订单加入取餐队列 */
    @PostMapping("/api/pickup/queue")
    Result<Void> addToQueue(@RequestBody PickupQueueRequest request);
}
