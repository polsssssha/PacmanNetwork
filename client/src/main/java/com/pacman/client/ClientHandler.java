package com.pacman.client;

import com.pacman.common.GameMap;
import com.pacman.common.PacketPlayerPos;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

import java.util.List;

public class ClientHandler extends ChannelInboundHandlerAdapter {

    private final PacmanGame game;

    public ClientHandler(PacmanGame game) {
        this.game = game;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        System.out.println("Подключение к серверу установлено!");
    }

    @Override
    @SuppressWarnings("unchecked")
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        if (msg instanceof List) {
            List<PacketPlayerPos> players = (List<PacketPlayerPos>) msg;
            game.updateOtherPlayers(players);

        } else if (msg instanceof GameMap) {
            game.setMap((GameMap) msg);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }
}