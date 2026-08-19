package dev.livin.ai_employee.mcp

import dev.livin.ai_employee.model.EmployeeDetails
import dev.livin.ai_employee.repository.EmployeeRepository
import io.ktor.server.cio.CIO
import io.ktor.server.engine.embeddedServer
import io.modelcontextprotocol.kotlin.sdk.server.Server
import io.modelcontextprotocol.kotlin.sdk.server.ServerOptions
import io.modelcontextprotocol.kotlin.sdk.server.mcpStreamableHttp
import io.modelcontextprotocol.kotlin.sdk.types.CallToolResult
import io.modelcontextprotocol.kotlin.sdk.types.GetPromptResult
import io.modelcontextprotocol.kotlin.sdk.types.Implementation
import io.modelcontextprotocol.kotlin.sdk.types.PromptArgument
import io.modelcontextprotocol.kotlin.sdk.types.PromptMessage
import io.modelcontextprotocol.kotlin.sdk.types.ReadResourceResult
import io.modelcontextprotocol.kotlin.sdk.types.Role
import io.modelcontextprotocol.kotlin.sdk.types.ServerCapabilities
import io.modelcontextprotocol.kotlin.sdk.types.TextContent
import io.modelcontextprotocol.kotlin.sdk.types.TextResourceContents
import io.modelcontextprotocol.kotlin.sdk.types.ToolSchema
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.double
import kotlinx.serialization.json.int
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonObject


fun buildEmployeeMCPServer(repo: EmployeeRepository): Server {

    val server = Server(
        serverInfo = Implementation(name = "employee_mcp_server", version = "1.0.0"), options = ServerOptions(
            capabilities = ServerCapabilities(
                tools = ServerCapabilities.Tools(true),
                resources = ServerCapabilities.Resources(subscribe = false, listChanged = false),
                prompts = ServerCapabilities.Prompts(listChanged = false),
            )
        )
    )

    // Retrieve all employees from the company
    server.addResource(
        uri = "employees://all",
        name = "All Employees",
        description = "Retrieve all Employees from the company",
        mimeType = "text/plain"
    ) {
        val employees = repo.getEmployees()
        ReadResourceResult(
            contents = listOf(
                TextResourceContents(
                    uri = "employees://all", text = employees.toString()
                )
            )
        )
    }

    // Retrieve a specific employee by ID from the company
    server.addResourceTemplate(
        uriTemplate = "employees://{id}",
        name = "Employee by ID",
        description = "Retrieve detailed information about a specific Employee by id from the company",
        mimeType = "text/plain"
    ) { request, uriVariables ->
        val id = uriVariables["id"]?.toIntOrNull() ?: return@addResourceTemplate ReadResourceResult(
            contents = listOf(
                TextResourceContents(
                    uri = request.uri, text = "Invalid or missing employee ID"
                )
            )
        )
        val employee = repo.getEmployeeById(id)
        ReadResourceResult(
            contents = listOf(
                TextResourceContents(
                    uri = request.uri, text = employee?.toString() ?: "Employee does not exist with id: $id"
                )
            )
        )
    }



    server.addTool(
        name = "Add new Employee", description = "Add a new employee into the company", inputSchema = ToolSchema(
            properties = buildJsonObject {
                putJsonObject("name") { put("type", "string") }
                putJsonObject("designation") { put("type", "string") }
                putJsonObject("department") { put("type", "string") }
                putJsonObject("salary") { put("type", "number") }
            }, required = listOf("name", "designation", "department", "salary")
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


    server.addPrompt(
        name = "Add Employee", description = "Add a new employee to the company", arguments = listOf(
            PromptArgument(
                name = "name",
                description = "The name of the employee to add",
                required = true,
            ), PromptArgument(
                name = "designation",
                description = "The designation of the employee to add",
                required = true,
            ), PromptArgument(
                name = "department",
                description = "The department of the employee to add",
                required = true,
            ), PromptArgument(
                name = "salary",
                description = "The salary of the employee to add",
                required = true,
            )
        )
    ) { request ->
        val name = request.arguments?.get("name") ?: ""
        val designation = request.arguments?.get("designation") ?: ""
        val department = request.arguments?.get("department") ?: ""
        val salary = request.arguments?.get("salary") ?: 0.0




        GetPromptResult(
            messages = listOf(
                PromptMessage(
                    role = Role.User,
                    content = TextContent(
                        "Add employee with details: " +
                                "\nname: $name, " +
                                "\ndesignation: $designation, " +
                                "\ndepartment: $department, " +
                                "\nsalary: $salary"
                    )
                )
            )
        )
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
