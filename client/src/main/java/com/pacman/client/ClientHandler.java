package com.pacman.client;

import com.pacman.common.GameMap;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

public class ClientHandler extends ChannelInboundHandlerAdapter {

    private PacmanGame game;

    public ClientHandler(PacmanGame game) {
        this.game = game;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        System.out.println("Успешно подключились к серверу!");
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        if (msg instanceof java.util.List) {
            java.util.List<com.pacman.common.PacketPlayerPos> players = (java.util.List<com.pacman.common.PacketPlayerPos>) msg;

            game.updateOtherPlayers(players);

           // System.out.println("Клиент получил список игроков! Кол-во: " + players.size());
        } else if (msg instanceof com.pacman.common.GameMap) {
            game.setMap((com.pacman.common.GameMap) msg);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }
}