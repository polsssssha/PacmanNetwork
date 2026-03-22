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

        if (msg instanceof GameMap) {
            game.map = (GameMap) msg;
            System.out.println("Карта получена от сервера!");
        }

        // Позже здесь мы будем обрабатывать и другие сообщения (позиции игроков)
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }
}