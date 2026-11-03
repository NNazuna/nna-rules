package io.github.nnazuna.rules

import android.app.Application
import io.github.nnazuna.rules.data.RuleRepository
import io.github.nnazuna.rules.data.RulesDatabase

class NnaRulesApp : Application() {
    private val database: RulesDatabase by lazy { RulesDatabase.getInstance(this) }
    val ruleRepository: RuleRepository by lazy { RuleRepository(database.ruleDao()) }

    override fun onCreate() {
        super.onCreate()
        io.github.nnazuna.rules.util.ExecutionHistoryStore.init(this)
        io.github.nnazuna.rules.service.ScheduleReceiver.scheduleNextTick(this)
    }
}
