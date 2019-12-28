package com.larryhsiao.auxo.utils;

import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;

public class SingleMediaPlayer {
    private static MediaPlayer player = null;

    public static void release() {
        if (player != null) {
            player.stop();
            player.dispose();
            player = null;
        }
    }

    public static MediaPlayer newPlayer(String url) {
        release();
        player = new MediaPlayer(new Media(url));
        return player;
    }
}
