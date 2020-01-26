package com.larryhsiao.auxo.controller;

import com.google.gson.JsonObject;
import com.larryhsiao.auxo.devices.TkFileHeads;
import com.larryhsiao.auxo.devices.TkFiles;
import com.larryhsiao.auxo.utils.dialogs.ExceptionAlert;
import com.silverhetch.clotho.Source;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.util.Callback;
import org.takes.facets.fork.FkMethods;
import org.takes.facets.fork.FkRegex;
import org.takes.facets.fork.TkFork;
import org.takes.http.FtBasic;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.net.*;
import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;

/**
 * Controller for device list.
 */
public class Devices implements Initializable, Closeable {
    private static final int PORT_DISCOVERING = 24000;
    private static final int PORT_API = 24001;
    private final Source<Connection> db;
    private final File root;
    private boolean running = true;
    private DatagramSocket socket = null;
    private Map<String, Target> targetData = new HashMap<>();
    @FXML private ListView<Target> listView;

    public Devices(Source<Connection> db, File root) {
        this.db = db;
        this.root = root;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        try {
            listView.setCellFactory(new Callback<>() {
                @Override
                public ListCell<Target> call(ListView<Target> param) {
                    return new ListCell<>() {
                        @Override
                        protected void updateItem(Target item, boolean empty) {
                            super.updateItem(item, empty);
                            if (empty) {
                                setText("");
                            } else {
                                setText(item.address().toString());
                            }
                        }
                    };
                }
            });
            socket = new DatagramSocket(PORT_DISCOVERING);
            receivingPacket();
            sendingPacket(resources);

            new Thread(() -> {
                try {
                    new FtBasic(
                        new TkFork(
                            new FkRegex(".+", new TkFork(
                                new FkMethods("GET", new TkFiles(db, root)),
                                new FkMethods("HEAD", new TkFileHeads(root))
                            ))
                        ), PORT_API
                    ).start(() -> !running);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }).start();
        } catch (IOException e) {
            new ExceptionAlert(e, resources).fire();
        }
    }

    private void sendingPacket(ResourceBundle resources) {
        new Thread(() -> {
            while (running) {
                try {
                    NetworkInterface.getNetworkInterfaces().asIterator()
                        .forEachRemaining(networkInterface -> {
                            sendPackage(networkInterface, resources);
                        });
                    Thread.sleep(1000);
                } catch (SocketException | InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private void sendPackage(NetworkInterface ni, ResourceBundle res) {
        try {
            if (!ni.isUp() || ni.isLoopback()) {
                return;
            }
            for (InterfaceAddress addr : ni.getInterfaceAddresses()) {
                if (addr.getBroadcast() != null) {
                    String msg = packetMsg();
                    socket.send(new DatagramPacket(
                        msg.getBytes(), 0, msg.length(),
                        addr.getBroadcast(), PORT_DISCOVERING
                    ));
                }
            }
        } catch (Exception e) {
            new ExceptionAlert(e, res).fire();
        }
    }

    private String packetMsg() {
        try {
            var json = new JsonObject();
            json.addProperty("name", InetAddress.getLocalHost().getHostName());
            json.addProperty("port", PORT_API);
            return json.toString();
        } catch (Exception e) {
            return "I don't know who i am.";
        }
    }

    private void receivingPacket() {
        new Thread(() -> {
            try {
                while (running) {
                    final byte[] buffer = new byte[1024];
                    final DatagramPacket packet =
                        new DatagramPacket(buffer, 1024);
                    socket.receive(packet);
                    targetData.put(
                        packet.getAddress().toString(),
                        new ConstTarget(
                            packet.getAddress(),
                            new String(buffer).trim()
                        )
                    );
                    Platform.runLater(this::updateList);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void updateList() {
        listView.getItems().clear();
        listView.getItems().addAll(targetData.values());
    }

    @Override
    public void close() throws IOException {
        running = false;
        socket.close();
    }

    /**
     * Constant implementation of {@link Target}
     */
    private static class ConstTarget implements Target {
        private final InetAddress address;
        private final String message;

        private ConstTarget(InetAddress address, String message) {
            this.address = address;
            this.message = message;
        }

        @Override
        public InetAddress address() {
            return address;
        }

        @Override
        public String title() {
            return message;
        }
    }

    /**
     * The founded remote device
     */
    private interface Target {

        /**
         * @return The address that we received message from.
         */
        InetAddress address();

        /**
         * The title of this device target.
         */
        String title();
    }
}
