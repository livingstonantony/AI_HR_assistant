package dev.livin.ai_employee.mcp

import dev.livin.ai_employee.model.EmployeeDetails
import dev.livin.ai_employee.repository.EmployeeRepository
import io.ktor.server.cio.CIO
import io.ktor.server.engine.embeddedServer
import io.modelcontextprotocol.kotlin.sdk.server.Server
import io.modelcontextprotocol.kotlin.sdk.server.ServerOptions
import io.modelcontextprotocol.kotlin.sdk.server.mcpStreamableHttp
import io.modelcontextprotocol.kotlin.sdk.types.CallToolResult
import io.modelcontextprotocol.kotlin.sdk.types.Implementation
import io.modelcontextprotocol.kotlin.sdk.types.ServerCapabilities
import io.modelcontextprotocol.kotlin.sdk.types.TextContent
import io.modelcontextprotocol.kotlin.sdk.types.ToolSchema
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.double
import kotlinx.serialization.json.int
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonObject


fun buildEmployeeMCPServer(repo: EmployeeRepository): Server {

    val server = Server(
        serverInfo = Implementation(name = "employee_mcp_server", version = "1.0.0"),
        options = ServerOptions(
            capabilities = ServerCapabilities(tools = ServerCapabilities.Tools(true))
        )
    )

    server.addTool(
        name = "get_employees",
        description = "Retrieve Employees from the company",
        inputSchema = ToolSchema()
    ) {
        val employees = repo.getEmployees()
        CallToolResult(content = listOf(TextContent(text = employees.toString())))
    }

    server.addTool(
        name = "get_employee_by_id",
        description = "Retrieve detailed information about a specific Employee by id from the company",
        inputSchema = ToolSchema(
            properties = buildJsonObject {
                putJsonObject("id") { put("type", "integer") }
            },
            required = listOf("id")
        )
    ) { request ->
        val id = request.arguments?.get("id")?.jsonPrimitive?.int
            ?: return@addTool CallToolResult(
                content = listOf(TextContent(text = "Missing required argument: id"))
            )
        val employee = repo.getEmployeeById(id)
        CallToolResult(
            content = listOf(
                TextContent(
                    text = employee?.toString() ?: "Employee does not exist with id: $id"
                )
            )
        )
    }

    server.addTool(
        name = "add_employee",
        description = "Add a new employee into the company",
        inputSchema = ToolSchema(
            properties = buildJsonObject {
                putJsonObject("name") { put("type", "string") }
                putJsonObject("designation") { put("type", "string") }
                putJsonObject("department") { put("type", "string") }
                putJsonObject("salary") { put("type", "number") }
            },
            required = listOf("name", "designation", "department", "salary")
        )
    ) { request ->

        val args = request.arguments ?: buildJsonObject { }
        val employee = EmployeeDetails(
            id = 0,
            name = args["name"]?.jsonPrimitive?.content ?: "",
            designation = args["designation"]?.jsonPrimitive?.content ?: "",
            department = args["department"]?.jsonPrimitive?.content ?: "",
            salary = args["salary"]?.jsonPrimitive?.double ?: 0.0,
        )

        val data = repo.addEmployee(employee)
        CallToolResult(content = listOf(TextContent(text = "Employee added successfully: $data")))
    }
    return server
}

/*
fun main(args: Array<String>) {

    val port = args.firstOrNull()?.toIntOrNull() ?: 3001
    val repo = EmployeeRepository()
    embeddedServer(CIO, host = "127.0.0.1", port = port) {
        mcpStreamableHttp {
            buildEmployeeMCPServer(repo)
        }
    }.start(wait = true)
}*/
