package com.zzia.excle.handler;

import org.apache.poi.ss.usermodel.Row;

/**
 * @author luo gaoYang
 * @date 2021-08-20 10:04
 **/

public interface ExcelHandler {
    /**
     * 处理业务逻辑.
     * @param row 行对象
     * @param params 业务逻辑中所需参数
     * @return object
     */
    Object importExcel(Row row,Object... params);
}
