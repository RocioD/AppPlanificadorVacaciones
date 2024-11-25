package cl.ejercicios.appplanificadorvacaciones

import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCapture.OnImageSavedCallback
import androidx.camera.core.ImageCapture.OutputFileOptions
import androidx.camera.core.ImageCaptureException
import androidx.camera.view.LifecycleCameraController
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusGroup
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import cl.ejercicios.appplanificadorvacaciones.db.DataBase
import cl.ejercicios.appplanificadorvacaciones.db.Lugar
import cl.ejercicios.appplanificadorvacaciones.ui.theme.AppPlanificadorVacacionesTheme
import cl.ejercicios.appplanificadorvacaciones.ws.Fabrica
import coil.compose.AsyncImage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import java.io.File
import java.time.LocalDateTime

enum class Pantalla{
    DETALLE,
    CAMARA,
    MAPA
}

class AppVM:ViewModel() {
    val pantallaActual = mutableStateOf(Pantalla.DETALLE)
    var onPermisoCamaraOk:() -> Unit = {}
    var onPermisoUbicacionOk: () -> Unit = {}
}

class DatosLugar : ViewModel() {
    val lugar = mutableStateOf<Lugar?>(null)
    val foto = mutableStateOf< Uri? > ( null )
}

class DetalleLugar : ComponentActivity() {

    val appVM: AppVM by viewModels()

    lateinit var cameraController: LifecycleCameraController

    val lanzadorPermisos = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ){
        if ( it[android.Manifest.permission.CAMERA]?:false) {
            appVM.onPermisoCamaraOk()
        }
        if (
            ( it[android.Manifest.permission.ACCESS_FINE_LOCATION]?:false ) or
            ( it[android.Manifest.permission.ACCESS_COARSE_LOCATION]?:false )
        ){
            appVM.onPermisoUbicacionOk()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        cameraController = LifecycleCameraController(this)
        cameraController.bindToLifecycle(this)
        cameraController.cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

        enableEdgeToEdge()
        setContent {
            val id = intent.extras?.getInt("idLugar")?:-1
            intent.extras?.get("pantalla")?.let{
                if(it == "mapa") {
                    appVM.pantallaActual.value = Pantalla.MAPA
                }
            }
            AppUI(id, lanzadorPermisos, cameraController)
        }
    }
}

