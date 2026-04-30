package com.smartcanteen.pickup.vo;

import com.smartcanteen.common.enums.WindowStatus;
import com.smartcanteen.pickup.entity.PickupWindow;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class PickupWindowVO {

    private Long id;
    private String name;
    private String location;
    private String status;
    private String statusDesc;
    private LocalDateTime createdAt;

    public static PickupWindowVO fromEntity(PickupWindow w) {
        PickupWindowVO vo = new PickupWindowVO();
        vo.setId(w.getId());
        vo.setName(w.getName());
        vo.setLocation(w.getLocation());
        vo.setStatus(w.getStatus());
        vo.setCreatedAt(w.getCreatedAt());
        try {
            vo.setStatusDesc(WindowStatus.valueOf(w.getStatus()).getDescription());
        } catch (IllegalArgumentException e) {
            vo.setStatusDesc(w.getStatus());
        }
        return vo;
    }
}
