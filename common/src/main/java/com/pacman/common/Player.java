package com.pacman.common;

import java.io.Serializable;

public class Player implements Serializable {
    public int id;
    public int x, y; // Позиция на сетке

    public Player(int id, int x, int y) {
        this.id = id;
        this.x = x;
        this.y = y;
    }
}