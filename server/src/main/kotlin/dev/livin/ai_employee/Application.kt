package dev.livin.ai_employee

import dev.livin.ai_employee.mcp.buildEmployeeMCPServer
import dev.livin.ai_employee.model.Employee
import dev.livin.ai_employee.repository.EmployeeRepository
import dev.livin.ai_employee.routes.employeeRoutes
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.*
import io.ktor.server.cio.CIO
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.plugins.cors.routing.CORS
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.modelcontextprotocol.kotlin.sdk.server.mcpStreamableHttp

fun main() {

    val employeeRepository = EmployeeRepository()
    embeddedServer(
        CIO,
        port = 8080, host = "127.0.0.1",
    ) {
        module(employeeRepository)
        mcp(employeeRepository)
    }
        .start(wait = true)
}

fun Application.module(employeeRepository: EmployeeRepository) {

    routing {
        get("/") {
            call.respondText(sayHello("Ktor"))
        }

        employeeRoutes(employeeRepository)
    }


}

fun Application.mcp(repository: EmployeeRepository) {
    mcpStreamableHttp("/mcp") {
        buildEmployeeMCPServer(repository)
    }
}
