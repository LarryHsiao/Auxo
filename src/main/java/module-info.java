module auxo {
    requires javafx.base;
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.media;
    requires clotho;
    requires java.sql;
    requires java.desktop;
    requires org.controlsfx.controls;
    requires kotlin.stdlib.jdk8;
    requires kotlin.stdlib;
    requires javafx.swing;
    requires juno;
    requires takes;
    requires com.google.gson;

    opens com.larryhsiao.auxo.controller to javafx.fxml;
    exports com.larryhsiao.auxo;
}