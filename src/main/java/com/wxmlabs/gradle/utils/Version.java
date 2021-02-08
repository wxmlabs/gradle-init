package com.wxmlabs.gradle.utils;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

class Version {
    private String version;
    private String downloadUrl;
    private boolean release;

    public Version() {
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getDownloadUrl() {
        return downloadUrl;
    }

    public void setDownloadUrl(String downloadUrl) {
        this.downloadUrl = downloadUrl;
    }

    public boolean isRelease() {
        return release;
    }

    public void setRelease(boolean release) {
        this.release = release;
    }

    @Override
    public String toString() {
        return version;
    }

    static ArrayList<String> notReleaseBooleanCheckKey = new ArrayList<>(4);
    static ArrayList<String> notReleaseStringCheckKey = new ArrayList<>(2);

    static {
        notReleaseBooleanCheckKey.add("snapshot");
        notReleaseBooleanCheckKey.add("nightly");
        notReleaseBooleanCheckKey.add("releaseNightly");
        notReleaseBooleanCheckKey.add("activeRc");
        notReleaseStringCheckKey.add("rcFor");
        notReleaseStringCheckKey.add("milestoneFor");
    }

    private static final JsonFactory jsonFactory = new JsonFactory();

    static JsonFactory getJsonFactory() {
        return jsonFactory;
    }

    static Version parseJson(InputStream inputStream) throws IOException {
        return parseJson(getJsonFactory().createParser(inputStream));
    }

    static Version parseJson(JsonParser jsonParser) throws IOException {
        Version version = new Version();
        boolean release = true;
        while (jsonParser.nextToken() != JsonToken.END_OBJECT) {
            String name = jsonParser.getCurrentName();
            if (release) {
                boolean notRelease = false;
                if (notReleaseBooleanCheckKey.contains(name)) {
                    jsonParser.nextToken();
                    notRelease = jsonParser.getBooleanValue();
                } else if (notReleaseStringCheckKey.contains(name)) {
                    jsonParser.nextToken();
                    notRelease = !jsonParser.getText().isEmpty();
                }
                if (notRelease) {
                    release = false;
                }
            }
            if ("version".equals(name)) {
                jsonParser.nextToken();
                version.setVersion(jsonParser.getText());
                if (version.getVersion().contains("-")) {
                    release = false;
                }
            } else if ("downloadUrl".equals(name)) {
                jsonParser.nextToken();
                version.setDownloadUrl(jsonParser.getText());
            }
        }
        version.setRelease(release);
        return version;
    }
}
