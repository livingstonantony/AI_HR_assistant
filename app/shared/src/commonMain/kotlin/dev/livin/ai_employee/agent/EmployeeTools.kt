package dev.livin.ai_employee.agent

import ai.koog.agents.core.tools.Tool
import ai.koog.serialization.typeToken
import dev.livin.ai_employee.EmployeeApi
import dev.livin.ai_employee.model.Employee
import dev.livin.ai_employee.model.EmployeeDetails
import dev.livin.ai_employee.model.EmployeeItem
import dev.livin.ai_employee.model.ResponseMessage
import kotlinx.serialization.Serializable



@Serializable
data class DeleteEmployeeArgs(val id: Int)

class DeleteEmployeeByIDTool(private val api: EmployeeApi) : Tool<DeleteEmployeeArgs, ResponseMessage>(
    argsType = typeToken<DeleteEmployeeArgs>(),
    resultType = typeToken<ResponseMessage>(),
    name = "Delete Employee",
    description = "Deletes an employee by their ID."
) {
    override suspend fun execute(args: DeleteEmployeeArgs): ResponseMessage = api.deleteEmployeeById(args.id)
}

/*class GetEmployeesTool(private val api: EmployeeApi) : Tool<Unit, List<EmployeeItem>>(
    argsType = typeToken<Unit>(),
    resultType = typeToken<List<EmployeeItem>>(),
    name = "Get All Employees",
    description = "Retrieves all employees id and name using the provided API: api.getEomployees() that returns list of EmployeeItem(id, name)"
) {
    override suspend fun execute(args: Unit): List<EmployeeItem> {
        return api.getEmployees()
    }
}*/

/*
// 1. Define a wrapper for the arguments
@Serializable
data class GetEmployeeArgs(val id: Int)


class GetEmployeeByIdTool(private val api: EmployeeApi) : Tool<GetEmployeeArgs, EmployeeDetails>(
    argsType = typeToken<GetEmployeeArgs>(),
    resultType = typeToken<EmployeeDetails>(),
    name = "Get Employee By Id",
    description = "Retrieves detailed information about a specific employee using their ID."
) {
    override suspend fun execute(args: GetEmployeeArgs): EmployeeDetails = api.getEmployeeById(args.id)
}

@Serializable
data class AddEmployeeArgs(

    val name: String,
    val designation: String,
    val department: String,
    val salary: Double
)

class AddEmployeeTool(private val api: EmployeeApi) : Tool<AddEmployeeArgs, ResponseMessage>(
    argsType = typeToken<AddEmployeeArgs>(),
    resultType = typeToken<ResponseMessage>(),
    name = "Add Employee",
    description = "Add a new employee to the company by the given details of [name, designation, department, salary]"
) {
    override suspend fun execute(args: AddEmployeeArgs): ResponseMessage = api.addEmployee(
        EmployeeDetails(
            name = args.name,
            designation = args.designation,
            department = args.department,
            salary = args.salary
        )
    )
}
*/
