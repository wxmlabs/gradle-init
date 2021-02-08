package com.wxmlabs.gradle.utils;

import com.fasterxml.jackson.core.JsonFactory;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URI;
import java.util.Collection;
import java.util.concurrent.ForkJoinPool;

import static com.wxmlabs.gradle.utils.WrapperUtil.createSymbolicLink;
import static com.wxmlabs.gradle.utils.WrapperUtil.download;
import static com.wxmlabs.gradle.utils.WrapperUtil.getCurrentVersion;

/**
 * 解决wrapper反复下载不同版本的gradle造成的困扰。
 * 仅下载current版本，其他版本向current版本做软链接。
 */
public class GradleInit {
    private Download download;
    private Logger logger;
    private JsonFactory jsonFactory;

    public static void main(String[] args) {
        GradleInit init = new GradleInit();
        init.init();
    }

    GradleInit() {
        logger = new Logger(false);
        download = new Download(logger, "gradlew", Download.UNKNOWN_VERSION);
        jsonFactory = new JsonFactory();
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
            VersionDistribution currentB = currentDistributions.bin();
            if (!WrapperUtil.isOk(currentA)) {
                download(download, currentA);
                WrapperUtil.unzip(currentA);
                WrapperUtil.markOk(currentA);
            }
            if (!WrapperUtil.isOk(currentB)) {
                try {
                    createSymbolicLink(currentB, currentA);
                    WrapperUtil.markOk(currentB);
                } catch (IOException e) {
                    e.printStackTrace();
                }
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
                                WrapperUtil.createSymbolicLink(distribution, currentA);
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

