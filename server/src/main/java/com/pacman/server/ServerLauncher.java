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

                for (int y = 0; y < map.getHeight(); y++) {
                    for (int x = 0; x < map.getWidth(); x++) {
                        if (map.getCell(x, y) == ' ') {
                            map.setCell(x, y, '.');
                        }
                    }
                }
                System.out.println("Точки расставлены на карте.");

                new Thread(() -> {
                    io.netty.channel.EventLoopGroup bossGroup = new io.netty.channel.nio.NioEventLoopGroup(1);
                    io.netty.channel.EventLoopGroup workerGroup = new io.netty.channel.nio.NioEventLoopGroup();
                    try {
                        io.netty.bootstrap.ServerBootstrap b = new io.netty.bootstrap.ServerBootstrap();
                        b.group(bossGroup, workerGroup)
                                .channel(io.netty.channel.socket.nio.NioServerSocketChannel.class)
                                .childHandler(new io.netty.channel.ChannelInitializer<io.netty.channel.socket.SocketChannel>() {
                                    @Override
                                    public void initChannel(io.netty.channel.socket.SocketChannel ch) {
                                        ch.pipeline().addLast(new io.netty.handler.codec.serialization.ObjectEncoder());
                                        ch.pipeline().addLast(new io.netty.handler.codec.serialization.ObjectDecoder(
                                                io.netty.handler.codec.serialization.ClassResolvers.cacheDisabled(null)));
                                        ch.pipeline().addLast(new ServerHandler());
                                    }
                                });

                        System.out.println("Netty сервер слушает порт 8080...");
                        b.bind(8080).sync().channel().closeFuture().sync();
                    } catch (Exception e) {
                        e.printStackTrace();
                    } finally {
                        bossGroup.shutdownGracefully();
                        workerGroup.shutdownGracefully();
                    }
                }).start();
            }

        } catch (IOException e) {
            System.err.println("Ошибка: не удалось прочитать map.txt. Проверь, лежит ли он в корне проекта.");
        }
    }
}