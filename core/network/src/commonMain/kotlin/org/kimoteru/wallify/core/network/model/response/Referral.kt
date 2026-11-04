package org.kimoteru.wallify.core.network.model.response

private const val REFERRAL_QUERY = "utm_source=Wallify&utm_medium=referral"

/**
 * Adds the referral parameters the backend asks for to a link to its photo or profile page, so the link that
 * credits the author and the source also tells where the visitor came from.
 */
internal fun String.withReferral(): String {
    val separator = if (contains('?')) '&' else '?'
    return "$this$separator$REFERRAL_QUERY"
}