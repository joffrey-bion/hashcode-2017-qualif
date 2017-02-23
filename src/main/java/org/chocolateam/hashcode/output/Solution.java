package org.chocolateam.hashcode.output;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.chocolateam.hashcode.input.Video;

public class Solution {

    private final Map<Integer, List<Video>> videosPerCache;

    public Solution(Map<Integer, List<Video>> videosPerCache) {
        this.videosPerCache = videosPerCache;
    }

    public List<String> outputLines() {
        System.out.println("Printing solution...");
        List<String> lines = new ArrayList<>(videosPerCache.size() + 1);
        lines.add(String.valueOf(videosPerCache.size()));
        for (Entry<Integer, List<Video>> rankedVideoForCache : videosPerCache.entrySet()) {
            lines.add(createCacheLine(rankedVideoForCache.getKey(), rankedVideoForCache.getValue()));
        }
        return lines;
    }

    private String createCacheLine(Integer cache, List<Video> videos) {
        return String.valueOf(cache) + " " + videos.stream()
                .map(v -> v.id)
                .map(String::valueOf)
                .collect(Collectors.joining(" "));
    }

}
