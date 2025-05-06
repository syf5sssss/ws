package com.example.ws;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private WebView webView;
    private WSServer wsServer;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        webView = findViewById(R.id.webview);
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setAllowFileAccess(true); // 允许访问文件
        webSettings.setDomStorageEnabled(true); // 启用 DOM 存储
        webSettings.setAllowContentAccess(true); // 允许内容访问
        webSettings.setAllowFileAccessFromFileURLs(true); // 允许通过 file URL 访问文件
        webSettings.setAllowUniversalAccessFromFileURLs(true); // 允许通过 file URL 访问所有
        // 启用混合内容加载（如果需要加载 HTTP 内容）
        webSettings.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
        webView.addJavascriptInterface(new AndroidInterface(), "Android");
        webView.setWebViewClient(new WebViewClient());
        // 设置 WebChromeClient 以支持 JavaScript 的对话框等
        webView.setWebChromeClient(new WebChromeClient());
        if (BuildConfig.DEBUG) {
            webView.setWebContentsDebuggingEnabled(true);
        }
        webView.loadUrl("file:///android_asset/index.html");//发布使用
//        webView.loadUrl("http://10.0.2.2:5173");//虚拟机调试使用
//        webView.loadUrl("http://172.168.54.182:5173/");//真机调试使用

    }

    @Override
    public void onBackPressed() {
        // 如果 WebView 可以回退，则回退页面
        if (webView.canGoBack()) {
            webView.goBack();
        } else {
            super.onBackPressed();
        }
    }


    public AndroidInterface getAndroidInterface() {
        return new AndroidInterface();
    }

    public class AndroidInterface {
        @JavascriptInterface
        public void startWebSocketServer(int port) {
            if (wsServer == null) {
                try {
                    wsServer = WSServer.getInstance(port,MainActivity.this);
                    wsServer.start();
                    List<String> ipAddresses = NetworkUtils.getDeviceIpAddresses();
                    String ipAddressesString = String.join(", ", ipAddresses);
                    Log.d("NetworkUtils", "IP Addresses: " + ipAddressesString);
                    webView.post(() -> {
                        webView.evaluateJavascript("window.ipshow('" + ipAddressesString + "')", null);
                    });
                } catch (Exception e) {
                    // 错误处理
                }
            }
        }

        @JavascriptInterface
        public void stopWebSocketServer() {
            if (wsServer != null) {
                wsServer.stop();
                wsServer = null;
            }
        }

        @JavascriptInterface
        public void sendMessageToClients(String message) {
            if (wsServer != null) {
                wsServer.broadcast(message);
            }
        }

        @JavascriptInterface
        public void receiveMessageFromClient(String message) {
            Log.d("WebSocket", "receiveMessageFromClient: " + message);
            webView.post(() -> {
                webView.evaluateJavascript("window.receiveMessage('" + message.replace("'", "\\'") + "')", null);
            });
        }
    }

    @Override
    protected void onDestroy() {
        if (wsServer != null) {
            wsServer.stop();
        }
        super.onDestroy();
    }

}