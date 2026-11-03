package io.github.nnazuna.rules.core.action

interface ActionContext {
    fun postNotification(appLabel: String, text: String, withSound: Boolean)

    fun copyToClipboard(text: String, clearAfterSeconds: Int)
}
