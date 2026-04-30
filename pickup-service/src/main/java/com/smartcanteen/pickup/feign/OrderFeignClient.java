package com.smartcanteen.pickup.feign;

import com.smartcanteen.common.result.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;

@FeignClient(name = "order-service")
public interface OrderFeignClient {

    @PutMapping("/api/orders/{orderId}/complete")
    Result<Void> completeOrder(@PathVariable("orderId") Long orderId);
}
