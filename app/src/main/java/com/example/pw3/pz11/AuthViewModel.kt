package com.example.pw3.pz11

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import retrofit2.HttpException
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import com.example.pw3.mock.MockEvBackendInterceptor

/**
 * ViewModel керування сесією користувача, JWT-токеном та розмежуванням прав доступу (RBAC).
 */
class AuthViewModel(
    private val authDataStore: AuthDataStore,
    private val authApi: AuthApiService
) : ViewModel() {

    var token by mutableStateOf<String?>(null)
        private set

    var role by mutableStateOf<String?>(null)
        private set

    var currentUsername by mutableStateOf<String?>(null)
        private set

    var driverProfile by mutableStateOf<ProtectedDriverProfile?>(null)
        private set

    var adminStations by mutableStateOf<List<AdminStationControlDto>?>(null)
        private set

    var statusMessage by mutableStateOf<String?>(null)
        private set

    init {
        // Підписка на зміни токена у DataStore та декодування ролі з JWT
        viewModelScope.launch {
            authDataStore.tokenFlow.collect { savedToken ->
                token = savedToken
                val claims = JwtDecoder.parseToken(savedToken)
                if (claims != null && !claims.isExpired) {
                    role = claims.role
                    currentUsername = claims.username
                } else if (savedToken != null && claims?.isExpired == true) {
                    // Якщо токен прострочений — інформуємо користувача
                    role = claims.role
                    currentUsername = claims.username
                    statusMessage = "Увага: термін дії токена вичерпано! Потрібен повторний вхід."
                } else {
                    role = null
                    currentUsername = null
                }
            }
        }
    }

    fun login(username: String, password: String, onError: (String) -> Unit) {
        if (username.isBlank() || password.isBlank()) {
            onError("Будь ласка, введіть логін та пароль")
            return
        }
        viewModelScope.launch {
            try {
                val response = authApi.login(LoginRequest(username.trim(), password.trim()))
                authDataStore.saveToken(response.token)
                driverProfile = null
                adminStations = null
                statusMessage = "Успішний вхід як ${response.username} (Роль: ${response.role})"
            } catch (e: HttpException) {
                val msg = if (e.code() == 401) "Помилка 401: Невірний логін або пароль"
                else "HTTP помилка ${e.code()}"
                onError(msg)
            } catch (e: Exception) {
                onError("Помилка входу: ${e.localizedMessage}")
            }
        }
    }

    fun loadProtectedData(onError: (String) -> Unit) {
        // Попередня клієнтська перевірка терміну дії JWT-токена
        val claims = JwtDecoder.parseToken(token)
        if (claims == null || claims.isExpired) {
            val msg = "Токен недійсний або прострочений. Виконайте повторний вхід!"
            statusMessage = msg
            onError(msg)
            return
        }

        viewModelScope.launch {
            try {
                driverProfile = authApi.getProtectedDriverProfile()
                statusMessage = "Захищений профіль водія успішно завантажено"
            } catch (e: HttpException) {
                val msg = when (e.code()) {
                    401 -> "401 Unauthorized: Токен відхилено сервером"
                    403 -> "403 Forbidden: Недостатньо прав доступу"
                    else -> "Помилка сервера: ${e.code()}"
                }
                statusMessage = msg
                onError(msg)
            } catch (e: Exception) {
                onError(e.localizedMessage ?: "Помилка завантаження захищених даних")
            }
        }
    }

    fun loadAdminData(onError: (String) -> Unit) {
        viewModelScope.launch {
            try {
                adminStations = authApi.getAdminStationsControl()
                statusMessage = "Адміністративну телеметрію ЕЗС завантажено"
            } catch (e: HttpException) {
                val msg = when (e.code()) {
                    401 -> "401 Unauthorized: Відсутній або прострочений токен"
                    403 -> "403 Forbidden: Доступ дозволено лише адміністраторам ЕЗС!"
                    else -> "HTTP помилка: ${e.code()}"
                }
                statusMessage = msg
                onError(msg)
            } catch (e: Exception) {
                onError(e.localizedMessage ?: "Помилка завантаження адмін-даних")
            }
        }
    }

    /**
     * Допоміжний метод для демонстрації на захисті: підміняє токен на прострочений,
     * щоб перевірити реакцію захищеного ендпоінту.
     */
    fun simulateTokenExpiration() {
        viewModelScope.launch {
            val expired = JwtDecoder.createExpiredMockToken(
                username = currentUsername ?: "driver",
                role = role ?: "USER"
            )
            authDataStore.saveToken(expired)
        }
    }

    fun logout() {
        viewModelScope.launch {
            authDataStore.clearToken()
            driverProfile = null
            adminStations = null
            statusMessage = "Ви вийшли із системи"
        }
    }
}

class AuthViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AuthViewModel::class.java)) {
            val dataStore = AuthDataStore(context.applicationContext)
            val okHttpClient = OkHttpClient.Builder()
                .addInterceptor(AuthInterceptor(dataStore))
                .addInterceptor(MockEvBackendInterceptor())
                .build()

            val retrofit = Retrofit.Builder()
                .baseUrl("http://10.0.2.2:5156/")
                .client(okHttpClient)
                .addConverterFactory(GsonConverterFactory.create())
                .build()

            val api = retrofit.create(AuthApiService::class.java)
            @Suppress("UNCHECKED_CAST")
            return AuthViewModel(dataStore, api) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}