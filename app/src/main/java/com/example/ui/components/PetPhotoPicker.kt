package com.example.ui.components

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import java.io.File
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.R
import com.example.data.local.entity.Pet
import com.example.util.ImageStorageHelper

/**
 * Resolves pet photo model for Coil: local file, drawable resource ID or default breed portrait
 */
fun resolvePetImageModel(context: android.content.Context, pet: Pet?): Any? {
    if (pet == null) return null
    val uri = pet.imageUri.trim()
    if (uri.isNotBlank()) {
        if (uri.startsWith("/") || uri.startsWith("file:") || uri.startsWith("content:")) {
            val file = File(uri)
            return if (file.exists()) file else uri
        }
        val cleanName = uri.substringAfterLast("/").substringBeforeLast(".")
        if (cleanName.contains("bary", ignoreCase = true)) {
            return R.drawable.img_bary_dog_1793267982723
        }
        if (cleanName.contains("luna", ignoreCase = true)) {
            return R.drawable.img_luna_cat_1793268004228
        }
        val resId = context.resources.getIdentifier(cleanName, "drawable", context.packageName)
        if (resId != 0) return resId
        val resIdDirect = context.resources.getIdentifier(uri, "drawable", context.packageName)
        if (resIdDirect != 0) return resIdDirect
    }
    // Default illustrative photo for Bary and Luna!
    if (pet.name.equals("Bary", ignoreCase = true)) {
        return R.drawable.img_bary_dog_1793267982723
    }
    if (pet.name.equals("Luna", ignoreCase = true)) {
        return R.drawable.img_luna_cat_1793268004228
    }
    return null
}

/**
 * Reusable Pet Avatar displaying either pet photo or initial letter on background color
 */
@Composable
fun PetAvatar(
    pet: Pet?,
    size: Dp = 48.dp,
    showCameraBadge: Boolean = false,
    onClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val imageModel = androidx.compose.runtime.remember(pet?.id, pet?.name, pet?.imageUri) {
        resolvePetImageModel(context, pet)
    }
    val petColor = if (pet != null) Color(pet.avatarColorHex) else MaterialTheme.colorScheme.primary
    val initial = pet?.name?.take(1)?.uppercase() ?: "?"

    Box(
        modifier = modifier
            .size(size)
            .then(
                if (onClick != null) {
                    Modifier
                        .clip(CircleShape)
                        .clickable(onClick = onClick)
                } else {
                    Modifier.clip(CircleShape)
                }
            ),
        contentAlignment = Alignment.Center
    ) {
        if (imageModel != null && pet != null) {
            AsyncImage(
                model = ImageRequest.Builder(context)
                    .data(imageModel)
                    .crossfade(true)
                    .build(),
                contentDescription = "Fotografia ${pet.name}",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxSize()
                    .clip(CircleShape)
                    .border(1.5.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.6f), CircleShape)
            )
        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(petColor)
                    .border(1.dp, Color.White.copy(alpha = 0.3f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = initial,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = (size.value * 0.44f).sp
                )
            }
        }

        if (showCameraBadge) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .size((size.value * 0.38f).coerceAtLeast(18f).dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary)
                    .border(1.dp, MaterialTheme.colorScheme.surface, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.CameraAlt,
                    contentDescription = "Odfotiť alebo zmeniť foto",
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size((size.value * 0.22f).coerceAtLeast(10f).dp)
                )
            }
        }
    }
}

/**
 * Dialog allowing user to take a new photo with camera or choose from gallery for a pet
 */
@Composable
fun PetPhotoPickerDialog(
    petName: String,
    currentImageUri: String = "",
    onDismiss: () -> Unit,
    onPhotoSelected: (String) -> Unit,
    onPhotoRemoved: () -> Unit = {}
) {
    val context = LocalContext.current

    var tempPhotoFile by remember { mutableStateOf<File?>(null) }

    // Full-resolution camera capture launcher using FileProvider
    val fullPictureLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success: Boolean ->
        if (success && tempPhotoFile != null && tempPhotoFile!!.exists() && tempPhotoFile!!.length() > 0) {
            val path = ImageStorageHelper.saveCapturedFileToInternalStorage(
                context,
                tempPhotoFile!!,
                prefix = "pet_${petName.lowercase()}"
            )
            if (path != null) {
                onPhotoSelected(path)
                onDismiss()
            }
        }
    }

    // Fallback thumbnail camera launcher (TakePicturePreview)
    val previewCameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap: Bitmap? ->
        if (bitmap != null) {
            val path = ImageStorageHelper.saveBitmapToInternalStorage(
                context,
                bitmap,
                prefix = "pet_${petName.lowercase()}"
            )
            if (path != null) {
                onPhotoSelected(path)
                onDismiss()
            }
        }
    }

    val launchCameraInternal: () -> Unit = {
        try {
            val tempPair = ImageStorageHelper.createTempCameraImageUri(
                context,
                prefix = "pet_${petName.lowercase()}"
            )
            if (tempPair != null) {
                tempPhotoFile = tempPair.first
                fullPictureLauncher.launch(tempPair.second)
            } else {
                previewCameraLauncher.launch(null)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            try {
                previewCameraLauncher.launch(null)
            } catch (e2: Exception) {
                e2.printStackTrace()
                Toast.makeText(
                    context,
                    "Nepodarilo sa otvoriť fotoaparát. Môžete vybrať fotku z úložiska.",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    // Runtime permission request launcher for Camera
    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            launchCameraInternal()
        } else {
            Toast.makeText(
                context,
                "Pre odfotenie zvieratka je potrebné povoliť prístup k fotoaparátu v nastaveniach.",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    // Gallery picker launcher
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            val path = ImageStorageHelper.saveUriToInternalStorage(context, uri, prefix = "pet_${petName.lowercase()}")
            if (path != null) {
                onPhotoSelected(path)
                onDismiss()
            }
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(22.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.PhotoCamera,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Fotografia zvieratka",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Zavrieť")
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "Pridajte alebo odfotografujte zvieratko ($petName). Fotka sa zobrazí v profile a hlavičke.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Camera Action Card
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .clickable {
                            val permissionCheck = ContextCompat.checkSelfPermission(
                                context,
                                Manifest.permission.CAMERA
                            )
                            if (permissionCheck == PackageManager.PERMISSION_GRANTED) {
                                launchCameraInternal()
                            } else {
                                cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                            }
                        }
                        .testTag("take_photo_camera_button")
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.PhotoCamera,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(14.dp))
                        Column {
                            Text(
                                text = "Odfotiť fotoaparátom",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Spustiť kameru a okamžite odfotiť zvieratko",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Gallery Action Card
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .clickable {
                            galleryLauncher.launch("image/*")
                        }
                        .testTag("pick_photo_gallery_button")
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.secondary),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.PhotoLibrary,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSecondary,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(14.dp))
                        Column {
                            Text(
                                text = "Vybrať z galérie",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Zvoliť uloženú fotku z telefónu",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                if (currentImageUri.isNotBlank()) {
                    Spacer(modifier = Modifier.height(10.dp))

                    // Remove Photo Button
                    OutlinedButton(
                        onClick = {
                            onPhotoRemoved()
                            onDismiss()
                        },
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.error
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("remove_pet_photo_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Odstrániť fotku a vrátiť iniciálu")
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                OutlinedButton(
                    onClick = onDismiss,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Zavrieť")
                }
            }
        }
    }
}
