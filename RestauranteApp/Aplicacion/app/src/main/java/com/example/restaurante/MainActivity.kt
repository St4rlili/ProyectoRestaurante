package com.example.restaurante

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private lateinit var botones: List<Button>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.initial_activity)

        botones = listOf(
            findViewById(R.id.mesa1),
            findViewById(R.id.mesa2),
            findViewById(R.id.mesa3),
            findViewById(R.id.mesa4),
            findViewById(R.id.mesa5)
        )

        val botonSalir = findViewById<Button>(R.id.botonSalir)
        botonSalir.setOnClickListener {
            AlertDialog.Builder(this).apply {
                setTitle("Salir")
                setMessage("¿Estás seguro de que quieres salir de la aplicación?")
                setPositiveButton("Sí") { _, _ ->
                    finishAffinity()
                }
                setNegativeButton("No", null)
                create()
                show()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        verificarEstadosMesas()
    }

    private fun verificarEstadosMesas() {
        lifecycleScope.launch {
            try {
                val response = RetrofitClient.instance.getEstadosMesas()
                if (response.isSuccessful) {
                    val estadosDeMesas = response.body() ?: emptyList()
                    // Convierte la lista de objetos en una lista de IDs de mesas ocupadas.
                    val mesasOcupadasIds = estadosDeMesas.map { it.mesaId }
                    actualizarEstadoBotones(mesasOcupadasIds)
                } else {
                    Toast.makeText(this@MainActivity, "Error al verificar estado de mesas", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Log.e("MainActivity", "Fallo de conexión al verificar mesas", e)
                Toast.makeText(this@MainActivity, "Fallo de conexión", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun actualizarEstadoBotones(mesasOcupadas: List<Int>) {
        botones.forEachIndexed { index, button ->
            val mesaId = index + 1
            if (mesasOcupadas.contains(mesaId)) {
                button.setBackgroundResource(R.drawable.boton_ocupado)
                button.isClickable = true
                button.setOnClickListener {
                    Toast.makeText(this@MainActivity, "Esta mesa está ocupada", Toast.LENGTH_SHORT).show()
                }

            } else {
                button.setBackgroundResource(R.drawable.boton_generico)
                button.isClickable = true
                button.setOnClickListener {
                    val intent = Intent(this@MainActivity, MenuActivity::class.java)
                    intent.putExtra("MESA_ID", mesaId)
                    startActivity(intent)
                }
            }
        }
    }
}
