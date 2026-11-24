package com.example.pw4.composables

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.pw4.data.ChargingTelemetry
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.retryWhen
import kotlinx.coroutines.flow.shareIn
import java.util.Locale
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.random.Random

/**
 * ПЗ №13 (Просунутий рівень):
 * - Використання "гарячих" потоків (shareIn) для постійної генерації телеметрії ЕЗС;
 * - Трансформація (map, filter) та комбінування потоків (combine);
 * - Безпечне автоматичне відновлення збору даних при помилці (retryWhen).
 */
@Composable
fun Practical13AdvancedScreen() {
    var telemetry by remember {
        mutableStateOf(
            ChargingTelemetry(
                activePortPowerKw = 0f,
                gridReserveKw = 0f,
                totalLoadKw = 0f,
                loadPercent = 0,
                isOverloadRisk = false
            )
        )
    }
    var statusMessage by remember { mutableStateOf("Ініціалізація гарячих потоків...") }
    var isErrorState by remember { mutableStateOf(false) }
    var retryCounter by remember { mutableIntStateOf(0) }

    // Потокобезпечний прапорець для імітації збою датчика при натисканні кнопки
    val simulateFaultTrigger = remember { AtomicBoolean(false) }

    // Виділений CoroutineScope для роботи "гарячих" потоків у фоні
    val hotFlowScope = remember { CoroutineScope(SupervisorJob() + Dispatchers.Default) }

    // Гарячий потік №1: постійна генерація потужності порту CCS2 (оновлюється кожну 1 с)
    val portPowerFlow = remember {
        createHotChargingPortFlow(scope = hotFlowScope)
    }

    // Гарячий потік №2: прогноз доступного резерву потужності мережі (оновлюється кожні 1.5 с)
    val gridReserveFlow = remember {
        createHotGridReserveFlow(scope = hotFlowScope)
    }

    LaunchedEffect(Unit) {
        // Трансформуємо потік потужності порту:
        // 1. onEach перевіряє імітацію збою ПІСЛЯ shareIn, щоб помилка потрапила у retryWhen
        // 2. filter відсіює шуми < 5 кВт
        // 3. map враховує ККД перетворювача 0.96
        val normalizedPortFlow = portPowerFlow
            .onEach {
                if (simulateFaultTrigger.compareAndSet(true, false)) {
                    throw IllegalStateException("Втрата зв'язку по шині CAN/OCPP")
                }
            }
            .filter { rawPower -> rawPower >= 5f }
            .map { validPower -> validPower * 0.96f }

        // Об'єднуємо два гарячі потоки за допомогою оператора combine
        combine(normalizedPortFlow, gridReserveFlow) { portPower, gridReserve ->
            val maxSubstationLimit = 250f
            val totalLoad = portPower + (100f - gridReserve).coerceAtLeast(0f)
            val loadPct = ((totalLoad / maxSubstationLimit) * 100).toInt().coerceIn(0, 100)

            ChargingTelemetry(
                activePortPowerKw = portPower,
                gridReserveKw = gridReserve,
                totalLoadKw = totalLoad,
                loadPercent = loadPct,
                isOverloadRisk = loadPct >= 80
            )
        }
            // Перехоплюємо виняток і автоматично перезапускаємо збір через 1.5 секунди
            .retryWhen { cause, _ ->
                isErrorState = true
                retryCounter += 1
                statusMessage = "Збій контролера (${cause.message}). Повторна спроба №$retryCounter..."
                delay(1500)
                true
            }
            .collect { combinedData ->
                telemetry = combinedData
                isErrorState = false
                statusMessage = "Потоки успішно об'єднані (телеметрія в реальному часі)"
            }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "ПЗ №13: Потокова телеметрія ЕЗС (Flow & Coroutines)",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        // Картка показників обох потоків та комбінованого результату
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(4.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Джерело 1 (Гарячий потік порту CCS2):",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Потужність заряджання: ${String.format(Locale.US, "%.2f", telemetry.activePortPowerKw)} кВт",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "Джерело 2 (Гарячий потік резерву мережі):",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.secondary
                )
                Text(
                    text = "Вільний резерв підстанції: ${String.format(Locale.US, "%.2f", telemetry.gridReserveKw)} кВт",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Результат combine (Сумарне навантаження вузла): " +
                            "${String.format(Locale.US, "%.2f", telemetry.totalLoadKw)} кВт (${telemetry.loadPercent}%)",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )

                LinearProgressIndicator(
                    progress = { telemetry.loadPercent / 100f },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(10.dp),
                    color = if (telemetry.isOverloadRisk) Color(0xFFD32F2F) else MaterialTheme.colorScheme.primary
                )

                if (telemetry.isOverloadRisk) {
                    Text(
                        text = "Увага: високе навантаження на трансформатор (>80%)!",
                        color = Color(0xFFD32F2F),
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // Картка статусу та механізму retryWhen
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = if (isErrorState) Color(0xFFFFEBEE) else Color(0xFFE8F5E9)
            )
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    text = "Статус потоку:",
                    fontWeight = FontWeight.Bold,
                    color = if (isErrorState) Color(0xFFC62828) else Color(0xFF2E7D32)
                )
                Text(
                    text = statusMessage,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Black
                )
                if (retryCounter > 0) {
                    Text(
                        text = "Виконано автоматичних відновлень (retryWhen): $retryCounter",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.DarkGray
                    )
                }
            }
        }

        // Кнопка для демонстрації відмовостійкості retryWhen на захисті
        Row(modifier = Modifier.fillMaxWidth()) {
            Button(
                onClick = { simulateFaultTrigger.set(true) },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE65100))
            ) {
                Text("Зімітувати збій датчика ЕЗС (перевірка retryWhen)")
            }
        }
    }
}

/**
 * Створює "гарячий" потік (SharedFlow через shareIn) для постійної
 * генерації потужності від датчика зарядного порту електромобіля.
 */
private fun createHotChargingPortFlow(scope: CoroutineScope): Flow<Float> = flow {
    while (true) {
        delay(1000) // Дані з датчика порту надходять кожну 1 секунду
        val sensorPowerKw = 35f + Random.nextFloat() * 115f
        emit(sensorPowerKw)
    }
}.shareIn(
    scope = scope,
    started = SharingStarted.WhileSubscribed(stopTimeoutMillis = 5000),
    replay = 1
)

/**
 * Створює "гарячий" потік (SharedFlow через shareIn) для прогнозування
 * вільного резерву потужності розподільчої мережі.
 */
private fun createHotGridReserveFlow(scope: CoroutineScope): Flow<Float> = flow {
    while (true) {
        delay(1500) // Дані резерву мережі оновлюються кожні 1.5 секунди
        val reserveKw = 20f + Random.nextFloat() * 80f
        emit(reserveKw)
    }
}.shareIn(
    scope = scope,
    started = SharingStarted.WhileSubscribed(stopTimeoutMillis = 5000),
    replay = 1
)