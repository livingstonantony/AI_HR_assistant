package dev.livin.ai_employee.model


import kotlinx.serialization.Serializable

@Serializable
data class Employee(
    val id: Int,
    val name: String,
    val designation: String,
    val department: String,
    val salary: Double
)


@Serializable
data class EmployeeItem(
    val id: Int,
    val name: String
)

@Serializable
data class EmployeeDetails(
    val id: Int?=0,
    val name: String,
    val designation: String,
    val department: String,
    val salary: Double
)

@Serializable
data class ResponseMessage(
    val message: String? = ""
)