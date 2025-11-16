# Sistema de Gestión de Restaurante

Este proyecto es un sistema de gestión de pedidos para un restaurante, compuesto por tres aplicaciones que se comunican a través de una API REST.

## Componentes

1.  **Servidor (Node.js + Express)**
    *   API central que gestiona la lógica del restaurante y los datos (platos, pedidos).
    *   Expone endpoints para que los clientes (apps) interactúen con el sistema.

2.  **App Móvil (Android/Kotlin)**
    *   **Función:** Permite a los clientes ver el menú, crear un pedido para su mesa, enviarlo y pagarlo.

3.  **App de Escritorio (Java + JavaFX)**
    *   **Función:** Panel de gestión que muestra en tiempo real los pedidos entrantes, permitiendo ver sus detalles y confirmarlos.

## Flujo Básico

1.  El **cliente** elige una mesa libre en la **app móvil** y envía un pedido.
2.  La **app de escritorio** muestra el nuevo pedido en estado "esperando".
3.  Desde la **app de escritorio** se confirma el pedido.
4.  La **app móvil** recibe el cambio y permite volver a pedir (repitiendo el ciclo) o pagar.
5.  El cliente **paga** desde la app móvil, liberando la mesa.
