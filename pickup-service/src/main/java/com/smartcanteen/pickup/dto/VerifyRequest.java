package com.smartcanteen.pickup.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class VerifyRequest {

    @NotNull(message = "取餐号不能为空")
    private Integer pickupNo;

    @NotNull(message = "取餐码不能为空")
    private String pickupCode;
}
