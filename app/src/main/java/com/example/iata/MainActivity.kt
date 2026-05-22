package com.example.iata

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.iata.data.FlightDatabase
import com.example.iata.data.UserPreferencesRepository
import com.example.iata.ui.FlightSearchViewModel
import com.example.iata.ui.screen.AirportSearchScreen
import com.example.iata.ui.theme.IATATheme

private const val SEARCH_PREFERENCES_NAME = "search_preferences"
private val Context.dataStore by preferencesDataStore(name = SEARCH_PREFERENCES_NAME)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        val database = FlightDatabase.getDatabase(this)
        val userPreferencesRepository = UserPreferencesRepository(dataStore)
        
        enableEdgeToEdge()
        setContent {
            IATATheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    val viewModel: FlightSearchViewModel = viewModel(
                        factory = FlightSearchViewModel.factory(
                            database.airportDao(),
                            userPreferencesRepository
                        )
                    )
                    AirportSearchScreen(
                        viewModel = viewModel,
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}
