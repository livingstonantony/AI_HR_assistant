package dev.livin.ai_employee

import dev.livin.ai_employee.mcp.buildEmployeeMCPServer
import dev.livin.ai_employee.repository.EmployeeRepository
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.testing.*
import kotlinx.coroutines.runBlocking
import kotlin.test.*

class ApplicationTest {

    @Test
    fun testRoot() = testApplication {
        application {
            module(dev.livin.ai_employee.repository.EmployeeRepository())
        }
        val response = client.get("/")
        assertEquals(HttpStatusCode.OK, response.status)
        assertEquals("Hello, Ktor!", response.bodyAsText())
    }

    @Test
    fun testMCPTools() = runBlocking {
        val repo = EmployeeRepository()
        val server = buildEmployeeMCPServer(repo)

    }
}