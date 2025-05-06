package com.example.ws;

import android.util.Log;
import android.app.Activity;
import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;

import java.net.InetSocketAddress;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class WSServer extends WebSocketServer {
    private Set<WebSocket> connections = Collections.synchronizedSet(new HashSet<>());
    private static WSServer instance;
    private Activity activity;

    public WSServer(InetSocketAddress address, Activity activity) {
        super(address);
        setReuseAddr(true); // 允许端口复用
        this.activity = activity;
    }

    public static WSServer getInstance(int port, Activity activity) {
        Log.d("WebSocket", port + " - source-port");
        if (port <= 0) {
            port = 9999;
        }
        Log.d("WebSocket", port + " - return-port");

        if (instance == null) {
            InetSocketAddress address = new InetSocketAddress("0.0.0.0", port);
            instance = new WSServer(address, activity);
            Log.d("WebSocket", instance.connections.toString());
        }
        return instance;
    }

    @Override
    public void onOpen(WebSocket conn, ClientHandshake handshake) {
        Log.d("WebSocket", "onOpen");
        connections.add(conn);
        Log.d("WebSocket", "新连接: " + conn.getRemoteSocketAddress());
    }

    @Override
    public void onClose(WebSocket conn, int code, String reason, boolean remote) {
        connections.remove(conn);
        Log.d("WebSocket", "连接关闭: " + reason);
    }

    @Override
    public void onMessage(WebSocket conn, String message) {
        Log.d("WebSocket", "收到消息: " + message);
        if (activity instanceof MainActivity) {
            MainActivity mainActivity = (MainActivity) activity;
            mainActivity.getAndroidInterface().receiveMessageFromClient(message);
        }
    }

    @Override
    public void onError(WebSocket conn, Exception ex) {
        ex.printStackTrace();
        Log.e("WebSocket", "发生错误：" + ex.getMessage(), ex);
    }

    @Override
    public void onStart() {
        Log.d("WebSocket", "onStart");
        Log.d("WebSocket", "服务已启动，监听地址: " + getAddress());
    }

    public void broadcast(String message) {
        Log.d("WebSocket", "broadcast");
        for (WebSocket conn : connections) {
            conn.send(message);
        }
    }

    public void stop() {
        try {
            super.stop();
            instance = null;
            Log.d("WebSocket", "服务已停止");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
