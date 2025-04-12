package com.autoqr.screens

import android.util.Log
import androidx.annotation.OptIn
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import com.autoqr.services.PredefinedMessagesRepository
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage

@Composable
fun ScanScreenContent(navController: NavController) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val previewView = remember { PreviewView(context) }

    var scannedQr by remember { mutableStateOf<String?>(null) }
    var showMessageDialog by remember { mutableStateOf(false) }
    var cameraStarted by remember { mutableStateOf(false) }
    var alerts by remember { mutableStateOf<List<String>>(emptyList()) }


    LaunchedEffect(Unit) {
        val repo = PredefinedMessagesRepository(context)
        val response = repo.loadPredefinedMessages()
        alerts = response?.alerts ?: emptyList()
    }

    Column(modifier = Modifier.fillMaxSize()) {

        AndroidView(
            factory = { previewView },
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(16.dp)
                .clickable {
                    if (!cameraStarted) {
                        cameraStarted = true
                        startCamera(
                            context = context,
                            previewView = previewView,
                            lifecycleOwner = lifecycleOwner
                        ) { qrCode ->
                            scannedQr = qrCode
                            showMessageDialog = true
                        }
                    }
                }
        )


        Button(
            onClick = {
                scannedQr = "autoqr:test"
                showMessageDialog = true
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text("Simulează scanare QR către test")
        }
    }


    if (showMessageDialog && scannedQr != null) {
        AlertDialog(
            onDismissRequest = { showMessageDialog = false },
            title = { Text("Choose a message to send") },
            text = {
                Column {
                    alerts.forEach { msg ->
                        Text(
                            text = msg,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp)
                                .clickable {
                                    val encodedMessage = java.net.URLEncoder.encode(msg, "UTF-8")
                                    navController.navigate("sendMessage/${scannedQr}/${encodedMessage}")
                                    showMessageDialog = false
                                }
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showMessageDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@OptIn(ExperimentalGetImage::class)
private fun startCamera(
    context: android.content.Context,
    previewView: PreviewView,
    lifecycleOwner: androidx.lifecycle.LifecycleOwner,
    onQrScanned: (String) -> Unit
) {
    val cameraProviderFuture = ProcessCameraProvider.getInstance(context)

    cameraProviderFuture.addListener({
        val cameraProvider = cameraProviderFuture.get()

        val preview = Preview.Builder().build().also {
            it.setSurfaceProvider(previewView.surfaceProvider)
        }

        val scanner = BarcodeScanning.getClient()

        val analysisUseCase = ImageAnalysis.Builder()
            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
            .build()
            .also { analysis ->
                analysis.setAnalyzer(ContextCompat.getMainExecutor(context)) { imageProxy ->
                    val mediaImage = imageProxy.image
                    if (mediaImage != null) {
                        val inputImage = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
                        scanner.process(inputImage)
                            .addOnSuccessListener { barcodes ->
                                barcodes.firstOrNull()?.rawValue?.let { qr ->
                                    Log.d("ScanScreen", "QR scanned: $qr")
                                    onQrScanned(qr)
                                }
                            }
                            .addOnFailureListener {
                                Log.e("ScanScreen", "Scan error", it)
                            }
                            .addOnCompleteListener {
                                imageProxy.close()
                            }
                    } else {
                        imageProxy.close()
                    }
                }
            }

        val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

        try {
            cameraProvider.unbindAll()
            cameraProvider.bindToLifecycle(lifecycleOwner, cameraSelector, preview, analysisUseCase)
        } catch (e: Exception) {
            Log.e("ScanScreen", "Camera binding failed", e)
        }
    }, ContextCompat.getMainExecutor(context))
}
