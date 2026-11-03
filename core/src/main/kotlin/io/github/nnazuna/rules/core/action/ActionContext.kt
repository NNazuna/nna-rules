package io.github.nnazuna.rules.core.action

interface ActionContext {
    fun postNotification(appLabel: String, text: String, withSound: Boolean)

    fun copyToClipboard(text: String, clearAfterSeconds: Int)

    fun setVolume(level: Int, streamType: String)

    fun setBrightness(level: Int)

    fun setDndMode(mode: String)

    fun appendToCsv(filePath: String, columns: List<String>, value: String)

    fun queueNotification(ruleId: Long, packageName: String, appLabel: String, text: String)
}
