package dev.livin.ai_employee.agent

import ai.koog.agents.core.agent.AIAgent
import ai.koog.agents.core.tools.ToolRegistry
import ai.koog.agents.features.eventHandler.feature.EventHandler
import dev.livin.ai_employee.EmployeeApi
import dev.livin.ai_employee.getPlatform

class EmployeeAgentProvider {

    val toolRegistry = ToolRegistry {
        val api = EmployeeApi()
        tool(GetEmployeesTool(api))
        tool(GetEmployeeByIdTool(api))
        tool(AddEmployeeTool(api))

    }

    fun provideAgent(): AIAgent<String, String> {
        val agent = AIAgent(
            toolRegistry = toolRegistry,
            promptExecutor = getPlatform().promptExecutor,
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