require('dotenv').config();
const express = require('express');
const { MongoClient } = require('mongodb');
const cors = require('cors');
const app = express();
const port = process.env.PORT || 3000;
app.use(cors());
app.use(express.json());

const client = new MongoClient(process.env.MONGO_URI);
let db;

async function connectToDb() {
    try {
        await client.connect();
        db = client.db(process.env.DATABASE_NAME);
        console.log("Conectado exitosamente a MongoDB Atlas");
    } catch (error) {
        console.error("Falló la conexión a MongoDB", error);
        process.exit(1);
    }
}

// Devuelve una lista de todos los platos de la colección 'platos'.
app.get('/platos', async (req, res) => {
    try {
        const platosCollection = db.collection('platos');
        const platos = await platosCollection.find({}).sort({ _id: 1 }).toArray();
        res.status(200).json(platos);
    } catch (error) {
        console.error("Error al obtener platos:", error);
        res.status(500).json({ message: 'Error al obtener los platos' });
    }
});

// Devuelve una lista de todas las bebidas de la colección 'bebidas'.
app.get('/bebidas', async (req, res) => {
    try {
        const bebidasCollection = db.collection('bebidas');
        const bebidas = await bebidasCollection.find({}).sort({ _id: 1 }).toArray();
        res.status(200).json(bebidas);
    } catch (error) {
        console.error("Error al obtener bebidas:", error);
        res.status(500).json({ message: 'Error al obtener las bebidas' });
    }
});


// Devuelve una lista de todos los pedidos activos. Solo los campos mesaId y estado.
app.get('/pedidos/estados', async (req, res) => {
    try {
        const pedidosActivos = await db.collection('pedidos').find({}, 
            { projection: { mesaId: 1, estado: 1, _id: 0 } }
        ).toArray();
        res.status(200).json(pedidosActivos);
    } catch (error) {
        console.error("Error al obtener estados de las mesas:", error);
        res.status(500).json({ message: 'Error al obtener los estados de las mesas' });
    }
});


// Devuelve el estado ('abierto' o 'confirmado') de un pedido para una mesa específica.
app.get('/pedidos/estado/:mesaId', async (req, res) => {
    try {
        const mesaId = parseInt(req.params.mesaId, 10);
        const pedido = await db.collection('pedidos').findOne(
            { mesaId: mesaId },
            { projection: { estado: 1, _id: 0 } }
        );
        if (pedido) {
            res.status(200).json(pedido);
        } else {
            res.status(200).json({ estado: 'cerrado' });
        }
    } catch (error) {
        console.error("Error al obtener estado del pedido:", error);
        res.status(500).json({ message: 'Error al obtener el estado.' });
    }
});

// Verifica si existe un pedido activo ('abierto' o 'confirmado') para una mesa.
app.get('/pedidos/:mesaId', async (req, res) => {
    try {
        const mesaId = parseInt(req.params.mesaId, 10);
        const pedidoAbierto = await db.collection('pedidos').findOne({
            mesaId: mesaId,
            estado: { $in: ['abierto', 'confirmado'] }
        });
        res.status(200).json({ existe: !!pedidoAbierto }); // Devuelve true si existe, false si no.
    } catch (error) {
        console.error("Error al verificar el pedido de la mesa: ", error);
        res.status(500).json({ message: 'Error al obtener el estado del pedido' });
    }
});

// Devuelve todos los datos de un pedido activo para una mesa.
app.get('/pedidos/completo/:mesaId', async (req, res) => {
    try {
        const mesaId = parseInt(req.params.mesaId, 10);
        const pedido = await db.collection('pedidos').findOne({ mesaId: mesaId, estado: { $in: ['abierto', 'confirmado'] } });
        if (pedido) {
            res.status(200).json(pedido);
        } else {
            res.status(404).json({ message: 'No se encontró pedido para esa mesa.' });
        }
    } catch (error) {
        res.status(500).json({ message: 'Error al obtener el pedido.' });
    }
});


// Crea un nuevo pedido en la base de datos.
app.post('/pedidos', async (req, res) => {
    const { mesaId, items } = req.body;
    if (!mesaId || !items || !items.length) {
        return res.status(400).json({ message: 'Faltan datos.' });
    }
    try {
        const pedidosCollection = db.collection('pedidos');
        const pedidoExistente = await pedidosCollection.findOne({ mesaId: mesaId, estado: { $in: ['abierto', 'confirmado'] } });
        if (pedidoExistente) {
            return res.status(409).json({ message: 'Ya existe un pedido abierto para esta mesa.' });
        }
        const nuevoPedido = { mesaId, items, estado: 'abierto', fecha: new Date() };
        await pedidosCollection.insertOne(nuevoPedido);
        res.status(201).json({ message: 'Pedido creado correctamente.' });
    } catch (error) {
        console.error("Error al crear pedido:", error);
        res.status(500).json({ message: 'Error al crear el pedido' });
    }
});

// Actualiza un pedido existente, añadiendo nuevos items.
app.put('/pedidos/:mesaId', async (req, res) => {
    const mesaId = parseInt(req.params.mesaId, 10);
    const { items } = req.body;
    if (isNaN(mesaId) || !items || !items.length) {
        return res.status(400).json({ message: 'Faltan datos.' });
    }
    try {
        const pedidosCollection = db.collection('pedidos');
        const pedidoExistente = await pedidosCollection.findOne({ mesaId: mesaId, estado: { $in: ['abierto', 'confirmado'] } });
        if (!pedidoExistente) {
            return res.status(404).json({ message: 'No se encontró un pedido abierto para actualizar.' });
        }
        await pedidosCollection.updateOne(
            { _id: pedidoExistente._id },
            { $push: { items: { $each: items } }, $set: { estado: 'abierto' } }
        );
        res.status(200).json({ message: 'Pedido actualizado correctamente.' });
    } catch (error) {
        console.error("Error al actualizar pedido:", error);
        res.status(500).json({ message: 'Error al actualizar el pedido' });
    }
});

// Cambia el estado de un pedido de 'abierto' a 'confirmado'.
app.put('/pedidos/confirmar/:mesaId', async (req, res) => {
    try {
        const mesaId = parseInt(req.params.mesaId, 10);
        const resultado = await db.collection('pedidos').updateOne(
            { mesaId: mesaId, estado: 'abierto' },
            { $set: { estado: 'confirmado' } }
        );
        if (resultado.modifiedCount > 0) {
            res.status(200).json({ message: 'Pedido confirmado exitosamente.' });
        } else {
            res.status(404).json({ message: 'No se encontró un pedido abierto para confirmar para esa mesa.' });
        }
    } catch (error) {
        console.error("Error al confirmar pedido:", error);
        res.status(500).json({ message: 'Error interno al confirmar el pedido' });
    }
});

// Elimina un pedido activo de la base de datos.
app.delete('/pedidos/:mesaId', async (req, res) => {
    try {
        const mesaId = parseInt(req.params.mesaId, 10);
        const result = await db.collection('pedidos').deleteOne({ mesaId: mesaId, estado: { $in: ['abierto', 'confirmado'] } });
        if (result.deletedCount > 0) {
            res.status(200).json({ message: 'Pedido pagado y eliminado.' });
        } else {
            res.status(404).json({ message: 'No se encontró pedido para eliminar.' });
        }
    } catch (error) {
        res.status(500).json({ message: 'Error al eliminar el pedido.' });
    }
});


connectToDb().then(() => {
    app.listen(port, () => {
        console.log(`Servidor escuchando en http://localhost:${port}`);
    });
});
