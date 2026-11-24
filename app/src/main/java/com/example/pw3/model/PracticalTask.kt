package com.example.pw3.model

/**
 * Перелік практичних занять у межах Практичної роботи №3.
 * Дозволяє швидко перемикатися між усіма 4 частинами завдання на просунутому рівні.
 */
enum class PracticalTask(val shortTitle: String, val fullTitle: String) {
    PZ9("ПЗ №9", "ПЗ №9: Repository та Data Source (EV Кешування та Аналітика)"),
    PZ10("ПЗ №10", "ПЗ №10: REST API, Retrofit та Gson (Мережа ЕЗС)"),
    PZ11("ПЗ №11", "ПЗ №11: JWT Автентифікація та Ролі (Admin / Driver)"),
    PZ12("ПЗ №12", "ПЗ №12: Обробка помилок мережі, Retry та Логування")
}