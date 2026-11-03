package io.github.nnazuna.rules

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import io.github.nnazuna.rules.ui.NnaRulesNavHost
import io.github.nnazuna.rules.ui.theme.NnaRulesTheme
import io.github.nnazuna.rules.util.OnboardingPrefs

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val startWithOnboarding = !OnboardingPrefs.isSeen(this)
        setContent {
            NnaRulesTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    NnaRulesNavHost(startWithOnboarding = startWithOnboarding)
                }
            }
        }
    }
}
