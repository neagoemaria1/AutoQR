package com.autoqr.screens

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.os.Environment
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
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
import com.autoqr.utils.generateQrCode
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreenContent(onLogout: () -> Unit) {
    val context = LocalContext.current
    val dataStore = remember { DataStoreManager(context) }
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    var userData by remember { mutableStateOf<UserProfileResponse?>(null) }
    var qrBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        selectedImageUri = uri
        // TODO: Save locally or upload to backend
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

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "My Profile",
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    )
                },
                modifier = Modifier.padding(top = 8.dp)
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            userData?.let { user ->
                val profilePainter: AsyncImagePainter = rememberAsyncImagePainter(
                    model = selectedImageUri ?: user.profileImageUrl,
                    error = painterResource(R.drawable.default_avatar)
                )

                Image(
                    painter = profilePainter,
                    contentDescription = "Profile Picture",
                    modifier = Modifier
                        .size(180.dp)
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop
                )

                TextButton(
                    onClick = { imagePickerLauncher.launch("image/*") },
                    modifier = Modifier.padding(top = 4.dp)
                ) {
                    Text(
                        text = "Change Photo",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Medium
                    )
                }

                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Username",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                    OutlinedTextField(
                        value = user.username,
                        onValueChange = {},
                        enabled = false,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(10.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            disabledTextColor = MaterialTheme.colorScheme.onSurface,
                            disabledBorderColor = MaterialTheme.colorScheme.outline,
                            disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Email",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                    OutlinedTextField(
                        value = user.email,
                        onValueChange = {},
                        enabled = false,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(10.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            disabledTextColor = MaterialTheme.colorScheme.onSurface,
                            disabledBorderColor = MaterialTheme.colorScheme.outline,
                            disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                }

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    qrBitmap?.let {
                        Image(
                            bitmap = it.asImageBitmap(),
                            contentDescription = "QR Code",
                            modifier = Modifier
                                .padding(top = 10.dp)
                                .size(200.dp)
                        )
                    } ?: Text("QR Code Placeholder")
                }

                Button(
                    onClick = {
                        qrBitmap?.let { bitmap ->
                            val file = saveQrToPdf(context, bitmap, user.username)
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
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text("EXPORT QR", color = MaterialTheme.colorScheme.onPrimary)
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
                    Text("LOGOUT", color = MaterialTheme.colorScheme.onError)
                }
            } ?: CircularProgressIndicator()
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
        canvas.drawColor(Color.WHITE)
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
 