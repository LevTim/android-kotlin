package com.example.pw3.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.pw3.pz12.FaultTolerantViewModel

@Composable
fun Pz12AdvancedScreen() {
    val context = LocalContext.current
    val viewModel = remember { FaultTolerantViewModel(context.applicationContext) }
    val snackbarHostState = remember { SnackbarHostState() }

    // Реактивний показ Snackbar при виникненні мережевої або HTTP помилки
    LaunchedEffect(viewModel.errorMessage) {
        viewModel.errorMessage?.let { msg ->
            snackbarHostState.showSnackbar(message = msg, duration = SnackbarDuration.Short)
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = "ПЗ №12: Обробка помилок, Retry-логіка та Файловий журнал",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            if (viewModel.isLoading) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            }

            viewModel.retryProgressInfo?.let { info ->
                Text(
                    text = info,
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.SemiBold
                )
            }

            // Кнопка штатного запиту телеметрії з retry
            Button(
                onClick = { viewModel.fetchTelemetryWithRetry(useFlakyEndpoint = false) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Отримати телеметрію ЕЗС (з retry)")
            }

            // Кнопка демонстрації автоматичного відновлення після 2 тимчасових збоїв 503
            FilledTonalButton(
                onClick = { viewModel.fetchTelemetryWithRetry(useFlakyEndpoint = true) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Тест Retry: 2 збої 503 → Успіх на 3-й спробі")
            }

            // Кнопки симуляції різних HTTP-кодів помилок
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(
                    onClick = { viewModel.simulateHttpError(404) },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Помилка 404")
                }
                OutlinedButton(
                    onClick = { viewModel.simulateHttpError(500) },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Помилка 500")
                }
            }

            // Відображення отриманої телеметрії зарядної станції
            viewModel.telemetry?.let { t ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text("Станція: ${t.stationCode}", fontWeight = FontWeight.Bold)
                        Text("Час вимірювання: ${t.timestamp}")
                        Text("Напруга / Струм: ${t.voltageV} В / ${t.currentA} А")
                        Text("Миттєва потужність: ${t.powerKw} кВт")
                        Text("Температура кабелю: ${t.connectorTempC} °C")
                        Text("Статус: ${t.status}", fontWeight = FontWeight.SemiBold)
                    }
                }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

            // Журнал помилок із внутрішнього файлу ev_error_log.txt
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Журнал помилок (ev_error_log.txt):", fontWeight = FontWeight.Bold)
                TextButton(onClick = { viewModel.clearLogs() }) {
                    Text("Очистити лог")
                }
            }

            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = MaterialTheme.shapes.small,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = viewModel.logFileContent,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(10.dp)
                )
            }
        }
    }
}