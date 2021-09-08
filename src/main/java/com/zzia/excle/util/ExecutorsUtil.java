package com.zzia.excle.util;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author luo gaoYang
 * @date 2021-09-08 10:06
 **/

public class ExecutorsUtil {

    public static int corePoolSize;

    private ExecutorsUtil(){}

    public static ExecutorService newFixedThreadPool(){
        // 获取cpu的核心数
        int cpuCount = Runtime.getRuntime().availableProcessors();
        // 计算密集型任务，线程数 = 核心数 + 1
        corePoolSize = cpuCount + 1;
        return newFixedThreadPool(corePoolSize);
    }

    public static ExecutorService newFixedThreadPool(int nThreads){
        corePoolSize = nThreads;
        return Executors.newFixedThreadPool(nThreads);
    }
}
