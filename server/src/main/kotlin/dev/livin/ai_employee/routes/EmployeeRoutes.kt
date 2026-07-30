package dev.livin.ai_employee.routes

import dev.livin.ai_employee.model.EmployeeDetails
import dev.livin.ai_employee.model.EmployeeItem
import dev.livin.ai_employee.repository.EmployeeRepository


import io.ktor.http.*
import io.ktor.server.request.receive
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.coroutines.delay

fun Route.employeeRoutes() {

    val repository = EmployeeRepository()

    route("/") {

        // GET /employees
        get("employees") {

            val employees:List<EmployeeItem> = repository.getEmployees().map { data -> EmployeeItem(data.id, data.name) }
            call.respond(employees)
        }

        // GET /employee/1
        get("employee/{id}") {

            val id = call.parameters["id"]?.toIntOrNull()

            if (id == null) {
                call.respond(
                    HttpStatusCode.BadRequest,
                    mapOf("message" to "Invalid employee id")
                )
                return@get
            }

            val employee: EmployeeDetails? = repository.getEmployee(id).let {
                if (it == null) {
                    null
                } else {
                    EmployeeDetails(it.id, it.name, it.designation, it.department, it.salary)
                }
            }

            if (employee == null) {
                call.respond(
                    HttpStatusCode.NotFound,
                    mapOf("message" to "Employee not found")
                )
            } else {
                call.respond(employee)
            }
        }

        post("/employee") {
            try {
                val employee = call.receive<EmployeeDetails>()
                repository.addEmployee(employee)
                call.respond(
                    HttpStatusCode.Created,
                    mapOf("message" to "Employee added successfully")
                )

            }catch (e: Exception){
                call.respond(
                    HttpStatusCode.BadRequest,
                    mapOf("message" to "Invalid employee data: $e")
                )
            }
        }
    }
}