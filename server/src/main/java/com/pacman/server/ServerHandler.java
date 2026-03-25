package com.pacman.server;

import com.pacman.common.PacketPlayerPos;
import com.pacman.common.GameMap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

public class ServerHandler extends ChannelInboundHandlerAdapter {

    private static final ConcurrentHashMap<Channel, PacketPlayerPos> playerPositions = new ConcurrentHashMap<>();

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        if (ServerLauncher.map != null) {
            ctx.writeAndFlush(ServerLauncher.map);
        }

        playerPositions.put(ctx.channel(), new PacketPlayerPos(1, 1, "Connecting...", 0));

        System.out.println("Новый игрок! Всего в сети: " + playerPositions.size());
    }

    // метод для удаления игрока
    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        playerPositions.remove(ctx.channel());

        System.out.println("Игрок отключился. Осталось: " + playerPositions.size());

        broadcastPlayers();

        super.channelInactive(ctx);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        if (msg instanceof PacketPlayerPos) {
            PacketPlayerPos pos = (PacketPlayerPos) msg;

            playerPositions.put(ctx.channel(), pos);

            GameMap map = ServerLauncher.map;
            if (map != null && map.getCell(pos.x, pos.y) == '.') {
                map.setCell(pos.x, pos.y, ' ');
                for (Channel ch : playerPositions.keySet()) {
                    ch.writeAndFlush(map);
                }
            }
            broadcastPlayers();
        }
    }

    private void broadcastPlayers() {
        ArrayList<PacketPlayerPos> allPositions = new ArrayList<>(playerPositions.values());

        for (Channel ch : playerPositions.keySet()) {
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