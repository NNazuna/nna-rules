package io.github.nnazuna.rules.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import io.github.nnazuna.rules.extraction.ClipboardMode
import io.github.nnazuna.rules.extraction.ExtractionMode

enum class ActionType {
    NOTIFY,
    SOUND,
    CLIPBOARD,
}

@Entity(tableName = "rules")
data class Rule(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "package_name") val packageName: String,
    @ColumnInfo(name = "app_label") val appLabel: String,
    val keyword: String? = null,
    val action: ActionType,
    @ColumnInfo(name = "clipboard_mode", defaultValue = "'FULL'")
    val clipboardMode: ClipboardMode = ClipboardMode.FULL,
    @ColumnInfo(name = "extraction_mode", defaultValue = "'OTP'")
    val extractionMode: ExtractionMode = ExtractionMode.OTP,
    @ColumnInfo(name = "custom_regex") val customRegex: String? = null,
    @ColumnInfo(name = "clear_clipboard_seconds", defaultValue = "0")
    val clearClipboardSeconds: Int = 0,
    val enabled: Boolean = true,
    @ColumnInfo(name = "created_at") val createdAt: Long = System.currentTimeMillis(),
)
