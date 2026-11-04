package org.kimoteru.wallify.core.network.model.request.collection

/** Body of the collection create and update requests, sent as a form. `null` fields are not sent. */
data class CollectionRequest(
    val title: String? = null,
    val description: String? = null,
    val isPrivate: Boolean? = null
)