package dev.androidbasekit.common

import org.junit.Assert.*
import org.junit.Test
import java.io.ByteArrayInputStream
import java.math.BigDecimal
import java.util.Locale
import java.util.TimeZone

/**
 * 常用工具的边界回归测试：Unicode、严格日期、十进制舍入、摘要与流所有权。
 */
class CommonToolsTest {
    private val utc = TimeZone.getTimeZone("UTC")

    @Test fun blankFallbackKeepsMeaningfulWhitespace() {
        assertTrue(Texts.isBlank(null))
        assertEquals("fallback", Texts.orDefault(" \t", "fallback"))
        assertEquals(" a ", Texts.orDefault(" a ", "fallback"))
    }

    @Test fun truncateDoesNotSplitSurrogatePair() {
        assertEquals("A😀…", Texts.truncate("A😀B", 2))
        assertEquals("A😀B", Texts.truncate("A😀B", 3))
        assertEquals("", Texts.truncate("A", 0, ""))
    }

    @Test(expected = IllegalArgumentException::class)
    fun negativeTextLimitRejected() { Texts.truncate("abc", -1) }

    @Test fun dateParsingRequiresValidCalendarDateAndFullInput() {
        assertNull(Dates.parse("2025-02-29", "yyyy-MM-dd", Locale.US, utc))
        assertNull(Dates.parse("2024-02-29junk", "yyyy-MM-dd", Locale.US, utc))
        val millis = Dates.parse("2024-02-29", "yyyy-MM-dd", Locale.US, utc)!!
        assertEquals("2024-02-29", Dates.format(millis, "yyyy-MM-dd", Locale.US, utc))
    }

    @Test fun dateZoneIsExplicitAndTimestampUsesMillis() {
        assertEquals("1970-01-01 00:00", Dates.format(0, "yyyy-MM-dd HH:mm", Locale.US, utc))
        assertEquals("1970-01-01 08:00", Dates.format(0, "yyyy-MM-dd HH:mm", Locale.US, TimeZone.getTimeZone("GMT+08:00")))
    }

    @Test fun decimalUsesExactRoundingAndGroupingUsesLocale() {
        assertEquals("1.01", Numbers.decimal(BigDecimal("1.005"), 2))
        assertEquals("-1.01", Numbers.decimal(BigDecimal("-1.005"), 2))
        assertEquals("1,234.50", Numbers.grouped(BigDecimal("1234.5"), 2, Locale.US))
        assertEquals("1.234,50", Numbers.grouped(BigDecimal("1234.5"), 2, Locale.GERMANY))
    }

    @Test(expected = IllegalArgumentException::class)
    fun unboundedPrecisionRejected() { Numbers.decimal(BigDecimal.ONE, 101) }

    @Test fun digestMatchesKnownVectorWithoutClosingCallerStream() {
        val expected = "ba7816bf8f01cfea414140de5dae2223b00361a396177a9cb410ff61f20015ad"
        var closed = false
        val stream = object : ByteArrayInputStream("abc".toByteArray()) {
            override fun close() { closed = true; super.close() }
        }
        assertEquals(expected, Digests.sha256("abc"))
        assertEquals(expected, Digests.sha256(stream))
        assertFalse(closed)
        assertEquals(-1, stream.read())
    }
}
