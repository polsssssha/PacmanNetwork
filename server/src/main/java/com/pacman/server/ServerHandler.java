package com.pacman.server;

import com.pacman.common.GameMap;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

public class ServerHandler extends ChannelInboundHandlerAdapter {

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        System.out.println("Клиент подключился! Отправляю карту...");
        // Отправляем карту, которую мы загрузили в ServerLauncher
        ctx.writeAndFlush(ServerLauncher.map);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        if (msg instanceof com.pacman.common.PacketPlayerPos) {
            com.pacman.common.PacketPlayerPos pos = (com.pacman.common.PacketPlayerPos) msg;
            System.out.println("Сервер получил позицию игрока: X=" + pos.x + " Y=" + pos.y);

            // В будущем здесь сервер будет проверять:
            // "А не пытается ли игрок пройти сквозь стену?"
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }
}