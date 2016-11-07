package com.ccc.ws;

import java.io.IOException;
import java.util.Date;
import java.util.concurrent.CopyOnWriteArraySet;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;
import org.springframework.stereotype.Component;


/**
 * 在用
 * --- Be used 
 *  
 * */
@ServerEndpoint(value = "/lysocket")
@Component
public class LyWebSocket {

    //concurrent包的线程安全Set，用来存放每个客户端对应的MyWebSocket对象。
    private static CopyOnWriteArraySet<LyWebSocket> webSocketSet = new CopyOnWriteArraySet<LyWebSocket>();
    //与某个客户端的连接会话，需要通过它来给客户端发送数据
    private Session session;


    /**
     * 连接建立成功调用的方法*/
    @OnOpen
    public void onOpen(Session session) {
    	System.out.println("--- LyWebSocket --- onOpen() ----");
        String  message1="--- LyWebSocket --- onOpen() ---- 向前台 传送的消息:"+new Date();
        this.session = session;
        webSocketSet.add(this);//加入set中        
        try {
        	//session.getBasicRemote().sendText(message1);
        	sendMessage(message1);
            //sendInfo(message1);
        } catch (IOException e) {
            System.out.println("IO异常");
        }
    }

    /**
     * 连接关闭调用的方法
     */
    @OnClose
    public void onClose() {
        webSocketSet.remove(this);  //从set中删除
        //subOnlineCount();           //在线数减1        
        System.out.println("--- LyWebSocket --- onClose() ----");
    }

    /**
     * 收到客户端消息后调用的方法
     *
     * @param message 客户端发送过来的消息
     * @throws IOException */
    @OnMessage
    public void onMessage(String message, Session session) throws IOException {
        System.out.println("来自客户端的消息:" + message);

        //群发消息
        for (LyWebSocket item : webSocketSet) {
            try {
            	item.sendMessage(message);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        
        //一对一发送消息        
        session.getBasicRemote().sendText("一对一消息: ---send到 ："+session.getId()+" ;");
    }

    /**
     * 发生错误时调用
     * */
    @OnError
    public void onError(Session session, Throwable error) {
        System.out.println("--- LyWebSocket --- onError() ---- 系统发生错误");
        error.printStackTrace();
    }


    public void sendMessage(String message) throws IOException {
        this.session.getBasicRemote().sendText(message);
        //this.session.getAsyncRemote().sendText(message);
    }


    /**
     * 群发自定义消息
     * */
    public static void sendInfo(String message) throws IOException {
        for (LyWebSocket item : webSocketSet) {
            try {
                //item.sendMessage(message);
                item.sendInfo(message);
            } catch (IOException e) {
                continue;
            }
        }
    }


}

