package dev.ridill.mym.application

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.view.WindowCompat
import androidx.navigation.compose.rememberNavController
import dagger.hilt.android.AndroidEntryPoint
import dev.ridill.mym.core.ui.navigation.MYMNavHost
import dev.ridill.mym.core.ui.theme.MYMTheme

@AndroidEntryPoint
class MYMActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        setContent {
            MYMTheme {
                val navController = rememberNavController()
                MYMNavHost(navController = navController)
            }
        }
    }
}