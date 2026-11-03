package io.github.nnazuna.rules.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import io.github.nnazuna.rules.ui.onboarding.OnboardingScreen
import io.github.nnazuna.rules.ui.rules.RuleListScreen
import io.github.nnazuna.rules.util.OnboardingPrefs

private object Routes {
    const val Onboarding = "onboarding"
    const val Rules = "rules"
}

@Composable
fun NnaRulesNavHost(
    startWithOnboarding: Boolean,
    modifier: Modifier = Modifier,
) {
    val navController = rememberNavController()
    val context = LocalContext.current

    NavHost(
        navController = navController,
        startDestination = if (startWithOnboarding) Routes.Onboarding else Routes.Rules,
        modifier = modifier,
    ) {
        composable(Routes.Onboarding) {
            OnboardingScreen(
                onContinue = {
                    OnboardingPrefs.markSeen(context)
                    navController.navigate(Routes.Rules) {
                        popUpTo(Routes.Onboarding) { inclusive = true }
                    }
                },
            )
        }
        composable(Routes.Rules) {
            RuleListScreen()
        }
    }
}
