package com.example.pw4.composables

import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.pw4.viewModels.ChargingStationViewModel

/**
 * ПЗ №15 (Просунутий рівень):
 * - Реактивне управління списком через ChargingStationViewModel;
 * - Пошук за назвою/адресою;
 * - Багаторівнева фільтрація (тип конектора + статус роботи);
 * - Сортування за різними параметрами та детальний перегляд обраної станції.
 */
@Composable
fun Practical15AdvancedScreen(
    viewModel: ChargingStationViewModel = viewModel()
) {
    val stations = viewModel.filteredStations
    val selectedStation = viewModel.selectedStation

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 14.dp, vertical = 8.dp)
    ) {
        Text(
            text = "ПЗ №15: Фільтрація, сортування та пошук ЕЗС",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(6.dp))

        // Поле пошуку за назвою або адресою зарядної станції
        OutlinedTextField(
            value = viewModel.searchQuery,
            onValueChange = { viewModel.updateSearchQuery(it) },
            label = { Text("Пошук за назвою або адресою") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(6.dp))

        // Рівень 1 фільтрації: тип зарядного конектора
        Text(
            text = "Фільтр 1 — Тип конектора:",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            listOf("Всі", "CCS2", "Type 2", "CHAdeMO").forEach { connector ->
                FilterChip(
                    selected = viewModel.selectedConnector == connector,
                    onClick = { viewModel.updateConnectorFilter(connector) },
                    label = { Text(connector) }
                )
            }
        }

        // Рівень 2 фільтрації: поточний статус зарядної станції
        Text(
            text = "Фільтр 2 — Статус роботи:",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            listOf("Всі", "Вільна", "Заряджає", "На ремонті").forEach { status ->
                FilterChip(
                    selected = viewModel.selectedStatus == status,
                    onClick = { viewModel.updateStatusFilter(status) },
                    label = { Text(status) }
                )
            }
        }

        // Багатокритеріальне сортування
        Text(
            text = "Сортування:",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            listOf("Потужність", "Назва", "Тип", "Дата", "Тариф").forEach { criteria ->
                FilterChip(
                    selected = viewModel.sortBy == criteria,
                    onClick = { viewModel.updateSortBy(criteria) },
                    label = { Text(criteria) }
                )
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Відображення розширеної панелі для вибраної користувачем станції
        selectedStation?.let { station ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = "Вибрана станція: ${station.name}",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Роз'єм: ${station.connectorType} (${station.currentType}) • Потужність: ${station.powerKw} кВт",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Text(
                        text = "Тариф: ${station.pricePerKwh} грн/кВт·год • Портів вільно: ${station.freePorts} з ${station.totalPorts}",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Text(
                        text = "Адреса: ${station.address} • Оновлено: ${station.lastUpdate}",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }

        // Динамічний список станцій або повідомлення про відсутність результатів
        if (stations.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "За заданими критеріями зарядних станцій не знайдено",
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.height(8.dp))
                Button(onClick = { viewModel.resetFilters() }) {
                    Text("Скинути фільтри")
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                items(stations, key = { it.id }) { station ->
                    val isSelected = station.id == selectedStation?.id
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { viewModel.selectStation(station) },
                        elevation = CardDefaults.cardElevation(if (isSelected) 6.dp else 2.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected) {
                                MaterialTheme.colorScheme.secondaryContainer
                            } else {
                                MaterialTheme.colorScheme.surfaceVariant
                            }
                        )
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = station.name,
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = station.status,
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = when (station.status) {
                                        "Вільна" -> Color(0xFF2E7D32)
                                        "Заряджає" -> Color(0xFFEF6C00)
                                        else -> Color(0xFFC62828)
                                    }
                                )
                            }
                            Text(
                                text = "Тип: ${station.connectorType} (${station.currentType}) | Потужність: ${station.powerKw} кВт",
                                style = MaterialTheme.typography.bodySmall
                            )
                            Text(
                                text = "Тариф: ${station.pricePerKwh} грн/кВт·год | Вільні порти: ${station.freePorts}/${station.totalPorts} | Оновлено: ${station.lastUpdate}",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
            }
        }
    }
}