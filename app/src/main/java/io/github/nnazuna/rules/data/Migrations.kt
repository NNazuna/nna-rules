package io.github.nnazuna.rules.data

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            "ALTER TABLE rules ADD COLUMN clipboard_mode TEXT NOT NULL DEFAULT 'FULL'",
        )
        db.execSQL(
            "ALTER TABLE rules ADD COLUMN extraction_mode TEXT NOT NULL DEFAULT 'OTP'",
        )
        db.execSQL("ALTER TABLE rules ADD COLUMN custom_regex TEXT")
        db.execSQL(
            "ALTER TABLE rules ADD COLUMN clear_clipboard_seconds INTEGER NOT NULL DEFAULT 0",
        )
    }
}
