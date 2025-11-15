package com.example.restaurante

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class ResumenAdapter(
    private val items: MutableList<ItemPedido>,
    // Función que se ejecuta cuando se elimina un ítem del resumen. Se pasa desde MenuActivity.
    private val onItemRemoved: (ItemPedido) -> Unit
) : RecyclerView.Adapter<ResumenAdapter.ResumenViewHolder>() {

    inner class ResumenViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val textoProducto: TextView = itemView.findViewById(R.id.textoProductoResumen)
        val botonEliminar: ImageButton = itemView.findViewById(R.id.botonEliminarItem)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ResumenViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_resumen, parent, false)
        return ResumenViewHolder(view)
    }

    override fun onBindViewHolder(holder: ResumenViewHolder, position: Int) {
        val item = items[position]
        holder.textoProducto.text = "- ${item.producto.nombre} (x${item.cantidad})"

        holder.botonEliminar.setOnClickListener {
            val removedItem = items.removeAt(position)
            notifyItemRemoved(position)
            notifyItemRangeChanged(position, items.size)

            // Llama a la función lambda 'onItemRemoved' pasada desde MenuActivity.
            // Esto le "devuelve" el ítem eliminado a la actividad para que pueda actualizar su pedido.
            onItemRemoved(removedItem)
        }
    }

    override fun getItemCount(): Int = items.size
}

