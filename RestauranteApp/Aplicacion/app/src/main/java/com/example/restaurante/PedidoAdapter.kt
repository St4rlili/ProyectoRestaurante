package com.example.restaurante

import android.app.AlertDialog
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import coil.load

class PedidoAdapter(
    private val pedidoList: List<PedidoModel>,
    private val onItemClicked: (producto: PedidoModel, cantidad: Int) -> Unit
) : RecyclerView.Adapter<PedidoAdapter.PedidoViewHolder>() {

    inner class PedidoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val nombreView: TextView = itemView.findViewById(R.id.nombrePedido)
        val precioView: TextView = itemView.findViewById(R.id.precioPedido)
        val imagenView: ImageView = itemView.findViewById(R.id.imagenPedido)

        init {
            itemView.setOnClickListener {
                val position = bindingAdapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    val pedido = pedidoList[position]
                    mostrarDialogoCantidad(itemView, pedido)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PedidoViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_pedido, parent, false)
        return PedidoViewHolder(view)
    }

    override fun onBindViewHolder(holder: PedidoViewHolder, position: Int) {
        val pedido = pedidoList[position]
        holder.nombreView.text = pedido.nombre
        holder.precioView.text = "${pedido.precio} €"
        holder.imagenView.load(pedido.imagen) {
            crossfade(true) // Animación de fundido.
            placeholder(R.drawable.ic_launcher_foreground)
            error(R.drawable.ic_launcher_background)
        }
    }

    private fun mostrarDialogoCantidad(view: View, pedido: PedidoModel) {
        val context = view.context
        val dialogView = LayoutInflater.from(context).inflate(R.layout.popup_cantidad, null)
        val dialog = AlertDialog.Builder(context)
            .setView(dialogView)
            .create()

        val nombrePedido: TextView = dialogView.findViewById(R.id.nombrePedidoPopup)
        val cantidadPedido: EditText = dialogView.findViewById(R.id.cantidadPedidoPopup)
        val botonMas: Button = dialogView.findViewById(R.id.masCantidadPopup)
        val botonMenos: Button = dialogView.findViewById(R.id.menosCantidadPopup)
        val botonAceptar: Button = dialogView.findViewById(R.id.aceptarCantidadPopup)
        val botonCancelar: Button = dialogView.findViewById(R.id.cancelarCantidadPopup)

        nombrePedido.text = pedido.nombre

        var cantidad = 1
        cantidadPedido.setText(cantidad.toString())

        botonMas.setOnClickListener {
            cantidad++
            cantidadPedido.setText(cantidad.toString())
        }

        botonMenos.setOnClickListener {
            if (cantidad > 1) {
                cantidad--
                cantidadPedido.setText(cantidad.toString())
            }
        }

        botonCancelar.setOnClickListener {
            dialog.dismiss()
        }

        botonAceptar.setOnClickListener {
            val cantidadFinalStr = cantidadPedido.text.toString()
            if (cantidadFinalStr.isEmpty() || !cantidadFinalStr.matches(Regex("\\d+")) || cantidadFinalStr.toInt() <= 0) {
                Toast.makeText(context, "La cantidad debe ser un número mayor que cero.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val cantidadFinal = cantidadFinalStr.toInt()

            // Llama a la función 'onItemClicked' que fue pasada desde MenuActivity, y envia el pedido y la cantidad de vuelta.
            onItemClicked(pedido, cantidadFinal)

            Toast.makeText(context, "Añadido x${cantidadFinal} ${pedido.nombre}", Toast.LENGTH_SHORT).show()
            dialog.dismiss()
        }

        dialog.show()
    }

    override fun getItemCount(): Int = pedidoList.size
}
