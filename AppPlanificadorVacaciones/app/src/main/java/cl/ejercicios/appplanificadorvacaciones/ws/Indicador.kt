package cl.ejercicios.appplanificadorvacaciones.ws

data class Indicador(
    val codigo:String,
    val valor:Double
    )

data class Dolar(
    val dolar:Indicador
){
    fun getValorDolar():Double{
        return dolar.valor
    }
}