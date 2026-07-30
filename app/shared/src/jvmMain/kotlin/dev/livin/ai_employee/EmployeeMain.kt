package dev.livin.ai_employee

import dev.livin.ai_employee.agent.EmployeeAgentProvider

suspend fun main() {
    println("\nRunning HR assistant agent...\n")
    val agent = EmployeeAgentProvider().provideAgent()
    val response = agent.run("Get all employees from the company")

    println("")
    println(response)
    println("")
}