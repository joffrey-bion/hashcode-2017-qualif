package org.chocolateam.hashcode.util;

import org.chocolateam.hashcode.model.ScoredVideo;
import org.chocolateam.hashcode.model.Video;
import org.junit.Assert;
import org.junit.Test;

public class PriorityQueueTest {
    @Test
    public void removeSimilar() {
        PriorityQueue<ScoredVideo> queue = new PriorityQueue<>();
        ScoredVideo queuedVid = new ScoredVideo(new Video(42, 10), 123.0);
        queue.add(queuedVid);
        ScoredVideo similarVid = new ScoredVideo(new Video(42, 10), 500.0);
        ScoredVideo removed = queue.removeMatching(v -> v.video.id == similarVid.video.id);
        Assert.assertSame(queuedVid, removed);
    }

}