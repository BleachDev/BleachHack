package org.bleachhack.util;

import org.apache.commons.io.FileUtils;
import org.bleachhack.util.io.BleachFileMang;
import org.bleachhack.util.io.BleachOnlineMang;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class DoomUtils {
    public static void downloadWads(boolean log) {
        try {
            FileUtils.copyURLToFile(
                    BleachOnlineMang.getResourceUrl().resolve("doom/wads/wads.zip").toURL(),
                    BleachFileMang.getDir().resolve("doom/wads/wads.zip").toFile());
            ZipFile zip = new ZipFile(BleachFileMang.getDir().resolve("doom/wads/wads.zip").toFile());
            Enumeration<? extends ZipEntry> files = zip.entries();
            int count = 0;
            while (files.hasMoreElements()) {
                count++;
                ZipEntry file = files.nextElement();
                Path outFile = BleachFileMang.getDir().resolve("doom/wads").resolve(file.getName());
                if (file.isDirectory()) {
                    outFile.toFile().mkdirs();
                } else {
                    outFile.toFile().getParentFile().mkdirs();

                    try (InputStream zipStream = zip.getInputStream(file)){
                        Files.copy(zipStream, outFile);
                    }
                }
            }

            zip.close();
            Files.deleteIfExists(BleachFileMang.getDir().resolve("doom/wads").resolve("wads.zip"));

            if (log)
                BleachLogger.info("Downloaded " + count + " Wads");
        } catch (Exception e) {
            if (log)
                BleachLogger.warn("Files either exist or failed to download... " + e);
            e.printStackTrace();
        }
    }
}
