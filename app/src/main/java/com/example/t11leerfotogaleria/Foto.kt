package com.example.t11leerfotogaleria

import android.net.Uri
import android.os.Environment
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.core.net.toUri
import coil3.Image
import coil3.compose.AsyncImage
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun CameraWithIntent() {
    val context = LocalContext.current
    val directorio=Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).absoluteFile
    val photoFile = remember {
        /* val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
         val storageDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
         File(storageDir, "JPEG_${timeStamp}.jpg").apply {
             photoUri = FileProvider.getUriForFile(
                 context,
                 "${context.packageName}.fileprovider",
                 this
             )}*/
        //File(context.externalMediaDirs.first(), "${System.currentTimeMillis()}.jpg")
        FileProvider.getUriForFile(
            context,
            "com.example.t11leerfotogaleria.fileprovider", // Replace with your authority

            File(
                context.getExternalFilesDir(Environment.DIRECTORY_PICTURES),
                "${System.currentTimeMillis()}.jpg"
            )
        )

    }
    var photoUri = remember { photoFile }
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture(),
        onResult = { success ->
            if (success) {
                Toast.makeText(context, "Foto guardada en: $photoUri", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(context, "Error al tomar la foto", Toast.LENGTH_SHORT).show()
            }
        }
    )

    // Crear el archivo donde se guardará la foto



    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Button(onClick = {
            launcher.launch(photoUri) // Lanzar la cámara
        }) {
            Text(text = "Abrir cámara")
        }

        Spacer(modifier = Modifier.height(16.dp))

        photoUri?.let{ uri ->

            AsyncImage(
                model = uri,
                contentDescription = null,
                modifier = Modifier.size(200.dp)
            )
        }
    }
}
