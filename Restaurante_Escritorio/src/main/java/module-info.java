module com.example.restaurante_escritorio {
    requires javafx.controls;
    requires javafx.fxml;
    requires com.google.gson;
    requires java.net.http;


    opens com.example.restaurante_escritorio to javafx.fxml;
    exports com.example.restaurante_escritorio;
}