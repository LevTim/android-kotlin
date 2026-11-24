package com.example.pw4.composables

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.pw4.data.ChargingStation
import com.example.pw4.data.ChargingStationsRepository
import kotlinx.coroutines.launch

/**
 * ПЗ №14 (Просунутий рівень):
 * Динамічний список карток (LazyColumn) із двосторонньою синхронізацією з мапою.
 * - Вибір картки у списку фокусує мапу на відповідному маркері ЕЗС.
 * - Натискання на маркер на мапі підсвічує картку та прокручує LazyColumn до неї.
 */
@Composable
fun Practical14AdvancedScreen() {
    val stations = remember { ChargingStationsRepository.getSampleStations() }
    var selectedStation by remember { mutableStateOf<ChargingStation?>(stations.first()) }
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    // Обчислення меж координат для точного позиціонування маркерів на координатній мапі
    val minLat = remember(stations) { stations.minOf { it.latitude } - 0.01 }
    val maxLat = remember(stations) { stations.maxOf { it.latitude } + 0.01 }
    val minLon = remember(stations) { stations.minOf { it.longitude } - 0.015 }
    val maxLon = remember(stations) { stations.maxOf { it.longitude } + 0.015 }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(12.dp)
    ) {
        Text(
            text = "ПЗ №14: Зарядні станції та інтерактивна мапа",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Натисніть картку у списку або маркер на мапі для синхронізації",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(10.dp))

        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // ЛІВА ПАНЕЛЬ: Динамічний список карток LazyColumn (48% ширини)
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(0.48f)
                    .fillMaxHeight()
            ) {
                itemsIndexed(stations, key = { _, item -> item.id }) { _, station ->
                    val isSelected = station.id == selectedStation?.id
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clickable {
                                // При виборі картки переводимо фокус мапи на відповідний маркер
                                selectedStation = station
                            },
                        elevation = CardDefaults.cardElevation(if (isSelected) 6.dp else 2.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected) {
                                MaterialTheme.colorScheme.primaryContainer
                            } else {
                                MaterialTheme.colorScheme.surfaceVariant
                            }
                        )
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text(
                                text = station.name,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "${station.connectorType} • ${station.powerKw.toInt()} кВт",
                                style = MaterialTheme.typography.bodySmall
                            )
                            Text(
                                text = "Порти: ${station.freePorts}/${station.totalPorts} • ${station.status}",
                                style = MaterialTheme.typography.labelSmall,
                                color = when (station.status) {
                                    "Вільна" -> Color(0xFF2E7D32)
                                    "Заряджає" -> Color(0xFFEF6C00)
                                    else -> Color(0xFFC62828)
                                },
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }

            // ПРАВА ПАНЕЛЬ: Інтерактивна координатна карта-схема з маркерами (52% ширини)
            Column(
                modifier = Modifier
                    .weight(0.52f)
                    .fillMaxHeight()
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFFE3F2FD))
                        .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(12.dp))
                ) {
                    // Малювання координатної сітки та перехрестя фокусу на обраній станції
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val stepX = size.width / 4
                        val stepY = size.height / 5
                        for (i in 1..3) {
                            drawLine(
                                color = Color(0xFFBBDEFB),
                                start = Offset(stepX * i, 0f),
                                end = Offset(stepX * i, size.height),
                                strokeWidth = 2f
                            )
                        }
                        for (j in 1..4) {
                            drawLine(
                                color = Color(0xFFBBDEFB),
                                start = Offset(0f, stepY * j),
                                end = Offset(size.width, stepY * j),
                                strokeWidth = 2f
                            )
                        }

                        // Приціл фокусу камери на вибраній станції
                        selectedStation?.let { target ->
                            val normX = ((target.longitude - minLon) / (maxLon - minLon)).toFloat()
                            val normY = 1f - ((target.latitude - minLat) / (maxLat - minLat)).toFloat()
                            val focusCenter = Offset(normX * size.width, normY * size.height)

                            drawCircle(
                                color = Color(0x331976D2),
                                radius = 58f,
                                center = focusCenter
                            )
                        }
                    }

                    // Розміщення інтерактивних маркерів ЕЗС відповідно до їхніх координат
                    stations.forEachIndexed { index, station ->
                        val isSelected = station.id == selectedStation?.id
                        val scale by animateFloatAsState(
                            targetValue = if (isSelected) 1.25f else 1.0f,
                            label = "markerScale"
                        )

                        val normX = ((station.longitude - minLon) / (maxLon - minLon)).toFloat()
                        val normY = 1f - ((station.latitude - minLat) / (maxLat - minLat)).toFloat()

                        Column(
                            modifier = Modifier
                                .align(Alignment.TopStart)
                                .offset(
                                    x = (normX * 145).dp,
                                    y = (normY * 260).dp
                                )
                                .scale(scale)
                                .clickable {
                                    // При кліку на маркер підсвічуємо його та прокручуємо список до картки
                                    selectedStation = station
                                    coroutineScope.launch {
                                        listState.animateScrollToItem(index)
                                    }
                                },
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (isSelected) Color(0xFFD32F2F) else Color(0xFF1976D2)
                                    )
                                    .border(2.dp, Color.White, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "${station.id}",
                                    color = Color.White,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Text(
                                text = "ЕЗС #${station.id}",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) Color(0xFFB71C1C) else Color.Black
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Інформаційна панель обраного маркера внизу мапи
                selectedStation?.let { station ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer
                        )
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text(
                                text = "Фокус мапи: ${station.name}",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Координати: ${station.latitude}, ${station.longitude}",
                                style = MaterialTheme.typography.bodySmall
                            )
                            Text(
                                text = "Адреса: ${station.address}",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
            }
        }
    }
}