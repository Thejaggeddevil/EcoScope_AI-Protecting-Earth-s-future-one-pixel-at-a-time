package com.himanshu.ecoscope

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.himanshu.ecoscope.ui.theme.EcoScopeTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            EcoScopeTheme {
                // Call LoginScreen here
                LoginScreen(activity = this)
            }
        }
    }
}
