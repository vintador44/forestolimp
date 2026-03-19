package org.example.project.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.yandex.mapkit.geometry.Point
import com.yandex.mapkit.geometry.Polyline
import com.yandex.mapkit.map.CameraPosition
import com.yandex.mapkit.map.InputListener
import com.yandex.mapkit.map.Map
import com.yandex.mapkit.mapview.MapView
import org.example.project.data.api.RetrofitClient
import org.example.project.data.models.Road
import org.example.project.data.models.WeatherPoint
import org.example.project.ui.viewmodel.RoadsViewModel
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.*

@Composable
fun RoadsListScreen(
    viewModel: RoadsViewModel,
    onRoadClick: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    var showCreateDialog by remember { mutableStateOf(false) }
    
    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = { showCreateDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Создать маршрут")
            }
        }
    ) { padding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(padding)
                .background(Color(0xFFF5F5F5))
        ) {
            Text(
                "Маршруты",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(16.dp)
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
                            Button(onClick = { viewModel.loadRoads() }) { Text("Повторить") }
                        }
                    }
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(uiState.roads) { road ->
                            RoadCard(road = road, onClick = { onRoadClick(road.id) })
                        }
                    }
                }
            }
        }
    }

    if (showCreateDialog) {
        CreateRoadMapDialog(
            onDismiss = { showCreateDialog = false },
            onConfirm = { name, description, points, duration, date ->
                viewModel.createRoad(name, description, points, duration, date) { success ->
                    if (success) showCreateDialog = false
                }
            },
            viewModel = viewModel
        )
    }
}

@Composable
fun RoadCard(
    road: Road,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Парсим название и описание из поля Description (так как в БД нет отдельного поля Name)
    val fullDesc = road.description ?: ""
    val name = if (fullDesc.contains("\n")) fullDesc.substringBefore("\n") else road.name ?: "Маршрут #${road.id}"
    val description = if (fullDesc.contains("\n")) fullDesc.substringAfter("\n") else fullDesc

    // Считаем дистанцию на лету по точкам
    val distanceKm = remember(road.dots) {
        var total = 0.0
        for (i in 0 until road.dots.size - 1) {
            val p1 = road.dots[i].thisDotCoordinates
            val p2 = road.dots[i+1].thisDotCoordinates
            total += calculateDistance(p1.lat, p1.lng, p2.lat, p2.lng)
        }
        total
    }

    Card(
        modifier = modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = name, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.Black)
            if (description.isNotBlank()) {
                Text(text = description, fontSize = 13.sp, color = Color.Gray, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Автор: ${road.user?.fio ?: "Пользователь #${road.userId ?: road.id}"}",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(12.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                ComplexityBadge(road.complexity ?: "Средний")
                Text(String.format(Locale.US, "%.1f км", distanceKm), fontWeight = FontWeight.Bold, color = Color.DarkGray)
            }
        }
    }
}

// Формула Haversine для расчета расстояния между точками
fun calculateDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
    val r = 6371 // Радиус Земли
    val dLat = Math.toRadians(lat2 - lat1)
    val dLon = Math.toRadians(lon2 - lon1)
    val a = sin(dLat / 2).pow(2) + cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) * sin(dLon / 2).pow(2)
    val c = 2 * atan2(sqrt(a), sqrt(1 - a))
    return r * c
}

