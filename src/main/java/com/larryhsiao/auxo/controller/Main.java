package com.larryhsiao.auxo.controller;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.larryhsiao.auxo.controller.devices.Devices;
import com.larryhsiao.auxo.dialogs.ExceptionAlert;
import com.silverhetch.clotho.Source;
import com.silverhetch.clotho.file.TextFile;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.sql.Connection;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.ResourceBundle;

import static java.util.Locale.US;
import static javafx.scene.input.MouseEvent.MOUSE_PRESSED;
import static javafx.stage.StageStyle.UNDECORATED;

/**
 * Controller for entry page of Auxo.
 */
public class Main implements Initializable, Closeable {
    private static final int PAGE_TAG_MANAGEMENT = 1;
    private static final int PAGE_FILE_MANAGEMENT = 2;
    private static final int PAGE_DEVICES = 3;

    private final File root;
    private final Source<Connection> db;
    private int currentPage = -1;
    private Object currentPageController = null;
    @FXML private Button tagManagement;
    @FXML private Button fileManagement;
    @FXML private Button devices;
    @FXML private Button about;
    @FXML private AnchorPane content;
    @FXML private Button nyxImport;

    public Main(File root, Source<Connection> db) {
        this.root = root;
        this.db = db;
    }

    @Override
    public void initialize(URL location, ResourceBundle res) {
        tagManagement.setText(res.getString("tag_management"));
        tagManagement.setOnAction(event -> loadTagManagement(res));
        fileManagement.setText(res.getString("file_management"));
        fileManagement.setOnAction(event -> loadFileList(res));
        loadFileList(res);
        fileManagement.setOnAction(event -> loadFileList(res));
        devices.setText(res.getString("devices"));
        devices.setOnAction(event -> loadDevices(res));
        about.setOnAction(event -> loadAbout(res));
        about.setText(res.getString("about"));
        nyxImport.setText("Nyx import");
        nyxImport.setOnAction(event -> {
            try {
                var client = new OkHttpClient();
                var response = client.newCall(new Request.Builder()
                    .url("http://192.168.0.101:8080/diaries")
                    .build()
                ).execute();
                var array = JsonParser.parseString(
                    response.body().string()
                ).getAsJsonArray();
                for (int i = 0; i < array.size(); i++) {
                    var obj = array.get(i).getAsJsonObject();
                    var content = obj.get("title").getAsString();
                    var formatter = new SimpleDateFormat("yyyy_MM_dd_HH_mm", US);
                    var date = new Date();
                    date.setTime(obj.get("time").getAsLong());
                    var dir = new File(root, formatter.format(date));
//                    if (dir.exists()){
//                        continue;
//                    }
                    dir.mkdir();
                    new TextFile(
                        new File(dir, "content.txt"), content).value();
                    System.out.println("" + formatter.format(date));
                    System.out.println(content);
                    for (JsonElement attach : obj.get("attachments")
                        .getAsJsonArray()) {
                        var attachment = attach.getAsString();
                        loadAttachment(dir, attachment);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        loadFileList(res);
    }

    private void loadAttachment(File dir, String attachment)
        throws IOException {
        var client = new OkHttpClient();
        var res = client.newCall(new Request.Builder()
            .url(HttpUrl.parse("http://192.168.0.101:8080/files/")
            .newBuilder()
                .addPathSegment(attachment)
                .build())
            .build())
            .execute();
        String fileName;
        if (attachment.startsWith("geo")){
            new File(dir, attachment).createNewFile();
        }else {
            Files.write(new File(dir,attachment+".jpeg" ).toPath(), res.body().bytes());
        }
    }

    private void loadAbout(ResourceBundle res) {
        try {
            final Scene scene = new Scene(FXMLLoader.load(
                getClass().getResource("/com/larryhsiao/auxo/about.fxml"),
                res
            ));
            scene.getStylesheets().addAll(content.getScene().getStylesheets());
            final Stage stage = new Stage();
            stage.initStyle(UNDECORATED);
            stage.setScene(scene);
            stage.addEventHandler(MOUSE_PRESSED, event -> stage.close());
            stage.focusedProperty().addListener(
                (observable, oldValue, newValue) -> {
                    if (!newValue) {
                        stage.close();
                    }
                });
            stage.showAndWait();
        } catch (IOException e) {
            new ExceptionAlert(e, res).fire();
        }
    }

    private void loadDevices(ResourceBundle res) {
        try {
            if (currentPage == PAGE_DEVICES) {
                return;
            }
            tearDownCurrentController(res);
            currentPage = PAGE_DEVICES;
            final FXMLLoader loader = new FXMLLoader(
                getClass().getResource(
                    "/com/larryhsiao/auxo/devices/devices.fxml"), res);
            currentPageController = new Devices(db, root);
            loader.setController(currentPageController);
            Parent parent = loader.load();
            content.getChildren().clear();
            content.getChildren().add(parent);
        } catch (IOException e) {
            new ExceptionAlert(e, res).fire();
        }
    }

    private void loadFileList(ResourceBundle res) {
        try {
            if (currentPage == PAGE_FILE_MANAGEMENT) {
                return;
            }
            tearDownCurrentController(res);
            currentPage = PAGE_FILE_MANAGEMENT;
            content.getChildren().clear();
            final FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/com/larryhsiao/auxo/file_list.fxml"),
                res
            );
            loader.setController(new FileList(root, db));
            content.getChildren().add(loader.load());
        } catch (IOException e) {
            new ExceptionAlert(e, res).fire();
        }
    }

    private void loadTagManagement(ResourceBundle res) {
        try {
            if (currentPage == PAGE_TAG_MANAGEMENT) {
                return;
            }
            tearDownCurrentController(res);
            currentPage = PAGE_TAG_MANAGEMENT;
            content.getChildren().clear();

            final FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/com/larryhsiao/auxo/tags.fxml"),
                res
            );
            loader.setController(new TagList(root, db));
            content.getChildren().add(loader.load());
        } catch (IOException e) {
            new ExceptionAlert(e, res).fire();
        }
    }

    @Override
    public void close() throws IOException {
        tearDownCurrentController(null);
    }

    private void tearDownCurrentController(ResourceBundle res) {
        try {
            if (currentPageController != null &&
                currentPageController instanceof Closeable) {
                ((Closeable) currentPageController).close();
            }
            currentPageController = null;
        } catch (IOException e) {
            if (res != null) {
                new ExceptionAlert(e, res).fire();
            } else {
                e.printStackTrace();
            }
        }
    }
}
