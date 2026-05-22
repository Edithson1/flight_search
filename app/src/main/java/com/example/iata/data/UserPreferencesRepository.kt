package com.example.iata.data

import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

class UserPreferencesRepository(private val dataStore: DataStore<Preferences>) {

    private companion object {
        val SEARCH_QUERY = stringPreferencesKey("search_query")
        val SELECTED_AIRPORT_IATA = stringPreferencesKey("selected_airport_iata")
        const val TAG = "UserPreferencesRepo"
    }

    val searchQuery: Flow<String> = dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                Log.e(TAG, "Error reading preferences.", exception)
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            preferences[SEARCH_QUERY] ?: ""
        }

    val selectedAirportIata: Flow<String> = dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            preferences[SELECTED_AIRPORT_IATA] ?: ""
        }

    suspend fun saveSearchQuery(query: String) {
        dataStore.edit { preferences ->
            preferences[SEARCH_QUERY] = query
        }
    }

    suspend fun saveSelectedAirportIata(iataCode: String) {
        dataStore.edit { preferences ->
            preferences[SELECTED_AIRPORT_IATA] = iataCode
        }
    }
}
