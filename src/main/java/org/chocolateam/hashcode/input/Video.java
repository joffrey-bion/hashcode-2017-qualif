package org.chocolateam.hashcode.input;

import org.jetbrains.annotations.NotNull;

public class Video implements Comparable<Video> {

    public final int id;

    public double rank;

    public Video(int id, double rank) {
        this.id = id;
        this.rank = rank;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        Video video = (Video)o;

        return id == video.id;
    }

    @Override
    public int hashCode() {
        return id;
    }

    @Override
    public int compareTo(@NotNull Video o) {
        return (int)Math.signum(rank - o.rank);
    }

    @Override
    public String toString() {
        return id + " (r=" + rank + ")";
    }
}
