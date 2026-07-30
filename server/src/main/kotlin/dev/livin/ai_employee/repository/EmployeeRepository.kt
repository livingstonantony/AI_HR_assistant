package dev.livin.ai_employee.repository

import dev.livin.ai_employee.model.Employee
import dev.livin.ai_employee.model.EmployeeDetails


class EmployeeRepository {
    private val employees = mutableListOf(
        Employee(1, "John Doe", "Android Developer", "Mobile", 85000.0),
        Employee(2, "Alice Smith", "Backend Developer", "Platform", 90000.0),
        Employee(3, "David Brown", "QA Engineer", "Testing", 65000.0),
        Employee(4, "Sophia Wilson", "Project Manager", "Management", 120000.0),
        Employee(5, "Michael Chen", "UI/UX Designer", "Design", 70000.0),

    )


    fun getEmployees(): List<Employee> = employees

    fun getEmployee(id: Int): Employee? = employees.find { it.id == id }

    fun addEmployee(employee: EmployeeDetails) {
        val id = employees.size + 1
        val employee = Employee(id, employee.name, employee.designation, employee.department, employee.salary)
        employees.add(employee)
    }

}