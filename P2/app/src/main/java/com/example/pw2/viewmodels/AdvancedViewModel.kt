package com.example.pw2.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AdvancedViewModel : ViewModel() {
    private val _chargingPower = MutableStateFlow(0)
    val chargingPower: StateFlow<Int> = _chargingPower.asStateFlow()

    private val _batteryLevel = MutableStateFlow(20)
    val batteryLevel: StateFlow<Int> = _batteryLevel.asStateFlow()

    private val _status = MutableStateFlow("Очікування підключення EV")
    val status: StateFlow<String> = _status.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    fun startChargingSession() {
        viewModelScope.launch {
            _isLoading.value = true
            _status.value = "Ініціалізація порту CCS2..."
            delay(1200)

            val power = (22..150).random()
            _chargingPower.value = power
            _batteryLevel.value = (_batteryLevel.value + 15).coerceAtMost(100)

            _status.value = when {
                _batteryLevel.value >= 100 -> "Зарядка завершена (100%)"
                power > 50 -> "Швидка DC зарядка ($power кВт)"
                else -> "Стандартна AC зарядка ($power кВт)"
            }
            _isLoading.value = false
        }
    }
}