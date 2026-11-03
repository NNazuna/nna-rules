package io.github.nnazuna.rules.core.trigger

import io.github.nnazuna.rules.core.plugin.RulePlugins
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class NewTriggerCodecTest {

    private val registry = RulePlugins.registry

    @Test
    fun timeTriggerCodec_encodeDecodeRoundTrip() {
        val original = TimeTrigger(14, 30)
        val encoded = TimeTrigger.encode(original)
        val decoded = TimeTrigger.decode(encoded)
        assertEquals(original, decoded)
    }

    @Test
    fun wifiTriggerCodec_encodeDecodeRoundTrip() {
        val original = WifiTrigger("HomeNetwork")
        val encoded = WifiTrigger.encode(original)
        val decoded = WifiTrigger.decode(encoded)
        assertEquals(original, decoded)
    }

    @Test
    fun powerTriggerCodec_encodeDecodeRoundTrip() {
        val original = PowerTrigger(true)
        val encoded = PowerTrigger.encode(original)
        val decoded = PowerTrigger.decode(encoded)
        assertEquals(original, decoded)
    }

    @Test
    fun timeTriggerCodec_viaRegistry() {
        val trigger = TimeTrigger(10, 45)
        val plugin = registry.trigger("TIME") as? TimeTrigger.Companion
        assertNotNull(plugin)
        val encoded = plugin!!.encode(trigger)
        val decoded = plugin.decode(encoded)
        assertEquals(trigger, decoded)
    }

    @Test
    fun wifiTriggerCodec_viaRegistry() {
        val trigger = WifiTrigger("CoffeeShop")
        val plugin = registry.trigger("WIFI") as? WifiTrigger.Companion
        assertNotNull(plugin)
        val encoded = plugin!!.encode(trigger)
        val decoded = plugin.decode(encoded)
        assertEquals(trigger, decoded)
    }

    @Test
    fun powerTriggerCodec_viaRegistry() {
        val trigger = PowerTrigger(false)
        val plugin = registry.trigger("POWER") as? PowerTrigger.Companion
        assertNotNull(plugin)
        val encoded = plugin!!.encode(trigger)
        val decoded = plugin.decode(encoded)
        assertEquals(trigger, decoded)
    }

    @Test(expected = IllegalArgumentException::class)
    fun timeTriggerCodec_invalidHour() {
        val payload = buildJsonObject {
            put("hour", 25)
            put("minute", 0)
        }
        TimeTrigger.decode(payload)
    }

    @Test(expected = IllegalArgumentException::class)
    fun timeTriggerCodec_invalidMinute() {
        val payload = buildJsonObject {
            put("hour", 14)
            put("minute", 60)
        }
        TimeTrigger.decode(payload)
    }

    @Test(expected = IllegalArgumentException::class)
    fun wifiTriggerCodec_missingSSID() {
        val payload = buildJsonObject { }
        WifiTrigger.decode(payload)
    }

    @Test(expected = IllegalArgumentException::class)
    fun powerTriggerCodec_missingConnected() {
        val payload = buildJsonObject { }
        PowerTrigger.decode(payload)
    }
}
