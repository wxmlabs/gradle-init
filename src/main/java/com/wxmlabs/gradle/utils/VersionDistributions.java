package com.wxmlabs.gradle.utils;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Function;

class VersionDistributions extends HashMap<String, VersionDistribution> {

    public VersionDistributions(Version version) {
        super(2);
        super.put("all", new VersionDistribution(version).setClassifier("all"));
        super.put("bin", new VersionDistribution(version).setClassifier("bin"));
    }

    public VersionDistribution all() {
        return get("all");
    }

    public VersionDistribution bin() {
        return get("bin");
    }

    public VersionDistribution put(String key, VersionDistribution value) {
        throw new UnsupportedOperationException();
    }

    public void putAll(Map<? extends String, ? extends VersionDistribution> m) {
        throw new UnsupportedOperationException();
    }

    public VersionDistribution remove(Object key) {
        throw new UnsupportedOperationException();
    }

    public void clear() {
        throw new UnsupportedOperationException();
    }

    public Set<String> keySet() {
        return Collections.unmodifiableSet(super.keySet());
    }

    public Collection<VersionDistribution> values() {
        return Collections.unmodifiableCollection(super.values());
    }

    public Set<Entry<String, VersionDistribution>> entrySet() {
        return Collections.unmodifiableSet(super.entrySet());
    }

    public VersionDistribution putIfAbsent(String key, VersionDistribution value) {
        throw new UnsupportedOperationException();
    }

    public boolean remove(Object key, Object value) {
        throw new UnsupportedOperationException();
    }

    public boolean replace(String key, VersionDistribution oldValue, VersionDistribution newValue) {
        throw new UnsupportedOperationException();
    }

    public VersionDistribution replace(String key, VersionDistribution value) {
        throw new UnsupportedOperationException();
    }

    public VersionDistribution computeIfAbsent(String key, Function<? super String, ? extends VersionDistribution> mappingFunction) {
        throw new UnsupportedOperationException();
    }

    public VersionDistribution computeIfPresent(String key, BiFunction<? super String, ? super VersionDistribution, ? extends VersionDistribution> remappingFunction) {
        throw new UnsupportedOperationException();
    }

    public VersionDistribution compute(String key, BiFunction<? super String, ? super VersionDistribution, ? extends VersionDistribution> remappingFunction) {
        throw new UnsupportedOperationException();
    }

    public VersionDistribution merge(String key, VersionDistribution value, BiFunction<? super VersionDistribution, ? super VersionDistribution, ? extends VersionDistribution> remappingFunction) {
        throw new UnsupportedOperationException();
    }

    public void replaceAll(BiFunction<? super String, ? super VersionDistribution, ? extends VersionDistribution> function) {
        throw new UnsupportedOperationException();
    }
}
