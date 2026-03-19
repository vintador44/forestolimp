package org.example.project.ui.screens

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import org.example.project.viewmodel.MapViewModel
import androidx.compose.ui.viewinterop.AndroidView
import com.yandex.mapkit.mapview.MapView
import com.yandex.mapkit.map.CameraPosition
import com.yandex.mapkit.geometry.Point
import com.yandex.mapkit.geometry.Polyline
import com.yandex.mapkit.map.InputListener
import com.yandex.mapkit.map.Map
import com.yandex.mapkit.map.MapObjectTapListener
import com.yandex.runtime.image.ImageProvider
import org.example.project.data.models.Location
import org.example.project.data.models.Road
import org.example.project.data.api.PhotoData
import android.util.Base64
import android.graphics.BitmapFactory
import java.io.ByteArrayOutputStream

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapScreen(
    onNavigateToRoads: () -> Unit,
    onNavigateToLocations: () -> Unit,
    viewModel: MapViewModel = viewModel()
) {
    val roads by viewModel.roads.collectAsState()
    val locations by viewModel.locations.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val locationPhotos by viewModel.locationPhotos.collectAsState()

    var selectedPoint by remember { mutableStateOf<Point?>(null) }
    var showCreateLocationDialog by remember { mutableStateOf(false) }
    var selectedLocation by remember { mutableStateOf<Location?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Карта маршрутов") },
                actions = {
                    IconButton(onClick = onNavigateToRoads) {
                        Icon(Icons.Filled.List, contentDescription = "Маршруты")
                    }
                    IconButton(onClick = onNavigateToLocations) {
                        Icon(Icons.Filled.Place, contentDescription = "Локации")
                    }
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentAlignment = Alignment.Center
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                Box(modifier = Modifier.weight(1f)) {
                    YandexMapView(
                        locations = locations,
                        roads = roads,
                        onMapLongClick = { point ->
                            selectedPoint = point
                            showCreateLocationDialog = true
                        },
                        onLocationClick = { location ->
                            selectedLocation = location
                            viewModel.loadPhotos(location.id)
                        }
                    )
                    
                    if (isLoading) {
                        CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                    }
                }
                
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("Маршруты: ${roads.size}, Локации: ${locations.size}")
                        if (error != null) {
                            Text("Ошибка: $error", color = MaterialTheme.colorScheme.error, fontSize = 12.sp)
                        }
                        Text("Удерживайте палец на карте для добавления точки", style = MaterialTheme.typography.bodySmall)
                    }
                }
            }

            if (selectedLocation != null) {
                LocationDetailsSheet(
                    location = selectedLocation!!,
                    photos = locationPhotos[selectedLocation!!.id] ?: emptyList(),
                    onDismiss = { selectedLocation = null },
                    onUploadPhotos = { bytes ->
                        viewModel.uploadPhotos(selectedLocation!!.id, bytes)
                    }
                )
            }
        }
    }

    if (showCreateLocationDialog && selectedPoint != null) {
        AddLocationDialog(
            lat = selectedPoint!!.latitude,
            lng = selectedPoint!!.longitude,
            onDismiss = { 
                showCreateLocationDialog = false
                selectedPoint = null
            },
            onConfirm = { name, description, lat, lng, categories ->
                viewModel.createLocation(name, description, lat, lng, categories) { success ->
                    if (success) {
                        showCreateLocationDialog = false
                        selectedPoint = null
                    }
                }
            }
        )
    }
}

