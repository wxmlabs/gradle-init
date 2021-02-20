package com.wxmlabs.gradle.utils;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

import static com.wxmlabs.gradle.utils.FileUtil.createRelativeSymbolicLink;

public class WrapperUtil {
    static Version getCurrentVersion(Download download) throws Exception {
        byte[] currentVersionContent = download.download(URI.create("https://services.gradle.org/versions/current"));
        Version currentVersion = Version.parseJson(new ByteArrayInputStream(currentVersionContent));
        System.out.println("current version: " + currentVersion);
        return currentVersion;
    }

    static void download(Download download, VersionDistribution distribution) throws Exception {
        File distFile = distribution.localZip();
        if (!distFile.exists() || distFile.length() < 1) {
            Files.createDirectories(distFile.getParentFile().toPath());
            File tmpFile = File.createTempFile(distribution.gradleName() + "_", null, distFile.getParentFile());
            tmpFile.deleteOnExit();
            download.download(distribution.remoteUri(), tmpFile);
            Files.move(tmpFile.toPath(), distFile.toPath(), StandardCopyOption.ATOMIC_MOVE);
        }
    }

    static void makeCurrentSymbolicLink(VersionDistribution distribution) throws IOException {
        createRelativeSymbolicLink(distribution.currentVersionSymbolicLink(), distribution.gradleHome());
    }

    static void createSymbolicLinkToCurrent(VersionDistribution distribution) throws IOException {
        createRelativeSymbolicLink(distribution.gradleHome(), distribution.currentVersionSymbolicLink());
    }

    static void unzip(VersionDistribution distribution) throws IOException {
        File zipFile = distribution.localZip();
        ZipUtil.unzip(zipFile);
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    static boolean isOk(VersionDistribution distribution) {
        File gradleExec = Paths.get(distribution.gradleHome().getPath(), "bin", "gradle").toFile();
        return distribution.markerFile().exists() && gradleExec.exists();
    }

    static void markOk(VersionDistribution distribution) throws IOException {
        File okFile = distribution.markerFile();
        if (!okFile.exists()) {
            //noinspection ResultOfMethodCallIgnored
            okFile.createNewFile();
        }
    }
}
