package com.example.pw2.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.pw2.components.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun RegistrationFormScreen(navController: NavHostController) {
    var stationName by remember { mutableStateOf(TextFieldValue("")) }
    var operatorEmail by remember { mutableStateOf(TextFieldValue("")) }
    var password by remember { mutableStateOf(TextFieldValue("")) }
    var powerCapacity by remember { mutableStateOf(TextFieldValue("")) }

    var stationNameError by remember { mutableStateOf(false) }
    var emailError by remember { mutableStateOf(false) }
    var passwordError by remember { mutableStateOf(false) }
    var powerError by remember { mutableStateOf(false) }

    var isLoading by remember { mutableStateOf(false) }
    var isSubmitted by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        ScreenTitle("Заняття 6: Реєстрація EV станції")

        StationNameField(stationName, stationNameError) { stationName = it; stationNameError = false }
        Spacer(modifier = Modifier.height(8.dp))

        EmailField(operatorEmail, emailError) { operatorEmail = it; emailError = false }
        Spacer(modifier = Modifier.height(8.dp))

        PasswordField(password, passwordError) { password = it; passwordError = false }
        Spacer(modifier = Modifier.height(8.dp))

        PowerField(powerCapacity, powerError) { powerCapacity = it; powerError = false }
        Spacer(modifier = Modifier.height(16.dp))

        SubmitButton(onSubmit = {
            isSubmitted = false
            var valid = true

            if (stationName.text.isBlank()) { stationNameError = true; valid = false }
            if (!operatorEmail.text.contains("@") || !operatorEmail.text.contains(".")) { emailError = true; valid = false }
            if (password.text.length < 8 || !password.text.any { it.isUpperCase() } || !password.text.any { it.isDigit() }) {
                passwordError = true; valid = false
            }
            val powerValue = powerCapacity.text.toIntOrNull()
            if (powerValue == null || powerValue <= 0) { powerError = true; valid = false }

            if (valid) {
                isLoading = true
                scope.launch {
                    delay(2000)
                    isLoading = false
                    isSubmitted = true
                }
            }
        })

        if (isLoading) {
            Spacer(modifier = Modifier.height(16.dp))
            LoadingIndicator()
        }

        if (isSubmitted) {
            Spacer(modifier = Modifier.height(16.dp))
            Text("Станцію успішно зареєстровано та перевірено!", color = MaterialTheme.colorScheme.primary)
        }

        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = { navController.popBackStack() }) {
            Text("Назад")
        }
    }
}