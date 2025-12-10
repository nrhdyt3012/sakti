// File: app/src/main/java/com/example/saktinocompose/teknisi/pages/NotificationPage.kt

package com.example.saktinocompose.teknisi.pages

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.saktinocompose.network.dto.NotificationItem
import com.example.saktinocompose.utils.NetworkHelper
import com.example.saktinocompose.viewmodel.NotificationViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterialApi::class)
@Composable
fun NotificationPage(
    onBackClick: () -> Unit,
    onNotificationClick: (String) -> Unit = {}, // Navigate to CR detail with crId
    modifier: Modifier = Modifier,
    viewModel: NotificationViewModel = viewModel()
) {
    val context = LocalContext.current
    val notifications by viewModel.notifications.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val unreadCount by viewModel.unreadCount.collectAsState()

    // Check internet
    var isOnline by remember { mutableStateOf(NetworkHelper.isInternetAvailable(context)) }
    var showMarkAllDialog by remember { mutableStateOf(false) }
    var selectedFilter by remember { mutableStateOf(NotificationFilter.ALL) }

    LaunchedEffect(Unit) {
        while (true) {
            isOnline = NetworkHelper.isInternetAvailable(context)
            kotlinx.coroutines.delay(3000)
        }
    }

    // Pull to refresh
    val refreshing by remember { derivedStateOf { isLoading } }
    val pullRefreshState = rememberPullRefreshState(
        refreshing = refreshing,
        onRefresh = {
            if (isOnline) {
                viewModel.refreshNotifications()
            }
        }
    )

    // Mark All Dialog
    if (showMarkAllDialog) {
        AlertDialog(
            onDismissRequest = { showMarkAllDialog = false },
            title = { Text("Mark All as Read") },
            text = { Text("Mark all ${unreadCount} notifications as read?") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.markAllAsRead()
                        showMarkAllDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF4CAF50)
                    )
                ) {
                    Text("Yes")
                }
            },
            dismissButton = {
                TextButton(onClick = { showMarkAllDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .pullRefresh(pullRefreshState)
    ) {
        Column(
            modifier = modifier
                .fillMaxSize()
                .background(Color(0xFFF5F5F5))
        ) {
            // Top Bar
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text("Notifications", fontWeight = FontWeight.Bold)

                        if (unreadCount > 0) {
                            Badge(
                                containerColor = Color(0xFFD32F2F)
                            ) {
                                Text(
                                    text = if (unreadCount > 99) "99+" else unreadCount.toString(),
                                    fontSize = 11.sp,
                                    color = Color.White
                                )
                            }
                        }

                        if (!isOnline) {
                            Card(
                                colors = CardDefaults.cardColors(
                                    containerColor = Color(0xFFD32F2F)
                                ),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        Icons.Default.CloudOff,
                                        contentDescription = "Offline",
                                        modifier = Modifier.size(14.dp),
                                        tint = Color.White
                                    )
                                    Text(
                                        "Offline",
                                        fontSize = 11.sp,
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    // Mark all as read
                    if (unreadCount > 0) {
                        IconButton(onClick = { showMarkAllDialog = true }) {
                            Icon(
                                Icons.Default.DoneAll,
                                contentDescription = "Mark all as read",
                                tint = Color.White
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF384E66),
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )

            // Offline Warning
            if (!isOnline) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFFFF9800).copy(alpha = 0.1f)
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            Icons.Default.CloudOff,
                            contentDescription = null,
                            tint = Color(0xFFFF9800)
                        )
                        Text(
                            "You're offline. Showing cached notifications.",
                            fontSize = 13.sp,
                            color = Color.Gray
                        )
                    }
                }
            }

            // Error Message
            error?.let { errorMsg ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFFD32F2F).copy(alpha = 0.1f)
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            modifier = Modifier.weight(1f),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                Icons.Default.Error,
                                contentDescription = null,
                                tint = Color(0xFFD32F2F)
                            )
                            Text(
                                errorMsg,
                                fontSize = 13.sp,
                                color = Color(0xFFD32F2F)
                            )
                        }
                        IconButton(onClick = { viewModel.clearError() }) {
                            Icon(Icons.Default.Close, contentDescription = "Close")
                        }
                    }
                }
            }

            // Filter Tabs
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                NotificationFilter.values().forEach { filter ->
                    FilterChip(
                        selected = selectedFilter == filter,
                        onClick = { selectedFilter = filter },
                        label = {
                            Text(
                                text = when (filter) {
                                    NotificationFilter.ALL -> "All (${notifications.size})"
                                    NotificationFilter.UNREAD -> "Unread ($unreadCount)"
                                    NotificationFilter.READ -> "Read (${notifications.size - unreadCount})"
                                }
                            )
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = when (filter) {
                                    NotificationFilter.ALL -> Icons.Default.Inbox
                                    NotificationFilter.UNREAD -> Icons.Default.MarkEmailUnread
                                    NotificationFilter.READ -> Icons.Default.DoneAll
                                },
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    )
                }
            }

            // Notifications List
            val filteredNotifications = remember(notifications, selectedFilter) {
                when (selectedFilter) {
                    NotificationFilter.ALL -> notifications
                    NotificationFilter.UNREAD -> notifications.filter { !it.isRead }
                    NotificationFilter.READ -> notifications.filter { it.isRead }
                }
            }

            if (filteredNotifications.isEmpty() && !isLoading) {
                // Empty State
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Icon(
                            Icons.Default.Notifications,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = Color.Gray
                        )
                        Text(
                            text = when (selectedFilter) {
                                NotificationFilter.ALL -> "No notifications yet"
                                NotificationFilter.UNREAD -> "No unread notifications"
                                NotificationFilter.READ -> "No read notifications"
                            },
                            fontSize = 16.sp,
                            color = Color.Gray
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(filteredNotifications) { notification ->
                        NotificationCard(
                            notification = notification,
                            onClick = {
                                if (!notification.isRead) {
                                    viewModel.markAsRead(notification.id, notification.crId)
                                }
                                notification.crId?.let { onNotificationClick(it) }
                            }
                        )
                    }

                    item {
                        Spacer(modifier = Modifier.height(80.dp))
                    }
                }
            }
        }

        // Pull Refresh Indicator
        PullRefreshIndicator(
            refreshing = refreshing,
            state = pullRefreshState,
            modifier = Modifier.align(Alignment.TopCenter),
            backgroundColor = Color.White,
            contentColor = Color(0xFF384E66)
        )
    }
}

