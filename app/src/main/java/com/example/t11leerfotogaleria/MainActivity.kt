package com.example.t11leerfotogaleria

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.launch
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.example.t11leerfotogaleria.ui.theme.T11LeerFotoGaleriaTheme
import com.example.t11leerfotogaleria.ui.theme.utils.loadFromUri
import com.example.t11leerfotogaleria.ui.theme.utils.saveBitmapImage
import kotlinx.coroutines.launch
import java.io.File


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            T11LeerFotoGaleriaTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    SeleccionarImagenDesdeGaleria()
                   // CameraWithIntent()
                    //CameraXScreen()
                    //CameraWithIntent()
                    //CamaraX2View()

                }
            }
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    T11LeerFotoGaleriaTheme {
        Greeting("Android")
    }
}

@Composable
fun SeleccionarImagenDesdeGaleria() {
    var imagenUri by remember { mutableStateOf<Uri?>(null) }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val photoFile: File

    // Configura el launcher para abrir la galería
    val launcherGaleria = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri: Uri? ->
            //persistirPermisoUri(context, uri!!)
            scope.launch {
                val uriCopia = saveBitmapImage(context, loadFromUri(context, uri)!!)
                guardarUri(context, uriCopia!!)
                imagenUri = uriCopia
            }
        }
    )
    val launcherPhotoPreview = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview(),
        onResult = { bitmap ->
            if (bitmap != null) {
                scope.launch {
                    imagenUri = saveBitmapImage(context, bitmap)
                    guardarUri(context, imagenUri!!)
                    imagenUri = imagenUri
                }
            }
        }
    )
    var uriRemp:Uri?=null
    val launcherPhoto = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture(),
        onResult = { bitmap ->
            if (bitmap ) {
                scope.launch {
                   // imagenUri = saveBitmapImage(context, bitmap)
                    guardarUri(context, uriRemp!!)
                    imagenUri = uriRemp
                }
            }
        }
    )
       /* contract = ActivityResultContracts.TakePicture(),
        onResult = { success ->
            if (success) {
                Toast.makeText(context, "Foto guardada en: $photoUri", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(context, "Error al tomar la foto", Toast.LENGTH_SHORT).show()
            }
        }*/

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Button(onClick = { launcherGaleria.launch("image/*") }) {
            Text(text = "Seleccionar Imagen")
        }
        Button(onClick = {

            launcherPhotoPreview.launch()
            // photoFile = createImageFile(context)
           // launcherPhoto.launch(Uri.fromFile(photoFile))
        }) {
            Text(text = "Hacer Foto preview")
        }
        Button(onClick = {
            val directorio =
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).absoluteFile
            val resolver = context.contentResolver
            val contentValues = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, "photo_${System.currentTimeMillis()}.jpg")
                put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
                put(MediaStore.Images.ImageColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES)
            }
            resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
            uriRemp = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
            launcherPhoto.launch(uriRemp!!)
            // photoFile = createImageFile(context)
            // launcherPhoto.launch(Uri.fromFile(photoFile))
        }) {
            Text(text = "Hacer Foto")
        }
        Button(onClick = {
            val uri = obtenerUri(context)
            imagenUri = uri
        }) {
            Text(text = "recuperar Imagen")
        }
        Spacer(modifier = Modifier.height(16.dp))

        // Mostrar la imagen seleccionada
        imagenUri?.let { uri ->

            AsyncImage(
                model = uri,
                contentDescription = null,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
                    .clip(RoundedCornerShape(16.dp)) // Borde redondeado
                    .border(2.dp, Color.White, RoundedCornerShape(16.dp))
            )
        }
    }
}
fun guardarUri(context: Context, uri: Uri) {
    val sharedPreferences = context.getSharedPreferences("MisDatos", Context.MODE_PRIVATE)
    val editor = sharedPreferences.edit()
    editor.putString("uriGuardada", uri.toString())  // Guarda la URI como cadena
    editor.apply()  // Guarda los cambios de manera asíncrona

}
fun obtenerUri(context: Context): Uri? {
    val sharedPreferences = context.getSharedPreferences("MisDatos", Context.MODE_PRIVATE)
    val uriString = sharedPreferences.getString("uriGuardada", null)
    return uriString?.let { Uri.parse(it) }  // Convierte la cadena de vuelta a URI
}
fun persistirPermisoUri(context: Context, uri: Uri) {
    context.contentResolver.takePersistableUriPermission(
        uri,
        Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
    )
}
// Función auxiliar para crear un archivo de imagen
/*
fun createImageFile(context: Context): File {
    val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
    val storageDir: File? = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
    return File.createTempFile(
        "JPEG_${timeStamp}_", */
/* prefix *//*

        ".jpg", */
/* suffix *//*

        storageDir */
/* directory *//*

    )
}
*/
