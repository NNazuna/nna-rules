package io.github.nnazuna.rules.util

import android.content.Context

object OnboardingPrefs {
    private const val PREFS_NAME = "nna_rules_prefs"
    private const val KEY_ONBOARDING_SEEN = "onboarding_seen"

    fun isSeen(context: Context): Boolean =
        prefs(context).getBoolean(KEY_ONBOARDING_SEEN, false)

    fun markSeen(context: Context) {
        prefs(context).edit().putBoolean(KEY_ONBOARDING_SEEN, true).apply()
    }

    private fun prefs(context: Context) =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
}
