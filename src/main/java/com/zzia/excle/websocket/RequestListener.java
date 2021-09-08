package com.zzia.excle.websocket;

import org.springframework.stereotype.Component;

import javax.servlet.ServletRequestEvent;
import javax.servlet.ServletRequestListener;
import javax.servlet.http.HttpServletRequest;

/**
 * @author luo gaoYang
 * @date 2021-08-24 12:18
 **/
@Component
public class RequestListener implements ServletRequestListener {
    @Override
    public void requestDestroyed(ServletRequestEvent sre) {

    }

    public RequestListener() {

    }

    @Override
    public void requestInitialized(ServletRequestEvent sre) {
        // 将所有request请求都携带上httpSession
        ((HttpServletRequest) sre.getServletRequest()).getSession();
    }

}
