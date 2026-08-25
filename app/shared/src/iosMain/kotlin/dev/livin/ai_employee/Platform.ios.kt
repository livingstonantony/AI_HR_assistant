package dev.livin.ai_employee

import ai.koog.http.client.ktor.KtorKoogHttpClient
import ai.koog.prompt.executor.llms.all.simpleOllamaAIExecutor
import ai.koog.prompt.executor.model.PromptExecutor
import ai.koog.prompt.executor.ollama.client.OllamaModels
import ai.koog.prompt.llm.LLModel
import platform.UIKit.UIDevice

class IOSPlatform: Platform {
    override val BASE_URL: String
        get() = "http://192.168.0.4:8080"
    override val name: String = UIDevice.currentDevice.systemName() + " " + UIDevice.currentDevice.systemVersion
    override val llmModel: LLModel = OllamaModels.Meta.LLAMA_3_2
    override val promptExecutor: PromptExecutor = simpleOllamaAIExecutor("http://192.168.0.4:11434",httpClientFactory = KtorKoogHttpClient.Factory())
}

actual fun getPlatform(): Platform = IOSPlatform()