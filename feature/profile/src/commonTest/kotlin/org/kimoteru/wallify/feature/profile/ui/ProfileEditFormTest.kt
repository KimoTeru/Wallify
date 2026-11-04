package org.kimoteru.wallify.feature.profile.ui

import org.kimoteru.wallify.core.model.user.UpdateUserParams
import org.kimoteru.wallify.core.model.user.User
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ProfileEditFormTest {

    private val user = User(
        id = "u1",
        username = "jimmy",
        name = "Jimmy Example",
        firstName = "Jimmy",
        lastName = null,
        bio = "Photographer",
        location = null,
        portfolioUrl = null,
        instagramUsername = null,
        email = "jimmy@example.com",
        totalPhotos = 3,
        totalCollections = 1,
        profileImage = null,
        profileUrl = null
    )

    @Test
    fun `form starts with the saved values and empty text for missing ones`() {
        val form = EditForm.of(user)

        assertEquals("jimmy", form.username)
        assertEquals("Jimmy", form.firstName)
        assertEquals("", form.lastName)
        assertEquals("jimmy@example.com", form.email)
    }

    @Test
    fun `untouched form changes nothing`() {
        assertEquals(UpdateUserParams(), EditForm.of(user).changesTo(user))
    }

    @Test
    fun `only changed fields are sent`() {
        val form = EditForm.of(user)
            .with(EditField.Location, "  Montreal  ")
            .with(EditField.LastName, "Example")

        assertEquals(UpdateUserParams(lastName = "Example", location = "Montreal"), form.changesTo(user))
    }

    @Test
    fun `cleared field is sent as empty text`() {
        val form = EditForm.of(user).with(EditField.Bio, "")

        assertEquals(UpdateUserParams(bio = ""), form.changesTo(user))
    }

    @Test
    fun `form without a username is not valid`() {
        assertTrue(EditForm.of(user).isValid)
        assertFalse(EditForm.of(user).with(EditField.Username, "  ").isValid)
    }
}