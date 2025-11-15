package com.example.restaurante

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.widget.Button
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import retrofit2.Response

class MenuActivity : ComponentActivity() {

    private lateinit var platoAdapter: PedidoAdapter
    private val platoList = mutableListOf<PedidoModel>()
    private lateinit var bebidaAdapter: PedidoAdapter
    private val bebidaList = mutableListOf<PedidoModel>()
    private val pedidoTemporal = mutableMapOf<String, ItemPedido>()
    private var mesaId: Int = -1
    private var hayPedidoAbierto: Boolean = false
    private var confirmacionPendiente = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.menu_activity)

        mesaId = intent.getIntExtra("MESA_ID", -1)

        val botonSalir = findViewById<Button>(R.id.botonSalir)
        val botonPedir = findViewById<Button>(R.id.boton_pedir)
        val botonPagar = findViewById<Button>(R.id.boton_pagar)
        val recyclerPlatos = findViewById<RecyclerView>(R.id.recyclerViewPlatos)
        val recyclerBebidas = findViewById<RecyclerView>(R.id.recyclerViewBebidas)


        botonSalir.setOnClickListener {
            if (confirmacionPendiente || hayPedidoAbierto) {
                Toast.makeText(this, "No puedes salir hasta que el pedido haya sido pagado.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (pedidoTemporal.isNotEmpty()) {
                Toast.makeText(this, "Termina de enviar el pedido actual antes de salir.", Toast.LENGTH_SHORT).show()
            } else {
                finish()
            }
        }

        botonPagar.setOnClickListener {
            if (confirmacionPendiente) {
                Toast.makeText(this, "El pedido anterior aún no ha sido confirmado.", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }
            if (pedidoTemporal.isNotEmpty()) {
                Toast.makeText(this, "Primero pulsa 'PEDIR' para añadir los últimos productos al pedido antes de pagar.", Toast.LENGTH_LONG).show()
            } else if (!hayPedidoAbierto) {
                Toast.makeText(this, "No hay ningún pedido abierto para esta mesa.", Toast.LENGTH_LONG).show()
            } else {
                val intent = Intent(this@MenuActivity, FacturaActivity::class.java)
                intent.putExtra("MESA_ID", mesaId)
                startActivity(intent)
            }
        }

        botonPedir.setOnClickListener {
            if (confirmacionPendiente) {
                Toast.makeText(this, "Esperando confirmación del pedido anterior.", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }
            mostrarDialogoResumen()
        }

        platoAdapter = PedidoAdapter(platoList) { producto, cantidad -> addItemAlPedido(producto, cantidad) }
        bebidaAdapter = PedidoAdapter(bebidaList) { producto, cantidad -> addItemAlPedido(producto, cantidad) }
        recyclerPlatos.layoutManager = LinearLayoutManager(this)
        recyclerPlatos.adapter = platoAdapter
        recyclerBebidas.layoutManager = LinearLayoutManager(this)
        recyclerBebidas.adapter = bebidaAdapter

        lifecycleScope.launch {
            cargarPlatos()
            cargarBebidas()
            verificarEstadoMesa()
        }
    }

    private fun addItemAlPedido(producto: PedidoModel, cantidad: Int) {
        val productoId = producto.id
        if (pedidoTemporal.containsKey(productoId)) {
            pedidoTemporal[productoId]?.let { it.cantidad += cantidad }
        } else {
            pedidoTemporal[productoId] = ItemPedido(producto, cantidad)
        }
    }

    private suspend fun verificarEstadoMesa() {
        if (mesaId == -1) return
        try {
            val response = RetrofitClient.instance.verificarPedidoExistente(mesaId)
            hayPedidoAbierto = response.isSuccessful && response.body()?.existe == true
        } catch (e: Exception) {
            hayPedidoAbierto = false
            Log.e("MenuActivity", "Error al verificar estado de la mesa", e)
        }
    }

    private fun mostrarDialogoResumen() {
        val itemsCopia = pedidoTemporal.values.toMutableList()
        if (itemsCopia.isEmpty()) {
            Toast.makeText(this, "No has añadido nada al pedido.", Toast.LENGTH_SHORT).show()
            return
        }
        val dialogView = LayoutInflater.from(this).inflate(R.layout.popup_resumen_pedido, null)
        val dialog = AlertDialog.Builder(this).setView(dialogView).create()

        val recyclerResumen: RecyclerView = dialogView.findViewById(R.id.recyclerResumenPedido)
        val botonSeguirPidiendo: Button = dialogView.findViewById(R.id.botonSeguirPidiendo)
        val botonAceptarPedido: Button = dialogView.findViewById(R.id.botonAceptarPedido)

        val adapter = ResumenAdapter(itemsCopia) { itemEliminado ->
            pedidoTemporal.remove(itemEliminado.producto.id)
            if (itemsCopia.isEmpty()) {
                dialog.dismiss()
                Toast.makeText(this, "El pedido está vacío.", Toast.LENGTH_SHORT).show()
            }
        }
        recyclerResumen.layoutManager = LinearLayoutManager(this)
        recyclerResumen.adapter = adapter

        botonSeguirPidiendo.setOnClickListener { dialog.dismiss() }
        botonAceptarPedido.setOnClickListener {
            val itemsFinales = pedidoTemporal.values.toList()
            lifecycleScope.launch {
                enviarPedidoAlServidor(itemsFinales)
            }
            dialog.dismiss()
        }
        dialog.show()
    }

    private suspend fun enviarPedidoAlServidor(items: List<ItemPedido>) {
        if (items.isEmpty()) {
            Toast.makeText(this, "No hay nada que pedir.", Toast.LENGTH_SHORT).show()
            return
        }
        try {
            val response: Response<Unit>
            if (hayPedidoAbierto) {
                val itemsPayload = ItemsPayload(items = items)
                response = RetrofitClient.instance.actualizarPedido(this.mesaId, itemsPayload)
            } else {
                val pedidoPayload = PedidoPayload(mesaId = this.mesaId, items = items)
                response = RetrofitClient.instance.crearPedido(pedidoPayload)
            }

            if (response.isSuccessful) {
                Toast.makeText(this, "Pedido enviado. Esperando confirmación...", Toast.LENGTH_LONG).show()
                pedidoTemporal.clear()
                hayPedidoAbierto = true
                confirmacionPendiente = true
                iniciarComprobacionEstadoPedido()
            } else {
                Toast.makeText(this, "Error al enviar el pedido: ${response.code()} ${response.message()}", Toast.LENGTH_LONG).show()
            }
        } catch (e: Exception) {
            Log.e("MenuActivity", "Fallo al enviar el pedido", e)
            Toast.makeText(this, "Fallo de conexión al enviar el pedido: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun iniciarComprobacionEstadoPedido() {
        lifecycleScope.launch {
            var estadoActual = "abierto"
            while (estadoActual == "abierto") {
                try {
                    val response = RetrofitClient.instance.obtenerEstadoPedido(mesaId)
                    if (response.isSuccessful && response.body() != null) {
                        estadoActual = response.body()!!.estado
                        Log.d("MenuActivity", "Estado actual del pedido: $estadoActual")
                    } else {
                        estadoActual = "cerrado"
                    }
                } catch (e: Exception) {
                    Log.e("MenuActivity", "Error, deteniendo comprobación.", e)
                    estadoActual = "error"
                }
                if (estadoActual == "abierto") {
                    delay(5000)
                }
            }
            confirmacionPendiente = false
            Toast.makeText(this@MenuActivity, "Pedido confirmado. Ya puedes pedir o pagar.", Toast.LENGTH_LONG).show()
        }
    }

    private suspend fun cargarPlatos() {
        try {
            val response = RetrofitClient.instance.getPlatos()
            platoList.clear()
            platoList.addAll(response)
            platoAdapter.notifyDataSetChanged()
        } catch (e: Exception) {
            Log.e("MenuActivity", "Error al cargar platos", e)
        }
    }

    private suspend fun cargarBebidas() {
        try {
            val response = RetrofitClient.instance.getBebidas()
            bebidaList.clear()
            bebidaList.addAll(response)
            bebidaAdapter.notifyDataSetChanged()
        } catch (e: Exception) {
            Log.e("MenuActivity", "Error al cargar bebidas", e)
        }
    }
}
