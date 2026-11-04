package org.kimoteru.wallify.feature.profile.ui

import androidx.compose.runtime.Immutable
import org.kimoteru.wallify.core.model.user.UpdateUserParams
import org.kimoteru.wallify.core.model.user.User

/** Fields of the profile form. */
enum class EditField { Username, FirstName, LastName, Email, Portfolio, Location, Bio, Instagram }

/** Text of the profile form: every field is a string, an empty one means "not set". */
@Immutable
data class EditForm(
    val username: String,
    val firstName: String,
    val lastName: String,
    val email: String,
    val portfolio: String,
    val location: String,
    val bio: String,
    val instagram: String
) {

    /** The backend refuses a profile without a username. */
    val isValid: Boolean
        get() = username.isNotBlank()

    fun with(field: EditField, value: String): EditForm = when (field) {
        EditField.Username -> copy(username = value)
        EditField.FirstName -> copy(firstName = value)
        EditField.LastName -> copy(lastName = value)
        EditField.Email -> copy(email = value)
        EditField.Portfolio -> copy(portfolio = value)
        EditField.Location -> copy(location = value)
        EditField.Bio -> copy(bio = value)
        EditField.Instagram -> copy(instagram = value)
    }

    /** Only the fields that differ from [user] are sent; the others stay as they are on the backend. */
    fun changesTo(user: User): UpdateUserParams = UpdateUserParams(
        username = changedOf(username, user.username),
        firstName = changedOf(firstName, user.firstName),
        lastName = changedOf(lastName, user.lastName),
        email = changedOf(email, user.email),
        portfolioUrl = changedOf(portfolio, user.portfolioUrl),
        location = changedOf(location, user.location),
        bio = changedOf(bio, user.bio),
        instagramUsername = changedOf(instagram, user.instagramUsername)
    )

    private fun changedOf(text: String, saved: String?): String? {
        val trimmed = text.trim()
        return trimmed.takeIf { it != saved.orEmpty() }
    }

    companion object {
        fun of(user: User): EditForm = EditForm(
            username = user.username,
            firstName = user.firstName.orEmpty(),
            lastName = user.lastName.orEmpty(),
            email = user.email.orEmpty(),
            portfolio = user.portfolioUrl.orEmpty(),
            location = user.location.orEmpty(),
            bio = user.bio.orEmpty(),
            instagram = user.instagramUsername.orEmpty()
        )
    }
}