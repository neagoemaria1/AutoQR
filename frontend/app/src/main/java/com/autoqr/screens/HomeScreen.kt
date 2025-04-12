package com.autoqr.screens

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.statusBars
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.font.FontWeight
import androidx.navigation.NavController

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun HomeScreen(
    navController: NavController
) {
    val tabs = listOf("Inbox", "Scan", "Profile")
    var selectedTab by remember { mutableStateOf(0) }

    val topPadding: Dp = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()

    Column(modifier = Modifier.fillMaxSize()) {

        // Top Title Bar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.primary)
                .padding(top = topPadding + 12.dp, bottom = 16.dp, start = 16.dp, end = 16.dp)
        ) {
            Text(
                text = tabs[selectedTab],
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimary
            )
        }

        // Tabs Row
        TabRow(selectedTabIndex = selectedTab) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = { Text(title) },
                    modifier = Modifier.padding(8.dp)
                )
            }
        }

        // Ecrane pe tab
        when (selectedTab) {
            0 -> InboxScreenContent(navController = navController)
            1 -> ScanScreenContent(navController = navController)
            2 -> ProfileScreenContent(
                onLogout = {
                    navController.navigate("login") {
                        popUpTo("home") { inclusive = true }
                    }
                }
            )
        }
    }
}
