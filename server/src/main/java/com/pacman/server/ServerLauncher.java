package com.pacman.server;

import com.pacman.common.GameMap;
import java.io.IOException;

public class ServerLauncher {

    public static GameMap map;

    public static void main(String[] args) {
        System.out.println("=== Запуск сервера Пакмана ===");

        try {
            map = MapLoader.loadFromFile("map.txt");

            if (map != null) {
                System.out.println("Карта успешно загружена!");
                System.out.println("Размер: " + map.getWidth() + "x" + map.getHeight());
                System.out.println("--- Визуализация лабиринта ---");

                for (int y = map.getHeight() - 1; y >= 0; y--) {
                    for (int x = 0; x < map.getWidth(); x++) {
                        System.out.print(map.getCell(x, y));
                    }
                    System.out.println();
                }
                System.out.println("------------------------------");
            }

        } catch (IOException e) {
            System.err.println("Ошибка: не удалось прочитать map.txt. Проверь, лежит ли он в корне проекта.");
        }

        new Thread(() -> {
            try {
                new NettyServer(8080).run();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }
}