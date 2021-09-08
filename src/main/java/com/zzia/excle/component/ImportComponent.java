package com.zzia.excle.component;

import com.zzia.excle.handler.AbstractExcelHandler;
import com.zzia.excle.progress.Progress;
import com.zzia.excle.progress.SheetProgress;
import com.zzia.excle.task.WebsocketTask;
import com.zzia.excle.util.ExecutorsUtil;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author luo gaoYang
 * @date 2021-08-20 10:02
 **/
@Component
public class ImportComponent {

    @Autowired
    private ApplicationContext applicationContext;

    /**
     * 导入excel.
     * @param inputStream 输入流
     * @param sessionId 当前会话id
     * @param clazz 继承 {@link AbstractExcelHandler} 的类
     * @param hasTitle excel中是否包含title
     * @param params 处理业务逻辑时所需参数
     * @throws IOException
     */
    public void importExcel(InputStream inputStream, String sessionId, Class<? extends AbstractExcelHandler> clazz, boolean hasTitle, Object... params) throws IOException {
        createWorkbookExecutorService(inputStream,sessionId,clazz,hasTitle,params);
    }

    /**
     * 导入excel.
     * @param file 文件
     * @param sessionId 当前会话id
     * @param clazz 继承 {@link AbstractExcelHandler} 的类
     * @param hasTitle excel中是否包含title
     * @param params 处理业务逻辑时所需参数
     * @throws IOException
     */
    public void importExcel(File file, String sessionId, Class<? extends AbstractExcelHandler> clazz,boolean hasTitle,Object... params) throws IOException {
        InputStream inputStream = new FileInputStream(file);
        importExcel(inputStream,sessionId,clazz,hasTitle,params);
    }

    /**
     * 导入excel.
     * @param multipartFile 文件
     * @param sessionId 当前会话id
     * @param clazz 继承 {@link AbstractExcelHandler} 的类
     * @param hasTitle excel中是否包含title
     * @param params 处理业务逻辑时所需参数
     * @throws IOException
     */
    public void importExcel(MultipartFile multipartFile, String sessionId, Class<? extends AbstractExcelHandler> clazz,boolean hasTitle,Object... params) throws IOException {
        importExcel(multipartFile.getInputStream(),sessionId,clazz,hasTitle,params);
    }

    /**
     * 根据sheet数创建线程池
     * @param inputStream 输入流
     * @param sessionId 当前会话id
     * @param clazz 继承 {@link AbstractExcelHandler} 的类
     * @param hasTitle excel中是否包含title
     * @param params 处理业务逻辑时所需参数
     * @throws IOException
     */
    private void createWorkbookExecutorService(InputStream inputStream, String sessionId, Class<? extends AbstractExcelHandler> clazz,boolean hasTitle,Object... params) throws IOException {
        try (Workbook wb = WorkbookFactory.create(inputStream)) {
            int sheetCount = wb.getNumberOfSheets();
            // 创建线程池
            ExecutorService sheetExecutor = ExecutorsUtil.newFixedThreadPool(sheetCount);
            // 线程同步器
            CountDownLatch sheetLatch = new CountDownLatch(sheetCount);
            Iterator<Sheet> iterator = wb.sheetIterator();
            int sheetNum = 1;
            while (iterator.hasNext()) {
                int finalSheetNum = sheetNum;
                Sheet sheet = iterator.next();
                sheetExecutor.execute(() -> {
                    // 处理sheet中的数据
                    dealSheet(sessionId,sheet, finalSheetNum,hasTitle,clazz,params);
                    // 同步器计数减一
                    sheetLatch.countDown();
                });
                sheetNum++;
            }
            // 线程阻塞，直到同步器计数为0
            sheetLatch.await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            e.printStackTrace();
        }
    }

