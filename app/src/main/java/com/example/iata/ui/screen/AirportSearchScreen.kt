package com.example.iata.ui.screen

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.iata.data.Airport
import com.example.iata.data.Favorite
import com.example.iata.ui.FlightSearchViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AirportSearchScreen(
    viewModel: FlightSearchViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val suggestions by viewModel.suggestions.collectAsState()
    val flightResults by viewModel.flightResults.collectAsState()
    val favorites by viewModel.favorites.collectAsState()

    BackHandler(enabled = uiState.searchQuery.isNotEmpty() || uiState.selectedAirport != null) {
        if (uiState.selectedAirport != null) {
            viewModel.selectAirport(null)
        } else {
            viewModel.updateSearchQuery("")
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        OutlinedTextField(
            value = uiState.searchQuery,
            onValueChange = { viewModel.updateSearchQuery(it) },
            label = { Text("Enter airport name or IATA") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        if (uiState.searchQuery.isNotEmpty() && uiState.selectedAirport == null) {
            // Show suggestions
            LazyColumn {
                items(suggestions) { airport ->
                    SuggestionItem(airport = airport, onClick = { viewModel.selectAirport(airport) })
                }
            }
        } else if (uiState.selectedAirport != null) {
            // Show flight results from selected airport
            Text(
                text = "Flights from ${uiState.selectedAirport?.iataCode}",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(vertical = 8.dp)
            )
            LazyColumn {
                items(flightResults) { destination ->
                    FlightRow(
                        departure = uiState.selectedAirport!!,
                        destination = destination,
                        viewModel = viewModel
                    )
                }
            }
        } else if (uiState.searchQuery.isEmpty()) {
            // Show favorites
            Text(
                text = "Favorite Routes",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(vertical = 8.dp)
            )
            LazyColumn {
                items(favorites) { favorite ->
                    FavoriteRow(favorite = favorite, viewModel = viewModel)
                }
            }
        }
    }
}

@Composable
fun SuggestionItem(airport: Airport, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp, horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = airport.iataCode,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.width(60.dp)
        )
        Text(text = airport.name)
    }
}

@Composable
fun FlightRow(departure: Airport, destination: Airport, viewModel: FlightSearchViewModel) {
    val isFavorite by viewModel.isFavorite(departure.iataCode, destination.iataCode).collectAsState(initial = false)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = "DEPART", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                Text(text = "${departure.iataCode} - ${departure.name}", fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = "ARRIVE", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                Text(text = "${destination.iataCode} - ${destination.name}", fontWeight = FontWeight.Bold)
            }
            IconButton(onClick = { viewModel.toggleFavorite(departure.iataCode, destination.iataCode, isFavorite) }) {
                Icon(
                    imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                    contentDescription = "Favorite",
                    tint = if (isFavorite) Color.Red else Color.Gray
                )
            }
        }
    }
}

@Composable
fun FavoriteRow(favorite: Favorite, viewModel: FlightSearchViewModel) {
    var departureAirport by remember { mutableStateOf<Airport?>(null) }
    var destinationAirport by remember { mutableStateOf<Airport?>(null) }

    LaunchedEffect(favorite) {
        departureAirport = viewModel.getAirportByIata(favorite.departureCode)
        destinationAirport = viewModel.getAirportByIata(favorite.destinationCode)
    }

    if (departureAirport != null && destinationAirport != null) {
        FlightRow(departure = departureAirport!!, destination = destinationAirport!!, viewModel = viewModel)
    }
}
