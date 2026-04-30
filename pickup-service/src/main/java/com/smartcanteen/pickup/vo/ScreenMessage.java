package com.smartcanteen.pickup.vo;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class ScreenMessage {

    private String type;
    private Long windowId;
    private String windowName;
    private Integer currentPickupNo;
    private List<PickupQueueVO> waitingQueue;
    private LocalDateTime timestamp;
    private String message;

    public static ScreenMessage call(Long windowId, String windowName, Integer pickupNo,
                                     List<PickupQueueVO> waitingQueue, String message) {
        ScreenMessage msg = new ScreenMessage();
        msg.setType("CALL");
        msg.setWindowId(windowId);
        msg.setWindowName(windowName);
        msg.setCurrentPickupNo(pickupNo);
        msg.setWaitingQueue(waitingQueue);
        msg.setTimestamp(LocalDateTime.now());
        msg.setMessage(message);
        return msg;
    }
}
