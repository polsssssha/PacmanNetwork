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

                // Пропускаем пустые строки, комментарии и заголовки
                if (line.isEmpty() || line.startsWith("//") || line.toLowerCase().startsWith("walls")) {
                    continue;
                }

                String[] parts = line.split(";");
                if (parts.length != 2) continue;

                try {
                    int v1 = Integer.parseInt(parts[0]);
                    int v2 = Integer.parseInt(parts[1]);

                    if (map == null) {
                        // Первая пара чисел — это размеры карты
                        map = new GameMap(v1, v2);
                        initializeMap(map);
                    } else {
                        // Остальные пары — координаты стен
                        map.setCell(v1, v2, '#');
                    }
                } catch (NumberFormatException e) {
                    System.err.println("Ошибка формата в строке: " + line);
                }
            }
            return map;
        }
    }

    private static void initializeMap(GameMap map) {
        for (int y = 0; y < map.getHeight(); y++) {
            for (int x = 0; x < map.getWidth(); x++) {
                map.setCell(x, y, '.');
            }
        }
    }
}