package com.smartcanteen.pickup.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.smartcanteen.pickup.entity.PickupQueue;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface PickupQueueMapper extends BaseMapper<PickupQueue> {

    /** 原子更新取餐状态：仅当前状态为 expectedStatus 时才更新为 targetStatus */
    @Update("UPDATE pickup_queue SET status = #{targetStatus}, finish_time = NOW(), updated_at = NOW() " +
            "WHERE id = #{id} AND status = #{expectedStatus}")
    int updateStatus(@Param("id") Long id,
                     @Param("expectedStatus") String expectedStatus,
                     @Param("targetStatus") String targetStatus);
}
