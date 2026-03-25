package com.pacman.common;
import java.io.Serializable;

public class PacketPlayerPos implements Serializable {
    public int x, y;
    public String id;
    public int score;

    public PacketPlayerPos(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public PacketPlayerPos(int x, int y, String id, int score) {
        this.x = x;
        this.y = y;
        this.id = id;
        this.score = score;
    }
}