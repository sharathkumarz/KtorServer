
package org.example
import io.ktor.http.*
import io.ktor.serialization.jackson.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.litote.kmongo.coroutine.CoroutineClient
import org.litote.kmongo.coroutine.CoroutineCollection
import org.litote.kmongo.coroutine.CoroutineDatabase
import org.litote.kmongo.coroutine.coroutine
import org.litote.kmongo.eq
import org.litote.kmongo.reactivestreams.KMongo


data class Customer(
    val id: String,
    val name: String,
    val number: String
)

// Define the MongoDB client and collection once
val client: CoroutineClient = KMongo.createClient("mongodb+srv://user35:user35@cluster0.jb83w.mongodb.net/myDatabase?retryWrites=true&w=majority").coroutine
val database: CoroutineDatabase = client.getDatabase("CUSTOMERDATA")
val collection: CoroutineCollection<Customer> = database.getCollection<Customer>()

// Define the Ktor application
fun Application.module() {
    install(ContentNegotiation) {
        jackson { }
    }

    install(StatusPages) {
        exception<Throwable> { call,cause ->
            call.respond(HttpStatusCode.InternalServerError, cause.localizedMessage)
        }
    }

    routing {
        // Route to get all customers
        get("/customers") {
            try {
                val customers = withContext(Dispatchers.IO) { collection.find().toList() }
                call.respond(customers)
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, "Error retrieving customers: ${e.message}")
            }
        }

        // Route to get a customer by ID
        get("/customer/{id}") {
            val id = call.parameters["id"]
            if (id != null) {
                try {
                    val customer = withContext(Dispatchers.IO) { collection.findOne(Customer::id eq id) }
                    if (customer != null) {
                        call.respond(customer)
                    } else {
                        call.respond(HttpStatusCode.NotFound, "Customer not found.")
                    }
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.InternalServerError, "Error retrieving customer: ${e.message}")
                }
            } else {
                call.respond(HttpStatusCode.BadRequest, "Missing or invalid ID.")
            }
        }

        // Route to create a new customer
        post("/customer") {
            try {
                val customer = call.receive<Customer>()
                val result = withContext(Dispatchers.IO) { collection.insertOne(customer) }
                if (result.wasAcknowledged()) {
                    call.respond(HttpStatusCode.Created, "Customer created successfully.")
                } else {
                    call.respond(HttpStatusCode.BadRequest, "Error creating customer.")
                }
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, "Error creating customer: ${e.message}")
            }
        }

        // Route to update an existing customer by ID
        put("/customer/{id}") {
            val id = call.parameters["id"]
            if (id != null) {
                try {
                    val customer = call.receive<Customer>()
                    val result = withContext(Dispatchers.IO) { collection.replaceOne(Customer::id eq id, customer) }
                    if (result.modifiedCount > 0) {
                        call.respond(HttpStatusCode.OK, "Customer updated successfully.")
                    } else {
                        call.respond(HttpStatusCode.NotFound, "Customer not found.")
                    }
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.InternalServerError, "Error updating customer: ${e.message}")
                }
            } else {
                call.respond(HttpStatusCode.BadRequest, "Missing or invalid ID.")
            }
        }

        // Route to delete a customer by ID
        delete("/customer/{id}") {
            val id = call.parameters["id"]
            if (id != null) {
                try {
                    val result = withContext(Dispatchers.IO) { collection.deleteOne(Customer::id eq id) }
                    if (result.deletedCount > 0) {
                        call.respond(HttpStatusCode.OK, "Customer deleted successfully.")
                    } else {
                        call.respond(HttpStatusCode.NotFound, "Customer not found.")
                    }
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.InternalServerError, "Error deleting customer: ${e.message}")
                }
            } else {
                call.respond(HttpStatusCode.BadRequest, "Missing or invalid ID.")
            }
        }
    }
}

// Main function to start the Ktor server
fun main() {
    embeddedServer(Netty, port = 8080, module = Application::module).start(wait = true)
}
