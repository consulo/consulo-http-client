package consulo.httpClient.execution;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

/**
 * Reads files referenced from .http files (request bodies, {@code $readString}, imported scripts).
 * The IDE implementation prefers unsaved editor content, the default one reads from disk.
 *
 * @author VISTALL
 * @since 2026-11-03
 */
public interface HttpFileContentReader {
    HttpFileContentReader DISK = new HttpFileContentReader() {
        @Override
        public byte[] readBytes(File file) throws IOException {
            checkFile(file);
            return Files.readAllBytes(file.toPath());
        }

        @Override
        public String readText(File file) throws IOException {
            checkFile(file);
            return Files.readString(file.toPath(), StandardCharsets.UTF_8);
        }
    };

    byte[] readBytes(File file) throws IOException;

    String readText(File file) throws IOException;

    static void checkFile(File file) throws IOException {
        if (!file.exists()) {
            throw new FileNotFoundException(file.toPath().normalize().toAbsolutePath().toString());
        }
        if (file.isDirectory()) {
            throw new IllegalArgumentException("Not a file: " + file.toPath().normalize().toAbsolutePath());
        }
    }
}
