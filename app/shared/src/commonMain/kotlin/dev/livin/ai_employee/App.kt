package dev.livin.ai_employee

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import dev.livin.ai_employee.model.EmployeeItem
import dev.livin.ai_employee.ui.ChatScreen
import dev.livin.ai_employee.ui.EmployeeDetailScreen
import dev.livin.ai_employee.ui.EmployeesScreen
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable

@Serializable
object EmployeeListRoute
@Serializable
object ChatRoute

@Serializable
data class EmployeeDetailRoute(val id: Int) // Only pass the ID now


@Composable
fun App() {
    MaterialTheme {
        // 2. State-Hoisted Backstack (The core of Navigation 3)
        // We use a mutableStateListOf to act as our backstack.
        val navController = rememberNavController()

        val scope = rememberCoroutineScope()
        var employees by remember { mutableStateOf(emptyList<EmployeeItem>()) }

        NavHost(
            navController = navController,
            startDestination = EmployeeListRoute
        ) {
            composable<EmployeeListRoute> {

                // This triggers every time the user comes back to this screen
                LifecycleResumeEffect(Unit) {
                    val job = scope.launch {
                        try {
                            employees = EmployeeApi().getEmployees()
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                    onPauseOrDispose {
                        job.cancel()
                    }
                }

                EmployeesScreen(
                    employees = employees,
                    onEmployeeClick = { emp -> navController.navigate(EmployeeDetailRoute(emp.id)) },
                    onChatClick = { navController.navigate(ChatRoute) } // Handle FAB click
                )
            }
            composable<ChatRoute> {
                ChatScreen(onBackClick = { navController.popBackStack() })
            }

            composable<EmployeeDetailRoute> { backStackEntry ->
                val route = backStackEntry.toRoute<EmployeeDetailRoute>()
                // Pass the ID to the screen
                EmployeeDetailScreen(
                    employeeId = route.id,
                    onBackClick = { navController.popBackStack() }
                )
            }
        }
    }
}