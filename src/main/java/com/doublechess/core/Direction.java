package com.doublechess.core;

public class Direction {
    private int rank;
    private int file;

    public Direction(int rank, int file) {
        this.rank = rank;
        this.file = file;
    }

    public int getRank() {
        return rank;
    }

    public int getFile() {
        return file;
    }
}
