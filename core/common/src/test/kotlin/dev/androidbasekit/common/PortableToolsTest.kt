package dev.androidbasekit.common

import org.junit.Assert.*
import org.junit.Test

class PortableToolsTest {
    @Test fun durationUnitsAndHourBoundaries() {
        assertEquals("00:00", Durations.formatSeconds(0))
        assertEquals("02:05", Durations.formatSeconds(125))
        assertEquals("59:59", Durations.formatSeconds(3599))
        assertEquals("1:00:00", Durations.formatSeconds(3600))
        assertEquals("25:01:01", Durations.formatSeconds(90061))
        assertEquals("02:05", Durations.formatMillis(125999))
        assertEquals("2562047788015215:30:07", Durations.formatSeconds(Long.MAX_VALUE))
    }

    @Test fun negativeDurationsAreNotSilentlyClamped() {
        assertThrows(IllegalArgumentException::class.java) { Durations.formatSeconds(-1) }
        assertThrows(IllegalArgumentException::class.java) { Durations.formatMillis(-1) }
    }

    @Test fun binarySizeUnitsPrecisionAndLongLimit() {
        assertEquals("0 B", ByteSizes.format(0))
        assertEquals("1023 B", ByteSizes.format(1023))
        assertEquals("1.00 KiB", ByteSizes.format(1024))
        assertEquals("1.50 MiB", ByteSizes.format(1572864))
        assertEquals("2 KiB", ByteSizes.format(1536, 0))
        assertEquals("8.00 EiB", ByteSizes.format(Long.MAX_VALUE))
        assertEquals("1024.00 KiB", ByteSizes.format(1048575))
    }

    @Test fun invalidSizeInputsAreRejected() {
        assertThrows(IllegalArgumentException::class.java) { ByteSizes.format(-1) }
        assertThrows(IllegalArgumentException::class.java) { ByteSizes.format(0, 7) }
    }

    @Test fun webValidationRejectsMalformedOrCredentialUrls() {
        listOf("https://example.com", "HTTP://example.com:8080/a", "https://[::1]/").forEach {
            assertTrue(it, WebUrls.isHttpUrl(it))
        }
        listOf(null, "", "example.com", "javascript:alert(1)", "https:///path", "https://a b/",
            "https://user:pass@example.com/", "https://example.com:70000", "https://example.com/%GG").forEach {
            assertFalse(it, WebUrls.isHttpUrl(it))
        }
    }

    @Test fun queryEncodingPreservesExistingEscapesAndFragment() {
        assertEquals("a%20b%2Bc%26%3D%2F%E4%B8%AD", WebUrls.encodeQueryComponent("a b+c&=/中"))
        assertEquals("https://example.com/a%20b?old=%2F&q=a%20b%26%23#part?x=1",
            WebUrls.appendQueryParameter("https://example.com/a%20b?old=%2F#part?x=1", "q", "a b&#"))
        assertEquals("https://example.com?q=1&q=2", WebUrls.appendQueryParameter("https://example.com?q=1", "q", "2"))
        assertEquals("https://example.com?q=", WebUrls.appendQueryParameter("https://example.com?", "q", ""))
        assertThrows(IllegalArgumentException::class.java) { WebUrls.appendQueryParameter("file:///a", "q", "x") }
    }
}
