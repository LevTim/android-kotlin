package com.example.pw4

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.example.pw4.composables.Practical13AdvancedScreen
import com.example.pw4.composables.Practical14AdvancedScreen
import com.example.pw4.composables.Practical15AdvancedScreen
import com.example.pw4.composables.Practical16AdvancedScreen
import com.example.pw4.composables.PracticalLessonSelector
import com.example.pw4.ui.theme.PW4Theme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PW4Theme {
                // Зберігаємо обране практичне заняття навіть при зміні орієнтації екрана
                var currentLesson by rememberSaveable {
                    mutableStateOf(PracticalLesson.LESSON_13)
                }

                Surface(
                    modifier = Modifier
                        .fillMaxSize()
                        .safeDrawingPadding(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Column(modifier = Modifier.fillMaxSize()) {
                        // Панель перемикання між ПЗ №13, №14, №15 та №16
                        PracticalLessonSelector(
                            selectedLesson = currentLesson,
                            onLessonSelected = { currentLesson = it }
                        )

                        HorizontalDivider()

                        // Відображення відповідного екрана просунутого рівня
                        when (currentLesson) {
                            PracticalLesson.LESSON_13 -> Practical13AdvancedScreen()
                            PracticalLesson.LESSON_14 -> Practical14AdvancedScreen()
                            PracticalLesson.LESSON_15 -> Practical15AdvancedScreen()
                            PracticalLesson.LESSON_16 -> Practical16AdvancedScreen()
                        }
                    }
                }
            }
        }
    }
}