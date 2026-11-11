package org.kimoteru.wallify.core.presentation.photo

import kotlin.test.Test
import kotlin.test.assertEquals
import org.kimoteru.wallify.core.model.photo.ContentFilter
import org.kimoteru.wallify.core.model.photo.PhotoColor
import org.kimoteru.wallify.core.model.photo.PhotoOrientation
import org.kimoteru.wallify.core.model.photo.SearchOrder

/**
 * Every domain value has its own screen value and back. The mappers find the value by `domain` instead of a `when`,
 * so a value missing in `XxxUi` fails here, not on the screen.
 */
class PhotoUiTest {

    @Test
    fun everyContentFilterMapsBothWays() {
        ContentFilter.entries.forEach { filter -> assertEquals(filter, filter.toPresentation().toDomain()) }
        ContentFilterUi.entries.forEach { filter -> assertEquals(filter, filter.toDomain().toPresentation()) }
    }

    @Test
    fun everyPhotoColorMapsBothWays() {
        PhotoColor.entries.forEach { color -> assertEquals(color, color.toPresentation().toDomain()) }
        PhotoColorUi.entries.forEach { color -> assertEquals(color, color.toDomain().toPresentation()) }
    }

    @Test
    fun everyPhotoOrientationMapsBothWays() {
        PhotoOrientation.entries.forEach { orientation ->
            assertEquals(orientation, orientation.toPresentation().toDomain())
        }
        PhotoOrientationUi.entries.forEach { orientation ->
            assertEquals(orientation, orientation.toDomain().toPresentation())
        }
    }

    @Test
    fun everySearchOrderMapsBothWays() {
        SearchOrder.entries.forEach { order -> assertEquals(order, order.toPresentation().toDomain()) }
        SearchOrderUi.entries.forEach { order -> assertEquals(order, order.toDomain().toPresentation()) }
    }
}