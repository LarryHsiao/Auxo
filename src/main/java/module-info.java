module auxo {
    requires javafx.controls;
    requires javafx.fxml;
    requires clotho;
    requires java.sql;
    requires java.desktop;

    opens com.larryhsiao.auxo.controller to javafx.fxml;
    exports com.larryhsiao.auxo;
}