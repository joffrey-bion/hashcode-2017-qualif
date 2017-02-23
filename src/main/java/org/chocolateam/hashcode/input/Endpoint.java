package org.chocolateam.hashcode.input;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class Endpoint {

    public int id;

    public int dcLatency;

    public int nCaches;

    public Map<Integer, Integer> cacheLatencies = new HashMap<>();

    public Map<Integer, Integer> gainPerCache = new HashMap<>();

    public Map<Integer, Long> nRequestsPerVideo = new HashMap<>();

    public void setLatencies(Latency[] latencies) {
        Arrays.stream(latencies).forEach(l -> cacheLatencies.put(l.cacheIndex, l.latency));
        Arrays.stream(latencies).forEach(l -> gainPerCache.put(l.cacheIndex, dcLatency - l.latency));
    }

}
