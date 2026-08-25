package dev.livin.ai_employee

import dev.livin.ai_employee.model.Employee
import dev.livin.ai_employee.model.EmployeeDetails
import dev.livin.ai_employee.model.EmployeeItem
import dev.livin.ai_employee.model.ResponseMessage
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.delay
import kotlinx.serialization.json.Json

class EmployeeApi {

    val BASE_URL = "http://127.0.0.1:8080"
    private val client = HttpClient {
        install(ContentNegotiation) {
            json(Json { ignoreUnknownKeys = true })
        }
    }

    suspend fun getEmployees(): List<EmployeeItem> {
        // Use your local machine's IP or "10.0.2.2" for Android emulator
        return client.get("$BASE_URL/employees").body()
    }


    suspend fun getEmployeeById(id: Int): EmployeeDetails {

        return client.get("$BASE_URL/employee/$id").body()
    }

    suspend fun deleteEmployeeById(id: Int): ResponseMessage {

        delay(500L)
        return client.delete("$BASE_URL/employee/$id").body()
    }


    suspend fun addEmployee(employee: EmployeeDetails): ResponseMessage {

        delay(500L)
        return client.post("$BASE_URL/employee") {
            contentType(ContentType.Application.Json) // Set content type header
            setBody(employee)
        }.body()
    }
}