package com.example.restaurante

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import coil.load

class FacturaAdapter(
    private val items: List<ItemPedido>
) : RecyclerView.Adapter<FacturaAdapter.FacturaViewHolder>() {

    inner class FacturaViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imagen: ImageView = itemView.findViewById(R.id.imagenFactura)
        val nombre: TextView = itemView.findViewById(R.id.nombreFactura)
        val cantidad: TextView = itemView.findViewById(R.id.cantidadFactura)
        val precioUnitario: TextView = itemView.findViewById(R.id.precioUnitarioFactura)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FacturaViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_factura, parent, false)
        return FacturaViewHolder(view)
    }

    override fun onBindViewHolder(holder: FacturaViewHolder, position: Int) {
        val item = items[position]

        holder.nombre.text = item.producto.nombre
        holder.cantidad.text = "x${item.cantidad}"
        holder.precioUnitario.text = String.format("%.2f €/u", item.producto.precio)

        holder.imagen.load(item.producto.imagen) {
            crossfade(true)
            placeholder(R.drawable.ic_launcher_foreground)
            error(R.drawable.ic_launcher_background)
        }
    }

    override fun getItemCount(): Int = items.size
}
