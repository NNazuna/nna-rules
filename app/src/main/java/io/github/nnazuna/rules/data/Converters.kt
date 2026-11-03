package io.github.nnazuna.rules.data

import androidx.room.TypeConverter

class Converters {
    @TypeConverter
    fun fromActionType(action: ActionType): String = action.name

    @TypeConverter
    fun toActionType(value: String): ActionType = ActionType.valueOf(value)
}
