package com.autoqr.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.font.FontWeight

@Composable
fun HomeScreen() {
    // Definim tab-urile disponibile
    val tabs = listOf("Inbox", "Scan", "Settings")
    var selectedTab by remember { mutableStateOf(0) }

    Column(modifier = Modifier.fillMaxSize()) {
        // Bara de titlu afişează numele tab-ului selectat
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.primary)
                .padding(16.dp)
        ) {
            Text(
                text = tabs[selectedTab],
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimary
            )
        }

        // TabRow pentru navigare între secțiuni
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

        // Conținutul ecranului în funcție de tab-ul selectat
        when (selectedTab) {
            0 -> InboxScreenContent()
            1 -> ScanScreenContent()
            2 -> SettingsScreenContent()
        }
    }
}