@Composable
fun AppUI(id: Int, lanzadorPermisos:ActivityResultLauncher<Array<String>>,
          cameraController: LifecycleCameraController){
    val  appVM:AppVM = viewModel()
    val datosLugar: DatosLugar = viewModel()
    val contexto = LocalContext.current

    LaunchedEffect(Unit) {
        withContext(Dispatchers.IO) {
            val dao = DataBase.getInstance(contexto).lugarDAO()
            datosLugar.lugar.value = dao.findAll().find { it.id == id }!!
        }
    }
    when(appVM.pantallaActual.value){
        Pantalla.DETALLE -> {
            DetalleLugarUI(id, lanzadorPermisos)
        }
        Pantalla.CAMARA -> {
            CamaraUI(cameraController, lanzadorPermisos)
        }
        Pantalla.MAPA -> {
            MapaUI(lanzadorPermisos)
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DetalleLugarUI(id:Int, lanzadorPermisos:ActivityResultLauncher<Array<String>>) {
    val contexto = LocalContext.current
    val coroutineScope  = rememberCoroutineScope()
    val (valorDolar, setValorDolar) = remember { mutableDoubleStateOf(0.0) }

    val appVM: AppVM = viewModel()
    val datosLugar: DatosLugar = viewModel()

    LaunchedEffect(Unit) {
        withContext(Dispatchers.IO) {
            val service = Fabrica.getDolarService()
            setValorDolar( service.buscar().getValorDolar())
        }
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxSize()
            .padding(all = 20.dp)
    ) {
        Image(
            painter = painterResource(id = R.drawable.iconovolver),
            contentDescription = "Volver",
            modifier = Modifier.padding(all = 20.dp)
                .clickable {
                    val intent = Intent(contexto, MainActivity::class.java)
                    contexto.startActivity(intent)
                }
                .width(20.dp)
        )
        Text("${datosLugar.lugar.value?.lugar}",
            fontSize = 6.em,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(top = 20.dp)
        )
        AsyncImage(
            model = datosLugar.lugar.value?.imagenReferencial,
            contentDescription = "Imagen de ${datosLugar.lugar.value?.lugar}",
            error = painterResource(R.drawable.imagennodisponible),
            modifier = Modifier.width(200.dp)
        )
        Row (
            horizontalArrangement = Arrangement.SpaceBetween
        ){
            Column {
                Text(
                    stringResource(R.string.t_costo_noche),
                    fontSize = 4.em,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 15.dp)
                )
                Text("$${datosLugar.lugar.value?.costoAlojamiento} - " +
                        "${datosLugar.lugar.value?.costoAlojamiento?.div(valorDolar)?.toInt()}USD",
                    modifier = Modifier.padding(top = 10.dp)
                )
            }
            Column {
                Text(
                    stringResource(R.string.t_traslados),
                    fontSize = 4.em,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 15.dp)
                )
                Text("$${datosLugar.lugar.value?.costoTraslados} - " +
                        "${datosLugar.lugar.value?.costoTraslados?.div(valorDolar)?.toInt()}USD",
                    modifier = Modifier.padding(top = 10.dp)
                )
            }
        }
        Text(
            stringResource(R.string.t_comentarios),
            fontSize = 4.em,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(top = 15.dp)
        )
        Text(text= "${datosLugar.lugar.value?.comentarios}",
            modifier = Modifier.padding(top = 10.dp, bottom = 15.dp)
        )
        Row{
            Image(
                painter = painterResource(id = R.drawable.iconocamara),
                contentDescription = "Icono Camara",
                modifier = Modifier
                    .width(20.dp)
                    .clickable {
                        appVM.pantallaActual.value = Pantalla.CAMARA
                    }
            )
            Image(
                painter = painterResource(id = R.drawable.iconoeditar),
                contentDescription = "Icono Editar",
                modifier = Modifier
                    .padding(horizontal = 10.dp)
                    .width(20.dp)
                    .clickable {
                        coroutineScope.launch(Dispatchers.IO) {
                            val intent = Intent(contexto, Formulario::class.java)
                            contexto.startActivity(intent.putExtra("idLugar", id))
                        }
                    }
            )
            Image(
                painter = painterResource(id = R.drawable.iconoborrar),
                contentDescription = "Icono Borrar",
                modifier = Modifier
                    .width(20.dp)
                    .clickable {
                        coroutineScope.launch(Dispatchers.IO) {
                            val intent = Intent(contexto, MainActivity::class.java)
                            val dao = DataBase
                                .getInstance(contexto)
                                .lugarDAO()
                            datosLugar.lugar.value?.let { dao.eliminar(it) }
                            contexto.startActivity(intent)
                        }
                    }
            )
        }
        Text(text= stringResource(R.string.t_coordenadas),
            modifier = Modifier.padding(top = 10.dp, bottom = 15.dp)
        )
        Text(text= "${datosLugar.lugar.value?.latitud},${datosLugar.lugar.value?.longitud}",
            modifier = Modifier.padding(top = 10.dp, bottom = 15.dp)
        )
        Image(
            painter = painterResource(id = R.drawable.icono_de_localizacion),
            contentDescription = "Icono Editar",
            modifier = Modifier
                .padding(horizontal = 10.dp)
                .width(100.dp)
                .clickable {
                    coroutineScope.launch(Dispatchers.IO) {
                        appVM.pantallaActual.value = Pantalla.MAPA
                    }
                }
        )

        datosLugar.foto.value?.let {
            Image(
                painter = BitmapPainter(uri2imageBitmap(it, contexto)),
                contentDescription = "Imagen capturada desde CameraX",
                modifier = Modifier
                    .padding(all = 40.dp)
                    .width(100.dp)
                    .rotate(90f)
            )
        }
    }
}

@Composable
fun MapaUI(lanzadorPermisos: ActivityResultLauncher<Array<String>>){
    lanzadorPermisos.launch(arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION,
        android.Manifest.permission.ACCESS_COARSE_LOCATION))

    val contexto = LocalContext.current
    val appVM:AppVM = viewModel()
    val datosLugar:DatosLugar = viewModel()

    Box(contentAlignment = Alignment.TopStart,
        modifier = Modifier.fillMaxSize())
    {
        AndroidView(
            factory = {
                MapView(it).apply {
                    setTileSource(TileSourceFactory.MAPNIK)
                    org.osmdroid.config.Configuration.getInstance().userAgentValue = contexto.packageName
                    controller.setZoom(15.0)
                }
            }, update = {
                it.overlays.removeIf{ true }
                it.invalidate()

                var geoPoint = GeoPoint(0.0,0.0)
                datosLugar.lugar.value?.let{
                    geoPoint = GeoPoint(it.latitud, it.longitud)
                }
                it.controller.animateTo(geoPoint)

                val marcador = Marker(it)
                marcador.position = geoPoint
                marcador.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER)
                it.overlays.add(marcador)
            }
        )
        Image(
            painter = painterResource(id = R.drawable.iconovolver),
            contentDescription = "Volver",
            modifier = Modifier.padding(all = 20.dp)
                .clickable {
                    appVM.pantallaActual.value = Pantalla.DETALLE
                }
                .width(20.dp)
        )
    }
}

