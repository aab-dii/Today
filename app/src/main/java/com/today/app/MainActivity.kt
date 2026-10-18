package com.today.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.today.app.ui.TodayApp
import com.today.app.ui.theme.TodayTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            TodayTheme {
                TodayApp()
            }
        }
    }
}
