package org.example.project.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.example.project.data.models.Road
import org.example.project.data.models.RouteElevationResponse
import org.example.project.data.models.User
import org.example.project.ui.viewmodel.RoadDetailViewModel
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoadDetailScreen(
    viewModel: RoadDetailViewModel,
    roadId: Int,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    
    LaunchedEffect(roadId) {
        viewModel.loadRoad(roadId)
    }
    
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5))
    ) {
        TopAppBar(
            title = { Text("Детали маршрута") },
            navigationIcon = {
                IconButton(onClick = onBackClick) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                }
            }
        )
        
        when {
            uiState.isLoading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            uiState.error != null -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Ошибка: ${uiState.error}", color = Color.Red, modifier = Modifier.padding(16.dp))
                        Button(onClick = { viewModel.loadRoad(roadId) }) { Text("Повторить") }
                    }
                }
            }
            uiState.road != null -> {
                RoadDetailContent(
                    road = uiState.road!!,
                    routeDetails = uiState.routeDetails,
                    isDetailsLoading = uiState.isDetailsLoading,
                    onRefreshWeather = { newTime -> viewModel.updateDepartureTime(newTime) }
                )
            }
        }
    }
}

@Composable
fun RoadDetailContent(
    road: Road, 
    routeDetails: RouteElevationResponse?,
    isDetailsLoading: Boolean,
    onRefreshWeather: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var customStartTime by remember { mutableStateOf(road.startDateTime) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Основная инфо
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = road.name ?: "Маршрут #${road.id}",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f)
                    )
                    ComplexityBadge(road.complexity)
                }
                
                Text(text = road.description ?: "Описание отсутствует", fontSize = 14.sp, color = Color.Gray)
                
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                
                Text("Время отбытия для прогноза:", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    OutlinedTextField(
                        value = customStartTime,
                        onValueChange = { customStartTime = it },
                        modifier = Modifier.weight(1f),
                        textStyle = LocalTextStyle.current.copy(fontSize = 14.sp),
                        placeholder = { Text("ГГГГ-ММ-ДДTHH:ММ:СС") }
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(onClick = { onRefreshWeather(customStartTime) }) {
                        Text("Обновить", fontSize = 12.sp)
                    }
                }
            }
        }
        
        // Погода
        if (isDetailsLoading) {
            Box(modifier = Modifier.fillMaxWidth().height(100.dp), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (routeDetails != null && routeDetails.weatherTimeline.isNotEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFE3F2FD))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Прогноз погоды по маршруту", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Spacer(modifier = Modifier.height(12.dp))
                    LazyRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(routeDetails.weatherTimeline) { wp ->
                            WeatherPointItem(wp)
                        }
                    }
                }
            }
        }
        
        StatisticsCard(road, routeDetails)
        
        road.user?.let { UserInfoCard(it) }
    }
}

@Composable
fun StatisticsCard(road: Road, details: RouteElevationResponse?, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Статистика", fontSize = 16.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 12.dp))
            
            val dist = details?.statistics?.totalDistance?.toDouble() ?: road.totalDistance
            val climb = details?.statistics?.totalClimb?.toDouble() ?: road.totalClimb
            val descent = details?.statistics?.totalDescent?.toDouble() ?: road.totalDescent

            GridStatItem(label = "Дистанция", value = formatDistance(dist), modifier = Modifier.fillMaxWidth())
            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
            
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                GridStatItem(label = "Подъём", value = "${climb.toInt()} м", modifier = Modifier.weight(1f))
                GridStatItem(label = "Спуск", value = "${descent.toInt()} м", modifier = Modifier.weight(1f))
            }
        }
    }
}

@Composable
fun GridStatItem(label: String, value: String, modifier: Modifier = Modifier) {
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, fontSize = 16.sp, fontWeight = FontWeight.Bold)
        Text(label, fontSize = 12.sp, color = Color.Gray)
    }
}

@Composable
fun UserInfoCard(user: User, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Автор маршрута", fontSize = 16.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 12.dp))
            Text(user.fio ?: "Неизвестно", fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
            Text(user.email ?: "Неизвестно", fontSize = 12.sp, color = Color.Gray)
        }
    }
}

private fun formatDistance(meters: Double): String {
    return if (meters < 1000) "${meters.toInt()} м" else String.format(Locale.US, "%.1f км", meters / 1000.0)
}
