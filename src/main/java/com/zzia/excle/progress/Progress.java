package com.zzia.excle.progress;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;

/**
 * excel总进度
 * @author luo gaoYang
 * @date 2021-07-11 15:21
 **/
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Progress {
    public static final double COMPLETED = 1.00;

    /**
     * 总进度
     */
    private double totalProgress;
    /**
     * 每个sheet的进度
     */
    private Map<String, SheetProgress> sheetProgressMap;

    public void setProcess() {
        totalProgress = 0.00;

        sheetProgressMap.forEach((sheetNum, sheetProgress) -> totalProgress = totalProgress + sheetProgress.getProgress());

        BigDecimal bigDecimal = BigDecimal.valueOf(totalProgress);

        totalProgress = bigDecimal.divide(new BigDecimal(sheetProgressMap.size()),2, RoundingMode.DOWN).doubleValue();
    }
}
