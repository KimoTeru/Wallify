package org.kimoteru.wallify.core.common.extensions

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class ExtensionsTest {
    @Test
    fun clampToPercentLimitsRange() {
        assertEquals(0f, (-1f).clampToPercent())
        assertEquals(1f, 2f.clampToPercent())
        assertEquals(0.5f, 0.5f.clampToPercent())
        assertEquals(0f, Float.NaN.clampToPercent())
    }

    @Test
    fun pixelSizeIsAtLeastOne() {
        assertEquals(1, 0f.toPixelSize(100))
        assertEquals(50, 0.5f.toPixelSize(100))
        assertEquals(0, 0f.toPixelOffset(100))
    }

    @Test
    fun toTimeStringFormatsMinutesAndHours() {
        assertEquals("01:05", 65_000L.toTimeString())
        assertEquals("01:01:01", 3_661_000L.toTimeString())
    }

    @Test
    fun toYearReadsIsoDates() {
        assertEquals(2024, "2024-05-17T10:15:30.000Z".toYear())
        assertEquals(1999, "1999-01-01".toYear())
        assertNull("not a date".toYear())
    }

    @Test
    fun urlHelpersIgnoreQueryAndDecode() {
        val url = "https://cdn.example.com/photos/My%20Photo.JPG?w=100#top"
        assertEquals("My Photo", url.fileNameFromUrl())
        assertEquals("jpg", url.formatFromUrl())
        assertEquals("file", "https://example.com/".fileNameFromUrl())
        assertEquals("bin", "https://example.com/download".formatFromUrl())
    }

    @Test
    fun jsonToMapFlattensPrimitives() {
        val map = """{"a":1,"b":"x","c":true,"d":null,"e":1.5,"f":[1]}""".jsonToMap()
        assertEquals(1L, map["a"])
        assertEquals("x", map["b"])
        assertEquals(true, map["c"])
        assertNull(map["d"])
        assertEquals(1.5, map["e"])
        assertEquals("[1]", map["f"])
        assertEquals(emptyMap(), "broken".jsonToMap())
    }
}