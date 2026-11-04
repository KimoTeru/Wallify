package org.kimoteru.wallify.core.network

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import org.kimoteru.wallify.core.network.config.ApiCredentials
import org.kimoteru.wallify.core.network.config.apiNetworkConfig

class ApiNetworkConfigTest {

    @Test
    fun baseUrlIsEmbedded() {
        assertEquals("https://api.unsplash.com/", apiNetworkConfig(credentials()).baseUrl)
    }

    @Test
    fun accessKeyAndVersionBecomeStaticHeaders() {
        val config = apiNetworkConfig(credentials())

        assertEquals("Client-ID access", config.defaultHeaders["Authorization"])
        assertEquals("v1", config.defaultHeaders["Accept-Version"])
    }

    @Test
    fun secretKeyIsNeverSentWithRequests() {
        val config = apiNetworkConfig(credentials())

        assertFalse(config.defaultHeaders.values.any { "secret" in it })
    }

    @Test
    fun blankKeysAreRejected() {
        assertFailsWith<IllegalArgumentException> { ApiCredentials(accessKey = " ", secretKey = "secret") }
        assertFailsWith<IllegalArgumentException> { ApiCredentials(accessKey = "access", secretKey = "") }
    }

    @Test
    fun credentialsHideKeysInToString() {
        val text = ApiCredentials(accessKey = "access-value", secretKey = "secret-value").toString()

        assertFalse("access-value" in text)
        assertFalse("secret-value" in text)
    }

    private fun credentials() = ApiCredentials(accessKey = "access", secretKey = "secret")
}