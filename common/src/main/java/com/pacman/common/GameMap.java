package com.pacman.common;

import java.io.Serializable;

public class GameMap implements Serializable {
    private final int width;
    private final int height;
    private final char[][] cells;

    public GameMap(int width, int height) {
        this.width = width;
        this.height = height;
        this.cells = new char[height][width];
        // Заполняем карту пробелами по умолчанию
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                cells[y][x] = ' ';
            }
        }
    }

    public void setCell(int x, int y, char value) {
        if (isValid(x, y)) cells[y][x] = value;
    }

    public char getCell(int x, int y) {
        return isValid(x, y) ? cells[y][x] : '#';
    }

    public boolean isWall(int x, int y) {
        return getCell(x, y) == '#';
    }

    private boolean isValid(int x, int y) {
        return x >= 0 && x < width && y >= 0 && y < height;
    }

    public int getWidth() { return width; }
    public int getHeight() { return height; }
}