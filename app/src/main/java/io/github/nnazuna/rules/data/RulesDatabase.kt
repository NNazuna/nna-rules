package io.github.nnazuna.rules.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(entities = [Rule::class], version = 1, exportSchema = false)
@TypeConverters(Converters::class)
abstract class RulesDatabase : RoomDatabase() {
    abstract fun ruleDao(): RuleDao

    companion object {
        @Volatile
        private var instance: RulesDatabase? = null

        fun getInstance(context: Context): RulesDatabase =
            instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    RulesDatabase::class.java,
                    "nna_rules.db",
                ).build().also { instance = it }
            }
    }
}
