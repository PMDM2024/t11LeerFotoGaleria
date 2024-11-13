package com.example.t11leerfotogaleria

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.example.t11leerfotogaleria.ui.theme.T11LeerFotoGaleriaTheme


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            T11LeerFotoGaleriaTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    SeleccionarImagenDesdeGaleria()
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

    // Configura el launcher para abrir la galería
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri: Uri? ->
            //persistirPermisoUri(context, uri!!)
            guardarUri(context, uri!!)
            imagenUri = uri
        }
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Button(onClick = { launcher.launch("image/*") }) {
            Text(text = "Seleccionar Imagen")
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
                modifier = Modifier.size(200.dp)
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
