package com.example.saktinocompose.teknisi

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.saktinocompose.R
import com.example.saktinocompose.data.entity.ChangeRequest
import com.example.saktinocompose.menu.NavItem
import com.example.saktinocompose.teknisi.pages.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeknisiScreen(
    userId: Int,
    userEmail: String,
    userName: String,
    userRole: String,
    modifier: Modifier = Modifier
) {
    val navItemList = listOf(
        NavItem("Beranda", R.drawable.home),
        NavItem("CMDB", R.drawable.database)
    )

    var selectedIndex by remember { mutableIntStateOf(0) }
    var showProfile by remember { mutableStateOf(false) }
    var showDetailForm by remember { mutableStateOf(false) }
    var showCategoryList by remember { mutableStateOf(false) }
    var showFilteredList by remember { mutableStateOf(false) }
    var selectedChangeRequest by remember { mutableStateOf<ChangeRequest?>(null) }
    var selectedCategory by remember { mutableStateOf<String?>(null) }
    var filteredRequests by remember { mutableStateOf<List<ChangeRequest>>(emptyList()) }
    var filterType by remember { mutableStateOf<TeknisiFilterType?>(null) }
    var showExitDialog by remember { mutableStateOf(false) }

    // Back Handler untuk menangani tombol back
    BackHandler {
        when {
            showProfile -> showProfile = false
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
            else -> showExitDialog = true // Tampilkan dialog konfirmasi keluar
        }
    }

    // Dialog konfirmasi keluar aplikasi
    if (showExitDialog) {
        AlertDialog(
            onDismissRequest = { showExitDialog = false },
            title = { Text("Keluar Aplikasi") },
            text = { Text("Apakah Anda yakin ingin keluar dari aplikasi?") },
            confirmButton = {
                Button(
                    onClick = {
                        // Keluar dari aplikasi
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
            ProfilePage(
                userId = userId,
                userEmail = userEmail,
                userName = userName,
                userRole = userRole,
                onBackClick = { showProfile = false }
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
                            IconButton(onClick = {
                                // Navigate ke NotificationPage
                            }) {
                                Icon(
                                    imageVector = Icons.Default.Notifications,
                                    contentDescription = "Notifications"
                                )
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

                                // 🔥 inilah yang hilangin bubble
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
        1 -> CMDBPage(
            onCategoryClick = onCategoryClick
        )
    }
}