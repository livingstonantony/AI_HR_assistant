package dev.livin.ai_employee

import ai.koog.http.client.ktor.KtorKoogHttpClient
import ai.koog.prompt.executor.llms.all.simpleOllamaAIExecutor
import ai.koog.prompt.executor.model.PromptExecutor
import ai.koog.prompt.executor.ollama.client.OllamaModels
import ai.koog.prompt.llm.LLModel

class JVMPlatform: Platform {
    override val BASE_URL: String
        get() = "http://localhost:8080"
    override val name: String = "Java ${System.getProperty("java.version")}"
    override val llmModel: LLModel = OllamaModels.Meta.LLAMA_3_2
    override val promptExecutor: PromptExecutor = simpleOllamaAIExecutor("http://localhost:11434")
}

actual fun getPlatform(): Platform = JVMPlatform()