@Composable
fun NotificationCard(
    notification: NotificationItem,
    onClick: () -> Unit
) {
    val dateFormat = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault())
    val timestamp = try {
        val date = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
            .parse(notification.createdAt)
        dateFormat.format(date ?: Date())
    } catch (e: Exception) {
        notification.createdAt
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = if (notification.isRead) Color.White else Color(0xFFE3F2FD)
        ),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Unread indicator
            if (!notification.isRead) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .background(Color(0xFF2196F3), CircleShape)
                        .align(Alignment.Top)
                )
            } else {
                Spacer(modifier = Modifier.width(10.dp))
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Title
                Text(
                    text = notification.title,
                    fontSize = 15.sp,
                    fontWeight = if (notification.isRead) FontWeight.Medium else FontWeight.Bold,
                    color = Color.Black
                )

                // Body
                Text(
                    text = notification.body,
                    fontSize = 14.sp,
                    color = Color.Gray,
                    maxLines = 3
                )

                // Footer: Time & CR ID
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Schedule,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = Color.Gray
                        )
                        Text(
                            text = timestamp,
                            fontSize = 12.sp,
                            color = Color.Gray
                        )
                    }

                    notification.crId?.let { crId ->
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = Color(0xFF384E66).copy(alpha = 0.1f)
                            ),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = "CR: $crId",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF384E66),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }

            // Arrow icon
            Icon(
                Icons.Default.ChevronRight,
                contentDescription = "Open",
                tint = Color.Gray
            )
        }
    }
}

enum class NotificationFilter {
    ALL, UNREAD, READ
}