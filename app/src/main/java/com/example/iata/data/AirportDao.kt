package com.example.iata.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface AirportDao {

    @Query("SELECT * FROM airport WHERE iata_code LIKE '%' || :query || '%' OR name LIKE '%' || :query || '%' ORDER BY passengers DESC")
    fun getAirportsByQuery(query: String): Flow<List<Airport>>

    @Query("SELECT * FROM airport WHERE id != :departureId ORDER BY passengers DESC")
    fun getAllDestinationsExcept(departureId: Int?): Flow<List<Airport>>

    @Query("SELECT * FROM airport WHERE iata_code = :iataCode")
    suspend fun getAirportByIata(iataCode: String): Airport?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFavorite(favorite: Favorite)

    @Query("DELETE FROM favorite WHERE departure_code = :departureCode AND destination_code = :destinationCode")
    suspend fun deleteFavorite(departureCode: String, destinationCode: String)

    @Query("SELECT * FROM favorite")
    fun getFavorites(): Flow<List<Favorite>>
    
    @Query("SELECT EXISTS(SELECT 1 FROM favorite WHERE departure_code = :departureCode AND destination_code = :destinationCode)")
    fun isFavorite(departureCode: String, destinationCode: String): Flow<Boolean>
}
