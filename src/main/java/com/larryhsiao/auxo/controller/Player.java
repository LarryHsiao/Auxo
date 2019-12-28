package com.larryhsiao.auxo.controller;

import com.larryhsiao.auxo.utils.SingleMediaPlayer;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Slider;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.util.Duration;

import java.net.URL;
import java.util.ResourceBundle;

import static javafx.scene.input.MouseEvent.MOUSE_PRESSED;
import static javafx.scene.input.MouseEvent.MOUSE_RELEASED;
import static javafx.scene.media.MediaPlayer.Status.PLAYING;

/**
 * Controller for player.
 */
public class Player implements Initializable {
    private final String mediaUri;
    private final MediaPlayer player;

    @FXML private StackPane root;
    @FXML private MediaView playerView;
    @FXML private ImageView playback;
    @FXML private Slider progress;

    public Player(String mediaUri) {
        this.mediaUri = mediaUri;
        this.player = SingleMediaPlayer.newPlayer(mediaUri);
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
        player.totalDurationProperty().addListener(event -> {
            progress.setMax(player.getTotalDuration().toMillis());
        });
        player.currentTimeProperty().addListener(
            (observable, oldValue, newValue) -> {
                if (!progress.isValueChanging()) {
                    progress.setValue(newValue.toMillis());
                }
            });
        progress.valueProperty().addListener(
            (observable, oldValue, newValue) -> {
                if (progress.isValueChanging()) {
                    player.seek(new Duration(newValue.doubleValue()));
                }
            });
        progress.addEventHandler(MOUSE_PRESSED, event -> {
            progress.setValueChanging(true);
        });
        progress.addEventHandler(MOUSE_RELEASED, event -> {
            player.seek(new Duration(progress.getValue()));
            progress.setValueChanging(false);
        });
    }
}
