package com.example.restaurante_escritorio;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class MesasController {

    @FXML private Pane paneMesa1, paneMesa2, paneMesa3, paneMesa4, paneMesa5;
    @FXML private Circle circleMesa1, circleMesa2, circleMesa3, circleMesa4, circleMesa5;

    private final Map<Integer, Circle> circleMap = new HashMap<>(); // Mapa para asociar un ID de mesa con su objeto Círculo.
    private final Map<String, Integer> paneToMesaIdMap = new HashMap<>(); // Mapa para asociar el ID de un pane con su ID de mesa.

    private final Gson gson = new Gson();
    private ScheduledExecutorService scheduler;

    private final Color COLOR_LIBRE = Color.web("#28A745");
    private final Color COLOR_ESPERA = Color.web("#FFC107");
    private final Color COLOR_OCUPADO = Color.web("#DC3545");

    public static class EstadoMesa {
        public int mesaId;
        public String estado;
    }

    @FXML
    public void initialize() {
        circleMap.put(1, circleMesa1);
        circleMap.put(2, circleMesa2);
        circleMap.put(3, circleMesa3);
        circleMap.put(4, circleMesa4);
        circleMap.put(5, circleMesa5);

        paneToMesaIdMap.put("paneMesa1", 1);
        paneToMesaIdMap.put("paneMesa2", 2);
        paneToMesaIdMap.put("paneMesa3", 3);
        paneToMesaIdMap.put("paneMesa4", 4);
        paneToMesaIdMap.put("paneMesa5", 5);

        // Configura e inicia el planificador para refrescar los estados
        scheduler = Executors.newSingleThreadScheduledExecutor();
        scheduler.scheduleAtFixedRate(this::refrescarEstados, 0, 3, TimeUnit.SECONDS);
    }

    private void refrescarEstados() {
        ApiClient.obtenerMesasConPedido().thenAccept(json -> {
            Type listType = new TypeToken<List<EstadoMesa>>() {}.getType();
            List<EstadoMesa> mesasConPedido = gson.fromJson(json, listType);
            Platform.runLater(() -> actualizarCirculos(mesasConPedido));
        }).exceptionally(e -> {
            System.err.println("Error al refrescar estados: " + e.getMessage());
            return null;
        });
    }

    private void actualizarCirculos(List<EstadoMesa> mesasConPedido) {
        for (Circle circle : circleMap.values()) {
            circle.setFill(COLOR_LIBRE);
        }

        for (EstadoMesa mesa : mesasConPedido) {
            Circle circle = circleMap.get(mesa.mesaId);
            if (circle != null) {
                switch (mesa.estado) {
                    case "abierto":
                        circle.setFill(COLOR_ESPERA);
                        break;
                    case "confirmado":
                        circle.setFill(COLOR_OCUPADO);
                        break;
                    default:
                        circle.setFill(Color.GREY);
                        break;
                }
            }
        }
    }

    @FXML
    private void manejarClickMesa(MouseEvent event) {
        Pane clickedPane = (Pane) event.getSource();
        String paneId = clickedPane.getId();
        Integer mesaId = paneToMesaIdMap.get(paneId);

        if (mesaId == null || mesaId == -1) return;

        Circle circle = circleMap.get(mesaId);
        if (circle != null && circle.getFill().equals(COLOR_LIBRE)) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Mesa Libre");
            alert.setHeaderText(null);
            alert.setContentText("Esta mesa está libre y no tiene ningún pedido asociado.");
            alert.showAndWait();
            return;
        }

        // Bloquea la actualizacion de la UI
        if (scheduler != null && !scheduler.isShutdown()) {
            scheduler.shutdown();
        }

        ApiClient.obtenerPedidoCompleto(mesaId).thenAccept(json -> {
            Pedido pedidoCompleto = gson.fromJson(json, Pedido.class);
            Platform.runLater(() -> {
                try {
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/restaurante_escritorio/pedido.fxml"));
                    Scene scene = new Scene(loader.load());
                    Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();

                    PedidoController controller = loader.getController();
                    controller.setPedido(mesaId, pedidoCompleto.getItems(), pedidoCompleto.getEstado());

                    stage.setScene(scene);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        }).exceptionally(e -> {
            System.err.println("Error al obtener pedido completo: " + e.getMessage());
            Platform.runLater(() -> {
                if (scheduler.isShutdown()) {
                    scheduler = Executors.newSingleThreadScheduledExecutor();
                    scheduler.scheduleAtFixedRate(this::refrescarEstados, 0, 3, TimeUnit.SECONDS);
                }
            });
            return null;
        });
    }
}
