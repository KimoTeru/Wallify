package org.kimoteru.wallify.core.data.cache

import kotlin.time.Duration
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes

/**
 * How long a saved paged list is shown without asking the backend again (see [ListRemoteMediator]). Single items
 * and short lists have no age: they are refreshed every time they open ([refreshableFlow]).
 */
object CacheTtl {

    /** Lists that change often: feeds, search results, photos of a user, a topic or a collection. */
    val Feed: Duration = 5.minutes

    /** The list of topics. */
    val Slow: Duration = 6.hours
}