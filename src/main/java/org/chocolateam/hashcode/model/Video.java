package org.chocolateam.hashcode.model;

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
        return (int)(rank - o.rank);
    }

    @Override
    public String toString() {
        return String.format("%4d (r=%6.2f)", id, rank);
    }
}
