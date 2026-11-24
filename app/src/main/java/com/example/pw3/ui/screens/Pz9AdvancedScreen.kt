package com.example.pw3.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.pw3.pz9.ChargingViewModel
import java.util.Locale

@Composable
fun Pz9AdvancedScreen(viewModel: ChargingViewModel = viewModel()) {
    val sessions by viewModel.sessions.collectAsState()
    val summary by viewModel.summary.collectAsState()
    val sourceStatus by viewModel.sourceStatus.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var selectedConnector by remember { mutableStateOf("Усі") }
    var sortDesc by remember { mutableStateOf(true) }

    var newStation by remember { mutableStateOf("") }
    var newConnector by remember { mutableStateOf("CCS2") }
    var newPower by remember { mutableStateOf("") }
    var newEnergy by remember { mutableStateOf("") }
    var validationError by remember { mutableStateOf<String?>(null) }

    val connectors = listOf("Усі", "CCS2", "Type 2", "CHAdeMO")

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                text = "ПЗ №9: Repository, Кешування TTL та Бізнес-аналітика ЕЗС",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = sourceStatus,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.secondary
            )
        }

        // Картка агрегованих показників бізнес-логіки з Repository
        item {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "Агрегована аналітика зарядної мережі",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.height(6.dp))
                    Text("Сесій у вибірці: ${summary.totalSessions} (Активних: ${summary.activeSessionsCount})")
                    Text(String.format(Locale.US, "Віддано енергії: %.2f кВт·год", summary.totalEnergyKwh))
                    Text(String.format(Locale.US, "Середня потужність: %.1f кВт", summary.averagePowerKw))
                    Text(
                        String.format(Locale.US, "Розрахунковий дохід: %.2f грн", summary.estimatedRevenueUah),
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // Блок фільтрації та сортування
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = {
                        searchQuery = it
                        viewModel.updateFilter(searchQuery, selectedConnector)
                    },
                    label = { Text("Пошук станції / статусу") },
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
                Spacer(Modifier.width(8.dp))
                FilledTonalButton(onClick = {
                    sortDesc = viewModel.toggleSortOrder()
                }) {
                    Icon(Icons.Default.SwapVert, contentDescription = "Сортування")
                    Spacer(Modifier.width(4.dp))
                    Text(if (sortDesc) "кВт ↓" else "кВт ↑")
                }
            }

            Row(
                modifier = Modifier.padding(top = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                connectors.forEach { conn ->
                    FilterChip(
                        selected = selectedConnector == conn,
                        onClick = {
                            selectedConnector = conn
                            viewModel.updateFilter(searchQuery, selectedConnector)
                        },
                        label = { Text(conn) }
                    )
                }
            }
        }

        // Форма додавання нової сесії заряджання з валідацією
        item {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("Нова сесія заряджання", fontWeight = FontWeight.SemiBold)
                    OutlinedTextField(
                        value = newStation,
                        onValueChange = { newStation = it },
                        label = { Text("Назва станції (напр. SuperCharge #5)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = newPower,
                            onValueChange = { newPower = it },
                            label = { Text("Потужність (кВт)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = newEnergy,
                            onValueChange = { newEnergy = it },
                            label = { Text("Енергія (кВт·год)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                    }
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            listOf("CCS2", "Type 2", "CHAdeMO").forEach { c ->
                                AssistChip(
                                    onClick = { newConnector = c },
                                    label = { Text(if (newConnector == c) "✓ $c" else c) }
                                )
                            }
                        }
                        Button(onClick = {
                            val p = newPower.toDoubleOrNull()
                            val e = newEnergy.toDoubleOrNull()
                            if (newStation.isBlank() || p == null || p <= 0 || e == null || e <= 0) {
                                validationError = "Введіть коректну назву та додатні значення кВт і кВт·год"
                            } else {
                                validationError = null
                                viewModel.addSession(newStation, newConnector, p, e)
                                newStation = ""
                                newPower = ""
                                newEnergy = ""
                            }
                        }) {
                            Text("Додати")
                        }
                    }
                    validationError?.let {
                        Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }

        // Кнопка примусового оновлення з віддаленого джерела (обхід TTL кешу)
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Список сесій (${sessions.size})", fontWeight = FontWeight.Bold)
                OutlinedButton(onClick = { viewModel.loadSessions(forceRefresh = true) }) {
                    Icon(Icons.Default.Refresh, contentDescription = null)
                    Spacer(Modifier.width(4.dp))
                    Text("Оновити з API")
                }
            }
            if (isLoading) LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            errorMessage?.let {
                Text(it, color = MaterialTheme.colorScheme.error)
            }
        }

        // Список сесій заряджання
        items(sessions, key = { it.id }) { session ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Bolt,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(32.dp)
                    )
                    Spacer(Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(session.stationName, fontWeight = FontWeight.Bold)
                        Text("Конектор: ${session.connectorType} • ${session.powerKw} кВт")
                        Text("Передано: ${session.energyDeliveredKwh} кВт·год (${session.timestamp})")
                        TextButton(
                            onClick = { viewModel.toggleStatus(session) },
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Text("Статус: ${session.status} (натисніть для зміни)")
                        }
                    }
                    IconButton(onClick = { viewModel.deleteSession(session.id) }) {
                        Icon(Icons.Default.Delete, contentDescription = "Видалити")
                    }
                }
            }
        }
    }
}