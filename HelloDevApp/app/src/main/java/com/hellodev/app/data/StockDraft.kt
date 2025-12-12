package com.hellodev.app.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

@Entity(tableName = "stock_drafts")
@TypeConverters(DraftItemsConverter::class)
data class StockDraft(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val supplierName: String,
    val vehicleNumber: String,
    val draftDate: Long = System.currentTimeMillis(),
    val items: List<DraftItem> = emptyList(),
    val notes: String = ""
)

data class DraftItem(
    val categoryId: Int,
    val rubberName: String,
    val rubberId: Int,
    val numberOfRolls: Int,
    val weightInKg: Double,
    val costOfStock: Double = 0.0,
    val addedAt: Long = System.currentTimeMillis()
)

class DraftItemsConverter {
    private val gson = Gson()
    
    @TypeConverter
    fun fromDraftItemsList(items: List<DraftItem>): String {
        return gson.toJson(items)
    }
    
    @TypeConverter
    fun toDraftItemsList(itemsString: String): List<DraftItem> {
        val type = object : TypeToken<List<DraftItem>>() {}.type
        return gson.fromJson(itemsString, type)
    }
}
