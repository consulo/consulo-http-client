package org.javamaster.httpclient.model;

import consulo.virtualFileSystem.VirtualFile;
import org.javamaster.httpclient.psi.HttpDirectionComment;

import java.io.File;
import java.net.URL;

public class PreJsFile {
    private final HttpDirectionComment directionComment;
    private final URL url;
    private final File urlFile;

    private File file;
    private VirtualFile virtualFile;
    private String content;

    public PreJsFile(HttpDirectionComment directionComment, URL url) {
        this.directionComment = directionComment;
        this.url = url;
        this.urlFile = url != null ? new File(url.toString()) : null;
    }

    public HttpDirectionComment getDirectionComment() {
        return directionComment;
    }

    public URL getUrl() {
        return url;
    }

    public File getUrlFile() {
        return urlFile;
    }

    public File getFile() {
        return file;
    }

    public void setFile(File file) {
        this.file = file;
    }

    public VirtualFile getVirtualFile() {
        return virtualFile;
    }

    public void setVirtualFile(VirtualFile virtualFile) {
        this.virtualFile = virtualFile;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
