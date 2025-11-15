package com.example.restaurante_escritorio;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.CompletableFuture;

public class ApiClient {

    private static final String BASE_URL = "http://localhost:3000";

    private static final HttpClient client = HttpClient.newHttpClient();

    // Petición GET a /pedidos/estados para obtener una lista de todas las mesas que tienen un pedido activo y el estado de dicho pedido.
    public static CompletableFuture<String> obtenerMesasConPedido() {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/pedidos/estados"))
                .build();

        return client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(HttpResponse::body);
    }

    // Petición GET a /pedidos/completo/{mesaId} para obtener todos los detalles de un pedido específico.
    public static CompletableFuture<String> obtenerPedidoCompleto(int mesaId) {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/pedidos/completo/" + mesaId))
                .build();
        return client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(HttpResponse::body);
    }

    // Realiza una petición PUT a /pedidos/confirmar/{mesaId} para cambiar el estado de un pedido de "abierto" a "confirmado" en el servidor.
    public static CompletableFuture<HttpResponse<String>> confirmarPedido(int mesaId) {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/pedidos/confirmar/" + mesaId))
                .PUT(HttpRequest.BodyPublishers.noBody())
                .build();
        return client.sendAsync(request, HttpResponse.BodyHandlers.ofString());
    }
}
