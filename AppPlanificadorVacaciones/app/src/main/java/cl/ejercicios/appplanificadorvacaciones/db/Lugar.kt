package cl.ejercicios.appplanificadorvacaciones.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Lugar(

    @PrimaryKey(autoGenerate = true) var id:Int=0,
    var lugar:String,
    var imagenReferencial:String,
    var latitud:Double,
    var longitud:Double,
    var orden:Int,
    var costoAlojamiento:Int,
    var costoTraslados:Int,
    var comentarios:String=""
)
