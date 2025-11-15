package com.example.restaurante

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

// Uso de '@Parcelize' para que Android pueda pasar objetos de esta clase entre Activities.
@Parcelize
data class PedidoModel(

    // Uso de '@SerializedName' para trabajar con la librería Gson. Indicando como se deben mapear los campos.
    @SerializedName("_id")
    val id: String,

    @SerializedName("nombre")
    val nombre: String,

    @SerializedName("precio")
    val precio: Double,

    @SerializedName("imagen")
    val imagen: String

) : Parcelable
