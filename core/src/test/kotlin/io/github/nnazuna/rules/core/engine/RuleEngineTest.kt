package io.github.nnazuna.rules.core.engine

import io.github.nnazuna.rules.core.action.Action
import io.github.nnazuna.rules.core.action.NotifyAction
import io.github.nnazuna.rules.core.action.SoundAction
import io.github.nnazuna.rules.core.condition.Condition
import io.github.nnazuna.rules.core.condition.KeywordCondition
import io.github.nnazuna.rules.core.event.NotificationEvent
import io.github.nnazuna.rules.core.model.RuleDefinition
import io.github.nnazuna.rules.core.trigger.NotificationTrigger
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class RuleEngineTest {
    private val engine = RuleEngine()
    private val app = "com.example.app"

    private fun rule(
        id: Long = 1L,
        enabled: Boolean = true,
        packageName: String = app,
        conditions: List<Condition> = emptyList(),
        actions: List<Action> = listOf(NotifyAction),
    ) = RuleDefinition(
        id = id,
        appLabel = "Example",
        enabled = enabled,
        createdAt = 100L,
        trigger = NotificationTrigger(packageName),
        conditions = conditions,
        actions = actions,
    )

    private fun event(
        packageName: String = app,
        title: String = "",
        text: String = "",
    ) = NotificationEvent(
        packageName = packageName,
        appLabel = "Example",
        title = title,
        text = text,
    )

    @Test
    fun matchingRuleFiresWithItsActions() {
        val rules = listOf(rule(actions = listOf(NotifyAction, SoundAction)))

        val result = engine.evaluate(rules, event(text = "hello"))

        assertEquals(1, result.size)
        assertEquals(1L, result.first().rule.id)
        assertEquals(listOf(NotifyAction, SoundAction), result.first().rule.actions)
    }

    @Test
    fun packageMismatchDoesNotFire() {
        val result = engine.evaluate(
            listOf(rule(packageName = "com.other")),
            event(packageName = app),
        )

        assertTrue(result.isEmpty())
    }

    @Test
    fun keywordMatchesTitleAndTextIgnoringCase() {
        val rule = rule(conditions = listOf(KeywordCondition("otp")))
        val event = event(title = "Your OTP", text = "123456")

        assertEquals(1, engine.evaluate(listOf(rule), event).size)
    }

    @Test
    fun keywordMatchesTextBody() {
        val rule = rule(conditions = listOf(KeywordCondition("code")))
        val event = event(title = "Message", text = "Your CODE is 1234")

        assertEquals(1, engine.evaluate(listOf(rule), event).size)
    }

    @Test
    fun blankKeywordConditionMatchesAnything() {
        val rule = rule(conditions = listOf(KeywordCondition("   ")))

        assertEquals(1, engine.evaluate(listOf(rule), event(text = "anything")).size)
    }

    @Test
    fun allConditionsMustMatch() {
        val rule = rule(
            conditions = listOf(
                KeywordCondition("otp"),
                KeywordCondition("bank"),
            ),
        )

        assertTrue(engine.evaluate(listOf(rule), event(text = "otp only")).isEmpty())
        assertEquals(1, engine.evaluate(listOf(rule), event(text = "otp from bank")).size)
    }

    @Test
    fun disabledRuleDoesNotFire() {
        val result = engine.evaluate(listOf(rule(enabled = false)), event(text = "hello"))

        assertTrue(result.isEmpty())
    }

    @Test
    fun returnsAllMatchingRulesInInputOrder() {
        val rules = listOf(
            rule(id = 3L, conditions = listOf(KeywordCondition("otp"))),
            rule(id = 4L, conditions = listOf(KeywordCondition("missing"))),
            rule(id = 5L),
        )

        val result = engine.evaluate(rules, event(text = "otp"))

        assertEquals(listOf(3L, 5L), result.map { it.rule.id })
    }

    @Test
    fun noRulesReturnsEmpty() {
        assertTrue(engine.evaluate(emptyList(), event(text = "hello")).isEmpty())
    }
}
