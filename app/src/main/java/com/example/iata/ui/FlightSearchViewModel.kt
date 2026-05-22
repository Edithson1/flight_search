package com.example.iata.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.iata.data.Airport
import com.example.iata.data.AirportDao
import com.example.iata.data.Favorite
import com.example.iata.data.UserPreferencesRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.ExperimentalCoroutinesApi

data class FlightSearchUiState(
    val searchQuery: String = "",
    val selectedAirport: Airport? = null,
    val suggestions: List<Airport> = emptyList(),
    val flightResults: List<Airport> = emptyList(),
    val favorites: List<Favorite> = emptyList()
)

@OptIn(ExperimentalCoroutinesApi::class)
class FlightSearchViewModel(
    private val airportDao: AirportDao,
    private val userPreferencesRepository: UserPreferencesRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(FlightSearchUiState())
    val uiState: StateFlow<FlightSearchUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            userPreferencesRepository.searchQuery.collect { query ->
                if (_uiState.value.searchQuery != query) {
                    _uiState.value = _uiState.value.copy(searchQuery = query)
                }
            }
        }
        viewModelScope.launch {
            userPreferencesRepository.selectedAirportIata.collect { iata ->
                if (iata.isNotEmpty()) {
                    val airport = airportDao.getAirportByIata(iata)
                    _uiState.value = _uiState.value.copy(selectedAirport = airport)
                }
            }
        }
    }

    fun updateSearchQuery(query: String) {
        if (_uiState.value.searchQuery != query) {
            _uiState.value = _uiState.value.copy(searchQuery = query, selectedAirport = null)
            viewModelScope.launch {
                userPreferencesRepository.saveSearchQuery(query)
                userPreferencesRepository.saveSelectedAirportIata("")
            }
        }
    }

    fun selectAirport(airport: Airport?) {
        _uiState.value = _uiState.value.copy(selectedAirport = airport)
        viewModelScope.launch {
            userPreferencesRepository.saveSelectedAirportIata(airport?.iataCode ?: "")
        }
    }

    val suggestions: StateFlow<List<Airport>> = _uiState
        .flatMapLatest { state ->
            if (state.searchQuery.isBlank()) flowOf(emptyList())
            else airportDao.getAirportsByQuery(state.searchQuery)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val flightResults: StateFlow<List<Airport>> = _uiState
        .flatMapLatest { state ->
            val airport = state.selectedAirport
            if (airport == null) flowOf(emptyList())
            else airportDao.getAllDestinationsExcept(airport.id)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val favorites: StateFlow<List<Favorite>> = airportDao.getFavorites()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun toggleFavorite(departureCode: String, destinationCode: String, isFavorite: Boolean) {
        viewModelScope.launch {
            if (isFavorite) {
                airportDao.deleteFavorite(departureCode, destinationCode)
            } else {
                airportDao.insertFavorite(Favorite(departureCode = departureCode, destinationCode = destinationCode))
            }
        }
    }

    fun isFavorite(departureCode: String, destinationCode: String): Flow<Boolean> {
        return airportDao.isFavorite(departureCode, destinationCode)
    }

    suspend fun getAirportByIata(iataCode: String): Airport? {
        return airportDao.getAirportByIata(iataCode)
    }

    companion object {
        fun factory(airportDao: AirportDao, userPreferencesRepository: UserPreferencesRepository): ViewModelProvider.Factory = viewModelFactory {
            initializer {
                FlightSearchViewModel(airportDao, userPreferencesRepository)
            }
        }
    }
}
