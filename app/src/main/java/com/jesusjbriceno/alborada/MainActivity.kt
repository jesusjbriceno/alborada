package com.jesusjbriceno.alborada

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.jesusjbriceno.alborada.ui.alarm.AlarmListScreen
import com.jesusjbriceno.alborada.ui.theme.AlboradaTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AlboradaTheme {
                AlarmListScreen()
            }
        }
    }
}
