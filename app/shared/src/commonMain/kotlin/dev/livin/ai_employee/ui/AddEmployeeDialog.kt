package dev.livin.ai_employee.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import dev.livin.ai_employee.model.EmployeeDetails

@Composable
fun AddEmployeeDialog(
    showDialog: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (EmployeeDetails) -> Unit
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
                modifier = Modifier.padding(0.dp)
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
                                val data = EmployeeDetails(
                                    name = nameInput,
                                    designation = designationInput,
                                    department = departmentInput,
                                    salary = salaryInput.toDouble()
                                )
                                onConfirm(data)
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