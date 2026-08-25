package dev.livin.ai_employee

import ai.koog.http.client.ktor.KtorKoogHttpClient
import ai.koog.prompt.executor.llms.all.simpleOllamaAIExecutor
import ai.koog.prompt.executor.model.PromptExecutor
import ai.koog.prompt.executor.ollama.client.OllamaModels
import ai.koog.prompt.llm.LLModel
import android.os.Build

class AndroidPlatform : Platform {
    override val BASE_URL: String
        get() = "http://10.0.2.2:8080"
    override val name: String = "Android ${Build.VERSION.SDK_INT}"
    override val llmModel: LLModel = OllamaModels.Meta.LLAMA_3_2
    override val promptExecutor: PromptExecutor = simpleOllamaAIExecutor("http://10.0.2.2:11434",httpClientFactory = KtorKoogHttpClient.Factory())
}

actual fun getPlatform(): Platform = AndroidPlatform()