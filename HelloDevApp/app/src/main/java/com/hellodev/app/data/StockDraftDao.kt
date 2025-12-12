package com.hellodev.app.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface StockDraftDao {
    @Insert
    suspend fun insertDraft(draft: StockDraft): Long
    
    @Update
    suspend fun updateDraft(draft: StockDraft)
    
    @Delete
    suspend fun deleteDraft(draft: StockDraft)
    
    @Query("SELECT * FROM stock_drafts ORDER BY draftDate DESC")
    fun getAllDrafts(): Flow<List<StockDraft>>
    
    @Query("SELECT * FROM stock_drafts WHERE id = :draftId")
    suspend fun getDraftById(draftId: Int): StockDraft?
    
    @Query("DELETE FROM stock_drafts WHERE id = :draftId")
    suspend fun deleteDraftById(draftId: Int)
    
    @Query("DELETE FROM stock_drafts")
    suspend fun deleteAllDrafts()
}

class StockDraftRepository(private val draftDao: StockDraftDao) {
    val allDrafts: Flow<List<StockDraft>> = draftDao.getAllDrafts()
    
    suspend fun insertDraft(draft: StockDraft): Long {
        return draftDao.insertDraft(draft)
    }
    
    suspend fun updateDraft(draft: StockDraft) {
        draftDao.updateDraft(draft)
    }
    
    suspend fun deleteDraft(draft: StockDraft) {
        draftDao.deleteDraft(draft)
    }
    
    suspend fun getDraftById(draftId: Int): StockDraft? {
        return draftDao.getDraftById(draftId)
    }
    
    suspend fun deleteDraftById(draftId: Int) {
        draftDao.deleteDraftById(draftId)
    }
}
