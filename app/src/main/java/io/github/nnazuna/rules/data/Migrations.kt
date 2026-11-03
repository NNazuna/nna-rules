package io.github.nnazuna.rules.data

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import io.github.nnazuna.rules.core.codec.RuleCodec
import io.github.nnazuna.rules.core.legacy.LegacyRuleMapper
import io.github.nnazuna.rules.core.legacy.LegacyRuleSnapshot
import io.github.nnazuna.rules.core.plugin.RulePlugins

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

val MIGRATION_2_3 = object : Migration(2, 3) {
    override fun migrate(db: SupportSQLiteDatabase) {
        val codec = RuleCodec(RulePlugins.registry)
        db.execSQL(CREATE_RULES_NEW)
        db.query(SELECT_LEGACY_RULES).use { cursor ->
            val idIndex = cursor.getColumnIndexOrThrow("id")
            val packageIndex = cursor.getColumnIndexOrThrow("package_name")
            val labelIndex = cursor.getColumnIndexOrThrow("app_label")
            val keywordIndex = cursor.getColumnIndexOrThrow("keyword")
            val actionIndex = cursor.getColumnIndexOrThrow("action")
            val clipboardModeIndex = cursor.getColumnIndexOrThrow("clipboard_mode")
            val extractionModeIndex = cursor.getColumnIndexOrThrow("extraction_mode")
            val customRegexIndex = cursor.getColumnIndexOrThrow("custom_regex")
            val clearSecondsIndex = cursor.getColumnIndexOrThrow("clear_clipboard_seconds")
            val enabledIndex = cursor.getColumnIndexOrThrow("enabled")
            val createdAtIndex = cursor.getColumnIndexOrThrow("created_at")

            while (cursor.moveToNext()) {
                val snapshot = LegacyRuleSnapshot(
                    id = cursor.getLong(idIndex),
                    packageName = cursor.getString(packageIndex),
                    appLabel = cursor.getString(labelIndex),
                    keyword = cursor.getStringOrNull(keywordIndex),
                    action = cursor.getString(actionIndex),
                    clipboardMode = cursor.getString(clipboardModeIndex),
                    extractionMode = cursor.getString(extractionModeIndex),
                    customRegex = cursor.getStringOrNull(customRegexIndex),
                    clearClipboardSeconds = cursor.getInt(clearSecondsIndex),
                    enabled = cursor.getInt(enabledIndex) != 0,
                    createdAt = cursor.getLong(createdAtIndex),
                )
                val entity = LegacyRuleMapper.toDefinition(snapshot).toEntity(codec)
                db.execSQL(
                    INSERT_RULE,
                    arrayOf(
                        entity.id,
                        entity.appLabel,
                        if (entity.enabled) 1 else 0,
                        entity.createdAt,
                        entity.triggerType,
                        entity.triggerPayload,
                        entity.conditionsPayload,
                        entity.actionsPayload,
                    ),
                )
            }
        }
        db.execSQL("DROP TABLE `rules`")
        db.execSQL("ALTER TABLE `rules_new` RENAME TO `rules`")
    }
}

val MIGRATION_3_4 = object : Migration(3, 4) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            "CREATE TABLE IF NOT EXISTS `notification_queue` (" +
                "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                "`rule_id` INTEGER NOT NULL, " +
                "`package_name` TEXT NOT NULL, " +
                "`app_label` TEXT NOT NULL, " +
                "`text` TEXT NOT NULL, " +
                "`queued_at` INTEGER NOT NULL" +
                ")",
        )
    }
}

private fun android.database.Cursor.getStringOrNull(index: Int): String? =
    if (isNull(index)) null else getString(index)

private const val CREATE_RULES_NEW =
    "CREATE TABLE IF NOT EXISTS `rules_new` (" +
        "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
        "`app_label` TEXT NOT NULL, " +
        "`enabled` INTEGER NOT NULL, " +
        "`created_at` INTEGER NOT NULL, " +
        "`trigger_type` TEXT NOT NULL, " +
        "`trigger_payload` TEXT NOT NULL, " +
        "`conditions_payload` TEXT NOT NULL, " +
        "`actions_payload` TEXT NOT NULL" +
        ")"

private const val SELECT_LEGACY_RULES =
    "SELECT id, package_name, app_label, keyword, action, clipboard_mode, " +
        "extraction_mode, custom_regex, clear_clipboard_seconds, enabled, created_at " +
        "FROM `rules`"

private const val INSERT_RULE =
    "INSERT INTO `rules_new` (`id`, `app_label`, `enabled`, `created_at`, " +
        "`trigger_type`, `trigger_payload`, `conditions_payload`, `actions_payload`) " +
        "VALUES (?, ?, ?, ?, ?, ?, ?, ?)"
