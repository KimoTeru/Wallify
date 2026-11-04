package org.kimoteru.wallify.core.network

import kotlin.test.Test
import kotlin.test.assertEquals
import org.kimoteru.wallify.core.network.api.WebLinks

class WebLinksTest {

    @Test
    fun uploadPageIsOnTheWebsite() {
        assertEquals("https://unsplash.com/submit", WebLinks().uploadPageUrl)
    }
}