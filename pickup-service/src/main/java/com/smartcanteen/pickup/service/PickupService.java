package com.smartcanteen.pickup.service;

import com.smartcanteen.pickup.dto.CreateWindowDTO;
import com.smartcanteen.pickup.dto.VerifyRequest;
import com.smartcanteen.pickup.vo.PickupQueueVO;
import com.smartcanteen.pickup.vo.PickupWindowVO;

import java.util.List;

public interface PickupService {

    /** 新增窗口 */
    PickupWindowVO createWindow(CreateWindowDTO dto);

    /** 查询窗口列表 */
    List<PickupWindowVO> listWindows();

    /** 启用窗口 */
    void enableWindow(Long windowId);

    /** 停用窗口 */
    void disableWindow(Long windowId);

    /** 加入取餐队列（由 order-service Feign 调用） */
    void addToQueue(Long orderId, Long userId, Long windowId, Integer pickupNo, String pickupCode);

    /** 查询窗口排队队列 */
    List<PickupQueueVO> getWindowQueue(Long windowId);

    /** 叫下一号 */
    PickupQueueVO callNext(Long windowId);

    /** 取餐核销 */
    PickupQueueVO verifyPickup(VerifyRequest request);
}
