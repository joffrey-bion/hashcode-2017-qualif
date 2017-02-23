package org.chocolateam.hashcode.input;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.PriorityQueue;

import org.chocolateam.hashcode.output.Solution;
import org.jetbrains.annotations.NotNull;

public class StreamingProblem {

    private static final Comparator<Video> comp = Comparator.comparing((Video v) -> v.rank).reversed();

    public int nVideos;

    public int nEndpoints;

    public int nRequestDescriptions;

    public int nCaches;

    public int cacheSize;

    public int[] videoSizes;

    public Endpoint[] endpoints;

    public RequestDesc[] requestDescs;

    public Cache[] caches;

    private int[] remainingCacheCapacity;

    private Map<Integer, List<Video>> videosPerCache;

    public Solution solve() {
        caches = new Cache[nCaches];
        Map<Integer, Map<Integer, Long>> gainPerVideoPerCache = computeInitialGains();
        Map<Integer, PriorityQueue<Video>> rankedVideosPerCache = rankVideos(gainPerVideoPerCache);

        remainingCacheCapacity = new int[nCaches];
        Arrays.fill(remainingCacheCapacity, cacheSize);
        videosPerCache = new HashMap<>();

        while (findAndInsertMax(rankedVideosPerCache)) {
        }

        return new Solution(videosPerCache);
    }

    private boolean findAndInsertMax(Map<Integer, PriorityQueue<Video>> rankedVideosPerCache) {
        int maxCacheId = -1;
        Video globalMaxVideo = null;
        for (Entry<Integer, PriorityQueue<Video>> cacheHeap : rankedVideosPerCache.entrySet()) {
            int cacheId = cacheHeap.getKey();
            final PriorityQueue<Video> queue = cacheHeap.getValue();
            if (queue.isEmpty()) {
                continue;
            }
            Video localMaxVideo = queue.peek();
            if (globalMaxVideo == null || localMaxVideo.rank > globalMaxVideo.rank) {
                maxCacheId = cacheId;
                globalMaxVideo = localMaxVideo;
            }
        }
        if (globalMaxVideo == null) {
            System.out.println("No max video anymore");
            return false;
        }
        if (remainingCacheCapacity[maxCacheId] < videoSizes[globalMaxVideo.id]) {
            PriorityQueue<Video> queue = rankedVideosPerCache.get(maxCacheId);
            queue.poll();
            if (queue.isEmpty()) {
                rankedVideosPerCache.remove(maxCacheId);
                System.out.println("Queue for cache " + maxCacheId + " exhausted");
            }
        } else {
            insertMaxVideo(maxCacheId, globalMaxVideo, rankedVideosPerCache);
            remainingCacheCapacity[maxCacheId] -= videoSizes[globalMaxVideo.id];
            System.out.println(
                    String.format("Inserted video %s in cache %d (%.1f%% full, %d videos)", globalMaxVideo, maxCacheId,
                            getCurrentCacheUsage(maxCacheId), getNbVideosInCache(maxCacheId)));
        }
        return true;
    }

    private int getNbVideosInCache(int cacheId) {
        return videosPerCache.get(cacheId).size();
    }

    private double getCurrentCacheUsage(int cacheId) {
        int remaining = remainingCacheCapacity[cacheId];
        return (double)100 * (cacheSize - remaining) / cacheSize;
    }

    private void insertMaxVideo(int destinationCacheId, Video bestVideo,
            Map<Integer, PriorityQueue<Video>> rankedVideosPerCache) {
        for (Entry<Integer, PriorityQueue<Video>> cacheHeap : rankedVideosPerCache.entrySet()) {
            int cacheId = cacheHeap.getKey();
            PriorityQueue<Video> queue = cacheHeap.getValue();
            if (cacheId == destinationCacheId) {
                queue.poll();
            } else {
                updateRank(cacheId, queue, bestVideo);
            }
        }
        List<Video> videos = videosPerCache.computeIfAbsent(destinationCacheId, k -> new ArrayList<>());
        videos.add(bestVideo);
    }

