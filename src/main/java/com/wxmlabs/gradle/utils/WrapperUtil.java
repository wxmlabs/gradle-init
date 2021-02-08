package com.wxmlabs.gradle.utils;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

public class WrapperUtil {
    static Version getCurrentVersion(Download download) throws Exception {
        byte[] currentVersionContent = download.download(URI.create("https://services.gradle.org/versions/current"));
        Version currentVersion = Version.parseJson(new ByteArrayInputStream(currentVersionContent));
        System.out.println("current version: " + currentVersion);
        return currentVersion;
    }

    static void download(Download download, VersionDistribution distribution) throws Exception {
        File distFile = distribution.local();
        if (!distFile.exists() || distFile.length() < 1) {
            //noinspection ResultOfMethodCallIgnored
            distFile.getParentFile().mkdirs();
            File tmpFile = File.createTempFile(distribution.gradleName() + "_", null, distFile.getParentFile());
            tmpFile.deleteOnExit();
            download.download(distribution.remote(), tmpFile);
            Files.move(tmpFile.toPath(), distFile.toPath(), StandardCopyOption.ATOMIC_MOVE);
        }
    }

    static void createSymbolicLink(VersionDistribution distribution, VersionDistribution current) throws IOException {
        File target = current.gradleHome();
        File link = distribution.gradleHome();
        if (link.exists()) {
            return;
        }
        //noinspection ResultOfMethodCallIgnored
        link.getParentFile().mkdirs();
        Files.createSymbolicLink(link.toPath(), target.toPath());
    }

    static void unzip(VersionDistribution distribution) throws IOException {
        File zipFile = distribution.local();
        File targetDir = distribution.gradleHome();
        ZipUtil.unzip(zipFile, targetDir);
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    static boolean isOk(VersionDistribution distribution) {
        return distribution.okFile().exists();
    }

    static void markOk(VersionDistribution distribution) throws IOException {
        File okFile = distribution.okFile();
        if (!okFile.exists()) {
            //noinspection ResultOfMethodCallIgnored
            okFile.createNewFile();
        }
    }
}
