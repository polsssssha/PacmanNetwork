package com.pacman.client;

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;

public class DesktopLauncher {
    public static void main(String[] args) {
        Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();

        config.setTitle("Pacman Multiplayer");
        config.setWindowedMode(460, 440);
        config.setForegroundFPS(60);
        config.useVsync(true);

        new Lwjgl3Application(new PacmanGame(), config);
    }
}