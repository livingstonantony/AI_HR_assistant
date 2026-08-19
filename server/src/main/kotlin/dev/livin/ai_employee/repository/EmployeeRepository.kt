package dev.livin.ai_employee.repository

import dev.livin.ai_employee.model.Employee
import dev.livin.ai_employee.model.EmployeeDetails


class EmployeeRepository {
    private val employees = mutableListOf(
        Employee(1, "K John Doe", "Android Developer", "Mobile", 85000.0),
        Employee(2, "K Alice Smith", "Backend Developer", "Platform", 90000.0),
        Employee(3, "K David Brown", "QA Engineer", "Testing", 65000.0),
        Employee(4, "K Sophia Wilson", "Project Manager", "Management", 12000.0),
        Employee(5, "K Michael Chen", "UI/UX Designer", "Design", 70000.0),

    )


    fun getEmployees(): List<Employee> = employees

    fun getEmployeeById(id: Int): Employee? = employees.find { it.id == id }

    fun addEmployee(employee: EmployeeDetails): Employee {
        val id = employees.size + 1
        val employee = Employee(id, employee.name, employee.designation, employee.department, employee.salary)
        employees.add(employee)
        return employee
    }

}