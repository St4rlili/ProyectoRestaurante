package com.example.restaurante_escritorio;

import java.util.List;

public class Pedido {

    public int mesaId;
    public String estado;
    public List<ItemPedidoData> items;

    public static class ItemPedidoData {
        public PedidoModel producto;
        public int cantidad;
    }

    public static class PedidoModel {
        public String nombre;
        public double precio;
    }


    public int getMesaId() {
        return mesaId;
    }

    public String getEstado() {
        return estado;
    }

    public List<ItemPedidoData> getItems() {
        return items;
    }
}