@Composable
fun CamaraUI(cameraController: LifecycleCameraController,
             lanzadorPermisos:ActivityResultLauncher<Array<String>>){
    lanzadorPermisos.launch(arrayOf(android.Manifest.permission.CAMERA))

    val contexto = LocalContext.current
    val datosLugar: DatosLugar = viewModel()
    val appVM: AppVM = viewModel()

    Box(contentAlignment = Alignment.BottomCenter,
        modifier = Modifier.fillMaxSize()
            .padding(all = 10.dp)){
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = {
                PreviewView(it).apply {
                    controller = cameraController
                }
            }
        )
        Image(
            alignment = Alignment.Center,
            painter = painterResource(id = R.drawable.iconocamara),
            contentDescription = "Icono Camara",
            modifier = Modifier
                .width(30.dp)
                .clickable {
                    tomarFoto(
                        cameraController,
                        crearArchivoImagenPrivado(contexto,datosLugar.lugar.value),
                        contexto){
                        datosLugar.foto.value = it
                        Log.v("Uri", it.toString())
                        appVM.pantallaActual.value= Pantalla.DETALLE
                    }
                }
        )
    }
}

fun generarNombreSegunFechaHastaSegundo():String = LocalDateTime
    .now().toString().replace(Regex("[T:.-]"), "").substring(0, 14)

fun generarNombreSegunLugar(lugar: Lugar?):String {
    val id = lugar?.id.toString()
    val ajustador = "00000".substring(0, (5-id.length)) + id
    //Log.v("nombre", ajustador)
    val nombre = generarNombreSegunFechaHastaSegundo()+ajustador

    return nombre
}

fun crearArchivoImagenPrivado(contexto:Context, lugar:Lugar?): File = File(
    contexto.getExternalFilesDir(Environment.DIRECTORY_PICTURES),
    "${generarNombreSegunLugar(lugar)}.jpg"
)

fun tomarFoto(
    cameraController: LifecycleCameraController,
    archivo: File,
    contexto: Context,
    onImagenGuardada: (uri:Uri) -> Unit
){
    val opciones = OutputFileOptions.Builder(archivo).build()
    cameraController.takePicture(
        opciones,
        ContextCompat.getMainExecutor(contexto),
        object : OnImageSavedCallback {
            override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                outputFileResults.savedUri?.let{
                    onImagenGuardada(it)
                }
            }
            override fun onError(exception: ImageCaptureException) {
                Log.e("OnImageSavedCallback::onError", exception.message?:"Error")
            }
        }
    )
}

fun uri2imageBitmap(uri:Uri, contexto: Context) = BitmapFactory.decodeStream(
    contexto.contentResolver.openInputStream(uri)
).asImageBitmap()