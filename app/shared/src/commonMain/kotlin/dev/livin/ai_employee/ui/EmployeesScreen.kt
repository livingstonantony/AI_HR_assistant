package dev.livin.ai_employee.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Science
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import dev.livin.ai_employee.model.EmployeeItem

@Composable
fun EmployeesScreen(
    employees: List<EmployeeItem>,
    onChatClick: () -> Unit,
    onMcpExplorerClick: () -> Unit,
    onEmployeeClick: (EmployeeItem) -> Unit
) {

    var isAddUserDialogVisible by remember { mutableStateOf(false) }

    Scaffold(
        floatingActionButton = {

            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalAlignment = Alignment.End
            ) {
                ExtendedFloatingActionButton(
                    onClick = onMcpExplorerClick,
                    icon = { Icon(Icons.Filled.Science, contentDescription = null) },
                    text = { Text("MCP Explorer") },
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                )
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

            AddUserDialogVisible(
                showDialog = isAddUserDialogVisible,
                onDismiss = { isAddUserDialogVisible = false },
                onConfirm = { name ->
                    // Handle the new employee name here
                    println("New employee name: $name")
                }
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
fun EmployeeItem(employee: EmployeeItem, onClick: () -> Unit) {
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

        }

    }
}


@Composable
fun AddUserDialogVisible(
    showDialog: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var nameInput by remember { mutableStateOf("") }
    var designationInput by remember { mutableStateOf("") }
    var departmentInput by remember { mutableStateOf("") }
    var salaryInput by remember { mutableStateOf("") }

    val focusManager = LocalFocusManager.current

    val isFormValid = nameInput.isNotBlank() &&
            designationInput.isNotBlank() &&
            departmentInput.isNotBlank() &&
            salaryInput.isNotBlank()

    if (showDialog) {
        Dialog(onDismissRequest = onDismiss) {
            Card(
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.padding(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Enter user details",
                        style = MaterialTheme.typography.titleMedium
                    )

                    // Name
                    TextField(
                        modifier = Modifier.fillMaxWidth(),
                        value = nameInput,
                        onValueChange = { nameInput = it },
                        label = { Text("Name *") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Text,
                            imeAction = ImeAction.Next
                        ),
                        keyboardActions = KeyboardActions(
                            onNext = {
                                focusManager.moveFocus(FocusDirection.Down)
                            }
                        )
                    )

                    // Designation
                    TextField(
                        modifier = Modifier.fillMaxWidth(),
                        value = designationInput,
                        onValueChange = { designationInput = it },
                        label = { Text("Designation *") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Text,
                            imeAction = ImeAction.Next
                        ),
                        keyboardActions = KeyboardActions(
                            onNext = {
                                focusManager.moveFocus(FocusDirection.Down)
                            }
                        )
                    )

                    // Department
                    TextField(
                        modifier = Modifier.fillMaxWidth(),
                        value = departmentInput,
                        onValueChange = { departmentInput = it },
                        label = { Text("Department *") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Text,
                            imeAction = ImeAction.Next
                        ),
                        keyboardActions = KeyboardActions(
                            onNext = {
                                focusManager.moveFocus(FocusDirection.Down)
                            }
                        )
                    )

                    // Salary
                    TextField(
                        modifier = Modifier.fillMaxWidth(),
                        value = salaryInput,
                        onValueChange = { newValue ->
                            if (newValue.all { it.isDigit() }) {
                                salaryInput = newValue
                            }
                        },
                        label = { Text("Salary *") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number,
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = {
                                focusManager.clearFocus()
                            }
                        )
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = onDismiss) {
                            Text("Cancel")
                        }

                        Button(
                            enabled = isFormValid,
                            onClick = {
                                onConfirm(nameInput.trim())
                                focusManager.clearFocus()
                                onDismiss()
                            }
                        ) {
                            Text("Submit")
                        }
                    }
                }
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
        )
    }
}

@Preview
@Composable
fun EmployeeItem_Preview() {
    MaterialTheme {
        EmployeeItem(employee = EmployeeItem(1, "John")) {

        }
    }
}



