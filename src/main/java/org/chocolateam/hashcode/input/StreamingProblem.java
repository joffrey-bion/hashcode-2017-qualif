package org.chocolateam.hashcode.input;

import java.util.Map.Entry;

import org.chocolateam.hashcode.Queue;
import org.chocolateam.hashcode.model.Cache;
import org.chocolateam.hashcode.model.Video;
import org.chocolateam.hashcode.output.Solution;
import org.jetbrains.annotations.Nullable;

public class StreamingProblem {

    public int nVideos;

    public int nEndpoints;

    public int nRequestDescriptions;

    public int nCaches;

    public int cacheSize;

    public int[] videoSizes;

    public Endpoint[] endpoints;

    public RequestDesc[] requestDescs;

    public Cache[] caches;

    public Solution solve() {
        createCaches();
        computeInitialGains();
        rankVideos();

        while (findAndInsertMax()) {
        }

        return new Solution(caches);
    }

    private void createCaches() {
        caches = new Cache[nCaches];
        for (int i = 0; i < nCaches; i++) {
            caches[i] = new Cache(i, cacheSize);
        }
    }

    private void computeInitialGains() {
        System.out.println("Computing gain for each cache and video...");
        for (RequestDesc rd : requestDescs) {
            addGain(rd);
        }
    }

    private void addGain(RequestDesc requestDesc) {
        Endpoint endpoint = endpoints[requestDesc.endpointId];

        // for graph
        int videoId = requestDesc.videoId;
        endpoint.addRequests(videoId, requestDesc.count);

        for (Entry<Integer, Integer> gainForCache : endpoint.gainPerCache.entrySet()) {
            int cacheId = gainForCache.getKey();

            // for graph
            Cache cache = caches[cacheId];
            cache.endpoints.add(endpoint);

            int gainPerRequest = gainForCache.getValue();
            int totalGain = requestDesc.count * gainPerRequest;
            cache.gainPerVideo.putIfAbsent(videoId, 0L);
            cache.gainPerVideo.compute(videoId, (c, val) -> val + totalGain);
        }
    }

    private void rankVideos() {
        System.out.println("Computing rank for each video in each cache...");
        for (Cache cache : caches) {
            System.out.println("Computing rank for each video in cache " + cache);
            cache.addRankedVideos(videoSizes);
        }
    }

    private boolean findAndInsertMax() {
        Cache maxCache = getCacheWithBestVideo();
        if (maxCache == null) {
            System.out.println("We're done.");
            return false;
        }
        Video maxRankVideo = maxCache.rankedVideos.poll();
        if (fits(maxCache, maxRankVideo)) {
            cacheVideo(maxCache, maxRankVideo);
            System.out.println(
                    String.format("Inserted video %s in cache %3d (%4.1f%% full, %3d videos)", maxRankVideo, maxCache
                                    .id,
                            maxCache.getCurrentCacheUsage(cacheSize), maxCache.getNbVideosInCache()));
        }
        if (maxCache.rankedVideos.isEmpty()) {
            System.out.println("Queue for cache " + maxCache + " exhausted");
        }
        return true;
    }

    @Nullable
    private Cache getCacheWithBestVideo() {
        Cache maxCache = null;
        double globalMaxRank = Double.NEGATIVE_INFINITY;
        for (Cache cache : caches) {
            final Queue<Video> queue = cache.rankedVideos;
            if (queue.isEmpty()) {
                continue;
            }
            queue.sort();
            Video localMaxVideo = queue.peek();
            if (maxCache == null || localMaxVideo.rank > globalMaxRank) {
                maxCache = cache;
                globalMaxRank = localMaxVideo.rank;
            }
        }
        return maxCache;
    }

    private void cacheVideo(Cache maxCache, Video maxRankVideo) {
        maxCache.storedVideos.add(maxRankVideo);
        maxCache.remainingCapacity -= videoSizes[maxRankVideo.id];
        reduceRankInOtherCaches(maxCache, maxRankVideo);
    }

    private boolean fits(Cache cache, Video video) {
        return videoSizes[video.id] <= cache.remainingCapacity;
    }

    private void reduceRankInOtherCaches(Cache destinationCache, Video video) {
        for (Endpoint endpoint : destinationCache.endpoints) {
            int alreadyGained = endpoint.gainPerCache.get(destinationCache.id);
            Long nRequestsForVid = endpoint.nRequestsPerVideo.getOrDefault(video.id, null);
            if (nRequestsForVid == null) {
                continue; // no request for this video in this endpoint
            }
            long overestimationInGain = alreadyGained * nRequestsForVid;
            for (Entry<Integer, Integer> entry : endpoint.gainPerCache.entrySet()) {
                int epCacheId = entry.getKey();
                if (destinationCache.id != epCacheId) {
                    removeOverestimatedGain(video, overestimationInGain, caches[epCacheId]);
                }
            }
        }
    }

    private void removeOverestimatedGain(Video video, long overestimationInGain, Cache cache) {
        Queue<Video> queue = cache.rankedVideos;
        Video videoInThisCache = queue.findEqualElement(video);
        if (videoInThisCache == null) {
            return;
        }
        videoInThisCache.rank -= overestimationInGain;
        if (videoInThisCache.rank < 0) {
            videoInThisCache.rank = 0;
        }
    }
}