@Composable
fun YandexMapView(
    locations: List<Location>,
    roads: List<Road>,
    onMapLongClick: (Point) -> Unit,
    onLocationClick: (Location) -> Unit
) {
    val currentOnMapLongClick by rememberUpdatedState(onMapLongClick)
    val currentOnLocationClick by rememberUpdatedState(onLocationClick)
    
    val markerBitmap = remember {
        val size = 80
        val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val paint = Paint().apply {
            color = android.graphics.Color.RED
            style = Paint.Style.FILL
            isAntiAlias = true
        }
        canvas.drawCircle(size / 2f, size / 2f, size / 2.5f, paint)
        paint.color = android.graphics.Color.WHITE
        canvas.drawCircle(size / 2f, size / 2f, size / 5f, paint)
        bitmap
    }
    val imageProvider = remember { ImageProvider.fromBitmap(markerBitmap) }

    val inputListener = remember {
        object : InputListener {
            override fun onMapTap(map: Map, point: Point) {}
            override fun onMapLongTap(map: Map, point: Point) {
                currentOnMapLongClick(point)
            }
        }
    }

    val tapListener = remember {
        MapObjectTapListener { mapObject, _ ->
            val location = mapObject.userData as? Location
            if (location != null) {
                currentOnLocationClick(location)
                true
            } else false
        }
    }

    AndroidView(
        factory = { context ->
            MapView(context).apply {
                map.move(CameraPosition(Point(55.7558, 37.6173), 10.0f, 0.0f, 0.0f))
                map.addInputListener(inputListener)
            }
        },
        update = { mapView ->
            val mapObjects = mapView.map.mapObjects
            mapObjects.clear()
            
            // Отрисовка маршрутов
            roads.forEach { road ->
                if (road.dots.size >= 2) {
                    val points = road.dots.map { dot ->
                        Point(dot.thisDotCoordinates.lat, dot.thisDotCoordinates.lng)
                    }
                    mapObjects.addPolyline(Polyline(points)).apply {
                        strokeWidth = 4f
                        setStrokeColor(android.graphics.Color.argb(180, 0, 120, 255))
                    }
                    
                    // Ставим маркер на начало маршрута
                    val startPoint = points.first()
                    mapObjects.addPlacemark(startPoint).apply {
                        setIcon(imageProvider)
                        // Можно добавить userData для клика по маршруту
                    }
                }
            }
            
            // Отрисовка локаций
            locations.forEach { location ->
                val coords = location.coordinates
                if (coords.size >= 2) {
                    val point = Point(coords[0], coords[1])
                    val placemark = mapObjects.addPlacemark(point)
                    placemark.setIcon(imageProvider)
                    placemark.userData = location
                    placemark.addTapListener(tapListener)
                }
            }
        },
        modifier = Modifier.fillMaxSize()
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LocationDetailsSheet(
    location: Location,
    photos: List<PhotoData>,
    onDismiss: () -> Unit,
    onUploadPhotos: (List<ByteArray>) -> Unit
) {
    val context = LocalContext.current
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents()
    ) { uris: List<Uri> ->
        val byteArrays = uris.mapNotNull { uri ->
            try {
                context.contentResolver.openInputStream(uri)?.use { inputStream ->
                    val originalBitmap = BitmapFactory.decodeStream(inputStream)
                    if (originalBitmap != null) {
                        compressImage(originalBitmap)
                    } else null
                }
            } catch (e: Exception) {
                null
            }
        }
        if (byteArrays.isNotEmpty()) {
            onUploadPhotos(byteArrays)
        }
    }

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Text(text = location.name ?: "Локация", fontSize = 20.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = location.description ?: "Описание отсутствует", fontSize = 14.sp)
            
            if (location.categories != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = "Категории: ${location.categories}", color = Color.Gray, fontSize = 12.sp)
            }

            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "Фотографии", fontWeight = FontWeight.Bold)
                Button(onClick = { photoPickerLauncher.launch("image/*") }) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Text("Добавить фото")
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            if (photos.isEmpty()) {
                Text("Нет фотографий", color = Color.Gray, modifier = Modifier.padding(vertical = 16.dp))
            } else {
                LazyRow(
                    modifier = Modifier.height(150.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(photos) { photo ->
                        PhotoItem(photo)
                    }
                }
            }
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

fun compressImage(bitmap: Bitmap): ByteArray {
    val stream = ByteArrayOutputStream()
    val maxSize = 1600
    var width = bitmap.width
    var height = bitmap.height
    
    if (width > maxSize || height > maxSize) {
        val ratio = width.toFloat() / height.toFloat()
        if (ratio > 1) {
            width = maxSize
            height = (width / ratio).toInt()
        } else {
            height = maxSize
            width = (height * ratio).toInt()
        }
    }
    
    val scaledBitmap = Bitmap.createScaledBitmap(bitmap, width, height, true)
    scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 75, stream)
    return stream.toByteArray()
}

