package cl.ejercicios.appplanificadorvacaciones

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import cl.ejercicios.appplanificadorvacaciones.db.DataBase
import cl.ejercicios.appplanificadorvacaciones.db.Lugar
import cl.ejercicios.appplanificadorvacaciones.ui.theme.AppPlanificadorVacacionesTheme
import cl.ejercicios.appplanificadorvacaciones.ws.Fabrica
import cl.ejercicios.appplanificadorvacaciones.ws.Dolar
import coil.compose.AsyncImage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.Dispatcher

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ListadoLugaresUI()
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ListadoLugaresUI() {

    val contexto = LocalContext.current
    val coroutineScope  = rememberCoroutineScope()
    val (valorDolar, setValorDolar) = remember { mutableDoubleStateOf(1.0) }
    val (listaLugares, setListaLugares) = remember {  mutableStateOf(emptyList<Lugar>()) }

    LaunchedEffect(Unit) {
        withContext(Dispatchers.IO){
            val dao = DataBase.getInstance( contexto ).lugarDAO()
            setListaLugares(dao.findAll())
            val service = Fabrica.getDolarService()
            setValorDolar( service.buscar().getValorDolar())
        }
    }

    Column (
        modifier = Modifier.fillMaxSize()
    ){
        if(listaLugares.isNotEmpty()){
            LazyColumn (
                modifier = Modifier.fillMaxWidth()
                    .padding(vertical = 20.dp).fillMaxHeight(0.9f),
                verticalArrangement = Arrangement.Top,
                horizontalAlignment = Alignment.CenterHorizontally

            ) {
                items(listaLugares) { lugar ->
                    Row (
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                            .padding(all = 20.dp)
                            .clickable {
                                coroutineScope.launch(Dispatchers.IO) {
                                    val intent = Intent(contexto, DetalleLugar::class.java)
                                    contexto.startActivity(intent.putExtra("idLugar", lugar.id))
                                }
                            }
                    ) {

                        AsyncImage(
                            model = lugar.imagenReferencial,
                            contentDescription = "Imagen de",
                            error = painterResource(R.drawable.imagennodisponible),
                            modifier = Modifier.width(130.dp)
                        )
                        Column {
                            Text(lugar.lugar,
                                fontSize = 4.em,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                            Row {
                                Text(stringResource(R.string.t_costo_noche) + ":",
                                    fontWeight = FontWeight.Bold)
                                Text("$${lugar.costoAlojamiento} - ${(lugar.costoAlojamiento/valorDolar.toInt())}USD")
                            }
                            Row {
                                Text(stringResource(R.string.t_traslados) + ":",
                                    fontWeight = FontWeight.Bold)
                                Text("$${lugar.costoTraslados} - ${(lugar.costoTraslados/valorDolar.toInt())}USD")
                            }
                            Row(

                            ){
                                Image(
                                    painter = painterResource(id = R.drawable.icono_de_localizacion),
                                    contentDescription = "Icono Ubicacion",
                                    modifier = Modifier.width(20.dp)
                                        .clickable {
                                            coroutineScope.launch(Dispatchers.IO) {
                                                val intent = Intent(contexto, DetalleLugar::class.java)
                                                intent.putExtra("pantalla","mapa")
                                                contexto.startActivity(intent.putExtra("idLugar", lugar.id))
                                            }
                                        }
                                )
                                Image(
                                    painter = painterResource(id = R.drawable.iconoeditar),
                                    contentDescription = "Icono Editar",
                                    modifier = Modifier.padding(horizontal = 10.dp)
                                        .width(20.dp)
                                        .clickable {
                                            coroutineScope.launch(Dispatchers.IO) {
                                                val intent = Intent(contexto, Formulario::class.java)
                                                contexto.startActivity(intent.putExtra("idLugar", lugar.id))
                                            }
                                        }
                                )
                                Image(
                                    painter = painterResource(id = R.drawable.iconoborrar),
                                    contentDescription = "Icono Borrar",
                                    modifier = Modifier.width(20.dp)
                                        .clickable {
                                            coroutineScope.launch(Dispatchers.IO) {
                                                val dao = DataBase.getInstance( contexto ).lugarDAO()
                                                dao.eliminar(lugar)
                                                setListaLugares(dao.findAll())
                                            }
                                        }
                                )
                            }
                        }
                    }
                }
            }
        }

        Button(
            onClick = {
                val intent = Intent(contexto, Formulario::class.java)
                contexto.startActivity(intent)
            },
            modifier = Modifier.background(color = Color.White)
        ) {
            Image(
                painter = painterResource(id = R.drawable.iconomas),
                contentDescription = "Icono Mas",
                modifier = Modifier.width(30.dp)
            )
            Text("  " + stringResource(R.string.btn_agregar))
        }
    }

}