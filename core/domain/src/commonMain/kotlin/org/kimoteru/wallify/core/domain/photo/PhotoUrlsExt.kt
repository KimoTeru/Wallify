package org.kimoteru.wallify.core.domain.photo

import org.kimoteru.wallify.core.model.photo.ImageQuality
import org.kimoteru.wallify.core.model.photo.PhotoUrls

/**
 * The one place that maps a quality to an image address the screens show. The address is used as it is, so the
 * image loader keys its caches by it.
 */
fun PhotoUrls.forQuality(quality: ImageQuality): String = when (quality) {
    ImageQuality.Low -> thumb
    ImageQuality.Medium -> small
    ImageQuality.High -> regular
    ImageQuality.Original -> raw
}