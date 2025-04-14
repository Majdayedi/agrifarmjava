module org.example.java1 {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.web;

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires net.synedra.validatorfx;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.bootstrapfx.core;
    requires jbcrypt;
    requires com.google.gson;
    requires java.sql;

    opens org.example.java1 to javafx.fxml;
    opens org.example.java1.controllers to javafx.fxml;
    opens org.example.java1.entity to javafx.base;

    exports org.example.java1;
    exports org.example.java1.controllers;
    exports org.example.java1.entity;  // Add this line to export the entity package
}
