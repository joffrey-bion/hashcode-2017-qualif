package org.chocolateam.hashcode.model;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.chocolateam.hashcode.input.Endpoint;
import org.chocolateam.hashcode.util.PriorityQueue;

public class Cache {

    private static final Comparator<ScoredVideo> comp = Comparator.comparingDouble(v -> v.score);

    public final int id;

    public int remainingCapacity;

    public Set<Endpoint> endpoints = new HashSet<>();

    private Map<Video, Long> gainPerVideo = new HashMap<>();

    private List<Video> storedVideos = new ArrayList<>();

    public PriorityQueue<ScoredVideo> scoredVideos;

    private int totalCandidateVideosSize;

    public Cache(int id, int initialCapacity) {
        this.id = id;
        this.remainingCapacity = initialCapacity;
        this.scoredVideos = new PriorityQueue<>(comp.reversed());
        this.totalCandidateVideosSize = 0;
    }

    public double getCurrentCacheUsage(int totalCapacity) {
        return (double)100 * (totalCapacity - remainingCapacity) / totalCapacity;
    }

    public int getNbVideosInCache() {
        return storedVideos.size();
    }

    public void addGainForVideo(Video video, long savedMillis) {
        gainPerVideo.putIfAbsent(video, 0L);
        gainPerVideo.merge(video, savedMillis, (old, add) -> old + add);
    }

    public void scoreVideos() {
        for (Entry<Video, Long> gainForVideo : gainPerVideo.entrySet()) {
            Video video = gainForVideo.getKey();
            long totalGainForVideo = gainForVideo.getValue();
            double rank = (double)totalGainForVideo / video.size;
            scoredVideos.add(new ScoredVideo(video, rank));
            totalCandidateVideosSize += video.size;
        }
    }

    public boolean allCandidatesFit() {
        return totalCandidateVideosSize <= remainingCapacity;
    }

    public ScoredVideo pollBestCandidate() {
        return scoredVideos.poll();
    }

    public ScoredVideo peekBestCandidate() {
        return scoredVideos.peek();
    }

    public boolean isQueueEmpty() {
        return scoredVideos.isEmpty();
    }

    public boolean canHold(ScoredVideo video) {
        return video.video.size <= remainingCapacity;
    }

    public void store(Video video) {
        storedVideos.add(video);
        remainingCapacity -= video.size;
    }

    public List<Video> getStoredVideos() {
        return storedVideos;
    }

    public void reduceVideoScore(Video video, double amount) {
        ScoredVideo videoInThisCache = scoredVideos.removeFirstMatch(v -> v.video.id == video.id);
        if (videoInThisCache == null) {
            return;
        }
        videoInThisCache.score -= amount;
        if (videoInThisCache.score < 0) {
            videoInThisCache.score = 0;
        }
        scoredVideos.add(videoInThisCache);
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
