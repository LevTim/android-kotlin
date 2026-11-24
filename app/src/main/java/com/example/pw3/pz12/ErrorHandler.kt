package com.example.pw3.pz12

import android.content.Context
import android.util.Log
import retrofit2.HttpException
import java.io.File
import java.io.FileOutputStream
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Архітектурне рішення (Централізований обробник помилок):
 * Відокремлює логіку класифікації винятків та журналювання від UI-шару.
 * Записує всі помилки у файл ev_error_log.txt та системний журнал Logcat,
 * повертаючи користувачу зрозуміле локалізоване повідомлення.
 */
object ErrorHandler {

    private const val LOG_FILE_NAME = "ev_error_log.txt"
    private const val TAG = "EvErrorHandler"

    fun logErrorToFile(context: Context, message: String) {
        try {
            val file = File(context.filesDir, LOG_FILE_NAME)
            val timeStr = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
            FileOutputStream(file, true).bufferedWriter().use { writer ->
                writer.write("[$timeStr] $message\n")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Не вдалося записати лог у файл: ${e.localizedMessage}")
        }
    }

    fun readErrorLog(context: Context): String {
        val file = File(context.filesDir, LOG_FILE_NAME)
        return if (file.exists()) file.readText().ifBlank { "Журнал помилок порожній." }
        else "Журнал помилок ще не створено."
    }

    fun clearErrorLog(context: Context) {
        val file = File(context.filesDir, LOG_FILE_NAME)
        if (file.exists()) file.writeText("")
    }

    fun handleException(context: Context, e: Throwable): String {
        val userMessage = when (e) {
            is HttpException -> {
                when (e.code()) {
                    400 -> "Помилка 400: Некоректний запит до контролера ЕЗС"
                    401 -> "Помилка 401: Необхідна авторизація оператора"
                    403 -> "Помилка 403: Доступ до телеметрії заборонено"
                    404 -> "Помилка 404: Зарядну станцію або ресурс не знайдено"
                    408 -> "Помилка 408: Час очікування відповіді станції вичерпано"
                    500 -> "Помилка 500: Внутрішня помилка сервера ЕЗС, спробуйте пізніше"
                    503 -> "Помилка 503: Сервер ЕЗС тимчасово перевантажений"
                    else -> "HTTP помилка сервера: код ${e.code()}"
                }
            }
            is SocketTimeoutException -> "Таймаут з'єднання: зарядна станція не відповідає"
            is UnknownHostException -> "Сервер недоступний: перевірте адресу або мережу"
            else -> "Мережевий збій: ${e.localizedMessage ?: "Невідома помилка"}"
        }

        Log.e(TAG, userMessage, e)
        logErrorToFile(context, "$userMessage (${e.javaClass.simpleName})")
        return userMessage
    }
}