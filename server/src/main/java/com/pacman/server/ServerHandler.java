package com.pacman.server;

import com.pacman.common.GameMap;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.Channel;
import java.util.concurrent.ConcurrentHashMap;
import java.util.ArrayList;

public class ServerHandler extends ChannelInboundHandlerAdapter {

    private static final ConcurrentHashMap<Channel, com.pacman.common.PacketPlayerPos> playerPositions = new ConcurrentHashMap<>();

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        if (com.pacman.server.ServerLauncher.map != null) {
            ctx.writeAndFlush(com.pacman.server.ServerLauncher.map);
        }

        playerPositions.put(ctx.channel(), new com.pacman.common.PacketPlayerPos(1, 1));

        System.out.println("Новый игрок подключился! Карта отправлена. Всего игроков: " + playerPositions.size());
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        // Убираем игрока, когда он вышел
        playerPositions.remove(ctx.channel());
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        if (msg instanceof com.pacman.common.PacketPlayerPos) {
            // Обновляем позицию того, кто прислал пакет
            playerPositions.put(ctx.channel(), (com.pacman.common.PacketPlayerPos) msg);

            // Собираем все позиции в один пакет
            com.pacman.common.PacketAllPlayers allPlayers = new com.pacman.common.PacketAllPlayers(
                    new ArrayList<>(playerPositions.values())
            );

            // Рассылаем всем!
            for (Channel ch : playerPositions.keySet()) {
                ch.writeAndFlush(allPlayers);
            }
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }
}