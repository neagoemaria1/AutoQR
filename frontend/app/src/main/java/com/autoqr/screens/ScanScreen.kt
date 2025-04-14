package com.autoqr.screens

import android.util.Log
import androidx.annotation.OptIn
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import com.autoqr.services.PredefinedMessagesRepository
import com.autoqr.ui.Color.AquaBlue
import com.autoqr.ui.Color.ElectricBlue
import com.autoqr.ui.Color.White
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage
import kotlinx.coroutines.launch
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.material.icons.filled.Warning
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.net.URLEncoder

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun ScanScreenContent(navController: NavController) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val previewView = remember { PreviewView(context) }
    var scannedQr by remember { mutableStateOf<String?>(null) }
    var showMessageDialog by remember { mutableStateOf(false) }
    var cameraStarted by remember { mutableStateOf(false) }
    var alerts by remember { mutableStateOf<List<String>>(emptyList()) }
    var cameraProvider by remember { mutableStateOf<ProcessCameraProvider?>(null) }
    LaunchedEffect(Unit) {
        val repo = PredefinedMessagesRepository(context)
        alerts = repo.loadPredefinedMessages()?.alerts ?: emptyList()
    }
    Scaffold(
        topBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            listOf(ElectricBlue, AquaBlue)
                        )
                    )
                    .padding(vertical = 16.dp)
            ) {
                Text(
                    text = "Scan QR",
                    modifier = Modifier.align(Alignment.Center),
                    fontWeight = FontWeight.Bold,
                    fontSize = 24.sp,
                    color = White
                )
            }
        },
        containerColor = Color(0xFF0D0D0D)
    ) { padding ->
        Box(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(
                        Brush.verticalGradient(
                            listOf(Color(0xFF0D0D0D), Color(0xFF1A1A1A))
                        )
                    )
            )
            Canvas(modifier = Modifier.matchParentSize()) {
                val w = size.width
                val h = size.height
                val topDiag = Path().apply {
                    moveTo(0f, 0f)
                    lineTo(w, 0f)
                    lineTo(w, h * 0.25f)
                    lineTo(0f, h * 0.35f)
                    close()
                }
                drawPath(
                    path = topDiag,
                    brush = Brush.linearGradient(
                        listOf(
                            Color(0xFF444444).copy(alpha = 0.25f),
                            Color(0xFF303030).copy(alpha = 0.1f)
                        ),
                        start = Offset.Zero,
                        end = Offset(w, 0f)
                    )
                )
                val bottomDiag = Path().apply {
                    moveTo(0f, h)
                    lineTo(w, h)
                    lineTo(w, h * 0.70f)
                    lineTo(0f, h * 0.60f)
                    close()
                }
                drawPath(
                    path = bottomDiag,
                    brush = Brush.linearGradient(
                        listOf(
                            Color(0xFF217373).copy(alpha = 0.2f),
                            Color(0xFF14FFEC).copy(alpha = 0.05f)
                        ),
                        start = Offset(0f, h),
                        end = Offset(w, h * 0.7f)
                    )
                )
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(Color(0xFF14FFEC).copy(alpha = 0.15f), Color.Transparent),
                        center = Offset(w * 0.8f, h * 0.25f)
                    ),
                    radius = size.minDimension * 0.4f
                )
            }
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp)
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(top = 16.dp)
                        .clip(RoundedCornerShape(24.dp))
                        .alpha(0.93f)
                        .background(Color(0xFF303030)),
                    contentAlignment = Alignment.Center
                ) {
                    if (cameraStarted) {
                        AndroidView(
                            factory = { previewView },
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(RoundedCornerShape(24.dp))
                        )
                        Text(
                            text = "Scanning...",
                            color = Color.White,
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .padding(bottom = 12.dp)
                        )
                    } else {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.QrCode,
                                contentDescription = null,
                                tint = AquaBlue,
                                modifier = Modifier.size(50.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Camera not started", color = Color.White)
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(16.dp))
                            .background(ElectricBlue)
                            .clickable {
                                if (!cameraStarted) {
                                    cameraStarted = true
                                    startCamera(
                                        context,
                                        previewView,
                                        lifecycleOwner,
                                        onQrScanned = {
                                            scannedQr = it
                                            showMessageDialog = true
                                        },
                                        onCameraProviderReady = {
                                            cameraProvider = it
                                        }
                                    )
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            modifier = Modifier.padding(vertical = 14.dp, horizontal = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = if (cameraStarted) Icons.Default.Check else Icons.Default.CameraAlt,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (cameraStarted) "Active" else "Start",
                                fontSize = 14.sp,
                                color = Color.White
                            )
                        }
                    }
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color.Gray)
                            .clickable {
                                scannedQr = "autoqr:test"
                                showMessageDialog = true
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            modifier = Modifier.padding(vertical = 14.dp, horizontal = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.QrCode,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Simulate QR",
                                fontSize = 15.sp,
                                color = Color.White
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                if (cameraStarted) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(16.dp))
                                .background(Color(0xFF505050))
                                .clickable {
                                    cameraProvider?.unbindAll()
                                    cameraStarted = false
                                }
                                .padding(horizontal = 24.dp, vertical = 14.dp)
                        ) {
                            Text(
                                text = "Stop Camera",
                                fontSize = 15.sp,
                                color = Color.White
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }

            }
        }
    }
    if (showMessageDialog && scannedQr != null) {
        ChooseMessageDialog(
            alertMessages = alerts,
            scannedQr = scannedQr!!,
            onDismiss = { showMessageDialog = false },
            onMessageSelected = { encodedMsg ->
                navController.navigate("sendMessage/$scannedQr/$encodedMsg")
                showMessageDialog = false
            }
        )
    }
}


