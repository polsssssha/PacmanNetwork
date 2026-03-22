package com.pacman.server;

import com.pacman.common.GameMap;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class MapLoader {
    public static GameMap loadFromFile(String path) throws IOException {
        try (BufferedReader br = new BufferedReader(new FileReader(path))) {
            String line;
            GameMap map = null;

            while ((line = br.readLine()) != null) {
                line = line.trim();

                if (line.isEmpty() || line.startsWith("//") || line.equalsIgnoreCase("walls:")) {
                    continue;
                }

                String[] parts = line.split(";");
                if (parts.length != 2) continue;

                int v1 = Integer.parseInt(parts[0]);
                int v2 = Integer.parseInt(parts[1]);

                if (map == null) {
                    map = new GameMap(v1, v2);
                    fillEmpty(map);
                } else {
                    map.setCell(v1, v2, '#');
                }
            }
            return map;
        }
    }

    private static void fillEmpty(GameMap map) {
        for (int y = 0; y < map.getHeight(); y++) {
            for (int x = 0; x < map.getWidth(); x++) {
                map.setCell(x, y, '.');
            }
        }
    }
}