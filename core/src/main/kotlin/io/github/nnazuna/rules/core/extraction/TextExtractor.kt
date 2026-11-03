package io.github.nnazuna.rules.core.extraction

import java.util.regex.PatternSyntaxException

enum class ClipboardMode {
    FULL,
    EXTRACTED,
}

enum class ExtractionMode {
    OTP,
    CUSTOM_REGEX,
}

enum class Problem {
    NO_MATCH,
    INVALID_PATTERN,
}

enum class ClipboardOutcome {
    FULL,
    EXTRACTED,
    FALLBACK_NO_MATCH,
    FALLBACK_INVALID_PATTERN,
}

data class ExtractionResult(
    val text: String,
    val extracted: Boolean,
    val problem: Problem? = null,
)

data class ClipboardText(
    val text: String,
    val outcome: ClipboardOutcome,
)

object TextExtractor {
    private val OTP_PATTERN =
        Regex("(?<![A-Za-z0-9])(?=[A-Za-z0-9]*\\d)[A-Za-z0-9]{4,8}(?![A-Za-z0-9])")

    fun extract(
        text: String,
        mode: ExtractionMode,
        customRegex: String? = null,
    ): ExtractionResult = when (mode) {
        ExtractionMode.OTP -> extractOtp(text)
        ExtractionMode.CUSTOM_REGEX -> extractCustom(text, customRegex)
    }

    fun resolveForClipboard(
        text: String,
        mode: ClipboardMode,
        extractionMode: ExtractionMode,
        customRegex: String? = null,
    ): ClipboardText {
        if (mode == ClipboardMode.FULL) {
            return ClipboardText(text, ClipboardOutcome.FULL)
        }
        val result = extract(text, extractionMode, customRegex)
        if (result.extracted) {
            return ClipboardText(result.text, ClipboardOutcome.EXTRACTED)
        }
        val outcome = when (result.problem) {
            Problem.INVALID_PATTERN -> ClipboardOutcome.FALLBACK_INVALID_PATTERN
            Problem.NO_MATCH, null -> ClipboardOutcome.FALLBACK_NO_MATCH
        }
        return ClipboardText(text, outcome)
    }

    fun isValidPattern(pattern: String?): Boolean {
        val trimmed = pattern?.trim().orEmpty()
        if (trimmed.isEmpty()) return false
        return try {
            Regex(trimmed)
            true
        } catch (_: PatternSyntaxException) {
            false
        }
    }

    private fun extractOtp(text: String): ExtractionResult {
        val match = OTP_PATTERN.find(text)
        if (match == null || match.value.isBlank()) {
            return ExtractionResult(text = text, extracted = false, problem = Problem.NO_MATCH)
        }
        return ExtractionResult(text = match.value, extracted = true)
    }

    private fun extractCustom(text: String, customRegex: String?): ExtractionResult {
        val pattern = customRegex?.trim().orEmpty()
        if (pattern.isEmpty()) {
            return ExtractionResult(text = text, extracted = false, problem = Problem.INVALID_PATTERN)
        }
        val regex = try {
            Regex(pattern)
        } catch (_: PatternSyntaxException) {
            return ExtractionResult(text = text, extracted = false, problem = Problem.INVALID_PATTERN)
        }
        val match = regex.find(text)
            ?: return ExtractionResult(text = text, extracted = false, problem = Problem.NO_MATCH)
        val group = if (match.groups.size > 1) match.groups[1]?.value else null
        val extracted = group?.takeIf { it.isNotBlank() } ?: match.value
        if (extracted.isBlank()) {
            return ExtractionResult(text = text, extracted = false, problem = Problem.NO_MATCH)
        }
        return ExtractionResult(text = extracted, extracted = true)
    }
}
