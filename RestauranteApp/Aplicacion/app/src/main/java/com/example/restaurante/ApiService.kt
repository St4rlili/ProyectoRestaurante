package com.example.restaurante

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

// Representa un único ítem dentro de un pedido

data class ItemPedido(
    val producto: PedidoModel,
    var cantidad: Int
)


// Carga de datos que se envía al servidor al crear un pedido nuevo.
data class PedidoPayload(
    val mesaId: Int,
    val items: List<ItemPedido>
)

// Carga de datos que se envía al servidor para añadir nuevos ítems a un pedido ya existente.
data class ItemsPayload(
    val items: List<ItemPedido>
)

// Respuesta de la API cuando se pregunta si una mesa ya tiene un pedido.
data class RespuestaPedidoExistente(
    val existe: Boolean
)

// Modela la estructura completa de un pedido cuando se recibe del servidor.

data class PedidoCompleto(
    val _id: String,
    val mesaId: Int,
    val items: List<ItemPedido>,
    val estado: String,
    val fecha: String
)

// Modela la respuesta del servidor cuando se pregunta solo por el estado de un pedido.
data class EstadoPedidoResponse(
    val estado: String
)

// Modela la respuesta del servidor para un único estado de mesa.
data class EstadoMesa(
    val mesaId: Int,
    val estado: String
)

interface ApiService {

    // Endpoint para obtener la lista de todos los platos.
    @GET("platos")
    suspend fun getPlatos(): List<PedidoModel>

    // Endpoint para obtener la lista de todas las bebidas.

    @GET("bebidas")
    suspend fun getBebidas(): List<PedidoModel>

    // Endpoint para obtener el estado de todas las mesas que tienen un pedido.
    @GET("pedidos/estados")
    suspend fun getEstadosMesas(): Response<List<EstadoMesa>>

    // Endpoint para verificar si ya existe un pedido para una mesa específica.
    @GET("pedidos/{mesaId}")
    suspend fun verificarPedidoExistente(@Path("mesaId") mesaId: Int): Response<RespuestaPedidoExistente>

    // Endpoint para obtener únicamente el estado de un pedido para una mesa.
    @GET("pedidos/estado/{mesaId}")
    suspend fun obtenerEstadoPedido(@Path("mesaId") mesaId: Int): Response<EstadoPedidoResponse>

    // Endpoint para obtener todos los detalles de un pedido de una mesa.
    @GET("pedidos/completo/{mesaId}")
    suspend fun obtenerPedidoPorMesa(@Path("mesaId") mesaId: Int): Response<PedidoCompleto>

    //Endpoint para crear un nuevo pedido.
    @POST("pedidos")
    suspend fun crearPedido(@Body pedido: PedidoPayload): Response<Unit>

    // Endpoint para actualizar un pedido existente (añadir más ítems).
    @PUT("pedidos/{mesaId}")
    suspend fun actualizarPedido(@Path("mesaId") mesaId: Int, @Body items: ItemsPayload): Response<Unit>

    // Endpoint para borrar un pedido de la base de datos (cuando se paga).
    @DELETE("pedidos/{mesaId}")
    suspend fun borrarPedido(@Path("mesaId") mesaId: Int): Response<Unit>
}
