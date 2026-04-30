package com.smartcanteen.pickup.controller;

import com.smartcanteen.common.result.Result;
import com.smartcanteen.pickup.dto.AddToQueueRequest;
import com.smartcanteen.pickup.dto.CreateWindowDTO;
import com.smartcanteen.pickup.dto.VerifyRequest;
import com.smartcanteen.pickup.service.PickupService;
import com.smartcanteen.pickup.vo.PickupQueueVO;
import com.smartcanteen.pickup.vo.PickupWindowVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/pickup")
@RequiredArgsConstructor
public class PickupController {

    private final PickupService pickupService;

    /** 新增窗口 */
    @PostMapping("/windows")
    public Result<PickupWindowVO> createWindow(@Valid @RequestBody CreateWindowDTO dto) {
        return Result.success("窗口创建成功", pickupService.createWindow(dto));
    }

    /** 查询窗口列表 */
    @GetMapping("/windows")
    public Result<List<PickupWindowVO>> listWindows() {
        return Result.success(pickupService.listWindows());
    }

    /** 启用窗口 */
    @PutMapping("/windows/{id}/enable")
    public Result<Void> enableWindow(@PathVariable("id") Long id) {
        pickupService.enableWindow(id);
        return Result.success();
    }

    /** 停用窗口 */
    @PutMapping("/windows/{id}/disable")
    public Result<Void> disableWindow(@PathVariable("id") Long id) {
        pickupService.disableWindow(id);
        return Result.success();
    }

    /** 加入取餐队列（由 order-service Feign 调用） */
    @PostMapping("/queue")
    public Result<Void> addToQueue(@Valid @RequestBody AddToQueueRequest request) {
        pickupService.addToQueue(request.getOrderId(), request.getUserId(),
                request.getWindowId(), request.getPickupNo(), request.getPickupCode());
        return Result.success();
    }

    /** 查询窗口排队队列 */
    @GetMapping("/windows/{windowId}/queue")
    public Result<List<PickupQueueVO>> getWindowQueue(@PathVariable Long windowId) {
        return Result.success(pickupService.getWindowQueue(windowId));
    }

    /** 叫下一号 */
    @PostMapping("/windows/{windowId}/call-next")
    public Result<PickupQueueVO> callNext(@PathVariable Long windowId) {
        return Result.success("叫号成功", pickupService.callNext(windowId));
    }

    /** 取餐核销 */
    @PostMapping("/verify")
    public Result<PickupQueueVO> verifyPickup(@Valid @RequestBody VerifyRequest request) {
        return Result.success("核销成功", pickupService.verifyPickup(request));
    }
}
