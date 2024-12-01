package com.example.t11leerfotogaleria

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.view.ViewGroup
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.view.LifecycleCameraController
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material3.FabPosition
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.compose.LocalLifecycleOwner
import coil3.compose.AsyncImage
import com.example.t11leerfotogaleria.ui.theme.utils.loadFromUri
import com.example.t11leerfotogaleria.ui.theme.utils.saveBitmapImage
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.concurrent.Executor


@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun CamaraX2View() {
    val permissions = rememberMultiplePermissionsState(
        permissions = mutableListOf(
            android.Manifest.permission.CAMERA
        ).apply {
            if (android.os.Build.VERSION.SDK_INT <= android.os.Build.VERSION_CODES.P) {
                add(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                add(android.Manifest.permission.READ_EXTERNAL_STORAGE)
            }
        }

    )

    //val permisoCamaraState = rememberPermissionState(permission = Manifest.permission.CAMERA)
    val context = LocalContext.current
    val camaraController = remember { LifecycleCameraController(context) }
    val lifecycle = LocalLifecycleOwner.current

    val directorio =
        Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).absoluteFile
    val imagenUri = remember { mutableStateOf<Uri?>(null) }

    // Corutina para lanzar la solicitud de permiso de la camara
    LaunchedEffect(key1 = Unit) {
        //permisoCamaraState.launchPermissionRequest()
        permissions.launchMultiplePermissionRequest()
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize(),
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    val executor = ContextCompat.getMainExecutor(context)
                    tomarFoto(context, camaraController, executor, directorio,imagenUri)
                }
            ) {
                Icon(
                    Icons.Filled.AccountCircle,
                    tint = Color.White,
                    contentDescription = ""
                )
            }
        },
        floatingActionButtonPosition = FabPosition.Center
    ) {
          Box(
              modifier = Modifier
                  .fillMaxSize() // Imagen de fondo ocupa toda la pantalla
                  .padding(it)

          ){
              if (permissions.allPermissionsGranted) {

                  CamaraComposable(
                      camaraController,
                      lifecycle,
                      modifier = Modifier.padding(it)
                  )
              }
              else {
                  Text(
                      text = "Permisos denegados",
                      modifier = Modifier
                          .padding(it)
                  )
              }
              /*Image(
                  painter = painterResource(id = R.drawable.small_image), // Reemplaza con tu imagen
                  contentDescription = "Imagen pequeña",
                  contentScale = ContentScale.Crop,

              )*/
              /*AsyncImage(
                  //model = uri,
                  model= R.drawable.android3,
                  contentDescription = null,
                  modifier = Modifier
                      .size(width = 200.dp, height = 300.dp) // Tamaño de 200x300 dp
                      .clip(RoundedCornerShape(16.dp)) // Esquinas redondeadas
                      .align(Alignment.BottomStart) // Posición abajo a la izquierda
                      .padding(16.dp) // Margen desde el borde de la pantalla
                      .border(2.dp, Color.White, RoundedCornerShape(8.dp))
              )*/
              imagenUri.value?.let { uri ->
                  AsyncImage(
                      model = uri,
                      contentDescription = null,
                      contentScale = ContentScale.Crop,
                      modifier = Modifier
                          .size(width = 200.dp, height = 150.dp) // Tamaño de 200x300 dp
                          .clip(RoundedCornerShape(16.dp)) // Esquinas redondeadas
                          .align(Alignment.BottomStart) // Posición abajo a la izquierda
                          .padding(16.dp) // Margen desde el borde de la pantalla
                          .border(4.dp, Color.White, RoundedCornerShape(8.dp))
                  )
              }
          }
    }
}

@Composable
fun CamaraComposable(
    camaraController: LifecycleCameraController,
    lifecycle: LifecycleOwner,
    modifier: Modifier = Modifier
) {
    camaraController.bindToLifecycle(lifecycle)
    AndroidView(
        modifier = modifier,
        factory = {
            val previaView = PreviewView(it).apply {
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
            }

            previaView.controller = camaraController
            previaView
        }
    )
}
private const val FILENAME_FORMAT = "yyyy-MM-dd-HH-mm-ss-SSS"
private fun tomarFoto(
    context: Context,
    camaraController: LifecycleCameraController,
    executor: Executor,
    directorio: File,
    imagenUri: MutableState<Uri?>
) {
    /*val image = File.createTempFile("img_", ".jpg", directorio)
    val outputDirectory = ImageCapture.OutputFileOptions.Builder(image).build()*/
    // Create time stamped name and MediaStore entry.
    val name = SimpleDateFormat(FILENAME_FORMAT, Locale.US)
        .format(System.currentTimeMillis())
    val contentValues = ContentValues().apply {
        put(MediaStore.MediaColumns.DISPLAY_NAME, name)
        put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
        if(Build.VERSION.SDK_INT > Build.VERSION_CODES.P) {
            put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/${context.getString(R.string.app_name)}")
        }
    }

    // Create output options object which contains file + metadata
    val outputOptions = ImageCapture.OutputFileOptions
        .Builder(context.contentResolver,
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            contentValues)
        .build()

    camaraController.takePicture(
        //outputDirectory,
        outputOptions,
        executor,
        object : ImageCapture.OnImageSavedCallback {
            override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                println(outputFileResults.savedUri)
               /* GlobalScope.launch(Dispatchers.IO){
                imagenUri.value =saveBitmapImage(context, loadFromUri(context, outputFileResults.savedUri)!!)
                //imagenUri.value = outputFileResults.savedUri
                }*/
                imagenUri.value = outputFileResults.savedUri
            }

            override fun onError(exception: ImageCaptureException) {
                println()
            }

        }
    )
}