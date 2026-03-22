package com.pacman.client;

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;

public class DesktopLauncher {
    public static void main(String[] args) {
        Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
        config.setTitle("Pacman Multiplayer");

        // Устанавливаем размер окна точно под карту
        config.setWindowedMode(460, 440);

        config.setForegroundFPS(60);
        new Lwjgl3Application(new PacmanGame(), config);
    }
}