package dev.livin.ai_employee

import dev.livin.ai_employee.mcp.buildEmployeeMCPServer
import dev.livin.ai_employee.model.Employee
import dev.livin.ai_employee.repository.EmployeeRepository
import dev.livin.ai_employee.routes.employeeRoutes
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.modelcontextprotocol.kotlin.sdk.server.mcpStreamableHttp

fun main() {

    val employeeRepository = EmployeeRepository()
    embeddedServer(
        Netty,
            port = 8080, host = "0.0.0.0",) {
        module(employeeRepository)
    }
        .start(wait = true)
}

fun Application.module(employeeRepository: EmployeeRepository) {
    install(ContentNegotiation) {
        json()
    }

    routing {
        get("/") {
            call.respondText(sayHello("Ktor"))
        }

        employeeRoutes(employeeRepository)
    }

    mcpStreamableHttp(path = "/mcp") {
        buildEmployeeMCPServer(employeeRepository)
    }
}