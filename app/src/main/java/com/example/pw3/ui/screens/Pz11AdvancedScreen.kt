package com.example.pw3.ui.screens

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.pw3.pz11.AuthViewModel
import com.example.pw3.pz11.AuthViewModelFactory

@Composable
fun Pz11AdvancedScreen() {
    val context = LocalContext.current
    val authViewModel: AuthViewModel = viewModel(factory = AuthViewModelFactory(context))

    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                text = "ПЗ №11: JWT Автентифікація, DataStore та Ролі (RBAC)",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            authViewModel.statusMessage?.let {
                Text(it, color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.bodySmall)
            }
        }

        if (authViewModel.token == null) {
            // Екран входу для неавторизованого користувача
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text("Авторизація в системі ЕЗС", style = MaterialTheme.typography.titleMedium)

                        // Кнопки швидкого автозаповнення тестових облікових записів для зручного захисту
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            AssistChip(
                                onClick = { username = "admin"; password = "admin123" },
                                label = { Text("Тест: ADMIN") }
                            )
                            AssistChip(
                                onClick = { username = "driver"; password = "driver123" },
                                label = { Text("Тест: USER (Водій)") }
                            )
                        }

                        OutlinedTextField(
                            value = username,
                            onValueChange = { username = it },
                            label = { Text("Логін (admin або driver)") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = password,
                            onValueChange = { password = it },
                            label = { Text("Пароль (admin123 або driver123)") },
                            singleLine = true,
                            visualTransformation = PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                            modifier = Modifier.fillMaxWidth()
                        )
                        Button(
                            onClick = {
                                authViewModel.login(username, password) { err ->
                                    Toast.makeText(context, err, Toast.LENGTH_SHORT).show()
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Увійти та отримати JWT")
                        }
                    }
                }
            }
        } else {
            // Інтерфейс для авторизованого користувача з розмежуванням прав доступу за роллю з токена
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (authViewModel.role == "ADMIN") Icons.Default.AdminPanelSettings else Icons.Default.Person,
                                contentDescription = null
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = "Користувач: ${authViewModel.currentUsername} | Роль у JWT: ${authViewModel.role}",
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = "JWT: ${authViewModel.token?.take(36)}...",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }

            item {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = {
                            authViewModel.loadProtectedData { err ->
                                Toast.makeText(context, err, Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Профіль водія")
                    }
                    OutlinedButton(
                        onClick = { authViewModel.simulateTokenExpiration() },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Прострочити JWT")
                    }
                }
            }

            // Відображення захищених даних профілю (доступно обом ролям при валідному токені)
            authViewModel.driverProfile?.let { profile ->
                item {
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text("Захищений кабінет водія ЕЗС", fontWeight = FontWeight.Bold)
                            Text("RFID-картка: ${profile.rfidCardNumber}")
                            Text("Баланс рахунку: ${profile.balanceUah} грн")
                            Text("Тариф: ${profile.activeTariff}")
                        }
                    }
                }
            }

            // Адаптація UI: панель адміністратора відображається ТІЛЬКИ якщо роль у токені == ADMIN
            if (authViewModel.role == "ADMIN") {
                item {
                    Button(
                        onClick = {
                            authViewModel.loadAdminData { err ->
                                Toast.makeText(context, err, Toast.LENGTH_SHORT).show()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Панель адміністратора: Завантажити станції ЕЗС")
                    }
                }
            }

            authViewModel.adminStations?.let { stations ->
                items(stations, key = { it.id }) { st ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text("${st.stationCode} — Статус: ${st.status}", fontWeight = FontWeight.Bold)
                            Text("Потужність: ${st.totalCapacityKw} кВт • Uptime: ${st.uptimePercent}% • Авто: ${st.activeVehicles}")
                        }
                    }
                }
            }

            item {
                OutlinedButton(
                    onClick = { authViewModel.logout() },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Вийти з облікового запису (Очистити DataStore)")
                }
            }
        }
    }
}