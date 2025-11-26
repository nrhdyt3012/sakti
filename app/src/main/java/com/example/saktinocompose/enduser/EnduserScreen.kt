package com.example.saktinocompose.enduser

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.saktinocompose.menu.NavItem
import com.example.saktinocompose.R
import com.example.saktinocompose.data.entity.ChangeRequest
import com.example.saktinocompose.enduser.pages.*
import com.example.saktinocompose.viewmodel.NotificationViewModel
import com.example.saktinocompose.viewmodel.ChangeRequestViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EnduserScreen(
    userId: Int,
    userEmail: String,
    userName: String,
    userRole: String,
) {
    val navItemList = listOf(
        NavItem("Beranda", R.drawable.home),
        NavItem("Status", R.drawable.database)
    )

    val notificationViewModel: NotificationViewModel = viewModel()
    val changeRequestViewModel: ChangeRequestViewModel = viewModel()
    val scope = rememberCoroutineScope()
    val unreadCount by notificationViewModel.getUnreadCount(userId).collectAsState(initial = 0)

    var selectedIndex by remember { mutableIntStateOf(0) }
    var showProfile by remember { mutableStateOf(false) }
    var showNotification by remember { mutableStateOf(false) }
    var showFormInput by remember { mutableStateOf(false) }
    var showDetailForm by remember { mutableStateOf(false) }
    var showStatusHistory by remember { mutableStateOf(false) }
    var showFilteredList by remember { mutableStateOf(false) }
    var selectedChangeRequest by remember { mutableStateOf<ChangeRequest?>(null) }
    var filteredRequests by remember { mutableStateOf<List<ChangeRequest>>(emptyList()) }
    var filterType by remember { mutableStateOf<FilterType?>(null) }
    var showExitDialog by remember { mutableStateOf(false) }
    var revisionChangeRequest by remember { mutableStateOf<ChangeRequest?>(null) }
    var showRevisionForm by remember { mutableStateOf(false) }


    // Back Handler
    BackHandler {
        when {
            showProfile -> showProfile = false
            showNotification -> showNotification = false
            showRevisionForm -> showRevisionForm = false
            showFilteredList -> {
                showFilteredList = false
                filterType = null
            }
            showDetailForm -> {
                showDetailForm = false
                if (filterType != null) {
                    showFilteredList = true
                } else {
                    selectedChangeRequest = null
                }
            }
            showStatusHistory -> {
                showStatusHistory = false
                selectedChangeRequest = null
            }
            showFormInput -> showFormInput = false
            selectedIndex != 0 -> selectedIndex = 0
            else -> showExitDialog = true
        }
    }

    // Exit Dialog
    if (showExitDialog) {
        AlertDialog(
            onDismissRequest = { showExitDialog = false },
            title = { Text("Keluar Aplikasi") },
            text = { Text("Apakah Anda yakin ingin keluar dari aplikasi?") },
            confirmButton = {
                Button(
                    onClick = {
                        android.os.Process.killProcess(android.os.Process.myPid())
                    }
                ) {
                    Text("Ya")
                }
            },
            dismissButton = {
                TextButton(onClick = { showExitDialog = false }) {
                    Text("Tidak")
                }
            }
        )
    }

    when {
        showProfile -> {
            ProfilePage2(
                userId = userId,
                userEmail = userEmail,
                userName = userName,
                userRole = userRole,
                onBackClick = { showProfile = false }
            )
        }
        showNotification -> {
            NotificationPage(
                userId = userId,
                onBackClick = { showNotification = false },
                onNotificationClick = { changeRequestId ->
                    // Load change request dan cek status
                    showNotification = false
                    scope.launch {
                        val request = changeRequestViewModel.getChangeRequestById(changeRequestId)
                        if (request?.status == "Revision") {
                            // Buka form revision dengan data existing
                            revisionChangeRequest = request
                            showRevisionForm = true
                        } else {
                            // Buka detail biasa
                            selectedChangeRequest = request
                            showDetailForm = true
                        }
                    }
                }
            )
        }
        showRevisionForm && revisionChangeRequest != null -> {
            // Form untuk revisi dengan data pre-populated
            EnduserForm(
                userId = userId,
                userName = userName,
                existingRequest = revisionChangeRequest,
                onFormSubmitted = {
                    showRevisionForm = false
                    revisionChangeRequest = null
                    selectedIndex = 1 // Ke halaman status
                },
                onBackClick = {
                    showRevisionForm = false
                    revisionChangeRequest = null
                }
            )
        }
        showFilteredList && filterType != null -> {
            FilteredListPage(
                filterType = filterType!!,
                changeRequests = filteredRequests,
                onBackClick = {
                    showFilteredList = false
                    filterType = null
                },
                onDetailClick = { changeRequest ->
                    selectedChangeRequest = changeRequest
                    showDetailForm = true
                    showFilteredList = false
                }
            )
        }
        showDetailForm && selectedChangeRequest != null -> {
            FormDetailPage(
                changeRequest = selectedChangeRequest!!,
                onBackClick = {
                    showDetailForm = false
                    if (filterType != null) {
                        showFilteredList = true
                    } else {
                        selectedChangeRequest = null
                    }
                }
            )
        }
        showStatusHistory && selectedChangeRequest != null -> {
            StatusHistoryPage(
                changeRequest = selectedChangeRequest!!,
                onBackClick = {
                    showStatusHistory = false
                    selectedChangeRequest = null
                }
            )
        }
        showFormInput -> {
            EnduserForm(
                userId = userId,
                userName = userName,
                onFormSubmitted = {
                    showFormInput = false
                    selectedIndex = 1
                },
                onBackClick = {
                    showFormInput = false
                }
            )
        }
        else -> {
            Scaffold(
                modifier = Modifier.fillMaxSize(),
                topBar = {
                    TopAppBar(
                        title = {},
                        navigationIcon = {
                            Image(
                                painter = painterResource(id = R.drawable.logo_app),
                                contentDescription = "Logo",
                                modifier = Modifier
                                    .padding(start = 8.dp)
                                    .size(width = 100.dp, height = 40.dp)
                                    .clickable {
                                        selectedIndex = 0
                                        showFormInput = false
                                        showFilteredList = false
                                        filterType = null
                                    }
                            )
                        },
                        actions = {
                            // Notification Icon with Badge
                            Box(
                                modifier = Modifier.padding(end = 8.dp)
                            ) {
                                IconButton(onClick = {
                                    showNotification = true
                                }) {
                                    Icon(
                                        imageVector = Icons.Default.Notifications,
                                        contentDescription = "Notifications"
                                    )
                                }

                                // Badge
                                if (unreadCount > 0) {
                                    Box(
                                        modifier = Modifier
                                            .align(Alignment.TopEnd)
                                            .offset(x = (-4).dp, y = 4.dp)
                                            .size(20.dp)
                                            .background(Color.Red, CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = if (unreadCount > 9) "9+" else unreadCount.toString(),
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                    }
                                }
                            }

                            IconButton(onClick = {
                                showProfile = true
                            }) {
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = "Profile"
                                )
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = Color(0xFF384E66),
                            titleContentColor = Color.White,
                            navigationIconContentColor = Color.White,
                            actionIconContentColor = Color.White
                        )
                    )
                },
                bottomBar = {
                    NavigationBar(containerColor = Color(0xFF384E66)) {
                        navItemList.forEachIndexed { index, navItem ->
                            val isSelected = selectedIndex == index

                            NavigationBarItem(
                                selected = isSelected,
                                onClick = {
                                    selectedIndex = index
                                    showFormInput = false
                                    showFilteredList = false
                                    filterType = null
                                },
                                icon = {
                                    Icon(
                                        painter = painterResource(id = navItem.icon),
                                        contentDescription = navItem.label,
                                        modifier = Modifier.size(24.dp),
                                        tint = if (isSelected) Color(0xFFB0BEC5) else Color(0xFF121524)
                                    )
                                },
                                label = {
                                    Text(
                                        text = navItem.label,
                                        color = if (isSelected) Color(0xFFCFD8DC) else Color(0xFF121524)
                                    )
                                },
                                alwaysShowLabel = true,

                                colors = NavigationBarItemDefaults.colors(
                                    indicatorColor = Color.Transparent
                                )
                            )
                        }
                    }
                }
            ) { innerPadding ->
                ContentScreen(
                    modifier = Modifier.padding(innerPadding),
                    userId = userId,
                    userName = userName,
                    selectedIndex = selectedIndex,
                    onCreateFormClick = { showFormInput = true },
                    onStatusHistoryClick = { changeRequest ->
                        selectedChangeRequest = changeRequest
                        showStatusHistory = true
                    },
                    onDetailClick = { changeRequest ->
                        selectedChangeRequest = changeRequest
                        showDetailForm = true
                    },
                    onFilterClick = { type, requests ->
                        filterType = type
                        filteredRequests = requests
                        showFilteredList = true
                    }
                )
            }
        }
    }
}

@Composable
fun ContentScreen(
    modifier: Modifier = Modifier,
    userId: Int,
    userName: String,
    selectedIndex: Int,
    onCreateFormClick: () -> Unit,
    onStatusHistoryClick: (ChangeRequest) -> Unit,
    onDetailClick: (ChangeRequest) -> Unit,
    onFilterClick: (FilterType, List<ChangeRequest>) -> Unit
) {
    when (selectedIndex) {
        0 -> EnduserBerandaPage(
            userId = userId,
            userName = userName,
            onCreateFormClick = onCreateFormClick,
            onFilterClick = onFilterClick
        )
        1 -> EnduserStatusPage(
            userId = userId,
            onStatusHistoryClick = onStatusHistoryClick,
            onDetailClick = onDetailClick
        )
    }
}