@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun ChooseMessageDialog(
    alertMessages: List<String>,
    scannedQr: String,
    onDismiss: () -> Unit,
    onMessageSelected: (String) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color(0xFF050505), Color(0xFF151515))
                )
            )
    ) {
        Canvas(modifier = Modifier.matchParentSize()) {
            val w = size.width
            val h = size.height

            val topDiag = Path().apply {
                moveTo(0f, 0f)
                lineTo(w, 0f)
                lineTo(w, h * 0.25f)
                lineTo(0f, h * 0.35f)
                close()
            }
            drawPath(
                path = topDiag,
                brush = Brush.linearGradient(
                    listOf(
                        Color(0xFF444444).copy(alpha = 0.25f),
                        Color(0xFF303030).copy(alpha = 0.1f)
                    ),
                    start = Offset.Zero,
                    end = Offset(w, 0f)
                )
            )

            val bottomDiag = Path().apply {
                moveTo(0f, h)
                lineTo(w, h)
                lineTo(w, h * 0.70f)
                lineTo(0f, h * 0.60f)
                close()
            }
            drawPath(
                path = bottomDiag,
                brush = Brush.linearGradient(
                    listOf(
                        Color(0xFF217373).copy(alpha = 0.2f),
                        Color(0xFF14FFEC).copy(alpha = 0.05f)
                    ),
                    start = Offset(0f, h),
                    end = Offset(w, h * 0.7f)
                )
            )
        }

        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(Color(0xFF1B1B1B))
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Warning, contentDescription = null, tint = Color(0xFF00C6FF), modifier = Modifier.size(22.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Choose a message", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color(0xFF00C6FF))
            }

            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                alertMessages.forEach { message ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFF2A2A2A))
                            .clickable {
                                val encoded = URLEncoder.encode(message, "UTF-8")
                                onMessageSelected(encoded)
                            }
                            .padding(vertical = 12.dp, horizontal = 16.dp)
                    ) {
                        Text(
                            text = message,
                            fontSize = 15.sp,
                            color = Color.White
                        )
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(onClick = onDismiss) {
                    Text("Cancel", fontSize = 16.sp, color = Color.LightGray)
                }
            }
        }
    }
}



@OptIn(ExperimentalGetImage::class)
private fun startCamera(
    context: android.content.Context,
    previewView: PreviewView,
    lifecycleOwner: androidx.lifecycle.LifecycleOwner,
    onQrScanned: (String) -> Unit,
    onCameraProviderReady: (ProcessCameraProvider) -> Unit
) {
    val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
    cameraProviderFuture.addListener({
        val provider = cameraProviderFuture.get()
        onCameraProviderReady(provider)
        val preview = Preview.Builder().build().also {
            it.setSurfaceProvider(previewView.surfaceProvider)
        }
        val scanner = BarcodeScanning.getClient()
        val analysisUseCase = ImageAnalysis.Builder()
            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
            .build().also { analysis ->
                analysis.setAnalyzer(ContextCompat.getMainExecutor(context)) { imageProxy ->
                    val mediaImage = imageProxy.image
                    if (mediaImage != null) {
                        val inputImage = InputImage.fromMediaImage(
                            mediaImage,
                            imageProxy.imageInfo.rotationDegrees
                        )
                        scanner.process(inputImage)
                            .addOnSuccessListener { barcodes ->
                                barcodes.firstOrNull()?.rawValue?.let(onQrScanned)
                            }
                            .addOnFailureListener { Log.e("ScanScreen", "Scan error", it) }
                            .addOnCompleteListener { imageProxy.close() }
                    } else {
                        imageProxy.close()
                    }
                }
            }
        val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
        try {
            provider.unbindAll()
            provider.bindToLifecycle(lifecycleOwner, cameraSelector, preview, analysisUseCase)
        } catch (e: Exception) {
            Log.e("ScanScreen", "Camera binding failed", e)
        }
    }, ContextCompat.getMainExecutor(context))
}
