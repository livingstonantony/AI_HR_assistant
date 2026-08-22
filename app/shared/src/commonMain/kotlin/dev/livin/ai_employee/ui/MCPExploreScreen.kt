package dev.livin.ai_employee.ui

import ai.koog.agents.core.agent.AIAgent
import ai.koog.agents.mcp.McpTool
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Build
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import dev.livin.ai_employee.agent.EmployeeAgentProvider
import dev.livin.ai_employee.agent.MCPService
import kotlinx.coroutines.launch

// -- MCP feature descriptor
private enum class McpType { TOOL, RESOURCES, PROMPT }

private data class McpFeature(
    val type: McpType,
    val name: String,
    val description: String,
    val query: String // sent to the AI agent to invoke this feature
)

private val MCP_FEATURES = listOf(
    // -- Tools ---------------------------------
    McpFeature(McpType.TOOL, "get_all_employees", "List every employee", "List all employees"),
    McpFeature(McpType.TOOL, "get_employee_by_id", "Fetches employee with id=1", "Get employee with id 1"),
    McpFeature(
        McpType.TOOL,
        "search_employees",
        "Searches by name or department",
        "Search for employees in Engineering department"
    ),
    McpFeature(
        McpType.TOOL,
        "get_department_summary",
        "Headcount & avg salary per department",
        "Show department summary"
    ),
    McpFeature(
        McpType.TOOL,
        "add_employee",
        "Adds a new employee record",
        "Add employee Jane Smith, Senior Engineer, Engineering, salary 95000"
    ),
    // -- Resources ---------------------------------
    McpFeature(
        McpType.RESOURCES,
        "employees://all",
        "Read the all-employees resource",
        "Read the employees://all resource and list everyone"
    ),
    McpFeature(McpType.RESOURCES, "departments://all", "Read the department-list resource", "What departments exist?"),
    McpFeature(
        McpType.RESOURCES,
        "employee://{id}",
        "Read employee by ID template",
        "Read the employee resource for id 1"
    ),
    // -- Prompts ---------------------------------
    McpFeature(McpType.PROMPT, "hr_summary", "Executive HR summary prompt", "Generate an HR executive summary"),
    McpFeature(
        McpType.PROMPT,
        "add_employee",
        "Guided add-employee prompt",
        "Use the add_employee prompt for Alice Chen, Product Manager, Product, 85000"
    ),
    McpFeature(
        McpType.PROMPT,
        "onboarding_checklist",
        "Onboarding checklist prompt",
        "Create onboarding checklist bor Bob Lee joining Engineering"
    ),
)

@Composable
fun MCPExploreScreen(mcpService: MCPService, onBackClick: () -> Unit) {
    val scope = rememberCoroutineScope()
    var agent by remember { mutableStateOf<AIAgent<String, String>?>(null) }
    var isConnecting by remember { mutableStateOf(true) }
    var connectionError by remember { mutableStateOf<String?>(null) }

    // Per-feature state: null = idle, empty = loading, non-empty = result
    val results = remember { mutableStateMapOf<String, String?>() }
    val loading = remember { mutableStateMapOf<String, Boolean>() }


    LaunchedEffect(Unit) {
        try {
            agent = EmployeeAgentProvider().provideAgent()
        } catch (e: Exception) {
            connectionError = e.message
        } finally {
            isConnecting = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("MCP Explore") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // -- Connection status banner --------------
            ConnectionBanner(isConnecting = isConnecting, error = connectionError, connected = agent != null)

            if (isConnecting) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator()
                        Spacer(Modifier.height(12.dp))
                        Text("Connecting to MCP Server...", style = MaterialTheme.typography.bodyMedium)
                    }
                }
                return@Scaffold
            }

            LazyColumn(
                contentPadding = PaddingValues(12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                // Group by type for clear section headers
                McpType.entries.forEach { type ->
                    val features = MCP_FEATURES.filter { it.type == type }
                    item {
                        SectionHeader(type = type)
                    }
                    items(features){ feature ->
                        FeatureCard(
                            feature = feature,
                            isLoading = loading[feature.name] == true,
                            result = results[feature.name],
                            agentReady = agent != null,
                            onTryClick = {

                                val currentAgent = agent

                                loading[feature.name] = true
                                // Clear previous result and set loading state
                                results[feature.name] = null

                                // Launch a coroutine to call the agent
                                scope.launch {
                                    try {
                                        results[feature.name] = when(feature.type){
                                            // RESOURCE: readd directly via MCPService -- LLM not involved
                                            McpType.RESOURCES -> mcpService.readResource(feature.name)
                                            // PROMPT: fetch message via MCPService, then send to agent
                                            McpType.PROMPT -> {
                                                val msg = mcpService.fetchPrompt(feature.name, emptyMap())
                                                currentAgent?.run(msg)?: msg
                                            }
                                            // TOOL: agent decides when and how to call it
                                            McpType.TOOL -> currentAgent?.run(feature.query)?: "Agent not ready"
                                        }
                                    } catch (e: Exception) {
                                        results[feature.name] = "Error: ${e.message}"
                                    } finally {
                                        loading[feature.name] = false
                                    }
                                }
                            }
                        )

                    }
                }
            }

        }

    }
}

@Composable
private fun ConnectionBanner(isConnecting: Boolean, error: String?, connected: Boolean) {
    val (bg, text) = when {
        isConnecting -> MaterialTheme.colorScheme.surfaceVariant to "Connecting..."
        error != null -> Color(0xFFFFCDD2) to "Connection failed: $error"
        connected -> Color(0xFFC8E6C9) to "Connected to MCP Server (http://...:8080/mcp)"
        else -> MaterialTheme.colorScheme.surfaceVariant to ""
    }

    if (text.isNotEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(bg)
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Text(text, style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
private fun SectionHeader(type: McpType) {
    val (icon, label, color) = when (type) {
        McpType.TOOL -> Triple(Icons.Default.Build, "Tools", Color(0xFF1565C0))
        McpType.RESOURCES -> Triple(Icons.Default.Build, "Resources", Color(0xFF2E7D32))
        McpType.PROMPT -> Triple(Icons.Default.Build, "Prompts", Color(0xFF6A1B9A))
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(top = 8.dp, bottom = 8.dp)
    ) {
        Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(18.dp))
        Spacer(Modifier.width(6.dp))
        Text(label, style = MaterialTheme.typography.titleSmall.copy(color = color, fontWeight = FontWeight.Bold))
    }
}

@Composable
private fun FeatureCard(
    feature: McpFeature,
    isLoading: Boolean,
    result: String?,
    agentReady: Boolean,
    onTryClick: () -> Unit,
) {
    val accentColor = when (feature.type) {
        McpType.TOOL -> Color(0xFF1565C0)
        McpType.RESOURCES -> Color(0xFF2E7D32)
        McpType.PROMPT -> Color(0xFF6A1B9A)
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(2.dp),
        shape = RoundedCornerShape(10.dp),
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        feature.name,
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                        fontFamily = FontFamily.Monospace,
                    )
                    Text(
                        feature.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(Modifier.width(8.dp))
                Button(
                    onClick = onTryClick,
                    enabled = agentReady && !isLoading,
                    colors = ButtonDefaults.textButtonColors(contentColor = accentColor),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text("Try", style = MaterialTheme.typography.labelMedium)
                    }
                }
            }

            // Result area
            if (result != null) {
                Spacer(Modifier.width(8.dp))
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = result,
                        modifier = Modifier.padding(10.dp),
                        style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace)
                    )
                }
            }
        }
    }

}