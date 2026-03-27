package com.pacman.server;

import com.pacman.common.GameMap;
import com.pacman.common.PacketPlayerPos;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

public class ServerHandler extends ChannelInboundHandlerAdapter {

    private static final ConcurrentHashMap<Channel, PacketPlayerPos> players = new ConcurrentHashMap<>();

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        if (ServerLauncher.map != null) {
            ctx.writeAndFlush(ServerLauncher.map);
        }

        // Временный пакет для инициализации
        players.put(ctx.channel(), new PacketPlayerPos(1, 1, "Connecting...", 0));
        System.out.println("Новый игрок! В сети: " + players.size());
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        players.remove(ctx.channel());
        System.out.println("Игрок ушел. Осталось: " + players.size());

        broadcastPlayers();
        super.channelInactive(ctx);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        if (msg instanceof PacketPlayerPos) {
            PacketPlayerPos pos = (PacketPlayerPos) msg;
            players.put(ctx.channel(), pos);

            // Обработка съеденных точек
            GameMap map = ServerLauncher.map;
            if (map != null && map.getCell(pos.x, pos.y) == '.') {
                map.setCell(pos.x, pos.y, ' ');
                broadcastMap(map);
            }

            broadcastPlayers();
        }
    }

    private void broadcastPlayers() {
        ArrayList<PacketPlayerPos> allPos = new ArrayList<>(players.values());
        players.keySet().forEach(ch -> ch.writeAndFlush(allPos));
    }

    private void broadcastMap(GameMap map) {
        players.keySet().forEach(ch -> ch.writeAndFlush(map));
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        players.remove(ctx.channel());
        ctx.close();
    }
}