package cl.ejercicios.appplanificadorvacaciones

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.rememberDateRangePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import cl.ejercicios.appplanificadorvacaciones.db.DataBase
import cl.ejercicios.appplanificadorvacaciones.db.Lugar
import cl.ejercicios.appplanificadorvacaciones.ui.theme.AppPlanificadorVacacionesTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class Formulario : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            var id = intent.extras?.getInt("idLugar")?:-1
            FormularioUI(id)
        }
    }
}



@Preview(showBackground = true)
@Composable
fun FormularioUI(id:Int) {

    val (nombreLugar, setNombreLugar) = remember {mutableStateOf("")}
    val (imagenReferencial, setImagenReferencial) = remember {mutableStateOf("")}
    val (latitudLongitud, setLatitudLongitud) = remember {mutableStateOf("")}
    val (orden, setOrden) = remember {mutableStateOf("")}
    val (costoAlojamiento, setCostoAlojamiento) = remember {mutableStateOf("")}
    val (costoTraslado, setCostoTraslado) = remember {mutableStateOf("")}
    val (comentarios, setComentarios) = remember {mutableStateOf("")}
    val contexto = LocalContext.current
    val coroutineScope  = rememberCoroutineScope()
    var latitud = 0.0
    var longitud = 0.0
    var lugar:Lugar

    if (id>-1) {
        LaunchedEffect(Unit) {
            withContext(Dispatchers.IO){
                val dao = DataBase.getInstance( contexto ).lugarDAO()
                lugar = dao.findAll().find { it.id == id }!!
                setNombreLugar(lugar.lugar)
                setImagenReferencial(lugar.imagenReferencial)
                setLatitudLongitud("${lugar.latitud},${lugar.longitud}")
                setOrden("${lugar.orden}")
                setCostoAlojamiento(lugar.costoAlojamiento.toString())
                setCostoTraslado(lugar.costoTraslados.toString())
                setComentarios(lugar.comentarios)
            }
        }
    }

    fun separaLatitudLongitud() {
        var latLon = latitudLongitud.split(',')
        latitud = latLon[0].toDouble()
        longitud = latLon[1].toDouble()
    }

    Column (
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxSize()
            .padding(all = 20.dp)
    ){
        Text(stringResource(R.string.t_lugar),
            fontSize = 4.em,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(top = 14.dp))
        TextField(
            value = nombreLugar,
            onValueChange = { setNombreLugar(it) },
            modifier = Modifier.fillMaxWidth()
                .padding(vertical = 8.dp)
        )
        Text(stringResource(R.string.t_imagen),
            fontSize = 4.em,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(top = 14.dp))
        TextField(
            value = imagenReferencial,
            onValueChange = { setImagenReferencial(it) },
            modifier = Modifier.fillMaxWidth()
                .padding(vertical = 8.dp)
        )
        Text(stringResource(R.string.t_coordenadas),
            fontSize = 4.em,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(top = 14.dp))
        TextField(
            value = latitudLongitud,
            onValueChange = { setLatitudLongitud(it) },
            modifier = Modifier.fillMaxWidth()
                .padding(vertical = 8.dp)
        )
        Text(stringResource(R.string.t_orden),
            fontSize = 4.em,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(top = 14.dp))
        TextField(
            value = orden,
            onValueChange = { setOrden(it) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
                .padding(vertical = 8.dp)
        )
        Text(stringResource(R.string.t_costo_noche),
            fontSize = 4.em,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(top = 14.dp))
        TextField(
            value = costoAlojamiento,
            onValueChange = { setCostoAlojamiento(it) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
                .padding(vertical = 8.dp)
        )
        Text(stringResource(R.string.t_traslados),
            fontSize = 4.em,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(top = 14.dp))
        TextField(
            value = costoTraslado,
            onValueChange = { setCostoTraslado(it) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
                .padding(vertical = 8.dp)
        )
        Text(stringResource(R.string.t_comentarios),
            fontSize = 4.em,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(top = 14.dp))
        TextField(
            value = comentarios,
            onValueChange = { setComentarios(it) },
            modifier = Modifier.fillMaxWidth()
                .padding(vertical = 8.dp)
        )
        Button(
            modifier = Modifier.padding(top = 15.dp),
            onClick = {
                val intent = Intent(contexto, MainActivity::class.java)
                var ordenNumero = orden.toInt()
                var costoNoche = costoAlojamiento.toInt()
                var costoTrasladosNumero = costoTraslado.toInt()
                separaLatitudLongitud()
                if (id==-1){
                    lugar = Lugar(lugar = nombreLugar, imagenReferencial = imagenReferencial,
                        latitud = latitud, longitud = longitud, orden = ordenNumero, costoAlojamiento = costoNoche,
                        costoTraslados = costoTrasladosNumero, comentarios = comentarios)

                    coroutineScope.launch(Dispatchers.IO) {
                        val dao = DataBase.getInstance( contexto ).lugarDAO()
                        dao.insertar(lugar)
                        contexto.startActivity(intent)
                    }
                } else {
                    lugar = Lugar(id = id, lugar = nombreLugar, imagenReferencial = imagenReferencial,
                        latitud = latitud, longitud = longitud, orden = ordenNumero, costoAlojamiento = costoNoche,
                        costoTraslados = costoTrasladosNumero, comentarios = comentarios)

                    coroutineScope.launch(Dispatchers.IO) {
                        val dao = DataBase.getInstance( contexto ).lugarDAO()
                        dao.actualizar(lugar)
                        contexto.startActivity(intent)
                    }
                }
            }
        ) {
            Text(stringResource(R.string.btn_guardar))
        }
    }
}