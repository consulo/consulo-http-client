package org.javamaster.httpclient.impl.ws;

import consulo.disposer.Disposable;
import consulo.application.Application;
import consulo.disposer.Disposer;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.javamaster.httpclient.impl.dashboard.HttpProcessHandler;
import org.javamaster.httpclient.model.ParamEnum;
import org.javamaster.httpclient.map.LinkedMultiValueMap;
import org.javamaster.httpclient.NlsBundle;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.WebSocket;
import java.nio.ByteBuffer;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.function.Consumer;

/**
 * Support WebSocket request
 *
 * @author yudong
 */
public class WsRequest implements Disposable {
    private final String url;
    private final LinkedMultiValueMap<String, String> reqHeaderMap;
    private final HttpProcessHandler httpProcessHandler;
    private final Map<String, String> paramMap;
    private WebSocket webSocket;
    private Consumer<String> resConsumer;

    public WsRequest(
            String url,
            LinkedMultiValueMap<String, String> reqHeaderMap,
            HttpProcessHandler httpProcessHandler,
            Map<String, String> paramMap,
            Disposable parentDisposable
    ) {
        this.url = url;
        this.reqHeaderMap = reqHeaderMap;
        this.httpProcessHandler = httpProcessHandler;
        this.paramMap = paramMap;

        Disposer.register(parentDisposable, this);
    }

    public void setResConsumer(Consumer<String> resConsumer) {
        this.resConsumer = resConsumer;
    }

    public void connect() {
        returnResMsg(NlsBundle.message("connecting") + " " + url + "\n");

        URI uri = URI.create(url);

        String connectTimeoutStr = paramMap.get(ParamEnum.CONNECT_TIMEOUT_NAME.getParam());
        long connectTimeout = connectTimeoutStr != null ? Long.parseLong(connectTimeoutStr) : 6;

        HttpClient client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(connectTimeout))
                .build();

        WebSocket.Builder builder = client.newWebSocketBuilder();
        for (Map.Entry<String, List<String>> entry : reqHeaderMap.entrySet()) {
            for (String value : entry.getValue()) {
                builder.header(entry.getKey(), value);
            }
        }

        WsListener listener = new WsListener(this, httpProcessHandler);

        builder.buildAsync(uri, listener)
                .whenComplete((ws, ex) -> {
                    if (ex == null) {
                        webSocket = ws;
                        return;
                    }

                    httpProcessHandler.hasError = true;
                    httpProcessHandler.destroyProcess();

                    returnResMsg(NlsBundle.message("connected.failed") + ExceptionUtils.getStackTrace(ex) + "\n");
                });
    }

    public void abortConnect() {
        if (webSocket == null) {
            return;
        }

        webSocket.abort();
        returnResMsg(NlsBundle.message("ws.disconnected") + "\n");
    }

    public void sendWsMsg(String msg) {
        if (webSocket != null) {
            webSocket.sendText(msg, true)
                    .whenComplete((unused, u) -> {
                        if (u == null) {
                            returnResMsg("↑↑↑ " + NlsBundle.message("succeed") + ":" + msg + "\n");
                        } else {
                            returnResMsg("↑↑↑ " + NlsBundle.message("failed") + ":" + u.getMessage() + "\n");
                        }
                    });
        }
    }

    public void returnResMsg(String msg) {
        Application.get().invokeLater(() ->
                Application.get().runWriteAction(() ->
                        resConsumer.accept(msg)
                )
        );
    }

    @Override
    public void dispose() {
        abortConnect();
    }
}

class WsListener implements WebSocket.Listener {
    private final WsRequest wsRequest;
    private final HttpProcessHandler httpProcessHandler;

    WsListener(WsRequest wsRequest, HttpProcessHandler httpProcessHandler) {
        this.wsRequest = wsRequest;
        this.httpProcessHandler = httpProcessHandler;
    }

    @Override
    public CompletionStage<?> onText(WebSocket webSocket, CharSequence data, boolean last) {
        if (webSocket != null) {
            webSocket.request(1);
        }

        wsRequest.returnResMsg("↓↓↓ " + NlsBundle.message("text.data") + ":" + data + "\n");
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public CompletionStage<?> onBinary(WebSocket webSocket, ByteBuffer data, boolean last) {
        if (webSocket != null) {
            webSocket.request(1);
        }

        wsRequest.returnResMsg("↓↓↓ " + NlsBundle.message("binary.data") + ":" + data + "\n");
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public void onOpen(WebSocket webSocket) {
        if (webSocket != null) {
            webSocket.request(1);
        }

        wsRequest.returnResMsg(NlsBundle.message("connect.succeed") + "\n");
    }

    @Override
    public CompletionStage<?> onClose(WebSocket webSocket, int statusCode, String reason) {
        httpProcessHandler.destroyProcess();

        wsRequest.returnResMsg(NlsBundle.message("ws.closed") + ",statusCode: " + statusCode + ", reason: " + reason + "\n");
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public void onError(WebSocket webSocket, Throwable error) {
        httpProcessHandler.destroyProcess();

        wsRequest.returnResMsg(NlsBundle.message("ws.failed") + ", " + error + "\n");
    }
}
