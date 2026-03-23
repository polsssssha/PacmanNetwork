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
        playerPositions.remove(ctx.channel());
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        if (msg instanceof com.pacman.common.PacketPlayerPos) {
            com.pacman.common.PacketPlayerPos pos = (com.pacman.common.PacketPlayerPos) msg;

//            String clientId = ctx.channel().id().asLongText();
//            pos.id = clientId;

            playerPositions.put(ctx.channel(), pos);

            com.pacman.common.GameMap map = com.pacman.server.ServerLauncher.map;
            if (map != null && map.getCell(pos.x, pos.y) == '.') {
                map.setCell(pos.x, pos.y, ' '); // Точка исчезает!
                // Рассылаем обновленную карту ВСЕМ
                for (io.netty.channel.Channel ch : playerPositions.keySet()) {
                    ch.writeAndFlush(map);
                }
            }

            broadcastPlayers();
        }
    }
    private void broadcastPlayers() {
        java.util.ArrayList<com.pacman.common.PacketPlayerPos> allPositions =
                new java.util.ArrayList<>(playerPositions.values());

        for (io.netty.channel.Channel ch : playerPositions.keySet()) {
            if (ch.isActive()) {
                ch.writeAndFlush(allPositions);
            }
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }
}