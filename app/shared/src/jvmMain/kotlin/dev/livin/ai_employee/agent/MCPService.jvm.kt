package dev.livin.ai_employee.agent

import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO


import io.modelcontextprotocol.kotlin.sdk.client.Client
import io.modelcontextprotocol.kotlin.sdk.client.StreamableHttpClientTransport
import io.modelcontextprotocol.kotlin.sdk.types.GetPromptRequest
import io.modelcontextprotocol.kotlin.sdk.types.GetPromptRequestParams
import io.modelcontextprotocol.kotlin.sdk.types.Implementation
import io.modelcontextprotocol.kotlin.sdk.types.Prompt
import io.modelcontextprotocol.kotlin.sdk.types.ReadResourceRequest
import io.modelcontextprotocol.kotlin.sdk.types.ReadResourceRequestParams
import io.modelcontextprotocol.kotlin.sdk.types.Resource
import io.modelcontextprotocol.kotlin.sdk.types.TextContent
import io.modelcontextprotocol.kotlin.sdk.types.TextResourceContents

actual class MCPService actual constructor(private val mcpUrl: String) {

    private val httpClient = HttpClient(CIO)
    private val client = Client(clientInfo = Implementation(name = "employee_app", version = "1.0.0"))
    private var connected = false


    private suspend fun ensureConnected() {
        if (connected) return
        client.connect(StreamableHttpClientTransport(client = httpClient, url = mcpUrl))
        connected = true
    }

    actual suspend fun listResources(): List<Resource> {
        ensureConnected()
        return client.listResources().resources
    }

    actual suspend fun readResource(uri: String): String {
        ensureConnected()
        return client.readResource(ReadResourceRequest(params = ReadResourceRequestParams(uri = uri)))
            .contents.filterIsInstance<TextResourceContents>().joinToString("\n") { it.text }
    }

    actual suspend fun listPrompts(): List<Prompt> {
       ensureConnected()
        return client.listPrompts().prompts
    }

    actual suspend fun fetchPrompt(name: String, args: Map<String, String>): String {
       ensureConnected()
        return client.getPrompt(GetPromptRequest(params = GetPromptRequestParams(name = name, arguments = args)))
            .messages.joinToString("\n\n") { msg->
                "[${msg.role.name.uppercase()}]\n" + ((msg.content as? TextContent)?.text ?: "")
            }
    }

    actual fun close() {
        httpClient.close()
    }
}