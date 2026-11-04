package org.kimoteru.wallify.core.database.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * One place in an ordered list: the [position]-th item of the list [listKey] is the row with id [itemId] in the
 * table of the list's items. The same photo can sit in many lists, so order lives here and not in the item table.
 */
@Entity(
    tableName = "list_entry",
    primaryKeys = ["listKey", "position"],
    indices = [Index("itemId")]
)
data class ListEntryEntity(
    val listKey: String,
    val position: Int,
    val itemId: String
)

/** When the list [listKey] was last loaded from the backend; a list without a row has never been loaded. */
@Entity(tableName = "list_state")
data class ListStateEntity(
    @PrimaryKey val listKey: String,
    val updatedAtEpochMillis: Long
)