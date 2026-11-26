package org.kimoteru.wallify.core.data.cache

import kotlin.time.Duration
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.hours

/**
 * How long saved data is shown without asking the backend again: paged lists ([ListRemoteMediator]), single items
 * and short lists ([refreshableFlow], [refreshableListFlow]). The age is stored in `list_state`. Only the user
 * overrides it: a pull to refresh, a retry, a changed filter, a shuffle, or an invalidation after a change.
 *
 * The values follow how often the data really changes, not how fresh it could be: the demo key of the backend allows
 * 50 requests an hour, the saved data is shown at once anyway (an old one too), and fresh data is one pull away.
 */
object CacheTtl {

    /** Lists that get new items: feeds, search results, photos of a user, a topic or a collection. */
    val Feed: Duration = 1.hours

    /** Single photos, users, collections and topics: their details almost never change, the counters may lag. */
    val Item: Duration = 1.days

    /** The full photo (`GET /photos/{id}`): its details rarely change, and every open of a photo costs a request. */
    val Photo: Duration = 7.days

    /**
     * Collections of the signed-in user. The photo screen (add to collection) and the profile share this list, so the
     * TTL is one for both. Changes made in the app invalidate it (`CacheStore.invalidateLists`).
     */
    val UserCollections: Duration = 1.days

    /** Statistics of a user and the counters of the service: the backend counts them by day. */
    val Stats: Duration = 1.days

    /** The list of topics: a new topic appears rarely. */
    val Slow: Duration = 1.days

    /** Lists and statistics not loaded for this long are removed from the database at the start of the app. */
    val Retention: Duration = 14.days
}