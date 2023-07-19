package dev.ridill.mym.application

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import dagger.hilt.android.AndroidEntryPoint
import dev.ridill.mym.ui.theme.MYMTheme

@AndroidEntryPoint
class MYMActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MYMTheme {

            }
        }
    }
}