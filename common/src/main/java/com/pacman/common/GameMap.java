package com.pacman.common;

public class GameMap implements java.io.Serializable {
    private final int width;
    private final int height;
    private final char[][] cells; // Массив для хранения стен '#' и пустых мест '.'

    public GameMap(int width, int height) {
        this.width = width;
        this.height = height;
        this.cells = new char[height][width];
    }

    public void setCell(int x, int y, char value) {
        if (x >= 0 && x < width && y >= 0 && y < height) {
            cells[y][x] = value;
        }
    }

    public char getCell(int x, int y) {
        if (x >= 0 && x < width && y >= 0 && y < height) {
            return cells[y][x];
        }
        return '#';
    }

    public boolean isWall(int x, int y) {
        return getCell(x, y) == '#';
    }

    public int getWidth() { return width; }
    public int getHeight() { return height; }
}