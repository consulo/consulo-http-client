package org.javamaster.httpclient.impl.dashboard.support;

import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

/**
 * @author yudong
 */
public class TgzExtractor {

    public static void extract(String tgzFilePath, String outputDir) throws Exception {
        try (FileInputStream fis = new FileInputStream(tgzFilePath);
             GzipCompressorInputStream gzip = new GzipCompressorInputStream(fis);
             TarArchiveInputStream tarIn = new TarArchiveInputStream(gzip)) {

            TarArchiveEntry entry = tarIn.getNextTarEntry();
            while (entry != null) {
                File outputFile = new File(outputDir, entry.getName());

                if (entry.isDirectory()) {
                    outputFile.mkdirs();
                } else {
                    outputFile.getParentFile().mkdirs();
                    try (FileOutputStream fos = new FileOutputStream(outputFile)) {
                        IOUtils.copy(tarIn, fos);
                    }
                }

                entry = tarIn.getNextTarEntry();
            }
        }
    }
}
