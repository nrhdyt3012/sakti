// File: app/src/main/java/com/example/saktinocompose/teknisi/TeknisiScreen.kt
// ✅ UPDATED VERSION with Notification Navigation

package com.example.saktinocompose.teknisi

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.saktinocompose.R
import com.example.saktinocompose.data.model.ChangeRequest
import com.example.saktinocompose.menu.NavItem
import com.example.saktinocompose.teknisi.pages.*
import com.example.saktinocompose.utils.NetworkHelper
import com.example.saktinocompose.viewmodel.NotificationViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeknisiScreen(
    userId: String,
    userEmail: String,
    userName: String,
    userRole: String,
    userInstansi: String,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val notificationViewModel: NotificationViewModel = viewModel()
    val unreadCount by notificationViewModel.unreadCount.collectAsState()

    val navItemList = listOf(
        NavItem("Beranda", R.drawable.home),
        NavItem("Emergency", R.drawable.add_emergency),
        NavItem("Category", R.drawable.database)
    )

    var selectedIndex by remember { mutableIntStateOf(0) }
    var showProfile by remember { mutableStateOf(false) }
    var showNotification by remember { mutableStateOf(false) } // ✅ NEW
    var showDetailForm by remember { mutableStateOf(false) }
    var showCategoryList by remember { mutableStateOf(false) }
    var showFilteredList by remember { mutableStateOf(false) }
    var selectedChangeRequest by remember { mutableStateOf<ChangeRequest?>(null) }
    var selectedCategory by remember { mutableStateOf<String?>(null) }
    var filteredRequests by remember { mutableStateOf<List<ChangeRequest>>(emptyList()) }
    var filterType by remember { mutableStateOf<TeknisiFilterType?>(null) }
    var showExitDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        if(!NetworkHelper.isInternetAvailable(context)) {
            Toast.makeText(
                context,
                "No Internet Connection.",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    // ✅ UPDATED: Back Handler with Notification
    BackHandler {
        when {
            showProfile -> showProfile = false
            showNotification -> showNotification = false // ✅ NEW
            showFilteredList -> {
                showFilteredList = false
                filterType = null
            }
            showDetailForm -> {
                showDetailForm = false
                if (selectedCategory != null) {
                    showCategoryList = true
                } else if (filterType != null) {
                    showFilteredList = true
                } else {
                    selectedChangeRequest = null
                }
            }
            showCategoryList -> {
                showCategoryList = false
                selectedCategory = null
            }
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

    // ✅ ROUTING LOGIC
    when {
        showProfile -> {
            ProfilePage(
                userId = userId,
                userEmail = userEmail,
                userName = userName,
                userRole = userRole,
                userInstansi = userInstansi,
                onBackClick = { showProfile = false }
            )
        }
        // ✅ NEW: Notification Page
        showNotification -> {
            NotificationPage(
                onBackClick = { showNotification = false },
                onNotificationClick = { crId ->
                    // Navigate to detail form when notification clicked
                    // Find the CR by ID and show detail
                    showNotification = false
                    // TODO: Fetch CR by crId and show detail
                }
            )
        }
        showFilteredList && filterType != null -> {
            TeknisiFilteredListPage(
                filterType = filterType!!,
                changeRequests = filteredRequests,
                onBackClick = {
                    showFilteredList = false
                    filterType = null
                },
                onDetailClick = { request ->
                    selectedChangeRequest = request
                    showDetailForm = true
                    showFilteredList = false
                }
            )
        }
        showDetailForm && selectedChangeRequest != null -> {
            DetailFormTeknisiPage(
                changeRequest = selectedChangeRequest!!,
                teknisiId = userId,
                teknisiName = userName,
                onBackClick = {
                    showDetailForm = false
                    if (selectedCategory != null) {
                        showCategoryList = true
                    } else if (filterType != null) {
                        showFilteredList = true
                    } else {
                        selectedChangeRequest = null
                    }
                }
            )
        }
        showCategoryList && selectedCategory != null -> {
            CMDBCategoryListPage(
                categoryName = selectedCategory!!,
                teknisiId = userId,
                teknisiName = userName,
                onBackClick = {
                    showCategoryList = false
                    selectedCategory = null
                },
                onDetailClick = { request ->
                    selectedChangeRequest = request
                    showDetailForm = true
                    showCategoryList = false
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
                                    }
                            )
                        },
                        actions = {
                            // ✅ UPDATED: Notification Button with Badge
                            BadgedBox(
                                badge = {
                                    if (unreadCount > 0) {
                                        Badge(
                                            containerColor = Color(0xFFD32F2F)
                                        ) {
                                            Text(
                                                text = if (unreadCount > 99) "99+" else unreadCount.toString(),
                                                color = Color.White
                                            )
                                        }
                                    }
                                }
                            ) {
                                IconButton(onClick = {
                                    showNotification = true
                                }) {
                                    Icon(
                                        imageVector = Icons.Default.Notifications,
                                        contentDescription = "Notifications"
                                    )
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
                    userName = userName,
                    userId = userId,
                    selectedIndex = selectedIndex,
                    onDetailClick = { request ->
                        selectedChangeRequest = request
                        showDetailForm = true
                    },
                    onCategoryClick = { category ->
                        selectedCategory = category
                        showCategoryList = true
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
    userName: String,
    userId: String,
    selectedIndex: Int,
    onDetailClick: (ChangeRequest) -> Unit,
    onCategoryClick: (String) -> Unit,
    onFilterClick: (TeknisiFilterType, List<ChangeRequest>) -> Unit
) {
    when (selectedIndex) {
        0 -> BerandaPage(
            userName = userName,
            onDetailClick = onDetailClick,
            onFilterClick = onFilterClick
        )
        1 -> EmergencyFormPage(
            userId = userId,
            userName = userName
        )
        2 -> CMDBPage(
            onCategoryClick = onCategoryClick
        )
    }
}