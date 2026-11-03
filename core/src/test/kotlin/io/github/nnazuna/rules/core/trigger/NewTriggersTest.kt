package io.github.nnazuna.rules.core.trigger

import io.github.nnazuna.rules.core.event.PowerEvent
import io.github.nnazuna.rules.core.event.TimeEvent
import io.github.nnazuna.rules.core.event.WifiEvent
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Calendar
import java.util.TimeZone

class NewTriggersTest {

    @Test
    fun timeTrigger_matchesCorrectTime() {
        val trigger = TimeTrigger(14, 30)

        val cal = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
            set(Calendar.HOUR_OF_DAY, 14)
            set(Calendar.MINUTE, 30)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        val matchingEvent = TimeEvent(14, 30, cal.timeInMillis)
        val nonMatchingEvent = TimeEvent(14, 31, cal.timeInMillis + 60000)

        assertTrue(trigger.matches(matchingEvent))
        assertFalse(trigger.matches(nonMatchingEvent))
    }

    @Test
    fun wifiTrigger_matchesCorrectSsid() {
        val trigger = WifiTrigger("HomeNetwork")

        val matchingEvent = WifiEvent("HomeNetwork", true)
        val nonMatchingEvent = WifiEvent("OfficeNetwork", true)
        val disconnectedEvent = WifiEvent("HomeNetwork", false)

        assertTrue(trigger.matches(matchingEvent))
        assertFalse(trigger.matches(nonMatchingEvent))
        assertTrue(trigger.matches(disconnectedEvent)) // WifiTrigger checks SSID, not connection status unless specified
    }

    @Test
    fun powerTrigger_matchesCorrectState() {
        val trigger = PowerTrigger(true)

        val matchingEvent = PowerEvent(true)
        val nonMatchingEvent = PowerEvent(false)

        assertTrue(trigger.matches(matchingEvent))
        assertFalse(trigger.matches(nonMatchingEvent))
    }
}
