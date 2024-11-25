package cl.ejercicios.appplanificadorvacaciones.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

@Dao
interface LugarDAO {

    @Query("Select * From lugar Order By orden")
    fun findAll(): List<Lugar>

    @Insert
    fun insertar(lugar:Lugar):Long

    @Update
    fun actualizar(lugar:Lugar)

    @Delete
    fun eliminar(lugar:Lugar)
}