package com.example.pw3.pz12

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import retrofit2.HttpException
import com.example.pw3.mock.MockEvBackendInterceptor

/**
 * ViewModel для ПЗ №12 (Просунутий рівень):
 * Реалізує перевірку наявності Інтернету, retry-логіку з експоненційною затримкою
 * для тимчасових помилок (500, 503, таймаути) та інтеграцію з файловим журналом помилок.
 */
class FaultTolerantViewModel(private val context: Context) : ViewModel() {

    var telemetry by mutableStateOf<ChargerTelemetryDto?>(null)
        private set

    var errorMessage by mutableStateOf<String?>(null)
        private set

    var retryProgressInfo by mutableStateOf<String?>(null)
        private set

    var isLoading by mutableStateOf(false)
        private set

    var logFileContent by mutableStateOf(ErrorHandler.readErrorLog(context))
        private set

    private val maxRetries = 3
    private val baseRetryDelayMs = 1000L

    /**
     * Виконує запит телеметрії з автоматичними повторними спробами при тимчасових збоях.
     * @param useFlakyEndpoint якщо true, звертається до нестабільного ендпоінту (2 збої -> успіх)
     */
    fun fetchTelemetryWithRetry(useFlakyEndpoint: Boolean = false) {
        if (!isInternetAvailable(context)) {
            val noNetMsg = "Немає підключення до Інтернету"
            ErrorHandler.logErrorToFile(context, noNetMsg)
            refreshLogContent()
            errorMessage = noNetMsg
            return
        }

        if (useFlakyEndpoint) {
            MockEvBackendInterceptor.resetFlakyCounter()
        }

        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            retryProgressInfo = null

            var attempt = 0
            while (attempt < maxRetries) {
                try {
                    val result = if (useFlakyEndpoint) {
                        FaultTolerantRetrofitClient.api.getFlakyTelemetry()
                    } else {
                        FaultTolerantRetrofitClient.api.getLiveTelemetry()
                    }
                    telemetry = result
                    retryProgressInfo = if (attempt > 0) {
                        "Успішно відновлено зв'язок на спробі №${attempt + 1}!"
                    } else {
                        "Дані отримано з першої спроби"
                    }
                    isLoading = false
                    return@launch
                } catch (e: Throwable) {
                    attempt++
                    val handledMsg = ErrorHandler.handleException(context, e)
                    refreshLogContent()

                    // Повторні спроби виконуємо лише для тимчасових збоїв (500, 503 або мережеві винятки)
                    val isTransient = (e is HttpException && (e.code() == 500 || e.code() == 503)) ||
                            (e !is HttpException)

                    if (isTransient && attempt < maxRetries) {
                        val waitMs = baseRetryDelayMs * attempt
                        retryProgressInfo = "Спроба $attempt/$maxRetries невдала ($handledMsg). Повтор через ${waitMs}мс..."
                        delay(waitMs)
                    } else {
                        errorMessage = handledMsg
                        retryProgressInfo = "Вичерпано спроби ($attempt/$maxRetries)"
                        break
                    }
                }
            }
            isLoading = false
        }
    }

    /**
     * Симулює конкретний код HTTP-помилки (наприклад, 404 або 500) для перевірки ErrorHandler.
     */
    fun simulateHttpError(code: Int) {
        if (!isInternetAvailable(context)) {
            errorMessage = "Немає підключення до Інтернету"
            return
        }

        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            retryProgressInfo = null
            try {
                FaultTolerantRetrofitClient.api.simulateError(code)
            } catch (e: Throwable) {
                errorMessage = ErrorHandler.handleException(context, e)
                refreshLogContent()
            } finally {
                isLoading = false
            }
        }
    }

    fun clearLogs() {
        ErrorHandler.clearErrorLog(context)
        refreshLogContent()
    }

    fun refreshLogContent() {
        logFileContent = ErrorHandler.readErrorLog(context)
    }
}