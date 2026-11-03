package io.github.nnazuna.rules.core.event

data class TimeEvent(
    val hour: Int,
    val minute: Int,
    val timestamp: Long = System.currentTimeMillis(),
) : RuleEvent

data class WifiEvent(
    val ssid: String,
    val connected: Boolean,
    val timestamp: Long = System.currentTimeMillis(),
) : RuleEvent

data class PowerEvent(
    val connected: Boolean,
    val timestamp: Long = System.currentTimeMillis(),
) : RuleEvent
