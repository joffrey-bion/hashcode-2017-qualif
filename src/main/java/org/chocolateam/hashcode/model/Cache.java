package org.chocolateam.hashcode.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.chocolateam.hashcode.Queue;
import org.chocolateam.hashcode.input.Endpoint;

public class Cache {

    public final int id;

    public int remainingCapacity;

    public Set<Endpoint> endpoints = new HashSet<>();

    public Map<Integer, Long> gainPerVideo = new HashMap<>();

    public List<Video> storedVideos = new ArrayList<>();

    public Queue<Video> rankedVideos = new Queue<>();

    public Cache(int id, int initialCapacity) {
        this.id = id;
        this.remainingCapacity = initialCapacity;
    }

    public double getCurrentCacheUsage(int totalCapacity) {
        return (double)100 * (totalCapacity - remainingCapacity) / totalCapacity;
    }

    public int getNbVideosInCache() {
        return storedVideos.size();
    }

    public void addRankedVideos(int[] videoSizes) {
        for (Entry<Integer, Long> gainForVideo : gainPerVideo.entrySet()) {
            int videoId = gainForVideo.getKey();
            long totalGainForVideo = gainForVideo.getValue();
            double rank = (double)totalGainForVideo / videoSizes[videoId];
            rankedVideos.add(new Video(videoId, rank));
        }
        rankedVideos.sort();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        Cache cache = (Cache)o;

        return id == cache.id;
    }

    @Override
    public int hashCode() {
        return id;
    }

    @Override
    public String toString() {
        return String.valueOf(id);
    }
}
