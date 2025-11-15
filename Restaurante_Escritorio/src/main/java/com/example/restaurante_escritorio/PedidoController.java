package com.example.restaurante_escritorio;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

public class PedidoController {

    @FXML private Label lblMesa;
    @FXML private TableView<ItemPedidoView> tablePedido;
    @FXML private TableColumn<ItemPedidoView, String> colNombre;
    @FXML private TableColumn<ItemPedidoView, Integer> colCantidad;
    @FXML private TableColumn<ItemPedidoView, Double> colPrecio;
    @FXML private Button btnConfirmar;

    private int numeroMesa;

    public static class ItemPedidoView {
        private String nombre;
        private int cantidad;
        private double precio;

        public ItemPedidoView(String nombre, int cantidad, double precio) {
            this.nombre = nombre;
            this.cantidad = cantidad;
            this.precio = precio;
        }
        public String getNombre() {
            return nombre;
        }
        public int getCantidad() {
            return cantidad;
        }
        public double getPrecio() {
            return precio;
        }
    }

    public void setPedido(int mesa, List<Pedido.ItemPedidoData> items, String estado) {
        this.numeroMesa = mesa;
        lblMesa.setText("Pedido Mesa " + mesa + " - Estado: " + estado.toUpperCase());

        // Busca un getter correspondiente al nombre que se le pasa para obtener los valores
        colNombre.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        colCantidad.setCellValueFactory(new PropertyValueFactory<>("cantidad"));
        colPrecio.setCellValueFactory(new PropertyValueFactory<>("precio"));

        List<ItemPedidoView> itemsParaVista = items.stream()
                .map(item -> new ItemPedidoView(item.producto.nombre, item.cantidad, item.producto.precio))
                .collect(Collectors.toList());

        tablePedido.setItems(FXCollections.observableArrayList(itemsParaVista));

        boolean esVisible = "abierto".equals(estado);
        btnConfirmar.setVisible(esVisible);
        btnConfirmar.setManaged(esVisible);
    }

    @FXML
    private void confirmarPedido(ActionEvent event) {
        ApiClient.confirmarPedido(numeroMesa).thenAccept(response -> {
            Platform.runLater(() -> {
                if (response.statusCode() == 200) {
                    Alert alert = new Alert(Alert.AlertType.INFORMATION, "Pedido confirmado con éxito.");
                    alert.showAndWait();
                    try {
                        volverAMesas();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } else {
                    Alert alert = new Alert(Alert.AlertType.ERROR, "Error al confirmar: " + response.body());
                    alert.show();
                }
            });
        }).exceptionally(e -> {
            Platform.runLater(() -> {
                Alert alert = new Alert(Alert.AlertType.ERROR, "Fallo de conexión: " + e.getMessage());
                alert.show();
            });
            return null;
        });
    }

    @FXML
    private void volverAMesas() throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/restaurante_escritorio/mesas.fxml"));
        Scene scene = new Scene(loader.load());
        Stage stage = (Stage) lblMesa.getScene().getWindow();
        stage.setScene(scene);
    }
}
