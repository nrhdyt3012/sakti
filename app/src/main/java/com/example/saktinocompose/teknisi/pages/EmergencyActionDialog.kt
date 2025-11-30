package com.example.saktinocompose.teknisi.pages

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.saktinocompose.data.entity.ChangeRequest
import com.example.saktinocompose.viewmodel.ChangeRequestViewModel
import com.example.saktinocompose.viewmodel.ApprovalHistoryViewModel
import com.example.saktinocompose.viewmodel.NotificationViewModel
import androidx.compose.ui.res.stringResource
import com.example.saktinocompose.R
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EmergencyActionDialog(
    changeRequest: ChangeRequest,
    teknisiId: Int,
    teknisiName: String,
    onDismiss: () -> Unit,
    onSuccess: () -> Unit,
    changeRequestViewModel: ChangeRequestViewModel = viewModel(),
    approvalHistoryViewModel: ApprovalHistoryViewModel = viewModel(),
    notificationViewModel: NotificationViewModel = viewModel()
) {
    var selectedAction by remember { mutableStateOf<String?>(null) }
    var notes by remember { mutableStateOf("") }
    var isProcessing by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = { if (!isProcessing) onDismiss() },
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Error,
                    contentDescription = "Emergency",
                    tint = Color(0xFFFF5722)
                )
                Text(
                    text = stringResource(R.string.emergency_action),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Info Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFFFF5722).copy(alpha = 0.1f)
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.emergency_change_request),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFFF5722)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = stringResource(R.string.emergency_direct_status),
                            fontSize = 11.sp,
                            color = Color.Gray
                        )
                    }
                }

                Text(
                    text = stringResource(R.string.select_final_status),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold
                )

                // Completed Option
                Card(
                    modifier = Modifier
                        .fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = if (selectedAction == "Completed")
                            Color(0xFF4CAF50).copy(alpha = 0.2f)
                        else
                            Color.White
                    ),
                    shape = RoundedCornerShape(8.dp),
                    onClick = { selectedAction = "Completed" }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        RadioButton(
                            selected = selectedAction == "Completed",
                            onClick = { selectedAction = "Completed" }
                        )
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Success",
                            tint = Color(0xFF4CAF50)
                        )
                        Column {
                            Text(
                                text = "Completed",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF4CAF50)
                            )
                            Text(
                                text = stringResource(R.string.change_successfully_implemented),
                                fontSize = 11.sp,
                                color = Color.Gray
                            )
                        }
                    }
                }

                // Failed Option
                Card(
                    modifier = Modifier
                        .fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = if (selectedAction == "Failed")
                            Color(0xFFD32F2F).copy(alpha = 0.2f)
                        else
                            Color.White
                    ),
                    shape = RoundedCornerShape(8.dp),
                    onClick = { selectedAction = "Failed" }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        RadioButton(
                            selected = selectedAction == "Failed",
                            onClick = { selectedAction = "Failed" }
                        )
                        Icon(
                            imageVector = Icons.Default.Error,
                            contentDescription = "Failed",
                            tint = Color(0xFFD32F2F)
                        )
                        Column {
                            Text(
                                text = "Failed",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFD32F2F)
                            )
                            Text(
                                text = stringResource(R.string.change_failed_to_implement),
                                fontSize = 11.sp,
                                color = Color.Gray
                            )
                        }
                    }
                }

                // Notes
                Text(
                    text = stringResource(R.string.notes),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold
                )
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                    placeholder = { Text("Add a note about implementation...") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White
                    )
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (selectedAction != null) {
                        isProcessing = true

                        // Update status
                        changeRequestViewModel.updateChangeRequestStatus(
                            changeRequest = changeRequest,
                            newStatus = selectedAction!!
                        )

                        // Add approval history
                        approvalHistoryViewModel.addApprovalHistory(
                            changeRequestId = changeRequest.id,
                            approverUserId = teknisiId,
                            approverName = teknisiName,
                            fromStatus = changeRequest.status,
                            toStatus = selectedAction!!,
                            notes = "EMERGENCY: ${if (notes.isBlank()) "No notes" else notes}"
                        )

                        // Kirim notifikasi ke end user
                        notificationViewModel.createNotification(
                            userId = changeRequest.userId,
                            changeRequestId = changeRequest.id,
                            ticketId = changeRequest.ticketId,
                            fromStatus = changeRequest.status,
                            toStatus = selectedAction!!
                        )

                        onSuccess()
                    }
                },
                enabled = selectedAction != null && !isProcessing,
                colors = ButtonDefaults.buttonColors(
                    containerColor = when (selectedAction) {
                        "Completed" -> Color(0xFF4CAF50)
                        "Failed" -> Color(0xFFD32F2F)
                        else -> Color.Gray
                    }
                )
            ) {
                if (isProcessing) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(stringResource(R.string.save))
                }
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                enabled = !isProcessing
            ) {
                Text("Cancel")
            }
        }
    )
}