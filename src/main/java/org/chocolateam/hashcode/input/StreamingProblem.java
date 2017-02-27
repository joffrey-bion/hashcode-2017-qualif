package org.chocolateam.hashcode.input;

import java.util.Arrays;
import java.util.List;
import java.util.Map.Entry;

import org.chocolateam.hashcode.model.Cache;
import org.chocolateam.hashcode.model.ScoredVideo;
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

    private Cache[] caches;

    private Video[] videos;

    private long totalUsedCacheCapacity;

    private long totalCacheCapacity;

    public List<String> solve() {
        System.out.println(
                String.format("Solving for %d videos, %d caches, %d endpoints, %d requests", nVideos, nCaches,
                        nEndpoints, nRequestDescriptions));

        createCaches();
        createVideos();
        computeInitialGains();
        scoreVideos();

        Arrays.stream(caches).filter(Cache::allCandidatesFit).forEach(this::cacheAllCandidates);

        while (findAndInsertMax()) {
        }

        return new Solution(caches).outputLines();
    }

    private void createCaches() {
        caches = new Cache[nCaches];
        for (int i = 0; i < nCaches; i++) {
            caches[i] = new Cache(i, cacheSize);
        }
        totalCacheCapacity = cacheSize * nCaches;
        totalUsedCacheCapacity = 0;
    }

    private void createVideos() {
        videos = new Video[nVideos];
        for (int i = 0; i < nVideos; i++) {
            videos[i] = new Video(i, videoSizes[i]);
        }
    }

    private void computeInitialGains() {
        System.out.println("Computing gain for each cache and video...");
        for (RequestDesc rd : requestDescs) {
            readRequest(rd);
        }
    }

    private void readRequest(RequestDesc requestDesc) {
        Endpoint endpoint = endpoints[requestDesc.endpointId];
        Video video = videos[requestDesc.videoId];

        endpoint.addRequests(video, requestDesc.count);

        for (Entry<Integer, Integer> gainForCache : endpoint.gainPerCache.entrySet()) {
            int cacheId = gainForCache.getKey();

            // for graph
            Cache cache = caches[cacheId];
            cache.endpoints.add(endpoint);

            int gainPerRequest = gainForCache.getValue();
            int totalGain = requestDesc.count * gainPerRequest;
            cache.addGainForVideo(video, totalGain);
        }
    }

    private void scoreVideos() {
        System.out.println("Computing score for each video in each cache...");
        for (Cache cache : caches) {
            System.out.println("Computing score for each video in cache " + cache);
            cache.scoreVideos();
        }
    }

    private boolean findAndInsertMax() {
        if (Thread.currentThread().isInterrupted()) {
            System.out.println("INTERRUPTED. Stopping here.");
            return false;
        }
        Cache maxCache = getCacheWithBestVideo();
        if (maxCache == null) {
            System.out.println("We're done.");
            return false;
        }
        ScoredVideo maxRankVideo = maxCache.pollBestCandidate();
        if (maxCache.canHold(maxRankVideo)) {
            cacheVideo(maxCache, maxRankVideo.video);
            System.out.println(String.format(
                    "Inserted video %s in cache %3d (%5.1f%% full, %3d videos)  %5.1f%% total cache capacity",
                    maxRankVideo, maxCache.id, maxCache.getCurrentCacheUsage(cacheSize), maxCache.getNbVideosInCache(),
                    getOverallCacheUsage()));
            if (maxCache.isQueueEmpty()) {
                System.out.println("Queue for cache " + maxCache + " exhausted");
            }
        } else if (maxCache.allCandidatesFit()) {
            // we're gonna add them anyway at some point, better update the score of these videos in other caches up
            // front
            System.out.println("All remaining candidates videos for cache " + maxCache + " will fit, adding them all");
            cacheAllCandidates(maxCache);
        }
        return true;
    }

    private double getOverallCacheUsage() {
        return (double) totalUsedCacheCapacity * 100 / totalCacheCapacity;
    }

    @Nullable
    private Cache getCacheWithBestVideo() {
        Cache cacheWithBestVideo = null;
        double globalMaxScore = Double.NEGATIVE_INFINITY;
        for (Cache cache : caches) {
            if (cache.scoredVideos.isEmpty()) {
                continue;
            }
            ScoredVideo localMaxVideo = cache.peekBestCandidate();
            if (cacheWithBestVideo == null || localMaxVideo.score > globalMaxScore) {
                cacheWithBestVideo = cache;
                globalMaxScore = localMaxVideo.score;
            }
        }
        return cacheWithBestVideo;
    }

    private void cacheVideo(Cache cache, Video video) {
        cache.store(video);
        totalUsedCacheCapacity += video.size;
        reduceScoreInSiblingCaches(cache, video);
    }

    private void cacheAllCandidates(Cache cache) {
        while (!cache.isQueueEmpty()) {
            cacheVideo(cache, cache.pollBestCandidate().video);
        }
    }

    private void reduceScoreInSiblingCaches(Cache destinationCache, Video video) {
        for (Endpoint endpoint : destinationCache.endpoints) {
            int alreadyGained = endpoint.gainPerCache.get(destinationCache.id);
            long nRequestsForVid = endpoint.getNbRequests(video);
            if (nRequestsForVid == 0) {
                continue; // no request for this video in this endpoint
            }
            long overestimationInGain = alreadyGained * nRequestsForVid;
            double overestimationInRank = overestimationInGain / video.size;
            reduceVideoScores(video, overestimationInRank, endpoint.cacheIds, destinationCache.id);
        }
    }

    private void reduceVideoScores(Video video, double overestimationInRank, int[] cacheIds, int chosenCache) {
        for (int cacheId : cacheIds) {
            if (cacheId != chosenCache) {
                reduceVideoScore(video, overestimationInRank, caches[cacheId]);
            }
        }
    }

    private static void reduceVideoScore(Video video, double amount, Cache cache) {
        ScoredVideo videoInThisCache = cache.scoredVideos.removeMatching(v -> v.video.id == video.id);
        if (videoInThisCache == null) {
//            System.out.println(
//                    String.format("Video %s already stored in cache %s, not in queue anymore", video, cache));
            return;
        }
//        System.out.println(
//                String.format("Removing %7.2f score for video %s for cache %s", amount, video, cache));
        videoInThisCache.score -= amount;
        if (videoInThisCache.score < 0) {
            videoInThisCache.score = 0;
        }
        cache.scoredVideos.add(videoInThisCache);
    }
}
