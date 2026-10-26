package dev.livin.ai_employee.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Science
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dev.livin.ai_employee.model.EmployeeDetails
import dev.livin.ai_employee.model.EmployeeItem

@Composable
fun EmployeesScreen(
    employees: List<EmployeeItem>,
    onChatClick: () -> Unit,
    onMcpExplorerClick: () -> Unit,
    onEmployeeClick: (EmployeeItem) -> Unit,
    onConfirm: (EmployeeDetails) -> Unit,
    onDelete: (Int) -> Unit = {}

) {

    var isAddUserDialogVisible by remember { mutableStateOf(false) }

    Scaffold(
        floatingActionButton = {

            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalAlignment = Alignment.End
            ) {
/*                ExtendedFloatingActionButton(
                    onClick = onMcpExplorerClick,
                    icon = { Icon(Icons.Filled.Science, contentDescription = null) },
                    text = { Text("MCP Explorer") },
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                )*/
                FloatingActionButton(onClick = {
                    isAddUserDialogVisible = true
                }) {
                    Icon(Icons.Filled.Add, contentDescription = "Add User")
                }
                FloatingActionButton(onClick = onChatClick) {
                    Icon(Icons.Default.Chat, contentDescription = "Chat with AI")
                }
            }
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .safeContentPadding(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Employee Directory",
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(16.dp)
            )

            AddEmployeeDialog(
                showDialog = isAddUserDialogVisible,
                onDismiss = { isAddUserDialogVisible = false },
                onConfirm = onConfirm
            )

            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(0.dp),
            ) {
                itemsIndexed(employees) { index, employee ->

//                    val employee = employees[index]
                    EmployeeItem(
                        employee,
                        onDelete = onDelete,
                        onClick = { onEmployeeClick(employee) }
                    )

                    HorizontalDivider(
                        thickness = 1.dp,
                        color = Color.LightGray
                    )

                }
            }
        }
    }
}


@Composable
fun EmployeeItem(employee: EmployeeItem,  onDelete: (Int) -> Unit,onClick: () -> Unit,) {
    Column(
        modifier = Modifier
            .clickable {
                onClick()
            }
            .fillMaxWidth()
//        onClick = onClick,
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary),
                contentAlignment = Alignment.Center

            ) {
                Text(
                    text = employee.id.toString(),
                    color = MaterialTheme.colorScheme.onPrimary
                )

            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = employee.name,
                style = MaterialTheme.typography.titleLarge
            )
            Spacer(modifier = Modifier.weight(1f))
            IconButton(
                onClick = { onDelete(employee.id) },
                modifier = Modifier.size(24.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete ${employee.name}"
                )
            }


        }

    }
}


@Preview
@Composable
fun EmployeesScreenPreview() {
    MaterialTheme {
        EmployeesScreen(
            employees = listOf(
                EmployeeItem(1, "John Doe"),
                EmployeeItem(2, "Jane Smith"),
                EmployeeItem(3, "Alice Johnson")
            ),
            onChatClick = {},
            onMcpExplorerClick = {},
            onEmployeeClick = {},
            onConfirm = {}
        )
    }
}

@Preview
@Composable
fun EmployeeItem_Preview() {
    MaterialTheme {
        EmployeeItem(employee = EmployeeItem(1, "John"), onDelete = {}) {

        }
    }
}



