package com.autoqr.screens

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.os.Environment
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImagePainter
import coil.compose.rememberAsyncImagePainter
import com.autoqr.R
import com.autoqr.model.UserProfileResponse
import com.autoqr.network.ApiClient
import com.autoqr.storage.DataStoreManager
import com.autoqr.ui.Color.*
import com.autoqr.utils.generateQrCode
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import androidx.compose.foundation.Canvas
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreenContent(onLogout: () -> Unit) {
    val context = LocalContext.current
    val dataStore = remember { DataStoreManager(context) }
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    var userData by remember { mutableStateOf<UserProfileResponse?>(null) }
    var qrBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var localImagePath by remember { mutableStateOf<String?>(null) }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            val savedPath = storeImageLocally(context, it)
            if (savedPath != null) {
                localImagePath = savedPath

                userData?.let { user ->
                    scope.launch {
                        dataStore.saveProfileImagePathForUser(user.username, savedPath)
                    }
                }
            }
        }
    }

    LaunchedEffect(Unit) {
        scope.launch {
            dataStore.token.collect { token ->
                if (token != null) {
                    val api = ApiClient.create(context)
                    val response = api.getCurrentUser("Bearer $token")
                    if (response.isSuccessful) {
                        userData = response.body()
                        qrBitmap = generateQrCode(userData?.qrCode ?: "")
                    }
                }
            }
        }
    }

    LaunchedEffect(userData) {
        userData?.let { user ->
            scope.launch {
                dataStore.getProfileImagePathForUser(user.username).collect { savedPath ->
                    if (!savedPath.isNullOrBlank()) {
                        localImagePath = savedPath
                    }
                }
            }
        }
    }

    Scaffold(
        topBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(ElectricBlue, AquaBlue)
                        )
                    )
                    .padding(vertical = 16.dp)
            ) {
                Text(
                    text = "My Profile",
                    modifier = Modifier.align(Alignment.Center),
                    fontWeight = FontWeight.Bold,
                    fontSize = 24.sp,
                    color = White
                )
            }
        },
        containerColor = Color(0xFF0D0D0D),
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Box(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            // Fondul de bază cu gradient
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(Brush.verticalGradient(listOf(Color(0xFF0D0D0D), Color(0xFF1A1A1A))))
            )
            // Canvas-ul de fundal (identic cu cel din LoginScreen)
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
                        colors = listOf(
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
                        colors = listOf(
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

            // Conținutul principal al profilului
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (userData == null) {
                    CircularProgressIndicator(color = ElectricBlue)
                } else {
                    val profilePainter: AsyncImagePainter = rememberAsyncImagePainter(
                        model = localImagePath ?: userData!!.profileImageUrl,
                        error = painterResource(R.drawable.default_avatar)
                    )

                    Card(
                        modifier = Modifier.size(180.dp),
                        shape = CircleShape,
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF292929)),
                        elevation = CardDefaults.cardElevation(8.dp)
                    ) {
                        Image(
                            painter = profilePainter,
                            contentDescription = "Profile Picture",
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(CircleShape),
                            contentScale = ContentScale.Crop
                        )
                    }

                    TextButton(onClick = { imagePickerLauncher.launch("image/*") }) {
                        Text(
                            text = "Change Photo",
                            fontSize = 14.sp,
                            color = ElectricBlue,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF292929)),
                        elevation = CardDefaults.cardElevation(8.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                text = "Username",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = White
                            )
                            OutlinedTextField(
                                value = userData!!.username,
                                onValueChange = {},
                                enabled = false,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(56.dp),
                                shape = RoundedCornerShape(10.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    disabledTextColor = White,
                                    disabledBorderColor = MediumGray,
                                    disabledLabelColor = LightGray
                                )
                            )

                            Text(
                                text = "Email",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = White
                            )
                            OutlinedTextField(
                                value = userData!!.email,
                                onValueChange = {},
                                enabled = false,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(56.dp),
                                shape = RoundedCornerShape(10.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    disabledTextColor = White,
                                    disabledBorderColor = MediumGray,
                                    disabledLabelColor = LightGray
                                )
                            )
                        }
                    }

                    Card(
                        modifier = Modifier.size(220.dp),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF292929)),
                        elevation = CardDefaults.cardElevation(8.dp)
                    ) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            qrBitmap?.let {
                                Image(
                                    bitmap = it.asImageBitmap(),
                                    contentDescription = "QR Code",
                                    modifier = Modifier.size(200.dp)
                                )
                            } ?: Text("QR Code Placeholder", color = White)
                        }
                    }

                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Button(
                            onClick = {
                                qrBitmap?.let { bitmap ->
                                    val file = saveQrToPdf(context, bitmap, userData!!.username)
                                    scope.launch {
                                        snackbarHostState.showSnackbar(
                                            if (file != null)
                                                "QR code exported to Downloads/AutoQR"
                                            else
                                                "Export failed."
                                        )
                                    }
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = ElectricBlue)
                        ) {
                            Text("EXPORT QR", color = White)
                        }

                        Button(
                            onClick = {
                                scope.launch {
                                    dataStore.clearToken()
                                    onLogout()
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                        ) {
                            Text("LOGOUT", color = White)
                        }
                    }
                }
            }
        }
    }
}

fun saveQrToPdf(context: Context, bitmap: Bitmap, filename: String): File? {
    return try {
        val downloads = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        val autoQRFolder = File(downloads, "AutoQR")
        if (!autoQRFolder.exists()) autoQRFolder.mkdirs()
        val file = File(autoQRFolder, "$filename.pdf")
        val document = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(bitmap.width, bitmap.height, 1).create()
        val page = document.startPage(pageInfo)
        val canvas: Canvas = page.canvas
        canvas.drawColor(0xFFFFFF)
        canvas.drawBitmap(bitmap, 0f, 0f, null)
        document.finishPage(page)
        FileOutputStream(file).use { document.writeTo(it) }
        document.close()
        file
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

fun storeImageLocally(context: Context, sourceUri: Uri): String? {
    return try {
        val inputStream = context.contentResolver.openInputStream(sourceUri) ?: return null
        val file = File(context.filesDir, "profile_picture_${System.currentTimeMillis()}.jpg")
        file.outputStream().use { output ->
            inputStream.copyTo(output)
        }
        inputStream.close()
        file.absolutePath
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}
