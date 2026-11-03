package io.github.nnazuna.rules.data

import androidx.room.TypeConverter
import io.github.nnazuna.rules.core.extraction.ClipboardMode
import io.github.nnazuna.rules.core.extraction.ExtractionMode

class Converters {
    @TypeConverter
    fun fromActionType(action: ActionType): String = action.name

    @TypeConverter
    fun toActionType(value: String): ActionType = ActionType.valueOf(value)

    @TypeConverter
    fun fromClipboardMode(mode: ClipboardMode): String = mode.name

    @TypeConverter
    fun toClipboardMode(value: String): ClipboardMode = ClipboardMode.valueOf(value)

    @TypeConverter
    fun fromExtractionMode(mode: ExtractionMode): String = mode.name

    @TypeConverter
    fun toExtractionMode(value: String): ExtractionMode = ExtractionMode.valueOf(value)
}
