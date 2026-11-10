package org.kimoteru.wallify.core.data.cache

import kotlin.time.Duration
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes

/**
 * How long saved data is shown without asking the backend again: paged lists ([ListRemoteMediator]), single items
 * and short lists ([refreshableFlow], [refreshableListFlow]). The age is stored in `list_state`. Only the user
 * overrides it: a pull to refresh, a retry, a changed filter, a shuffle, or an invalidation after a change.
 */
object CacheTtl {

    /** Lists that change often: feeds, search results, photos of a user, a topic or a collection. */
    val Feed: Duration = 5.minutes

    /** Single photos, users, collections and topics. */
    val Item: Duration = 10.minutes

    /** Statistics of a photo or a user and the counters of the service. */
    val Stats: Duration = 1.hours

    /** The list of topics. */
    val Slow: Duration = 6.hours

    /** Lists and statistics not loaded for this long are removed from the database at the start of the app. */
    val Retention: Duration = 14.days
}