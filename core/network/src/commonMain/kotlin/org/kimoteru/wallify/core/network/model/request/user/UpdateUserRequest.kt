package org.kimoteru.wallify.core.network.model.request.user

import org.kimoteru.wallify.core.model.user.UpdateUserParams

/** Body of the current user update request, sent as a form. `null` fields are not sent. */
data class UpdateUserRequest(
    val username: String? = null,
    val firstName: String? = null,
    val lastName: String? = null,
    val email: String? = null,
    val url: String? = null,
    val location: String? = null,
    val bio: String? = null,
    val instagramUsername: String? = null
)

fun UpdateUserParams.toRequest(): UpdateUserRequest = UpdateUserRequest(
    username = username,
    firstName = firstName,
    lastName = lastName,
    email = email,
    url = portfolioUrl,
    location = location,
    bio = bio,
    instagramUsername = instagramUsername
)