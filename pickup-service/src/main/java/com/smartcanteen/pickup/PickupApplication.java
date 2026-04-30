package com.smartcanteen.pickup;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * 取餐与排队服务启动类
 */
@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients
public class PickupApplication {

    public static void main(String[] args) {
        SpringApplication.run(PickupApplication.class, args);
    }
}
