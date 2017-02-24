package org.chocolateam.hashcode.input;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.chocolateam.hashcode.model.Video;

public class Endpoint {

    public int dcLatency;

    public int nCaches;

    public Map<Integer, Integer> cacheLatencies = new HashMap<>();

    public Map<Integer, Integer> gainPerCache = new HashMap<>();

    public Map<Video, Long> nRequestsPerVideo = new HashMap<>();

    public void setLatencies(Latency[] latencies) {
        Arrays.stream(latencies).forEach(l -> cacheLatencies.put(l.cacheIndex, l.latency));
        Arrays.stream(latencies).forEach(l -> gainPerCache.put(l.cacheIndex, dcLatency - l.latency));
    }

    public void addRequests(Video video, int nbRequests) {
        nRequestsPerVideo.putIfAbsent(video, 0L);
        nRequestsPerVideo.compute(video, (c, val) -> val + nbRequests);
    }

    public long getNbRequests(Video video) {
        return nRequestsPerVideo.getOrDefault(video, 0L);
    }
}
