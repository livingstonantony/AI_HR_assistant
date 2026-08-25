package dev.livin.ai_employee.ui

import ai.koog.agents.core.agent.AIAgent
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.livin.ai_employee.agent.EmployeeAgentProvider
import dev.livin.ai_employee.agent.MCPService
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(mcpService: MCPService, onBackClick: () -> Unit) {
    val scope = rememberCoroutineScope()
    val scopeMCP = rememberCoroutineScope()

    // Store the agent in a state
    var agent by remember { mutableStateOf<AIAgent<String, String>?>(null) }
    var isLoading by remember { mutableStateOf(true) } // Start as true while initializing
    var inputText by remember { mutableStateOf("") }
    var messages by remember {
        mutableStateOf(
            listOf(
                ChatMessage(
                    "Hello! How can I help you with employee info?",
                    false
                )
            )
        )
    }


    LaunchedEffect(Unit) {
        if (agent != null) return@LaunchedEffect
        try {
            val newAgent = EmployeeAgentProvider().provideAgent()
            agent = newAgent
            println("Agent successfully initialized")
        } catch (e: Exception) {
            // Check if it's a real error and not just a normal coroutine cancellation
            println("Initialization error: ${e.message}")
            messages = messages + ChatMessage("Error initializing: ${e.message}", false)
        } finally {
            isLoading = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("HR AI Assistant") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            LazyColumn(
                modifier = Modifier.weight(1f).fillMaxWidth().padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(vertical = 16.dp)
            ) {
                items(messages) { msg ->
                    ChatBubble(msg)
                }
                if (isLoading) {
                    item {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp).padding(8.dp)
                        )
                    }
                }
            }

            // -- MCP Prompt quick-select chips ---------
            // Each chips fetches the prompt from the MCP server, then sends the resulting message to the agent - demonstrating MCP Prompts.
            PromptChipsRow(
                mcpService = mcpService,
                agent = agent,
                isLoading = isLoading,
                onMessage = { msg -> messages = messages + msg },
                onLoading = { isLoading = it },
                inputText = { inputText = it }
            )

            Row(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                OutlinedTextField(
                    value = inputText,
                    onValueChange = { inputText = it },
                    modifier = Modifier.weight(1f),
                    enabled = !isLoading && agent != null
                )
                IconButton(
                    onClick = {
                        val currentAgent = agent ?: return@IconButton
                        val text = inputText
                        messages = messages + ChatMessage(text, true)
                        inputText = ""
                        isLoading = true
                        scope.launch {
                            try {
                                val response = currentAgent.run(text)
                                messages = messages + ChatMessage(response, false)
                            } catch (e: Exception) {
                                messages = messages + ChatMessage("Error: ${e.message}", false)
                            } finally {
                                isLoading = false
                            }
                        }
                    },
                    enabled = inputText.isNotBlank() && !isLoading && agent != null
                ) {
                    Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Send")
                }
            }
        }
    }
}

data class ChatMessage(val text: String, val isUser: Boolean)

@Composable
fun ChatBubble(message: ChatMessage) {
    val alignment = if (message.isUser) Alignment.End else Alignment.Start
    val color =
        if (message.isUser) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.secondaryContainer
    Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = alignment) {
        Surface(shape = RoundedCornerShape(12.dp), color = color) {
            Text(text = message.text, modifier = Modifier.padding(12.dp))
        }
    }
}

// Prompt chips - each one fetches an MCP prompt then send it to the agent
private data class PromptShortCut(val label: String, val promptName: String, val args: Map<String, String>)

private val PROMPT_SHORTCUTS = listOf(
//    PromptShortCut("HR Summary", "hr_summary", emptyMap()),
    PromptShortCut(
        "Add Employee",
        "add_employee",
        mapOf("name" to "John Doe", "designation" to "Engineering", "salary" to "80000")
    ),
    PromptShortCut(
        "Delete Employee By ID",
        "delete_employee_by_id",
        mapOf("id" to "1")
    )
    /*PromptShortCut(
        "Onboarding",
        "onboarding_checklist",
        mapOf("employee_name" to "JJane Smith", "department" to "Product")
    ),*/

    )

@Composable
private fun PromptChipsRow(
    mcpService: MCPService,
    agent: AIAgent<String, String>?,
    isLoading: Boolean,
    onMessage: (ChatMessage) -> Unit,
    onLoading: (Boolean) -> Unit,
    inputText: (String) -> Unit,
) {

    val scope = rememberCoroutineScope()
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 12.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        PROMPT_SHORTCUTS.forEach { shortCut ->
            AssistChip(
                onClick = {
                    val currentAgent = agent ?: return@AssistChip
                    scope.launch {
                        onLoading(true)

                        try {
                            // 1. Fetch the MCP prompt -> get the pre-built message
                            val promptMessage = mcpService.fetchPrompt(shortCut.promptName, shortCut.args)
//                            onMessage(ChatMessage("Using Prompt: ${shortCut.promptName}", false))

                            println("PROMPT_MESSAGE: \n\n$promptMessage")
                            inputText(promptMessage)

                            // 2. Send that message to the LLM agent
//                            val response = currentAgent.run(promptMessage)
//                            onMessage(ChatMessage(response, false))
                        } catch (e: Exception) {
                            onMessage(ChatMessage("Prompt error: ${e.message}", false))
                        } finally {
                            onLoading(false)
                        }
                    }
                },
                label = { Text(shortCut.label, style = MaterialTheme.typography.labelSmall) },
                enabled = !isLoading && agent != null,
            )
        }
    }
}