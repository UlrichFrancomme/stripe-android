package com.stripe.android

import java.net.HttpURLConnection
import org.json.JSONException
import org.json.JSONObject

/**
 * Represents a response from the Stripe servers.
 *
 * @param responseCode the response code (i.e. 404)
 * @param responseBody the body of the response
 * @param responseHeaders any headers associated with the response
 */
internal data class StripeResponse internal constructor(
    /**
     * @return the response code
     */
    internal val responseCode: Int,
    /**
     * @return the response body
     */
    internal val responseBody: String?,
    /**
     * @return the response headers
     */
    internal val responseHeaders: Map<String, List<String>> = emptyMap()
) {
    internal val isOk: Boolean
        get() = responseCode == HttpURLConnection.HTTP_OK

    internal val requestId: String?
        get() {
            return getHeaderValue(REQUEST_ID_HEADER)?.firstOrNull()
        }

    internal fun hasErrorCode(): Boolean {
        return responseCode < 200 || responseCode >= 300
    }

    internal val responseJson: JSONObject
        @Throws(JSONException::class)
        get() {
            return responseBody?.let {
                JSONObject(it)
            } ?: JSONObject()
        }

    override fun toString(): String {
        return "Request-Id: $requestId, Status Code: $responseCode"
    }

    internal fun getHeaderValue(key: String): List<String>? {
        return responseHeaders.entries
            .firstOrNull {
                it.key.equals(key, ignoreCase = true)
            }?.value
    }

    private companion object {
        private const val REQUEST_ID_HEADER = "Request-Id"
    }
}
