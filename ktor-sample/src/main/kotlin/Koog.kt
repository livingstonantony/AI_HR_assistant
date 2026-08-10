package com.example

import ai.koog.ktor.Koog
import ai.koog.ktor.aiAgent
import ai.koog.prompt.executor.clients.openai.OpenAIModels
import ai.koog.prompt.executor.ollama.client.OllamaModels
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Application.configureKoog() {
    install(Koog) {
        llm {
            openAI(apiKey = "your-openai-api-key")
            anthropic(apiKey = "your-anthropic-api-key")
            ollama { baseUrl = "http://localhost:11434" }
            google(apiKey = "your-google-api-key")
            openRouter(apiKey = "your-openrouter-api-key")
            deepSeek(apiKey = "your-deepseek-api-key")
        }
    }

    routing {
        route("/ai") {
            post("/chat") {
                val userInput = call.receive<String>()
                val output = aiAgent(userInput, model = OllamaModels.Meta.LLAMA_3_2)
                call.respondText(output)
            }
        }
    }
}
