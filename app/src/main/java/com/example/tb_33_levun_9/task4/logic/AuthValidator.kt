package com.example.tb_33_levun_9.task4.logic

import com.example.tb_33_levun_9.task4.model.ValidationResult

object AuthValidator {

    fun validateLogin(login: String, pass: String): ValidationResult {
        val errs = mutableMapOf<String, String>()
        if (login.isBlank()) errs["login"] = "Введіть логін або Email"
        if (pass.isBlank()) errs["pass"] = "Введіть пароль"
        return ValidationResult(errs.isEmpty(), errs)
    }

    fun validateRegistration(name: String, email: String, pass: String, confirmPass: String): ValidationResult {
        val errs = mutableMapOf<String, String>()
        if (name.isBlank()) errs["name"] = "Введіть ім'я"
        if (email.isBlank() || !email.contains("@")) errs["email"] = "Некоректний Email"
        if (pass.length < 6) errs["pass"] = "Пароль має бути від 6 символів"
        if (confirmPass != pass) errs["confirmPass"] = "Паролі не збігаються"
        return ValidationResult(errs.isEmpty(), errs)
    }
}