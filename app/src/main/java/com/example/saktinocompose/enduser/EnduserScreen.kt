package com.example.saktinocompose.enduser

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
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
import com.example.saktinocompose.menu.NavItem
import com.example.saktinocompose.R
import com.example.saktinocompose.data.entity.ChangeRequest
import com.example.saktinocompose.enduser.pages.*

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

    var selectedIndex by remember { mutableIntStateOf(0) }
    var showProfile by remember { mutableStateOf(false) }
    var showFormInput by remember { mutableStateOf(false) }
    var showDetailForm by remember { mutableStateOf(false) }
    var showStatusHistory by remember { mutableStateOf(false) }
    var selectedChangeRequest by remember { mutableStateOf<ChangeRequest?>(null) }

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
        showDetailForm && selectedChangeRequest != null -> {
            FormDetailPage(
                changeRequest = selectedChangeRequest!!,
                onBackClick = {
                    showDetailForm = false
                    selectedChangeRequest = null
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
                            containerColor = Color(0xFF37474F),
                            titleContentColor = Color.White,
                            navigationIconContentColor = Color.White,
                            actionIconContentColor = Color.White
                        )
                    )
                },
                bottomBar = {
                    NavigationBar {
                        navItemList.forEachIndexed { index, navItem ->
                            NavigationBarItem(
                                selected = selectedIndex == index,
                                onClick = {
                                    selectedIndex = index
                                    showFormInput = false
                                },
                                icon = {
                                    Icon(
                                        painter = painterResource(id = navItem.icon),
                                        contentDescription = "Icon",
                                        modifier = Modifier.size(24.dp)
                                    )
                                },
                                label = {
                                    Text(text = navItem.label)
                                }
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
    onDetailClick: (ChangeRequest) -> Unit
) {
    when (selectedIndex) {
        0 -> EnduserBerandaPage(
            userId = userId,
            userName = userName,
            onCreateFormClick = onCreateFormClick
        )
        1 -> EnduserStatusPage(
            userId = userId,
            onStatusHistoryClick = onStatusHistoryClick,
            onDetailClick = onDetailClick
        )
    }
}