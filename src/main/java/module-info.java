module com.example.courseworkdemo1 {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.management;
    requires com.sun.jna;
    requires com.sun.jna.platform;
    requires javax.websocket.api;
    requires java.sql;


    opens com.example.courseworkdemo1 to javafx.fxml;
    exports com.example.courseworkdemo1;
}