    /**
     * 处理sheet中的数据
     * @param sessionId 当前会话id
     * @param sheet sheet对象
     * @param sheetNum sheet编号
     * @param hasTitle excel中是否包含title
     * @param clazz 继承 {@link AbstractExcelHandler} 的类
     * @param params 处理业务逻辑时所需参数
     */
    private synchronized void dealSheet(String sessionId,Sheet sheet,int sheetNum,boolean hasTitle, Class<? extends AbstractExcelHandler> clazz,Object... params){
        // sheet中的总条数
        final int rowCount;
        // 数据开始的索引
        int firstRowIndex;
        if(hasTitle){
            rowCount = sheet.getLastRowNum();
            firstRowIndex = 1;
        }else {
            rowCount = sheet.getLastRowNum() + 1;
            firstRowIndex = 0;
        }
        if(rowCount > 0){
            // 处理sheet中的数据
            createSheetExecutorService(sheet,sessionId,rowCount,firstRowIndex,sheetNum,clazz,params);
        }
    }

    /**
     * 创建处理sheet数据的线程池
     * @param sheet sheet对象
     * @param sessionId 当前会话id
     * @param rowCount sheet中的总条数
     * @param firstRowIndex 数据开始的索引
     * @param sheetNum sheet编号
     * @param clazz 继承 {@link AbstractExcelHandler} 的类
     * @param params 处理业务逻辑时所需参数
     */
    private synchronized void createSheetExecutorService(Sheet sheet,String sessionId,int rowCount,int firstRowIndex,int sheetNum,Class<? extends AbstractExcelHandler> clazz,Object... params){
        // sheet中的总条数
        AtomicInteger totalCount = new AtomicInteger();
        // 已经处理的数量
        AtomicInteger dealCount = new AtomicInteger();

        totalCount.addAndGet(rowCount);

        ExecutorService service = ExecutorsUtil.newFixedThreadPool();
        // 线程数
        int nThreads = ExecutorsUtil.corePoolSize;
        // 平均每个线程处理数据的条数
        int avg = rowCount / nThreads;

        for (int t = 0; t < nThreads; t++) {
            int finalT = t;
            service.execute(() -> {
                // 获取数据
                int startRow = finalT * avg + firstRowIndex;
                int endRow;
                if(finalT + 1 == nThreads){
                    endRow = rowCount + firstRowIndex;
                }else {
                    endRow = (finalT + 1) * avg + firstRowIndex;
                }

                while (startRow < endRow){
                    Row row = sheet.getRow(startRow);
                    // 获取到继承AbstractExcelHandler的类，调用importExcel()方法
                    applicationContext.getBean(clazz).importExcel(row,params);
                    // 处理数量加1
                    dealCount.incrementAndGet();
                    // 进度计算
                    process(sessionId,sheetNum,totalCount.get(),dealCount.get());
                    startRow++;
                }
            });
        }
    }

    /**
     * 进度计算
     * @param sessionId 当前会话id
     * @param sheetNum sheet编号
     * @param totalCount sheet中的总条数
     * @param dealCount 已经处理的数量
     */
    private synchronized void process(String sessionId,int sheetNum,int totalCount,int dealCount){
        // 获取数据
        Progress vo = WebsocketTask.CONCURRENT_HASH_MAP.get(sessionId);
        Map<String, SheetProgress> sheetProgressMap;
        SheetProgress sheetProgress;
        if(vo == null){
            vo = new Progress();
            sheetProgressMap = new ConcurrentHashMap<>(5);
            sheetProgress = new SheetProgress();
            vo.setSheetProgressMap(sheetProgressMap);
        }else {
            sheetProgressMap = vo.getSheetProgressMap();
            sheetProgress = sheetProgressMap.get(String.valueOf(sheetNum)) == null ? new SheetProgress() : sheetProgressMap.get(String.valueOf(sheetNum));
        }

        sheetProgress.setTotalCount(totalCount);
        sheetProgressMap.put(String.valueOf(sheetNum), sheetProgress);

        sheetProgress.setSheet(sheetNum);
        sheetProgress.setDealCount(dealCount);
        BigDecimal deal = new BigDecimal(dealCount);
        BigDecimal total = new BigDecimal(totalCount);
        sheetProgress.setProgress(deal.divide(total,2, RoundingMode.DOWN).doubleValue());
        sheetProgress.setMsg();
        vo.setProcess();
        WebsocketTask.CONCURRENT_HASH_MAP.put(sessionId,vo);
    }

}
