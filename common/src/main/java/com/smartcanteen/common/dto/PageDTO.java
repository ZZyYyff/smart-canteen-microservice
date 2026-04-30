package com.smartcanteen.common.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Collections;
import java.util.List;

/**
 * 通用分页返回对象
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PageDTO<T> {

    private Long total;

    private Integer page;

    private Integer size;

    private List<T> records;

    public static <T> PageDTO<T> of(Long total, Integer page, Integer size, List<T> records) {
        return new PageDTO<>(total, page, size, records);
    }

    public static <T> PageDTO<T> empty() {
        return new PageDTO<>(0L, 1, 10, Collections.emptyList());
    }
}
