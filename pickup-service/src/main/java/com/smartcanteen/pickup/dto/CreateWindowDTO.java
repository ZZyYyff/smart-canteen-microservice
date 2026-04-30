package com.smartcanteen.pickup.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreateWindowDTO {

    @NotBlank(message = "窗口名称不能为空")
    private String name;

    private String location;
}
