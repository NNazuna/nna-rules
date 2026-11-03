package io.github.nnazuna.rules.core.action

import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class SituationActionsTest {

    @Test
    fun volumeAction_encodeDecode() {
        val original = VolumeAction(level = 80, streamType = "RING")
        val encoded = VolumeAction.encode(original)
        val decoded = VolumeAction.decode(encoded)
        assertEquals(original, decoded)
    }

    @Test
    fun volumeAction_defaultStreamType() {
        val payload = buildJsonObject { put("level", 50) }
        val action = VolumeAction.decode(payload) as VolumeAction
        assertEquals("RING", action.streamType)
    }

    @Test
    fun brightnessAction_encodeDecode() {
        val original = BrightnessAction(level = 200)
        val encoded = BrightnessAction.encode(original)
        val decoded = BrightnessAction.decode(encoded)
        assertEquals(original, decoded)
    }

    @Test
    fun dndAction_encodeDecode() {
        val original = DndAction(mode = "PRIORITY")
        val encoded = DndAction.encode(original)
        val decoded = DndAction.decode(encoded)
        assertEquals(original, decoded)
    }

    @Test(expected = IllegalArgumentException::class)
    fun volumeAction_levelOutOfRangeLow() {
        val payload = buildJsonObject { put("level", -1); put("streamType", "RING") }
        VolumeAction.decode(payload)
    }

    @Test(expected = IllegalArgumentException::class)
    fun volumeAction_levelOutOfRangeHigh() {
        val payload = buildJsonObject { put("level", 101); put("streamType", "RING") }
        VolumeAction.decode(payload)
    }

    @Test(expected = IllegalArgumentException::class)
    fun brightnessAction_levelOutOfRange() {
        val payload = buildJsonObject { put("level", 300) }
        BrightnessAction.decode(payload)
    }

    @Test(expected = IllegalArgumentException::class)
    fun dndAction_invalidMode() {
        val payload = buildJsonObject { put("mode", "INVALID") }
        DndAction.decode(payload)
    }

    @Test
    fun dndAction_validModes() {
        val valid = listOf("OFF", "PRIORITY", "SILENT", "ALARMS")
        valid.forEach { mode ->
            val payload = buildJsonObject { put("mode", mode) }
            val action = DndAction.decode(payload) as DndAction
            assertEquals(mode, action.mode)
        }
    }

    @Test
    fun volumeActionViaRegistry() {
        val plugin = io.github.nnazuna.rules.core.plugin.RulePlugins.registry.action("VOLUME") as? VolumeAction.Companion
        assertNotNull(plugin)
        val action = VolumeAction(70, "MUSIC")
        val encoded = plugin!!.encode(action)
        val decoded = plugin.decode(encoded)
        assertEquals(action, decoded)
    }

    @Test
    fun brightnessActionViaRegistry() {
        val plugin = io.github.nnazuna.rules.core.plugin.RulePlugins.registry.action("BRIGHTNESS") as? BrightnessAction.Companion
        assertNotNull(plugin)
        val action = BrightnessAction(150)
        val encoded = plugin!!.encode(action)
        val decoded = plugin.decode(encoded)
        assertEquals(action, decoded)
    }

    @Test
    fun dndActionViaRegistry() {
        val plugin = io.github.nnazuna.rules.core.plugin.RulePlugins.registry.action("DND") as? DndAction.Companion
        assertNotNull(plugin)
        val action = DndAction("SILENT")
        val encoded = plugin!!.encode(action)
        val decoded = plugin.decode(encoded)
        assertEquals(action, decoded)
    }
}
