package com.pacman.common;

import java.io.Serializable;
import java.util.List;

public class PacketAllPlayers implements Serializable {
    public List<PacketPlayerPos> players;

    public PacketAllPlayers(List<PacketPlayerPos> players) {
        this.players = players;
    }
}