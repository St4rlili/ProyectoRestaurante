package com.example.restaurante_escritorio;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Launcher extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/restaurante_escritorio/mesas.fxml"));
        Scene scene = new Scene(loader.load(), 680, 500);
        stage.setTitle("Gestión Escritorio");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}
