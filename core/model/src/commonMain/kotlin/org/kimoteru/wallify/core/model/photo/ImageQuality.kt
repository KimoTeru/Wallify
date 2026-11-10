package org.kimoteru.wallify.core.model.photo

import kotlinx.serialization.Serializable

/**
 * Quality of a photo image, from the smallest file to the original. Each level matches one size of [PhotoUrls]:
 * [Low] is `thumb`, [Medium] is `small`, [High] is `regular`, [Original] is `raw`.
 * The user picks one level for photo lists, one for the photo screen and one for downloads.
 */
@Serializable
enum class ImageQuality { Low, Medium, High, Original }