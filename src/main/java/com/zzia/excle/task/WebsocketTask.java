package com.zzia.excle.task;

import com.alibaba.fastjson.JSON;
import com.zzia.excle.progress.Progress;
import com.zzia.excle.websocket.WebsocketEndPoint;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 定时任务，发送socket消息
 * @author luo gaoYang
 * @date 2021-08-18 18:16
 **/
@Component
@EnableScheduling
public class WebsocketTask {

    public static final Map<String, Progress> CONCURRENT_HASH_MAP = new ConcurrentHashMap<>();

    /**
     * 每秒发送一次进度消息
     */
    @Scheduled(cron = "0/1 * * * * *")
    public void sendMsg() {
        Set<WebsocketEndPoint> copyOnWriteArraySet = WebsocketEndPoint.getWebSocketSet();
        if(CONCURRENT_HASH_MAP.size() > 0){
            copyOnWriteArraySet.forEach(c -> {
                // 取得的c表示该用户会话的类
                if (CONCURRENT_HASH_MAP.containsKey(c.getSessionId())) {
                    // 获取当前会话中的进度
                    Progress vo = CONCURRENT_HASH_MAP.get(c.getSessionId());
                    // 判断当前会话中导入的excel是否处理完成
                    if (vo.getTotalProgress() == Progress.COMPLETED) {
                        // 上传完成删除concurrentHashMap里的该用户信息
                        CONCURRENT_HASH_MAP.remove(c.getSessionId());
                    }
                    try {
                        //发送进度给客户端
                        c.sendMessage(JSON.toJSONString(vo));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
        }
    }
}
