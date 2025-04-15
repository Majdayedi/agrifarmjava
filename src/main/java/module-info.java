module org.example.piarticle {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires com.google.protobuf;

    // Export main package
    exports org.example.piarticle;

    // Export controller package
    exports org.example.piarticle.Controllers;

    // Open packages for FXML and reflection
    opens org.example.piarticle to javafx.fxml;
    opens org.example.piarticle.Controllers to javafx.fxml;
}