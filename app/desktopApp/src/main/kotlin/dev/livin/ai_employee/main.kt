package dev.livin.ai_employee

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "AI_Employee",
    ) {
        App()
    }
}