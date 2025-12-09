package com.example.saktinocompose.teknisi.pages

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.saktinocompose.data.model.ChangeRequest

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImplementationPlanDialog(
    changeRequest: ChangeRequest,
    onDismiss: () -> Unit,
    onSave: (implementationPlan: String, rollbackPlan: String) -> Unit
) {
    var implementationPlan by remember {
        mutableStateOf(changeRequest.rencanaImplementasi)
    }
    var rollbackPlan by remember {
        mutableStateOf(changeRequest.rollbackPlan)
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Description,
                    contentDescription = "Implementation Plan",
                    tint = Color(0xFF2196F3)
                )
                Text(
                    text = "Implementation Plan",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 500.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Info Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFF2196F3).copy(alpha = 0.1f)
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "Ticket: ${changeRequest.ticketId}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF2196F3)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Scheduled: ${changeRequest.scheduledDate}",
                            fontSize = 11.sp,
                            color = Color.Gray
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Please provide implementation and rollback plans before starting",
                            fontSize = 11.sp,
                            color = Color.Gray
                        )
                    }
                }

                // Implementation Plan
                Text(
                    text = "1. Implementation Plan *",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "Describe step-by-step how the change will be implemented",
                    fontSize = 12.sp,
                    color = Color.Gray
                )
                OutlinedTextField(
                    value = implementationPlan,
                    onValueChange = { implementationPlan = it },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 5,
                    placeholder = {
                        Text(
                            """Example:
1. Backup current configuration
2. Stop affected services
3. Apply changes
4. Test functionality
5. Start services
6. Verify operation""".trimIndent()
                        )
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White
                    )
                )

                // Rollback Plan
                Text(
                    text = "2. Rollback Plan *",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "Describe how to revert the change if implementation fails",
                    fontSize = 12.sp,
                    color = Color.Gray
                )
                OutlinedTextField(
                    value = rollbackPlan,
                    onValueChange = { rollbackPlan = it },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 4,
                    placeholder = {
                        Text(
                            """Example:
1. Stop services
2. Restore backup configuration
3. Restart services
4. Verify rollback success""".trimIndent()
                        )
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White
                    )
                )

                // Warning Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFFFF9800).copy(alpha = 0.1f)
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            Icons.Default.Info,
                            contentDescription = null,
                            tint = Color(0xFFFF9800),
                            modifier = Modifier.size(20.dp)
                        )
                        Column {
                            Text(
                                "Important",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFFF9800)
                            )
                            Text(
                                "These plans will guide the implementation process and help in case of failures",
                                fontSize = 11.sp,
                                color = Color.Gray
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (implementationPlan.isNotBlank() && rollbackPlan.isNotBlank()) {
                        onSave(implementationPlan, rollbackPlan)
                    }
                },
                enabled = implementationPlan.isNotBlank() && rollbackPlan.isNotBlank(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF2196F3)
                )
            ) {
                Text("Save Plans")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}