package io.github.nnazuna.rules.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import io.github.nnazuna.rules.ui.edit.RuleEditScreen
import io.github.nnazuna.rules.ui.onboarding.OnboardingScreen
import io.github.nnazuna.rules.ui.rules.RuleListScreen
import io.github.nnazuna.rules.util.OnboardingPrefs

private object Routes {
    const val Onboarding = "onboarding"
    const val Rules = "rules"
    const val RuleEdit = "rule_edit?ruleId={ruleId}"
}

private const val RULE_ID_ARG = "ruleId"

private fun ruleEditRoute(ruleId: Long) = "rule_edit?ruleId=$ruleId"

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
            RuleListScreen(
                onCreateRule = { navController.navigate(ruleEditRoute(-1L)) },
                onEditRule = { ruleId -> navController.navigate(ruleEditRoute(ruleId)) },
            )
        }
        composable(
            route = Routes.RuleEdit,
            arguments = listOf(
                navArgument(RULE_ID_ARG) {
                    type = NavType.LongType
                    defaultValue = -1L
                },
            ),
        ) { backStackEntry ->
            val ruleId = backStackEntry.arguments?.getLong(RULE_ID_ARG) ?: -1L
            RuleEditScreen(
                ruleId = ruleId,
                onBack = { navController.popBackStack() },
            )
        }
    }
}
