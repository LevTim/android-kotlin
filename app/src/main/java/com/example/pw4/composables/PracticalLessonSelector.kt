package com.example.pw4.composables

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.pw4.PracticalLesson

/**
 * Верхня панель навігації для перемикання між ПЗ №13, №14, №15 та №16.
 */
@Composable
fun PracticalLessonSelector(
    selectedLesson: PracticalLesson,
    onLessonSelected: (PracticalLesson) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Варіант 9: Зарядна станція для електромобілів (Просунутий рівень)",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(6.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            PracticalLesson.entries.forEach { lesson ->
                val isSelected = lesson == selectedLesson
                Button(
                    onClick = { onLessonSelected(lesson) },
                    modifier = Modifier.weight(1f),
                    enabled = !isSelected,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Text(
                        text = lesson.shortTitle,
                        style = MaterialTheme.typography.labelMedium
                    )
                }
            }
        }
    }
}