package dev.livin.ai_employee.agent

import io.modelcontextprotocol.kotlin.sdk.types.Resource

actual class MCPService actual constructor(mcpUrl: String) {
    actual suspend fun listResources(): List<Resource> {
        return emptyList() // Placeholder implementation
    }

    actual suspend fun readResource(uri: String): String {
        return "Direct resource access is not available on iOS. Use the chat agent instead."
    }

    actual suspend fun listPrompts(): List<io.modelcontextprotocol.kotlin.sdk.types.Prompt> {
        return emptyList() // Placeholder implementation
    }

    actual suspend fun fetchPrompt(name: String, args: Map<String, String>): String {
        return "Prompt fetching is not available on iOS. Use the chat agent instead."
    }

    actual fun close() {
        
    }
}