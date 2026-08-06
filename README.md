# AI Employee - Kotlin Multiplaform + AI Agents

A teaching project demonstrating how to build an **AI-powered HR assistant** using Kotlin Multiplatform (KMP), Compose Multiplatform, Ktor, and the [Koog](https://github.com/Jetbrains/koog) AI agent framework. The app runs on **Android, iOS, and Desktop (JVM)** from a single shared codebase and talks to a **Ktor backend Server**

> **Disclaimer:** This is not a production-ready of fully functional HR assistant. Think of it as a **"Hello, World!" for AI Integration** - built purely for learning purpose to understand how to wire up the koog agent framework in a Kotlin Multiplatform project. Feautres like error handling, authentication, and real HR workflow are intentionally out of scope.

---

## Project Structure

```
.
├── app/
│   ├── androidAPP/       # Android entry point
|   ├── desktopApp/       # Desktop (JVM) entry point
|   ├── iosAP/            # iOS entry point (Xcode)
│   └── shared/           # Shared Compose Multiplatform UI & logic
|       └── src/
|           ├── commonMain/     ← THE MOST IMPORTANT FOLDER (see below)
|           ├── androidMain/    # Android-specific Platform implementation 
|           ├── iosMain/        # iOS-specific Platform implementation       
│           └── jvmMain/        # Desktop-specific Platform implementation        
├── core/                  # Shared data models (used by all modules)
└── server/                # Ktor backend REST API

```

---

## The `commonMain` Module - What Lives Here

The [`app/shared/src/commonMain`](./app/shared/commonMain/kotlin/dev/livin/ai_employee) folder holds all the code that runs indentically on every platform.


### 1. Data Models - `core/src/commonMain`

Define in [`core`](./core/src/commonMain/kotlin/dev/livin/ai_employee/model/Employee.kt), these models are shared everywhere:

| Class | Purpose |
|---|---|
| `EmployeeItem` | Lightweight list item: `id` + `name` |
| `EmployeeDetails` | Full employee record: `id`, `name`, `designation`, `department`, `salary` |
| `ResponseMessage` | Generic server response wrapper |


### 2. Platform Interface - `Platform.kt`

```kotlin
interface Platform {
    val name: String
    val llmModel: LLModel   // Which AI model to use
    val promptExecutor: PromptExecutor // how to call the model
}
expect fun getPlatform(): Platform
```

Each platform (`androidMain`, `iosMain`, `jvmMain`) provides its own `actual` implementation, wiring up the local **Ollama** server (`http://localhost:11434`). This is the bridge between the shared agent code and the platform-specific AI runtime.

### 3. HTTP API Client - `EmployeeApi.kt`

A Ktor `HttpClient` that talks to the backend server. Exposes three suspend functions:

```kotlin
suspend fun getEmployees(): List<EmployeeItem>
suspend fun getEmployeeById(id: Int): EmployeeDetails
suspend fun addEmployee(employee: EmployeeDetails): ResponseMessage
```

> **Note:** The base URL is `http://192.168.0.3:8080`. Update this to your machine's local IP address
> For the Android emulator, use `http://10.0.2.2:8080` instead.

---

### How an AI Agent Works (conceptually)

```
User message -> Agent -> LLM decides which Tool to Call -> Tool executes -> Result returned to LLM -> LLM answer user
```


The agent has:
- A **system prompt** that defines it's personality and role
- A **tool registry** listing what actions it can take
- A **prompt executor** that sends message to the LLM

### Agent Tools - `EmployeeTools.kt`

Each tool extends `Tool<Args, Result>` and wraps a call to `EmployeeApi`. The LLM reads the `name` and `description` fields to decide when to use each tool.

| Tool class | Args | Result | Description |
|---|---|---|---|
| `GetEmployeeTool` | `Unit` (none) | `List<EmployeeItem>` | Fetches all employees | 
| `GetEmployeeByIdTool` | `GetEmployeeArgs(id)` | `EmployeeDetails` | Fetches one employee by ID |
| `AddEmployeeTool` | `AddEmployeeArgs(name. designation, department, salary)` | `ResponseMessage` | Adds a new employee |

**Example tool definition:**
```kotlin
class GetEmployeeByTool(private val api: EmployeeApi)
      : Tool<GetEmployeeArgs, EmployeeDetails>(
      name = "Get Employee By Id",
      description = "Retrieves detailed information about a specific employee using their ID."
){
    overrride suspend fun execute(args: GetEmployeeArgs): EmployeeDetails = 
        api.getEmployeeById(args.id)
}   
```

### Agent Provider - `EmployeeAgentProvider.kt`

Assembles everything into a ready-to-use `AIAgent`:

```kotlin
class EmployeeAgentProvider {

    val toolRegistry = ToolRegistry {
        tool(GetEmployeesTool(api))
        tool(GetEmployeeByIdTool(api))
        tool(AddEmployeeTool(api))
    }

    fun provideAgent(): AIAgent<String, String> = AIAgent(
        toolRegister = toolRegistry,
        promptExecutor = getPlatform().promptExecutor,
        llmModel = getPlatform().llmModel,
        systemPrompt = "You are a helpful HR assistant. Use the provided tools to fetch employee data when asked."
    )
}
```

---

## UI Screens - `ui/`

Built with **Compose Mltiplatform**, navigation is handled by `App.kt` using `andoridx.navigation`.

| Screen | Route | Description |
|---|---|---|
| `EmployeeScreen` | `EmployeeListRoute` | Shows all employees; FAB opens the AI chat |
| `EmployeeDetailScreen` | `EmployeeDetailRoute(id)` | Shows full details for one employee |
| `ChatScreen` | `ChatRoute` | Chat UI - creates and `EmployeeAgentProvider`, sends user message to the agent, and displays response |

The **Chat screen** is where the agent comes to life:
```kotlin
val agent = remember { EmployeeAgentProvider().provideAgent() }
// On send:
val response = agent.run(userMessage)
```

---

### Backend Server - `server/`

A **Ktor** server exposing a simple REST API for employee data:

| Method | Endpoint | Description |
|---|---|---|
| `GET` | `/employees` | Returns `List<EmployeeItem>` |
| `GET` | '/employee/{id}' | Returns 'EmployeeDetails' for one employee | 
| `POST` | `/employee` | Adds a new employee|

---

### Running the project

### Prerequisites
- **Ollama** installed and running locally: `ollama server`
- A model pulled, e.g.: `ollama pull llama3`
- Update `BASE_URL` in `EmployeeApi.kt` and the Ollama URL in your platform's `getPlatform()` to match your network.

### Run commands

| Target | Command |
|---|---|
| Backend server | `./gradlew :server.run` |
| Android app | `./gradlew :app:androidApp:assembleDebug` |
| Desktop app | `../gradlew :app:desktopApp:run` |
| Desktop (hot reload) | `./gradlew :app:desktopApp:hotRun --auto` |
| iOS app | Open [`/app/iosApp`](./app/iosApp/) in Xcode and run |

### Run tests

| Target | Command |
|---|---|
| Android | `./gradlew :app:shared:testAndroidHostTest` |
| Desktop | `./gradlew :app:shared:jvmTest` |
| Server | `./gradlew :server:test` |
| iOS | `./gradlew :app:shared:iosSimulatorArm64Test` |

---

## key Libraries

| Library | Purpose |
|---|---|
| [Kotlin Multiplatform](https://www.jetbrains.com/help/kotlin-multiplatform-dev/get-started.html) | Share code across Android, iOS, Desktop |
| [Compose Multiplatform](https://www.jetbrains.com/lp/compose-multiplatform/) | Shared declartion UI |
| [Koog](https://github.com/JetBrains/koog) (`ai.koog.agents`) | AI agent famework - tools, LLM orchestration |
| [Ktor Client](https://ktor.io/docs/client-create-new-application.html) | HTTP calls to the backend |
| [Ktor Server](https://ktor.io/) | Backend REST API |
| [Ollama](https://ollama.com/) | Local LLM runtime |