    private void updateRank(int cacheId, PriorityQueue<Video> queue, Video video) {
        Video videoInThisCache = removeAndRetrieve(queue, video);
        if (videoInThisCache == null) {
            return;
        }
        Cache cache = caches[cacheId];
        for (Endpoint endpoint : cache.endpoints) {
            int gainForUsedCache = endpoint.gainPerCache.get(cacheId);
            Long nRequestsForVid = endpoint.nRequestsPerVideo.getOrDefault(video.id, null);
            if (nRequestsForVid == null) {
                continue;
            }
            for (Entry<Integer, Integer> entry : endpoint.gainPerCache.entrySet()) {
                int epCacheId = entry.getKey();
                if (cacheId != epCacheId) {
                    videoInThisCache.rank -= gainForUsedCache * nRequestsForVid;
                    if (videoInThisCache.rank < 0) {
                        videoInThisCache.rank = 0;
                    }
                }
            }
        }
        queue.add(videoInThisCache);
    }

    private Video removeAndRetrieve(PriorityQueue<Video> queue, Video similarVideo) {
        Video videoToRemove = null;
        for (Video video : queue) {
            if (video.equals(similarVideo)) {
                videoToRemove = video;
                break;
            }
        }
        queue.remove(videoToRemove);
        return videoToRemove;
    }

    private Map<Integer, Map<Integer, Long>> computeInitialGains() {
        System.out.println("Computing gain for each cache and video...");
        Map<Integer, Map<Integer, Long>> gainPerVideoPerCache = new HashMap<>();
        Arrays.stream(requestDescs).forEach(rd -> addGain(gainPerVideoPerCache, rd));
        return gainPerVideoPerCache;
    }

    private void addGain(Map<Integer, Map<Integer, Long>> gainPerVideoPerCache, RequestDesc requestDesc) {
        Endpoint endpoint = endpoints[requestDesc.endpointId];
        // for graph
        endpoint.id = requestDesc.endpointId;

        int videoId = requestDesc.videoId;
        endpoint.nRequestsPerVideo.putIfAbsent(videoId, 0L);
        endpoint.nRequestsPerVideo.compute(videoId, (c, val) -> val + requestDesc.count);

        for (Entry<Integer, Integer> gainForCache : endpoint.gainPerCache.entrySet()) {
            int cacheId = gainForCache.getKey();

            // for graph
            Cache cache = caches[cacheId];
            if (cache == null) {
                cache = new Cache();
                caches[cacheId] = cache;
            }
            cache.endpoints.add(endpoint);

            int gainPerRequest = gainForCache.getValue();
            int totalGain = requestDesc.count * gainPerRequest;
            Map<Integer, Long> gainForVideo = gainPerVideoPerCache.computeIfAbsent(cacheId, k -> new HashMap<>());
            gainForVideo.putIfAbsent(videoId, 0L);
            gainForVideo.compute(videoId, (c, val) -> val + totalGain);
        }
    }

    @NotNull
    private Map<Integer, PriorityQueue<Video>> rankVideos(Map<Integer, Map<Integer, Long>> gainPerVideoPerCache) {
        System.out.println("Computing rank for each video in each cache...");
        Map<Integer, PriorityQueue<Video>> rankedVideosPerCache = new HashMap<>();
        for (Entry<Integer, Map<Integer, Long>> integerMapEntry : gainPerVideoPerCache.entrySet()) {
            int cacheId = integerMapEntry.getKey();
            Map<Integer, Long> gainPerVideo = integerMapEntry.getValue();
            System.out.println("Computing rank for each video in cache " + cacheId);
            PriorityQueue<Video> videos = rankVideosForCache(gainPerVideo);
            rankedVideosPerCache.put(cacheId, videos);
        }
        return rankedVideosPerCache;
    }

    private PriorityQueue<Video> rankVideosForCache(Map<Integer, Long> gainPerVideo) {
        PriorityQueue<Video> videos = new PriorityQueue<>(comp);
        for (Entry<Integer, Long> gainForVideo : gainPerVideo.entrySet()) {
            int videoId = gainForVideo.getKey();
            long totalGainForVideo = gainForVideo.getValue();
            double rank = totalGainForVideo / videoSizes[videoId];
            videos.add(new Video(videoId, rank));
        }
        return videos;
    }
}
