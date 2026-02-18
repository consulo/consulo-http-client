package consulo.restClient;

import consulo.annotation.component.ExtensionImpl;
import consulo.virtualFileSystem.fileType.FileTypeConsumer;
import consulo.virtualFileSystem.fileType.FileTypeFactory;
import jakarta.annotation.Nonnull;
import org.javamaster.httpclient.HttpFileType;

/**
 * @author VISTALL
 * @since 2025-07-21
 */
@ExtensionImpl
public class HttpFileTypeFactory extends FileTypeFactory {
    @Override
    public void createFileTypes(@Nonnull FileTypeConsumer fileTypeConsumer) {
        fileTypeConsumer.consume(HttpFileType.INSTANCE);
    }
}
