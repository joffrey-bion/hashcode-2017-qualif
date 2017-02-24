package org.chocolateam.hashcode.model;

public class ScoredVideo {

    public final Video video;

    public double score;

    public ScoredVideo(Video video, double score) {
        this.video = video;
        this.score = score;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        ScoredVideo that = (ScoredVideo)o;
        return video.equals(that.video);
    }

    @Override
    public int hashCode() {
        return video.hashCode();
    }

    @Override
    public String toString() {
        return String.format("%4d (s=%11.2f)", video.id, score);
    }
}
