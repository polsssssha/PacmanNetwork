package com.pacman.client;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.pacman.common.GameMap;
import com.pacman.common.PacketPlayerPos;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.serialization.ClassResolvers;
import io.netty.handler.codec.serialization.ObjectDecoder;
import io.netty.handler.codec.serialization.ObjectEncoder;

import java.util.ArrayList;
import java.util.List;

public class PacmanGame extends ApplicationAdapter {

    private SpriteBatch batch;
    private Texture wallTex, pacmanTex, otherPacTex;
    private BitmapFont font;

    private GameMap map;
    private Channel channel;
    private String myId;
    private volatile List<PacketPlayerPos> otherPlayers = new ArrayList<>();

    private int myScore = 0;
    private int currX = 1, currY = 1;
    private int targetX = 1, targetY = 1;
    private float visualX = 20, visualY = 20;
    private float progress = 1.0f;

    @Override
    public void create() {
        batch = new SpriteBatch();
        initTextures();

        map = new GameMap(23, 22);
        font = new BitmapFont();
        font.getData().setScale(1.2f);

        startClientNetwork();
    }

    private void initTextures() {
        // Стена
        Pixmap p = new Pixmap(20, 20, Pixmap.Format.RGBA8888);
        p.setColor(Color.BLUE);
        p.fill();
        wallTex = new Texture(p);

        // Игрок
        p.setColor(Color.YELLOW);
        p.fill();
        pacmanTex = new Texture(p);

        // Другие игроки / Точки
        p.setColor(1f, 0.7f, 0.3f, 1);
        p.fill();
        otherPacTex = new Texture(p);
        p.dispose();
    }

    private void startClientNetwork() {
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
                                ch.pipeline().addLast(new ClientHandler(PacmanGame.this));
                            }
                        });
                channel = b.connect("localhost", 8080).sync().channel();
                myId = "PLR_" + (int)(Math.random() * 1000);
                channel.closeFuture().sync();
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

        handleMovement(Gdx.graphics.getDeltaTime());

        batch.begin();
        drawMap();
        drawPlayers();
        drawUI();
        batch.end();
    }

    private void handleMovement(float deltaTime) {
        if (progress < 1.0f) {
            progress += deltaTime * 5;
            progress = Math.min(progress, 1.0f);
            visualX = (currX + (targetX - currX) * progress) * 20;
            visualY = (currY + (targetY - currY) * progress) * 20;
        } else {
            currX = targetX;
            currY = targetY;

            if (channel != null && channel.isActive()) {
                channel.writeAndFlush(new PacketPlayerPos(currX, currY, myId, myScore));
            }

            int dx = 0, dy = 0;
            if (Gdx.input.isKeyPressed(Keys.W)) dy = 1;
            else if (Gdx.input.isKeyPressed(Keys.S)) dy = -1;
            else if (Gdx.input.isKeyPressed(Keys.D)) dx = 1;
            else if (Gdx.input.isKeyPressed(Keys.A)) dx = -1;

            if ((dx != 0 || dy != 0) && map != null && !map.isWall(currX + dx, currY + dy)) {
                if (map.getCell(currX + dx, currY + dy) == '.') {
                    myScore += 10;
                }
                targetX = currX + dx;
                targetY = currY + dy;
                progress = 0;
            }
        }
    }

    private void drawMap() {
        if (map == null) return;
        for (int y = 0; y < map.getHeight(); y++) {
            for (int x = 0; x < map.getWidth(); x++) {
                if (map.getCell(x, y) == '#') {
                    batch.draw(wallTex, x * 20, y * 20);
                } else if (map.getCell(x, y) == '.') {
                    batch.draw(otherPacTex, x * 20 + 8, y * 20 + 8, 4, 4);
                }
            }
        }
    }

    private void drawPlayers() {
        batch.setColor(Color.WHITE);

        for (PacketPlayerPos op : otherPlayers) {
            if (op.id.equals(myId)) continue;
            batch.draw(otherPacTex, op.x * 20, op.y * 20);
        }
        batch.draw(pacmanTex, visualX, visualY);
    }

    private void drawUI() {
        List<PacketPlayerPos> leaderBoard = new ArrayList<>(otherPlayers);
        // Добавляем себя, если нас еще нет в списке
        if (leaderBoard.stream().noneMatch(p -> p.id.equals(myId))) {
            leaderBoard.add(new PacketPlayerPos(currX, currY, "YOU", myScore));
        }

        leaderBoard.sort((p1, p2) -> Integer.compare(p2.score, p1.score));

        int offset = 20;
        for (int i = 0; i < leaderBoard.size(); i++) {
            PacketPlayerPos p = leaderBoard.get(i);
            boolean isMe = p.id.equals("YOU") || p.id.equals(myId);

            font.setColor(isMe ? Color.YELLOW : Color.ORANGE);
            String name = isMe ? "MY SCORE" : "PLAYER " + p.id.substring(0, Math.min(p.id.length(), 5));
            font.draw(batch, (i + 1) + ". " + name + ": " + p.score, 20, Gdx.graphics.getHeight() - offset);
            offset += 25;

            if (p.score >= 500) drawWinMessage(isMe, name);
        }
    }

    private void drawWinMessage(boolean isMe, String name) {
        font.setColor(isMe ? Color.GREEN : Color.RED);
        font.getData().setScale(2.0f);
        String msg = isMe ? "YOU WIN!" : name + " WINS!";
        font.draw(batch, msg, Gdx.graphics.getWidth() / 4f, Gdx.graphics.getHeight() / 2f);
        font.getData().setScale(1.2f);
    }

    @Override
    public void dispose() {
        if (channel != null) channel.close();
        batch.dispose();
        wallTex.dispose();
        pacmanTex.dispose();
        otherPacTex.dispose();
        font.dispose();
        System.exit(0);
    }

    public void updateOtherPlayers(List<PacketPlayerPos> players) { this.otherPlayers = players; }
    public void setMap(GameMap newMap) { this.map = newMap; }
}