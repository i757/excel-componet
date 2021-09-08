package com.zzia.excle.websocket;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpSession;
import javax.websocket.*;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * @author luo gaoYang
 * @date 2021-07-02 11:03
 **/
@Slf4j
@Component
@ServerEndpoint(value = "/excel/websocket",configurator = GetHttpSessionConfigurator.class)
public class WebsocketEndPoint {

    /**
     * 用来存放每个客户端对应的WebsocketEndPoint对象
     */
    private static final Set<WebsocketEndPoint> WEB_SOCKET_SET = new CopyOnWriteArraySet<>();

    private Session session;

    private String sessionId;

    @OnOpen
    public void onOpen(Session session,EndpointConfig config){
        this.session = session;
        HttpSession httpSession= (HttpSession) config.getUserProperties().get(HttpSession.class.getName());
        WEB_SOCKET_SET.add(this);
        try {
            sendMessage("连接成功");
        } catch (IOException e) {
            e.printStackTrace();
            log.error("websocket IO 异常");
        }
        sessionId = httpSession.getId();
    }

    @OnClose
    public void onClose(){
        WEB_SOCKET_SET.remove(this);
    }

    @OnMessage
    public void onMessage(String message,Session session){
    }

    @OnError
    public void onError(Session session, Throwable error) {
        log.error("发生错误");
        error.printStackTrace();
    }

    public void sendMessage(String message) throws IOException {
        session.getBasicRemote().sendText(message);
    }

    public void sendMessage(Object object) throws IOException, EncodeException {
        session.getBasicRemote().sendObject(object);
    }

    public static Set<WebsocketEndPoint> getWebSocketSet() {
        return WEB_SOCKET_SET;
    }

    public Session getSession() {
        return session;
    }

    public void setSession(Session session) {
        this.session = session;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public String getSessionId() {
        return sessionId;
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return super.equals(obj);
    }
}
