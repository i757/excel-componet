package com.zzia.excle.progress;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 每个sheet页的进度
 * @author luo gaoYang
 * @date 2021-07-11 15:21
 **/
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SheetProgress {
    /**
     * sheet中的总条数
     */
    private int totalCount;
    /**
     * 已经处理的条数
     */
    private int dealCount;
    /**
     * sheet的索引，从1开始
     */
    private int sheet;
    /**
     * sheet中的进度
     */
    private double progress;

    private String msg;

    public void setMsg() {
        msg = "当前正在处理第 " + sheet + " 个sheet页，进度：" + progress;
    }
}
