package org.chocolateam.hashcode.output;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.chocolateam.hashcode.model.Cache;

public class Solution {

    private final Cache[] caches;

    public Solution(Cache[] caches) {
        this.caches = caches;
    }

    public List<String> outputLines() {
        System.out.println("Printing solution...");
        List<String> lines = new ArrayList<>(caches.length + 1);
        lines.add(String.valueOf(caches.length));
        Arrays.stream(caches).map(Solution::createCacheLine).forEach(lines::add);
        return lines;
    }

    private static String createCacheLine(Cache cache) {
        return String.valueOf(cache.id) + " " + cache.getStoredVideos().stream()
                .map(v -> v.id)
                .map(String::valueOf)
                .collect(Collectors.joining(" "));
    }

}
