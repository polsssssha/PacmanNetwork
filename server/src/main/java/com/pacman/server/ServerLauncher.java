package com.pacman.server;

import com.pacman.common.GameMap;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.serialization.ClassResolvers;
import io.netty.handler.codec.serialization.ObjectDecoder;
import io.netty.handler.codec.serialization.ObjectEncoder;

import java.io.IOException;

public class ServerLauncher {

    public static GameMap map;

    public static void main(String[] args) {
        System.out.println("=== Запуск сервера Пакмана ===");

        try {
            map = MapLoader.loadFromFile("map.txt");
            if (map == null) return;

            System.out.println("Карта загружена!");

            // Автоматическая расстановка точек в пустые клетки
            for (int y = 0; y < map.getHeight(); y++) {
                for (int x = 0; x < map.getWidth(); x++) {
                    if (map.getCell(x, y) == ' ') {
                        map.setCell(x, y, '.');
                    }
                }
            }

            startNettyServer();

        } catch (IOException e) {
            System.err.println("Ошибка: не удалось найти map.txt");
        }
    }

    private static void startNettyServer() {
        new Thread(() -> {
            try {
                new NettyServer(8080).run();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }
}