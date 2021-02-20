package com.wxmlabs.gradle.utils;

import com.fasterxml.jackson.core.JsonFactory;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.util.Collection;
import java.util.Locale;
import java.util.concurrent.ForkJoinPool;

import static com.wxmlabs.gradle.utils.WrapperUtil.download;
import static com.wxmlabs.gradle.utils.WrapperUtil.getCurrentVersion;

/**
 * 解决wrapper反复下载不同版本的gradle造成的困扰。
 * 仅下载current版本，其他版本向current版本做软链接。
 */
public class GradleInit {
    private final Download download;
    private final Logger logger;

    public static void main(String[] args) {
        System.setProperty("user.language", "en");
        Locale.setDefault(Locale.ENGLISH);
        GradleInit init = new GradleInit();
        init.init();
    }

    GradleInit() {
        logger = new Logger(false);
        download = new Download(logger, "gradlew", Download.UNKNOWN_VERSION);
        JsonFactory jsonFactory = new JsonFactory();
    }

    private void init() {
        // https://services.gradle.org/versions/all 读取最新版本列表
        // 通过gradle版本列表构建目录结构，若存在gradle-xxx.ok文件则跳过该目录
        // /
        // |-/gradle-d.d[.d]-<bin|all>
        // | |-/{distribution-hash}
        // |   |-/gradle-d.d[.d]-<bin|all> --> ../../gradle-d[.d]-current/
        // |   |-gradle-d.d[.d]-<bin|all>.ok
        // |-/gradle-d.d-current
        //   |-{gradle-content}
        // current为兼容版本的最新版本
        try {
            // download current version
            Version currentVersion = getCurrentVersion(download);
            VersionDistributions currentDistributions = new VersionDistributions(currentVersion);
            VersionDistribution currentA = currentDistributions.all();
            if (!WrapperUtil.isOk(currentA)) {
                download(download, currentA);
                Files.deleteIfExists(currentA.gradleHome().toPath());
                WrapperUtil.unzip(currentA);
                WrapperUtil.markOk(currentA);
                WrapperUtil.makeCurrentSymbolicLink(currentA);
            }

            // create symbolic link for other versions
            byte[] allVersionsContent = download.download(URI.create("https://services.gradle.org/versions/all"));
            Versions allVersions = Versions.parseJson(new ByteArrayInputStream(allVersionsContent));
            ForkJoinPool forkJoinPool = null;
            try {
                forkJoinPool = new ForkJoinPool(3);
                allVersions.stream().parallel()
                        .filter(Version::isRelease)
                        .map(VersionDistributions::new)
                        .map(VersionDistributions::values)
                        .flatMap(Collection::stream)
                        .filter(distribution -> !WrapperUtil.isOk(distribution))
                        .forEach(distribution -> {
                            try {
                                Files.deleteIfExists(distribution.gradleHome().toPath());
                                WrapperUtil.createSymbolicLinkToCurrent(distribution);
                                WrapperUtil.markOk(distribution);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        });
            } finally {
                if (forkJoinPool != null) {
                    forkJoinPool.shutdown();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

