package com.example.t11leerfotogaleria


import android.content.ContentValues
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp

import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.compose.LocalLifecycleOwner
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun CameraPreview(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val previewView = remember { PreviewView(context) }
    val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
   Column(modifier = modifier) {
    AndroidView(factory = { previewView },Modifier.weight(1f)) { view ->
        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()
            val preview = Preview.Builder().build().also {
                it.setSurfaceProvider(view.surfaceProvider)
            }

            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    lifecycleOwner,
                    cameraSelector,
                    preview
                )
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }, ContextCompat.getMainExecutor(context))
    }
   }
}
@Composable
fun CapturePhoto(modifier: Modifier = Modifier,onPhotoCaptured: (Uri?) -> Unit ) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
    val imageCapture = remember { ImageCapture.Builder().build() }

    val outputOptions = ImageCapture.OutputFileOptions.Builder(
        File(context.externalMediaDirs.first(), "${System.currentTimeMillis()}.jpg")
    ).build()
Column(modifier = modifier) {
    AndroidView(factory = { PreviewView(context) }, modifier = Modifier.weight(1f)) { previewView ->
        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()
            val preview = Preview.Builder().build().also {
                it.setSurfaceProvider(previewView.surfaceProvider)
            }

            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    lifecycleOwner,
                    cameraSelector,
                    preview,
                    imageCapture
                )
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }, ContextCompat.getMainExecutor(context))

    }

    Button(onClick = {
        imageCapture.takePicture(outputOptions, ContextCompat.getMainExecutor(context),
            object : ImageCapture.OnImageSavedCallback {
                override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                    onPhotoCaptured(outputFileResults.savedUri)
                }

                override fun onError(exception: ImageCaptureException) {
                    exception.printStackTrace()
                }
            })
    }) {
        Text("Tomar foto")
    }
}
}
@Composable
fun CameraXScreen() {
    var capturedPhotoUri by remember { mutableStateOf<Uri?>(null) }

    Column {
        CameraPreview()
        Spacer(modifier = Modifier.height(16.dp))
        CapturePhoto(Modifier.weight(1f)) { uri ->
            capturedPhotoUri = uri
        }

        capturedPhotoUri?.let { uri ->
            Text("Foto guardada en: $uri",)
              //  modifier = Modifier.align(Alignment.BottomCenter))
        }
        Text("Tomar foto")
    }
}
@Composable
fun CameraScreenWithCaptureButton(onPhotoCaptured: (Uri?) -> Unit) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
    val imageCapture = remember { ImageCapture.Builder().build() }
     val FILENAME_FORMAT = "yyyy-MM-dd-HH-mm-ss-SSS"

    // Crear un archivo temporal para almacenar la foto
    val name = SimpleDateFormat(FILENAME_FORMAT, Locale.US)
        .format(System.currentTimeMillis())
    // val name = "practica5_1"
    val contentValues = ContentValues().apply {
        put(MediaStore.MediaColumns.DISPLAY_NAME, name)
        put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
        //funiona en versiones superiores a Android 9
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.P) {
            put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/"+stringResource(R.string.app_name))
            //put(MediaStore.Images.Media.RELATIVE_PATH, "Practica5")
        }
    }

    // Create output options object which contains file + metadata
    val outputOptions = ImageCapture.OutputFileOptions
        .Builder(
            context.contentResolver,
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            contentValues
        )
        .build()

    /////
    var photoUri:Uri by remember { mutableStateOf(Uri.EMPTY)
        /*
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val file = File(
            context.getExternalFilesDir(Environment.DIRECTORY_PICTURES),
            "JPEG_${timeStamp}.jpg"
        )
        FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )*/
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.BottomCenter // Para posicionar el botón abajo
    ) {
        // Vista de cámara
        AndroidView(factory = { PreviewView(context) }) { previewView ->
            cameraProviderFuture.addListener({
                val cameraProvider = cameraProviderFuture.get()
                val preview = Preview.Builder().build().also {
                    it.setSurfaceProvider(previewView.surfaceProvider)
                }

                val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
                try {
                    cameraProvider.unbindAll()
                    cameraProvider.bindToLifecycle(
                        lifecycleOwner,
                        cameraSelector,
                        preview,
                        imageCapture
                    )
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }, ContextCompat.getMainExecutor(context))
        }

        // Botón flotante para tomar la foto
        FloatingActionButton(
            onClick = {
                imageCapture.takePicture(
                    /*ImageCapture
                        .OutputFileOptions
                        .Builder(
                        File(
                            photoUri.path ?: "")
                    )
                        .build()*/
                    outputOptions,
                    ContextCompat.getMainExecutor(context),
                    object : ImageCapture.OnImageSavedCallback {
                        override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                            onPhotoCaptured(photoUri) // Retorna la URI de la foto
                        }

                        override fun onError(exception: ImageCaptureException) {
                            exception.printStackTrace()
                        }
                    }
                )
            },
            modifier = Modifier.padding(bottom = 16.dp) // Separación desde el borde inferior
        ) {
            Icon(
                imageVector = Icons.Default.Star,
                contentDescription = "Tomar foto",
                tint = Color.White
            )
        }
    }
}