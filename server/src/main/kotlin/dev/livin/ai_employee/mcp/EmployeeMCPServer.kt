package dev.livin.ai_employee.mcp

import dev.livin.ai_employee.model.EmployeeDetails
import dev.livin.ai_employee.repository.EmployeeRepository
import io.ktor.server.cio.CIO
import io.ktor.server.engine.embeddedServer
import io.modelcontextprotocol.kotlin.sdk.server.Server
import io.modelcontextprotocol.kotlin.sdk.server.ServerOptions
import io.modelcontextprotocol.kotlin.sdk.server.mcpStreamableHttp
import io.modelcontextprotocol.kotlin.sdk.types.CallToolResult
import io.modelcontextprotocol.kotlin.sdk.types.CompleteRequest
import io.modelcontextprotocol.kotlin.sdk.types.CompleteResult
import io.modelcontextprotocol.kotlin.sdk.types.GetPromptResult
import io.modelcontextprotocol.kotlin.sdk.types.Implementation
import io.modelcontextprotocol.kotlin.sdk.types.ListToolsRequest
import io.modelcontextprotocol.kotlin.sdk.types.ListToolsResult
import io.modelcontextprotocol.kotlin.sdk.types.LoggingLevel
import io.modelcontextprotocol.kotlin.sdk.types.LoggingMessageNotification
import io.modelcontextprotocol.kotlin.sdk.types.LoggingMessageNotificationParams
import io.modelcontextprotocol.kotlin.sdk.types.Method
import io.modelcontextprotocol.kotlin.sdk.types.PromptArgument
import io.modelcontextprotocol.kotlin.sdk.types.PromptMessage
import io.modelcontextprotocol.kotlin.sdk.types.ReadResourceResult
import io.modelcontextprotocol.kotlin.sdk.types.ResourceTemplate
import io.modelcontextprotocol.kotlin.sdk.types.ResourceTemplateReference
import io.modelcontextprotocol.kotlin.sdk.types.Role
import io.modelcontextprotocol.kotlin.sdk.types.ServerCapabilities
import io.modelcontextprotocol.kotlin.sdk.types.TextContent
import io.modelcontextprotocol.kotlin.sdk.types.TextResourceContents
import io.modelcontextprotocol.kotlin.sdk.types.Tool
import io.modelcontextprotocol.kotlin.sdk.types.ToolSchema
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.double
import kotlinx.serialization.json.int
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonObject
import kotlin.collections.emptyMap


// -- Completion suggestion list -------------------
private val DEPARTMENTS = listOf("Engineering", "Product", "Design", "Marketing", "Finance", "Sales", "HR")
private val DESIGNATIONS = listOf(
    "Software Engineer",
    "Product Manager",
    "Designer",
    "Marketing Specialist",
    "Financial Analyst",
    "Sales Representative",
    "HR Manager"
)


// Small page size so pagination can be tested easily
private const val TOOLS_PAGE_SIZE = 3

// Cursor-base page slicer: cursor is a string-encoded offset integer
private fun <T> List<T>.page(cursor: String?): Pair<List<T>, String?> {
    val offset = cursor?.toIntOrNull() ?: 0
    val slice = drop(offset).take(TOOLS_PAGE_SIZE)
    val next = if (offset + TOOLS_PAGE_SIZE < this.size) (offset + TOOLS_PAGE_SIZE).toString() else null
    return slice to next
}


