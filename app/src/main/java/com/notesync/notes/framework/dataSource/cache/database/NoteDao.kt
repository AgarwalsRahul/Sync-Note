package com.notesync.notes.framework.dataSource.cache.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.notesync.notes.business.domain.model.Note
import com.notesync.notes.framework.dataSource.cache.model.NoteCacheEntity
import com.notesync.notes.framework.dataSource.cache.model.TrashNoteCacheEntity
import kotlinx.coroutines.flow.Flow


const val NOTE_ORDER_ASC: String = ""
const val NOTE_ORDER_DESC: String = "-"
const val NOTE_FILTER_TITLE = "title"
const val NOTE_FILTER_DATE_CREATED = "created_at"

const val ORDER_BY_ASC_DATE_UPDATED = NOTE_ORDER_ASC + NOTE_FILTER_DATE_CREATED
const val ORDER_BY_DESC_DATE_UPDATED = NOTE_ORDER_DESC + NOTE_FILTER_DATE_CREATED
const val ORDER_BY_ASC_TITLE = NOTE_ORDER_ASC + NOTE_FILTER_TITLE
const val ORDER_BY_DESC_TITLE = NOTE_ORDER_DESC + NOTE_FILTER_TITLE

const val NOTE_PAGINATION_PAGE_SIZE = 30

@Dao
interface NoteDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNote(note: NoteCacheEntity): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertNotes(notes: List<NoteCacheEntity>): LongArray

    @Query("SELECT * FROM notes WHERE id = :id")
    suspend fun searchNoteById(id: String): NoteCacheEntity?

    @Query("DELETE FROM notes WHERE id IN (:ids)")
    suspend fun deleteNotes(ids: List<String>): Int

    @Query("DELETE FROM notes")
    suspend fun deleteAllNotes()

    @Query("DELETE FROM trashNotes")
    suspend fun emptyTrash() : Int

    @Query(
        """
        UPDATE notes 
        SET 
        title = :title, 
        body = :body,
        updated_at = :updated_at
        WHERE id = :primaryKey
        """
    )
    suspend fun updateNote(
        primaryKey: String,
        title: String,
        body: String?,
        updated_at: String
    ): Int

    @Query("DELETE FROM notes WHERE id = :primaryKey")
    suspend fun deleteNote(primaryKey: String): Int

    @Query("SELECT * FROM notes")
    fun searchNotes(): Flow<List<NoteCacheEntity>>

    @Query(
        """
        SELECT * FROM notes 
        WHERE title LIKE '%' || :query || '%' 
        OR body LIKE '%' || :query || '%' 
        ORDER BY updated_at DESC LIMIT (:page * :pageSize)
        """
    )
     fun searchNotesOrderByDateDESC(
        query: String,
        page: Int,
        pageSize: Int = NOTE_PAGINATION_PAGE_SIZE
    ): Flow<List<NoteCacheEntity>>

    @Query(
        """
        SELECT * FROM notes 
        WHERE title LIKE '%' || :query || '%' 
        OR body LIKE '%' || :query || '%' 
        ORDER BY updated_at ASC LIMIT (:page * :pageSize)
        """
    )
     fun searchNotesOrderByDateASC(
        query: String,
        page: Int,
        pageSize: Int = NOTE_PAGINATION_PAGE_SIZE
    ): Flow<List<NoteCacheEntity>>

    @Query(
        """
        SELECT * FROM notes 
        WHERE title LIKE '%' || :query || '%' 
        OR body LIKE '%' || :query || '%' 
        ORDER BY title DESC LIMIT (:page * :pageSize)
        """
    )
     fun searchNotesOrderByTitleDESC(
        query: String,
        page: Int,
        pageSize: Int = NOTE_PAGINATION_PAGE_SIZE
    ): Flow<List<NoteCacheEntity>>

    @Query(
        """
        SELECT * FROM notes 
        WHERE title LIKE '%' || :query || '%' 
        OR body LIKE '%' || :query || '%' 
        ORDER BY title ASC LIMIT (:page * :pageSize)
        """
    )
     fun searchNotesOrderByTitleASC(
        query: String,
        page: Int,
        pageSize: Int = NOTE_PAGINATION_PAGE_SIZE
    ): Flow<List<NoteCacheEntity>>


    @Query("SELECT COUNT(*) FROM notes")
    suspend fun getNumNotes(): Int

    @Query("DELETE FROM trashNotes WHERE id = :primaryKey")
    suspend fun deleteTrashNote(primaryKey: String): Int

    @Query("DELETE FROM trashNotes WHERE id IN (:ids)")
    suspend fun deleteTrashNotes(ids: List<String>): Int

    @Query("SELECT COUNT(*) FROM trashNotes")
    suspend fun getNumTrashNotes(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTrashNote(note:TrashNoteCacheEntity):Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertTrashNotes(notes:List<TrashNoteCacheEntity>):LongArray

    @Query(
        """
        SELECT * FROM trashNotes
        ORDER BY updated_at DESC LIMIT (:page * :pageSize)
        """
    )
    fun getTrashNotes(page:Int,pageSize: Int= NOTE_PAGINATION_PAGE_SIZE):Flow<List<Note>>
}


 fun NoteDao.returnOrderedQuery(
    query: String,
    filterAndOrder: String,
    page: Int
): Flow<List<NoteCacheEntity>> {

    when {

        filterAndOrder.contains(ORDER_BY_DESC_DATE_UPDATED) -> {
            return searchNotesOrderByDateDESC(
                query = query,
                page = page
            )
        }

        filterAndOrder.contains(ORDER_BY_ASC_DATE_UPDATED) -> {
            return searchNotesOrderByDateASC(
                query = query,
                page = page
            )
        }

        filterAndOrder.contains(ORDER_BY_DESC_TITLE) -> {
            return searchNotesOrderByTitleDESC(
                query = query,
                page = page
            )
        }

        filterAndOrder.contains(ORDER_BY_ASC_TITLE) -> {
            return searchNotesOrderByTitleASC(
                query = query,
                page = page
            )
        }
        else ->
            return searchNotesOrderByDateDESC(
                query = query,
                page = page
            )
    }

}
