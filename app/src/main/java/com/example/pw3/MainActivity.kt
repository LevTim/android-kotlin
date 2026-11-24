package com.example.pw3

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.example.pw3.model.PracticalTask
import com.example.pw3.ui.screens.Pz9AdvancedScreen
import com.example.pw3.ui.screens.Pz10AdvancedScreen
import com.example.pw3.ui.screens.Pz11AdvancedScreen
import com.example.pw3.ui.screens.Pz12AdvancedScreen
import com.example.pw3.ui.screens.TaskModeSelector
import com.example.pw3.ui.theme.PW3Theme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            PW3Theme {
                var selectedTask by remember { mutableStateOf(PracticalTask.PZ9) }

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Column(modifier = Modifier.fillMaxSize()) {
                        TaskModeSelector(
                            selectedTask = selectedTask,
                            onSelectTask = { selectedTask = it }
                        )

                        when (selectedTask) {
                            PracticalTask.PZ9 -> Pz9AdvancedScreen()
                            PracticalTask.PZ10 -> Pz10AdvancedScreen()
                            PracticalTask.PZ11 -> Pz11AdvancedScreen()
                            PracticalTask.PZ12 -> Pz12AdvancedScreen()
                        }
                    }
                }
            }
        }
    }
}