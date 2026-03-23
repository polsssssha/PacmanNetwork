package com.pacman.client;

import io.netty.channel.Channel;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.pacman.common.GameMap;
import io.netty.handler.codec.serialization.ClassResolvers;
import io.netty.handler.codec.serialization.ObjectDecoder;
import io.netty.handler.codec.serialization.ObjectEncoder;

public class PacmanGame extends ApplicationAdapter {

    java.util.List<com.pacman.common.PacketPlayerPos> otherPlayers = new java.util.ArrayList<>();
    SpriteBatch batch;
    Texture wallTex;
    Texture pacmanTex;
    GameMap map;
    Channel channel;

    int currX = 1, currY = 1;
    int targetX = 1, targetY = 1;
    float visualX = 20, visualY = 20; // Реальные пиксели для отрисовки
    float progress = 1.0f;

    @Override
    public void create() {
        batch = new SpriteBatch();

        Pixmap pWall = new Pixmap(20, 20, Pixmap.Format.RGBA8888);
        pWall.setColor(0, 0, 1, 1); // Синий
        pWall.fill();
        wallTex = new Texture(pWall);
        pWall.dispose();

        Pixmap pPac = new Pixmap(18, 18, Pixmap.Format.RGBA8888);
        pPac.setColor(1, 1, 0, 1); // Желтый
        pPac.fill();
        pacmanTex = new Texture(pPac);
        pPac.dispose();

        map = new GameMap(23, 22);

        new Thread(() -> {
            NioEventLoopGroup group = new NioEventLoopGroup();
            try {
                Bootstrap b = new Bootstrap();
                b.group(group)
                        .channel(NioSocketChannel.class)
                        .handler(new ChannelInitializer<SocketChannel>() {
                            @Override
                            public void initChannel(SocketChannel ch) {
                                ch.pipeline().addLast(new ObjectEncoder());
                                ch.pipeline().addLast(new ObjectDecoder(ClassResolvers.cacheDisabled(null)));
                                ch.pipeline().addLast(new ClientHandler(PacmanGame.this)); // Передаем ссылку на игру
                            }
                        });
                Channel ch = b.connect("localhost", 8080).sync().channel();
                this.channel = ch; // Сохраняем канал в поле класса
                ch.closeFuture().sync();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                group.shutdownGracefully();
            }
        }).start();
    }

    @Override
    public void render() {
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        float deltaTime = Gdx.graphics.getDeltaTime();

        if (progress < 1.0f) {
            // Мы в пути между клетками
            progress += deltaTime * 5; // Скорость перемещения (5 клеток в секунду)
            if (progress > 1.0f) progress = 1.0f;

            // Плавно вычисляем визуальную позицию (интерполяция)
            visualX = (currX + (targetX - currX) * progress) * 20;
            visualY = (currY + (targetY - currY) * progress) * 20;
        } else {
            // Мы стоим в клетке, пора выбрать следующую!
            currX = targetX;
            currY = targetY;

            if (channel != null && channel.isActive()) {
                channel.writeAndFlush(new com.pacman.common.PacketPlayerPos(currX, currY));
            }

            visualX = currX * 20;
            visualY = currY * 20;

            // Логика Пункта 8: Приоритеты кнопок
            int nextDX = 0;
            int nextDY = 0;

            if (Gdx.input.isKeyPressed(com.badlogic.gdx.Input.Keys.W)) nextDY = 1;
            else if (Gdx.input.isKeyPressed(com.badlogic.gdx.Input.Keys.S)) nextDY = -1;

            if (Gdx.input.isKeyPressed(com.badlogic.gdx.Input.Keys.D)) nextDX = 1;
            else if (Gdx.input.isKeyPressed(com.badlogic.gdx.Input.Keys.A)) nextDX = -1;

            // Проверяем возможность хода (сначала вертикаль, потом горизонталь - для Пункта 8.а)
            if (nextDY != 0 && map != null && !map.isWall(currX, currY + nextDY)) {
                targetY = currY + nextDY;
                progress = 0;
            } else if (nextDX != 0 && map != null && !map.isWall(currX + nextDX, currY)) {
                targetX = currX + nextDX;
                progress = 0;
            }
        }

        batch.begin();
        // Отрисовка карты
        if (map != null) {
            for (int y = 0; y < map.getHeight(); y++) {
                for (int x = 0; x < map.getWidth(); x++) {
                    if (map.getCell(x, y) == '#') batch.draw(wallTex, x * 20, y * 20);
                }
            }
        }

        for (com.pacman.common.PacketPlayerPos op : otherPlayers) {
            if (op.x == currX && op.y == currY) continue;

            // Рисуем чужого Пакмана
            batch.draw(pacmanTex, op.x * 20, op.y * 20);
        }

        // Рисуем Пакмана в визуальной позиции
        batch.draw(pacmanTex, visualX, visualY);
        batch.end();
    }

    @Override
    public void dispose() {
        batch.dispose();
        wallTex.dispose();
        pacmanTex.dispose();
    }

    public void updateOtherPlayers(java.util.List<com.pacman.common.PacketPlayerPos> players) {
        this.otherPlayers = players;
    }

    public void setMap(com.pacman.common.GameMap newMap) {
        this.map = newMap;
    }
}