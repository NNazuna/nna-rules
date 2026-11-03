package io.github.nnazuna.rules.action

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Build
import android.os.Handler
import android.os.Looper

object ClipboardClearScheduler {
    private val handler = Handler(Looper.getMainLooper())
    private var pending: Runnable? = null

    @Synchronized
    fun schedule(context: Context, seconds: Int) {
        cancel()
        if (seconds <= 0) return
        val appContext = context.applicationContext
        val runnable = Runnable {
            synchronized(this) { pending = null }
            clear(appContext)
        }
        pending = runnable
        handler.postDelayed(runnable, seconds.toLong() * 1_000L)
    }

    @Synchronized
    fun cancel() {
        pending?.let(handler::removeCallbacks)
        pending = null
    }

    private fun clear(context: Context) {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
            ?: return
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                clipboard.clearPrimaryClip()
            } else {
                clipboard.setPrimaryClip(ClipData.newPlainText("", ""))
            }
        } catch (_: SecurityException) {
            // Clipboard access was revoked; nothing to do.
        }
    }
}
