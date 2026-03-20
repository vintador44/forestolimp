package org.example.project.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.CloudQueue
import androidx.compose.material.icons.filled.Category
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class MenuItem(
    val title: String,
    val description: String,
    val icon: ImageVector,
    val color: Color,
    val action: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MenuScreen(
    onMenuItemClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val menuItems = listOf(
        MenuItem(
            title = "Маршруты",
            description = "Просмотр всех маршрутов",
            icon = Icons.Default.Map,
            color = Color(0xFF4CAF50),
            action = "roads"
        ),
        MenuItem(
            title = "Локации",
            description = "Интересные места",
            icon = Icons.Default.Place,
            color = Color(0xFF2196F3),
            action = "locations"
        ),
        MenuItem(
            title = "Погода",
            description = "Прогноз погоды",
            icon = Icons.Default.CloudQueue,
            color = Color(0xFF9C27B0),
            action = "weather"
        ),
        MenuItem(
            title = "Категории",
            description = "Категории маршрутов",
            icon = Icons.Default.Category,
            color = Color(0xFFFF9800),
            action = "categories"
        )
    )
    
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5))
    ) {
        // Header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF2E7D32))
                .padding(24.dp)
        ) {
            Column {
                Text(
                    "Forest Navigation",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    "Исследуйте лесные маршруты",
                    fontSize = 14.sp,
                    color = Color.White.copy(alpha = 0.8f)
                )
            }
        }
        
        // Menu items
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(menuItems) { item ->
                MenuItemCard(item, onMenuItemClick)
            }
        }
    }
}

@Composable
fun MenuItemCard(
    item: MenuItem,
    onMenuItemClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(120.dp)
            .clickable { onMenuItemClick(item.action) },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            Surface(
                modifier = Modifier.size(80.dp),
                color = item.color,
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(
                    imageVector = item.icon,
                    contentDescription = item.title,
                    tint = Color.White,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                )
            }
            

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = item.title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
                Text(
                    text = item.description,
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }
        }
    }
}
