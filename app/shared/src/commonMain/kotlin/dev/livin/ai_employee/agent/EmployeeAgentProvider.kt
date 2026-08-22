package dev.livin.ai_employee.agent

import ai.koog.agents.core.agent.AIAgent
import ai.koog.agents.core.tools.ToolRegistry
import ai.koog.agents.features.eventHandler.feature.EventHandler
import ai.koog.agents.mcp.McpToolRegistryProvider
import dev.livin.ai_employee.EmployeeApi
import dev.livin.ai_employee.getPlatform
import kotlinx.coroutines.delay
import kotlinx.coroutines.withTimeout
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

// Retries until the block succeeds or the total timeout elapses; returns all on timeout.
private suspend fun <T> retryUntilReady(label: String, timeout: Duration = 10.seconds, block: suspend () -> T): T? {
    return withTimeoutOrNull(timeout) {
        val delayMs = 500L
        while (true) {
            try {
                return@withTimeoutOrNull block()
            } catch (e: Exception) {
                println("$label not ready (${e.message}),  Retrying in ${delayMs} ms...")
                delay(delayMs)
                if (delayMs < 8_000L) delayMs * 2
            }
        }
        @Suppress("UNREACHABLE_CODE")
        null
    }
}

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

        val mcpRegistry = retryUntilReady("MCP tools"){
            McpToolRegistryProvider.fromSseUrl(mcpUrl)
        }?: run {
            println("MCP Server not available after timeout - starting without MCP tools")
            ToolRegistry {}
        }

        val combinedRegistry = mcpRegistry + ToolRegistry{
            val api = EmployeeApi()
            tool(GetEmployeesTool(api))
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
        ) {
            install(EventHandler) {
                onToolCallStarting { ctx ->
                    println(">> Calling tool: ${ctx.toolName} with args ${ctx.toolArgs}")
                }
            }
        }


        return agent

    }
}