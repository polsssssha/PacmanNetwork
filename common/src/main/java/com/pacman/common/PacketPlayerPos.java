package com.pacman.common;

import java.io.Serializable;

public class PacketPlayerPos implements Serializable {
    public int x; // Было float
    public int y; // Было float

    public PacketPlayerPos(int x, int y) { // Тут тоже на int
        this.x = x;
        this.y = y;
    }
}