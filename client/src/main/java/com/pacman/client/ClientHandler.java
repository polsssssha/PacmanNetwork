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
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        if (msg instanceof List) {
            game.updateOtherPlayers((List<PacketPlayerPos>) msg);
        } else if (msg instanceof GameMap) {
            game.setMap((GameMap) msg);
        } else if (msg instanceof PacketPlayerPos) {
            PacketPlayerPos pos = (PacketPlayerPos) msg;
            // Если сервер прислал INIT — это наша стартовая позиция
            if ("INIT".equals(pos.id)) {
                game.setStartPosition(pos.x, pos.y);
            }
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }
}