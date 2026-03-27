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
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.MathUtils;
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
    private ShapeRenderer shapeRenderer;
    private Texture dotTex;
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

    // Новые переменные для графики
    private float rotation = 0;
    private float mouthAnimationTime = 0;

    @Override
    public void create() {
        batch = new SpriteBatch();
        shapeRenderer = new ShapeRenderer();

        // Создаем маленькую текстуру точки для еды
        Pixmap p = new Pixmap(4, 4, Pixmap.Format.RGBA8888);
        p.setColor(1f, 0.7f, 0.3f, 1);
        p.fillCircle(2, 2, 2);
        dotTex = new Texture(p);
        p.dispose();

        map = new GameMap(23, 22);
        font = new BitmapFont();
        font.getData().setScale(1.2f);

        startClientNetwork();
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
        float deltaTime = Gdx.graphics.getDeltaTime();
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        handleMovement(deltaTime);

        // Рисуем геометрию (Стены и Пакманы)
        // Рисуем контуры стен
        shapeRenderer.begin(ShapeType.Line);
        Gdx.gl.glLineWidth(2);
        drawMapShapes();
        shapeRenderer.end();

        // Рисуем залитых пакманов
        shapeRenderer.begin(ShapeType.Filled);
        drawMyPacman();
        drawOtherPlayersShapes();
        shapeRenderer.end();

        // Рисуем текстуры и текст
        batch.begin();
        drawDots();
        drawUI();
        batch.end();
    }

    private void handleMovement(float deltaTime) {
        mouthAnimationTime += deltaTime;

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

            boolean up = Gdx.input.isKeyPressed(Keys.W);
            boolean down = Gdx.input.isKeyPressed(Keys.S);
            boolean left = Gdx.input.isKeyPressed(Keys.A);
            boolean right = Gdx.input.isKeyPressed(Keys.D);

            int dx = 0, dy = 0;

            // Сначала определяем желаемое направление
            if (up) dy = 1;
            else if (down) dy = -1;

            if (right) dx = 1;
            else if (left) dx = -1;

            // Сначала пробуем поехать по вертикали
            if (dy != 0 && map != null && !map.isWall(currX, currY + dy)) {
                targetY = currY + dy;
                targetX = currX; // Фиксируем X
                progress = 0;
                // Устанавливаем поворот для вертикали
                rotation = (dy == 1) ? 90 : 270;
            }
            // Если по вертикали не поехали (стенка или не нажато), пробуем горизонталь
            else if (dx != 0 && map != null && !map.isWall(currX + dx, currY)) {
                targetX = currX + dx;
                targetY = currY; // Фиксируем Y
                progress = 0;
                // Устанавливаем поворот для горизонтали
                rotation = (dx == 1) ? 0 : 180;
            }

            //  Сбор очков
            if (progress == 0 && map != null && map.getCell(targetX, targetY) == '.') {
                myScore += 10;
            }
        }
    }

    private void drawMapShapes() {
        if (map == null) return;
        shapeRenderer.setColor(Color.BLUE);
        for (int y = 0; y < map.getHeight(); y++) {
            for (int x = 0; x < map.getWidth(); x++) {
                if (map.getCell(x, y) == '#') {
                    // Рисуем пустой прямоугольник — синий контур стены
                    shapeRenderer.rect(x * 20 + 1, y * 20 + 1, 18, 18);
                }
            }
        }
    }

    private void drawMyPacman() {
        shapeRenderer.setColor(Color.YELLOW);

        float mouthAngle;
        // Если progress < 1.0f, значит мы в процессе перехода между клетками
        if (progress < 1.0f) {
            mouthAngle = 20 + 20 * MathUtils.sin(mouthAnimationTime * 15);
        } else {
            mouthAngle = 15; // В покое рот приоткрыт
        }

        float startAngle = rotation + mouthAngle;
        float degrees = 360 - mouthAngle * 2;
        shapeRenderer.arc(visualX + 10, visualY + 10, 9, startAngle, degrees);
    }

    private void drawOtherPlayersShapes() {
        shapeRenderer.setColor(Color.ORANGE);
        for (PacketPlayerPos op : otherPlayers) {
            if (op.id.equals(myId)) continue;

            float otherMouth = 15 + 15 * MathUtils.sin(mouthAnimationTime * 10);
            shapeRenderer.arc(op.x * 20 + 10, op.y * 20 + 10, 8, otherMouth, 360 - otherMouth * 2);
        }
    }

    private void drawDots() {
        if (map == null) return;
        batch.setColor(Color.WHITE);
        for (int y = 0; y < map.getHeight(); y++) {
            for (int x = 0; x < map.getWidth(); x++) {
                if (map.getCell(x, y) == '.') {
                    batch.draw(dotTex, x * 20 + 8, y * 20 + 8);
                }
            }
        }
    }

    private void drawUI() {
        List<PacketPlayerPos> leaderBoard = new ArrayList<>(otherPlayers);
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
        shapeRenderer.dispose();
        dotTex.dispose();
        font.dispose();
        System.exit(0);
    }

    public void setStartPosition(int x, int y) {
        this.currX = x; this.currY = y;
        this.targetX = x; this.targetY = y;
        this.visualX = x * 20; this.visualY = y * 20;
        this.progress = 1.0f;
    }

    public void updateOtherPlayers(List<PacketPlayerPos> players) { this.otherPlayers = players; }
    public void setMap(GameMap newMap) { this.map = newMap; }
}