@Composable
fun PhotoItem(photo: PhotoData) {
    val bitmap = remember(photo.base64) {
        try {
            val decodedString = Base64.decode(photo.base64, Base64.DEFAULT)
            BitmapFactory.decodeByteArray(decodedString, 0, decodedString.size)
        } catch (e: Exception) {
            null
        }
    }

    Card(
        modifier = Modifier.width(150.dp).fillMaxHeight(),
        shape = RoundedCornerShape(8.dp)
    ) {
        if (bitmap != null) {
            Image(
                bitmap = bitmap.asImageBitmap(),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        } else {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddLocationDialog(
    lat: Double,
    lng: Double,
    onDismiss: () -> Unit,
    onConfirm: (String, String, Double, Double, String?) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    
    val availableCategories = listOf(
        "Лес", "Озеро", "Дворец", "Водопады", "Пещеры", 
        "Водохранилища", "Вулканы", "Горы", "Реки", "Каньоны", 
        "Карьеры", "Остров", "Поле", "Деревня", "Ущелья", 
        "Святые места", "Тропы", "Пруды", "Пляжи", "Мистические места"
    )
    
    val selectedCategories = remember { mutableStateListOf<String>() }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Новая локация") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text("Координаты: ${"%.4f".format(lat)}, ${"%.4f".format(lng)}", style = MaterialTheme.typography.bodySmall)
                
                OutlinedTextField(
                    value = name, 
                    onValueChange = { name = it }, 
                    label = { Text("Название") },
                    modifier = Modifier.fillMaxWidth()
                )
                
                OutlinedTextField(
                    value = description, 
                    onValueChange = { description = it }, 
                    label = { Text("Описание") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2
                )
                
                Text("Выберите категории:", style = MaterialTheme.typography.titleSmall)
                
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    mainAxisSpacing = 8.dp,
                    crossAxisSpacing = 8.dp
                ) {
                    availableCategories.forEach { category ->
                        val isSelected = selectedCategories.contains(category)
                        FilterChip(
                            selected = isSelected,
                            onClick = {
                                if (isSelected) selectedCategories.remove(category)
                                else selectedCategories.add(category)
                            },
                            label = { Text(category, fontSize = 12.sp) },
                            leadingIcon = if (isSelected) {
                                { Icon(Icons.Filled.Check, contentDescription = null, modifier = Modifier.size(16.dp)) }
                            } else null
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { 
                    val categoriesString = if (selectedCategories.isEmpty()) null else selectedCategories.joinToString(", ")
                    onConfirm(name, description, lat, lng, categoriesString) 
                },
                enabled = name.isNotBlank() && description.isNotBlank()
            ) {
                Text("Создать")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Отмена")
            }
        }
    )
}

@Composable
fun FlowRow(
    modifier: Modifier = Modifier,
    mainAxisSpacing: androidx.compose.ui.unit.Dp = 0.dp,
    crossAxisSpacing: androidx.compose.ui.unit.Dp = 0.dp,
    content: @Composable () -> Unit
) {
    androidx.compose.ui.layout.Layout(
        content = content,
        modifier = modifier
    ) { measurables, constraints ->
        val placeholders = measurables.map { it.measure(constraints.copy(minWidth = 0, minHeight = 0)) }
        val rows = mutableListOf<MutableList<androidx.compose.ui.layout.Placeable>>()
        var currentRow = mutableListOf<androidx.compose.ui.layout.Placeable>()
        var currentRowWidth = 0

        placeholders.forEach { placeable ->
            if (currentRowWidth + placeable.width + mainAxisSpacing.roundToPx() > constraints.maxWidth && currentRow.isNotEmpty()) {
                rows.add(currentRow)
                currentRow = mutableListOf()
                currentRowWidth = 0
            }
            currentRow.add(placeable)
            currentRowWidth += placeable.width + mainAxisSpacing.roundToPx()
        }
        if (currentRow.isNotEmpty()) rows.add(currentRow)

        val height = (rows.sumOf { it.maxOf { p -> p.height } } + (rows.size - 1).coerceAtLeast(0) * crossAxisSpacing.roundToPx()).coerceAtMost(constraints.maxHeight)
        layout(constraints.maxWidth, height) {
            var y = 0
            rows.forEach { row ->
                var x = 0
                val rowHeight = row.maxOf { it.height }
                row.forEach { placeable ->
                    placeable.placeRelative(x, y)
                    x += placeable.width + mainAxisSpacing.roundToPx()
                }
                y += rowHeight + crossAxisSpacing.roundToPx()
            }
        }
    }
}
