package org.javamaster.httpclient.impl.mock;

import com.google.common.net.HttpHeaders;
import consulo.application.Application;
import consulo.util.lang.Pair;
import consulo.util.lang.Formats;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.intellij.markdown.html.UrlEncodeKt;
import org.javamaster.httpclient.model.ParamEnum;
import org.javamaster.httpclient.map.LinkedMultiValueMap;
import org.javamaster.httpclient.NlsBundle;
import org.javamaster.httpclient.psi.HttpHeaderField;
import org.javamaster.httpclient.psi.HttpPsiUtils;
import org.javamaster.httpclient.psi.HttpRequest;
import org.javamaster.httpclient.psi.HttpRequestTarget;
import org.javamaster.httpclient.psi.HttpTypes;
import org.javamaster.httpclient.psi.impl.HttpPathAbsoluteImpl;
import org.javamaster.httpclient.psi.impl.HttpPortImpl;
import org.javamaster.httpclient.impl.resolve.VariableResolver;
import org.javamaster.httpclient.impl.utils.HttpUtils;

import javax.activation.MimetypesFileTypeMap;
import java.io.*;
import java.net.ServerSocket;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

import static org.javamaster.httpclient.impl.utils.HttpUtils.CR_LF;

/**
 * @author yudong
 */
public class MockServer {
    private static final MimetypesFileTypeMap mimetypesFileTypeMap = new MimetypesFileTypeMap();

    private Consumer<String> resConsumer;

    public void setResConsumer(Consumer<String> resConsumer) {
        this.resConsumer = resConsumer;
    }

