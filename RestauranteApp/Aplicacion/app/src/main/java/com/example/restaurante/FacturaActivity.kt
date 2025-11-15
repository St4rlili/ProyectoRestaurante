package com.example.restaurante

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.launch

class FacturaActivity : ComponentActivity() {

    private lateinit var facturaAdapter: FacturaAdapter
    private var mesaId: Int = -1
    private val itemsFactura = mutableListOf<ItemPedido>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.factura_activity)

        if (intent.hasExtra("MESA_ID")) {
            mesaId = intent.getIntExtra("MESA_ID", -1)
        }

        if (mesaId == -1) {
            Toast.makeText(this, "Error: No se ha proporcionado un ID de mesa válido.", Toast.LENGTH_LONG).show()
            finish()
            return
        }

        val recyclerFactura = findViewById<RecyclerView>(R.id.recyclerFactura)
        val botonPagar = findViewById<Button>(R.id.botonPagar)
        val botonVolver = findViewById<Button>(R.id.botonVolver)

        facturaAdapter = FacturaAdapter(itemsFactura)
        recyclerFactura.layoutManager = LinearLayoutManager(this)
        recyclerFactura.adapter = facturaAdapter

        botonVolver.setOnClickListener {
            finish()
        }

        botonPagar.setOnClickListener {
            mostrarDialogoConfirmacionPago()
        }

        cargarPedidoParaFactura()
    }


    private fun cargarPedidoParaFactura() {
        lifecycleScope.launch {
            try {
                val response = RetrofitClient.instance.obtenerPedidoPorMesa(mesaId)
                if (response.isSuccessful && response.body() != null) {
                    val pedidoCompleto = response.body()!!
                    val itemsAgrupados = agruparItemsDePedido(pedidoCompleto.items)

                    itemsFactura.clear()
                    itemsFactura.addAll(itemsAgrupados)
                    facturaAdapter.notifyDataSetChanged()
                    calcularYMostrarTotal()
                } else {
                    Toast.makeText(this@FacturaActivity, "No se encontró un pedido abierto para esta mesa", Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                Log.e("FacturaActivity", "Error al cargar el pedido", e)
                Toast.makeText(this@FacturaActivity, "Fallo de conexión al cargar el pedido", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun agruparItemsDePedido(items: List<ItemPedido>): List<ItemPedido> {
        if (items.isEmpty()) return emptyList()

        val mapaAgrupado = mutableMapOf<String, ItemPedido>()
        items.forEach { itemActual ->
            val idProducto = itemActual.producto.id
            val itemExistente = mapaAgrupado[idProducto]

            if (itemExistente != null) {
                itemExistente.cantidad += itemActual.cantidad
            } else {
                mapaAgrupado[idProducto] = itemActual.copy(producto = itemActual.producto.copy())
            }
        }
        return mapaAgrupado.values.toList()
    }

    private fun calcularYMostrarTotal() {
        val textoTotalCantidad = findViewById<TextView>(R.id.textoTotalCantidad)
        var total = 0.0
        itemsFactura.forEach { item ->
            total += item.producto.precio * item.cantidad
        }
        textoTotalCantidad.text = String.format("%.2f €", total)
    }

    private fun mostrarDialogoConfirmacionPago() {
        AlertDialog.Builder(this).apply {
            setTitle("Confirmar Pago")
            setMessage("¿Estás seguro de que quieres finalizar y pagar el pedido?")
            setPositiveButton("Sí, Pagar") { _, _ ->
                procesarPago()
            }
            setNegativeButton("Cancelar", null)
            create()
            show()
        }
    }

    private fun procesarPago() {
        lifecycleScope.launch {
            try {
                val response = RetrofitClient.instance.borrarPedido(mesaId)
                if (response.isSuccessful) {
                    Toast.makeText(this@FacturaActivity, "Pago realizado. ¡Gracias!", Toast.LENGTH_LONG).show()
                    val intent = Intent(this@FacturaActivity, MainActivity::class.java)
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK) // Estas flags limpian el historial de pantallas
                    startActivity(intent)
                } else {
                    Toast.makeText(this@FacturaActivity, "Error al procesar el pago en el servidor", Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                Log.e("FacturaActivity", "Error al procesar el pago", e)
                Toast.makeText(this@FacturaActivity, "Fallo de conexión al procesar el pago", Toast.LENGTH_LONG).show()
            }
        }
    }
}
