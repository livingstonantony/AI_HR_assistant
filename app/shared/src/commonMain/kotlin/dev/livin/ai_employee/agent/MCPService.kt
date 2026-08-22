package dev.livin.ai_employee.agent

import io.modelcontextprotocol.kotlin.sdk.types.Prompt
import io.modelcontextprotocol.kotlin.sdk.types.Resource

expect class MCPService(mcpUrl: String) {
    suspend fun listResources(): List<Resource>
    suspend fun readResource(uri: String): String
    suspend fun listPrompts(): List<Prompt>
    suspend fun fetchPrompt(name: String, args: Map<String, String>): String
    fun close()
}

// Convenience overload - not in expect so it lives here in commonMain
suspend fun MCPService.fetchPrompt(name: String): String {
    return fetchPrompt(name, emptyMap())
}

