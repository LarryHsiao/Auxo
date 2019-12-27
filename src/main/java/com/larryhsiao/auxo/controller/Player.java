package com.larryhsiao.auxo.controller;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;

import java.net.URL;
import java.util.ResourceBundle;

import static javafx.scene.media.MediaPlayer.Status.PLAYING;

/**
 * Controller for player.
 */
public class Player implements Initializable {
    private final MediaPlayer player;

    @FXML private StackPane root;
    @FXML private MediaView playerView;
    @FXML private ImageView playback;

    public Player(MediaPlayer player) {
        this.player = player;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        playerView.setMediaPlayer(player);
        playerView.fitWidthProperty().bind(root.widthProperty());
        playerView.fitHeightProperty().bind(root.heightProperty());
        playback.setImage(
            new Image(
                getClass().getResource("/images/play.png").toString()
            )
        );
        playback.setOnMouseClicked(event -> {
            if (player.statusProperty().get() == PLAYING) {
                player.pause();
                playback.setImage(
                    new Image(
                        getClass().getResource("/images/play.png").toString()
                    )
                );
            } else {
                player.play();
                playback.setImage(
                    new Image(
                        getClass().getResource("/images/pause.png").toString()
                    )
                );
            }
        });
    }
}
