package com.wxmlabs.gradle.utils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;

public class FileUtil {
    public static void createRelativeSymbolicLink(File link, File target) throws IOException {
        Path linkPath = link.toPath();
        Path targetPath = target.toPath();
        Path relativePath = linkPath.getParent().relativize(targetPath);
        Files.deleteIfExists(linkPath);
        Files.createDirectories(linkPath.getParent());
        createSymbolicLink(linkPath, relativePath);
    }

    private static void createSymbolicLink(Path link, Path target) throws IOException {
        if (isWindows()) {
            // Files.createSymbolicLink(): Got an exception on Windows.
            // java.nio.file.FileSystemException: A required privilege is not held by the client.
            Files.createSymbolicLink(link, target);
        } else {
            Files.createSymbolicLink(link, target);
        }
    }

    private static boolean isWindows() {
        String osName = System.getProperty("os.name").toLowerCase(Locale.US);
        return osName.contains("windows");
    }
}
