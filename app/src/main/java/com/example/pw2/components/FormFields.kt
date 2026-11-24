package com.example.pw2.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.TextFieldValue

@Composable
fun StationNameField(value: TextFieldValue, isError: Boolean, onValueChange: (TextFieldValue) -> Unit) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text("Назва зарядної станції (EV Hub)") },
        isError = isError,
        modifier = Modifier.fillMaxWidth()
    )
    if (isError) {
        Text("Назва станції не може бути порожньою", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
    }
}

@Composable
fun EmailField(value: TextFieldValue, isError: Boolean, onValueChange: (TextFieldValue) -> Unit) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text("Email оператора локації") },
        isError = isError,
        modifier = Modifier.fillMaxWidth()
    )
    if (isError) {
        Text("Введіть коректний email (name@domain.com)", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
    }
}

@Composable
fun PasswordField(value: TextFieldValue, isError: Boolean, onValueChange: (TextFieldValue) -> Unit) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text("Пароль доступу") },
        isError = isError,
        visualTransformation = PasswordVisualTransformation(),
        modifier = Modifier.fillMaxWidth()
    )
    if (isError) {
        Text("Мін. 8 символів, містить велику літеру та цифру", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
    }
}

@Composable
fun PowerField(value: TextFieldValue, isError: Boolean, onValueChange: (TextFieldValue) -> Unit) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text("Максимальна потужність станції (кВт)") },
        isError = isError,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        modifier = Modifier.fillMaxWidth()
    )
    if (isError) {
        Text("Потужність повинна бути більшою за 0 кВт", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
    }
}

@Composable
fun SubmitButton(onSubmit: () -> Unit) {
    Button(onClick = onSubmit, modifier = Modifier.fillMaxWidth()) {
        Text("Зареєструвати зарядну станцію")
    }
}

@Composable
fun LoadingIndicator() {
    CircularProgressIndicator()
}