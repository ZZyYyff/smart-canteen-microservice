package com.smartcanteen.pickup.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartcanteen.common.enums.ErrorCode;
import com.smartcanteen.common.enums.PickupQueueStatus;
import com.smartcanteen.common.enums.WindowStatus;
import com.smartcanteen.common.exception.BusinessException;
import com.smartcanteen.common.result.Result;
import com.smartcanteen.pickup.dto.CreateWindowDTO;
import com.smartcanteen.pickup.dto.VerifyRequest;
import com.smartcanteen.pickup.entity.PickupQueue;
import com.smartcanteen.pickup.entity.PickupWindow;
import com.smartcanteen.pickup.feign.OrderFeignClient;
import com.smartcanteen.pickup.mapper.PickupQueueMapper;
import com.smartcanteen.pickup.mapper.PickupWindowMapper;
import com.smartcanteen.pickup.service.PickupService;
import com.smartcanteen.pickup.vo.PickupQueueVO;
import com.smartcanteen.pickup.vo.PickupWindowVO;
import com.smartcanteen.pickup.vo.ScreenMessage;
import com.smartcanteen.pickup.websocket.PickupScreenHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class PickupServiceImpl implements PickupService {

    private final PickupWindowMapper windowMapper;
    private final PickupQueueMapper queueMapper;
    private final OrderFeignClient orderFeignClient;

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper()
            .findAndRegisterModules();

    @Override
    @Transactional
    public PickupWindowVO createWindow(CreateWindowDTO dto) {
        PickupWindow window = new PickupWindow();
        window.setName(dto.getName());
        window.setLocation(dto.getLocation());
        window.setStatus(WindowStatus.ACTIVE.name());
        window.setCreatedAt(LocalDateTime.now());
        window.setUpdatedAt(LocalDateTime.now());
        windowMapper.insert(window);
        log.info("窗口创建成功: id={}, name={}", window.getId(), dto.getName());
        return PickupWindowVO.fromEntity(window);
    }

    @Override
    public List<PickupWindowVO> listWindows() {
        List<PickupWindow> windows = windowMapper.selectList(null);
        return windows.stream().map(PickupWindowVO::fromEntity).toList();
    }

    @Override
    @Transactional
    public void enableWindow(Long windowId) {
        PickupWindow window = getWindowOrThrow(windowId);
        window.setStatus(WindowStatus.ACTIVE.name());
        window.setUpdatedAt(LocalDateTime.now());
        windowMapper.updateById(window);
        log.info("窗口已启用: id={}", windowId);
    }

    @Override
    @Transactional
    public void disableWindow(Long windowId) {
        PickupWindow window = getWindowOrThrow(windowId);
        window.setStatus(WindowStatus.DISABLED.name());
        window.setUpdatedAt(LocalDateTime.now());
        windowMapper.updateById(window);
        log.info("窗口已停用: id={}", windowId);
    }

    @Override
    @Transactional
    public void addToQueue(Long orderId, Long userId, Long windowId, Integer pickupNo, String pickupCode) {
        PickupQueue queue = new PickupQueue();
        queue.setWindowId(windowId);
        queue.setOrderId(orderId);
        queue.setPickupNo(pickupNo);
        queue.setPickupCode(pickupCode);
        queue.setStatus(PickupQueueStatus.WAITING.name());
        queue.setQueueTime(LocalDateTime.now());
        queue.setCreatedAt(LocalDateTime.now());
        queue.setUpdatedAt(LocalDateTime.now());
        queueMapper.insert(queue);
        log.info("加入取餐队列: orderId={}, windowId={}, pickupNo={}", orderId, windowId, pickupNo);
    }

    @Override
    public List<PickupQueueVO> getWindowQueue(Long windowId) {
        List<PickupQueue> queues = queueMapper.selectList(
                new LambdaQueryWrapper<PickupQueue>()
                        .eq(PickupQueue::getWindowId, windowId)
                        .eq(PickupQueue::getStatus, PickupQueueStatus.WAITING.name())
                        .orderByAsc(PickupQueue::getQueueTime));
        PickupWindow window = windowMapper.selectById(windowId);
        String windowName = window != null ? window.getName() : null;
        return queues.stream().map(q -> {
            PickupQueueVO vo = PickupQueueVO.fromEntity(q);
            vo.setWindowName(windowName);
            return vo;
        }).toList();
    }

    @Override
    @Transactional
    public PickupQueueVO callNext(Long windowId) {
        PickupWindow window = getWindowOrThrow(windowId);

        // 找 queue_time 最早的 WAITING 记录
        List<PickupQueue> waitingList = queueMapper.selectList(
                new LambdaQueryWrapper<PickupQueue>()
                        .eq(PickupQueue::getWindowId, windowId)
                        .eq(PickupQueue::getStatus, PickupQueueStatus.WAITING.name())
                        .orderByAsc(PickupQueue::getQueueTime)
                        .last("LIMIT 1"));

        if (waitingList.isEmpty()) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "当前窗口没有排队记录");
        }

        PickupQueue queue = waitingList.get(0);
        queue.setStatus(PickupQueueStatus.CALLED.name());
        queue.setCallTime(LocalDateTime.now());
        queue.setUpdatedAt(LocalDateTime.now());
        queueMapper.updateById(queue);

        log.info("叫号: windowId={}, pickupNo={}", windowId, queue.getPickupNo());

        // 获取更新后的等待队列并推送大屏
        List<PickupQueueVO> waitingQueue = getWindowQueue(windowId);
        PickupQueueVO vo = PickupQueueVO.fromEntity(queue);
        vo.setWindowName(window.getName());

        try {
            ScreenMessage msg = ScreenMessage.call(windowId, window.getName(),
                    queue.getPickupNo(), waitingQueue,
                    "请 " + queue.getPickupNo() + " 号到【" + window.getName() + "】取餐");
            PickupScreenHandler.broadcast(OBJECT_MAPPER.writeValueAsString(msg));
        } catch (Exception e) {
            log.error("WebSocket 推送失败: windowId={}", windowId, e);
        }

        return vo;
    }

    @Override
    @Transactional
    public PickupQueueVO verifyPickup(VerifyRequest request) {
        // 根据 pickupNo 查找排队记录（取最新一条）
        List<PickupQueue> queues = queueMapper.selectList(
                new LambdaQueryWrapper<PickupQueue>()
                        .eq(PickupQueue::getPickupNo, request.getPickupNo())
                        .orderByDesc(PickupQueue::getQueueTime)
                        .last("LIMIT 1"));

        if (queues.isEmpty()) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "取餐记录不存在");
        }

        PickupQueue queue = queues.get(0);

        // 校验取餐码
        if (!request.getPickupCode().equals(queue.getPickupCode())) {
            throw new BusinessException(5001, "取餐码错误");
        }

        // 校验状态
        if (PickupQueueStatus.FINISHED.name().equals(queue.getStatus())) {
            throw new BusinessException(5002, "该订单已取餐");
        }

        if (!PickupQueueStatus.CALLED.name().equals(queue.getStatus())) {
            throw new BusinessException(ErrorCode.ORDER_STATUS_ERROR, "当前状态不允许核销，请等待叫号");
        }

        // 原子更新：仅 CALLED → FINISHED，防止并发重复核销
        int rows = queueMapper.updateStatus(queue.getId(),
                PickupQueueStatus.CALLED.name(), PickupQueueStatus.FINISHED.name());
        if (rows == 0) {
            throw new BusinessException(5002, "该订单已取餐或状态异常");
        }
        queue.setStatus(PickupQueueStatus.FINISHED.name());
        queue.setFinishTime(LocalDateTime.now());

        // 调用 order-service 完成订单
        try {
            Result<Void> result = orderFeignClient.completeOrder(queue.getOrderId());
            if (result.getCode() != 200) {
                log.warn("调用order-service完成订单返回非成功: orderId={}, message={}",
                        queue.getOrderId(), result.getMessage());
            }
        } catch (Exception e) {
            log.error("调用order-service完成订单失败: orderId={}", queue.getOrderId(), e);
        }

        PickupWindow window = windowMapper.selectById(queue.getWindowId());
        PickupQueueVO vo = PickupQueueVO.fromEntity(queue);
        if (window != null) {
            vo.setWindowName(window.getName());
        }

        log.info("取餐核销成功: pickupNo={}, orderId={}", request.getPickupNo(), queue.getOrderId());
        return vo;
    }

    private PickupWindow getWindowOrThrow(Long windowId) {
        PickupWindow window = windowMapper.selectById(windowId);
        if (window == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "窗口不存在");
        }
        return window;
    }
}
