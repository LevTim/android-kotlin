package com.example.pw3.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EvStation
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.pw3.pz10.EvViewModel
import java.util.Locale

@Composable
fun Pz10AdvancedScreen(viewModel: EvViewModel = viewModel()) {
    val stations by viewModel.stations.collectAsState()
    val sessionsMap by viewModel.sessions.collectAsState()
    val statsMap by viewModel.stats.collectAsState()
    val loading by viewModel.loading.collectAsState()
    val error by viewModel.error.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadStations()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "ПЗ №10: REST API, Вкладений JSON, Retrofit + Gson та Кеш",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Spacer(Modifier.height(8.dp))

        if (loading) {
            LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(8.dp))
        }

        error?.let {
            Text(it, color = MaterialTheme.colorScheme.error)
            Spacer(Modifier.height(8.dp))
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(stations, key = { it.id }) { station ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.EvStation,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(32.dp)
                            )
                            Spacer(Modifier.width(8.dp))
                            Column {
                                Text(station.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                Text("Оператор: ${station.operator} • Макс: ${station.maxPowerKw} кВт")
                            }
                        }

                        Spacer(Modifier.height(6.dp))
                        // Вкладений об'єкт LocationInfo з JSON
                        Text(
                            text = "Адреса: ${station.location.address} (${station.location.latitude}, ${station.location.longitude})",
                            style = MaterialTheme.typography.bodySmall
                        )

                        // Вкладений масив портів ConnectorPortDto з JSON
                        Spacer(Modifier.height(6.dp))
                        Text("Зарядні порти:", fontWeight = FontWeight.SemiBold)
                        station.ports.forEach { port ->
                            Text(" • Порт ${port.portCode}: ${port.connectorType} (${port.powerKw} кВт) — ${port.state}")
                        }

                        // Статистика з окремого ендпоінту /stats
                        statsMap[station.id]?.let { st ->
                            Spacer(Modifier.height(8.dp))
                            Surface(
                                color = MaterialTheme.colorScheme.secondaryContainer,
                                shape = MaterialTheme.shapes.small,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Text("Телеметрія станції:", fontWeight = FontWeight.Bold)
                                    Text(String.format(Locale.US, "Середня потужність: %.1f кВт", st.averagePowerKw))
                                    Text(String.format(Locale.US, "Віддано всього: %.1f кВт·год", st.totalEnergyDeliveredKwh))
                                    Text(String.format(Locale.US, "Температура конектора: %.1f °C", st.averageConnectorTempC))
                                    Text(String.format(Locale.US, "Завантаженість станції: %.1f%%", st.utilizationPercent))
                                }
                            }
                        }

                        Spacer(Modifier.height(10.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(
                                onClick = { viewModel.loadSessionsAndStats(station.id) },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Завантажити дані")
                            }
                            OutlinedButton(
                                onClick = { viewModel.generateSessions(station.id, 3) },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("+3 сесії (POST)")
                            }
                        }

                        val stationSessions = sessionsMap[station.id].orEmpty()
                        if (stationSessions.isNotEmpty()) {
                            Spacer(Modifier.height(8.dp))
                            Text("Історія заряджань (${stationSessions.size}):", fontWeight = FontWeight.SemiBold)
                            stationSessions.forEach { rec ->
                                Text(
                                    text = "${rec.timestamp}: ${rec.vehicleModel} — ${rec.energyKwh} кВт·год (${rec.avgPowerKw} кВт, ${rec.connectorTempC}°C)",
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}