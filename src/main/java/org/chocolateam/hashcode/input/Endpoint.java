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

    public Map<Integer, Long> nRequestsPerVideo = new HashMap<>();

    public void setLatencies(Latency[] latencies) {
        Arrays.stream(latencies).forEach(l -> cacheLatencies.put(l.cacheIndex, l.latency));
        Arrays.stream(latencies).forEach(l -> gainPerCache.put(l.cacheIndex, dcLatency - l.latency));
    }

    public void addRequests(int videoId, int nbRequests) {
        nRequestsPerVideo.putIfAbsent(videoId, 0L);
        nRequestsPerVideo.compute(videoId, (c, val) -> val + nbRequests);
    }
}