// -- Public factory -------------------------------------
fun buildEmployeeMCPServer(repo: EmployeeRepository): Server {

    val server = Server(
        serverInfo = Implementation(name = "employee_mcp_server", version = "1.0.0"), options = ServerOptions(
            capabilities = ServerCapabilities(
                tools = ServerCapabilities.Tools(true),
                resources = ServerCapabilities.Resources(subscribe = false, listChanged = false),
                prompts = ServerCapabilities.Prompts(listChanged = false),
                logging = ServerCapabilities.Logging, // enables logging capability
                completions = ServerCapabilities.Completions // enables completion capability
            ),
        ),
        // Instruction are show to the LLM / MCP Inspector about how use this server
        instructions = """
            This is an Employee Management MCP server.
            Use the tools to list. search, and add employees to the company.
            Use the prompts for guided HR workflows.
            Resources expose raw employee and department data.
        """.trimIndent()
    )

    // ========================================================================
    // RESOURCES
    // A Resource is a readable data source identified by a URI.
    // Static resources have a fixed URI; templates use {variable} syntax.

    // Static resource - all employees
    server.addResource(
        uri = "employees://all",
        name = "All Employees",
        description = "Returns the complete list of all employees in the company",
        mimeType = "text/plain"
    ) {
        // LOGGING - Debug: low-severity trace message
        sendLoggingMessage(
            LoggingMessageNotification(
                params = LoggingMessageNotificationParams(
                    level = LoggingLevel.Debug,
                    logger = "resource.employees_all",
                    data = JsonPrimitive("Reading employees://all")
                )
            )
        )

        ReadResourceResult(
            contents = listOf(
                TextResourceContents(
                    uri = "employees://all", text = repo.getEmployees().joinToString("\n")
                )
            )

        )

    }

    // Static resource - department catalogue
    server.addResource(
        uri = "departments://all",
        name = "All Departments",
        description = "Returns all department names available in the company",
        mimeType = "text/plain"
    ) {
        ReadResourceResult(
            contents = listOf(
                TextResourceContents(uri = "departments://all", text = DEPARTMENTS.joinToString(", "))
            )
        )
    }


    // Resource template - employee by ID ({id} is extracted from the URI)
    server.addResourceTemplate(
        uriTemplate = "employee://{id}",
        name = "Employee by ID",
        description = "Retrieve a specific employee by their numeric ID. Completion suggests valid IDs.",
        mimeType = "text/plain"
    ) { request, uriVariables ->
        val id = uriVariables["id"]?.toIntOrNull() ?: return@addResourceTemplate ReadResourceResult(
            contents = listOf(TextResourceContents(uri = request.uri, text = "Invalid employee ID:"))
        )

        // LOGGING - Debug
        sendLoggingMessage(
            LoggingMessageNotification(
                params = LoggingMessageNotificationParams(
                    level = LoggingLevel.Debug,
                    logger = "resource.employee_by_id",
                    data = JsonPrimitive("Reading employee://$id")
                )
            )

        )
        val employee = repo.getEmployeeById(id)
        ReadResourceResult(
            contents = listOf(
                TextResourceContents(uri = request.uri, text = employee?.toString() ?: "Employee not found with id $id")
            )
        )


    }

    // ===================================================================
    // TOOLS
    // A Tool is an action the LLM can invoke. Each tool declares an.
    // inputSchema so the client know what arguments to supply.
    // =================================================================================

    // Tool 1: get_all_employees - no input, basic listing
    server.addTool(
        name = "get_all_employees",
        description = "Retrieve the complete list of all employees in the company",
        inputSchema = ToolSchema()
    ) { _ ->

        val employees = repo.getEmployees()
        // LOGGING - Info: normal operation message
        sendLoggingMessage(
            LoggingMessageNotification(
                params = LoggingMessageNotificationParams(
                    level = LoggingLevel.Info,
                    logger = "tool.get_all_employees",
                    data = JsonPrimitive("Returning ${employees.size} employees")
                )
            )
        )
        CallToolResult(content = listOf(TextContent(text = employees.joinToString("\n"))))

    }

    // Tool 2: get_employee_by_id - required input + error handling
    server.addTool(
        name = "get_employee_by_id",
        description = "Retrieve a specific employee by their ID",
        inputSchema = ToolSchema(
            properties = buildJsonObject {
                putJsonObject("id") {
                    put("type", "integer");
                    put("description", "The numeric ID of the employee to retrieve")
                }
            },
            required = listOf("id")
        )
    ) { request ->
        val id = request.arguments?.get("id")?.jsonPrimitive?.intOrNull

        if (id == null) {
            // LOGGING - Warning:  caller passes bad input

            sendLoggingMessage(
                LoggingMessageNotification(
                    params = LoggingMessageNotificationParams(
                        level = LoggingLevel.Warning,
                        logger = "tool.get_employee_by_id",
                        data = JsonPrimitive("Missing or invalid parameter: id")
                    )
                )
            )
            return@addTool CallToolResult(
                content = listOf(TextContent(text = "Missing or invalid parameter: id"))
            )
        }

        val employee = repo.getEmployeeById(id)
        if (employee == null) {
            sendLoggingMessage(
                LoggingMessageNotification(
                    params = LoggingMessageNotificationParams(
                        level = LoggingLevel.Warning,
                        logger = "tool.get_employee_by_id",
                        data = JsonPrimitive("Employee not found with id $id")
                    )
                )
            )
            CallToolResult(
                content = listOf(TextContent(text = "Employee not found with id $id")),
                isError = true
            )
        } else {
            CallToolResult(content = listOf(TextContent(text = employee.toString())))
        }
    }

    // Tool 3: add_employee - write operation with full schema
    server.addTool(
        name = "add_employee",
        description = "Add a new employee to the company",
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

        val added = repo.addEmployee(
            EmployeeDetails(
                id = 0,
                name = args["name"]?.jsonPrimitive?.content ?: "",
                designation = args["designation"]?.jsonPrimitive?.content ?: "",
                department = args["department"]?.jsonPrimitive?.content ?: "",
                salary = args["salary"]?.jsonPrimitive?.double ?: 0.0
            )
        )

        // LOGGING - Info
        sendLoggingMessage(
            LoggingMessageNotification(
                params = LoggingMessageNotificationParams(
                    level = LoggingLevel.Info,
                    logger = "tool.add_employee",
                    data = JsonPrimitive("Added employee: ${added.name} (id=${added.id})")
                )
            )
        )
        CallToolResult(content = listOf(TextContent(text = "Employee added successfully: $added")))

    }


    // Tool 4: get_department_summary - derived / aggregated data
    server.addTool(
        name = "get_department_summary",
        description = "Get headcount and average salary grouped by department",
        inputSchema = ToolSchema()
    ) { _ ->
        val summary = repo.getEmployees()
            .groupBy { it.department }
            .entries.joinToString("\n") { (dept, emps) ->
                val avg = emps.map { it.salary }.average()
                "$dept - ${emps.size} employee(s), avg salary: ${"$%.2f".format(avg)}"
            }

        CallToolResult(content = listOf(TextContent(text = summary.ifEmpty { "No employees found" })))
    }

    // Tool 5: search_employees - demonstrates filtering
    server.addTool(
        name = "search_employee",
        description = "Search employees by name or department (case-insensitive)",
        inputSchema = ToolSchema(
            properties = buildJsonObject {
                putJsonObject("query") {
                    put("type", "string"); put(
                    "description",
                    "Term to match against name or department"
                )
                }
            },
            required = listOf("query")
        )
    ) { request ->

        val query = request.arguments?.get("query")?.jsonPrimitive?.content ?: ""
        val results = repo.getEmployees().filter {
            it.name.contains(query, ignoreCase = true) || it.department.contains(query, ignoreCase = true)
        }
        val text = if (results.isEmpty()) "No employees found for: $query" else results.joinToString("\n")
        CallToolResult(content = listOf(TextContent(text = text)))

    }

    // ====================================================================
    // PROMPTS
    // A Prompt is a pre-built message template the client can use to guide the LLM. Prompts can include arguments and variables.
    // Arguments marked required = true are mandatory; completion is provided by the Server handler above.

    // Prompt 1: add_employee - arguments with complete on department & designation
    server.addPrompt(
        name = "add_employee",
        description = "Generate a prompt to add a new employee (department & designation support auto complete)",
        arguments = listOf(
            PromptArgument(name = "name", description = "Full name", required = true),
            PromptArgument(
                name = "designation",
                description = "Job designation - start typing for suggestions",
                required = true
            ),
            PromptArgument(
                name = "department",
                description = "Department name - start typing for suggestions",
                required = true
            ),
            PromptArgument(name = "salary", description = "Salary in USD", required = false)
        )
    ) { request ->
        val a = request.arguments ?: emptyMap()

        GetPromptResult(
            description = "Ask the LLM to add a new employee with supplied details",
            messages = listOf(
                PromptMessage(
                    role = Role.User,
                    content = TextContent(
                        "Add a new employee to the company with the following details: \n" +
                                "* Name: ${a["name"]}\n" +
                                "* Designation: ${a["designation"]}\n" +
                                "* Department: ${a["department"]}\n" +
                                "* Salary: ${a["salary"] ?: "Not specified"}"
                    )
                )
            )
        )

    }

    // Prompt 2: hr_summary - no arguments, uses live data from the repository
    server.addPrompt(
        name = "hr_summary",
        description = "Generate an executive HR summary across all departments",
        arguments = emptyList(),
    ) { _ ->

        val employees = repo.getEmployees()
        val lines = employees.groupBy { it.department }.entries.joinToString("\n") { (dept, emps) ->
            " $dept: ${emps.size} employee(s), avg salary ${"$%.0f".format(emps.map { it.salary }.average())}"
        }

        GetPromptResult(
            messages = listOf(
                PromptMessage(
                    role = Role.User,
                    content = TextContent(
                        "Total headcount: ${employees.size}\n\nBy department:\n\n$lines\n\n" +
                                "Please provide an executive HR summary with insights and recommendations",
                    )
                )
            )
        )

    }

    // Prompt 3: onboarding_checklist - department argument also has completion
    server.addPrompt(
        name = "onboarding_checklist",
        description = "Create a personalized onboarding checklist for a new hire",
        arguments = listOf(
            PromptArgument(
                name = "employee_name",
                description = "Name of the new employee",
                required = true
            ),
            PromptArgument(
                name = "department_name",
                description = "Department being joined - type for suggestions",
                required = true
            )
        ),
    ) { request ->

        val a = request.params.arguments ?: emptyMap()
        GetPromptResult(
            messages = listOf(
                PromptMessage(
                    role = Role.User,
                    content = TextContent(
                        "Create a comprehensive onboarding checklist for ${a["employee_name"] ?: "the new employee"} " +
                                "who is joining the ${a["department_name"] ?: "company"} department" +
                                "Include: first-day tasks, equipment & access setup, team introductions, " +
                                "and 30/60/90-day milestones"
                    )
                )
            )
        )

    }


    // -- COMPLETION + PAGINATION --------------------------------------
    // createSession is final in Server, so we hook into onConnect instead
    // At the time onConnect fires, the new session is already in server.sessions.
    val configuredSessions = mutableSetOf<String>()
    server.onConnect {
        server.sessions.values
            .filter { it.sessionId !in configuredSessions }
            .forEach { session ->
                configuredSessions.add(session.sessionId)

                // COMPLETION: auto-complete suggestions for prompt arguments
                session.setRequestHandler<CompleteRequest>(Method.Defined.CompletionComplete) { request, _ ->
                    val partial = request.argument.value
                    val suggestions: List<String> = when {
                        request.argument.name == "department" ->
                            DEPARTMENTS.filter { it.startsWith(partial, ignoreCase = true) }

                        request.argument.name == "designation" ->
                            DESIGNATIONS.filter { it.startsWith(partial, ignoreCase = true) }

                        request.ref is ResourceTemplateReference -> {
                            server.resources.keys
                                .mapNotNull { uri -> uri.removePrefix("employees://").toIntOrNull()?.toString() }
                                .filter { it.startsWith(partial) }
                        }

                        else -> emptyList()
                    }

                    CompleteResult(
                        completion = CompleteResult.Completion(values = suggestions, hasMore = false)
                    )

                }
                // PAGINATION: override default ToolList handler to honour cursor
                session.setRequestHandler<ListToolsRequest>(Method.Defined.ToolsList) { request, _ ->
                    val all: List<Tool> = server.tools.values.map { it.tool }
                    val (page, nextCursor) = all.page(request.cursor)
                    ListToolsResult(tools = page, nextCursor = nextCursor)
                }

            }


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
