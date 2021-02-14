package com.wxmlabs.gradle.utils;

import java.io.File;
import java.math.BigInteger;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.MessageDigest;

class VersionDistribution {
    private static final String gradleDistributionsBaseUri = "https://services.gradle.org/distributions";
    private final Version version;

    public VersionDistribution(Version version) {
        this.version = version;
    }

    private String classifier = "bin"; // must be bin or all

    VersionDistribution setClassifier(String classifier) {
        this.classifier = classifier;
        return this;
    }

    public File localZip() {
        String filePath = String.join(File.separator, getGradleUserHome(), "wrapper", "dists", name(), distUriHash(remoteUri().toString()), fileName());
        return new File(filePath);
    }

    public URI remoteUri() {
        try {
            return safeUri(new URI(String.join("/", gradleDistributionsBaseUri, fileName())));
        } catch (URISyntaxException e) {
            throw new RuntimeException("Failed to parse URI", e);
        }
    }

    File markerFile() {
        return new File(localZip().getParentFile(), fileName() + ".ok");
    }

    public File gradleHome() {
        return new File(localZip().getParentFile(), gradleName());
    }

    File currentVersionSymbolicLink() {
        String filePath = String.join(File.separator, getGradleUserHome(), "wrapper", "dists", "current");
        return new File(filePath);
    }

    @Override
    public String toString() {
        return name();
    }

    String gradleName() {
        return String.format("gradle-%s", version);
    }

    String name() {
        return String.format("%s-%s", gradleName(), classifier);
    }

    String fileName() {
        return String.format("%s.zip", name());
    }

    private static final String DEFAULT_GRADLE_USER_HOME = String.join(File.separator, System.getProperty("user.home"), ".gradle");

    public static String getGradleUserHome() {
        return System.getProperty("GRADLE_USER_HOME", DEFAULT_GRADLE_USER_HOME);
    }

    /**
     * This method computes a hash of the provided {@code string}.
     * <p>
     * The algorithm in use by this method is as follows:
     * <ol>
     *    <li>Compute the MD5 value of {@code string}.</li>
     *    <li>Truncate leading zeros (i.e., treat the MD5 value as a number).</li>
     *    <li>Convert to base 36 (the characters {@code 0-9a-z}).</li>
     * </ol>
     */
    private static String distUriHash(String string) {
        try {
            MessageDigest messageDigest = MessageDigest.getInstance("MD5");
            byte[] bytes = string.getBytes();
            messageDigest.update(bytes);
            return new BigInteger(1, messageDigest.digest()).toString(36);
        } catch (Exception e) {
            throw new RuntimeException("Could not hash input string.", e);
        }
    }

    /**
     * Create a safe URI from the given one by stripping out user info.
     *
     * @param uri Original URI
     * @return a new URI with no user info
     */
    static URI safeUri(URI uri) {
        try {
            return new URI(uri.getScheme(), null, uri.getHost(), uri.getPort(), uri.getPath(), uri.getQuery(), uri.getFragment());
        } catch (URISyntaxException e) {
            throw new RuntimeException("Failed to parse URI", e);
        }
    }
}
