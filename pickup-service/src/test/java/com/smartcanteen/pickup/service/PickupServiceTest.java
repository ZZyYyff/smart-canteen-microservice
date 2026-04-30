package com.smartcanteen.pickup.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
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
import com.smartcanteen.pickup.service.impl.PickupServiceImpl;
import com.smartcanteen.pickup.vo.PickupQueueVO;
import com.smartcanteen.pickup.vo.PickupWindowVO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PickupServiceTest {

    @Mock private PickupWindowMapper windowMapper;
    @Mock private PickupQueueMapper queueMapper;
    @Mock private OrderFeignClient orderFeignClient;
    @InjectMocks private PickupServiceImpl pickupService;

    private PickupWindow mockWindow;
    private PickupQueue mockQueue;

    @BeforeEach
    void setUp() {
        mockWindow = new PickupWindow();
        mockWindow.setId(1L);
        mockWindow.setName("1号窗口");
        mockWindow.setLocation("一楼A区");
        mockWindow.setStatus(WindowStatus.ACTIVE.name());

        mockQueue = new PickupQueue();
        mockQueue.setId(1L);
        mockQueue.setWindowId(1L);
        mockQueue.setOrderId(100L);
        mockQueue.setPickupNo(123);
        mockQueue.setPickupCode("567890");
        mockQueue.setStatus(PickupQueueStatus.WAITING.name());
        mockQueue.setQueueTime(LocalDateTime.now());
    }

    // ==================== 窗口管理测试 ====================

    @Test
    @DisplayName("1. 新增窗口成功")
    void testCreateWindow() {
        CreateWindowDTO dto = new CreateWindowDTO();
        dto.setName("1号窗口");
        dto.setLocation("一楼A区");

        when(windowMapper.insert(any(PickupWindow.class))).thenReturn(1);

        PickupWindowVO result = pickupService.createWindow(dto);
        assertNotNull(result);
        assertEquals("1号窗口", result.getName());
        assertEquals(WindowStatus.ACTIVE.name(), result.getStatus());
        assertEquals("启用", result.getStatusDesc());
        verify(windowMapper).insert(any(PickupWindow.class));
    }

    @Test
    @DisplayName("2. 查询窗口列表")
    void testListWindows() {
        when(windowMapper.selectList(isNull())).thenReturn(List.of(mockWindow));

        List<PickupWindowVO> results = pickupService.listWindows();
        assertEquals(1, results.size());
        assertEquals("1号窗口", results.get(0).getName());
    }

    @Test
    @DisplayName("3. 启用窗口")
    void testEnableWindow() {
        when(windowMapper.selectById(1L)).thenReturn(mockWindow);
        when(windowMapper.updateById(any(PickupWindow.class))).thenReturn(1);

        mockWindow.setStatus(WindowStatus.DISABLED.name());
        pickupService.enableWindow(1L);
        assertEquals(WindowStatus.ACTIVE.name(), mockWindow.getStatus());
        verify(windowMapper).updateById(mockWindow);
    }

    @Test
    @DisplayName("4. 停用窗口")
    void testDisableWindow() {
        when(windowMapper.selectById(1L)).thenReturn(mockWindow);
        when(windowMapper.updateById(any(PickupWindow.class))).thenReturn(1);

        pickupService.disableWindow(1L);
        assertEquals(WindowStatus.DISABLED.name(), mockWindow.getStatus());
        verify(windowMapper).updateById(mockWindow);
    }

    @Test
    @DisplayName("5. 操作不存在的窗口抛异常")
    void testWindowNotFound() {
        when(windowMapper.selectById(999L)).thenReturn(null);
        assertThrows(BusinessException.class, () -> pickupService.enableWindow(999L));
    }

    // ==================== 排队队列测试 ====================

    @Test
    @DisplayName("6. 加入取餐队列成功")
    void testAddToQueue() {
        when(queueMapper.insert(any(PickupQueue.class))).thenReturn(1);

        pickupService.addToQueue(100L, 1L, 1L, 123, "567890");

        verify(queueMapper).insert(any(PickupQueue.class));
    }

    @Test
    @DisplayName("7. 查询窗口等待队列（按 queue_time 升序）")
    void testGetWindowQueue() {
        PickupQueue q1 = new PickupQueue();
        q1.setId(1L); q1.setWindowId(1L); q1.setPickupNo(101);
        q1.setStatus(PickupQueueStatus.WAITING.name());
        q1.setQueueTime(LocalDateTime.now().minusMinutes(5));

        PickupQueue q2 = new PickupQueue();
        q2.setId(2L); q2.setWindowId(1L); q2.setPickupNo(102);
        q2.setStatus(PickupQueueStatus.WAITING.name());
        q2.setQueueTime(LocalDateTime.now());

        when(windowMapper.selectById(1L)).thenReturn(mockWindow);
        when(queueMapper.selectList(any(LambdaQueryWrapper.class)))
                .thenReturn(List.of(q1, q2));

        List<PickupQueueVO> results = pickupService.getWindowQueue(1L);
        assertEquals(2, results.size());
        assertEquals(101, results.get(0).getPickupNo());
        assertEquals(102, results.get(1).getPickupNo());
        assertEquals("1号窗口", results.get(0).getWindowName());
    }

    @Test
    @DisplayName("8. 空窗口队列返回空列表")
    void testGetWindowQueueEmpty() {
        when(windowMapper.selectById(1L)).thenReturn(mockWindow);
        when(queueMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(List.of());

        List<PickupQueueVO> results = pickupService.getWindowQueue(1L);
        assertTrue(results.isEmpty());
    }

    // ==================== 叫号测试 ====================

    @Test
    @DisplayName("9. 叫号成功：选择最早的 WAITING 记录，状态改为 CALLED")
    void testCallNext() {
        when(windowMapper.selectById(1L)).thenReturn(mockWindow);

        PickupQueue waitingQueue = new PickupQueue();
        waitingQueue.setId(1L);
        waitingQueue.setWindowId(1L);
        waitingQueue.setOrderId(100L);
        waitingQueue.setPickupNo(123);
        waitingQueue.setPickupCode("567890");
        waitingQueue.setStatus(PickupQueueStatus.WAITING.name());
        waitingQueue.setQueueTime(LocalDateTime.now().minusMinutes(10));

        // callNext 内部查询最早 WAITING
        when(queueMapper.selectList(any(LambdaQueryWrapper.class)))
                .thenReturn(List.of(waitingQueue))   // 第一次查询：找到等待记录
                .thenReturn(List.of());               // 第二次查询（getWindowQueue for broadcast）：空队列
        when(queueMapper.updateById(any(PickupQueue.class))).thenReturn(1);

        PickupQueueVO result = pickupService.callNext(1L);
        assertNotNull(result);
        assertEquals(123, result.getPickupNo());
        assertEquals(PickupQueueStatus.CALLED.name(), result.getStatus());

        verify(queueMapper).updateById(any(PickupQueue.class));
    }

    @Test
    @DisplayName("10. 空队列叫号抛异常")
    void testCallNextEmptyQueue() {
        when(windowMapper.selectById(1L)).thenReturn(mockWindow);
        when(queueMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(List.of());

        BusinessException ex = assertThrows(BusinessException.class,
                () -> pickupService.callNext(1L));
        assertThat(ex.getMessage()).contains("没有排队记录");
    }

    // ==================== 核销测试 ====================

    @Test
    @DisplayName("11. 错误取餐码核销失败")
    void testVerifyWrongPickupCode() {
        PickupQueue calledQueue = new PickupQueue();
        calledQueue.setId(1L);
        calledQueue.setWindowId(1L);
        calledQueue.setPickupNo(123);
        calledQueue.setPickupCode("567890");
        calledQueue.setStatus(PickupQueueStatus.CALLED.name());

        when(queueMapper.selectList(any(LambdaQueryWrapper.class)))
                .thenReturn(List.of(calledQueue));

        VerifyRequest req = new VerifyRequest();
        req.setPickupNo(123);
        req.setPickupCode("wrong-code");

        BusinessException ex = assertThrows(BusinessException.class,
                () -> pickupService.verifyPickup(req));
        assertThat(ex.getMessage()).contains("取餐码错误");
    }

    @Test
    @DisplayName("12. 正确取餐码核销成功，并调用 order-service")
    void testVerifyPickupSuccess() {
        PickupQueue calledQueue = new PickupQueue();
        calledQueue.setId(1L);
        calledQueue.setWindowId(1L);
        calledQueue.setOrderId(100L);
        calledQueue.setPickupNo(123);
        calledQueue.setPickupCode("567890");
        calledQueue.setStatus(PickupQueueStatus.CALLED.name());

        when(queueMapper.selectList(any(LambdaQueryWrapper.class)))
                .thenReturn(List.of(calledQueue));
        when(queueMapper.updateStatus(1L, PickupQueueStatus.CALLED.name(),
                PickupQueueStatus.FINISHED.name())).thenReturn(1);
        when(windowMapper.selectById(1L)).thenReturn(mockWindow);
        when(orderFeignClient.completeOrder(100L)).thenReturn(Result.success());

        VerifyRequest req = new VerifyRequest();
        req.setPickupNo(123);
        req.setPickupCode("567890");

        PickupQueueVO result = pickupService.verifyPickup(req);
        assertNotNull(result);
        assertEquals(PickupQueueStatus.FINISHED.name(), result.getStatus());
        assertEquals("已取餐", result.getStatusDesc());

        verify(orderFeignClient).completeOrder(100L);
        verify(queueMapper).updateStatus(1L, PickupQueueStatus.CALLED.name(),
                PickupQueueStatus.FINISHED.name());
    }

    @Test
    @DisplayName("13. 取餐记录不存在")
    void testVerifyPickupNotFound() {
        when(queueMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(List.of());

        VerifyRequest req = new VerifyRequest();
        req.setPickupNo(999);
        req.setPickupCode("000000");

        assertThrows(BusinessException.class, () -> pickupService.verifyPickup(req));
    }

    @Test
    @DisplayName("14. 已完成取餐不可重复核销")
    void testVerifyAlreadyFinished() {
        PickupQueue finishedQueue = new PickupQueue();
        finishedQueue.setId(1L);
        finishedQueue.setPickupNo(123);
        finishedQueue.setPickupCode("567890");
        finishedQueue.setStatus(PickupQueueStatus.FINISHED.name());

        when(queueMapper.selectList(any(LambdaQueryWrapper.class)))
                .thenReturn(List.of(finishedQueue));

        VerifyRequest req = new VerifyRequest();
        req.setPickupNo(123);
        req.setPickupCode("567890");

        BusinessException ex = assertThrows(BusinessException.class,
                () -> pickupService.verifyPickup(req));
        assertThat(ex.getMessage()).contains("已取餐");
    }
}
