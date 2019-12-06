module auxo {
    requires javafx.base;
    requires javafx.controls;
    requires javafx.fxml;
    requires clotho;
    requires java.sql;
    requires java.desktop;
    requires org.controlsfx.controls;

    opens com.larryhsiao.auxo.controller to javafx.fxml;
    exports com.larryhsiao.auxo;
}