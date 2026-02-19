package org.javamaster.httpclient;

import consulo.platform.base.icon.PlatformIconGroup;
import consulo.httpClient.icon.HttpClientIconGroup;
import consulo.ui.image.Image;

/**
 * @author yudong
 */
public class HttpIcons {

    public static final Image FILE = HttpClientIconGroup.http();
    public static final Image GET = HttpClientIconGroup.get();
    public static final Image POST = HttpClientIconGroup.post();
    public static final Image PUT = HttpClientIconGroup.put();
    public static final Image DELETE = HttpClientIconGroup.delete();
    public static final Image IMAGE = HttpClientIconGroup.image();
    public static final Image DUBBO = HttpClientIconGroup.dubbo();
    public static final Image WS = HttpClientIconGroup.ws();

    public static final Image RUN_ALL = HttpClientIconGroup.runall();
    public static final Image STOP = HttpClientIconGroup.stop();

    public static final Image SCROLL_UP = HttpClientIconGroup.scrollup();
    public static final Image SCROLL_DOWN = HttpClientIconGroup.scrolldown();

    public static final Image COPY = PlatformIconGroup.actionsCopy();
    public static final Image HISTORY = PlatformIconGroup.vcsHistory();
    public static final Image INSPECTIONS_EYE = PlatformIconGroup.generalInspectionseye();
    public static final Image BLANK = Image.empty(Image.DEFAULT_ICON_SIZE);
    public static final Image REQUEST_MAPPING = HttpClientIconGroup.requestmapping();
}
