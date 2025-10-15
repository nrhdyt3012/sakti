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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.saktinocompose.menu.NavItem
import com.example.saktinocompose.R
import com.example.saktinocompose.teknisi.pages.ActionPage
import com.example.saktinocompose.teknisi.pages.BerandaPage
import com.example.saktinocompose.teknisi.pages.DatabasePage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeknisiScreen(userEmail: String, userRole: String,modifier: Modifier = Modifier) {
    val navItemList = listOf(
        NavItem("Beranda", R.drawable.home),
        NavItem("Action", R.drawable.build),
        NavItem("Database", R.drawable.database),
//        NavItem("Setting", R.drawable.setting),
    )

    var selectedIndex by remember { mutableStateOf(0) }
    var selectedStatus by remember { mutableStateOf<String?>(null) }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {},
                navigationIcon = {
                    Image(
                        painter = painterResource(id = R.drawable.logo_app),
                        contentDescription = "Back to Beranda",
                        modifier = Modifier
                            .padding(start = 8.dp)
                            .size(width = 100.dp, height = 40.dp)
                            .clickable {
                                selectedIndex = 0
                                selectedStatus = null
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
                        // Navigate ke ProfilPage
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
                            selectedStatus = null // Reset status ketika pindah tab
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
            selectedIndex = selectedIndex,
            selectedStatus = selectedStatus,
            onStatusClick = { status ->
                selectedStatus = status
                selectedIndex = 1 // Pindah ke ActionPage
            }
        )
    }
}

@Composable
fun ContentScreen(
    modifier: Modifier = Modifier,
    selectedIndex: Int,
    selectedStatus: String?,
    onStatusClick: (String) -> Unit
) {
    when (selectedIndex) {
        0 -> BerandaPage()
        1 -> ActionPage(filterStatus = selectedStatus)
        2 -> DatabasePage(onStatusClick = onStatusClick)
//        3 -> SettingPage()
    }
}