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
        if (msg instanceof com.pacman.common.GameMap) {
            game.setMap((com.pacman.common.GameMap) msg);
        }

        else if (msg instanceof com.pacman.common.PacketAllPlayers) {
            com.pacman.common.PacketAllPlayers all = (com.pacman.common.PacketAllPlayers) msg;
            // Передаем список всех игроков в основной класс игры
            game.updateOtherPlayers(all.players);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }
}