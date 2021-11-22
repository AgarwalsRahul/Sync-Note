package com.notesync.notes.framework.dataSource.cache.mappers


import com.notesync.notes.business.domain.model.Note
import com.notesync.notes.business.domain.util.DateUtil
import com.notesync.notes.business.domain.util.EntityMapper
import com.notesync.notes.framework.dataSource.cache.model.NoteCacheEntity
import com.notesync.notes.framework.dataSource.cache.model.TrashNoteCacheEntity
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TrashCacheMapper @Inject constructor(private val dateUtil: DateUtil) :
    EntityMapper<TrashNoteCacheEntity, Note> {

    fun entityListToNoteList(entities: List<TrashNoteCacheEntity>): List<Note> {
        val list: ArrayList<Note> = ArrayList()
        for (entity in entities) {
            list.add(mapFromEntity(entity,null))
        }
        return list
    }

    fun noteListToEntityList(notes: List<Note>): List<TrashNoteCacheEntity> {
        val entities: ArrayList<TrashNoteCacheEntity> = ArrayList()
        for (note in notes) {
            entities.add(mapToEntity(note,null))
        }
        return entities
    }

    override fun mapFromEntity(entity: TrashNoteCacheEntity,key:String?): Note {
        return Note(
            id = entity.id,
            title = entity.title,
            body = entity.body,
            updated_at = entity.updated_at,
            created_at = entity.created_at,
            device_id = entity.device_id
        )
    }

    override fun mapToEntity(domainModel: Note,key:String?): TrashNoteCacheEntity {
        return TrashNoteCacheEntity(
            id = domainModel.id,
            title = domainModel.title,
            body = domainModel.body,
            updated_at = domainModel.updated_at,
            created_at = domainModel.created_at,
            device_id = domainModel.device_id
        )
    }


}