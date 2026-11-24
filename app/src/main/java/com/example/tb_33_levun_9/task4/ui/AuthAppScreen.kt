package com.example.tb_33_levun_9.task4.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tb_33_levun_9.task4.logic.AuthValidator
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun AuthAppScreen() {
    var isRegisterScreen by remember { mutableStateOf(false) }

    var loginInput by remember { mutableStateOf("") }
    var passwordInput by remember { mutableStateOf("") }

    var regName by remember { mutableStateOf("") }
    var regEmail by remember { mutableStateOf("") }
    var regPass by remember { mutableStateOf("") }
    var regConfirmPass by remember { mutableStateOf("") }

    var errors by remember { mutableStateOf<Map<String, String>>(emptyMap()) }
    var isLoading by remember { mutableStateOf(false) }
    var successMsg by remember { mutableStateOf("") }

    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = if (isRegisterScreen) "Реєстрація у кабінеті EV" else "Вхід до мережі Зарядних Станцій",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(16.dp))

        if (!isRegisterScreen) {
            // ФОРМА ВХОДУ
            OutlinedTextField(
                value = loginInput,
                onValueChange = { loginInput = it },
                label = { Text("Логін / Email") },
                isError = errors.containsKey("login"),
                modifier = Modifier.fillMaxWidth()
            )
            errors["login"]?.let { Text(it, color = MaterialTheme.colorScheme.error, fontSize = 12.sp) }

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = passwordInput,
                onValueChange = { passwordInput = it },
                label = { Text("Пароль") },
                visualTransformation = PasswordVisualTransformation(),
                isError = errors.containsKey("pass"),
                modifier = Modifier.fillMaxWidth()
            )
            errors["pass"]?.let { Text(it, color = MaterialTheme.colorScheme.error, fontSize = 12.sp) }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    val validation = AuthValidator.validateLogin(loginInput, passwordInput)
                    errors = validation.errors
                    if (validation.isValid) {
                        scope.launch {
                            isLoading = true
                            delay(1500) // Імітація запиту на сервер
                            isLoading = false
                            successMsg = "Авторизація успішна! Вітаємо у системі."
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                if (isLoading) CircularProgressIndicator(modifier = Modifier.size(20.dp), color = MaterialTheme.colorScheme.onPrimary)
                else Text("Увійти")
            }

            TextButton(onClick = { isRegisterScreen = true; errors = emptyMap(); successMsg = "" }) {
                Text("Немає акаунту? Зареєструватися")
            }
        } else {
            // ФОРМА РЕЄСТРАЦІЇ
            OutlinedTextField(value = regName, onValueChange = { regName = it }, label = { Text("Ім'я") }, modifier = Modifier.fillMaxWidth())
            errors["name"]?.let { Text(it, color = MaterialTheme.colorScheme.error, fontSize = 12.sp) }

            OutlinedTextField(value = regEmail, onValueChange = { regEmail = it }, label = { Text("Email") }, modifier = Modifier.fillMaxWidth())
            errors["email"]?.let { Text(it, color = MaterialTheme.colorScheme.error, fontSize = 12.sp) }

            OutlinedTextField(value = regPass, onValueChange = { regPass = it }, label = { Text("Пароль") }, visualTransformation = PasswordVisualTransformation(), modifier = Modifier.fillMaxWidth())
            errors["pass"]?.let { Text(it, color = MaterialTheme.colorScheme.error, fontSize = 12.sp) }

            OutlinedTextField(value = regConfirmPass, onValueChange = { regConfirmPass = it }, label = { Text("Підтвердіть пароль") }, visualTransformation = PasswordVisualTransformation(), modifier = Modifier.fillMaxWidth())
            errors["confirmPass"]?.let { Text(it, color = MaterialTheme.colorScheme.error, fontSize = 12.sp) }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    val valRes = AuthValidator.validateRegistration(regName, regEmail, regPass, regConfirmPass)
                    errors = valRes.errors
                    if (valRes.isValid) {
                        scope.launch {
                            isLoading = true
                            delay(1500)
                            isLoading = false
                            successMsg = "Реєстрація успішна! Тепер увійдіть."
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                if (isLoading) CircularProgressIndicator(modifier = Modifier.size(20.dp), color = MaterialTheme.colorScheme.onPrimary)
                else Text("Зареєструватися")
            }

            TextButton(onClick = { isRegisterScreen = false; errors = emptyMap(); successMsg = "" }) {
                Text("Вже є акаунт? Увійти")
            }
        }

        if (successMsg.isNotEmpty()) {
            Spacer(modifier = Modifier.height(12.dp))
            Text(successMsg, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
        }
    }
}