@Composable
fun ComplexityBadge(complexity: String?) {
    val color = when (complexity?.lowercase()) {
        "лёгкий" -> Color(0xFF81C784)
        "средний" -> Color(0xFFFFEB3B)
        "сложный" -> Color(0xFFFF9800)
        "эксперт" -> Color(0xFFE53935)
        else -> Color(0xFFE0E0E0)
    }
    Surface(color = color, shape = RoundedCornerShape(20.dp)) {
        Text(text = complexity ?: "Средний", fontSize = 10.sp, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), color = Color.Black)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateRoadMapDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, String, List<Point>, Double, String) -> Unit,
    viewModel: RoadsViewModel
) {
    var step by remember { mutableStateOf(1) }
    val points = remember { mutableStateListOf<Point>() }
    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var duration by remember { mutableStateOf("3.0") }
    
    val currentDateTime = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault()).format(Date())
    var startDateTime by remember { mutableStateOf(currentDateTime) }
    
    val uiState by viewModel.uiState.collectAsState()

    AlertDialog(
        onDismissRequest = onDismiss,
        properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false),
        modifier = Modifier.fillMaxSize(),
        content = {
            Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                if (step == 1) {
                    Column {
                        Box(modifier = Modifier.weight(1f)) {
                            RoadCreationMapView(points)
                            IconButton(onClick = onDismiss, modifier = Modifier.align(Alignment.TopStart).padding(16.dp)) { Icon(Icons.Default.Close, "Close") }
                            if (points.isNotEmpty()) {
                                Button(onClick = { points.removeAt(points.size - 1) }, modifier = Modifier.align(Alignment.BottomStart).padding(16.dp), colors = ButtonDefaults.buttonColors(containerColor = Color.Red)) { Text("Удалить точку") }
                            }
                        }
                        Card(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text("Точек: ${points.size}", fontWeight = FontWeight.Bold)
                                Button(onClick = { step = 2 }, enabled = points.size >= 2, modifier = Modifier.fillMaxWidth()) { Text("Далее") }
                            }
                        }
                    }
                } else {
                    Column(modifier = Modifier.padding(16.dp).verticalScroll(rememberScrollState())) {
                        Text("Новый маршрут", style = MaterialTheme.typography.headlineSmall)
                        Spacer(modifier = Modifier.height(16.dp))
                        OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Название") }, modifier = Modifier.fillMaxWidth())
                        OutlinedTextField(value = description, onValueChange = { description = it }, label = { Text("Описание") }, modifier = Modifier.fillMaxWidth())
                        OutlinedTextField(value = startDateTime, onValueChange = { startDateTime = it }, label = { Text("Дата старта") }, modifier = Modifier.fillMaxWidth())
                        
                        Spacer(modifier = Modifier.height(24.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedButton(onClick = { step = 1 }, modifier = Modifier.weight(1f)) { Text("Назад") }
                            Button(
                                onClick = { onConfirm(name, description, points.toList(), duration.toDoubleOrNull() ?: 3.0, startDateTime) },
                                enabled = name.isNotBlank(),
                                modifier = Modifier.weight(1f)
                            ) { Text("Создать") }
                        }
                    }
                }
            }
        }
    )
}

@Composable
fun RoadCreationMapView(points: MutableList<Point>) {
    val inputListener = remember {
        object : InputListener {
            override fun onMapTap(map: Map, point: Point) { points.add(point) }
            override fun onMapLongTap(map: Map, point: Point) {}
        }
    }
    AndroidView(
        factory = { context -> MapView(context).apply { map.move(CameraPosition(Point(55.7558, 37.6173), 10.0f, 0.0f, 0.0f)); map.addInputListener(inputListener) } },
        update = { mapView ->
            mapView.map.mapObjects.clear()
            if (points.size >= 2) mapView.map.mapObjects.addPolyline(Polyline(points)).apply { strokeWidth = 5f; setStrokeColor(android.graphics.Color.BLUE) }
            points.forEach { mapView.map.mapObjects.addPlacemark(it) }
        },
        modifier = Modifier.fillMaxSize()
    )
}

@Composable
fun WeatherPointItem(wp: WeatherPoint) {
    Card(shape = RoundedCornerShape(8.dp), colors = CardDefaults.cardColors(containerColor = Color.White), elevation = CardDefaults.cardElevation(2.dp), modifier = Modifier.width(100.dp)) {
        Column(modifier = Modifier.padding(8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(wp.estimatedTime.split("T").last().substring(0, 5), fontSize = 10.sp, color = Color.Gray)
            Icon(Icons.Default.WbSunny, null, tint = Color(0xFFFFB300), modifier = Modifier.size(24.dp))
            Text("${wp.weather?.temperature?.toInt() ?: 0}°C", fontWeight = FontWeight.Bold)
        }
    }
}
