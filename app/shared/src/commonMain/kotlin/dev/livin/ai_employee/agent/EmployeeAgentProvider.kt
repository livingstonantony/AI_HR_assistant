package dev.livin.ai_employee.agent

import ai.koog.agents.core.agent.AIAgent
import ai.koog.agents.core.tools.ToolRegistry
import ai.koog.agents.features.eventHandler.feature.EventHandler
import ai.koog.agents.mcp.McpToolRegistryProvider
import dev.livin.ai_employee.EmployeeApi
import dev.livin.ai_employee.getPlatform
import kotlinx.coroutines.withTimeout
import kotlin.time.Duration.Companion.milliseconds

class EmployeeAgentProvider {



    suspend fun provideAgent(): AIAgent<String, String> {
        val platform = getPlatform()

        // Use 10.0.2.2 for Android Emulator to reach the host computer, localhost for Desktop

        val host = if (platform.name.contains("Android", ignoreCase = true)) {
            "192.168.0.3" // Change this to your actual machine IP if 192.168.0.3 is wrong
        } else {
            "localhost"
        }
        val mcpUrl = "http://$host:8080/mcp"
        println("MCP:URL: $mcpUrl")

        val mcpRegistry = try {
            withTimeout(5000L.milliseconds) { // 5 second timeout
                McpToolRegistryProvider.fromSseUrl(mcpUrl)
            }
        } catch (e: Exception) {
            println("MCP Connection failed: ${e.message}")
            // Fallback to empty registry so the agent can still start with local tools
            ToolRegistry { }
        }


        // 2. Define local tools and merge with MCP tools using the '+' operator
        val combinedRegistry = mcpRegistry + ToolRegistry {
            val api = EmployeeApi()
            tool(GetEmployeesTool(api))
            // tool(GetEmployeeByIdTool(api))
            // tool(AddEmployeeTool(api))
        }

        // 3.
        val agent = AIAgent(
            toolRegistry = combinedRegistry,
            promptExecutor = platform.promptExecutor,
            llmModel = getPlatform().llmModel,
            systemPrompt = """
                You are a helpful HR assistant. 
                Use the provided tools to fetch employee data when asked.
            """.trimIndent()
        ){
            install(EventHandler) {
                onToolCallStarting { ctx ->
                    println(">> Calling tool: ${ctx.toolName} with args ${ctx.toolArgs}")
                }
            }
        }


        return agent

    }
}