package com.wxmlabs.gradle.utils;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

class Versions extends ArrayList<Version> {
    public Versions() {
    }

    public static Versions parseJson(InputStream inputStream) throws IOException {
        JsonParser jsonParser = Version.getJsonFactory().createParser(inputStream);
        Versions versions = new Versions();
        while (jsonParser.nextToken() != JsonToken.END_ARRAY) {
            versions.add(Version.parseJson(jsonParser));
        }
        return versions;
    }
}