    public ServerSocket startServerAsync(
            HttpRequest request,
            VariableResolver variableResolver,
            Map<String, String> paramMap
    ) {
        ServerSocket serverSocket;
        try {
            serverSocket = new ServerSocket(resolvePort(request));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        String path = resolvePath(request, variableResolver);

        File staticFolder = checkStaticFolder(paramMap.get(ParamEnum.STATIC_FOLDER.getParam()));

        resConsumer.accept(appendTime(NlsBundle.message("mock.server.start", serverSocket.getLocalPort()) + "\n"));

        CompletableFuture.supplyAsync(() -> {
            while (true) {
                try (var socket = serverSocket.accept();
                     var inputStream = socket.getInputStream();
                     var outputStream = socket.getOutputStream()) {

                    resConsumer.accept(appendTime(NlsBundle.message("mock.server.receive", socket.getPort()) + "\n"));

                    String reqStr = readAsString(inputStream);
                    if (!reqStr.isEmpty()) {
                        resConsumer.accept(reqStr.replace(CR_LF, "\n") + "\n");

                        String reqPath = URLDecoder.decode(
                                reqStr.split(CR_LF)[0].split(" ")[1],
                                StandardCharsets.UTF_8.toString()
                        );

                        if (staticFolder != null) {
                            if (reqPath.startsWith(path)) {
                                String resolvePath = reqPath.substring(path.length());
                                File file = new File(staticFolder, resolvePath);
                                if (file.isDirectory()) {
                                    String res = constructFileListResponse(file, reqPath);
                                    writeStrResAndLog(res, outputStream);
                                } else {
                                    if (file.exists()) {
                                        writeFileResponse(file, outputStream);
                                        resConsumer.accept("Write file to client: " + file + "\n");
                                        resConsumer.accept("-----------------------------\n");
                                    } else {
                                        String resStr = construct404Response(reqPath);
                                        writeStrResAndLog(resStr, outputStream);
                                    }
                                }
                            } else {
                                String resStr = construct404Response(reqPath);
                                writeStrResAndLog(resStr, outputStream);
                            }
                        } else {
                            String resStr;
                            if (reqPath.equals(path)) {
                                Pair<Object, LinkedMultiValueMap<String, String>> resBody = computeResInfo(request, variableResolver, paramMap);
                                resStr = constructResponse(resBody, paramMap);
                            } else {
                                resStr = construct404Response(reqPath);
                            }

                            writeStrResAndLog(resStr, outputStream);
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).exceptionally(ex -> {
            ex.printStackTrace();
            resConsumer.accept(appendTime(NlsBundle.message("mock.server.error", ex) + "\n"));
            return null;
        });

        return serverSocket;
    }

    private void writeStrResAndLog(String resStr, OutputStream outputStream) throws IOException {
        outputStream.write(resStr.getBytes(StandardCharsets.UTF_8));

        resConsumer.accept(appendTime(NlsBundle.message("mock.server.res") + "\n"));
        resConsumer.accept(resStr.replace(CR_LF, "\n") + "\n");
        resConsumer.accept("-----------------------------\n");
    }

    private String readAsString(InputStream inputStream) throws IOException {
        InputStreamReader reader = new InputStreamReader(inputStream, StandardCharsets.UTF_8);
        char[] buffer = new char[819200000];

        int bytesRead = reader.read(buffer);
        if (bytesRead == -1) {
            return "";
        }

        StringBuilder out = new StringBuilder();
        out.append(buffer, 0, bytesRead);

        return out.toString();
    }

    private Pair<Object, LinkedMultiValueMap<String, String>> computeResInfo(
            HttpRequest request,
            VariableResolver variableResolver,
            Map<String, String> paramMap
    ) {
        return Application.get().runReadAction(() -> {
            Object reqBody = HttpUtils.convertToReqBody(request, variableResolver, paramMap);

            List<HttpHeaderField> httpHeaderFields = request.getHeader() != null ?
                    request.getHeader().getHeaderFieldList() : null;
            LinkedMultiValueMap<String, String> reqHeaderMap = HttpUtils.convertToReqHeaderMap(httpHeaderFields, variableResolver);

            return new Pair<>(reqBody, reqHeaderMap);
        });
    }

    private String constructResponse(
            Pair<Object, LinkedMultiValueMap<String, String>> pair,
            Map<String, String> paramMap
    ) {
        String statusCodeStr = paramMap.get(ParamEnum.RESPONSE_STATUS.getParam());
        long statusCode = statusCodeStr != null ? Long.parseLong(statusCodeStr) : 200;

        List<String> list = new ArrayList<>();
        list.add("HTTP/1.1 " + statusCode + " OK" + CR_LF);

        for (Map.Entry<String, List<String>> entry : pair.getSecond().entrySet()) {
            for (String value : entry.getValue()) {
                list.add(entry.getKey() + ": " + value + CR_LF);
            }
        }

        int length = 0;
        String bodyStr = null;

        Object resBody = pair.getFirst();
        if (resBody != null) {
            bodyStr = resBody.toString();
            length = bodyStr.getBytes(StandardCharsets.UTF_8).length;
        }

        list.add(HttpHeaders.CONTENT_LENGTH + ": " + length + CR_LF);
        list.add(CR_LF);

        if (bodyStr != null) {
            list.add(bodyStr);
        }

        return String.join("", list);
    }

    private String constructFileListResponse(File root, String reqPath) throws IOException {
        List<String> list = new ArrayList<>();
        list.add("HTTP/1.1 200 OK" + CR_LF);
        list.add(HttpHeaders.CONTENT_TYPE + ": text/html;charset=utf-8" + CR_LF);
        list.add("Date: " + new Date() + CR_LF);
        list.add("Server: ServerSocket" + CR_LF);

        String[] files = root.list();
        if (files == null) {
            files = new String[0];
        }

        StringBuilder bodyBuilder = new StringBuilder();
        for (String fileName : files) {
            File file = new File(root, fileName);
            var filePath = file.toPath();
            String linkPath = reqPath.endsWith("/") ?
                    reqPath + UrlEncodeKt.urlEncode(fileName) :
                    reqPath + "/" + UrlEncodeKt.urlEncode(fileName);

            String name;
            String size;
            if (file.isFile()) {
                name = "(File) " + fileName;
                size = Formats.formatFileSize(Files.size(filePath));
            } else {
                name = "(Dir)  " + fileName;
                size = "";
            }

            String time = DateFormatUtils.format(Files.getLastModifiedTime(filePath).toMillis(), "yyyy/MM/dd HH:mm");

            bodyBuilder.append("<tr>\n")
                    .append("    <td>\n")
                    .append("        <a href='").append(linkPath).append("'>").append(name).append("</a>\n")
                    .append("    </td>\n")
                    .append("    <td>").append(size).append("</td>\n")
                    .append("    <td>").append(time).append("</td>\n")
                    .append("</tr>\n");
        }

        String bodyStr = "<!doctype html>\n" +
                "<html lang=\"zh\">\n" +
                "<head>\n" +
                "    <title>Files</title>\n" +
                "    <style>\n" +
                "        body { font-family: Tahoma,Arial,sans-serif; }\n" +
                "        h1, h2, h3, b { color: white; background-color: #525D76; }\n" +
                "        table { width: 80%; text-align: left; }\n" +
                "        .line { height: 1px; background-color: #525D76; border: none; }\n" +
                "    </style>\n" +
                "</head>\n" +
                "<body>\n" +
                "<h1>Directory listing for " + reqPath + "</h1>\n" +
                "<hr class=\"line\"/>\n" +
                "<table>\n" +
                "    <thead>\n" +
                "    <tr>\n" +
                "        <th>Name</th>\n" +
                "        <th>Size</th>\n" +
                "        <th>Last Modified</th>\n" +
                "    </tr>\n" +
                "    </thead>\n" +
                "    <tbody>\n" +
                "        " + bodyBuilder + "\n" +
                "    </tbody>\n" +
                "</table>\n" +
                "</body>\n" +
                "</html>";

        int length = bodyStr.getBytes(StandardCharsets.UTF_8).length;

        list.add(HttpHeaders.CONTENT_LENGTH + ": " + length + CR_LF);
        list.add(CR_LF);
        list.add(bodyStr);

        return String.join("", list);
    }

    private void writeFileResponse(File file, OutputStream outputStream) throws IOException {
        String mimeType = mimetypesFileTypeMap.getContentType(file.getName());
        String filename = UrlEncodeKt.urlEncode(file.getName());

        writeStr(outputStream, "HTTP/1.1 200 OK" + CR_LF);
        writeStr(outputStream, HttpHeaders.CONTENT_TYPE + ": " + mimeType + CR_LF);
        writeStr(outputStream, HttpHeaders.CONTENT_DISPOSITION + ":name=\"attachment\"; filename=\"" + filename + "\"" + CR_LF);
        writeStr(outputStream, "Date: " + new Date() + CR_LF);
        writeStr(outputStream, "Server: ServerSocket" + CR_LF);

        byte[] bytes = Files.readAllBytes(file.toPath());
        int length = bytes.length;

        writeStr(outputStream, HttpHeaders.CONTENT_LENGTH + ": " + length + CR_LF);
        writeStr(outputStream, CR_LF);

        try {
            outputStream.write(bytes);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void writeStr(OutputStream outputStream, String str) throws IOException {
        outputStream.write(str.getBytes(StandardCharsets.UTF_8));
    }

    private String construct404Response(String reqPath) {
        List<String> list = new ArrayList<>();
        list.add("HTTP/1.1 404 Not Found" + CR_LF);
        list.add(HttpHeaders.CONTENT_TYPE + ": text/html;charset=utf-8" + CR_LF);
        list.add("Date: " + new Date() + CR_LF);
        list.add("Server: ServerSocket" + CR_LF);

        String bodyStr = "<!doctype html>\n" +
                "<html lang=\"zh\">\n" +
                "    <head>\n" +
                "        <title>HTTP status 404 - Not Found</title>\n" +
                "        <style type=\"text/css\">\n" +
                "            body { font-family: Tahoma,Arial,sans-serif; }\n" +
                "            h1, h2, h3, b { color: white; background-color: #525D76; }   \n" +
                "            .line { height: 1px; background-color: #525D76; border: none; }                                                        \n" +
                "        </style>\n" +
                "    </head>\n" +
                "    <body>\n" +
                "        <h1>HTTP status 404 - Not Found</h1>\n" +
                "        <hr class=\"line\"/>\n" +
                "        <p>\n" +
                "            <b>类型</b>\n" +
                "            状态报告\n" +
                "        </p>\n" +
                "        <p>\n" +
                "            <b>消息</b>\n" +
                "            path [" + reqPath + "] not found\n" +
                "        </p>\n" +
                "        <hr class=\"line\"/>\n" +
                "        <h3>Java ServerSocket</h3>\n" +
                "    </body>\n" +
                "</html>";

        int length = bodyStr.getBytes(StandardCharsets.UTF_8).length;

        list.add(HttpHeaders.CONTENT_LENGTH + ": " + length + CR_LF);
        list.add(CR_LF);
        list.add(bodyStr);

        return String.join("", list);
    }

    private int resolvePort(HttpRequest request) {
        HttpRequestTarget requestTarget = request.getRequestTarget();
        if (requestTarget == null) {
            return 80;
        }

        HttpPortImpl httpPort = (HttpPortImpl) requestTarget.getPort();
        if (httpPort != null) {
            var firstChild = httpPort.getFirstChild();
            var portSegment = HttpPsiUtils.getNextSiblingByType(firstChild, HttpTypes.PORT_SEGMENT, false);
            if (portSegment != null) {
                return Integer.parseInt(portSegment.getText());
            }
        }

        return 80;
    }

    private String resolvePath(
            HttpRequest request,
            VariableResolver variableResolver
    ) {
        HttpRequestTarget requestTarget = request.getRequestTarget();
        if (requestTarget == null) {
            return "/";
        }

        HttpPathAbsoluteImpl pathAbsolute = (HttpPathAbsoluteImpl) requestTarget.getPathAbsolute();
        if (pathAbsolute != null) {
            return variableResolver.resolve(pathAbsolute.getText());
        }

        return "/";
    }

    private File checkStaticFolder(String staticFolder) {
        if (staticFolder == null) {
            return null;
        }

        File file = new File(staticFolder);
        if (!file.exists()) {
            throw new RuntimeException(NlsBundle.message("folder.not.exist", file.getAbsolutePath()));
        }

        if (!file.isDirectory()) {
            throw new RuntimeException(NlsBundle.message("not.folder", file.getAbsolutePath()));
        }

        return file;
    }

    private String appendTime(String msg) {
        String time = DateFormatUtils.format(new Date(), "yyyy-MM-dd HH:mm:ss,SSS");
        return time + " - " + msg;
    }
}
