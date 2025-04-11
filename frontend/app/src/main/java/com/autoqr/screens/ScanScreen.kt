// File: ScanScreenContent.kt
package com.autoqr.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.Button
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun ScanScreenContent(onQrScanned: (String) -> Unit = {}) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        Text(
            text = "SCAN QR",
            fontSize = 20.sp,
            color = MaterialTheme.colorScheme.onBackground
        )
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(240.dp),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                // Placeholder – înlocuiește cu integrarea reală a camerei și scanării QR.
                Text("Camera preview / QR code placeholder")
            }
        }
        Text(
            text = "Scanează QR Code-ul pentru a trimite mesaje.",
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onBackground
        )
        Button(onClick = { onQrScanned("autoqr:test") }) {
            Text("Simulează scanare QR")
        }
    }
}
