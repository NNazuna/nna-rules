package io.github.nnazuna.rules.core.extraction

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class TextExtractorTest {

    private fun otp(text: String): ExtractionResult =
        TextExtractor.extract(text, ExtractionMode.OTP)

    private fun custom(text: String, pattern: String?): ExtractionResult =
        TextExtractor.extract(text, ExtractionMode.CUSTOM_REGEX, pattern)

    @Test
    fun otpExtractsStandaloneCode() {
        val result = otp("Your code is 123456")
        assertEquals("123456", result.text)
        assertTrue(result.extracted)
        assertNull(result.problem)
    }

    @Test
    fun otpUsesFirstMatch() {
        assertEquals("1234", otp("1234 and 5678").text)
    }

    @Test
    fun otpRejectsTooShortCode() {
        val result = otp("code 123")
        assertFalse(result.extracted)
        assertEquals(Problem.NO_MATCH, result.problem)
        assertEquals("code 123", result.text)
    }

    @Test
    fun otpRejectsLongDigitRunWithoutPartialMatch() {
        assertFalse(otp("123456789").extracted)
        assertFalse(otp("Your code is 123456789").extracted)
    }

    @Test
    fun otpRequiresSeparatorAroundAlphanumericCode() {
        assertFalse(otp("OTP123456").extracted)
        assertTrue(otp("OTP: 123456").extracted)
    }

    @Test
    fun otpAcceptsAlphanumericCodeWithDigit() {
        assertEquals("A1B2C3", otp("Your OTP is A1B2C3.").text)
        assertEquals("AB1234", otp("Your code is AB1234.").text)
    }

    @Test
    fun otpRejectsDigitsSeparatedBySpaces() {
        assertFalse(otp("123 456").extracted)
    }

    @Test
    fun otpRejectsTextWithoutDigits() {
        assertFalse(otp("no code here").extracted)
        assertFalse(otp("").extracted)
        assertFalse(otp("   ").extracted)
    }

    @Test
    fun otpIgnoresFullWidthDigits() {
        assertFalse(otp("コードは１２３４５６です").extracted)
    }

    @Test
    fun customRegexUsesFirstGroup() {
        val result = custom("code: 987654", "code: (\\d+)")
        assertEquals("987654", result.text)
        assertTrue(result.extracted)
    }

    @Test
    fun customRegexFallsBackToWholeMatchWithoutGroup() {
        assertEquals("987654", custom("code: 987654", "\\d{4,6}").text)
    }

    @Test
    fun customRegexFallsBackToWholeMatchWhenGroupDoesNotParticipate() {
        val result = custom("1234", "(?:(x))?(\\d+)")
        assertEquals("1234", result.text)
        assertTrue(result.extracted)
    }

    @Test
    fun customRegexFallsBackToWholeMatchWhenGroupIsBlank() {
        val result = custom("b", "(a*)b")
        assertEquals("b", result.text)
        assertTrue(result.extracted)
    }

    @Test
    fun customRegexReportsNoMatch() {
        val result = custom("nothing here", "\\d+")
        assertFalse(result.extracted)
        assertEquals(Problem.NO_MATCH, result.problem)
    }

    @Test
    fun customRegexReportsInvalidPattern() {
        val result = custom("code: 1234", "(")
        assertFalse(result.extracted)
        assertEquals(Problem.INVALID_PATTERN, result.problem)
        assertEquals("code: 1234", result.text)
    }

    @Test
    fun blankPatternIsInvalid() {
        val result = custom("code: 1234", "   ")
        assertFalse(result.extracted)
        assertEquals(Problem.INVALID_PATTERN, result.problem)
        assertFalse(TextExtractor.isValidPattern(""))
        assertFalse(TextExtractor.isValidPattern(null))
        assertTrue(TextExtractor.isValidPattern("(\\d+)"))
        assertFalse(TextExtractor.isValidPattern("("))
    }

    @Test
    fun customRegexSupportsInlineFlags() {
        val result = custom("CODE", "(?i)code")
        assertEquals("CODE", result.text)
        assertTrue(result.extracted)
    }

    @Test
    fun fullModeIgnoresExtractionSettings() {
        val result = TextExtractor.resolveForClipboard(
            text = "hello",
            mode = ClipboardMode.FULL,
            extractionMode = ExtractionMode.CUSTOM_REGEX,
            customRegex = "(",
        )
        assertEquals("hello", result.text)
        assertEquals(ClipboardOutcome.FULL, result.outcome)
    }

    @Test
    fun extractedModeReturnsCode() {
        val result = TextExtractor.resolveForClipboard(
            text = "Your code is 4321",
            mode = ClipboardMode.EXTRACTED,
            extractionMode = ExtractionMode.OTP,
        )
        assertEquals("4321", result.text)
        assertEquals(ClipboardOutcome.EXTRACTED, result.outcome)
    }

    @Test
    fun noMatchFallsBackToFullText() {
        val result = TextExtractor.resolveForClipboard(
            text = "nothing here",
            mode = ClipboardMode.EXTRACTED,
            extractionMode = ExtractionMode.OTP,
        )
        assertEquals("nothing here", result.text)
        assertEquals(ClipboardOutcome.FALLBACK_NO_MATCH, result.outcome)
    }

    @Test
    fun invalidPatternFallsBackToFullText() {
        val result = TextExtractor.resolveForClipboard(
            text = "nothing here",
            mode = ClipboardMode.EXTRACTED,
            extractionMode = ExtractionMode.CUSTOM_REGEX,
            customRegex = "(",
        )
        assertEquals("nothing here", result.text)
        assertEquals(ClipboardOutcome.FALLBACK_INVALID_PATTERN, result.outcome)
    }
}
