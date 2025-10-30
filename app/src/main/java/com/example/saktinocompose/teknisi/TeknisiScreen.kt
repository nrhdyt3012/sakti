package com.example.saktinocompose.teknisi

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
    var selectedChangeRequest by remember { mutableStateOf<ChangeRequest?>(null) }
    var selectedCategory by remember { mutableStateOf<String?>(null) }

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
        showDetailForm && selectedChangeRequest != null -> {
            DetailFormTeknisiPage(
                changeRequest = selectedChangeRequest!!,
                teknisiId = userId,
                teknisiName = userName,
                onBackClick = {
                    showDetailForm = false
                    selectedChangeRequest = null
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
                    userName = userName,
                    selectedIndex = selectedIndex,
                    onDetailClick = { request ->
                        selectedChangeRequest = request
                        showDetailForm = true
                    },
                    onCategoryClick = { category ->
                        selectedCategory = category
                        showCategoryList = true
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
    onCategoryClick: (String) -> Unit
) {
    when (selectedIndex) {
        0 -> BerandaPage(
            userName = userName,
            onDetailClick = onDetailClick
        )
        1 -> CMDBPage(
            onCategoryClick = onCategoryClick
        )
    }
}